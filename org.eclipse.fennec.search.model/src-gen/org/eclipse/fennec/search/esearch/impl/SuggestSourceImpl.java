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

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectResolvingEList;

import org.eclipse.fennec.search.esearch.AnalyzerDefinition;
import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.SuggestSource;
import org.eclipse.fennec.search.esearch.SuggesterKind;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Suggest Source</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.SuggestSourceImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.SuggestSourceImpl#getFeature <em>Feature</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.SuggestSourceImpl#getWeight <em>Weight</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.SuggestSourceImpl#getContexts <em>Contexts</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.SuggestSourceImpl#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.SuggestSourceImpl#getAnalyzer <em>Analyzer</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SuggestSourceImpl extends MinimalEObjectImpl.Container implements SuggestSource {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

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
	 * The cached value of the '{@link #getWeight() <em>Weight</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWeight()
	 * @generated
	 * @ordered
	 */
	protected EAttribute weight;

	/**
	 * The cached value of the '{@link #getContexts() <em>Contexts</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContexts()
	 * @generated
	 * @ordered
	 */
	protected EList<EAttribute> contexts;

	/**
	 * The default value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected static final SuggesterKind KIND_EDEFAULT = SuggesterKind.ANALYZING;

	/**
	 * The cached value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected SuggesterKind kind = KIND_EDEFAULT;

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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SuggestSourceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.SUGGEST_SOURCE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.SUGGEST_SOURCE__NAME, oldName, name));
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.SUGGEST_SOURCE__FEATURE, oldFeature, feature));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.SUGGEST_SOURCE__FEATURE, oldFeature, feature));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getWeight() {
		if (weight != null && weight.eIsProxy()) {
			InternalEObject oldWeight = (InternalEObject)weight;
			weight = (EAttribute)eResolveProxy(oldWeight);
			if (weight != oldWeight) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.SUGGEST_SOURCE__WEIGHT, oldWeight, weight));
			}
		}
		return weight;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute basicGetWeight() {
		return weight;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setWeight(EAttribute newWeight) {
		EAttribute oldWeight = weight;
		weight = newWeight;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.SUGGEST_SOURCE__WEIGHT, oldWeight, weight));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<EAttribute> getContexts() {
		if (contexts == null) {
			contexts = new EObjectResolvingEList<EAttribute>(EAttribute.class, this, ESearchPackage.SUGGEST_SOURCE__CONTEXTS);
		}
		return contexts;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SuggesterKind getKind() {
		return kind;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setKind(SuggesterKind newKind) {
		SuggesterKind oldKind = kind;
		kind = newKind == null ? KIND_EDEFAULT : newKind;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.SUGGEST_SOURCE__KIND, oldKind, kind));
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.SUGGEST_SOURCE__ANALYZER, oldAnalyzer, analyzer));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.SUGGEST_SOURCE__ANALYZER, oldAnalyzer, analyzer));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ESearchPackage.SUGGEST_SOURCE__NAME:
				return getName();
			case ESearchPackage.SUGGEST_SOURCE__FEATURE:
				if (resolve) return getFeature();
				return basicGetFeature();
			case ESearchPackage.SUGGEST_SOURCE__WEIGHT:
				if (resolve) return getWeight();
				return basicGetWeight();
			case ESearchPackage.SUGGEST_SOURCE__CONTEXTS:
				return getContexts();
			case ESearchPackage.SUGGEST_SOURCE__KIND:
				return getKind();
			case ESearchPackage.SUGGEST_SOURCE__ANALYZER:
				if (resolve) return getAnalyzer();
				return basicGetAnalyzer();
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
			case ESearchPackage.SUGGEST_SOURCE__NAME:
				setName((String)newValue);
				return;
			case ESearchPackage.SUGGEST_SOURCE__FEATURE:
				setFeature((EAttribute)newValue);
				return;
			case ESearchPackage.SUGGEST_SOURCE__WEIGHT:
				setWeight((EAttribute)newValue);
				return;
			case ESearchPackage.SUGGEST_SOURCE__CONTEXTS:
				getContexts().clear();
				getContexts().addAll((Collection<? extends EAttribute>)newValue);
				return;
			case ESearchPackage.SUGGEST_SOURCE__KIND:
				setKind((SuggesterKind)newValue);
				return;
			case ESearchPackage.SUGGEST_SOURCE__ANALYZER:
				setAnalyzer((AnalyzerDefinition)newValue);
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
			case ESearchPackage.SUGGEST_SOURCE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ESearchPackage.SUGGEST_SOURCE__FEATURE:
				setFeature((EAttribute)null);
				return;
			case ESearchPackage.SUGGEST_SOURCE__WEIGHT:
				setWeight((EAttribute)null);
				return;
			case ESearchPackage.SUGGEST_SOURCE__CONTEXTS:
				getContexts().clear();
				return;
			case ESearchPackage.SUGGEST_SOURCE__KIND:
				setKind(KIND_EDEFAULT);
				return;
			case ESearchPackage.SUGGEST_SOURCE__ANALYZER:
				setAnalyzer((AnalyzerDefinition)null);
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
			case ESearchPackage.SUGGEST_SOURCE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ESearchPackage.SUGGEST_SOURCE__FEATURE:
				return feature != null;
			case ESearchPackage.SUGGEST_SOURCE__WEIGHT:
				return weight != null;
			case ESearchPackage.SUGGEST_SOURCE__CONTEXTS:
				return contexts != null && !contexts.isEmpty();
			case ESearchPackage.SUGGEST_SOURCE__KIND:
				return kind != KIND_EDEFAULT;
			case ESearchPackage.SUGGEST_SOURCE__ANALYZER:
				return analyzer != null;
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
		result.append(" (name: ");
		result.append(name);
		result.append(", kind: ");
		result.append(kind);
		result.append(')');
		return result.toString();
	}

} //SuggestSourceImpl
