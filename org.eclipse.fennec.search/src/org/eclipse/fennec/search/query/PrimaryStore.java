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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * The primary store behind a secondary index (#41): where the original objects live when a
 * class is mapped with {@code SOURCE_URI} materialization. The index stores the original's
 * URI; this collaborator turns those URIs back into objects — explicitly attached to an
 * {@link IndexSearch}, instead of riding on whatever {@code ResourceSet} happens to be
 * ambient.
 * <p>
 * Resolution is batched: one search window is one call, so a store backed by JPA or Mongo
 * can answer with one query instead of one per hit. A URI the store cannot resolve is
 * simply absent from the answer — the hit then keeps its proxy, and the caller sees exactly
 * what the index knew.
 *
 * @author Data In Motion Consulting
 */
@FunctionalInterface
public interface PrimaryStore {

	/**
	 * Resolves the originals behind the given source URIs, in one batch.
	 *
	 * @param sourceUris the URIs the index stored under {@code SOURCE_URI}, in hit order
	 * @return the resolved objects by their URI; a URI the store cannot answer is absent
	 * @throws IOException if the store itself fails — not for a URI that resolves to nothing
	 */
	Map<URI, EObject> resolve(List<URI> sourceUris) throws IOException;

	/**
	 * A primary store over a {@link ResourceSet} — the explicit form of what the resource
	 * path does implicitly. Every URI resolves through {@code getEObject(uri, true)}; one
	 * that loads nothing stays unresolved rather than failing the batch.
	 */
	static PrimaryStore of(ResourceSet resourceSet) {
		Objects.requireNonNull(resourceSet, "resourceSet");
		return uris -> {
			Map<URI, EObject> resolved = new LinkedHashMap<>();
			for (URI uri : uris) {
				EObject object = resourceSet.getEObject(uri, true);
				if (object != null) {
					resolved.put(uri, object);
				}
			}
			return resolved;
		};
	}
}
