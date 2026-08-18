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
package org.eclipse.fennec.search.query;

import java.util.List;
import java.util.Objects;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.fennec.persistence.query.api.QueryPlan;
import org.eclipse.fennec.persistence.query.api.QueryShape;

/**
 * A canonical query translated into Lucene: one {@link Query}, an optional {@link Sort},
 * and the paging window.
 * <p>
 * The plan is what {@code translate} produces and the resource executes; it holds no
 * searcher and touches no index, so it can be built, inspected and asserted on without
 * an open unit. That is deliberate — the translation is the part with the semantics in
 * it (§5.1), and it deserves tests that do not need a directory.
 *
 * @author Data In Motion Consulting
 */
public final class LuceneQueryPlan implements QueryPlan {

	/**
	 * The honest group-by subset (S7, #11): one group key that is a declared facet
	 * dimension, one COUNT — answered from the same SortedSet doc values the facet API
	 * counts.
	 *
	 * @param dimension the facet dimension the key resolves to
	 * @param key the group key attribute, for converting labels back to EMF values
	 * @param keyAlias the key column's name in the rows (the feature name)
	 * @param countAlias the count column's name (the aggregate's alias)
	 */
	public record Aggregation(String dimension, EAttribute key, String keyAlias,
			String countAlias) {
	}

	private final org.eclipse.fennec.model.query.Query source;
	private final QueryShape shape;
	private final Query query;
	private final Sort sort;
	private final int skip;
	private final int limit;
	private final List<String> rowFields;
	private final List<String> rowAliases;
	private final Aggregation aggregation;
	private final boolean withScores;

	LuceneQueryPlan(org.eclipse.fennec.model.query.Query source, QueryShape shape, Query query, Sort sort,
			int skip, int limit, List<String> rowFields, List<String> rowAliases) {
		this(source, shape, query, sort, skip, limit, rowFields, rowAliases, null);
	}

	LuceneQueryPlan(org.eclipse.fennec.model.query.Query source, QueryShape shape, Query query, Sort sort,
			int skip, int limit, List<String> rowFields, List<String> rowAliases, Aggregation aggregation) {
		this.source = Objects.requireNonNull(source, "source");
		this.shape = Objects.requireNonNull(shape, "shape");
		this.query = Objects.requireNonNull(query, "query");
		this.sort = sort;
		this.skip = skip;
		this.limit = limit;
		this.rowFields = rowFields == null ? List.of() : List.copyOf(rowFields);
		this.rowAliases = rowAliases == null ? List.of() : List.copyOf(rowAliases);
		this.aggregation = aggregation;
		this.withScores = source.isWithScores();
	}

	/** Whether the envelope asked for scored hits (emf.persistence-jpa#165). */
	public boolean withScores() {
		return withScores;
	}

	@Override
	public org.eclipse.fennec.model.query.Query source() {
		return source;
	}

	@Override
	public QueryShape shape() {
		return shape;
	}

	/**
	 * The Lucene query, including the guards every plan carries: the root-document marker
	 * (so the children of a block never count as hits) and the type filter derived from
	 * the query's {@code from} class.
	 */
	public Query query() {
		return query;
	}

	/** The sort to apply, or null for index order (relevance is S6). */
	public Sort sort() {
		return sort;
	}

	/** How many leading hits to discard; 0 for none. */
	public int skip() {
		return skip;
	}

	/** The maximum number of hits to return, or -1 for unbounded. */
	public int limit() {
		return limit;
	}

	/** For {@link QueryShape#PROJECTION}: the stored fields each row reads, in order. */
	public List<String> rowFields() {
		return rowFields;
	}

	/** For {@link QueryShape#PROJECTION}: the column name of each row field, in order. */
	public List<String> rowAliases() {
		return rowAliases;
	}

	/** For {@link QueryShape#AGGREGATION}: the group-by subset; null otherwise. */
	public Aggregation aggregation() {
		return aggregation;
	}

	@Override
	public String toString() {
		return "LuceneQueryPlan[shape=" + shape + ", query=" + query + ", sort=" + sort + ", skip=" + skip
				+ ", limit=" + limit + "]";
	}
}
