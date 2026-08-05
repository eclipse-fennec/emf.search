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
 * When the searcher is reopened — deliberately separate from {@link Visibility}, which
 * decides what a reopened searcher is able to see. Mixing the two is how "refresh on
 * commit" quietly turns into "sees uncommitted data as soon as anyone refreshes".
 *
 * @param mode     what triggers a reopen
 * @param interval for {@link Mode#BACKGROUND}, the longest a change may stay invisible;
 *                 ignored otherwise
 * @author Data In Motion Consulting
 */
public record RefreshTrigger(Mode mode, Duration interval) {

	/** What triggers a reopen. */
	public enum Mode {
		/** A background thread reopens within {@link RefreshTrigger#interval()}. */
		BACKGROUND,
		/** The searcher is reopened when the unit commits. */
		ON_COMMIT,
		/** Only {@link IndexUnit#refresh()} reopens — deterministic, for tests and bulk work. */
		MANUAL
	}

	/** Default staleness bound for {@link Mode#BACKGROUND}. */
	public static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(1);

	public RefreshTrigger {
		Objects.requireNonNull(mode, "mode");
		Objects.requireNonNull(interval, "interval");
		if (mode == Mode.BACKGROUND && (interval.isZero() || interval.isNegative())) {
			throw new IllegalArgumentException("BACKGROUND needs a positive interval, got " + interval);
		}
	}

	/** Reopen in the background with the {@link #DEFAULT_INTERVAL}. */
	public static RefreshTrigger background() {
		return background(DEFAULT_INTERVAL);
	}

	/** Reopen in the background within the given bound. */
	public static RefreshTrigger background(Duration interval) {
		return new RefreshTrigger(Mode.BACKGROUND, interval);
	}

	/** Reopen when the unit commits. */
	public static RefreshTrigger onCommit() {
		return new RefreshTrigger(Mode.ON_COMMIT, DEFAULT_INTERVAL);
	}

	/** Reopen only on an explicit {@link IndexUnit#refresh()}. */
	public static RefreshTrigger manual() {
		return new RefreshTrigger(Mode.MANUAL, DEFAULT_INTERVAL);
	}
}
