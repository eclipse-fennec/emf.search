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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The backend options this engine reads from the query call — {@code QueryContext.options()},
 * the extension point the persistence contract provides for exactly this: knobs that belong
 * to one engine and have no place in the shared query IR.
 * <p>
 * There is one so far, and its shape is the point of §5.3: a consumer <b>selects declared
 * rank signals by name</b>, it never sends a scoring formula. Everything about how a signal
 * behaves — the saturating function, its pivot, its weight — is declared in the mapping
 * model, where a relevance decision can be reviewed, versioned and reasoned about against
 * the corpus. That is what keeps the refusal of arithmetic pushdown meaningful: opening a
 * formula channel here would reintroduce per-document arithmetic through the back door.
 *
 * @author Data In Motion Consulting
 */
public final class SearchOptions {

	/**
	 * Selects the rank signals folded into this query's score (S14, #16). The value is a
	 * single signal name or a {@code Collection} of them; a name no mapping reachable from
	 * the query's root type declares is refused, with the declared names in the message.
	 * <p>
	 * Signals shape the score and nothing else, so they show up in relevance order and in a
	 * projected score column. A query that sorts by a field reads no score, and one that
	 * only counts has none — the latter is refused rather than quietly ignored.
	 */
	public static final String RANK_SIGNALS = "search.rank.signals";

	private SearchOptions() {
	}

	/**
	 * The rank signals selected in these options, in the order given; empty when none are.
	 *
	 * @param options the query options, may be {@code null}
	 * @return the selected signal names, never {@code null}
	 * @throws IllegalArgumentException if the value is neither a name nor a collection of
	 *         names — a silently ignored option is worse than a refused one
	 */
	public static List<String> rankSignals(Map<?, ?> options) {
		Object value = options == null ? null : options.get(RANK_SIGNALS);
		if (value == null) {
			return List.of();
		}
		List<String> names = new ArrayList<>();
		if (value instanceof Collection<?> collection) {
			for (Object element : collection) {
				names.add(nameOf(element));
			}
		} else {
			names.add(nameOf(value));
		}
		return List.copyOf(names);
	}

	private static String nameOf(Object value) {
		if (value instanceof String name && !name.isBlank()) {
			return name;
		}
		throw new IllegalArgumentException("Option '" + RANK_SIGNALS + "' names declared rank signals: "
				+ "a String or a Collection of Strings, not '" + value + "'.");
	}
}
