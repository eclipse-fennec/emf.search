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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectResolvingEList;

import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Reference Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.ReferenceMappingImpl#getEReference <em>EReference</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.ReferenceMappingImpl#getStrategy <em>Strategy</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.ReferenceMappingImpl#getPrefix <em>Prefix</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.ReferenceMappingImpl#getDepth <em>Depth</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.ReferenceMappingImpl#getTarget <em>Target</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.ReferenceMappingImpl#getIncludes <em>Includes</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ReferenceMappingImpl extends MinimalEObjectImpl.Container implements ReferenceMapping {
	/**
	 * The cached value of the '{@link #getEReference() <em>EReference</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEReference()
	 * @generated
	 * @ordered
	 */
	protected EReference eReference;

	/**
	 * The default value of the '{@link #getStrategy() <em>Strategy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStrategy()
	 * @generated
	 * @ordered
	 */
	protected static final ReferenceStrategy STRATEGY_EDEFAULT = ReferenceStrategy.ID_ONLY;

	/**
	 * The cached value of the '{@link #getStrategy() <em>Strategy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStrategy()
	 * @generated
	 * @ordered
	 */
	protected ReferenceStrategy strategy = STRATEGY_EDEFAULT;

	/**
	 * The default value of the '{@link #getPrefix() <em>Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrefix()
	 * @generated
	 * @ordered
	 */
	protected static final String PREFIX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPrefix() <em>Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrefix()
	 * @generated
	 * @ordered
	 */
	protected String prefix = PREFIX_EDEFAULT;

	/**
	 * The default value of the '{@link #getDepth() <em>Depth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDepth()
	 * @generated
	 * @ordered
	 */
	protected static final int DEPTH_EDEFAULT = 1;

	/**
	 * The cached value of the '{@link #getDepth() <em>Depth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDepth()
	 * @generated
	 * @ordered
	 */
	protected int depth = DEPTH_EDEFAULT;

	/**
	 * The cached value of the '{@link #getTarget() <em>Target</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTarget()
	 * @generated
	 * @ordered
	 */
	protected DocumentMapping target;

	/**
	 * The cached value of the '{@link #getIncludes() <em>Includes</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIncludes()
	 * @generated
	 * @ordered
	 */
	protected EList<EStructuralFeature> includes;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ReferenceMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.REFERENCE_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEReference() {
		if (eReference != null && eReference.eIsProxy()) {
			InternalEObject oldEReference = (InternalEObject)eReference;
			eReference = (EReference)eResolveProxy(oldEReference);
			if (eReference != oldEReference) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.REFERENCE_MAPPING__EREFERENCE, oldEReference, eReference));
			}
		}
		return eReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference basicGetEReference() {
		return eReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEReference(EReference newEReference) {
		EReference oldEReference = eReference;
		eReference = newEReference;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.REFERENCE_MAPPING__EREFERENCE, oldEReference, eReference));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReferenceStrategy getStrategy() {
		return strategy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStrategy(ReferenceStrategy newStrategy) {
		ReferenceStrategy oldStrategy = strategy;
		strategy = newStrategy == null ? STRATEGY_EDEFAULT : newStrategy;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.REFERENCE_MAPPING__STRATEGY, oldStrategy, strategy));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getPrefix() {
		return prefix;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPrefix(String newPrefix) {
		String oldPrefix = prefix;
		prefix = newPrefix;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.REFERENCE_MAPPING__PREFIX, oldPrefix, prefix));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getDepth() {
		return depth;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDepth(int newDepth) {
		int oldDepth = depth;
		depth = newDepth;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.REFERENCE_MAPPING__DEPTH, oldDepth, depth));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DocumentMapping getTarget() {
		if (target != null && target.eIsProxy()) {
			InternalEObject oldTarget = (InternalEObject)target;
			target = (DocumentMapping)eResolveProxy(oldTarget);
			if (target != oldTarget) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ESearchPackage.REFERENCE_MAPPING__TARGET, oldTarget, target));
			}
		}
		return target;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DocumentMapping basicGetTarget() {
		return target;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTarget(DocumentMapping newTarget) {
		DocumentMapping oldTarget = target;
		target = newTarget;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.REFERENCE_MAPPING__TARGET, oldTarget, target));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<EStructuralFeature> getIncludes() {
		if (includes == null) {
			includes = new EObjectResolvingEList<EStructuralFeature>(EStructuralFeature.class, this, ESearchPackage.REFERENCE_MAPPING__INCLUDES);
		}
		return includes;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ESearchPackage.REFERENCE_MAPPING__EREFERENCE:
				if (resolve) return getEReference();
				return basicGetEReference();
			case ESearchPackage.REFERENCE_MAPPING__STRATEGY:
				return getStrategy();
			case ESearchPackage.REFERENCE_MAPPING__PREFIX:
				return getPrefix();
			case ESearchPackage.REFERENCE_MAPPING__DEPTH:
				return getDepth();
			case ESearchPackage.REFERENCE_MAPPING__TARGET:
				if (resolve) return getTarget();
				return basicGetTarget();
			case ESearchPackage.REFERENCE_MAPPING__INCLUDES:
				return getIncludes();
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
			case ESearchPackage.REFERENCE_MAPPING__EREFERENCE:
				setEReference((EReference)newValue);
				return;
			case ESearchPackage.REFERENCE_MAPPING__STRATEGY:
				setStrategy((ReferenceStrategy)newValue);
				return;
			case ESearchPackage.REFERENCE_MAPPING__PREFIX:
				setPrefix((String)newValue);
				return;
			case ESearchPackage.REFERENCE_MAPPING__DEPTH:
				setDepth((Integer)newValue);
				return;
			case ESearchPackage.REFERENCE_MAPPING__TARGET:
				setTarget((DocumentMapping)newValue);
				return;
			case ESearchPackage.REFERENCE_MAPPING__INCLUDES:
				getIncludes().clear();
				getIncludes().addAll((Collection<? extends EStructuralFeature>)newValue);
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
			case ESearchPackage.REFERENCE_MAPPING__EREFERENCE:
				setEReference((EReference)null);
				return;
			case ESearchPackage.REFERENCE_MAPPING__STRATEGY:
				setStrategy(STRATEGY_EDEFAULT);
				return;
			case ESearchPackage.REFERENCE_MAPPING__PREFIX:
				setPrefix(PREFIX_EDEFAULT);
				return;
			case ESearchPackage.REFERENCE_MAPPING__DEPTH:
				setDepth(DEPTH_EDEFAULT);
				return;
			case ESearchPackage.REFERENCE_MAPPING__TARGET:
				setTarget((DocumentMapping)null);
				return;
			case ESearchPackage.REFERENCE_MAPPING__INCLUDES:
				getIncludes().clear();
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
			case ESearchPackage.REFERENCE_MAPPING__EREFERENCE:
				return eReference != null;
			case ESearchPackage.REFERENCE_MAPPING__STRATEGY:
				return strategy != STRATEGY_EDEFAULT;
			case ESearchPackage.REFERENCE_MAPPING__PREFIX:
				return PREFIX_EDEFAULT == null ? prefix != null : !PREFIX_EDEFAULT.equals(prefix);
			case ESearchPackage.REFERENCE_MAPPING__DEPTH:
				return depth != DEPTH_EDEFAULT;
			case ESearchPackage.REFERENCE_MAPPING__TARGET:
				return target != null;
			case ESearchPackage.REFERENCE_MAPPING__INCLUDES:
				return includes != null && !includes.isEmpty();
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
		result.append(" (strategy: ");
		result.append(strategy);
		result.append(", prefix: ");
		result.append(prefix);
		result.append(", depth: ");
		result.append(depth);
		result.append(')');
		return result.toString();
	}

} //ReferenceMappingImpl
