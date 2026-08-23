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

import org.eclipse.emf.ecore.EAttribute;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Feature Source</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Rung one: one attribute of the mapped object, read with eGet — the same thing FieldMapping.feature says, spelled as a source so it can stand beside others. A field fed by one attribute alone needs no source at all; declare this one only where a second source joins it.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.FeatureSource#getFeature <em>Feature</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFeatureSource()
 * @model
 * @generated
 */
@ProviderType
public interface FeatureSource extends ValueSource {
	/**
	 * Returns the value of the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The attribute to read from the mapped object.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Feature</em>' reference.
	 * @see #setFeature(EAttribute)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFeatureSource_Feature()
	 * @model required="true"
	 * @generated
	 */
	EAttribute getFeature();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.FeatureSource#getFeature <em>Feature</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Feature</em>' reference.
	 * @see #getFeature()
	 * @generated
	 */
	void setFeature(EAttribute value);

} // FeatureSource
