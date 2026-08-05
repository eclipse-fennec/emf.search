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

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.RefreshMode;
import org.eclipse.fennec.search.esearch.RefreshPolicy;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Refresh Policy</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.RefreshPolicyImpl#getMode <em>Mode</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.RefreshPolicyImpl#getIntervalMillis <em>Interval Millis</em>}</li>
 * </ul>
 *
 * @generated
 */
public class RefreshPolicyImpl extends MinimalEObjectImpl.Container implements RefreshPolicy {
	/**
	 * The default value of the '{@link #getMode() <em>Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMode()
	 * @generated
	 * @ordered
	 */
	protected static final RefreshMode MODE_EDEFAULT = RefreshMode.NEAR_REAL_TIME;

	/**
	 * The cached value of the '{@link #getMode() <em>Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMode()
	 * @generated
	 * @ordered
	 */
	protected RefreshMode mode = MODE_EDEFAULT;

	/**
	 * The default value of the '{@link #getIntervalMillis() <em>Interval Millis</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIntervalMillis()
	 * @generated
	 * @ordered
	 */
	protected static final long INTERVAL_MILLIS_EDEFAULT = 1000L;

	/**
	 * The cached value of the '{@link #getIntervalMillis() <em>Interval Millis</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIntervalMillis()
	 * @generated
	 * @ordered
	 */
	protected long intervalMillis = INTERVAL_MILLIS_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RefreshPolicyImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.REFRESH_POLICY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RefreshMode getMode() {
		return mode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMode(RefreshMode newMode) {
		RefreshMode oldMode = mode;
		mode = newMode == null ? MODE_EDEFAULT : newMode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.REFRESH_POLICY__MODE, oldMode, mode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public long getIntervalMillis() {
		return intervalMillis;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIntervalMillis(long newIntervalMillis) {
		long oldIntervalMillis = intervalMillis;
		intervalMillis = newIntervalMillis;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.REFRESH_POLICY__INTERVAL_MILLIS, oldIntervalMillis, intervalMillis));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ESearchPackage.REFRESH_POLICY__MODE:
				return getMode();
			case ESearchPackage.REFRESH_POLICY__INTERVAL_MILLIS:
				return getIntervalMillis();
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
			case ESearchPackage.REFRESH_POLICY__MODE:
				setMode((RefreshMode)newValue);
				return;
			case ESearchPackage.REFRESH_POLICY__INTERVAL_MILLIS:
				setIntervalMillis((Long)newValue);
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
			case ESearchPackage.REFRESH_POLICY__MODE:
				setMode(MODE_EDEFAULT);
				return;
			case ESearchPackage.REFRESH_POLICY__INTERVAL_MILLIS:
				setIntervalMillis(INTERVAL_MILLIS_EDEFAULT);
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
			case ESearchPackage.REFRESH_POLICY__MODE:
				return mode != MODE_EDEFAULT;
			case ESearchPackage.REFRESH_POLICY__INTERVAL_MILLIS:
				return intervalMillis != INTERVAL_MILLIS_EDEFAULT;
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
		result.append(" (mode: ");
		result.append(mode);
		result.append(", intervalMillis: ");
		result.append(intervalMillis);
		result.append(')');
		return result.toString();
	}

} //RefreshPolicyImpl
