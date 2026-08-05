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
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.search.esearch.AnalyzerDefinition;
import org.eclipse.fennec.search.esearch.CommitPolicy;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.IndexSort;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.RefreshPolicy;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Index Unit Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.IndexUnitMappingImpl#getEPackage <em>EPackage</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.IndexUnitMappingImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.IndexUnitMappingImpl#getTypeField <em>Type Field</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.IndexUnitMappingImpl#isAutoMap <em>Auto Map</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.IndexUnitMappingImpl#getDefaultAnalyzer <em>Default Analyzer</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.IndexUnitMappingImpl#getRefresh <em>Refresh</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.IndexUnitMappingImpl#getCommit <em>Commit</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.IndexUnitMappingImpl#getSort <em>Sort</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.IndexUnitMappingImpl#getDocuments <em>Documents</em>}</li>
 * </ul>
 *
 * @generated
 */
public class IndexUnitMappingImpl extends MinimalEObjectImpl.Container implements IndexUnitMapping {
	/**
	 * The cached value of the '{@link #getEPackage() <em>EPackage</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEPackage()
	 * @generated
	 * @ordered
	 */
	protected EPackage ePackage;

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
	 * The default value of the '{@link #getTypeField() <em>Type Field</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTypeField()
	 * @generated
	 * @ordered
	 */
	protected static final String TYPE_FIELD_EDEFAULT = "_type";

	/**
	 * The cached value of the '{@link #getTypeField() <em>Type Field</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTypeField()
	 * @generated
	 * @ordered
	 */
	protected String typeField = TYPE_FIELD_EDEFAULT;

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
	 * The cached value of the '{@link #getDefaultAnalyzer() <em>Default Analyzer</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDefaultAnalyzer()
	 * @generated
	 * @ordered
	 */
	protected AnalyzerDefinition defaultAnalyzer;

	/**
	 * The cached value of the '{@link #getRefresh() <em>Refresh</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRefresh()
	 * @generated
	 * @ordered
	 */
	protected RefreshPolicy refresh;

	/**
	 * The cached value of the '{@link #getCommit() <em>Commit</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommit()
	 * @generated
	 * @ordered
	 */
	protected CommitPolicy commit;

	/**
	 * The cached value of the '{@link #getSort() <em>Sort</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSort()
	 * @generated
	 * @ordered
	 */
	protected IndexSort sort;

	/**
	 * The cached value of the '{@link #getDocuments() <em>Documents</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDocuments()
	 * @generated
	 * @ordered
	 */
	protected EList<DocumentMapping> documents;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IndexUnitMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.INDEX_UNIT_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EPackage getEPackage() {
		if (ePackage != null && ePackage.eIsProxy()) {
			InternalEObject oldEPackage = (InternalEObject)ePackage;
			ePackage = (EPackage)eResolveProxy(oldEPackage);
			if (ePackage != oldEPackage) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.INDEX_UNIT_MAPPING__EPACKAGE, oldEPackage, ePackage));
			}
		}
		return ePackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EPackage basicGetEPackage() {
		return ePackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEPackage(EPackage newEPackage) {
		EPackage oldEPackage = ePackage;
		ePackage = newEPackage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.INDEX_UNIT_MAPPING__EPACKAGE, oldEPackage, ePackage));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.INDEX_UNIT_MAPPING__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getTypeField() {
		return typeField;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTypeField(String newTypeField) {
		String oldTypeField = typeField;
		typeField = newTypeField;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.INDEX_UNIT_MAPPING__TYPE_FIELD, oldTypeField, typeField));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.INDEX_UNIT_MAPPING__AUTO_MAP, oldAutoMap, autoMap));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AnalyzerDefinition getDefaultAnalyzer() {
		if (defaultAnalyzer != null && defaultAnalyzer.eIsProxy()) {
			InternalEObject oldDefaultAnalyzer = (InternalEObject)defaultAnalyzer;
			defaultAnalyzer = (AnalyzerDefinition)eResolveProxy(oldDefaultAnalyzer);
			if (defaultAnalyzer != oldDefaultAnalyzer) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.INDEX_UNIT_MAPPING__DEFAULT_ANALYZER, oldDefaultAnalyzer, defaultAnalyzer));
			}
		}
		return defaultAnalyzer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AnalyzerDefinition basicGetDefaultAnalyzer() {
		return defaultAnalyzer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDefaultAnalyzer(AnalyzerDefinition newDefaultAnalyzer) {
		AnalyzerDefinition oldDefaultAnalyzer = defaultAnalyzer;
		defaultAnalyzer = newDefaultAnalyzer;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.INDEX_UNIT_MAPPING__DEFAULT_ANALYZER, oldDefaultAnalyzer, defaultAnalyzer));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RefreshPolicy getRefresh() {
		return refresh;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRefresh(RefreshPolicy newRefresh, NotificationChain msgs) {
		RefreshPolicy oldRefresh = refresh;
		refresh = newRefresh;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ESearchPackage.INDEX_UNIT_MAPPING__REFRESH, oldRefresh, newRefresh);
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
	public void setRefresh(RefreshPolicy newRefresh) {
		if (newRefresh != refresh) {
			NotificationChain msgs = null;
			if (refresh != null)
				msgs = ((InternalEObject)refresh).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ESearchPackage.INDEX_UNIT_MAPPING__REFRESH, null, msgs);
			if (newRefresh != null)
				msgs = ((InternalEObject)newRefresh).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ESearchPackage.INDEX_UNIT_MAPPING__REFRESH, null, msgs);
			msgs = basicSetRefresh(newRefresh, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.INDEX_UNIT_MAPPING__REFRESH, newRefresh, newRefresh));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CommitPolicy getCommit() {
		return commit;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetCommit(CommitPolicy newCommit, NotificationChain msgs) {
		CommitPolicy oldCommit = commit;
		commit = newCommit;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ESearchPackage.INDEX_UNIT_MAPPING__COMMIT, oldCommit, newCommit);
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
	public void setCommit(CommitPolicy newCommit) {
		if (newCommit != commit) {
			NotificationChain msgs = null;
			if (commit != null)
				msgs = ((InternalEObject)commit).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ESearchPackage.INDEX_UNIT_MAPPING__COMMIT, null, msgs);
			if (newCommit != null)
				msgs = ((InternalEObject)newCommit).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ESearchPackage.INDEX_UNIT_MAPPING__COMMIT, null, msgs);
			msgs = basicSetCommit(newCommit, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.INDEX_UNIT_MAPPING__COMMIT, newCommit, newCommit));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IndexSort getSort() {
		return sort;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSort(IndexSort newSort, NotificationChain msgs) {
		IndexSort oldSort = sort;
		sort = newSort;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ESearchPackage.INDEX_UNIT_MAPPING__SORT, oldSort, newSort);
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
	public void setSort(IndexSort newSort) {
		if (newSort != sort) {
			NotificationChain msgs = null;
			if (sort != null)
				msgs = ((InternalEObject)sort).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ESearchPackage.INDEX_UNIT_MAPPING__SORT, null, msgs);
			if (newSort != null)
				msgs = ((InternalEObject)newSort).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ESearchPackage.INDEX_UNIT_MAPPING__SORT, null, msgs);
			msgs = basicSetSort(newSort, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.INDEX_UNIT_MAPPING__SORT, newSort, newSort));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<DocumentMapping> getDocuments() {
		if (documents == null) {
			documents = new EObjectContainmentEList<DocumentMapping>(DocumentMapping.class, this, ESearchPackage.INDEX_UNIT_MAPPING__DOCUMENTS);
		}
		return documents;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ESearchPackage.INDEX_UNIT_MAPPING__REFRESH:
				return basicSetRefresh(null, msgs);
			case ESearchPackage.INDEX_UNIT_MAPPING__COMMIT:
				return basicSetCommit(null, msgs);
			case ESearchPackage.INDEX_UNIT_MAPPING__SORT:
				return basicSetSort(null, msgs);
			case ESearchPackage.INDEX_UNIT_MAPPING__DOCUMENTS:
				return ((InternalEList<?>)getDocuments()).basicRemove(otherEnd, msgs);
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
			case ESearchPackage.INDEX_UNIT_MAPPING__EPACKAGE:
				if (resolve) return getEPackage();
				return basicGetEPackage();
			case ESearchPackage.INDEX_UNIT_MAPPING__NAME:
				return getName();
			case ESearchPackage.INDEX_UNIT_MAPPING__TYPE_FIELD:
				return getTypeField();
			case ESearchPackage.INDEX_UNIT_MAPPING__AUTO_MAP:
				return isAutoMap();
			case ESearchPackage.INDEX_UNIT_MAPPING__DEFAULT_ANALYZER:
				if (resolve) return getDefaultAnalyzer();
				return basicGetDefaultAnalyzer();
			case ESearchPackage.INDEX_UNIT_MAPPING__REFRESH:
				return getRefresh();
			case ESearchPackage.INDEX_UNIT_MAPPING__COMMIT:
				return getCommit();
			case ESearchPackage.INDEX_UNIT_MAPPING__SORT:
				return getSort();
			case ESearchPackage.INDEX_UNIT_MAPPING__DOCUMENTS:
				return getDocuments();
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
			case ESearchPackage.INDEX_UNIT_MAPPING__EPACKAGE:
				setEPackage((EPackage)newValue);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__NAME:
				setName((String)newValue);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__TYPE_FIELD:
				setTypeField((String)newValue);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__AUTO_MAP:
				setAutoMap((Boolean)newValue);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__DEFAULT_ANALYZER:
				setDefaultAnalyzer((AnalyzerDefinition)newValue);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__REFRESH:
				setRefresh((RefreshPolicy)newValue);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__COMMIT:
				setCommit((CommitPolicy)newValue);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__SORT:
				setSort((IndexSort)newValue);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__DOCUMENTS:
				getDocuments().clear();
				getDocuments().addAll((Collection<? extends DocumentMapping>)newValue);
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
			case ESearchPackage.INDEX_UNIT_MAPPING__EPACKAGE:
				setEPackage((EPackage)null);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__TYPE_FIELD:
				setTypeField(TYPE_FIELD_EDEFAULT);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__AUTO_MAP:
				setAutoMap(AUTO_MAP_EDEFAULT);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__DEFAULT_ANALYZER:
				setDefaultAnalyzer((AnalyzerDefinition)null);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__REFRESH:
				setRefresh((RefreshPolicy)null);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__COMMIT:
				setCommit((CommitPolicy)null);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__SORT:
				setSort((IndexSort)null);
				return;
			case ESearchPackage.INDEX_UNIT_MAPPING__DOCUMENTS:
				getDocuments().clear();
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
			case ESearchPackage.INDEX_UNIT_MAPPING__EPACKAGE:
				return ePackage != null;
			case ESearchPackage.INDEX_UNIT_MAPPING__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ESearchPackage.INDEX_UNIT_MAPPING__TYPE_FIELD:
				return TYPE_FIELD_EDEFAULT == null ? typeField != null : !TYPE_FIELD_EDEFAULT.equals(typeField);
			case ESearchPackage.INDEX_UNIT_MAPPING__AUTO_MAP:
				return autoMap != AUTO_MAP_EDEFAULT;
			case ESearchPackage.INDEX_UNIT_MAPPING__DEFAULT_ANALYZER:
				return defaultAnalyzer != null;
			case ESearchPackage.INDEX_UNIT_MAPPING__REFRESH:
				return refresh != null;
			case ESearchPackage.INDEX_UNIT_MAPPING__COMMIT:
				return commit != null;
			case ESearchPackage.INDEX_UNIT_MAPPING__SORT:
				return sort != null;
			case ESearchPackage.INDEX_UNIT_MAPPING__DOCUMENTS:
				return documents != null && !documents.isEmpty();
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
		result.append(", typeField: ");
		result.append(typeField);
		result.append(", autoMap: ");
		result.append(autoMap);
		result.append(')');
		return result.toString();
	}

} //IndexUnitMappingImpl
