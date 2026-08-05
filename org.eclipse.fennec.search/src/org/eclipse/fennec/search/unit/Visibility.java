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
package org.eclipse.fennec.search.unit;

/**
 * What a searcher of this unit is able to see — the axis that decides which kind of
 * {@link org.apache.lucene.search.SearcherManager} is built, and therefore the honest
 * answer to "will I see my own write".
 *
 * @author Data In Motion Consulting
 */
public enum Visibility {

	/**
	 * Near-real-time: the searcher is opened from the {@code IndexWriter}, so a refresh
	 * exposes writes that have not been committed yet. Costs a reopen per refresh and
	 * gives the lowest latency between writing and finding.
	 */
	NRT,

	/**
	 * Committed only: the searcher is opened from the directory, so a refresh exposes
	 * exactly what has been committed and nothing else. Uncommitted writes stay invisible
	 * no matter how often a caller refreshes — which is what makes commit cadence, not
	 * refresh cadence, the thing that governs visibility here.
	 */
	COMMITTED
}
