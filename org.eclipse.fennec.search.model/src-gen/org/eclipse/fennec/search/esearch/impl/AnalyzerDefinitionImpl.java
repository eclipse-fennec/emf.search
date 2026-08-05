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
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.search.esearch.AnalyzerDefinition;
import org.eclipse.fennec.search.esearch.AnalyzerKind;
import org.eclipse.fennec.search.esearch.AnalyzerParameter;
import org.eclipse.fennec.search.esearch.ESearchPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Analyzer Definition</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.AnalyzerDefinitionImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.AnalyzerDefinitionImpl#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.AnalyzerDefinitionImpl#getLanguage <em>Language</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.AnalyzerDefinitionImpl#getServiceFilter <em>Service Filter</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.AnalyzerDefinitionImpl#getParameters <em>Parameters</em>}</li>
 * </ul>
 *
 * @generated
 */
public class AnalyzerDefinitionImpl extends MinimalEObjectImpl.Container implements AnalyzerDefinition {
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
	 * The default value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected static final AnalyzerKind KIND_EDEFAULT = AnalyzerKind.STANDARD;

	/**
	 * The cached value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected AnalyzerKind kind = KIND_EDEFAULT;

	/**
	 * The default value of the '{@link #getLanguage() <em>Language</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLanguage()
	 * @generated
	 * @ordered
	 */
	protected static final String LANGUAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLanguage() <em>Language</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLanguage()
	 * @generated
	 * @ordered
	 */
	protected String language = LANGUAGE_EDEFAULT;

	/**
	 * The default value of the '{@link #getServiceFilter() <em>Service Filter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getServiceFilter()
	 * @generated
	 * @ordered
	 */
	protected static final String SERVICE_FILTER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getServiceFilter() <em>Service Filter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getServiceFilter()
	 * @generated
	 * @ordered
	 */
	protected String serviceFilter = SERVICE_FILTER_EDEFAULT;

	/**
	 * The cached value of the '{@link #getParameters() <em>Parameters</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParameters()
	 * @generated
	 * @ordered
	 */
	protected EList<AnalyzerParameter> parameters;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AnalyzerDefinitionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.ANALYZER_DEFINITION;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.ANALYZER_DEFINITION__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AnalyzerKind getKind() {
		return kind;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setKind(AnalyzerKind newKind) {
		AnalyzerKind oldKind = kind;
		kind = newKind == null ? KIND_EDEFAULT : newKind;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.ANALYZER_DEFINITION__KIND, oldKind, kind));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLanguage() {
		return language;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLanguage(String newLanguage) {
		String oldLanguage = language;
		language = newLanguage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.ANALYZER_DEFINITION__LANGUAGE, oldLanguage, language));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getServiceFilter() {
		return serviceFilter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setServiceFilter(String newServiceFilter) {
		String oldServiceFilter = serviceFilter;
		serviceFilter = newServiceFilter;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.ANALYZER_DEFINITION__SERVICE_FILTER, oldServiceFilter, serviceFilter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<AnalyzerParameter> getParameters() {
		if (parameters == null) {
			parameters = new EObjectContainmentEList<AnalyzerParameter>(AnalyzerParameter.class, this, ESearchPackage.ANALYZER_DEFINITION__PARAMETERS);
		}
		return parameters;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ESearchPackage.ANALYZER_DEFINITION__PARAMETERS:
				return ((InternalEList<?>)getParameters()).basicRemove(otherEnd, msgs);
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
			case ESearchPackage.ANALYZER_DEFINITION__NAME:
				return getName();
			case ESearchPackage.ANALYZER_DEFINITION__KIND:
				return getKind();
			case ESearchPackage.ANALYZER_DEFINITION__LANGUAGE:
				return getLanguage();
			case ESearchPackage.ANALYZER_DEFINITION__SERVICE_FILTER:
				return getServiceFilter();
			case ESearchPackage.ANALYZER_DEFINITION__PARAMETERS:
				return getParameters();
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
			case ESearchPackage.ANALYZER_DEFINITION__NAME:
				setName((String)newValue);
				return;
			case ESearchPackage.ANALYZER_DEFINITION__KIND:
				setKind((AnalyzerKind)newValue);
				return;
			case ESearchPackage.ANALYZER_DEFINITION__LANGUAGE:
				setLanguage((String)newValue);
				return;
			case ESearchPackage.ANALYZER_DEFINITION__SERVICE_FILTER:
				setServiceFilter((String)newValue);
				return;
			case ESearchPackage.ANALYZER_DEFINITION__PARAMETERS:
				getParameters().clear();
				getParameters().addAll((Collection<? extends AnalyzerParameter>)newValue);
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
			case ESearchPackage.ANALYZER_DEFINITION__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ESearchPackage.ANALYZER_DEFINITION__KIND:
				setKind(KIND_EDEFAULT);
				return;
			case ESearchPackage.ANALYZER_DEFINITION__LANGUAGE:
				setLanguage(LANGUAGE_EDEFAULT);
				return;
			case ESearchPackage.ANALYZER_DEFINITION__SERVICE_FILTER:
				setServiceFilter(SERVICE_FILTER_EDEFAULT);
				return;
			case ESearchPackage.ANALYZER_DEFINITION__PARAMETERS:
				getParameters().clear();
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
			case ESearchPackage.ANALYZER_DEFINITION__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ESearchPackage.ANALYZER_DEFINITION__KIND:
				return kind != KIND_EDEFAULT;
			case ESearchPackage.ANALYZER_DEFINITION__LANGUAGE:
				return LANGUAGE_EDEFAULT == null ? language != null : !LANGUAGE_EDEFAULT.equals(language);
			case ESearchPackage.ANALYZER_DEFINITION__SERVICE_FILTER:
				return SERVICE_FILTER_EDEFAULT == null ? serviceFilter != null : !SERVICE_FILTER_EDEFAULT.equals(serviceFilter);
			case ESearchPackage.ANALYZER_DEFINITION__PARAMETERS:
				return parameters != null && !parameters.isEmpty();
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
		result.append(", language: ");
		result.append(language);
		result.append(", serviceFilter: ");
		result.append(serviceFilter);
		result.append(')');
		return result.toString();
	}

} //AnalyzerDefinitionImpl
