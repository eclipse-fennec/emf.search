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
 * Stores the serialized EObject alongside its indexed fields, so a hit can be returned as a complete object without consulting a primary store. This is what makes the standalone role self-sufficient; as a secondary index next to JPA or Mongo it is usually wasted space.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.Materialization#isStoreObject <em>Store Object</em>}</li>
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
	 * Returns the value of the '<em><b>Store Object</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Store the serialized EObject. False makes this element a no-op and is only useful to switch materialization off for one document while the surrounding configuration keeps it on.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Store Object</em>' attribute.
	 * @see #setStoreObject(boolean)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getMaterialization_StoreObject()
	 * @model default="true" required="true"
	 * @generated
	 */
	boolean isStoreObject();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.Materialization#isStoreObject <em>Store Object</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Store Object</em>' attribute.
	 * @see #isStoreObject()
	 * @generated
	 */
	void setStoreObject(boolean value);

	/**
	 * Returns the value of the '<em><b>Field Name</b></em>' attribute.
	 * The default value is <code>"_source"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Stored field holding the serialized object.
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
	 * Fennec codec format used for serialization (for example json or bson). Unset uses the backend default. Changing it invalidates stored objects written with the previous format.
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
