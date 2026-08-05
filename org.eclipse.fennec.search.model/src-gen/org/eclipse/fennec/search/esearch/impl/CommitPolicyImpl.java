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

import org.eclipse.fennec.search.esearch.CommitPolicy;
import org.eclipse.fennec.search.esearch.ESearchPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Commit Policy</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.CommitPolicyImpl#getMaxUncommittedDocs <em>Max Uncommitted Docs</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.CommitPolicyImpl#getMaxIntervalMillis <em>Max Interval Millis</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.CommitPolicyImpl#isCommitOnClose <em>Commit On Close</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CommitPolicyImpl extends MinimalEObjectImpl.Container implements CommitPolicy {
	/**
	 * The default value of the '{@link #getMaxUncommittedDocs() <em>Max Uncommitted Docs</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxUncommittedDocs()
	 * @generated
	 * @ordered
	 */
	protected static final int MAX_UNCOMMITTED_DOCS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getMaxUncommittedDocs() <em>Max Uncommitted Docs</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxUncommittedDocs()
	 * @generated
	 * @ordered
	 */
	protected int maxUncommittedDocs = MAX_UNCOMMITTED_DOCS_EDEFAULT;

	/**
	 * The default value of the '{@link #getMaxIntervalMillis() <em>Max Interval Millis</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxIntervalMillis()
	 * @generated
	 * @ordered
	 */
	protected static final long MAX_INTERVAL_MILLIS_EDEFAULT = 0L;

	/**
	 * The cached value of the '{@link #getMaxIntervalMillis() <em>Max Interval Millis</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxIntervalMillis()
	 * @generated
	 * @ordered
	 */
	protected long maxIntervalMillis = MAX_INTERVAL_MILLIS_EDEFAULT;

	/**
	 * The default value of the '{@link #isCommitOnClose() <em>Commit On Close</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isCommitOnClose()
	 * @generated
	 * @ordered
	 */
	protected static final boolean COMMIT_ON_CLOSE_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isCommitOnClose() <em>Commit On Close</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isCommitOnClose()
	 * @generated
	 * @ordered
	 */
	protected boolean commitOnClose = COMMIT_ON_CLOSE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CommitPolicyImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.COMMIT_POLICY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getMaxUncommittedDocs() {
		return maxUncommittedDocs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMaxUncommittedDocs(int newMaxUncommittedDocs) {
		int oldMaxUncommittedDocs = maxUncommittedDocs;
		maxUncommittedDocs = newMaxUncommittedDocs;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.COMMIT_POLICY__MAX_UNCOMMITTED_DOCS, oldMaxUncommittedDocs, maxUncommittedDocs));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public long getMaxIntervalMillis() {
		return maxIntervalMillis;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMaxIntervalMillis(long newMaxIntervalMillis) {
		long oldMaxIntervalMillis = maxIntervalMillis;
		maxIntervalMillis = newMaxIntervalMillis;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.COMMIT_POLICY__MAX_INTERVAL_MILLIS, oldMaxIntervalMillis, maxIntervalMillis));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isCommitOnClose() {
		return commitOnClose;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCommitOnClose(boolean newCommitOnClose) {
		boolean oldCommitOnClose = commitOnClose;
		commitOnClose = newCommitOnClose;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.COMMIT_POLICY__COMMIT_ON_CLOSE, oldCommitOnClose, commitOnClose));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ESearchPackage.COMMIT_POLICY__MAX_UNCOMMITTED_DOCS:
				return getMaxUncommittedDocs();
			case ESearchPackage.COMMIT_POLICY__MAX_INTERVAL_MILLIS:
				return getMaxIntervalMillis();
			case ESearchPackage.COMMIT_POLICY__COMMIT_ON_CLOSE:
				return isCommitOnClose();
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
			case ESearchPackage.COMMIT_POLICY__MAX_UNCOMMITTED_DOCS:
				setMaxUncommittedDocs((Integer)newValue);
				return;
			case ESearchPackage.COMMIT_POLICY__MAX_INTERVAL_MILLIS:
				setMaxIntervalMillis((Long)newValue);
				return;
			case ESearchPackage.COMMIT_POLICY__COMMIT_ON_CLOSE:
				setCommitOnClose((Boolean)newValue);
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
			case ESearchPackage.COMMIT_POLICY__MAX_UNCOMMITTED_DOCS:
				setMaxUncommittedDocs(MAX_UNCOMMITTED_DOCS_EDEFAULT);
				return;
			case ESearchPackage.COMMIT_POLICY__MAX_INTERVAL_MILLIS:
				setMaxIntervalMillis(MAX_INTERVAL_MILLIS_EDEFAULT);
				return;
			case ESearchPackage.COMMIT_POLICY__COMMIT_ON_CLOSE:
				setCommitOnClose(COMMIT_ON_CLOSE_EDEFAULT);
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
			case ESearchPackage.COMMIT_POLICY__MAX_UNCOMMITTED_DOCS:
				return maxUncommittedDocs != MAX_UNCOMMITTED_DOCS_EDEFAULT;
			case ESearchPackage.COMMIT_POLICY__MAX_INTERVAL_MILLIS:
				return maxIntervalMillis != MAX_INTERVAL_MILLIS_EDEFAULT;
			case ESearchPackage.COMMIT_POLICY__COMMIT_ON_CLOSE:
				return commitOnClose != COMMIT_ON_CLOSE_EDEFAULT;
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
		result.append(" (maxUncommittedDocs: ");
		result.append(maxUncommittedDocs);
		result.append(", maxIntervalMillis: ");
		result.append(maxIntervalMillis);
		result.append(", commitOnClose: ");
		result.append(commitOnClose);
		result.append(')');
		return result.toString();
	}

} //CommitPolicyImpl
