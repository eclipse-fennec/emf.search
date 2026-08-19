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
import org.eclipse.emf.ecore.EReference;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Geo Point Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A geographic point, indexed for distance, bounding-box and polygon queries and (with doc values) for distance sorting.
 * 
 * Three authoring shapes, exactly one of which must be used: latitude and longitude as separate attributes; the inherited feature pointing at a single attribute whose value carries both; or the packed shape — pointReference naming the reference that holds a GeoJSON-style point object and coordinates naming that object's many-valued numeric attribute in [lon, lat] order. Declaring none or more than one is refused.
 * 
 * The three shapes exist because the query IR names both bindings (emf.persistence-jpa#101, decision G1): a split feature pair is what most Ecore models carry, a packed point is what a GeoJSON producer and a Mongo 2dsphere index carry. Which one a model used stops mattering after indexing — all three write the same LatLonPoint field, so box, polygon and distance queries never see the difference.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getLatitude <em>Latitude</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getLongitude <em>Longitude</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getPointReference <em>Point Reference</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getCoordinates <em>Coordinates</em>}</li>
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

	/**
	 * Returns the value of the '<em><b>Point Reference</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The reference holding the packed point object — the segment a query's packed subject path names (GeoSubject.pathPoint). Set together with coordinates, and only for the packed shape.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Point Reference</em>' reference.
	 * @see #setPointReference(EReference)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getGeoPointFieldMapping_PointReference()
	 * @model
	 * @generated
	 */
	EReference getPointReference();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getPointReference <em>Point Reference</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Point Reference</em>' reference.
	 * @see #getPointReference()
	 * @generated
	 */
	void setPointReference(EReference value);

	/**
	 * Returns the value of the '<em><b>Coordinates</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The many-valued numeric attribute on the referenced point class, in GeoJSON order [lon, lat]. A value of any other arity is treated like a missing coordinate — UNKNOWN, not an error, per the 3VL discipline of the geo vocabulary (§5.5).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Coordinates</em>' reference.
	 * @see #setCoordinates(EAttribute)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getGeoPointFieldMapping_Coordinates()
	 * @model
	 * @generated
	 */
	EAttribute getCoordinates();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getCoordinates <em>Coordinates</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Coordinates</em>' reference.
	 * @see #getCoordinates()
	 * @generated
	 */
	void setCoordinates(EAttribute value);

} // GeoPointFieldMapping
