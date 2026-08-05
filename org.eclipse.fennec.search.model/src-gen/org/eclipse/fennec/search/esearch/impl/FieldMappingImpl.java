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

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.FacetMapping;
import org.eclipse.fennec.search.esearch.FieldMapping;
import org.eclipse.fennec.search.esearch.FieldUse;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.FieldMappingImpl#getFeature <em>Feature</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.FieldMappingImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.FieldMappingImpl#isIndexed <em>Indexed</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.FieldMappingImpl#isStored <em>Stored</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.FieldMappingImpl#isDocValues <em>Doc Values</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.FieldMappingImpl#getBoost <em>Boost</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.FieldMappingImpl#getFacet <em>Facet</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.FieldMappingImpl#getUse <em>Use</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.FieldMappingImpl#getSubFields <em>Sub Fields</em>}</li>
 * </ul>
 *
 * @generated
 */
public abstract class FieldMappingImpl extends MinimalEObjectImpl.Container implements FieldMapping {
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
	 * The default value of the '{@link #isIndexed() <em>Indexed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIndexed()
	 * @generated
	 * @ordered
	 */
	protected static final boolean INDEXED_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isIndexed() <em>Indexed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIndexed()
	 * @generated
	 * @ordered
	 */
	protected boolean indexed = INDEXED_EDEFAULT;

	/**
	 * The default value of the '{@link #isStored() <em>Stored</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isStored()
	 * @generated
	 * @ordered
	 */
	protected static final boolean STORED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isStored() <em>Stored</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isStored()
	 * @generated
	 * @ordered
	 */
	protected boolean stored = STORED_EDEFAULT;

	/**
	 * The default value of the '{@link #isDocValues() <em>Doc Values</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDocValues()
	 * @generated
	 * @ordered
	 */
	protected static final boolean DOC_VALUES_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isDocValues() <em>Doc Values</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDocValues()
	 * @generated
	 * @ordered
	 */
	protected boolean docValues = DOC_VALUES_EDEFAULT;

	/**
	 * The default value of the '{@link #getBoost() <em>Boost</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBoost()
	 * @generated
	 * @ordered
	 */
	protected static final float BOOST_EDEFAULT = 1.0F;

	/**
	 * The cached value of the '{@link #getBoost() <em>Boost</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBoost()
	 * @generated
	 * @ordered
	 */
	protected float boost = BOOST_EDEFAULT;

	/**
	 * The cached value of the '{@link #getFacet() <em>Facet</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFacet()
	 * @generated
	 * @ordered
	 */
	protected FacetMapping facet;

	/**
	 * The cached value of the '{@link #getUse() <em>Use</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUse()
	 * @generated
	 * @ordered
	 */
	protected EList<FieldUse> use;

	/**
	 * The cached value of the '{@link #getSubFields() <em>Sub Fields</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSubFields()
	 * @generated
	 * @ordered
	 */
	protected EList<FieldMapping> subFields;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FieldMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.FIELD_MAPPING;
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.FIELD_MAPPING__FEATURE, oldFeature, feature));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.FIELD_MAPPING__FEATURE, oldFeature, feature));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.FIELD_MAPPING__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isIndexed() {
		return indexed;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIndexed(boolean newIndexed) {
		boolean oldIndexed = indexed;
		indexed = newIndexed;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.FIELD_MAPPING__INDEXED, oldIndexed, indexed));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isStored() {
		return stored;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStored(boolean newStored) {
		boolean oldStored = stored;
		stored = newStored;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.FIELD_MAPPING__STORED, oldStored, stored));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isDocValues() {
		return docValues;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDocValues(boolean newDocValues) {
		boolean oldDocValues = docValues;
		docValues = newDocValues;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.FIELD_MAPPING__DOC_VALUES, oldDocValues, docValues));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public float getBoost() {
		return boost;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setBoost(float newBoost) {
		float oldBoost = boost;
		boost = newBoost;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.FIELD_MAPPING__BOOST, oldBoost, boost));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FacetMapping getFacet() {
		return facet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFacet(FacetMapping newFacet, NotificationChain msgs) {
		FacetMapping oldFacet = facet;
		facet = newFacet;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ESearchPackage.FIELD_MAPPING__FACET, oldFacet, newFacet);
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
	public void setFacet(FacetMapping newFacet) {
		if (newFacet != facet) {
			NotificationChain msgs = null;
			if (facet != null)
				msgs = ((InternalEObject)facet).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ESearchPackage.FIELD_MAPPING__FACET, null, msgs);
			if (newFacet != null)
				msgs = ((InternalEObject)newFacet).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ESearchPackage.FIELD_MAPPING__FACET, null, msgs);
			msgs = basicSetFacet(newFacet, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.FIELD_MAPPING__FACET, newFacet, newFacet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<FieldUse> getUse() {
		if (use == null) {
			use = new EDataTypeUniqueEList<FieldUse>(FieldUse.class, this, ESearchPackage.FIELD_MAPPING__USE);
		}
		return use;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<FieldMapping> getSubFields() {
		if (subFields == null) {
			subFields = new EObjectContainmentEList<FieldMapping>(FieldMapping.class, this, ESearchPackage.FIELD_MAPPING__SUB_FIELDS);
		}
		return subFields;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ESearchPackage.FIELD_MAPPING__FACET:
				return basicSetFacet(null, msgs);
			case ESearchPackage.FIELD_MAPPING__SUB_FIELDS:
				return ((InternalEList<?>)getSubFields()).basicRemove(otherEnd, msgs);
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
			case ESearchPackage.FIELD_MAPPING__FEATURE:
				if (resolve) return getFeature();
				return basicGetFeature();
			case ESearchPackage.FIELD_MAPPING__NAME:
				return getName();
			case ESearchPackage.FIELD_MAPPING__INDEXED:
				return isIndexed();
			case ESearchPackage.FIELD_MAPPING__STORED:
				return isStored();
			case ESearchPackage.FIELD_MAPPING__DOC_VALUES:
				return isDocValues();
			case ESearchPackage.FIELD_MAPPING__BOOST:
				return getBoost();
			case ESearchPackage.FIELD_MAPPING__FACET:
				return getFacet();
			case ESearchPackage.FIELD_MAPPING__USE:
				return getUse();
			case ESearchPackage.FIELD_MAPPING__SUB_FIELDS:
				return getSubFields();
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
			case ESearchPackage.FIELD_MAPPING__FEATURE:
				setFeature((EAttribute)newValue);
				return;
			case ESearchPackage.FIELD_MAPPING__NAME:
				setName((String)newValue);
				return;
			case ESearchPackage.FIELD_MAPPING__INDEXED:
				setIndexed((Boolean)newValue);
				return;
			case ESearchPackage.FIELD_MAPPING__STORED:
				setStored((Boolean)newValue);
				return;
			case ESearchPackage.FIELD_MAPPING__DOC_VALUES:
				setDocValues((Boolean)newValue);
				return;
			case ESearchPackage.FIELD_MAPPING__BOOST:
				setBoost((Float)newValue);
				return;
			case ESearchPackage.FIELD_MAPPING__FACET:
				setFacet((FacetMapping)newValue);
				return;
			case ESearchPackage.FIELD_MAPPING__USE:
				getUse().clear();
				getUse().addAll((Collection<? extends FieldUse>)newValue);
				return;
			case ESearchPackage.FIELD_MAPPING__SUB_FIELDS:
				getSubFields().clear();
				getSubFields().addAll((Collection<? extends FieldMapping>)newValue);
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
			case ESearchPackage.FIELD_MAPPING__FEATURE:
				setFeature((EAttribute)null);
				return;
			case ESearchPackage.FIELD_MAPPING__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ESearchPackage.FIELD_MAPPING__INDEXED:
				setIndexed(INDEXED_EDEFAULT);
				return;
			case ESearchPackage.FIELD_MAPPING__STORED:
				setStored(STORED_EDEFAULT);
				return;
			case ESearchPackage.FIELD_MAPPING__DOC_VALUES:
				setDocValues(DOC_VALUES_EDEFAULT);
				return;
			case ESearchPackage.FIELD_MAPPING__BOOST:
				setBoost(BOOST_EDEFAULT);
				return;
			case ESearchPackage.FIELD_MAPPING__FACET:
				setFacet((FacetMapping)null);
				return;
			case ESearchPackage.FIELD_MAPPING__USE:
				getUse().clear();
				return;
			case ESearchPackage.FIELD_MAPPING__SUB_FIELDS:
				getSubFields().clear();
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
			case ESearchPackage.FIELD_MAPPING__FEATURE:
				return feature != null;
			case ESearchPackage.FIELD_MAPPING__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ESearchPackage.FIELD_MAPPING__INDEXED:
				return indexed != INDEXED_EDEFAULT;
			case ESearchPackage.FIELD_MAPPING__STORED:
				return stored != STORED_EDEFAULT;
			case ESearchPackage.FIELD_MAPPING__DOC_VALUES:
				return docValues != DOC_VALUES_EDEFAULT;
			case ESearchPackage.FIELD_MAPPING__BOOST:
				return boost != BOOST_EDEFAULT;
			case ESearchPackage.FIELD_MAPPING__FACET:
				return facet != null;
			case ESearchPackage.FIELD_MAPPING__USE:
				return use != null && !use.isEmpty();
			case ESearchPackage.FIELD_MAPPING__SUB_FIELDS:
				return subFields != null && !subFields.isEmpty();
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
		result.append(", indexed: ");
		result.append(indexed);
		result.append(", stored: ");
		result.append(stored);
		result.append(", docValues: ");
		result.append(docValues);
		result.append(", boost: ");
		result.append(boost);
		result.append(", use: ");
		result.append(use);
		result.append(')');
		return result.toString();
	}

} //FieldMappingImpl
