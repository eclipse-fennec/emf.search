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

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.search.esearch.ESearchPackage
 * @generated
 */
@ProviderType
public interface ESearchFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ESearchFactory eINSTANCE = org.eclipse.fennec.search.esearch.impl.ESearchFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Search Mapping Registry</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Search Mapping Registry</em>'.
	 * @generated
	 */
	SearchMappingRegistry createSearchMappingRegistry();

	/**
	 * Returns a new object of class '<em>Index Unit Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Index Unit Mapping</em>'.
	 * @generated
	 */
	IndexUnitMapping createIndexUnitMapping();

	/**
	 * Returns a new object of class '<em>Document Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Document Mapping</em>'.
	 * @generated
	 */
	DocumentMapping createDocumentMapping();

	/**
	 * Returns a new object of class '<em>Text Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Text Field Mapping</em>'.
	 * @generated
	 */
	TextFieldMapping createTextFieldMapping();

	/**
	 * Returns a new object of class '<em>Keyword Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Keyword Field Mapping</em>'.
	 * @generated
	 */
	KeywordFieldMapping createKeywordFieldMapping();

	/**
	 * Returns a new object of class '<em>Numeric Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Numeric Field Mapping</em>'.
	 * @generated
	 */
	NumericFieldMapping createNumericFieldMapping();

	/**
	 * Returns a new object of class '<em>Geo Point Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Geo Point Field Mapping</em>'.
	 * @generated
	 */
	GeoPointFieldMapping createGeoPointFieldMapping();

	/**
	 * Returns a new object of class '<em>Range Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Range Field Mapping</em>'.
	 * @generated
	 */
	RangeFieldMapping createRangeFieldMapping();

	/**
	 * Returns a new object of class '<em>Rank Signal Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Rank Signal Field Mapping</em>'.
	 * @generated
	 */
	RankSignalFieldMapping createRankSignalFieldMapping();

	/**
	 * Returns a new object of class '<em>Vector Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Vector Field Mapping</em>'.
	 * @generated
	 */
	VectorFieldMapping createVectorFieldMapping();

	/**
	 * Returns a new object of class '<em>Feature Source</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Feature Source</em>'.
	 * @generated
	 */
	FeatureSource createFeatureSource();

	/**
	 * Returns a new object of class '<em>Path Source</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Path Source</em>'.
	 * @generated
	 */
	PathSource createPathSource();

	/**
	 * Returns a new object of class '<em>Ocl Source</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Ocl Source</em>'.
	 * @generated
	 */
	OclSource createOclSource();

	/**
	 * Returns a new object of class '<em>Reference Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Reference Mapping</em>'.
	 * @generated
	 */
	ReferenceMapping createReferenceMapping();

	/**
	 * Returns a new object of class '<em>Materialization</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Materialization</em>'.
	 * @generated
	 */
	Materialization createMaterialization();

	/**
	 * Returns a new object of class '<em>Facet Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Facet Mapping</em>'.
	 * @generated
	 */
	FacetMapping createFacetMapping();

	/**
	 * Returns a new object of class '<em>Suggest Source</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Suggest Source</em>'.
	 * @generated
	 */
	SuggestSource createSuggestSource();

	/**
	 * Returns a new object of class '<em>Refresh Policy</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Refresh Policy</em>'.
	 * @generated
	 */
	RefreshPolicy createRefreshPolicy();

	/**
	 * Returns a new object of class '<em>Commit Policy</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Commit Policy</em>'.
	 * @generated
	 */
	CommitPolicy createCommitPolicy();

	/**
	 * Returns a new object of class '<em>Index Sort</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Index Sort</em>'.
	 * @generated
	 */
	IndexSort createIndexSort();

	/**
	 * Returns a new object of class '<em>Sort Entry</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Sort Entry</em>'.
	 * @generated
	 */
	SortEntry createSortEntry();

	/**
	 * Returns a new object of class '<em>Analyzer Definition</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Analyzer Definition</em>'.
	 * @generated
	 */
	AnalyzerDefinition createAnalyzerDefinition();

	/**
	 * Returns a new object of class '<em>Analyzer Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Analyzer Parameter</em>'.
	 * @generated
	 */
	AnalyzerParameter createAnalyzerParameter();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ESearchPackage getESearchPackage();

} //ESearchFactory
