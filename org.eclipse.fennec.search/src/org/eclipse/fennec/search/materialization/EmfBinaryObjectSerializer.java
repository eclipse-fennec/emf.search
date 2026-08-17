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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * {@code STORED_OBJECT} through EMF's own binary resource format — the default serializer
 * (format id {@code binary}), chosen because it needs no dependency beyond EMF, is compact,
 * and treats external references by the standard rules: serialized as URIs, deserialized
 * as proxies.
 * <p>
 * Serialization works on an {@link EcoreUtil#copy}, because putting the live object into
 * the scratch resource would tear it out of its container — the write must not disturb
 * what it writes. The copy points at the <em>original</em> non-containment targets, so
 * their URIs are the ones the original held.
 *
 * @author Data In Motion Consulting
 */
public final class EmfBinaryObjectSerializer implements ObjectSerializer {

	/** The format id, and the backend default when a mapping declares none. */
	public static final String FORMAT = "binary";

	private static final URI SCRATCH_URI = URI.createURI("search:materialized");

	@Override
	public String format() {
		return FORMAT;
	}

	@Override
	public byte[] serialize(EObject object) throws IOException {
		Resource scratch = new BinaryResourceImpl(SCRATCH_URI);
		scratch.getContents().add(EcoreUtil.copy(object));
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		scratch.save(bytes, Map.of());
		return bytes.toByteArray();
	}

	@Override
	public EObject deserialize(byte[] bytes, EPackage.Registry packages) throws IOException {
		ResourceSetImpl scratchSet = new ResourceSetImpl();
		scratchSet.setPackageRegistry(new EPackageRegistryImpl(packages));
		Resource scratch = new BinaryResourceImpl(SCRATCH_URI);
		scratchSet.getResources().add(scratch);
		scratch.load(new ByteArrayInputStream(bytes), Map.of());
		if (scratch.getContents().isEmpty()) {
			throw new IOException("The stored object deserialized to nothing — the bytes are not a "
					+ "serialized EObject of format '" + FORMAT + "'.");
		}
		// Returning the object detaches it lazily: the caller's resource takes it over the
		// moment it is added there, which is the normal EMF hand-over.
		return scratch.getContents().get(0);
	}
}
