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
 * A representation of the model object '<em><b>Geo Point Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A geographic point, indexed for distance, bounding-box and polygon queries and (with doc values) for distance sorting.
 * 
 * Two authoring shapes, exactly one of which must be used: either latitude and longitude as separate attributes, or the inherited feature pointing at a single attribute whose value carries both. Declaring neither or both is refused.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getLatitude <em>Latitude</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getLongitude <em>Longitude</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getGeoPointFieldMapping()
 * @model
 * @generated
 */
@ProviderType
public interface GeoPointFieldMapping extends FieldMapping {
	/**
	 * Returns the value of the '<em><b>Latitude</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Latitude attribute, in degrees.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Latitude</em>' reference.
	 * @see #setLatitude(EAttribute)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getGeoPointFieldMapping_Latitude()
	 * @model
	 * @generated
	 */
	EAttribute getLatitude();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getLatitude <em>Latitude</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Latitude</em>' reference.
	 * @see #getLatitude()
	 * @generated
	 */
	void setLatitude(EAttribute value);

	/**
	 * Returns the value of the '<em><b>Longitude</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Longitude attribute, in degrees.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Longitude</em>' reference.
	 * @see #setLongitude(EAttribute)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getGeoPointFieldMapping_Longitude()
	 * @model
	 * @generated
	 */
	EAttribute getLongitude();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getLongitude <em>Longitude</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Longitude</em>' reference.
	 * @see #getLongitude()
	 * @generated
	 */
	void setLongitude(EAttribute value);

} // GeoPointFieldMapping
