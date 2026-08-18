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
package org.eclipse.fennec.search.highlight;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.fennec.model.query.Query;

/**
 * What to highlight: a canonical query whose predicate does the matching, and the analyzed
 * text attributes whose stored values are fragmented around the query's terms.
 *
 * @author Data In Motion Consulting
 */
public final class HighlightRequest {

	private final Query query;
	private final List<EAttribute> fields = new ArrayList<>();
	private Map<String, Object> parameters;
	private int maxPassages = 1;

	private HighlightRequest(Query query) {
		this.query = query;
	}

	/** Highlights the matches of this canonical query. */
	public static HighlightRequest over(Query query) {
		return new HighlightRequest(Objects.requireNonNull(query, "query"));
	}

	/** Adds an analyzed text attribute to highlight; at least one is required. */
	public HighlightRequest field(EAttribute field) {
		fields.add(Objects.requireNonNull(field, "field"));
		return this;
	}

	/** Binds the query's parameters. */
	public HighlightRequest parameters(Map<String, Object> parameters) {
		this.parameters = parameters;
		return this;
	}

	/** How many passages a field's snippet may join; 1 unless said otherwise. */
	public HighlightRequest maxPassages(int maxPassages) {
		if (maxPassages < 1) {
			throw new IllegalArgumentException("maxPassages must be at least 1, was " + maxPassages);
		}
		this.maxPassages = maxPassages;
		return this;
	}

	Query query() {
		return query;
	}

	List<EAttribute> fields() {
		return fields;
	}

	Map<String, Object> parameters() {
		return parameters;
	}

	int passages() {
		return maxPassages;
	}
}
