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
 * A representation of the model object '<em><b>Refresh Policy</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * When writes become visible to searchers. This is the honest limit of the standalone role: visibility is near-real-time, not read-your-writes, and the contract says so rather than pretending otherwise.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.RefreshPolicy#getMode <em>Mode</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.RefreshPolicy#getIntervalMillis <em>Interval Millis</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getRefreshPolicy()
 * @model
 * @generated
 */
@ProviderType
public interface RefreshPolicy extends EObject {
	/**
	 * Returns the value of the '<em><b>Mode</b></em>' attribute.
	 * The default value is <code>"NEAR_REAL_TIME"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.search.esearch.RefreshMode}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Refresh mode.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Mode</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.RefreshMode
	 * @see #setMode(RefreshMode)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getRefreshPolicy_Mode()
	 * @model default="NEAR_REAL_TIME" required="true"
	 * @generated
	 */
	RefreshMode getMode();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.RefreshPolicy#getMode <em>Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mode</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.RefreshMode
	 * @see #getMode()
	 * @generated
	 */
	void setMode(RefreshMode value);

	/**
	 * Returns the value of the '<em><b>Interval Millis</b></em>' attribute.
	 * The default value is <code>"1000"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * NEAR_REAL_TIME only: maximum time a write stays invisible. Lower values cost searcher reopens.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Interval Millis</em>' attribute.
	 * @see #setIntervalMillis(long)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getRefreshPolicy_IntervalMillis()
	 * @model default="1000"
	 * @generated
	 */
	long getIntervalMillis();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.RefreshPolicy#getIntervalMillis <em>Interval Millis</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Interval Millis</em>' attribute.
	 * @see #getIntervalMillis()
	 * @generated
	 */
	void setIntervalMillis(long value);

} // RefreshPolicy
