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
package org.eclipse.fennec.search.esearch.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.GeoPointFieldMapping;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Geo Point Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.GeoPointFieldMappingImpl#getLatitude <em>Latitude</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.GeoPointFieldMappingImpl#getLongitude <em>Longitude</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.GeoPointFieldMappingImpl#getPointReference <em>Point Reference</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.GeoPointFieldMappingImpl#getCoordinates <em>Coordinates</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GeoPointFieldMappingImpl extends FieldMappingImpl implements GeoPointFieldMapping {
	/**
	 * The cached value of the '{@link #getLatitude() <em>Latitude</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLatitude()
	 * @generated
	 * @ordered
	 */
	protected EAttribute latitude;

	/**
	 * The cached value of the '{@link #getLongitude() <em>Longitude</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLongitude()
	 * @generated
	 * @ordered
	 */
	protected EAttribute longitude;

	/**
	 * The cached value of the '{@link #getPointReference() <em>Point Reference</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPointReference()
	 * @generated
	 * @ordered
	 */
	protected EReference pointReference;

	/**
	 * The cached value of the '{@link #getCoordinates() <em>Coordinates</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCoordinates()
	 * @generated
	 * @ordered
	 */
	protected EAttribute coordinates;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GeoPointFieldMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.GEO_POINT_FIELD_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getLatitude() {
		if (latitude != null && latitude.eIsProxy()) {
			InternalEObject oldLatitude = (InternalEObject)latitude;
			latitude = (EAttribute)eResolveProxy(oldLatitude);
			if (latitude != oldLatitude) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.GEO_POINT_FIELD_MAPPING__LATITUDE, oldLatitude, latitude));
			}
		}
		return latitude;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute basicGetLatitude() {
		return latitude;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLatitude(EAttribute newLatitude) {
		EAttribute oldLatitude = latitude;
		latitude = newLatitude;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.GEO_POINT_FIELD_MAPPING__LATITUDE, oldLatitude, latitude));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getLongitude() {
		if (longitude != null && longitude.eIsProxy()) {
			InternalEObject oldLongitude = (InternalEObject)longitude;
			longitude = (EAttribute)eResolveProxy(oldLongitude);
			if (longitude != oldLongitude) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.GEO_POINT_FIELD_MAPPING__LONGITUDE, oldLongitude, longitude));
			}
		}
		return longitude;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute basicGetLongitude() {
		return longitude;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLongitude(EAttribute newLongitude) {
		EAttribute oldLongitude = longitude;
		longitude = newLongitude;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.GEO_POINT_FIELD_MAPPING__LONGITUDE, oldLongitude, longitude));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPointReference() {
		if (pointReference != null && pointReference.eIsProxy()) {
			InternalEObject oldPointReference = (InternalEObject)pointReference;
			pointReference = (EReference)eResolveProxy(oldPointReference);
			if (pointReference != oldPointReference) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.GEO_POINT_FIELD_MAPPING__POINT_REFERENCE, oldPointReference, pointReference));
			}
		}
		return pointReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference basicGetPointReference() {
		return pointReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPointReference(EReference newPointReference) {
		EReference oldPointReference = pointReference;
		pointReference = newPointReference;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.GEO_POINT_FIELD_MAPPING__POINT_REFERENCE, oldPointReference, pointReference));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCoordinates() {
		if (coordinates != null && coordinates.eIsProxy()) {
			InternalEObject oldCoordinates = (InternalEObject)coordinates;
			coordinates = (EAttribute)eResolveProxy(oldCoordinates);
			if (coordinates != oldCoordinates) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.GEO_POINT_FIELD_MAPPING__COORDINATES, oldCoordinates, coordinates));
			}
		}
		return coordinates;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute basicGetCoordinates() {
		return coordinates;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCoordinates(EAttribute newCoordinates) {
		EAttribute oldCoordinates = coordinates;
		coordinates = newCoordinates;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.GEO_POINT_FIELD_MAPPING__COORDINATES, oldCoordinates, coordinates));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__LATITUDE:
				if (resolve) return getLatitude();
				return basicGetLatitude();
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__LONGITUDE:
				if (resolve) return getLongitude();
				return basicGetLongitude();
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__POINT_REFERENCE:
				if (resolve) return getPointReference();
				return basicGetPointReference();
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__COORDINATES:
				if (resolve) return getCoordinates();
				return basicGetCoordinates();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__LATITUDE:
				setLatitude((EAttribute)newValue);
				return;
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__LONGITUDE:
				setLongitude((EAttribute)newValue);
				return;
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__POINT_REFERENCE:
				setPointReference((EReference)newValue);
				return;
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__COORDINATES:
				setCoordinates((EAttribute)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__LATITUDE:
				setLatitude((EAttribute)null);
				return;
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__LONGITUDE:
				setLongitude((EAttribute)null);
				return;
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__POINT_REFERENCE:
				setPointReference((EReference)null);
				return;
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__COORDINATES:
				setCoordinates((EAttribute)null);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__LATITUDE:
				return latitude != null;
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__LONGITUDE:
				return longitude != null;
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__POINT_REFERENCE:
				return pointReference != null;
			case ESearchPackage.GEO_POINT_FIELD_MAPPING__COORDINATES:
				return coordinates != null;
		}
		return super.eIsSet(featureID);
	}

} //GeoPointFieldMappingImpl
