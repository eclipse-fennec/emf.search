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
 * When writes become visible to searchers.
 * <p>
 * This is where the honest limit of the standalone role is configured: visibility is
 * near-real-time, not read-your-writes. A caller that has just written and immediately
 * searches may not see its own write unless it refreshes first.
 *
 * @param mode     how refreshes are triggered
 * @param interval for {@link Mode#NEAR_REAL_TIME}, the longest a write may stay invisible;
 *                 ignored otherwise
 * @author Data In Motion Consulting
 */
public record RefreshPolicy(Mode mode, Duration interval) {

	/** How searcher refreshes are triggered. */
	public enum Mode {
		/** A background thread reopens the searcher within {@link RefreshPolicy#interval()}. */
		NEAR_REAL_TIME,
		/** The searcher is reopened when the writer commits. */
		ON_COMMIT,
		/** Only {@link IndexUnit#refresh()} reopens the searcher — deterministic, for tests and bulk loads. */
		MANUAL
	}

	/** Default staleness bound for {@link Mode#NEAR_REAL_TIME}. */
	public static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(1);

	public RefreshPolicy {
		Objects.requireNonNull(mode, "mode");
		Objects.requireNonNull(interval, "interval");
		if (mode == Mode.NEAR_REAL_TIME && (interval.isZero() || interval.isNegative())) {
			throw new IllegalArgumentException("NEAR_REAL_TIME needs a positive interval, got " + interval);
		}
	}

	/** Near-real-time with the {@link #DEFAULT_INTERVAL}. */
	public static RefreshPolicy nearRealTime() {
		return nearRealTime(DEFAULT_INTERVAL);
	}

	/** Near-real-time with an explicit staleness bound. */
	public static RefreshPolicy nearRealTime(Duration interval) {
		return new RefreshPolicy(Mode.NEAR_REAL_TIME, interval);
	}

	/** Refresh only when the writer commits. */
	public static RefreshPolicy onCommit() {
		return new RefreshPolicy(Mode.ON_COMMIT, DEFAULT_INTERVAL);
	}

	/** Refresh only on an explicit {@link IndexUnit#refresh()}. */
	public static RefreshPolicy manual() {
		return new RefreshPolicy(Mode.MANUAL, DEFAULT_INTERVAL);
	}
}
