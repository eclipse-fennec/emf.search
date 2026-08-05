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
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.RangeFieldMapping;
import org.eclipse.fennec.search.esearch.RangeKind;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Range Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.RangeFieldMappingImpl#getLowerBound <em>Lower Bound</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.RangeFieldMappingImpl#getUpperBound <em>Upper Bound</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.RangeFieldMappingImpl#getKind <em>Kind</em>}</li>
 * </ul>
 *
 * @generated
 */
public class RangeFieldMappingImpl extends FieldMappingImpl implements RangeFieldMapping {
	/**
	 * The cached value of the '{@link #getLowerBound() <em>Lower Bound</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLowerBound()
	 * @generated
	 * @ordered
	 */
	protected EAttribute lowerBound;

	/**
	 * The cached value of the '{@link #getUpperBound() <em>Upper Bound</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUpperBound()
	 * @generated
	 * @ordered
	 */
	protected EAttribute upperBound;

	/**
	 * The default value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected static final RangeKind KIND_EDEFAULT = RangeKind.AUTO;

	/**
	 * The cached value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected RangeKind kind = KIND_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RangeFieldMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.RANGE_FIELD_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getLowerBound() {
		if (lowerBound != null && lowerBound.eIsProxy()) {
			InternalEObject oldLowerBound = (InternalEObject)lowerBound;
			lowerBound = (EAttribute)eResolveProxy(oldLowerBound);
			if (lowerBound != oldLowerBound) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.RANGE_FIELD_MAPPING__LOWER_BOUND, oldLowerBound, lowerBound));
			}
		}
		return lowerBound;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute basicGetLowerBound() {
		return lowerBound;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLowerBound(EAttribute newLowerBound) {
		EAttribute oldLowerBound = lowerBound;
		lowerBound = newLowerBound;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.RANGE_FIELD_MAPPING__LOWER_BOUND, oldLowerBound, lowerBound));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getUpperBound() {
		if (upperBound != null && upperBound.eIsProxy()) {
			InternalEObject oldUpperBound = (InternalEObject)upperBound;
			upperBound = (EAttribute)eResolveProxy(oldUpperBound);
			if (upperBound != oldUpperBound) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.RANGE_FIELD_MAPPING__UPPER_BOUND, oldUpperBound, upperBound));
			}
		}
		return upperBound;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute basicGetUpperBound() {
		return upperBound;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUpperBound(EAttribute newUpperBound) {
		EAttribute oldUpperBound = upperBound;
		upperBound = newUpperBound;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.RANGE_FIELD_MAPPING__UPPER_BOUND, oldUpperBound, upperBound));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RangeKind getKind() {
		return kind;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setKind(RangeKind newKind) {
		RangeKind oldKind = kind;
		kind = newKind == null ? KIND_EDEFAULT : newKind;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.RANGE_FIELD_MAPPING__KIND, oldKind, kind));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ESearchPackage.RANGE_FIELD_MAPPING__LOWER_BOUND:
				if (resolve) return getLowerBound();
				return basicGetLowerBound();
			case ESearchPackage.RANGE_FIELD_MAPPING__UPPER_BOUND:
				if (resolve) return getUpperBound();
				return basicGetUpperBound();
			case ESearchPackage.RANGE_FIELD_MAPPING__KIND:
				return getKind();
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
			case ESearchPackage.RANGE_FIELD_MAPPING__LOWER_BOUND:
				setLowerBound((EAttribute)newValue);
				return;
			case ESearchPackage.RANGE_FIELD_MAPPING__UPPER_BOUND:
				setUpperBound((EAttribute)newValue);
				return;
			case ESearchPackage.RANGE_FIELD_MAPPING__KIND:
				setKind((RangeKind)newValue);
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
			case ESearchPackage.RANGE_FIELD_MAPPING__LOWER_BOUND:
				setLowerBound((EAttribute)null);
				return;
			case ESearchPackage.RANGE_FIELD_MAPPING__UPPER_BOUND:
				setUpperBound((EAttribute)null);
				return;
			case ESearchPackage.RANGE_FIELD_MAPPING__KIND:
				setKind(KIND_EDEFAULT);
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
			case ESearchPackage.RANGE_FIELD_MAPPING__LOWER_BOUND:
				return lowerBound != null;
			case ESearchPackage.RANGE_FIELD_MAPPING__UPPER_BOUND:
				return upperBound != null;
			case ESearchPackage.RANGE_FIELD_MAPPING__KIND:
				return kind != KIND_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (kind: ");
		result.append(kind);
		result.append(')');
		return result.toString();
	}

} //RangeFieldMappingImpl
