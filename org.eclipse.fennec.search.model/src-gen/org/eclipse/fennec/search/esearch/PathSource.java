/*
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
 */
package org.eclipse.fennec.search.esearch;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EStructuralFeature;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Path Source</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Rung two: a value reached by navigating a chain of features (address.city), the last of which is an attribute. Still statically verifiable — every segment is checked against the metamodel when the mapping is read.
 * 
 * A path that crosses a reference makes this document depend on another object's state: it is recomputed when the owner is saved, and a change to the referenced object does not refresh it. That is the same exposure EMBED and NESTED carry, and it is why the path is declared rather than hidden in an expression.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.PathSource#getSegments <em>Segments</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getPathSource()
 * @model
 * @generated
 */
@ProviderType
public interface PathSource extends ValueSource {
	/**
	 * Returns the value of the '<em><b>Segments</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EStructuralFeature}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The features to follow, in order. Every segment but the last is a reference; the last is the attribute whose value is written.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Segments</em>' reference list.
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getPathSource_Segments()
	 * @model required="true"
	 * @generated
	 */
	EList<EStructuralFeature> getSegments();

} // PathSource
