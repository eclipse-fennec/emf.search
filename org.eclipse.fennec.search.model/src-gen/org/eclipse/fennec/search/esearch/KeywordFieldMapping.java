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
 * A representation of the model object '<em><b>Keyword Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Unanalyzed field indexed as a single term — the convention default for id attributes, enums and booleans. This is the field kind that answers exact equality, IN, prefix/wildcard matching and sorting.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.KeywordFieldMapping#getNormalizer <em>Normalizer</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getKeywordFieldMapping()
 * @model
 * @generated
 */
@ProviderType
public interface KeywordFieldMapping extends FieldMapping {
	/**
	 * Returns the value of the '<em><b>Normalizer</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optional normalization (lowercasing, ASCII folding) applied to the whole value before it becomes a term. This is what makes CASE_INSENSITIVE matching answerable without a wildcard scan.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Normalizer</em>' reference.
	 * @see #setNormalizer(AnalyzerDefinition)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getKeywordFieldMapping_Normalizer()
	 * @model
	 * @generated
	 */
	AnalyzerDefinition getNormalizer();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.KeywordFieldMapping#getNormalizer <em>Normalizer</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Normalizer</em>' reference.
	 * @see #getNormalizer()
	 * @generated
	 */
	void setNormalizer(AnalyzerDefinition value);

} // KeywordFieldMapping
