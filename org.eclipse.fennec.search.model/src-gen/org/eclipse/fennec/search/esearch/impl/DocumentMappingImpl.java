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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.search.esearch.AnalyzerDefinition;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.FieldMapping;
import org.eclipse.fennec.search.esearch.Materialization;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.SuggestSource;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Document Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.DocumentMappingImpl#getEClass <em>EClass</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.DocumentMappingImpl#getTypeName <em>Type Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.DocumentMappingImpl#getIdFeature <em>Id Feature</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.DocumentMappingImpl#isAutoMap <em>Auto Map</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.DocumentMappingImpl#getAnalyzer <em>Analyzer</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.DocumentMappingImpl#getMaterialization <em>Materialization</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.DocumentMappingImpl#getFields <em>Fields</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.DocumentMappingImpl#getReferences <em>References</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.DocumentMappingImpl#getSuggestions <em>Suggestions</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DocumentMappingImpl extends MinimalEObjectImpl.Container implements DocumentMapping {
	/**
	 * The cached value of the '{@link #getEClass() <em>EClass</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEClass()
	 * @generated
	 * @ordered
	 */
	protected EClass eClass;

	/**
	 * The default value of the '{@link #getTypeName() <em>Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTypeName()
	 * @generated
	 * @ordered
	 */
	protected static final String TYPE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTypeName() <em>Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTypeName()
	 * @generated
	 * @ordered
	 */
	protected String typeName = TYPE_NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getIdFeature() <em>Id Feature</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIdFeature()
	 * @generated
	 * @ordered
	 */
	protected EAttribute idFeature;

	/**
	 * The default value of the '{@link #isAutoMap() <em>Auto Map</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAutoMap()
	 * @generated
	 * @ordered
	 */
	protected static final boolean AUTO_MAP_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isAutoMap() <em>Auto Map</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAutoMap()
	 * @generated
	 * @ordered
	 */
	protected boolean autoMap = AUTO_MAP_EDEFAULT;

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
	 * The cached value of the '{@link #getMaterialization() <em>Materialization</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaterialization()
	 * @generated
	 * @ordered
	 */
	protected Materialization materialization;

	/**
	 * The cached value of the '{@link #getFields() <em>Fields</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFields()
	 * @generated
	 * @ordered
	 */
	protected EList<FieldMapping> fields;

	/**
	 * The cached value of the '{@link #getReferences() <em>References</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReferences()
	 * @generated
	 * @ordered
	 */
	protected EList<ReferenceMapping> references;

	/**
	 * The cached value of the '{@link #getSuggestions() <em>Suggestions</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSuggestions()
	 * @generated
	 * @ordered
	 */
	protected EList<SuggestSource> suggestions;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DocumentMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.DOCUMENT_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEClass() {
		if (eClass != null && eClass.eIsProxy()) {
			InternalEObject oldEClass = (InternalEObject)eClass;
			eClass = (EClass)eResolveProxy(oldEClass);
			if (eClass != oldEClass) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.DOCUMENT_MAPPING__ECLASS, oldEClass, eClass));
			}
		}
		return eClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass basicGetEClass() {
		return eClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEClass(EClass newEClass) {
		EClass oldEClass = eClass;
		eClass = newEClass;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.DOCUMENT_MAPPING__ECLASS, oldEClass, eClass));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getTypeName() {
		return typeName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTypeName(String newTypeName) {
		String oldTypeName = typeName;
		typeName = newTypeName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.DOCUMENT_MAPPING__TYPE_NAME, oldTypeName, typeName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIdFeature() {
		if (idFeature != null && idFeature.eIsProxy()) {
			InternalEObject oldIdFeature = (InternalEObject)idFeature;
			idFeature = (EAttribute)eResolveProxy(oldIdFeature);
			if (idFeature != oldIdFeature) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.DOCUMENT_MAPPING__ID_FEATURE, oldIdFeature, idFeature));
			}
		}
		return idFeature;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute basicGetIdFeature() {
		return idFeature;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIdFeature(EAttribute newIdFeature) {
		EAttribute oldIdFeature = idFeature;
		idFeature = newIdFeature;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.DOCUMENT_MAPPING__ID_FEATURE, oldIdFeature, idFeature));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isAutoMap() {
		return autoMap;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAutoMap(boolean newAutoMap) {
		boolean oldAutoMap = autoMap;
		autoMap = newAutoMap;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.DOCUMENT_MAPPING__AUTO_MAP, oldAutoMap, autoMap));
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.DOCUMENT_MAPPING__ANALYZER, oldAnalyzer, analyzer));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.DOCUMENT_MAPPING__ANALYZER, oldAnalyzer, analyzer));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Materialization getMaterialization() {
		return materialization;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMaterialization(Materialization newMaterialization, NotificationChain msgs) {
		Materialization oldMaterialization = materialization;
		materialization = newMaterialization;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ESearchPackage.DOCUMENT_MAPPING__MATERIALIZATION, oldMaterialization, newMaterialization);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMaterialization(Materialization newMaterialization) {
		if (newMaterialization != materialization) {
			NotificationChain msgs = null;
			if (materialization != null)
				msgs = ((InternalEObject)materialization).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ESearchPackage.DOCUMENT_MAPPING__MATERIALIZATION, null, msgs);
			if (newMaterialization != null)
				msgs = ((InternalEObject)newMaterialization).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ESearchPackage.DOCUMENT_MAPPING__MATERIALIZATION, null, msgs);
			msgs = basicSetMaterialization(newMaterialization, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.DOCUMENT_MAPPING__MATERIALIZATION, newMaterialization, newMaterialization));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<FieldMapping> getFields() {
		if (fields == null) {
			fields = new EObjectContainmentEList<FieldMapping>(FieldMapping.class, this, ESearchPackage.DOCUMENT_MAPPING__FIELDS);
		}
		return fields;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ReferenceMapping> getReferences() {
		if (references == null) {
			references = new EObjectContainmentEList<ReferenceMapping>(ReferenceMapping.class, this, ESearchPackage.DOCUMENT_MAPPING__REFERENCES);
		}
		return references;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<SuggestSource> getSuggestions() {
		if (suggestions == null) {
			suggestions = new EObjectContainmentEList<SuggestSource>(SuggestSource.class, this, ESearchPackage.DOCUMENT_MAPPING__SUGGESTIONS);
		}
		return suggestions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ESearchPackage.DOCUMENT_MAPPING__MATERIALIZATION:
				return basicSetMaterialization(null, msgs);
			case ESearchPackage.DOCUMENT_MAPPING__FIELDS:
				return ((InternalEList<?>)getFields()).basicRemove(otherEnd, msgs);
			case ESearchPackage.DOCUMENT_MAPPING__REFERENCES:
				return ((InternalEList<?>)getReferences()).basicRemove(otherEnd, msgs);
			case ESearchPackage.DOCUMENT_MAPPING__SUGGESTIONS:
				return ((InternalEList<?>)getSuggestions()).basicRemove(otherEnd, msgs);
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
			case ESearchPackage.DOCUMENT_MAPPING__ECLASS:
				if (resolve) return getEClass();
				return basicGetEClass();
			case ESearchPackage.DOCUMENT_MAPPING__TYPE_NAME:
				return getTypeName();
			case ESearchPackage.DOCUMENT_MAPPING__ID_FEATURE:
				if (resolve) return getIdFeature();
				return basicGetIdFeature();
			case ESearchPackage.DOCUMENT_MAPPING__AUTO_MAP:
				return isAutoMap();
			case ESearchPackage.DOCUMENT_MAPPING__ANALYZER:
				if (resolve) return getAnalyzer();
				return basicGetAnalyzer();
			case ESearchPackage.DOCUMENT_MAPPING__MATERIALIZATION:
				return getMaterialization();
			case ESearchPackage.DOCUMENT_MAPPING__FIELDS:
				return getFields();
			case ESearchPackage.DOCUMENT_MAPPING__REFERENCES:
				return getReferences();
			case ESearchPackage.DOCUMENT_MAPPING__SUGGESTIONS:
				return getSuggestions();
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
			case ESearchPackage.DOCUMENT_MAPPING__ECLASS:
				setEClass((EClass)newValue);
				return;
			case ESearchPackage.DOCUMENT_MAPPING__TYPE_NAME:
				setTypeName((String)newValue);
				return;
			case ESearchPackage.DOCUMENT_MAPPING__ID_FEATURE:
				setIdFeature((EAttribute)newValue);
				return;
			case ESearchPackage.DOCUMENT_MAPPING__AUTO_MAP:
				setAutoMap((Boolean)newValue);
				return;
			case ESearchPackage.DOCUMENT_MAPPING__ANALYZER:
				setAnalyzer((AnalyzerDefinition)newValue);
				return;
			case ESearchPackage.DOCUMENT_MAPPING__MATERIALIZATION:
				setMaterialization((Materialization)newValue);
				return;
			case ESearchPackage.DOCUMENT_MAPPING__FIELDS:
				getFields().clear();
				getFields().addAll((Collection<? extends FieldMapping>)newValue);
				return;
			case ESearchPackage.DOCUMENT_MAPPING__REFERENCES:
				getReferences().clear();
				getReferences().addAll((Collection<? extends ReferenceMapping>)newValue);
				return;
			case ESearchPackage.DOCUMENT_MAPPING__SUGGESTIONS:
				getSuggestions().clear();
				getSuggestions().addAll((Collection<? extends SuggestSource>)newValue);
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
			case ESearchPackage.DOCUMENT_MAPPING__ECLASS:
				setEClass((EClass)null);
				return;
			case ESearchPackage.DOCUMENT_MAPPING__TYPE_NAME:
				setTypeName(TYPE_NAME_EDEFAULT);
				return;
			case ESearchPackage.DOCUMENT_MAPPING__ID_FEATURE:
				setIdFeature((EAttribute)null);
				return;
			case ESearchPackage.DOCUMENT_MAPPING__AUTO_MAP:
				setAutoMap(AUTO_MAP_EDEFAULT);
				return;
			case ESearchPackage.DOCUMENT_MAPPING__ANALYZER:
				setAnalyzer((AnalyzerDefinition)null);
				return;
			case ESearchPackage.DOCUMENT_MAPPING__MATERIALIZATION:
				setMaterialization((Materialization)null);
				return;
			case ESearchPackage.DOCUMENT_MAPPING__FIELDS:
				getFields().clear();
				return;
			case ESearchPackage.DOCUMENT_MAPPING__REFERENCES:
				getReferences().clear();
				return;
			case ESearchPackage.DOCUMENT_MAPPING__SUGGESTIONS:
				getSuggestions().clear();
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
			case ESearchPackage.DOCUMENT_MAPPING__ECLASS:
				return eClass != null;
			case ESearchPackage.DOCUMENT_MAPPING__TYPE_NAME:
				return TYPE_NAME_EDEFAULT == null ? typeName != null : !TYPE_NAME_EDEFAULT.equals(typeName);
			case ESearchPackage.DOCUMENT_MAPPING__ID_FEATURE:
				return idFeature != null;
			case ESearchPackage.DOCUMENT_MAPPING__AUTO_MAP:
				return autoMap != AUTO_MAP_EDEFAULT;
			case ESearchPackage.DOCUMENT_MAPPING__ANALYZER:
				return analyzer != null;
			case ESearchPackage.DOCUMENT_MAPPING__MATERIALIZATION:
				return materialization != null;
			case ESearchPackage.DOCUMENT_MAPPING__FIELDS:
				return fields != null && !fields.isEmpty();
			case ESearchPackage.DOCUMENT_MAPPING__REFERENCES:
				return references != null && !references.isEmpty();
			case ESearchPackage.DOCUMENT_MAPPING__SUGGESTIONS:
				return suggestions != null && !suggestions.isEmpty();
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
		result.append(" (typeName: ");
		result.append(typeName);
		result.append(", autoMap: ");
		result.append(autoMap);
		result.append(')');
		return result.toString();
	}

} //DocumentMappingImpl
