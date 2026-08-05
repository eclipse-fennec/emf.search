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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Index Sort</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Physical ordering of the index. Queries sorted the same way can terminate early; everything else is unaffected. Fixed at creation time.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.IndexSort#getEntries <em>Entries</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getIndexSort()
 * @model
 * @generated
 */
@ProviderType
public interface IndexSort extends EObject {
	/**
	 * Returns the value of the '<em><b>Entries</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.search.esearch.SortEntry}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Sort keys, in order of precedence.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Entries</em>' containment reference list.
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getIndexSort_Entries()
	 * @model containment="true" required="true"
	 * @generated
	 */
	EList<SortEntry> getEntries();

} // IndexSort
