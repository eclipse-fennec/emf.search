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

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.SortedSetSelector;
import org.apache.lucene.search.grouping.GroupSelector;
import org.apache.lucene.search.grouping.SearchGroup;
import org.apache.lucene.util.BytesRef;

/**
 * Groups by a keyword field of this backend's own writing.
 * <p>
 * Lucene's {@code TermGroupSelector} reads {@code SORTED} doc values, and every keyword
 * field here is written as {@code SORTED_SET} — the shape a many-valued attribute needs and
 * the one the keyword sort already reads. So the values are wrapped rather than the mapping
 * bent: {@link SortedSetSelector} exposes a sorted view of the set, and the group key is
 * unambiguous because {@link GroupSearch} refuses a many-valued attribute in the first
 * place. A document without a value for the field belongs to no group and is skipped —
 * "everything else" is not a group anyone asked for.
 *
 * @author Data In Motion Consulting
 */
final class KeywordGroupSelector extends GroupSelector<BytesRef> {

	private final String field;

	private SortedDocValues values;
	private BytesRef current;
	/** The groups the second pass collects; null while the first pass is still finding them. */
	private Set<BytesRef> selected;

	KeywordGroupSelector(String field) {
		this.field = field;
	}

	@Override
	public void setNextReader(LeafReaderContext context) throws IOException {
		SortedSetDocValues set = DocValues.getSortedSet(context.reader(), field);
		this.values = SortedSetSelector.wrap(set, SortedSetSelector.Type.MIN);
		this.current = null;
	}

	@Override
	public void setScorer(Scorable scorer) throws IOException {
		// The group key is a stored value; nothing here depends on the score.
	}

	@Override
	public State advanceTo(int doc) throws IOException {
		if (!values.advanceExact(doc)) {
			current = null;
			return State.SKIP;
		}
		current = values.lookupOrd(values.ordValue());
		return selected == null || selected.contains(current) ? State.ACCEPT : State.SKIP;
	}

	@Override
	public BytesRef currentValue() {
		return current;
	}

	@Override
	public BytesRef copyValue() {
		return current == null ? null : BytesRef.deepCopyOf(current);
	}

	@Override
	public void setGroups(Collection<SearchGroup<BytesRef>> groups) {
		Set<BytesRef> wanted = new HashSet<>();
		for (SearchGroup<BytesRef> group : groups) {
			if (group.groupValue != null) {
				wanted.add(BytesRef.deepCopyOf(group.groupValue));
			}
		}
		this.selected = wanted;
	}
}
