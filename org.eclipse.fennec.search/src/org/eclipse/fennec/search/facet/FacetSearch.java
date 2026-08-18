/********************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 ********************************************************************/
package org.eclipse.fennec.search.facet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsCollectorManager;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.eclipse.fennec.persistence.api.ConverterService;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.support.QueryContexts;
import org.eclipse.fennec.search.facet.FacetResults.DimensionCounts;
import org.eclipse.fennec.search.facet.FacetResults.ValueCount;
import org.eclipse.fennec.search.mapping.FacetFields;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappingException;
import org.eclipse.fennec.search.query.LuceneQueryPlan;
import org.eclipse.fennec.search.query.LuceneQueryProcessor;
import org.eclipse.fennec.search.unit.IndexUnit;

/**
 * Facet counting over one index unit — the first of the own APIs next to the persistence
 * contract (docs/search-access.md §6 pattern, decided 2026-08-18): counting values over a
 * match set is not query-IR vocabulary, so it does not pretend to be. The base of every
 * request <em>is</em> the canonical query, translated by the same processor the query path
 * uses, so predicates, quantifiers and refusals behave identically here.
 * <p>
 * Counts come from SortedSet doc values inside the unit's own index (see
 * {@link FacetFields} for why there is no taxonomy side-car), and they count
 * <b>objects</b>: the plan's root filter keeps NESTED children out, exactly as it keeps
 * them out of hits.
 * <p>
 * Plain Java per unit; the OSGi layer publishes one as a service per unit, like suggest.
 *
 * @author Data In Motion Consulting
 */
public final class FacetSearch {

	private final IndexUnit unit;
	private final FacetFields facets;
	private final LuceneQueryProcessor processor;
	private final ConverterService converter;

	private FacetSearch(IndexUnit unit, IndexSchema schema, ConverterService converter) {
		this.unit = unit;
		this.facets = FacetFields.of(schema);
		this.processor = LuceneQueryProcessor.of(schema, unit.config().analyzers().defaultAnalyzer());
		this.converter = converter;
	}

	/**
	 * Facet counting for one unit and its schema.
	 *
	 * @throws MappingException if the mapping declares no facet dimension at all — a
	 *         counting API over a mapping that counts nothing is a configuration mistake,
	 *         cheaper caught here than debugged as empty results
	 */
	public static FacetSearch of(IndexUnit unit, IndexSchema schema) {
		return of(unit, schema, null);
	}

	/** As {@link #of(IndexUnit, IndexSchema)}, with a converter for parameter values. */
	public static FacetSearch of(IndexUnit unit, IndexSchema schema, ConverterService converter) {
		Objects.requireNonNull(unit, "unit");
		Objects.requireNonNull(schema, "schema");
		FacetSearch search = new FacetSearch(unit, schema, converter);
		if (search.facets.isEmpty()) {
			throw new MappingException("Unit '" + unit.name() + "' declares no facet dimension, so there "
					+ "is nothing this API could count. Declare a FacetMapping on a field first.");
		}
		return search;
	}

	/**
	 * Counts the request's dimensions over the base query's matches.
	 *
	 * @throws QueryException if the base query is refused, or a dimension is not declared
	 */
	public FacetResults count(FacetRequest request) throws IOException, QueryException {
		Objects.requireNonNull(request, "request");
		if (request.dimensions().isEmpty()) {
			throw new QueryException("The request names no dimension to count. Requesting nothing would "
					+ "have to answer nothing — name at least one.");
		}
		for (String dimension : request.dimensions()) {
			facets.dimension(dimension);
		}
		LuceneQueryPlan plan = (LuceneQueryPlan) processor.translate(request.query(),
				QueryContexts.of(request.query().getFrom(), converter, request.parameters(), null));
		Query base = plan.query();
		if (!request.drillDownSelections().isEmpty()) {
			DrillDownQuery drillDown = new DrillDownQuery(facets.config(), base);
			for (Map.Entry<String, String> selection : request.drillDownSelections().entrySet()) {
				facets.dimension(selection.getKey());
				drillDown.add(selection.getKey(), selection.getValue());
			}
			base = drillDown;
		}
		Query query = base;
		return unit.search(searcher -> {
			FacetsCollector collected = FacetsCollectorManager
					.search(searcher, query, 1, new FacetsCollectorManager()).facetsCollector();
			Facets counts = counts(searcher, collected);
			List<DimensionCounts> dimensions = new ArrayList<>(request.dimensions().size());
			for (String dimension : request.dimensions()) {
				dimensions.add(counts == null
						? new DimensionCounts(dimension, List.of())
						: dimensionCounts(counts, dimension, request.top()));
			}
			return new FacetResults(dimensions);
		});
	}

	/** Null when the index holds no facet field yet — an empty index has empty counts. */
	private Facets counts(IndexSearcher searcher, FacetsCollector collected)
			throws IOException {
		SortedSetDocValuesReaderState state;
		try {
			state = new DefaultSortedSetDocValuesReaderState(searcher.getIndexReader(), facets.config());
		} catch (IllegalArgumentException noFacetFieldIndexedYet) {
			return null;
		}
		return new SortedSetDocValuesFacetCounts(state, collected);
	}

	private DimensionCounts dimensionCounts(Facets counts, String dimension, int topN) throws IOException {
		FacetResult result = counts.getTopChildren(topN, dimension);
		if (result == null) {
			return new DimensionCounts(dimension, List.of());
		}
		List<ValueCount> values = new ArrayList<>(result.labelValues.length);
		for (LabelAndValue labelValue : result.labelValues) {
			values.add(new ValueCount(labelValue.label, labelValue.value.longValue()));
		}
		return new DimensionCounts(dimension, values);
	}
}
