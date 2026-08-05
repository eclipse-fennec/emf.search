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
 * What a unit is allowed to do with its index.
 *
 * @author Data In Motion Consulting
 */
public enum AccessMode {

	/** Reads and writes. The default. */
	READ_WRITE,

	/**
	 * Reads only: every write method refuses.
	 * <p>
	 * The unit still opens an {@link org.apache.lucene.index.IndexWriter} — a deliberate
	 * simplification, since one code path is easier to trust than two, and the writer is
	 * also what a near-real-time searcher is built from. The consequence is not hidden: a
	 * read-only unit still takes the directory's {@code write.lock}, so it cannot open an
	 * index on a write-protected filesystem, nor one another process is currently writing.
	 * Both cases fail at {@link IndexUnit#open(IndexUnitConfig)} with a message that says
	 * so rather than with a bare lock exception.
	 */
	READ_ONLY,

	/**
	 * Writes only — no searcher is opened and no reopen thread runs, so nothing pays for
	 * visibility while an index is being built. Searching refuses; reopen the unit in
	 * another mode when the load is done.
	 */
	BULK_LOAD;

	/** Whether write operations are permitted. */
	public boolean allowsWrites() {
		return this != READ_ONLY;
	}

	/** Whether a searcher is opened at all. */
	public boolean allowsSearch() {
		return this != BULK_LOAD;
	}
}
