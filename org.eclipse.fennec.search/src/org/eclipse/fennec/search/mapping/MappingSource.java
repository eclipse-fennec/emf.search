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
package org.eclipse.fennec.search.mapping;

import java.util.Optional;

import org.eclipse.fennec.search.esearch.IndexUnitMapping;

/**
 * "Give me the mapping for unit <em>X</em>" — the missing piece between the metamodel
 * (S2) and the mapper (S4): nothing else owns getting a mapping to a unit at runtime
 * (S23, #32).
 * <p>
 * Deliveries are pluggable and composable ({@link MappingSources#withPrecedence}): the
 * EObject registry carries authored {@code *.esearch} documents, the metadata aspect
 * plane carries what a model bundle ships with itself. A source answers by <b>unit
 * name</b> — the name inside the mapping is the truth, not the key a delivery happened to
 * file it under.
 * <p>
 * Resolution is the moment configuration mistakes surface: a consumer turns the answer
 * into an {@link IndexSchema} immediately (which validates the mapping as a whole), not
 * on the first document that happens to hit the problem.
 *
 * @author Data In Motion Consulting
 */
public interface MappingSource {

	/** The mapping declaring this unit name, when this source carries one. */
	Optional<IndexUnitMapping> mappingFor(String unit);

	/**
	 * Observes mapping changes. The default is the fixed source that cannot change; a
	 * live source (the registry) overrides both methods.
	 */
	default void addListener(MappingListener listener) {
	}

	default void removeListener(MappingListener listener) {
	}

	/** Notification that a unit's mapping appeared, changed or disappeared. */
	interface MappingListener {

		void mappingChanged(String unit);
	}
}
