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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sort Entry</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * One key of the index sort. The referenced attribute must have doc values.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.SortEntry#getFeature <em>Feature</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.SortEntry#isDescending <em>Descending</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.SortEntry#isMissingLast <em>Missing Last</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSortEntry()
 * @model
 * @generated
 */
@ProviderType
public interface SortEntry extends EObject {
	/**
	 * Returns the value of the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The attribute to sort by.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Feature</em>' reference.
	 * @see #setFeature(EAttribute)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSortEntry_Feature()
	 * @model required="true"
	 * @generated
	 */
	EAttribute getFeature();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.SortEntry#getFeature <em>Feature</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Feature</em>' reference.
	 * @see #getFeature()
	 * @generated
	 */
	void setFeature(EAttribute value);

	/**
	 * Returns the value of the '<em><b>Descending</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Sort direction.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Descending</em>' attribute.
	 * @see #setDescending(boolean)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSortEntry_Descending()
	 * @model default="false"
	 * @generated
	 */
	boolean isDescending();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.SortEntry#isDescending <em>Descending</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Descending</em>' attribute.
	 * @see #isDescending()
	 * @generated
	 */
	void setDescending(boolean value);

	/**
	 * Returns the value of the '<em><b>Missing Last</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Where documents without a value for this key are placed. Fixed with the index sort itself.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Missing Last</em>' attribute.
	 * @see #setMissingLast(boolean)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSortEntry_MissingLast()
	 * @model default="true"
	 * @generated
	 */
	boolean isMissingLast();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.SortEntry#isMissingLast <em>Missing Last</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Missing Last</em>' attribute.
	 * @see #isMissingLast()
	 * @generated
	 */
	void setMissingLast(boolean value);

} // SortEntry
