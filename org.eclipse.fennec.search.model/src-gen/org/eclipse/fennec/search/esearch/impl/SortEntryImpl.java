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
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.SortEntry;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sort Entry</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.SortEntryImpl#getFeature <em>Feature</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.SortEntryImpl#isDescending <em>Descending</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.SortEntryImpl#isMissingLast <em>Missing Last</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SortEntryImpl extends MinimalEObjectImpl.Container implements SortEntry {
	/**
	 * The cached value of the '{@link #getFeature() <em>Feature</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeature()
	 * @generated
	 * @ordered
	 */
	protected EAttribute feature;

	/**
	 * The default value of the '{@link #isDescending() <em>Descending</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDescending()
	 * @generated
	 * @ordered
	 */
	protected static final boolean DESCENDING_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isDescending() <em>Descending</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDescending()
	 * @generated
	 * @ordered
	 */
	protected boolean descending = DESCENDING_EDEFAULT;

	/**
	 * The default value of the '{@link #isMissingLast() <em>Missing Last</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isMissingLast()
	 * @generated
	 * @ordered
	 */
	protected static final boolean MISSING_LAST_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isMissingLast() <em>Missing Last</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isMissingLast()
	 * @generated
	 * @ordered
	 */
	protected boolean missingLast = MISSING_LAST_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SortEntryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.SORT_ENTRY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFeature() {
		if (feature != null && feature.eIsProxy()) {
			InternalEObject oldFeature = (InternalEObject)feature;
			feature = (EAttribute)eResolveProxy(oldFeature);
			if (feature != oldFeature) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.SORT_ENTRY__FEATURE, oldFeature, feature));
			}
		}
		return feature;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute basicGetFeature() {
		return feature;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFeature(EAttribute newFeature) {
		EAttribute oldFeature = feature;
		feature = newFeature;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.SORT_ENTRY__FEATURE, oldFeature, feature));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isDescending() {
		return descending;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDescending(boolean newDescending) {
		boolean oldDescending = descending;
		descending = newDescending;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.SORT_ENTRY__DESCENDING, oldDescending, descending));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isMissingLast() {
		return missingLast;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMissingLast(boolean newMissingLast) {
		boolean oldMissingLast = missingLast;
		missingLast = newMissingLast;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.SORT_ENTRY__MISSING_LAST, oldMissingLast, missingLast));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ESearchPackage.SORT_ENTRY__FEATURE:
				if (resolve) return getFeature();
				return basicGetFeature();
			case ESearchPackage.SORT_ENTRY__DESCENDING:
				return isDescending();
			case ESearchPackage.SORT_ENTRY__MISSING_LAST:
				return isMissingLast();
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
			case ESearchPackage.SORT_ENTRY__FEATURE:
				setFeature((EAttribute)newValue);
				return;
			case ESearchPackage.SORT_ENTRY__DESCENDING:
				setDescending((Boolean)newValue);
				return;
			case ESearchPackage.SORT_ENTRY__MISSING_LAST:
				setMissingLast((Boolean)newValue);
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
			case ESearchPackage.SORT_ENTRY__FEATURE:
				setFeature((EAttribute)null);
				return;
			case ESearchPackage.SORT_ENTRY__DESCENDING:
				setDescending(DESCENDING_EDEFAULT);
				return;
			case ESearchPackage.SORT_ENTRY__MISSING_LAST:
				setMissingLast(MISSING_LAST_EDEFAULT);
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
			case ESearchPackage.SORT_ENTRY__FEATURE:
				return feature != null;
			case ESearchPackage.SORT_ENTRY__DESCENDING:
				return descending != DESCENDING_EDEFAULT;
			case ESearchPackage.SORT_ENTRY__MISSING_LAST:
				return missingLast != MISSING_LAST_EDEFAULT;
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
		result.append(" (descending: ");
		result.append(descending);
		result.append(", missingLast: ");
		result.append(missingLast);
		result.append(')');
		return result.toString();
	}

} //SortEntryImpl
