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
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Declarative EClass-to-Lucene-index mapping for emf.search — "eorm for the index" (docs/search-access.md §4). Instances declare how EObjects become Lucene documents: index unit policies, per-feature field mappings, reference strategies, facets, rank signals, suggest sources.
 * 
 * Two rules shape this metamodel:
 * 
 * 1. Conventions over declarations. An unmapped EClass or EAttribute is mapped by convention (id -> stored keyword, strings -> analyzed text, numerics -> point + doc values), so small models need no mapping instance at all. A declaration is an override, never a prerequisite.
 * 
 * 2. Physical properties belong to the index unit, logical ones to the document. One unit is one Lucene directory with one IndexWriter, so refresh policy, commit policy and index sort are declared once per unit — not per EClass, which could not be honoured independently. Everything that describes a single document (fields, references, materialization) hangs off DocumentMapping.
 * 
 * Delivery uses the two mechanisms emf.osgi already provides, not a third one: an authored XMI document (*.esearch) is loaded into a named EObject registry (org.eclipse.fennec.emf.osgi.eobject.registry, which has a non-OSGi bootstrap so the plain-Java path is identical), and a model bundle can ship its own mapping as an AspectEntry with typeId "esearch" on the package metadata — the same slot the codec and orm aspects use.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.search.esearch.ESearchFactory
 * @model kind="package"
 *        annotation="Version value='1.0'"
 * @generated
 */
@ProviderType
@EPackage(uri = ESearchPackage.eNS_URI, fingerprint = "fp1:622f46015319bc800e39d29d0c2cd3fccc39bf2ae7c187e9f4c2701f3598c9c1", genModel = "/model/esearch.genmodel", genModelSourceLocations = {"model/esearch.genmodel","org.eclipse.fennec.search.model/model/esearch.genmodel"}, ecore = "/model/esearch.ecore", ecoreSourceLocations = "/model/esearch.ecore")
public interface ESearchPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "esearch";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://eclipse.org/fennec/search/esearch/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "esearch";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ESearchPackage eINSTANCE = org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.SearchMappingRegistryImpl <em>Search Mapping Registry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.SearchMappingRegistryImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getSearchMappingRegistry()
	 * @generated
	 */
	int SEARCH_MAPPING_REGISTRY = 0;

	/**
	 * The feature id for the '<em><b>Units</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEARCH_MAPPING_REGISTRY__UNITS = 0;

	/**
	 * The feature id for the '<em><b>Analyzers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEARCH_MAPPING_REGISTRY__ANALYZERS = 1;

	/**
	 * The number of structural features of the '<em>Search Mapping Registry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEARCH_MAPPING_REGISTRY_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Search Mapping Registry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SEARCH_MAPPING_REGISTRY_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.IndexUnitMappingImpl <em>Index Unit Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.IndexUnitMappingImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getIndexUnitMapping()
	 * @generated
	 */
	int INDEX_UNIT_MAPPING = 1;

	/**
	 * The feature id for the '<em><b>EPackage</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INDEX_UNIT_MAPPING__EPACKAGE = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INDEX_UNIT_MAPPING__NAME = 1;

	/**
	 * The feature id for the '<em><b>Type Field</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INDEX_UNIT_MAPPING__TYPE_FIELD = 2;

	/**
	 * The feature id for the '<em><b>Auto Map</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INDEX_UNIT_MAPPING__AUTO_MAP = 3;

	/**
	 * The feature id for the '<em><b>Default Analyzer</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INDEX_UNIT_MAPPING__DEFAULT_ANALYZER = 4;

	/**
	 * The feature id for the '<em><b>Refresh</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INDEX_UNIT_MAPPING__REFRESH = 5;

	/**
	 * The feature id for the '<em><b>Commit</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INDEX_UNIT_MAPPING__COMMIT = 6;

	/**
	 * The feature id for the '<em><b>Sort</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INDEX_UNIT_MAPPING__SORT = 7;

	/**
	 * The feature id for the '<em><b>Documents</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INDEX_UNIT_MAPPING__DOCUMENTS = 8;

	/**
	 * The number of structural features of the '<em>Index Unit Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INDEX_UNIT_MAPPING_FEATURE_COUNT = 9;

	/**
	 * The number of operations of the '<em>Index Unit Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INDEX_UNIT_MAPPING_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.DocumentMappingImpl <em>Document Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.DocumentMappingImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getDocumentMapping()
	 * @generated
	 */
	int DOCUMENT_MAPPING = 2;

	/**
	 * The feature id for the '<em><b>EClass</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_MAPPING__ECLASS = 0;

	/**
	 * The feature id for the '<em><b>Type Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_MAPPING__TYPE_NAME = 1;

	/**
	 * The feature id for the '<em><b>Id Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_MAPPING__ID_FEATURE = 2;

	/**
	 * The feature id for the '<em><b>Auto Map</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_MAPPING__AUTO_MAP = 3;

	/**
	 * The feature id for the '<em><b>Analyzer</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_MAPPING__ANALYZER = 4;

	/**
	 * The feature id for the '<em><b>Materialization</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_MAPPING__MATERIALIZATION = 5;

	/**
	 * The feature id for the '<em><b>Fields</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_MAPPING__FIELDS = 6;

	/**
	 * The feature id for the '<em><b>References</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_MAPPING__REFERENCES = 7;

	/**
	 * The feature id for the '<em><b>Suggestions</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_MAPPING__SUGGESTIONS = 8;

	/**
	 * The number of structural features of the '<em>Document Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_MAPPING_FEATURE_COUNT = 9;

	/**
	 * The number of operations of the '<em>Document Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_MAPPING_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.FieldMappingImpl <em>Field Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.FieldMappingImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getFieldMapping()
	 * @generated
	 */
	int FIELD_MAPPING = 3;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__FEATURE = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__NAME = 1;

	/**
	 * The feature id for the '<em><b>Indexed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__INDEXED = 2;

	/**
	 * The feature id for the '<em><b>Stored</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__STORED = 3;

	/**
	 * The feature id for the '<em><b>Doc Values</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__DOC_VALUES = 4;

	/**
	 * The feature id for the '<em><b>Boost</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__BOOST = 5;

	/**
	 * The feature id for the '<em><b>Facet</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__FACET = 6;

	/**
	 * The feature id for the '<em><b>Use</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__USE = 7;

	/**
	 * The feature id for the '<em><b>Sources</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__SOURCES = 8;

	/**
	 * The feature id for the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__SEPARATOR = 9;

	/**
	 * The feature id for the '<em><b>Sub Fields</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__SUB_FIELDS = 10;

	/**
	 * The number of structural features of the '<em>Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING_FEATURE_COUNT = 11;

	/**
	 * The number of operations of the '<em>Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.TextFieldMappingImpl <em>Text Field Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.TextFieldMappingImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getTextFieldMapping()
	 * @generated
	 */
	int TEXT_FIELD_MAPPING = 4;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING__FEATURE = FIELD_MAPPING__FEATURE;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING__NAME = FIELD_MAPPING__NAME;

	/**
	 * The feature id for the '<em><b>Indexed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING__INDEXED = FIELD_MAPPING__INDEXED;

	/**
	 * The feature id for the '<em><b>Stored</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING__STORED = FIELD_MAPPING__STORED;

	/**
	 * The feature id for the '<em><b>Doc Values</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING__DOC_VALUES = FIELD_MAPPING__DOC_VALUES;

	/**
	 * The feature id for the '<em><b>Boost</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING__BOOST = FIELD_MAPPING__BOOST;

	/**
	 * The feature id for the '<em><b>Facet</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING__FACET = FIELD_MAPPING__FACET;

	/**
	 * The feature id for the '<em><b>Use</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING__USE = FIELD_MAPPING__USE;

	/**
	 * The feature id for the '<em><b>Sources</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING__SOURCES = FIELD_MAPPING__SOURCES;

	/**
	 * The feature id for the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING__SEPARATOR = FIELD_MAPPING__SEPARATOR;

	/**
	 * The feature id for the '<em><b>Sub Fields</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING__SUB_FIELDS = FIELD_MAPPING__SUB_FIELDS;

	/**
	 * The feature id for the '<em><b>Analyzer</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING__ANALYZER = FIELD_MAPPING_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Term Vectors</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING__TERM_VECTORS = FIELD_MAPPING_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Text Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING_FEATURE_COUNT = FIELD_MAPPING_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Text Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEXT_FIELD_MAPPING_OPERATION_COUNT = FIELD_MAPPING_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.KeywordFieldMappingImpl <em>Keyword Field Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.KeywordFieldMappingImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getKeywordFieldMapping()
	 * @generated
	 */
	int KEYWORD_FIELD_MAPPING = 5;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEYWORD_FIELD_MAPPING__FEATURE = FIELD_MAPPING__FEATURE;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEYWORD_FIELD_MAPPING__NAME = FIELD_MAPPING__NAME;

	/**
	 * The feature id for the '<em><b>Indexed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEYWORD_FIELD_MAPPING__INDEXED = FIELD_MAPPING__INDEXED;

	/**
	 * The feature id for the '<em><b>Stored</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEYWORD_FIELD_MAPPING__STORED = FIELD_MAPPING__STORED;

	/**
	 * The feature id for the '<em><b>Doc Values</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEYWORD_FIELD_MAPPING__DOC_VALUES = FIELD_MAPPING__DOC_VALUES;

	/**
	 * The feature id for the '<em><b>Boost</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEYWORD_FIELD_MAPPING__BOOST = FIELD_MAPPING__BOOST;

	/**
	 * The feature id for the '<em><b>Facet</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEYWORD_FIELD_MAPPING__FACET = FIELD_MAPPING__FACET;

	/**
	 * The feature id for the '<em><b>Use</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEYWORD_FIELD_MAPPING__USE = FIELD_MAPPING__USE;

	/**
	 * The feature id for the '<em><b>Sources</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEYWORD_FIELD_MAPPING__SOURCES = FIELD_MAPPING__SOURCES;

	/**
	 * The feature id for the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEYWORD_FIELD_MAPPING__SEPARATOR = FIELD_MAPPING__SEPARATOR;

	/**
	 * The feature id for the '<em><b>Sub Fields</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEYWORD_FIELD_MAPPING__SUB_FIELDS = FIELD_MAPPING__SUB_FIELDS;

	/**
	 * The feature id for the '<em><b>Normalizer</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEYWORD_FIELD_MAPPING__NORMALIZER = FIELD_MAPPING_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Keyword Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEYWORD_FIELD_MAPPING_FEATURE_COUNT = FIELD_MAPPING_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Keyword Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEYWORD_FIELD_MAPPING_OPERATION_COUNT = FIELD_MAPPING_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.NumericFieldMappingImpl <em>Numeric Field Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.NumericFieldMappingImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getNumericFieldMapping()
	 * @generated
	 */
	int NUMERIC_FIELD_MAPPING = 6;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMERIC_FIELD_MAPPING__FEATURE = FIELD_MAPPING__FEATURE;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMERIC_FIELD_MAPPING__NAME = FIELD_MAPPING__NAME;

	/**
	 * The feature id for the '<em><b>Indexed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMERIC_FIELD_MAPPING__INDEXED = FIELD_MAPPING__INDEXED;

	/**
	 * The feature id for the '<em><b>Stored</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMERIC_FIELD_MAPPING__STORED = FIELD_MAPPING__STORED;

	/**
	 * The feature id for the '<em><b>Doc Values</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMERIC_FIELD_MAPPING__DOC_VALUES = FIELD_MAPPING__DOC_VALUES;

	/**
	 * The feature id for the '<em><b>Boost</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMERIC_FIELD_MAPPING__BOOST = FIELD_MAPPING__BOOST;

	/**
	 * The feature id for the '<em><b>Facet</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMERIC_FIELD_MAPPING__FACET = FIELD_MAPPING__FACET;

	/**
	 * The feature id for the '<em><b>Use</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMERIC_FIELD_MAPPING__USE = FIELD_MAPPING__USE;

	/**
	 * The feature id for the '<em><b>Sources</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMERIC_FIELD_MAPPING__SOURCES = FIELD_MAPPING__SOURCES;

	/**
	 * The feature id for the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMERIC_FIELD_MAPPING__SEPARATOR = FIELD_MAPPING__SEPARATOR;

	/**
	 * The feature id for the '<em><b>Sub Fields</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMERIC_FIELD_MAPPING__SUB_FIELDS = FIELD_MAPPING__SUB_FIELDS;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMERIC_FIELD_MAPPING__KIND = FIELD_MAPPING_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Numeric Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMERIC_FIELD_MAPPING_FEATURE_COUNT = FIELD_MAPPING_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Numeric Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NUMERIC_FIELD_MAPPING_OPERATION_COUNT = FIELD_MAPPING_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.GeoPointFieldMappingImpl <em>Geo Point Field Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.GeoPointFieldMappingImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getGeoPointFieldMapping()
	 * @generated
	 */
	int GEO_POINT_FIELD_MAPPING = 7;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__FEATURE = FIELD_MAPPING__FEATURE;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__NAME = FIELD_MAPPING__NAME;

	/**
	 * The feature id for the '<em><b>Indexed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__INDEXED = FIELD_MAPPING__INDEXED;

	/**
	 * The feature id for the '<em><b>Stored</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__STORED = FIELD_MAPPING__STORED;

	/**
	 * The feature id for the '<em><b>Doc Values</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__DOC_VALUES = FIELD_MAPPING__DOC_VALUES;

	/**
	 * The feature id for the '<em><b>Boost</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__BOOST = FIELD_MAPPING__BOOST;

	/**
	 * The feature id for the '<em><b>Facet</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__FACET = FIELD_MAPPING__FACET;

	/**
	 * The feature id for the '<em><b>Use</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__USE = FIELD_MAPPING__USE;

	/**
	 * The feature id for the '<em><b>Sources</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__SOURCES = FIELD_MAPPING__SOURCES;

	/**
	 * The feature id for the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__SEPARATOR = FIELD_MAPPING__SEPARATOR;

	/**
	 * The feature id for the '<em><b>Sub Fields</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__SUB_FIELDS = FIELD_MAPPING__SUB_FIELDS;

	/**
	 * The feature id for the '<em><b>Latitude</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__LATITUDE = FIELD_MAPPING_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Longitude</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__LONGITUDE = FIELD_MAPPING_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Point Reference</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__POINT_REFERENCE = FIELD_MAPPING_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Coordinates</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING__COORDINATES = FIELD_MAPPING_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Geo Point Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING_FEATURE_COUNT = FIELD_MAPPING_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>Geo Point Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GEO_POINT_FIELD_MAPPING_OPERATION_COUNT = FIELD_MAPPING_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.RangeFieldMappingImpl <em>Range Field Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.RangeFieldMappingImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getRangeFieldMapping()
	 * @generated
	 */
	int RANGE_FIELD_MAPPING = 8;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING__FEATURE = FIELD_MAPPING__FEATURE;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING__NAME = FIELD_MAPPING__NAME;

	/**
	 * The feature id for the '<em><b>Indexed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING__INDEXED = FIELD_MAPPING__INDEXED;

	/**
	 * The feature id for the '<em><b>Stored</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING__STORED = FIELD_MAPPING__STORED;

	/**
	 * The feature id for the '<em><b>Doc Values</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING__DOC_VALUES = FIELD_MAPPING__DOC_VALUES;

	/**
	 * The feature id for the '<em><b>Boost</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING__BOOST = FIELD_MAPPING__BOOST;

	/**
	 * The feature id for the '<em><b>Facet</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING__FACET = FIELD_MAPPING__FACET;

	/**
	 * The feature id for the '<em><b>Use</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING__USE = FIELD_MAPPING__USE;

	/**
	 * The feature id for the '<em><b>Sources</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING__SOURCES = FIELD_MAPPING__SOURCES;

	/**
	 * The feature id for the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING__SEPARATOR = FIELD_MAPPING__SEPARATOR;

	/**
	 * The feature id for the '<em><b>Sub Fields</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING__SUB_FIELDS = FIELD_MAPPING__SUB_FIELDS;

	/**
	 * The feature id for the '<em><b>Lower Bound</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING__LOWER_BOUND = FIELD_MAPPING_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Upper Bound</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING__UPPER_BOUND = FIELD_MAPPING_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING__KIND = FIELD_MAPPING_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Range Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING_FEATURE_COUNT = FIELD_MAPPING_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Range Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANGE_FIELD_MAPPING_OPERATION_COUNT = FIELD_MAPPING_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.RankSignalFieldMappingImpl <em>Rank Signal Field Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.RankSignalFieldMappingImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getRankSignalFieldMapping()
	 * @generated
	 */
	int RANK_SIGNAL_FIELD_MAPPING = 9;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING__FEATURE = FIELD_MAPPING__FEATURE;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING__NAME = FIELD_MAPPING__NAME;

	/**
	 * The feature id for the '<em><b>Indexed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING__INDEXED = FIELD_MAPPING__INDEXED;

	/**
	 * The feature id for the '<em><b>Stored</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING__STORED = FIELD_MAPPING__STORED;

	/**
	 * The feature id for the '<em><b>Doc Values</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING__DOC_VALUES = FIELD_MAPPING__DOC_VALUES;

	/**
	 * The feature id for the '<em><b>Boost</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING__BOOST = FIELD_MAPPING__BOOST;

	/**
	 * The feature id for the '<em><b>Facet</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING__FACET = FIELD_MAPPING__FACET;

	/**
	 * The feature id for the '<em><b>Use</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING__USE = FIELD_MAPPING__USE;

	/**
	 * The feature id for the '<em><b>Sources</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING__SOURCES = FIELD_MAPPING__SOURCES;

	/**
	 * The feature id for the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING__SEPARATOR = FIELD_MAPPING__SEPARATOR;

	/**
	 * The feature id for the '<em><b>Sub Fields</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING__SUB_FIELDS = FIELD_MAPPING__SUB_FIELDS;

	/**
	 * The feature id for the '<em><b>Function</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING__FUNCTION = FIELD_MAPPING_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Pivot</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING__PIVOT = FIELD_MAPPING_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Exponent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING__EXPONENT = FIELD_MAPPING_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Rank Signal Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING_FEATURE_COUNT = FIELD_MAPPING_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Rank Signal Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RANK_SIGNAL_FIELD_MAPPING_OPERATION_COUNT = FIELD_MAPPING_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.VectorFieldMappingImpl <em>Vector Field Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.VectorFieldMappingImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getVectorFieldMapping()
	 * @generated
	 */
	int VECTOR_FIELD_MAPPING = 10;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__FEATURE = FIELD_MAPPING__FEATURE;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__NAME = FIELD_MAPPING__NAME;

	/**
	 * The feature id for the '<em><b>Indexed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__INDEXED = FIELD_MAPPING__INDEXED;

	/**
	 * The feature id for the '<em><b>Stored</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__STORED = FIELD_MAPPING__STORED;

	/**
	 * The feature id for the '<em><b>Doc Values</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__DOC_VALUES = FIELD_MAPPING__DOC_VALUES;

	/**
	 * The feature id for the '<em><b>Boost</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__BOOST = FIELD_MAPPING__BOOST;

	/**
	 * The feature id for the '<em><b>Facet</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__FACET = FIELD_MAPPING__FACET;

	/**
	 * The feature id for the '<em><b>Use</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__USE = FIELD_MAPPING__USE;

	/**
	 * The feature id for the '<em><b>Sources</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__SOURCES = FIELD_MAPPING__SOURCES;

	/**
	 * The feature id for the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__SEPARATOR = FIELD_MAPPING__SEPARATOR;

	/**
	 * The feature id for the '<em><b>Sub Fields</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__SUB_FIELDS = FIELD_MAPPING__SUB_FIELDS;

	/**
	 * The feature id for the '<em><b>Embedding Sources</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__EMBEDDING_SOURCES = FIELD_MAPPING_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Provider</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__PROVIDER = FIELD_MAPPING_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Dimensions</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__DIMENSIONS = FIELD_MAPPING_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Similarity</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__SIMILARITY = FIELD_MAPPING_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Model Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING__MODEL_VERSION = FIELD_MAPPING_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>Vector Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING_FEATURE_COUNT = FIELD_MAPPING_FEATURE_COUNT + 5;

	/**
	 * The number of operations of the '<em>Vector Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VECTOR_FIELD_MAPPING_OPERATION_COUNT = FIELD_MAPPING_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.ValueSourceImpl <em>Value Source</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.ValueSourceImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getValueSource()
	 * @generated
	 */
	int VALUE_SOURCE = 11;

	/**
	 * The number of structural features of the '<em>Value Source</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALUE_SOURCE_FEATURE_COUNT = 0;

	/**
	 * The number of operations of the '<em>Value Source</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALUE_SOURCE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.FeatureSourceImpl <em>Feature Source</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.FeatureSourceImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getFeatureSource()
	 * @generated
	 */
	int FEATURE_SOURCE = 12;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_SOURCE__FEATURE = VALUE_SOURCE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Feature Source</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_SOURCE_FEATURE_COUNT = VALUE_SOURCE_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Feature Source</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_SOURCE_OPERATION_COUNT = VALUE_SOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.PathSourceImpl <em>Path Source</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.PathSourceImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getPathSource()
	 * @generated
	 */
	int PATH_SOURCE = 13;

	/**
	 * The feature id for the '<em><b>Segments</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PATH_SOURCE__SEGMENTS = VALUE_SOURCE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Path Source</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PATH_SOURCE_FEATURE_COUNT = VALUE_SOURCE_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Path Source</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PATH_SOURCE_OPERATION_COUNT = VALUE_SOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.OclSourceImpl <em>Ocl Source</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.OclSourceImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getOclSource()
	 * @generated
	 */
	int OCL_SOURCE = 14;

	/**
	 * The feature id for the '<em><b>Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_SOURCE__EXPRESSION = VALUE_SOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_SOURCE__CONTEXT = VALUE_SOURCE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Ocl Source</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_SOURCE_FEATURE_COUNT = VALUE_SOURCE_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Ocl Source</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_SOURCE_OPERATION_COUNT = VALUE_SOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.ReferenceMappingImpl <em>Reference Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.ReferenceMappingImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getReferenceMapping()
	 * @generated
	 */
	int REFERENCE_MAPPING = 15;

	/**
	 * The feature id for the '<em><b>EReference</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_MAPPING__EREFERENCE = 0;

	/**
	 * The feature id for the '<em><b>Strategy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_MAPPING__STRATEGY = 1;

	/**
	 * The feature id for the '<em><b>Prefix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_MAPPING__PREFIX = 2;

	/**
	 * The feature id for the '<em><b>Depth</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_MAPPING__DEPTH = 3;

	/**
	 * The feature id for the '<em><b>Target</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_MAPPING__TARGET = 4;

	/**
	 * The feature id for the '<em><b>Includes</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_MAPPING__INCLUDES = 5;

	/**
	 * The number of structural features of the '<em>Reference Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_MAPPING_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Reference Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_MAPPING_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.MaterializationImpl <em>Materialization</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.MaterializationImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getMaterialization()
	 * @generated
	 */
	int MATERIALIZATION = 16;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MATERIALIZATION__KIND = 0;

	/**
	 * The feature id for the '<em><b>Field Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MATERIALIZATION__FIELD_NAME = 1;

	/**
	 * The feature id for the '<em><b>Format</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MATERIALIZATION__FORMAT = 2;

	/**
	 * The number of structural features of the '<em>Materialization</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MATERIALIZATION_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Materialization</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MATERIALIZATION_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.FacetMappingImpl <em>Facet Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.FacetMappingImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getFacetMapping()
	 * @generated
	 */
	int FACET_MAPPING = 17;

	/**
	 * The feature id for the '<em><b>Dimension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FACET_MAPPING__DIMENSION = 0;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FACET_MAPPING__KIND = 1;

	/**
	 * The feature id for the '<em><b>Hierarchical</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FACET_MAPPING__HIERARCHICAL = 2;

	/**
	 * The feature id for the '<em><b>Multi Valued</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FACET_MAPPING__MULTI_VALUED = 3;

	/**
	 * The number of structural features of the '<em>Facet Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FACET_MAPPING_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Facet Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FACET_MAPPING_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.SuggestSourceImpl <em>Suggest Source</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.SuggestSourceImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getSuggestSource()
	 * @generated
	 */
	int SUGGEST_SOURCE = 18;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUGGEST_SOURCE__NAME = 0;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUGGEST_SOURCE__FEATURE = 1;

	/**
	 * The feature id for the '<em><b>Weight</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUGGEST_SOURCE__WEIGHT = 2;

	/**
	 * The feature id for the '<em><b>Contexts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUGGEST_SOURCE__CONTEXTS = 3;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUGGEST_SOURCE__KIND = 4;

	/**
	 * The feature id for the '<em><b>Analyzer</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUGGEST_SOURCE__ANALYZER = 5;

	/**
	 * The number of structural features of the '<em>Suggest Source</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUGGEST_SOURCE_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Suggest Source</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SUGGEST_SOURCE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.RefreshPolicyImpl <em>Refresh Policy</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.RefreshPolicyImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getRefreshPolicy()
	 * @generated
	 */
	int REFRESH_POLICY = 19;

	/**
	 * The feature id for the '<em><b>Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFRESH_POLICY__MODE = 0;

	/**
	 * The feature id for the '<em><b>Interval Millis</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFRESH_POLICY__INTERVAL_MILLIS = 1;

	/**
	 * The number of structural features of the '<em>Refresh Policy</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFRESH_POLICY_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Refresh Policy</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFRESH_POLICY_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.CommitPolicyImpl <em>Commit Policy</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.CommitPolicyImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getCommitPolicy()
	 * @generated
	 */
	int COMMIT_POLICY = 20;

	/**
	 * The feature id for the '<em><b>Max Uncommitted Docs</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMIT_POLICY__MAX_UNCOMMITTED_DOCS = 0;

	/**
	 * The feature id for the '<em><b>Max Interval Millis</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMIT_POLICY__MAX_INTERVAL_MILLIS = 1;

	/**
	 * The feature id for the '<em><b>Commit On Close</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMIT_POLICY__COMMIT_ON_CLOSE = 2;

	/**
	 * The number of structural features of the '<em>Commit Policy</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMIT_POLICY_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Commit Policy</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMIT_POLICY_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.IndexSortImpl <em>Index Sort</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.IndexSortImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getIndexSort()
	 * @generated
	 */
	int INDEX_SORT = 21;

	/**
	 * The feature id for the '<em><b>Entries</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INDEX_SORT__ENTRIES = 0;

	/**
	 * The number of structural features of the '<em>Index Sort</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INDEX_SORT_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Index Sort</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INDEX_SORT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.SortEntryImpl <em>Sort Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.SortEntryImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getSortEntry()
	 * @generated
	 */
	int SORT_ENTRY = 22;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SORT_ENTRY__FEATURE = 0;

	/**
	 * The feature id for the '<em><b>Descending</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SORT_ENTRY__DESCENDING = 1;

	/**
	 * The feature id for the '<em><b>Missing Last</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SORT_ENTRY__MISSING_LAST = 2;

	/**
	 * The number of structural features of the '<em>Sort Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SORT_ENTRY_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Sort Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SORT_ENTRY_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.AnalyzerDefinitionImpl <em>Analyzer Definition</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.AnalyzerDefinitionImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getAnalyzerDefinition()
	 * @generated
	 */
	int ANALYZER_DEFINITION = 23;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANALYZER_DEFINITION__NAME = 0;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANALYZER_DEFINITION__KIND = 1;

	/**
	 * The feature id for the '<em><b>Language</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANALYZER_DEFINITION__LANGUAGE = 2;

	/**
	 * The feature id for the '<em><b>Service Filter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANALYZER_DEFINITION__SERVICE_FILTER = 3;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANALYZER_DEFINITION__PARAMETERS = 4;

	/**
	 * The number of structural features of the '<em>Analyzer Definition</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANALYZER_DEFINITION_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>Analyzer Definition</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANALYZER_DEFINITION_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.impl.AnalyzerParameterImpl <em>Analyzer Parameter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.impl.AnalyzerParameterImpl
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getAnalyzerParameter()
	 * @generated
	 */
	int ANALYZER_PARAMETER = 24;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANALYZER_PARAMETER__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANALYZER_PARAMETER__VALUE = 1;

	/**
	 * The number of structural features of the '<em>Analyzer Parameter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANALYZER_PARAMETER_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Analyzer Parameter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ANALYZER_PARAMETER_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.FieldUse <em>Field Use</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.FieldUse
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getFieldUse()
	 * @generated
	 */
	int FIELD_USE = 25;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.ReferenceStrategy <em>Reference Strategy</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.ReferenceStrategy
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getReferenceStrategy()
	 * @generated
	 */
	int REFERENCE_STRATEGY = 26;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.MaterializationKind <em>Materialization Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.MaterializationKind
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getMaterializationKind()
	 * @generated
	 */
	int MATERIALIZATION_KIND = 27;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.RefreshMode <em>Refresh Mode</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.RefreshMode
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getRefreshMode()
	 * @generated
	 */
	int REFRESH_MODE = 28;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.FacetKind <em>Facet Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.FacetKind
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getFacetKind()
	 * @generated
	 */
	int FACET_KIND = 29;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.NumericKind <em>Numeric Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.NumericKind
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getNumericKind()
	 * @generated
	 */
	int NUMERIC_KIND = 30;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.RangeKind <em>Range Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.RangeKind
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getRangeKind()
	 * @generated
	 */
	int RANGE_KIND = 31;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.RankFunction <em>Rank Function</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.RankFunction
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getRankFunction()
	 * @generated
	 */
	int RANK_FUNCTION = 32;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.OclContextKind <em>Ocl Context Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.OclContextKind
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getOclContextKind()
	 * @generated
	 */
	int OCL_CONTEXT_KIND = 33;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.VectorSimilarity <em>Vector Similarity</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.VectorSimilarity
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getVectorSimilarity()
	 * @generated
	 */
	int VECTOR_SIMILARITY = 34;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.SuggesterKind <em>Suggester Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.SuggesterKind
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getSuggesterKind()
	 * @generated
	 */
	int SUGGESTER_KIND = 35;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.search.esearch.AnalyzerKind <em>Analyzer Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.search.esearch.AnalyzerKind
	 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getAnalyzerKind()
	 * @generated
	 */
	int ANALYZER_KIND = 36;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.SearchMappingRegistry <em>Search Mapping Registry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Search Mapping Registry</em>'.
	 * @see org.eclipse.fennec.search.esearch.SearchMappingRegistry
	 * @generated
	 */
	EClass getSearchMappingRegistry();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.search.esearch.SearchMappingRegistry#getUnits <em>Units</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Units</em>'.
	 * @see org.eclipse.fennec.search.esearch.SearchMappingRegistry#getUnits()
	 * @see #getSearchMappingRegistry()
	 * @generated
	 */
	EReference getSearchMappingRegistry_Units();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.search.esearch.SearchMappingRegistry#getAnalyzers <em>Analyzers</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Analyzers</em>'.
	 * @see org.eclipse.fennec.search.esearch.SearchMappingRegistry#getAnalyzers()
	 * @see #getSearchMappingRegistry()
	 * @generated
	 */
	EReference getSearchMappingRegistry_Analyzers();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping <em>Index Unit Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Index Unit Mapping</em>'.
	 * @see org.eclipse.fennec.search.esearch.IndexUnitMapping
	 * @generated
	 */
	EClass getIndexUnitMapping();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getEPackage <em>EPackage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>EPackage</em>'.
	 * @see org.eclipse.fennec.search.esearch.IndexUnitMapping#getEPackage()
	 * @see #getIndexUnitMapping()
	 * @generated
	 */
	EReference getIndexUnitMapping_EPackage();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.search.esearch.IndexUnitMapping#getName()
	 * @see #getIndexUnitMapping()
	 * @generated
	 */
	EAttribute getIndexUnitMapping_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getTypeField <em>Type Field</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type Field</em>'.
	 * @see org.eclipse.fennec.search.esearch.IndexUnitMapping#getTypeField()
	 * @see #getIndexUnitMapping()
	 * @generated
	 */
	EAttribute getIndexUnitMapping_TypeField();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#isAutoMap <em>Auto Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Auto Map</em>'.
	 * @see org.eclipse.fennec.search.esearch.IndexUnitMapping#isAutoMap()
	 * @see #getIndexUnitMapping()
	 * @generated
	 */
	EAttribute getIndexUnitMapping_AutoMap();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getDefaultAnalyzer <em>Default Analyzer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Default Analyzer</em>'.
	 * @see org.eclipse.fennec.search.esearch.IndexUnitMapping#getDefaultAnalyzer()
	 * @see #getIndexUnitMapping()
	 * @generated
	 */
	EReference getIndexUnitMapping_DefaultAnalyzer();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getRefresh <em>Refresh</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Refresh</em>'.
	 * @see org.eclipse.fennec.search.esearch.IndexUnitMapping#getRefresh()
	 * @see #getIndexUnitMapping()
	 * @generated
	 */
	EReference getIndexUnitMapping_Refresh();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getCommit <em>Commit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Commit</em>'.
	 * @see org.eclipse.fennec.search.esearch.IndexUnitMapping#getCommit()
	 * @see #getIndexUnitMapping()
	 * @generated
	 */
	EReference getIndexUnitMapping_Commit();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getSort <em>Sort</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Sort</em>'.
	 * @see org.eclipse.fennec.search.esearch.IndexUnitMapping#getSort()
	 * @see #getIndexUnitMapping()
	 * @generated
	 */
	EReference getIndexUnitMapping_Sort();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getDocuments <em>Documents</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Documents</em>'.
	 * @see org.eclipse.fennec.search.esearch.IndexUnitMapping#getDocuments()
	 * @see #getIndexUnitMapping()
	 * @generated
	 */
	EReference getIndexUnitMapping_Documents();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.DocumentMapping <em>Document Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Document Mapping</em>'.
	 * @see org.eclipse.fennec.search.esearch.DocumentMapping
	 * @generated
	 */
	EClass getDocumentMapping();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.DocumentMapping#getEClass <em>EClass</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>EClass</em>'.
	 * @see org.eclipse.fennec.search.esearch.DocumentMapping#getEClass()
	 * @see #getDocumentMapping()
	 * @generated
	 */
	EReference getDocumentMapping_EClass();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.DocumentMapping#getTypeName <em>Type Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type Name</em>'.
	 * @see org.eclipse.fennec.search.esearch.DocumentMapping#getTypeName()
	 * @see #getDocumentMapping()
	 * @generated
	 */
	EAttribute getDocumentMapping_TypeName();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.DocumentMapping#getIdFeature <em>Id Feature</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Id Feature</em>'.
	 * @see org.eclipse.fennec.search.esearch.DocumentMapping#getIdFeature()
	 * @see #getDocumentMapping()
	 * @generated
	 */
	EReference getDocumentMapping_IdFeature();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.DocumentMapping#isAutoMap <em>Auto Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Auto Map</em>'.
	 * @see org.eclipse.fennec.search.esearch.DocumentMapping#isAutoMap()
	 * @see #getDocumentMapping()
	 * @generated
	 */
	EAttribute getDocumentMapping_AutoMap();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.DocumentMapping#getAnalyzer <em>Analyzer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Analyzer</em>'.
	 * @see org.eclipse.fennec.search.esearch.DocumentMapping#getAnalyzer()
	 * @see #getDocumentMapping()
	 * @generated
	 */
	EReference getDocumentMapping_Analyzer();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.search.esearch.DocumentMapping#getMaterialization <em>Materialization</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Materialization</em>'.
	 * @see org.eclipse.fennec.search.esearch.DocumentMapping#getMaterialization()
	 * @see #getDocumentMapping()
	 * @generated
	 */
	EReference getDocumentMapping_Materialization();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.search.esearch.DocumentMapping#getFields <em>Fields</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Fields</em>'.
	 * @see org.eclipse.fennec.search.esearch.DocumentMapping#getFields()
	 * @see #getDocumentMapping()
	 * @generated
	 */
	EReference getDocumentMapping_Fields();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.search.esearch.DocumentMapping#getReferences <em>References</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>References</em>'.
	 * @see org.eclipse.fennec.search.esearch.DocumentMapping#getReferences()
	 * @see #getDocumentMapping()
	 * @generated
	 */
	EReference getDocumentMapping_References();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.search.esearch.DocumentMapping#getSuggestions <em>Suggestions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Suggestions</em>'.
	 * @see org.eclipse.fennec.search.esearch.DocumentMapping#getSuggestions()
	 * @see #getDocumentMapping()
	 * @generated
	 */
	EReference getDocumentMapping_Suggestions();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.FieldMapping <em>Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Field Mapping</em>'.
	 * @see org.eclipse.fennec.search.esearch.FieldMapping
	 * @generated
	 */
	EClass getFieldMapping();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.FieldMapping#getFeature <em>Feature</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Feature</em>'.
	 * @see org.eclipse.fennec.search.esearch.FieldMapping#getFeature()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EReference getFieldMapping_Feature();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.FieldMapping#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.search.esearch.FieldMapping#getName()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EAttribute getFieldMapping_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.FieldMapping#isIndexed <em>Indexed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Indexed</em>'.
	 * @see org.eclipse.fennec.search.esearch.FieldMapping#isIndexed()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EAttribute getFieldMapping_Indexed();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.FieldMapping#isStored <em>Stored</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Stored</em>'.
	 * @see org.eclipse.fennec.search.esearch.FieldMapping#isStored()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EAttribute getFieldMapping_Stored();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.FieldMapping#isDocValues <em>Doc Values</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Doc Values</em>'.
	 * @see org.eclipse.fennec.search.esearch.FieldMapping#isDocValues()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EAttribute getFieldMapping_DocValues();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.FieldMapping#getBoost <em>Boost</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Boost</em>'.
	 * @see org.eclipse.fennec.search.esearch.FieldMapping#getBoost()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EAttribute getFieldMapping_Boost();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.search.esearch.FieldMapping#getFacet <em>Facet</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Facet</em>'.
	 * @see org.eclipse.fennec.search.esearch.FieldMapping#getFacet()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EReference getFieldMapping_Facet();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.search.esearch.FieldMapping#getUse <em>Use</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Use</em>'.
	 * @see org.eclipse.fennec.search.esearch.FieldMapping#getUse()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EAttribute getFieldMapping_Use();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.search.esearch.FieldMapping#getSources <em>Sources</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Sources</em>'.
	 * @see org.eclipse.fennec.search.esearch.FieldMapping#getSources()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EReference getFieldMapping_Sources();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.FieldMapping#getSeparator <em>Separator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Separator</em>'.
	 * @see org.eclipse.fennec.search.esearch.FieldMapping#getSeparator()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EAttribute getFieldMapping_Separator();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.search.esearch.FieldMapping#getSubFields <em>Sub Fields</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Sub Fields</em>'.
	 * @see org.eclipse.fennec.search.esearch.FieldMapping#getSubFields()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EReference getFieldMapping_SubFields();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.TextFieldMapping <em>Text Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Text Field Mapping</em>'.
	 * @see org.eclipse.fennec.search.esearch.TextFieldMapping
	 * @generated
	 */
	EClass getTextFieldMapping();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.TextFieldMapping#getAnalyzer <em>Analyzer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Analyzer</em>'.
	 * @see org.eclipse.fennec.search.esearch.TextFieldMapping#getAnalyzer()
	 * @see #getTextFieldMapping()
	 * @generated
	 */
	EReference getTextFieldMapping_Analyzer();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.TextFieldMapping#isTermVectors <em>Term Vectors</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Term Vectors</em>'.
	 * @see org.eclipse.fennec.search.esearch.TextFieldMapping#isTermVectors()
	 * @see #getTextFieldMapping()
	 * @generated
	 */
	EAttribute getTextFieldMapping_TermVectors();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.KeywordFieldMapping <em>Keyword Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Keyword Field Mapping</em>'.
	 * @see org.eclipse.fennec.search.esearch.KeywordFieldMapping
	 * @generated
	 */
	EClass getKeywordFieldMapping();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.KeywordFieldMapping#getNormalizer <em>Normalizer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Normalizer</em>'.
	 * @see org.eclipse.fennec.search.esearch.KeywordFieldMapping#getNormalizer()
	 * @see #getKeywordFieldMapping()
	 * @generated
	 */
	EReference getKeywordFieldMapping_Normalizer();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.NumericFieldMapping <em>Numeric Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Numeric Field Mapping</em>'.
	 * @see org.eclipse.fennec.search.esearch.NumericFieldMapping
	 * @generated
	 */
	EClass getNumericFieldMapping();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.NumericFieldMapping#getKind <em>Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kind</em>'.
	 * @see org.eclipse.fennec.search.esearch.NumericFieldMapping#getKind()
	 * @see #getNumericFieldMapping()
	 * @generated
	 */
	EAttribute getNumericFieldMapping_Kind();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping <em>Geo Point Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Geo Point Field Mapping</em>'.
	 * @see org.eclipse.fennec.search.esearch.GeoPointFieldMapping
	 * @generated
	 */
	EClass getGeoPointFieldMapping();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getLatitude <em>Latitude</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Latitude</em>'.
	 * @see org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getLatitude()
	 * @see #getGeoPointFieldMapping()
	 * @generated
	 */
	EReference getGeoPointFieldMapping_Latitude();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getLongitude <em>Longitude</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Longitude</em>'.
	 * @see org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getLongitude()
	 * @see #getGeoPointFieldMapping()
	 * @generated
	 */
	EReference getGeoPointFieldMapping_Longitude();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getPointReference <em>Point Reference</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Point Reference</em>'.
	 * @see org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getPointReference()
	 * @see #getGeoPointFieldMapping()
	 * @generated
	 */
	EReference getGeoPointFieldMapping_PointReference();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getCoordinates <em>Coordinates</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Coordinates</em>'.
	 * @see org.eclipse.fennec.search.esearch.GeoPointFieldMapping#getCoordinates()
	 * @see #getGeoPointFieldMapping()
	 * @generated
	 */
	EReference getGeoPointFieldMapping_Coordinates();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.RangeFieldMapping <em>Range Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Range Field Mapping</em>'.
	 * @see org.eclipse.fennec.search.esearch.RangeFieldMapping
	 * @generated
	 */
	EClass getRangeFieldMapping();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.RangeFieldMapping#getLowerBound <em>Lower Bound</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Lower Bound</em>'.
	 * @see org.eclipse.fennec.search.esearch.RangeFieldMapping#getLowerBound()
	 * @see #getRangeFieldMapping()
	 * @generated
	 */
	EReference getRangeFieldMapping_LowerBound();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.RangeFieldMapping#getUpperBound <em>Upper Bound</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Upper Bound</em>'.
	 * @see org.eclipse.fennec.search.esearch.RangeFieldMapping#getUpperBound()
	 * @see #getRangeFieldMapping()
	 * @generated
	 */
	EReference getRangeFieldMapping_UpperBound();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.RangeFieldMapping#getKind <em>Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kind</em>'.
	 * @see org.eclipse.fennec.search.esearch.RangeFieldMapping#getKind()
	 * @see #getRangeFieldMapping()
	 * @generated
	 */
	EAttribute getRangeFieldMapping_Kind();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.RankSignalFieldMapping <em>Rank Signal Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Rank Signal Field Mapping</em>'.
	 * @see org.eclipse.fennec.search.esearch.RankSignalFieldMapping
	 * @generated
	 */
	EClass getRankSignalFieldMapping();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.RankSignalFieldMapping#getFunction <em>Function</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Function</em>'.
	 * @see org.eclipse.fennec.search.esearch.RankSignalFieldMapping#getFunction()
	 * @see #getRankSignalFieldMapping()
	 * @generated
	 */
	EAttribute getRankSignalFieldMapping_Function();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.RankSignalFieldMapping#getPivot <em>Pivot</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pivot</em>'.
	 * @see org.eclipse.fennec.search.esearch.RankSignalFieldMapping#getPivot()
	 * @see #getRankSignalFieldMapping()
	 * @generated
	 */
	EAttribute getRankSignalFieldMapping_Pivot();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.RankSignalFieldMapping#getExponent <em>Exponent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Exponent</em>'.
	 * @see org.eclipse.fennec.search.esearch.RankSignalFieldMapping#getExponent()
	 * @see #getRankSignalFieldMapping()
	 * @generated
	 */
	EAttribute getRankSignalFieldMapping_Exponent();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.VectorFieldMapping <em>Vector Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Vector Field Mapping</em>'.
	 * @see org.eclipse.fennec.search.esearch.VectorFieldMapping
	 * @generated
	 */
	EClass getVectorFieldMapping();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.fennec.search.esearch.VectorFieldMapping#getEmbeddingSources <em>Embedding Sources</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Embedding Sources</em>'.
	 * @see org.eclipse.fennec.search.esearch.VectorFieldMapping#getEmbeddingSources()
	 * @see #getVectorFieldMapping()
	 * @generated
	 */
	EReference getVectorFieldMapping_EmbeddingSources();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.VectorFieldMapping#getProvider <em>Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Provider</em>'.
	 * @see org.eclipse.fennec.search.esearch.VectorFieldMapping#getProvider()
	 * @see #getVectorFieldMapping()
	 * @generated
	 */
	EAttribute getVectorFieldMapping_Provider();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.VectorFieldMapping#getDimensions <em>Dimensions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dimensions</em>'.
	 * @see org.eclipse.fennec.search.esearch.VectorFieldMapping#getDimensions()
	 * @see #getVectorFieldMapping()
	 * @generated
	 */
	EAttribute getVectorFieldMapping_Dimensions();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.VectorFieldMapping#getSimilarity <em>Similarity</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Similarity</em>'.
	 * @see org.eclipse.fennec.search.esearch.VectorFieldMapping#getSimilarity()
	 * @see #getVectorFieldMapping()
	 * @generated
	 */
	EAttribute getVectorFieldMapping_Similarity();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.VectorFieldMapping#getModelVersion <em>Model Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Model Version</em>'.
	 * @see org.eclipse.fennec.search.esearch.VectorFieldMapping#getModelVersion()
	 * @see #getVectorFieldMapping()
	 * @generated
	 */
	EAttribute getVectorFieldMapping_ModelVersion();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.ValueSource <em>Value Source</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Value Source</em>'.
	 * @see org.eclipse.fennec.search.esearch.ValueSource
	 * @generated
	 */
	EClass getValueSource();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.FeatureSource <em>Feature Source</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Feature Source</em>'.
	 * @see org.eclipse.fennec.search.esearch.FeatureSource
	 * @generated
	 */
	EClass getFeatureSource();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.FeatureSource#getFeature <em>Feature</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Feature</em>'.
	 * @see org.eclipse.fennec.search.esearch.FeatureSource#getFeature()
	 * @see #getFeatureSource()
	 * @generated
	 */
	EReference getFeatureSource_Feature();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.PathSource <em>Path Source</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Path Source</em>'.
	 * @see org.eclipse.fennec.search.esearch.PathSource
	 * @generated
	 */
	EClass getPathSource();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.fennec.search.esearch.PathSource#getSegments <em>Segments</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Segments</em>'.
	 * @see org.eclipse.fennec.search.esearch.PathSource#getSegments()
	 * @see #getPathSource()
	 * @generated
	 */
	EReference getPathSource_Segments();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.OclSource <em>Ocl Source</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Ocl Source</em>'.
	 * @see org.eclipse.fennec.search.esearch.OclSource
	 * @generated
	 */
	EClass getOclSource();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.OclSource#getExpression <em>Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Expression</em>'.
	 * @see org.eclipse.fennec.search.esearch.OclSource#getExpression()
	 * @see #getOclSource()
	 * @generated
	 */
	EAttribute getOclSource_Expression();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.OclSource#getContext <em>Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Context</em>'.
	 * @see org.eclipse.fennec.search.esearch.OclSource#getContext()
	 * @see #getOclSource()
	 * @generated
	 */
	EAttribute getOclSource_Context();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.ReferenceMapping <em>Reference Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Reference Mapping</em>'.
	 * @see org.eclipse.fennec.search.esearch.ReferenceMapping
	 * @generated
	 */
	EClass getReferenceMapping();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getEReference <em>EReference</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>EReference</em>'.
	 * @see org.eclipse.fennec.search.esearch.ReferenceMapping#getEReference()
	 * @see #getReferenceMapping()
	 * @generated
	 */
	EReference getReferenceMapping_EReference();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getStrategy <em>Strategy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Strategy</em>'.
	 * @see org.eclipse.fennec.search.esearch.ReferenceMapping#getStrategy()
	 * @see #getReferenceMapping()
	 * @generated
	 */
	EAttribute getReferenceMapping_Strategy();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getPrefix <em>Prefix</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Prefix</em>'.
	 * @see org.eclipse.fennec.search.esearch.ReferenceMapping#getPrefix()
	 * @see #getReferenceMapping()
	 * @generated
	 */
	EAttribute getReferenceMapping_Prefix();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getDepth <em>Depth</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Depth</em>'.
	 * @see org.eclipse.fennec.search.esearch.ReferenceMapping#getDepth()
	 * @see #getReferenceMapping()
	 * @generated
	 */
	EAttribute getReferenceMapping_Depth();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getTarget <em>Target</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Target</em>'.
	 * @see org.eclipse.fennec.search.esearch.ReferenceMapping#getTarget()
	 * @see #getReferenceMapping()
	 * @generated
	 */
	EReference getReferenceMapping_Target();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getIncludes <em>Includes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Includes</em>'.
	 * @see org.eclipse.fennec.search.esearch.ReferenceMapping#getIncludes()
	 * @see #getReferenceMapping()
	 * @generated
	 */
	EReference getReferenceMapping_Includes();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.Materialization <em>Materialization</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Materialization</em>'.
	 * @see org.eclipse.fennec.search.esearch.Materialization
	 * @generated
	 */
	EClass getMaterialization();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.Materialization#getKind <em>Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kind</em>'.
	 * @see org.eclipse.fennec.search.esearch.Materialization#getKind()
	 * @see #getMaterialization()
	 * @generated
	 */
	EAttribute getMaterialization_Kind();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.Materialization#getFieldName <em>Field Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Field Name</em>'.
	 * @see org.eclipse.fennec.search.esearch.Materialization#getFieldName()
	 * @see #getMaterialization()
	 * @generated
	 */
	EAttribute getMaterialization_FieldName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.Materialization#getFormat <em>Format</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Format</em>'.
	 * @see org.eclipse.fennec.search.esearch.Materialization#getFormat()
	 * @see #getMaterialization()
	 * @generated
	 */
	EAttribute getMaterialization_Format();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.FacetMapping <em>Facet Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Facet Mapping</em>'.
	 * @see org.eclipse.fennec.search.esearch.FacetMapping
	 * @generated
	 */
	EClass getFacetMapping();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.FacetMapping#getDimension <em>Dimension</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dimension</em>'.
	 * @see org.eclipse.fennec.search.esearch.FacetMapping#getDimension()
	 * @see #getFacetMapping()
	 * @generated
	 */
	EAttribute getFacetMapping_Dimension();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.FacetMapping#getKind <em>Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kind</em>'.
	 * @see org.eclipse.fennec.search.esearch.FacetMapping#getKind()
	 * @see #getFacetMapping()
	 * @generated
	 */
	EAttribute getFacetMapping_Kind();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.FacetMapping#isHierarchical <em>Hierarchical</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Hierarchical</em>'.
	 * @see org.eclipse.fennec.search.esearch.FacetMapping#isHierarchical()
	 * @see #getFacetMapping()
	 * @generated
	 */
	EAttribute getFacetMapping_Hierarchical();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.FacetMapping#isMultiValued <em>Multi Valued</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Multi Valued</em>'.
	 * @see org.eclipse.fennec.search.esearch.FacetMapping#isMultiValued()
	 * @see #getFacetMapping()
	 * @generated
	 */
	EAttribute getFacetMapping_MultiValued();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.SuggestSource <em>Suggest Source</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Suggest Source</em>'.
	 * @see org.eclipse.fennec.search.esearch.SuggestSource
	 * @generated
	 */
	EClass getSuggestSource();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.SuggestSource#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.search.esearch.SuggestSource#getName()
	 * @see #getSuggestSource()
	 * @generated
	 */
	EAttribute getSuggestSource_Name();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.SuggestSource#getFeature <em>Feature</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Feature</em>'.
	 * @see org.eclipse.fennec.search.esearch.SuggestSource#getFeature()
	 * @see #getSuggestSource()
	 * @generated
	 */
	EReference getSuggestSource_Feature();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.SuggestSource#getWeight <em>Weight</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Weight</em>'.
	 * @see org.eclipse.fennec.search.esearch.SuggestSource#getWeight()
	 * @see #getSuggestSource()
	 * @generated
	 */
	EReference getSuggestSource_Weight();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.fennec.search.esearch.SuggestSource#getContexts <em>Contexts</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Contexts</em>'.
	 * @see org.eclipse.fennec.search.esearch.SuggestSource#getContexts()
	 * @see #getSuggestSource()
	 * @generated
	 */
	EReference getSuggestSource_Contexts();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.SuggestSource#getKind <em>Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kind</em>'.
	 * @see org.eclipse.fennec.search.esearch.SuggestSource#getKind()
	 * @see #getSuggestSource()
	 * @generated
	 */
	EAttribute getSuggestSource_Kind();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.SuggestSource#getAnalyzer <em>Analyzer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Analyzer</em>'.
	 * @see org.eclipse.fennec.search.esearch.SuggestSource#getAnalyzer()
	 * @see #getSuggestSource()
	 * @generated
	 */
	EReference getSuggestSource_Analyzer();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.RefreshPolicy <em>Refresh Policy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Refresh Policy</em>'.
	 * @see org.eclipse.fennec.search.esearch.RefreshPolicy
	 * @generated
	 */
	EClass getRefreshPolicy();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.RefreshPolicy#getMode <em>Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Mode</em>'.
	 * @see org.eclipse.fennec.search.esearch.RefreshPolicy#getMode()
	 * @see #getRefreshPolicy()
	 * @generated
	 */
	EAttribute getRefreshPolicy_Mode();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.RefreshPolicy#getIntervalMillis <em>Interval Millis</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Interval Millis</em>'.
	 * @see org.eclipse.fennec.search.esearch.RefreshPolicy#getIntervalMillis()
	 * @see #getRefreshPolicy()
	 * @generated
	 */
	EAttribute getRefreshPolicy_IntervalMillis();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.CommitPolicy <em>Commit Policy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Commit Policy</em>'.
	 * @see org.eclipse.fennec.search.esearch.CommitPolicy
	 * @generated
	 */
	EClass getCommitPolicy();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.CommitPolicy#getMaxUncommittedDocs <em>Max Uncommitted Docs</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max Uncommitted Docs</em>'.
	 * @see org.eclipse.fennec.search.esearch.CommitPolicy#getMaxUncommittedDocs()
	 * @see #getCommitPolicy()
	 * @generated
	 */
	EAttribute getCommitPolicy_MaxUncommittedDocs();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.CommitPolicy#getMaxIntervalMillis <em>Max Interval Millis</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max Interval Millis</em>'.
	 * @see org.eclipse.fennec.search.esearch.CommitPolicy#getMaxIntervalMillis()
	 * @see #getCommitPolicy()
	 * @generated
	 */
	EAttribute getCommitPolicy_MaxIntervalMillis();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.CommitPolicy#isCommitOnClose <em>Commit On Close</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Commit On Close</em>'.
	 * @see org.eclipse.fennec.search.esearch.CommitPolicy#isCommitOnClose()
	 * @see #getCommitPolicy()
	 * @generated
	 */
	EAttribute getCommitPolicy_CommitOnClose();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.IndexSort <em>Index Sort</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Index Sort</em>'.
	 * @see org.eclipse.fennec.search.esearch.IndexSort
	 * @generated
	 */
	EClass getIndexSort();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.search.esearch.IndexSort#getEntries <em>Entries</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Entries</em>'.
	 * @see org.eclipse.fennec.search.esearch.IndexSort#getEntries()
	 * @see #getIndexSort()
	 * @generated
	 */
	EReference getIndexSort_Entries();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.SortEntry <em>Sort Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sort Entry</em>'.
	 * @see org.eclipse.fennec.search.esearch.SortEntry
	 * @generated
	 */
	EClass getSortEntry();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.search.esearch.SortEntry#getFeature <em>Feature</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Feature</em>'.
	 * @see org.eclipse.fennec.search.esearch.SortEntry#getFeature()
	 * @see #getSortEntry()
	 * @generated
	 */
	EReference getSortEntry_Feature();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.SortEntry#isDescending <em>Descending</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Descending</em>'.
	 * @see org.eclipse.fennec.search.esearch.SortEntry#isDescending()
	 * @see #getSortEntry()
	 * @generated
	 */
	EAttribute getSortEntry_Descending();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.SortEntry#isMissingLast <em>Missing Last</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Missing Last</em>'.
	 * @see org.eclipse.fennec.search.esearch.SortEntry#isMissingLast()
	 * @see #getSortEntry()
	 * @generated
	 */
	EAttribute getSortEntry_MissingLast();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition <em>Analyzer Definition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Analyzer Definition</em>'.
	 * @see org.eclipse.fennec.search.esearch.AnalyzerDefinition
	 * @generated
	 */
	EClass getAnalyzerDefinition();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.search.esearch.AnalyzerDefinition#getName()
	 * @see #getAnalyzerDefinition()
	 * @generated
	 */
	EAttribute getAnalyzerDefinition_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition#getKind <em>Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kind</em>'.
	 * @see org.eclipse.fennec.search.esearch.AnalyzerDefinition#getKind()
	 * @see #getAnalyzerDefinition()
	 * @generated
	 */
	EAttribute getAnalyzerDefinition_Kind();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition#getLanguage <em>Language</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Language</em>'.
	 * @see org.eclipse.fennec.search.esearch.AnalyzerDefinition#getLanguage()
	 * @see #getAnalyzerDefinition()
	 * @generated
	 */
	EAttribute getAnalyzerDefinition_Language();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition#getServiceFilter <em>Service Filter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Service Filter</em>'.
	 * @see org.eclipse.fennec.search.esearch.AnalyzerDefinition#getServiceFilter()
	 * @see #getAnalyzerDefinition()
	 * @generated
	 */
	EAttribute getAnalyzerDefinition_ServiceFilter();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition#getParameters <em>Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Parameters</em>'.
	 * @see org.eclipse.fennec.search.esearch.AnalyzerDefinition#getParameters()
	 * @see #getAnalyzerDefinition()
	 * @generated
	 */
	EReference getAnalyzerDefinition_Parameters();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.search.esearch.AnalyzerParameter <em>Analyzer Parameter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Analyzer Parameter</em>'.
	 * @see org.eclipse.fennec.search.esearch.AnalyzerParameter
	 * @generated
	 */
	EClass getAnalyzerParameter();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.AnalyzerParameter#getKey <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see org.eclipse.fennec.search.esearch.AnalyzerParameter#getKey()
	 * @see #getAnalyzerParameter()
	 * @generated
	 */
	EAttribute getAnalyzerParameter_Key();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.search.esearch.AnalyzerParameter#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see org.eclipse.fennec.search.esearch.AnalyzerParameter#getValue()
	 * @see #getAnalyzerParameter()
	 * @generated
	 */
	EAttribute getAnalyzerParameter_Value();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.search.esearch.FieldUse <em>Field Use</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Field Use</em>'.
	 * @see org.eclipse.fennec.search.esearch.FieldUse
	 * @generated
	 */
	EEnum getFieldUse();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.search.esearch.ReferenceStrategy <em>Reference Strategy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Reference Strategy</em>'.
	 * @see org.eclipse.fennec.search.esearch.ReferenceStrategy
	 * @generated
	 */
	EEnum getReferenceStrategy();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.search.esearch.MaterializationKind <em>Materialization Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Materialization Kind</em>'.
	 * @see org.eclipse.fennec.search.esearch.MaterializationKind
	 * @generated
	 */
	EEnum getMaterializationKind();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.search.esearch.RefreshMode <em>Refresh Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Refresh Mode</em>'.
	 * @see org.eclipse.fennec.search.esearch.RefreshMode
	 * @generated
	 */
	EEnum getRefreshMode();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.search.esearch.FacetKind <em>Facet Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Facet Kind</em>'.
	 * @see org.eclipse.fennec.search.esearch.FacetKind
	 * @generated
	 */
	EEnum getFacetKind();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.search.esearch.NumericKind <em>Numeric Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Numeric Kind</em>'.
	 * @see org.eclipse.fennec.search.esearch.NumericKind
	 * @generated
	 */
	EEnum getNumericKind();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.search.esearch.RangeKind <em>Range Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Range Kind</em>'.
	 * @see org.eclipse.fennec.search.esearch.RangeKind
	 * @generated
	 */
	EEnum getRangeKind();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.search.esearch.RankFunction <em>Rank Function</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Rank Function</em>'.
	 * @see org.eclipse.fennec.search.esearch.RankFunction
	 * @generated
	 */
	EEnum getRankFunction();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.search.esearch.OclContextKind <em>Ocl Context Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Ocl Context Kind</em>'.
	 * @see org.eclipse.fennec.search.esearch.OclContextKind
	 * @generated
	 */
	EEnum getOclContextKind();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.search.esearch.VectorSimilarity <em>Vector Similarity</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Vector Similarity</em>'.
	 * @see org.eclipse.fennec.search.esearch.VectorSimilarity
	 * @generated
	 */
	EEnum getVectorSimilarity();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.search.esearch.SuggesterKind <em>Suggester Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Suggester Kind</em>'.
	 * @see org.eclipse.fennec.search.esearch.SuggesterKind
	 * @generated
	 */
	EEnum getSuggesterKind();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.search.esearch.AnalyzerKind <em>Analyzer Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Analyzer Kind</em>'.
	 * @see org.eclipse.fennec.search.esearch.AnalyzerKind
	 * @generated
	 */
	EEnum getAnalyzerKind();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ESearchFactory getESearchFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.SearchMappingRegistryImpl <em>Search Mapping Registry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.SearchMappingRegistryImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getSearchMappingRegistry()
		 * @generated
		 */
		EClass SEARCH_MAPPING_REGISTRY = eINSTANCE.getSearchMappingRegistry();

		/**
		 * The meta object literal for the '<em><b>Units</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SEARCH_MAPPING_REGISTRY__UNITS = eINSTANCE.getSearchMappingRegistry_Units();

		/**
		 * The meta object literal for the '<em><b>Analyzers</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SEARCH_MAPPING_REGISTRY__ANALYZERS = eINSTANCE.getSearchMappingRegistry_Analyzers();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.IndexUnitMappingImpl <em>Index Unit Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.IndexUnitMappingImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getIndexUnitMapping()
		 * @generated
		 */
		EClass INDEX_UNIT_MAPPING = eINSTANCE.getIndexUnitMapping();

		/**
		 * The meta object literal for the '<em><b>EPackage</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INDEX_UNIT_MAPPING__EPACKAGE = eINSTANCE.getIndexUnitMapping_EPackage();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INDEX_UNIT_MAPPING__NAME = eINSTANCE.getIndexUnitMapping_Name();

		/**
		 * The meta object literal for the '<em><b>Type Field</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INDEX_UNIT_MAPPING__TYPE_FIELD = eINSTANCE.getIndexUnitMapping_TypeField();

		/**
		 * The meta object literal for the '<em><b>Auto Map</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INDEX_UNIT_MAPPING__AUTO_MAP = eINSTANCE.getIndexUnitMapping_AutoMap();

		/**
		 * The meta object literal for the '<em><b>Default Analyzer</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INDEX_UNIT_MAPPING__DEFAULT_ANALYZER = eINSTANCE.getIndexUnitMapping_DefaultAnalyzer();

		/**
		 * The meta object literal for the '<em><b>Refresh</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INDEX_UNIT_MAPPING__REFRESH = eINSTANCE.getIndexUnitMapping_Refresh();

		/**
		 * The meta object literal for the '<em><b>Commit</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INDEX_UNIT_MAPPING__COMMIT = eINSTANCE.getIndexUnitMapping_Commit();

		/**
		 * The meta object literal for the '<em><b>Sort</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INDEX_UNIT_MAPPING__SORT = eINSTANCE.getIndexUnitMapping_Sort();

		/**
		 * The meta object literal for the '<em><b>Documents</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INDEX_UNIT_MAPPING__DOCUMENTS = eINSTANCE.getIndexUnitMapping_Documents();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.DocumentMappingImpl <em>Document Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.DocumentMappingImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getDocumentMapping()
		 * @generated
		 */
		EClass DOCUMENT_MAPPING = eINSTANCE.getDocumentMapping();

		/**
		 * The meta object literal for the '<em><b>EClass</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_MAPPING__ECLASS = eINSTANCE.getDocumentMapping_EClass();

		/**
		 * The meta object literal for the '<em><b>Type Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DOCUMENT_MAPPING__TYPE_NAME = eINSTANCE.getDocumentMapping_TypeName();

		/**
		 * The meta object literal for the '<em><b>Id Feature</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_MAPPING__ID_FEATURE = eINSTANCE.getDocumentMapping_IdFeature();

		/**
		 * The meta object literal for the '<em><b>Auto Map</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DOCUMENT_MAPPING__AUTO_MAP = eINSTANCE.getDocumentMapping_AutoMap();

		/**
		 * The meta object literal for the '<em><b>Analyzer</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_MAPPING__ANALYZER = eINSTANCE.getDocumentMapping_Analyzer();

		/**
		 * The meta object literal for the '<em><b>Materialization</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_MAPPING__MATERIALIZATION = eINSTANCE.getDocumentMapping_Materialization();

		/**
		 * The meta object literal for the '<em><b>Fields</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_MAPPING__FIELDS = eINSTANCE.getDocumentMapping_Fields();

		/**
		 * The meta object literal for the '<em><b>References</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_MAPPING__REFERENCES = eINSTANCE.getDocumentMapping_References();

		/**
		 * The meta object literal for the '<em><b>Suggestions</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT_MAPPING__SUGGESTIONS = eINSTANCE.getDocumentMapping_Suggestions();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.FieldMappingImpl <em>Field Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.FieldMappingImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getFieldMapping()
		 * @generated
		 */
		EClass FIELD_MAPPING = eINSTANCE.getFieldMapping();

		/**
		 * The meta object literal for the '<em><b>Feature</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FIELD_MAPPING__FEATURE = eINSTANCE.getFieldMapping_Feature();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIELD_MAPPING__NAME = eINSTANCE.getFieldMapping_Name();

		/**
		 * The meta object literal for the '<em><b>Indexed</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIELD_MAPPING__INDEXED = eINSTANCE.getFieldMapping_Indexed();

		/**
		 * The meta object literal for the '<em><b>Stored</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIELD_MAPPING__STORED = eINSTANCE.getFieldMapping_Stored();

		/**
		 * The meta object literal for the '<em><b>Doc Values</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIELD_MAPPING__DOC_VALUES = eINSTANCE.getFieldMapping_DocValues();

		/**
		 * The meta object literal for the '<em><b>Boost</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIELD_MAPPING__BOOST = eINSTANCE.getFieldMapping_Boost();

		/**
		 * The meta object literal for the '<em><b>Facet</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FIELD_MAPPING__FACET = eINSTANCE.getFieldMapping_Facet();

		/**
		 * The meta object literal for the '<em><b>Use</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIELD_MAPPING__USE = eINSTANCE.getFieldMapping_Use();

		/**
		 * The meta object literal for the '<em><b>Sources</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FIELD_MAPPING__SOURCES = eINSTANCE.getFieldMapping_Sources();

		/**
		 * The meta object literal for the '<em><b>Separator</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIELD_MAPPING__SEPARATOR = eINSTANCE.getFieldMapping_Separator();

		/**
		 * The meta object literal for the '<em><b>Sub Fields</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FIELD_MAPPING__SUB_FIELDS = eINSTANCE.getFieldMapping_SubFields();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.TextFieldMappingImpl <em>Text Field Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.TextFieldMappingImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getTextFieldMapping()
		 * @generated
		 */
		EClass TEXT_FIELD_MAPPING = eINSTANCE.getTextFieldMapping();

		/**
		 * The meta object literal for the '<em><b>Analyzer</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TEXT_FIELD_MAPPING__ANALYZER = eINSTANCE.getTextFieldMapping_Analyzer();

		/**
		 * The meta object literal for the '<em><b>Term Vectors</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TEXT_FIELD_MAPPING__TERM_VECTORS = eINSTANCE.getTextFieldMapping_TermVectors();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.KeywordFieldMappingImpl <em>Keyword Field Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.KeywordFieldMappingImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getKeywordFieldMapping()
		 * @generated
		 */
		EClass KEYWORD_FIELD_MAPPING = eINSTANCE.getKeywordFieldMapping();

		/**
		 * The meta object literal for the '<em><b>Normalizer</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference KEYWORD_FIELD_MAPPING__NORMALIZER = eINSTANCE.getKeywordFieldMapping_Normalizer();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.NumericFieldMappingImpl <em>Numeric Field Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.NumericFieldMappingImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getNumericFieldMapping()
		 * @generated
		 */
		EClass NUMERIC_FIELD_MAPPING = eINSTANCE.getNumericFieldMapping();

		/**
		 * The meta object literal for the '<em><b>Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NUMERIC_FIELD_MAPPING__KIND = eINSTANCE.getNumericFieldMapping_Kind();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.GeoPointFieldMappingImpl <em>Geo Point Field Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.GeoPointFieldMappingImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getGeoPointFieldMapping()
		 * @generated
		 */
		EClass GEO_POINT_FIELD_MAPPING = eINSTANCE.getGeoPointFieldMapping();

		/**
		 * The meta object literal for the '<em><b>Latitude</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GEO_POINT_FIELD_MAPPING__LATITUDE = eINSTANCE.getGeoPointFieldMapping_Latitude();

		/**
		 * The meta object literal for the '<em><b>Longitude</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GEO_POINT_FIELD_MAPPING__LONGITUDE = eINSTANCE.getGeoPointFieldMapping_Longitude();

		/**
		 * The meta object literal for the '<em><b>Point Reference</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GEO_POINT_FIELD_MAPPING__POINT_REFERENCE = eINSTANCE.getGeoPointFieldMapping_PointReference();

		/**
		 * The meta object literal for the '<em><b>Coordinates</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GEO_POINT_FIELD_MAPPING__COORDINATES = eINSTANCE.getGeoPointFieldMapping_Coordinates();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.RangeFieldMappingImpl <em>Range Field Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.RangeFieldMappingImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getRangeFieldMapping()
		 * @generated
		 */
		EClass RANGE_FIELD_MAPPING = eINSTANCE.getRangeFieldMapping();

		/**
		 * The meta object literal for the '<em><b>Lower Bound</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RANGE_FIELD_MAPPING__LOWER_BOUND = eINSTANCE.getRangeFieldMapping_LowerBound();

		/**
		 * The meta object literal for the '<em><b>Upper Bound</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RANGE_FIELD_MAPPING__UPPER_BOUND = eINSTANCE.getRangeFieldMapping_UpperBound();

		/**
		 * The meta object literal for the '<em><b>Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RANGE_FIELD_MAPPING__KIND = eINSTANCE.getRangeFieldMapping_Kind();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.RankSignalFieldMappingImpl <em>Rank Signal Field Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.RankSignalFieldMappingImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getRankSignalFieldMapping()
		 * @generated
		 */
		EClass RANK_SIGNAL_FIELD_MAPPING = eINSTANCE.getRankSignalFieldMapping();

		/**
		 * The meta object literal for the '<em><b>Function</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RANK_SIGNAL_FIELD_MAPPING__FUNCTION = eINSTANCE.getRankSignalFieldMapping_Function();

		/**
		 * The meta object literal for the '<em><b>Pivot</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RANK_SIGNAL_FIELD_MAPPING__PIVOT = eINSTANCE.getRankSignalFieldMapping_Pivot();

		/**
		 * The meta object literal for the '<em><b>Exponent</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RANK_SIGNAL_FIELD_MAPPING__EXPONENT = eINSTANCE.getRankSignalFieldMapping_Exponent();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.VectorFieldMappingImpl <em>Vector Field Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.VectorFieldMappingImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getVectorFieldMapping()
		 * @generated
		 */
		EClass VECTOR_FIELD_MAPPING = eINSTANCE.getVectorFieldMapping();

		/**
		 * The meta object literal for the '<em><b>Embedding Sources</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VECTOR_FIELD_MAPPING__EMBEDDING_SOURCES = eINSTANCE.getVectorFieldMapping_EmbeddingSources();

		/**
		 * The meta object literal for the '<em><b>Provider</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VECTOR_FIELD_MAPPING__PROVIDER = eINSTANCE.getVectorFieldMapping_Provider();

		/**
		 * The meta object literal for the '<em><b>Dimensions</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VECTOR_FIELD_MAPPING__DIMENSIONS = eINSTANCE.getVectorFieldMapping_Dimensions();

		/**
		 * The meta object literal for the '<em><b>Similarity</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VECTOR_FIELD_MAPPING__SIMILARITY = eINSTANCE.getVectorFieldMapping_Similarity();

		/**
		 * The meta object literal for the '<em><b>Model Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VECTOR_FIELD_MAPPING__MODEL_VERSION = eINSTANCE.getVectorFieldMapping_ModelVersion();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.ValueSourceImpl <em>Value Source</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.ValueSourceImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getValueSource()
		 * @generated
		 */
		EClass VALUE_SOURCE = eINSTANCE.getValueSource();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.FeatureSourceImpl <em>Feature Source</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.FeatureSourceImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getFeatureSource()
		 * @generated
		 */
		EClass FEATURE_SOURCE = eINSTANCE.getFeatureSource();

		/**
		 * The meta object literal for the '<em><b>Feature</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FEATURE_SOURCE__FEATURE = eINSTANCE.getFeatureSource_Feature();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.PathSourceImpl <em>Path Source</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.PathSourceImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getPathSource()
		 * @generated
		 */
		EClass PATH_SOURCE = eINSTANCE.getPathSource();

		/**
		 * The meta object literal for the '<em><b>Segments</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PATH_SOURCE__SEGMENTS = eINSTANCE.getPathSource_Segments();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.OclSourceImpl <em>Ocl Source</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.OclSourceImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getOclSource()
		 * @generated
		 */
		EClass OCL_SOURCE = eINSTANCE.getOclSource();

		/**
		 * The meta object literal for the '<em><b>Expression</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_SOURCE__EXPRESSION = eINSTANCE.getOclSource_Expression();

		/**
		 * The meta object literal for the '<em><b>Context</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_SOURCE__CONTEXT = eINSTANCE.getOclSource_Context();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.ReferenceMappingImpl <em>Reference Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.ReferenceMappingImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getReferenceMapping()
		 * @generated
		 */
		EClass REFERENCE_MAPPING = eINSTANCE.getReferenceMapping();

		/**
		 * The meta object literal for the '<em><b>EReference</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference REFERENCE_MAPPING__EREFERENCE = eINSTANCE.getReferenceMapping_EReference();

		/**
		 * The meta object literal for the '<em><b>Strategy</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REFERENCE_MAPPING__STRATEGY = eINSTANCE.getReferenceMapping_Strategy();

		/**
		 * The meta object literal for the '<em><b>Prefix</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REFERENCE_MAPPING__PREFIX = eINSTANCE.getReferenceMapping_Prefix();

		/**
		 * The meta object literal for the '<em><b>Depth</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REFERENCE_MAPPING__DEPTH = eINSTANCE.getReferenceMapping_Depth();

		/**
		 * The meta object literal for the '<em><b>Target</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference REFERENCE_MAPPING__TARGET = eINSTANCE.getReferenceMapping_Target();

		/**
		 * The meta object literal for the '<em><b>Includes</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference REFERENCE_MAPPING__INCLUDES = eINSTANCE.getReferenceMapping_Includes();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.MaterializationImpl <em>Materialization</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.MaterializationImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getMaterialization()
		 * @generated
		 */
		EClass MATERIALIZATION = eINSTANCE.getMaterialization();

		/**
		 * The meta object literal for the '<em><b>Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MATERIALIZATION__KIND = eINSTANCE.getMaterialization_Kind();

		/**
		 * The meta object literal for the '<em><b>Field Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MATERIALIZATION__FIELD_NAME = eINSTANCE.getMaterialization_FieldName();

		/**
		 * The meta object literal for the '<em><b>Format</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MATERIALIZATION__FORMAT = eINSTANCE.getMaterialization_Format();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.FacetMappingImpl <em>Facet Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.FacetMappingImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getFacetMapping()
		 * @generated
		 */
		EClass FACET_MAPPING = eINSTANCE.getFacetMapping();

		/**
		 * The meta object literal for the '<em><b>Dimension</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FACET_MAPPING__DIMENSION = eINSTANCE.getFacetMapping_Dimension();

		/**
		 * The meta object literal for the '<em><b>Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FACET_MAPPING__KIND = eINSTANCE.getFacetMapping_Kind();

		/**
		 * The meta object literal for the '<em><b>Hierarchical</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FACET_MAPPING__HIERARCHICAL = eINSTANCE.getFacetMapping_Hierarchical();

		/**
		 * The meta object literal for the '<em><b>Multi Valued</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FACET_MAPPING__MULTI_VALUED = eINSTANCE.getFacetMapping_MultiValued();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.SuggestSourceImpl <em>Suggest Source</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.SuggestSourceImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getSuggestSource()
		 * @generated
		 */
		EClass SUGGEST_SOURCE = eINSTANCE.getSuggestSource();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SUGGEST_SOURCE__NAME = eINSTANCE.getSuggestSource_Name();

		/**
		 * The meta object literal for the '<em><b>Feature</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SUGGEST_SOURCE__FEATURE = eINSTANCE.getSuggestSource_Feature();

		/**
		 * The meta object literal for the '<em><b>Weight</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SUGGEST_SOURCE__WEIGHT = eINSTANCE.getSuggestSource_Weight();

		/**
		 * The meta object literal for the '<em><b>Contexts</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SUGGEST_SOURCE__CONTEXTS = eINSTANCE.getSuggestSource_Contexts();

		/**
		 * The meta object literal for the '<em><b>Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SUGGEST_SOURCE__KIND = eINSTANCE.getSuggestSource_Kind();

		/**
		 * The meta object literal for the '<em><b>Analyzer</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SUGGEST_SOURCE__ANALYZER = eINSTANCE.getSuggestSource_Analyzer();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.RefreshPolicyImpl <em>Refresh Policy</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.RefreshPolicyImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getRefreshPolicy()
		 * @generated
		 */
		EClass REFRESH_POLICY = eINSTANCE.getRefreshPolicy();

		/**
		 * The meta object literal for the '<em><b>Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REFRESH_POLICY__MODE = eINSTANCE.getRefreshPolicy_Mode();

		/**
		 * The meta object literal for the '<em><b>Interval Millis</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REFRESH_POLICY__INTERVAL_MILLIS = eINSTANCE.getRefreshPolicy_IntervalMillis();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.CommitPolicyImpl <em>Commit Policy</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.CommitPolicyImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getCommitPolicy()
		 * @generated
		 */
		EClass COMMIT_POLICY = eINSTANCE.getCommitPolicy();

		/**
		 * The meta object literal for the '<em><b>Max Uncommitted Docs</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMIT_POLICY__MAX_UNCOMMITTED_DOCS = eINSTANCE.getCommitPolicy_MaxUncommittedDocs();

		/**
		 * The meta object literal for the '<em><b>Max Interval Millis</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMIT_POLICY__MAX_INTERVAL_MILLIS = eINSTANCE.getCommitPolicy_MaxIntervalMillis();

		/**
		 * The meta object literal for the '<em><b>Commit On Close</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMIT_POLICY__COMMIT_ON_CLOSE = eINSTANCE.getCommitPolicy_CommitOnClose();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.IndexSortImpl <em>Index Sort</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.IndexSortImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getIndexSort()
		 * @generated
		 */
		EClass INDEX_SORT = eINSTANCE.getIndexSort();

		/**
		 * The meta object literal for the '<em><b>Entries</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INDEX_SORT__ENTRIES = eINSTANCE.getIndexSort_Entries();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.SortEntryImpl <em>Sort Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.SortEntryImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getSortEntry()
		 * @generated
		 */
		EClass SORT_ENTRY = eINSTANCE.getSortEntry();

		/**
		 * The meta object literal for the '<em><b>Feature</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SORT_ENTRY__FEATURE = eINSTANCE.getSortEntry_Feature();

		/**
		 * The meta object literal for the '<em><b>Descending</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SORT_ENTRY__DESCENDING = eINSTANCE.getSortEntry_Descending();

		/**
		 * The meta object literal for the '<em><b>Missing Last</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SORT_ENTRY__MISSING_LAST = eINSTANCE.getSortEntry_MissingLast();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.AnalyzerDefinitionImpl <em>Analyzer Definition</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.AnalyzerDefinitionImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getAnalyzerDefinition()
		 * @generated
		 */
		EClass ANALYZER_DEFINITION = eINSTANCE.getAnalyzerDefinition();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANALYZER_DEFINITION__NAME = eINSTANCE.getAnalyzerDefinition_Name();

		/**
		 * The meta object literal for the '<em><b>Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANALYZER_DEFINITION__KIND = eINSTANCE.getAnalyzerDefinition_Kind();

		/**
		 * The meta object literal for the '<em><b>Language</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANALYZER_DEFINITION__LANGUAGE = eINSTANCE.getAnalyzerDefinition_Language();

		/**
		 * The meta object literal for the '<em><b>Service Filter</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANALYZER_DEFINITION__SERVICE_FILTER = eINSTANCE.getAnalyzerDefinition_ServiceFilter();

		/**
		 * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ANALYZER_DEFINITION__PARAMETERS = eINSTANCE.getAnalyzerDefinition_Parameters();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.impl.AnalyzerParameterImpl <em>Analyzer Parameter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.impl.AnalyzerParameterImpl
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getAnalyzerParameter()
		 * @generated
		 */
		EClass ANALYZER_PARAMETER = eINSTANCE.getAnalyzerParameter();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANALYZER_PARAMETER__KEY = eINSTANCE.getAnalyzerParameter_Key();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ANALYZER_PARAMETER__VALUE = eINSTANCE.getAnalyzerParameter_Value();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.FieldUse <em>Field Use</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.FieldUse
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getFieldUse()
		 * @generated
		 */
		EEnum FIELD_USE = eINSTANCE.getFieldUse();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.ReferenceStrategy <em>Reference Strategy</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.ReferenceStrategy
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getReferenceStrategy()
		 * @generated
		 */
		EEnum REFERENCE_STRATEGY = eINSTANCE.getReferenceStrategy();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.MaterializationKind <em>Materialization Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.MaterializationKind
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getMaterializationKind()
		 * @generated
		 */
		EEnum MATERIALIZATION_KIND = eINSTANCE.getMaterializationKind();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.RefreshMode <em>Refresh Mode</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.RefreshMode
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getRefreshMode()
		 * @generated
		 */
		EEnum REFRESH_MODE = eINSTANCE.getRefreshMode();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.FacetKind <em>Facet Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.FacetKind
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getFacetKind()
		 * @generated
		 */
		EEnum FACET_KIND = eINSTANCE.getFacetKind();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.NumericKind <em>Numeric Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.NumericKind
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getNumericKind()
		 * @generated
		 */
		EEnum NUMERIC_KIND = eINSTANCE.getNumericKind();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.RangeKind <em>Range Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.RangeKind
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getRangeKind()
		 * @generated
		 */
		EEnum RANGE_KIND = eINSTANCE.getRangeKind();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.RankFunction <em>Rank Function</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.RankFunction
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getRankFunction()
		 * @generated
		 */
		EEnum RANK_FUNCTION = eINSTANCE.getRankFunction();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.OclContextKind <em>Ocl Context Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.OclContextKind
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getOclContextKind()
		 * @generated
		 */
		EEnum OCL_CONTEXT_KIND = eINSTANCE.getOclContextKind();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.VectorSimilarity <em>Vector Similarity</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.VectorSimilarity
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getVectorSimilarity()
		 * @generated
		 */
		EEnum VECTOR_SIMILARITY = eINSTANCE.getVectorSimilarity();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.SuggesterKind <em>Suggester Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.SuggesterKind
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getSuggesterKind()
		 * @generated
		 */
		EEnum SUGGESTER_KIND = eINSTANCE.getSuggesterKind();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.search.esearch.AnalyzerKind <em>Analyzer Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.search.esearch.AnalyzerKind
		 * @see org.eclipse.fennec.search.esearch.impl.ESearchPackageImpl#getAnalyzerKind()
		 * @generated
		 */
		EEnum ANALYZER_KIND = eINSTANCE.getAnalyzerKind();

	}

} //ESearchPackage
