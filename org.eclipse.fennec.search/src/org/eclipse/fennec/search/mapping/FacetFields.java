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
package org.eclipse.fennec.search.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.search.IndexSearcher;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.FacetKind;
import org.eclipse.fennec.search.esearch.FacetMapping;
import org.eclipse.fennec.search.esearch.FieldMapping;

/**
 * The facet dimensions a mapping declares, derived once and shared by both directions —
 * the {@link DocumentMapper} writes facet fields through it, the facet counting reads with
 * the same {@link FacetsConfig} — so writer and counter cannot disagree about a dimension
 * (the same single-source doctrine as {@link IndexSchema} for fields).
 * <p>
 * SortedSet doc values only (S7, #11): SSDV facets live inside the unit's own index, so
 * they inherit its lifecycle, commits and NRT visibility. A taxonomy facet would bring a
 * side-car index with its own lifecycle to keep in step — declared refusable until a real
 * hierarchy depth asks for it, and {@code hierarchical} with it: an attribute value is one
 * path component, so a hierarchy has nothing to be built from yet.
 *
 * @author Data In Motion Consulting
 */
public final class FacetFields {

	/** One declared dimension. */
	public record Dimension(String name, EAttribute attribute, boolean multiValued) {
	}

	private final Map<String, Dimension> dimensions;
	private final FacetsConfig config;

	private FacetFields(Map<String, Dimension> dimensions) {
		this.dimensions = dimensions;
		this.config = new FacetsConfig();
		for (Dimension dimension : dimensions.values()) {
			config.setMultiValued(dimension.name(), dimension.multiValued());
		}
	}

	/** Derives the declared dimensions of a schema's mapping; empty when none declare one. */
	public static FacetFields of(IndexSchema schema) {
		Objects.requireNonNull(schema, "schema");
		Map<String, Dimension> dimensions = new LinkedHashMap<>();
		for (DocumentMapping document : schema.mapping().getDocuments()) {
			for (FieldMapping field : document.getFields()) {
				FacetMapping facet = field.getFacet();
				if (facet == null) {
					continue;
				}
				if (!(field.getFeature() instanceof EAttribute attribute)) {
					throw new MappingException("A facet on a field mapping without an attribute has no "
							+ "values to count.");
				}
				if (facet.getKind() == FacetKind.TAXONOMY) {
					throw new MappingException("Facet dimension on '" + attribute.getName() + "' declares "
							+ "TAXONOMY, which needs a side-car taxonomy index with its own lifecycle — "
							+ "not implemented until a real hierarchy asks for it (#11). SORTED_SET counts "
							+ "from inside the unit's index.");
				}
				if (facet.isHierarchical()) {
					throw new MappingException("Facet dimension on '" + attribute.getName() + "' declares "
							+ "hierarchical, but an attribute value is a single path component — there is "
							+ "no hierarchy to build yet (#11).");
				}
				String name = dimensionName(schema, field, attribute);
				boolean multiValued = facet.isMultiValued() || attribute.isMany();
				Dimension previous = dimensions.get(name);
				if (previous != null && previous.attribute() != attribute) {
					// Two classes may share a dimension (one drill-down over both), but then
					// the wider multi-valuedness wins so the config fits both writers.
					multiValued = multiValued || previous.multiValued();
				}
				dimensions.put(name, new Dimension(name, attribute, multiValued));
			}
		}
		return new FacetFields(dimensions);
	}

	/**
	 * The dimension name a declaration carries: the declared name, or the field's
	 * effective name — one rule, used by derivation, writing and the group-by subset.
	 */
	public static String dimensionName(IndexSchema schema, FieldMapping field, EAttribute attribute) {
		FacetMapping facet = field.getFacet();
		return facet.getDimension() == null || facet.getDimension().isBlank()
				? schema.fieldName(attribute, field)
				: facet.getDimension();
	}

	/** Whether the mapping declares any dimension at all. */
	public boolean isEmpty() {
		return dimensions.isEmpty();
	}

	/** The shared read/write configuration. */
	public FacetsConfig config() {
		return config;
	}

	/** The declared dimension, refused by name when there is none. */
	public Dimension dimension(String name) {
		Dimension dimension = dimensions.get(name);
		if (dimension == null) {
			throw new MappingException("No facet dimension '" + name + "' is declared"
					+ (dimensions.isEmpty() ? " — this mapping declares none."
							: "; declared: " + dimensions.keySet() + "."));
		}
		return dimension;
	}

	/** Adds one facet value for a declared field; the mapper calls this per value. */
	void add(Document document, FieldMapping field, IndexSchema schema, EAttribute attribute, String value) {
		document.add(new SortedSetDocValuesFacetField(dimensionName(schema, field, attribute), value));
	}

	/** One value of a dimension and how many matching objects carry it. */
	public record Count(String value, long count) {
	}

	/**
	 * Every value of one dimension with its count over the collected matches —
	 * count-descending, ties by value, so rows are deterministic. Empty when the index
	 * holds no facet field yet or the dimension was never written: an empty index has
	 * empty counts, not an error.
	 */
	public List<Count> countAll(IndexSearcher searcher, FacetsCollector collected, String dimension)
			throws IOException {
		SortedSetDocValuesReaderState state;
		try {
			state = new DefaultSortedSetDocValuesReaderState(searcher.getIndexReader(), config);
		} catch (IllegalArgumentException noFacetFieldIndexedYet) {
			return List.of();
		}
		FacetResult all;
		try {
			all = new SortedSetDocValuesFacetCounts(state, collected).getAllChildren(dimension);
		} catch (IllegalArgumentException dimensionNeverWritten) {
			return List.of();
		}
		if (all == null) {
			return List.of();
		}
		List<Count> counts = new ArrayList<>(all.labelValues.length);
		for (LabelAndValue labelValue : all.labelValues) {
			counts.add(new Count(labelValue.label, labelValue.value.longValue()));
		}
		counts.sort(Comparator.comparingLong(Count::count).reversed().thenComparing(Count::value));
		return counts;
	}

	/**
	 * Runs a document through the facet configuration — Lucene's required build step that
	 * turns declared facet fields into their indexable form. Identity when the mapping
	 * declares no dimension.
	 */
	Document build(Document document) {
		if (dimensions.isEmpty()) {
			return document;
		}
		try {
			return config.build(document);
		} catch (IOException e) {
			throw new MappingException("Building the facet fields failed: " + e.getMessage(), e);
		}
	}
}
