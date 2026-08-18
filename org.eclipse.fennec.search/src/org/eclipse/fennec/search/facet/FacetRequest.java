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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.fennec.model.query.Query;

/**
 * What to count: a canonical query as the base, the dimensions to count over its matches,
 * and optional drill-down selections that narrow the base — one selected value per call to
 * {@link #drillDown}, several calls for several dimensions.
 * <p>
 * Built fluently and immutable once handed to {@link FacetSearch#count}; the base query
 * decides the type, the predicate and — through the shared processor — everything the
 * query path can already say.
 *
 * @author Data In Motion Consulting
 */
public final class FacetRequest {

	private final Query query;
	private final List<String> dimensions = new ArrayList<>();
	private final Map<String, String> drillDown = new LinkedHashMap<>();
	private Map<String, Object> parameters;
	private int topN = 10;

	private FacetRequest(Query query) {
		this.query = query;
	}

	/** Counts over the matches of this canonical query. */
	public static FacetRequest over(Query query) {
		return new FacetRequest(Objects.requireNonNull(query, "query"));
	}

	/** Adds a dimension to count; at least one is required. */
	public FacetRequest dimension(String dimension) {
		dimensions.add(Objects.requireNonNull(dimension, "dimension"));
		return this;
	}

	/** Narrows the base to matches carrying this dimension value. */
	public FacetRequest drillDown(String dimension, String value) {
		drillDown.put(Objects.requireNonNull(dimension, "dimension"),
				Objects.requireNonNull(value, "value"));
		return this;
	}

	/** Binds the base query's parameters. */
	public FacetRequest parameters(Map<String, Object> parameters) {
		this.parameters = parameters;
		return this;
	}

	/** How many top values per dimension; 10 unless said otherwise. */
	public FacetRequest topN(int topN) {
		if (topN < 1) {
			throw new IllegalArgumentException("topN must be at least 1, was " + topN);
		}
		this.topN = topN;
		return this;
	}

	Query query() {
		return query;
	}

	List<String> dimensions() {
		return dimensions;
	}

	Map<String, String> drillDownSelections() {
		return drillDown;
	}

	Map<String, Object> parameters() {
		return parameters;
	}

	int top() {
		return topN;
	}
}
