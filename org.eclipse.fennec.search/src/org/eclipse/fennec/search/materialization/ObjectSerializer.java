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
package org.eclipse.fennec.search.materialization;

import java.io.IOException;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

/**
 * One way of turning an EObject tree into bytes and back — the pluggable half of
 * {@code STORED_OBJECT} materialization (docs/search-access.md §4.3).
 * <p>
 * The mapping selects an implementation by {@link #format()} id; the id is written into
 * nothing, so <b>changing a mapping's format invalidates the stored objects written with
 * the previous one</b> — the metamodel documentation says so, and nothing here softens it.
 * <p>
 * Contract: {@code deserialize(serialize(o), packages)} is the whole tree, containment
 * included; references leaving the tree come back as EMF proxies carrying the target's
 * URI, to be resolved by whatever {@code ResourceSet} the caller attaches the object to.
 * Serialization must leave the live object untouched — no moving it into a scratch
 * resource.
 *
 * @author Data In Motion Consulting
 */
public interface ObjectSerializer {

	/** The id the mapping's {@code format} attribute selects this serializer by. */
	String format();

	/**
	 * The object tree as bytes. The object itself is left exactly as it was — same
	 * container, same resource.
	 */
	byte[] serialize(EObject object) throws IOException;

	/**
	 * The object tree back from bytes.
	 *
	 * @param packages resolves the EPackages of the serialized objects; deserialization
	 *        must not fall back to the global registry behind the caller's back
	 */
	EObject deserialize(byte[] bytes, EPackage.Registry packages) throws IOException;
}
