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
 * A representation of the model object '<em><b>Analyzer Definition</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A named analyzer, resolved against the analyzer registry: a programmatic registry in plain Java, the service whiteboard in OSGi. Both answers exist by construction — an analyzer that only resolves inside a framework would make the core untestable without one.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition#getLanguage <em>Language</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition#getServiceFilter <em>Service Filter</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition#getParameters <em>Parameters</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getAnalyzerDefinition()
 * @model
 * @generated
 */
@ProviderType
public interface AnalyzerDefinition extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Registry key referenced by the mappings.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getAnalyzerDefinition_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Kind</b></em>' attribute.
	 * The default value is <code>"STANDARD"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.search.esearch.AnalyzerKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Built-in analyzer, or CUSTOM to resolve one from the registry.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.AnalyzerKind
	 * @see #setKind(AnalyzerKind)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getAnalyzerDefinition_Kind()
	 * @model default="STANDARD" required="true"
	 * @generated
	 */
	AnalyzerKind getKind();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition#getKind <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.AnalyzerKind
	 * @see #getKind()
	 * @generated
	 */
	void setKind(AnalyzerKind value);

	/**
	 * Returns the value of the '<em><b>Language</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optional language tag for language-specific built-in analyzers.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Language</em>' attribute.
	 * @see #setLanguage(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getAnalyzerDefinition_Language()
	 * @model
	 * @generated
	 */
	String getLanguage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition#getLanguage <em>Language</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Language</em>' attribute.
	 * @see #getLanguage()
	 * @generated
	 */
	void setLanguage(String value);

	/**
	 * Returns the value of the '<em><b>Service Filter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * OSGi only: LDAP filter selecting the analyzer service. Ignored in plain Java, where the name resolves in the programmatic registry — the declaration must work in both worlds, so this narrows the lookup rather than replacing it.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Service Filter</em>' attribute.
	 * @see #setServiceFilter(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getAnalyzerDefinition_ServiceFilter()
	 * @model
	 * @generated
	 */
	String getServiceFilter();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition#getServiceFilter <em>Service Filter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Service Filter</em>' attribute.
	 * @see #getServiceFilter()
	 * @generated
	 */
	void setServiceFilter(String value);

	/**
	 * Returns the value of the '<em><b>Parameters</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.search.esearch.AnalyzerParameter}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Configuration for CUSTOM analyzers, passed to the resolved factory.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parameters</em>' containment reference list.
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getAnalyzerDefinition_Parameters()
	 * @model containment="true"
	 * @generated
	 */
	EList<AnalyzerParameter> getParameters();

} // AnalyzerDefinition
