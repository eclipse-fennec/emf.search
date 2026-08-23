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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;

/**
 * The groups a {@link GroupSearch} found: each with its key, how many objects it holds and
 * the representatives asked for (S19, #21).
 * <p>
 * This is deliberately <b>not</b> a {@code QueryResult}. A grouped answer is neither the
 * objects nor the rows the persistence contract knows, and dressing it as either would
 * misreport what it is — the shape the query IR has no words for is exactly the shape this
 * type exists to carry.
 *
 * @param groups the groups, best group first
 * @param totalGroups how many groups the match set has in total, which is more than
 *        {@code groups.size()} whenever the request asked for fewer
 * @author Data In Motion Consulting
 */
public record GroupResults(List<Group> groups, long totalGroups) {

	/**
	 * One group.
	 *
	 * @param key the group value, as the indexed keyword reads
	 * @param totalHits how many objects of the match set fall into this group — the whole
	 *        group, not the number of representatives returned
	 * @param representatives the objects asked for, best match first, reconstructed like
	 *        every other read (docs/search-access.md §4.3)
	 */
	public record Group(String key, long totalHits, List<EObject> representatives) {

		public Group {
			Objects.requireNonNull(key, "key");
			representatives = List.copyOf(representatives);
		}
	}

	public GroupResults {
		groups = List.copyOf(groups);
	}

	/** The group under this key, if the answer holds one. */
	public Optional<Group> group(String key) {
		return groups.stream().filter(group -> group.key().equals(key)).findFirst();
	}
}
