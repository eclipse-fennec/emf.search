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
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.FacetKind;
import org.eclipse.fennec.search.esearch.FacetMapping;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Facet Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.FacetMappingImpl#getDimension <em>Dimension</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.FacetMappingImpl#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.FacetMappingImpl#isHierarchical <em>Hierarchical</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.FacetMappingImpl#isMultiValued <em>Multi Valued</em>}</li>
 * </ul>
 *
 * @generated
 */
public class FacetMappingImpl extends MinimalEObjectImpl.Container implements FacetMapping {
	/**
	 * The default value of the '{@link #getDimension() <em>Dimension</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDimension()
	 * @generated
	 * @ordered
	 */
	protected static final String DIMENSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDimension() <em>Dimension</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDimension()
	 * @generated
	 * @ordered
	 */
	protected String dimension = DIMENSION_EDEFAULT;

	/**
	 * The default value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected static final FacetKind KIND_EDEFAULT = FacetKind.SORTED_SET;

	/**
	 * The cached value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected FacetKind kind = KIND_EDEFAULT;

	/**
	 * The default value of the '{@link #isHierarchical() <em>Hierarchical</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHierarchical()
	 * @generated
	 * @ordered
	 */
	protected static final boolean HIERARCHICAL_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isHierarchical() <em>Hierarchical</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHierarchical()
	 * @generated
	 * @ordered
	 */
	protected boolean hierarchical = HIERARCHICAL_EDEFAULT;

	/**
	 * The default value of the '{@link #isMultiValued() <em>Multi Valued</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isMultiValued()
	 * @generated
	 * @ordered
	 */
	protected static final boolean MULTI_VALUED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isMultiValued() <em>Multi Valued</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isMultiValued()
	 * @generated
	 * @ordered
	 */
	protected boolean multiValued = MULTI_VALUED_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FacetMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.FACET_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDimension() {
		return dimension;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDimension(String newDimension) {
		String oldDimension = dimension;
		dimension = newDimension;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.FACET_MAPPING__DIMENSION, oldDimension, dimension));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FacetKind getKind() {
		return kind;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setKind(FacetKind newKind) {
		FacetKind oldKind = kind;
		kind = newKind == null ? KIND_EDEFAULT : newKind;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.FACET_MAPPING__KIND, oldKind, kind));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isHierarchical() {
		return hierarchical;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setHierarchical(boolean newHierarchical) {
		boolean oldHierarchical = hierarchical;
		hierarchical = newHierarchical;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.FACET_MAPPING__HIERARCHICAL, oldHierarchical, hierarchical));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isMultiValued() {
		return multiValued;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMultiValued(boolean newMultiValued) {
		boolean oldMultiValued = multiValued;
		multiValued = newMultiValued;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.FACET_MAPPING__MULTI_VALUED, oldMultiValued, multiValued));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ESearchPackage.FACET_MAPPING__DIMENSION:
				return getDimension();
			case ESearchPackage.FACET_MAPPING__KIND:
				return getKind();
			case ESearchPackage.FACET_MAPPING__HIERARCHICAL:
				return isHierarchical();
			case ESearchPackage.FACET_MAPPING__MULTI_VALUED:
				return isMultiValued();
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
			case ESearchPackage.FACET_MAPPING__DIMENSION:
				setDimension((String)newValue);
				return;
			case ESearchPackage.FACET_MAPPING__KIND:
				setKind((FacetKind)newValue);
				return;
			case ESearchPackage.FACET_MAPPING__HIERARCHICAL:
				setHierarchical((Boolean)newValue);
				return;
			case ESearchPackage.FACET_MAPPING__MULTI_VALUED:
				setMultiValued((Boolean)newValue);
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
			case ESearchPackage.FACET_MAPPING__DIMENSION:
				setDimension(DIMENSION_EDEFAULT);
				return;
			case ESearchPackage.FACET_MAPPING__KIND:
				setKind(KIND_EDEFAULT);
				return;
			case ESearchPackage.FACET_MAPPING__HIERARCHICAL:
				setHierarchical(HIERARCHICAL_EDEFAULT);
				return;
			case ESearchPackage.FACET_MAPPING__MULTI_VALUED:
				setMultiValued(MULTI_VALUED_EDEFAULT);
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
			case ESearchPackage.FACET_MAPPING__DIMENSION:
				return DIMENSION_EDEFAULT == null ? dimension != null : !DIMENSION_EDEFAULT.equals(dimension);
			case ESearchPackage.FACET_MAPPING__KIND:
				return kind != KIND_EDEFAULT;
			case ESearchPackage.FACET_MAPPING__HIERARCHICAL:
				return hierarchical != HIERARCHICAL_EDEFAULT;
			case ESearchPackage.FACET_MAPPING__MULTI_VALUED:
				return multiValued != MULTI_VALUED_EDEFAULT;
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
		result.append(" (dimension: ");
		result.append(dimension);
		result.append(", kind: ");
		result.append(kind);
		result.append(", hierarchical: ");
		result.append(hierarchical);
		result.append(", multiValued: ");
		result.append(multiValued);
		result.append(')');
		return result.toString();
	}

} //FacetMappingImpl
