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
package org.eclipse.fennec.search.group;

import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.fennec.model.query.Query;

/**
 * What to group, and how much of each group to bring back (S19, #21).
 * <p>
 * The base is a canonical {@link Query} — the same IR every other read speaks, translated
 * by the same processor — and everything this request adds is the part the IR has no words
 * for: which attribute forms the groups, how many groups to return and how many documents
 * to keep per group.
 *
 * @author Data In Motion Consulting
 */
public final class GroupRequest {

	private final Query query;
	private EAttribute by;
	private int representatives = 1;
	private int topGroups = 10;
	private Map<String, Object> parameters = Map.of();

	private GroupRequest(Query query) {
		this.query = query;
	}

	/** Groups the matches of this query; the query names the root type as everywhere else. */
	public static GroupRequest over(Query query) {
		return new GroupRequest(Objects.requireNonNull(query, "query"));
	}

	/**
	 * The attribute whose value forms the groups. It must be a keyword projection carrying
	 * doc values and hold at most one value per object — a document with two values would
	 * belong in two groups, and picking one silently is the kind of answer this backend does
	 * not give.
	 */
	public GroupRequest by(EAttribute attribute) {
		this.by = Objects.requireNonNull(attribute, "attribute");
		return this;
	}

	/** How many documents to bring back per group, best first. At least one; default one. */
	public GroupRequest representatives(int count) {
		this.representatives = Math.max(1, count);
		return this;
	}

	/** How many groups to return, best group first. At least one; default ten. */
	public GroupRequest topGroups(int count) {
		this.topGroups = Math.max(1, count);
		return this;
	}

	/** Values for the base query's declared parameters. */
	public GroupRequest parameters(Map<String, Object> parameters) {
		this.parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
		return this;
	}

	public Query query() {
		return query;
	}

	public EAttribute by() {
		return by;
	}

	public int representatives() {
		return representatives;
	}

	public int topGroups() {
		return topGroups;
	}

	public Map<String, Object> parameters() {
		return parameters;
	}
}
