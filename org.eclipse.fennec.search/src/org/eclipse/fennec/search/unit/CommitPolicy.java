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

import java.time.Duration;
import java.util.Objects;

/**
 * When the writer commits to disk.
 * <p>
 * Commits are also where a stream position can be recorded in the commit data, so the
 * cadence configured here decides how much replay a crash costs a stream-fed index.
 *
 * @param maxUncommittedDocuments commit once this many writes are uncommitted; {@code 0} disables the trigger
 * @param maxInterval             commit at most this long after the previous commit;
 *                                {@link Duration#ZERO} disables the trigger
 * @param commitOnClose           commit when the unit is closed
 * @author Data In Motion Consulting
 */
public record CommitPolicy(int maxUncommittedDocuments, Duration maxInterval, boolean commitOnClose) {

	public CommitPolicy {
		Objects.requireNonNull(maxInterval, "maxInterval");
		if (maxUncommittedDocuments < 0) {
			throw new IllegalArgumentException("maxUncommittedDocuments must not be negative, got "
					+ maxUncommittedDocuments);
		}
		if (maxInterval.isNegative()) {
			throw new IllegalArgumentException("maxInterval must not be negative, got " + maxInterval);
		}
	}

	/** Commit only when the unit is closed — the default. */
	public static CommitPolicy onClose() {
		return new CommitPolicy(0, Duration.ZERO, true);
	}

	/** Commit every {@code count} writes, and on close. */
	public static CommitPolicy afterDocuments(int count) {
		return new CommitPolicy(count, Duration.ZERO, true);
	}

	/** Commit on an interval, and on close. */
	public static CommitPolicy afterInterval(Duration interval) {
		return new CommitPolicy(0, interval, true);
	}

	/** Whether any automatic trigger is configured. */
	public boolean hasDocumentTrigger() {
		return maxUncommittedDocuments > 0;
	}

	/** Whether an interval trigger is configured. */
	public boolean hasIntervalTrigger() {
		return !maxInterval.isZero();
	}
}
