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

import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.RankFunction;
import org.eclipse.fennec.search.esearch.RankSignalFieldMapping;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Rank Signal Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.RankSignalFieldMappingImpl#getFunction <em>Function</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.RankSignalFieldMappingImpl#getPivot <em>Pivot</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.RankSignalFieldMappingImpl#getExponent <em>Exponent</em>}</li>
 * </ul>
 *
 * @generated
 */
public class RankSignalFieldMappingImpl extends FieldMappingImpl implements RankSignalFieldMapping {
	/**
	 * The default value of the '{@link #getFunction() <em>Function</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFunction()
	 * @generated
	 * @ordered
	 */
	protected static final RankFunction FUNCTION_EDEFAULT = RankFunction.SATURATION;

	/**
	 * The cached value of the '{@link #getFunction() <em>Function</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFunction()
	 * @generated
	 * @ordered
	 */
	protected RankFunction function = FUNCTION_EDEFAULT;

	/**
	 * The default value of the '{@link #getPivot() <em>Pivot</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPivot()
	 * @generated
	 * @ordered
	 */
	protected static final double PIVOT_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getPivot() <em>Pivot</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPivot()
	 * @generated
	 * @ordered
	 */
	protected double pivot = PIVOT_EDEFAULT;

	/**
	 * The default value of the '{@link #getExponent() <em>Exponent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExponent()
	 * @generated
	 * @ordered
	 */
	protected static final double EXPONENT_EDEFAULT = 1.0;

	/**
	 * The cached value of the '{@link #getExponent() <em>Exponent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExponent()
	 * @generated
	 * @ordered
	 */
	protected double exponent = EXPONENT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RankSignalFieldMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.RANK_SIGNAL_FIELD_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RankFunction getFunction() {
		return function;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFunction(RankFunction newFunction) {
		RankFunction oldFunction = function;
		function = newFunction == null ? FUNCTION_EDEFAULT : newFunction;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__FUNCTION, oldFunction, function));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getPivot() {
		return pivot;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPivot(double newPivot) {
		double oldPivot = pivot;
		pivot = newPivot;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__PIVOT, oldPivot, pivot));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getExponent() {
		return exponent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setExponent(double newExponent) {
		double oldExponent = exponent;
		exponent = newExponent;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__EXPONENT, oldExponent, exponent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__FUNCTION:
				return getFunction();
			case ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__PIVOT:
				return getPivot();
			case ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__EXPONENT:
				return getExponent();
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
			case ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__FUNCTION:
				setFunction((RankFunction)newValue);
				return;
			case ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__PIVOT:
				setPivot((Double)newValue);
				return;
			case ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__EXPONENT:
				setExponent((Double)newValue);
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
			case ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__FUNCTION:
				setFunction(FUNCTION_EDEFAULT);
				return;
			case ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__PIVOT:
				setPivot(PIVOT_EDEFAULT);
				return;
			case ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__EXPONENT:
				setExponent(EXPONENT_EDEFAULT);
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
			case ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__FUNCTION:
				return function != FUNCTION_EDEFAULT;
			case ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__PIVOT:
				return pivot != PIVOT_EDEFAULT;
			case ESearchPackage.RANK_SIGNAL_FIELD_MAPPING__EXPONENT:
				return exponent != EXPONENT_EDEFAULT;
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
		result.append(" (function: ");
		result.append(function);
		result.append(", pivot: ");
		result.append(pivot);
		result.append(", exponent: ");
		result.append(exponent);
		result.append(')');
		return result.toString();
	}

} //RankSignalFieldMappingImpl
