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
 * A representation of the model object '<em><b>Materialization</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Upgrades how a hit becomes an EObject again. Without this element a hit is partially reconstructed from stored fields and incomplete by design (docs/search-access.md §4.3); declaring it either stores the complete serialized object (STORED_OBJECT) or a pointer into the primary store (SOURCE_URI). Presence of this element is the switch.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.Materialization#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.Materialization#getFieldName <em>Field Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.Materialization#getFormat <em>Format</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getMaterialization()
 * @model
 * @generated
 */
@ProviderType
public interface Materialization extends EObject {
	/**
	 * Returns the value of the '<em><b>Kind</b></em>' attribute.
	 * The default value is <code>"STORED_OBJECT"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.search.esearch.MaterializationKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Which upgrade this document gets. STORED_OBJECT is the only kind that makes UPDATE_BY_SELECTOR possible for the class, because an update must rebuild the complete document.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.MaterializationKind
	 * @see #setKind(MaterializationKind)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getMaterialization_Kind()
	 * @model default="STORED_OBJECT"
	 * @generated
	 */
	MaterializationKind getKind();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.Materialization#getKind <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.MaterializationKind
	 * @see #getKind()
	 * @generated
	 */
	void setKind(MaterializationKind value);

	/**
	 * Returns the value of the '<em><b>Field Name</b></em>' attribute.
	 * The default value is <code>"_source"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Stored field holding the serialized object bytes (STORED_OBJECT) or the original URI (SOURCE_URI).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Field Name</em>' attribute.
	 * @see #setFieldName(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getMaterialization_FieldName()
	 * @model default="_source"
	 * @generated
	 */
	String getFieldName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.Materialization#getFieldName <em>Field Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Field Name</em>' attribute.
	 * @see #getFieldName()
	 * @generated
	 */
	void setFieldName(String value);

	/**
	 * Returns the value of the '<em><b>Format</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * ObjectSerializer id used for STORED_OBJECT (for example binary). Unset uses the backend default, binary. Changing it invalidates stored objects written with the previous format. Ignored for SOURCE_URI.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Format</em>' attribute.
	 * @see #setFormat(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getMaterialization_Format()
	 * @model
	 * @generated
	 */
	String getFormat();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.Materialization#getFormat <em>Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Format</em>' attribute.
	 * @see #getFormat()
	 * @generated
	 */
	void setFormat(String value);

} // Materialization
