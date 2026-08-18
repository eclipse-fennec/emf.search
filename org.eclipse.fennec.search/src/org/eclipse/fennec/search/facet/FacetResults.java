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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The counted dimensions of one {@link FacetRequest}, in request order. A dimension that
 * matched nothing is present with no values — absence would be indistinguishable from a
 * typo, and the request already named what it wanted counted.
 *
 * @param dimensions the counted dimensions, in request order
 * @author Data In Motion Consulting
 */
public record FacetResults(List<DimensionCounts> dimensions) {

	/** One dimension's top values, count-descending. */
	public record DimensionCounts(String dimension, List<ValueCount> values) {
		public DimensionCounts {
			Objects.requireNonNull(dimension, "dimension");
			values = List.copyOf(values);
		}
	}

	/** One value and how many matching documents carry it. */
	public record ValueCount(String value, long count) {
	}

	public FacetResults {
		dimensions = List.copyOf(dimensions);
	}

	/** The counts of one dimension, when it was requested. */
	public Optional<DimensionCounts> dimension(String name) {
		return dimensions.stream().filter(d -> d.dimension().equals(name)).findFirst();
	}
}
