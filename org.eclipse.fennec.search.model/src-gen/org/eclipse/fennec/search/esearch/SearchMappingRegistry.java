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
 * A representation of the model object '<em><b>Search Mapping Registry</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Root container. Reusable analyzer definitions are declared once here and referenced from the mappings by non-containment reference (the TrackingRegistry / sensinact PersistenceRuleRegistry pattern).
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.SearchMappingRegistry#getUnits <em>Units</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.SearchMappingRegistry#getAnalyzers <em>Analyzers</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSearchMappingRegistry()
 * @model
 * @generated
 */
@ProviderType
public interface SearchMappingRegistry extends EObject {
	/**
	 * Returns the value of the '<em><b>Units</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.search.esearch.IndexUnitMapping}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The index units declared by this registry.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Units</em>' containment reference list.
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSearchMappingRegistry_Units()
	 * @model containment="true"
	 * @generated
	 */
	EList<IndexUnitMapping> getUnits();

	/**
	 * Returns the value of the '<em><b>Analyzers</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.search.esearch.AnalyzerDefinition}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reusable analyzer definitions, referenced from IndexUnitMapping.defaultAnalyzer, DocumentMapping.analyzer, TextFieldMapping.analyzer, KeywordFieldMapping.normalizer and SuggestSource.analyzer.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Analyzers</em>' containment reference list.
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSearchMappingRegistry_Analyzers()
	 * @model containment="true"
	 * @generated
	 */
	EList<AnalyzerDefinition> getAnalyzers();

} // SearchMappingRegistry
