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

import java.util.Collection;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.ecore.util.EObjectResolvingEList;

import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.PathSource;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Path Source</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.PathSourceImpl#getSegments <em>Segments</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PathSourceImpl extends ValueSourceImpl implements PathSource {
	/**
	 * The cached value of the '{@link #getSegments() <em>Segments</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSegments()
	 * @generated
	 * @ordered
	 */
	protected EList<EStructuralFeature> segments;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PathSourceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.PATH_SOURCE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<EStructuralFeature> getSegments() {
		if (segments == null) {
			segments = new EObjectResolvingEList<EStructuralFeature>(EStructuralFeature.class, this, ESearchPackage.PATH_SOURCE__SEGMENTS);
		}
		return segments;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ESearchPackage.PATH_SOURCE__SEGMENTS:
				return getSegments();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ESearchPackage.PATH_SOURCE__SEGMENTS:
				getSegments().clear();
				getSegments().addAll((Collection<? extends EStructuralFeature>)newValue);
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
			case ESearchPackage.PATH_SOURCE__SEGMENTS:
				getSegments().clear();
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
			case ESearchPackage.PATH_SOURCE__SEGMENTS:
				return segments != null && !segments.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //PathSourceImpl
