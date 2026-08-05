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

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.search.esearch.AnalyzerDefinition;
import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.SearchMappingRegistry;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Search Mapping Registry</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.SearchMappingRegistryImpl#getUnits <em>Units</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.SearchMappingRegistryImpl#getAnalyzers <em>Analyzers</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SearchMappingRegistryImpl extends MinimalEObjectImpl.Container implements SearchMappingRegistry {
	/**
	 * The cached value of the '{@link #getUnits() <em>Units</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnits()
	 * @generated
	 * @ordered
	 */
	protected EList<IndexUnitMapping> units;

	/**
	 * The cached value of the '{@link #getAnalyzers() <em>Analyzers</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAnalyzers()
	 * @generated
	 * @ordered
	 */
	protected EList<AnalyzerDefinition> analyzers;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SearchMappingRegistryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.SEARCH_MAPPING_REGISTRY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<IndexUnitMapping> getUnits() {
		if (units == null) {
			units = new EObjectContainmentEList<IndexUnitMapping>(IndexUnitMapping.class, this, ESearchPackage.SEARCH_MAPPING_REGISTRY__UNITS);
		}
		return units;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<AnalyzerDefinition> getAnalyzers() {
		if (analyzers == null) {
			analyzers = new EObjectContainmentEList<AnalyzerDefinition>(AnalyzerDefinition.class, this, ESearchPackage.SEARCH_MAPPING_REGISTRY__ANALYZERS);
		}
		return analyzers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ESearchPackage.SEARCH_MAPPING_REGISTRY__UNITS:
				return ((InternalEList<?>)getUnits()).basicRemove(otherEnd, msgs);
			case ESearchPackage.SEARCH_MAPPING_REGISTRY__ANALYZERS:
				return ((InternalEList<?>)getAnalyzers()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ESearchPackage.SEARCH_MAPPING_REGISTRY__UNITS:
				return getUnits();
			case ESearchPackage.SEARCH_MAPPING_REGISTRY__ANALYZERS:
				return getAnalyzers();
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
			case ESearchPackage.SEARCH_MAPPING_REGISTRY__UNITS:
				getUnits().clear();
				getUnits().addAll((Collection<? extends IndexUnitMapping>)newValue);
				return;
			case ESearchPackage.SEARCH_MAPPING_REGISTRY__ANALYZERS:
				getAnalyzers().clear();
				getAnalyzers().addAll((Collection<? extends AnalyzerDefinition>)newValue);
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
			case ESearchPackage.SEARCH_MAPPING_REGISTRY__UNITS:
				getUnits().clear();
				return;
			case ESearchPackage.SEARCH_MAPPING_REGISTRY__ANALYZERS:
				getAnalyzers().clear();
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
			case ESearchPackage.SEARCH_MAPPING_REGISTRY__UNITS:
				return units != null && !units.isEmpty();
			case ESearchPackage.SEARCH_MAPPING_REGISTRY__ANALYZERS:
				return analyzers != null && !analyzers.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //SearchMappingRegistryImpl
