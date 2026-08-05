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
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.search.esearch.AnalyzerDefinition;
import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.TextFieldMapping;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Text Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.TextFieldMappingImpl#getAnalyzer <em>Analyzer</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.TextFieldMappingImpl#isTermVectors <em>Term Vectors</em>}</li>
 * </ul>
 *
 * @generated
 */
public class TextFieldMappingImpl extends FieldMappingImpl implements TextFieldMapping {
	/**
	 * The cached value of the '{@link #getAnalyzer() <em>Analyzer</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAnalyzer()
	 * @generated
	 * @ordered
	 */
	protected AnalyzerDefinition analyzer;

	/**
	 * The default value of the '{@link #isTermVectors() <em>Term Vectors</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isTermVectors()
	 * @generated
	 * @ordered
	 */
	protected static final boolean TERM_VECTORS_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isTermVectors() <em>Term Vectors</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isTermVectors()
	 * @generated
	 * @ordered
	 */
	protected boolean termVectors = TERM_VECTORS_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TextFieldMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.TEXT_FIELD_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AnalyzerDefinition getAnalyzer() {
		if (analyzer != null && analyzer.eIsProxy()) {
			InternalEObject oldAnalyzer = (InternalEObject)analyzer;
			analyzer = (AnalyzerDefinition)eResolveProxy(oldAnalyzer);
			if (analyzer != oldAnalyzer) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.TEXT_FIELD_MAPPING__ANALYZER, oldAnalyzer, analyzer));
			}
		}
		return analyzer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AnalyzerDefinition basicGetAnalyzer() {
		return analyzer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAnalyzer(AnalyzerDefinition newAnalyzer) {
		AnalyzerDefinition oldAnalyzer = analyzer;
		analyzer = newAnalyzer;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.TEXT_FIELD_MAPPING__ANALYZER, oldAnalyzer, analyzer));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isTermVectors() {
		return termVectors;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTermVectors(boolean newTermVectors) {
		boolean oldTermVectors = termVectors;
		termVectors = newTermVectors;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.TEXT_FIELD_MAPPING__TERM_VECTORS, oldTermVectors, termVectors));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ESearchPackage.TEXT_FIELD_MAPPING__ANALYZER:
				if (resolve) return getAnalyzer();
				return basicGetAnalyzer();
			case ESearchPackage.TEXT_FIELD_MAPPING__TERM_VECTORS:
				return isTermVectors();
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
			case ESearchPackage.TEXT_FIELD_MAPPING__ANALYZER:
				setAnalyzer((AnalyzerDefinition)newValue);
				return;
			case ESearchPackage.TEXT_FIELD_MAPPING__TERM_VECTORS:
				setTermVectors((Boolean)newValue);
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
			case ESearchPackage.TEXT_FIELD_MAPPING__ANALYZER:
				setAnalyzer((AnalyzerDefinition)null);
				return;
			case ESearchPackage.TEXT_FIELD_MAPPING__TERM_VECTORS:
				setTermVectors(TERM_VECTORS_EDEFAULT);
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
			case ESearchPackage.TEXT_FIELD_MAPPING__ANALYZER:
				return analyzer != null;
			case ESearchPackage.TEXT_FIELD_MAPPING__TERM_VECTORS:
				return termVectors != TERM_VECTORS_EDEFAULT;
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
		result.append(" (termVectors: ");
		result.append(termVectors);
		result.append(')');
		return result.toString();
	}

} //TextFieldMappingImpl
