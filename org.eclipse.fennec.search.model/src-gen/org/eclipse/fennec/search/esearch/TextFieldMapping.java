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
 * A representation of the model object '<em><b>Text Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Analyzed full-text field — the convention default for string attributes. Matching is analyzed: a WHERE_STRING_MATCH translates to an analyzed match, not to a wildcard over the raw value.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.TextFieldMapping#getAnalyzer <em>Analyzer</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.TextFieldMapping#isTermVectors <em>Term Vectors</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getTextFieldMapping()
 * @model
 * @generated
 */
@ProviderType
public interface TextFieldMapping extends FieldMapping {
	/**
	 * Returns the value of the '<em><b>Analyzer</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Per-field analyzer, overriding the document and unit defaults.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Analyzer</em>' reference.
	 * @see #setAnalyzer(AnalyzerDefinition)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getTextFieldMapping_Analyzer()
	 * @model
	 * @generated
	 */
	AnalyzerDefinition getAnalyzer();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.TextFieldMapping#getAnalyzer <em>Analyzer</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Analyzer</em>' reference.
	 * @see #getAnalyzer()
	 * @generated
	 */
	void setAnalyzer(AnalyzerDefinition value);

	/**
	 * Returns the value of the '<em><b>Term Vectors</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Store term vectors with positions and offsets. Costs index size; makes highlighting and MoreLikeThis similarity substantially cheaper.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Term Vectors</em>' attribute.
	 * @see #setTermVectors(boolean)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getTextFieldMapping_TermVectors()
	 * @model default="false"
	 * @generated
	 */
	boolean isTermVectors();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.TextFieldMapping#isTermVectors <em>Term Vectors</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Term Vectors</em>' attribute.
	 * @see #isTermVectors()
	 * @generated
	 */
	void setTermVectors(boolean value);

} // TextFieldMapping
