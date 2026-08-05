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
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Keyword Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.KeywordFieldMappingImpl#getNormalizer <em>Normalizer</em>}</li>
 * </ul>
 *
 * @generated
 */
public class KeywordFieldMappingImpl extends FieldMappingImpl implements KeywordFieldMapping {
	/**
	 * The cached value of the '{@link #getNormalizer() <em>Normalizer</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNormalizer()
	 * @generated
	 * @ordered
	 */
	protected AnalyzerDefinition normalizer;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected KeywordFieldMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.KEYWORD_FIELD_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AnalyzerDefinition getNormalizer() {
		if (normalizer != null && normalizer.eIsProxy()) {
			InternalEObject oldNormalizer = (InternalEObject)normalizer;
			normalizer = (AnalyzerDefinition)eResolveProxy(oldNormalizer);
			if (normalizer != oldNormalizer) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.KEYWORD_FIELD_MAPPING__NORMALIZER, oldNormalizer, normalizer));
			}
		}
		return normalizer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AnalyzerDefinition basicGetNormalizer() {
		return normalizer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNormalizer(AnalyzerDefinition newNormalizer) {
		AnalyzerDefinition oldNormalizer = normalizer;
		normalizer = newNormalizer;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.KEYWORD_FIELD_MAPPING__NORMALIZER, oldNormalizer, normalizer));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ESearchPackage.KEYWORD_FIELD_MAPPING__NORMALIZER:
				if (resolve) return getNormalizer();
				return basicGetNormalizer();
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
			case ESearchPackage.KEYWORD_FIELD_MAPPING__NORMALIZER:
				setNormalizer((AnalyzerDefinition)newValue);
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
			case ESearchPackage.KEYWORD_FIELD_MAPPING__NORMALIZER:
				setNormalizer((AnalyzerDefinition)null);
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
			case ESearchPackage.KEYWORD_FIELD_MAPPING__NORMALIZER:
				return normalizer != null;
		}
		return super.eIsSet(featureID);
	}

} //KeywordFieldMappingImpl
