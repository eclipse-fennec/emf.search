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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Suggest Source</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A suggestion source built from this document. Sharing the mapping model and the index lifecycle — but not the query API — is the correction of the predecessor's parallel suggest stack.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.SuggestSource#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.SuggestSource#getFeature <em>Feature</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.SuggestSource#getWeight <em>Weight</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.SuggestSource#getContexts <em>Contexts</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.SuggestSource#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.SuggestSource#getAnalyzer <em>Analyzer</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSuggestSource()
 * @model
 * @generated
 */
@ProviderType
public interface SuggestSource extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Name under which consumers request this suggester.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSuggestSource_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.SuggestSource#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The attribute whose values are suggested.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Feature</em>' reference.
	 * @see #setFeature(EAttribute)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSuggestSource_Feature()
	 * @model required="true"
	 * @generated
	 */
	EAttribute getFeature();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.SuggestSource#getFeature <em>Feature</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Feature</em>' reference.
	 * @see #getFeature()
	 * @generated
	 */
	void setFeature(EAttribute value);

	/**
	 * Returns the value of the '<em><b>Weight</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Numeric attribute ranking the suggestions. Unset weights all entries equally.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Weight</em>' reference.
	 * @see #setWeight(EAttribute)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSuggestSource_Weight()
	 * @model
	 * @generated
	 */
	EAttribute getWeight();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.SuggestSource#getWeight <em>Weight</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Weight</em>' reference.
	 * @see #getWeight()
	 * @generated
	 */
	void setWeight(EAttribute value);

	/**
	 * Returns the value of the '<em><b>Contexts</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EAttribute}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Attributes used as filter contexts, so suggestions can be restricted at request time (per tenant, per category).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Contexts</em>' reference list.
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSuggestSource_Contexts()
	 * @model
	 * @generated
	 */
	EList<EAttribute> getContexts();

	/**
	 * Returns the value of the '<em><b>Kind</b></em>' attribute.
	 * The default value is <code>"ANALYZING"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.search.esearch.SuggesterKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Suggester implementation.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.SuggesterKind
	 * @see #setKind(SuggesterKind)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSuggestSource_Kind()
	 * @model default="ANALYZING" required="true"
	 * @generated
	 */
	SuggesterKind getKind();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.SuggestSource#getKind <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.SuggesterKind
	 * @see #getKind()
	 * @generated
	 */
	void setKind(SuggesterKind value);

	/**
	 * Returns the value of the '<em><b>Analyzer</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Analyzer for the suggester's own index. Unset uses the document analyzer.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Analyzer</em>' reference.
	 * @see #setAnalyzer(AnalyzerDefinition)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSuggestSource_Analyzer()
	 * @model
	 * @generated
	 */
	AnalyzerDefinition getAnalyzer();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.SuggestSource#getAnalyzer <em>Analyzer</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Analyzer</em>' reference.
	 * @see #getAnalyzer()
	 * @generated
	 */
	void setAnalyzer(AnalyzerDefinition value);

} // SuggestSource
