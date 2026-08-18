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
package org.eclipse.fennec.search.similarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

/**
 * Whose neighbours to find: an already-indexed anchor object, and the analyzed text
 * attributes whose term statistics define what "similar" means.
 * <p>
 * The frequency knobs default to 1 so similarity answers on any corpus size; Lucene's own
 * defaults (2/5) assume a corpus large enough that rare terms are noise — raise them for
 * precision on large indexes.
 *
 * @author Data In Motion Consulting
 */
public final class SimilarityRequest {

	private final EObject anchor;
	private final List<EAttribute> fields = new ArrayList<>();
	private int maxHits = 10;
	private int minTermFreq = 1;
	private int minDocFreq = 1;

	private SimilarityRequest(EObject anchor) {
		this.anchor = anchor;
	}

	/** Finds objects similar to this already-indexed anchor. */
	public static SimilarityRequest to(EObject anchor) {
		return new SimilarityRequest(Objects.requireNonNull(anchor, "anchor"));
	}

	/** Adds an analyzed text attribute to the term statistics; at least one is required. */
	public SimilarityRequest field(EAttribute field) {
		fields.add(Objects.requireNonNull(field, "field"));
		return this;
	}

	/** How many neighbours to return; 10 unless said otherwise. */
	public SimilarityRequest maxHits(int maxHits) {
		if (maxHits < 1) {
			throw new IllegalArgumentException("maxHits must be at least 1, was " + maxHits);
		}
		this.maxHits = maxHits;
		return this;
	}

	/** How often a term must occur in the anchor to count; 1 unless said otherwise. */
	public SimilarityRequest minTermFreq(int minTermFreq) {
		if (minTermFreq < 1) {
			throw new IllegalArgumentException("minTermFreq must be at least 1, was " + minTermFreq);
		}
		this.minTermFreq = minTermFreq;
		return this;
	}

	/** In how many documents a term must occur to count; 1 unless said otherwise. */
	public SimilarityRequest minDocFreq(int minDocFreq) {
		if (minDocFreq < 1) {
			throw new IllegalArgumentException("minDocFreq must be at least 1, was " + minDocFreq);
		}
		this.minDocFreq = minDocFreq;
		return this;
	}

	EObject anchor() {
		return anchor;
	}

	List<EAttribute> fields() {
		return fields;
	}

	int hits() {
		return maxHits;
	}

	int termFreq() {
		return minTermFreq;
	}

	int docFreq() {
		return minDocFreq;
	}
}
