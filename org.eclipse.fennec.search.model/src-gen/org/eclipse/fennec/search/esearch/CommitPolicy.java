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
package org.eclipse.fennec.search.esearch;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Commit Policy</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * When the writer commits to disk. Note that the commit is also where the applied stream position is recorded (live commit data) — that is runtime behaviour, not declared here, but it is the reason commit cadence matters for the secondary-index role.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.CommitPolicy#getMaxUncommittedDocs <em>Max Uncommitted Docs</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.CommitPolicy#getMaxIntervalMillis <em>Max Interval Millis</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.CommitPolicy#isCommitOnClose <em>Commit On Close</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getCommitPolicy()
 * @model
 * @generated
 */
@ProviderType
public interface CommitPolicy extends EObject {
	/**
	 * Returns the value of the '<em><b>Max Uncommitted Docs</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Commit after this many uncommitted documents. 0 disables the trigger.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Max Uncommitted Docs</em>' attribute.
	 * @see #setMaxUncommittedDocs(int)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getCommitPolicy_MaxUncommittedDocs()
	 * @model default="0"
	 * @generated
	 */
	int getMaxUncommittedDocs();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.CommitPolicy#getMaxUncommittedDocs <em>Max Uncommitted Docs</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max Uncommitted Docs</em>' attribute.
	 * @see #getMaxUncommittedDocs()
	 * @generated
	 */
	void setMaxUncommittedDocs(int value);

	/**
	 * Returns the value of the '<em><b>Max Interval Millis</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Commit at most this long after the previous commit. 0 disables the trigger.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Max Interval Millis</em>' attribute.
	 * @see #setMaxIntervalMillis(long)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getCommitPolicy_MaxIntervalMillis()
	 * @model default="0"
	 * @generated
	 */
	long getMaxIntervalMillis();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.CommitPolicy#getMaxIntervalMillis <em>Max Interval Millis</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max Interval Millis</em>' attribute.
	 * @see #getMaxIntervalMillis()
	 * @generated
	 */
	void setMaxIntervalMillis(long value);

	/**
	 * Returns the value of the '<em><b>Commit On Close</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Commit when the unit is closed. Switching this off means an unclean shutdown loses everything since the last commit — including the recorded stream position.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Commit On Close</em>' attribute.
	 * @see #setCommitOnClose(boolean)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getCommitPolicy_CommitOnClose()
	 * @model default="true"
	 * @generated
	 */
	boolean isCommitOnClose();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.CommitPolicy#isCommitOnClose <em>Commit On Close</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Commit On Close</em>' attribute.
	 * @see #isCommitOnClose()
	 * @generated
	 */
	void setCommitOnClose(boolean value);

} // CommitPolicy
