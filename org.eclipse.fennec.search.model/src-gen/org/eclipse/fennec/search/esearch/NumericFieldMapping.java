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

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Numeric Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Point field plus doc values — the convention default for numeric and temporal attributes. Answers comparisons and ranges; doc values additionally allow sorting and numeric facets.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.NumericFieldMapping#getKind <em>Kind</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getNumericFieldMapping()
 * @model
 * @generated
 */
@ProviderType
public interface NumericFieldMapping extends FieldMapping {
	/**
	 * Returns the value of the '<em><b>Kind</b></em>' attribute.
	 * The default value is <code>"AUTO"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.search.esearch.NumericKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Point encoding. AUTO derives it from the attribute's EDataType; set it explicitly only to widen or to force a temporal encoding.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.NumericKind
	 * @see #setKind(NumericKind)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getNumericFieldMapping_Kind()
	 * @model default="AUTO"
	 * @generated
	 */
	NumericKind getKind();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.NumericFieldMapping#getKind <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.NumericKind
	 * @see #getKind()
	 * @generated
	 */
	void setKind(NumericKind value);

} // NumericFieldMapping
