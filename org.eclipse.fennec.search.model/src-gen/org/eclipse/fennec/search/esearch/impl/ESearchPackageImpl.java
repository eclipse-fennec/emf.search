/**
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
package org.eclipse.fennec.search.esearch.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.search.esearch.AnalyzerDefinition;
import org.eclipse.fennec.search.esearch.AnalyzerKind;
import org.eclipse.fennec.search.esearch.AnalyzerParameter;
import org.eclipse.fennec.search.esearch.CommitPolicy;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.FacetKind;
import org.eclipse.fennec.search.esearch.FacetMapping;
import org.eclipse.fennec.search.esearch.FieldMapping;
import org.eclipse.fennec.search.esearch.FieldUse;
import org.eclipse.fennec.search.esearch.GeoPointFieldMapping;
import org.eclipse.fennec.search.esearch.IndexSort;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.Materialization;
import org.eclipse.fennec.search.esearch.NumericFieldMapping;
import org.eclipse.fennec.search.esearch.NumericKind;
import org.eclipse.fennec.search.esearch.RangeFieldMapping;
import org.eclipse.fennec.search.esearch.RangeKind;
import org.eclipse.fennec.search.esearch.RankFunction;
import org.eclipse.fennec.search.esearch.RankSignalFieldMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.esearch.RefreshMode;
import org.eclipse.fennec.search.esearch.RefreshPolicy;
import org.eclipse.fennec.search.esearch.SearchMappingRegistry;
import org.eclipse.fennec.search.esearch.SortEntry;
import org.eclipse.fennec.search.esearch.SuggestSource;
import org.eclipse.fennec.search.esearch.SuggesterKind;
import org.eclipse.fennec.search.esearch.TextFieldMapping;
import org.eclipse.fennec.search.esearch.VectorFieldMapping;
import org.eclipse.fennec.search.esearch.VectorSimilarity;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ESearchPackageImpl extends EPackageImpl implements ESearchPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass searchMappingRegistryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass indexUnitMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass documentMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass fieldMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass textFieldMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass keywordFieldMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass numericFieldMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass geoPointFieldMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass rangeFieldMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass rankSignalFieldMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass vectorFieldMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass referenceMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass materializationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass facetMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass suggestSourceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass refreshPolicyEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass commitPolicyEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass indexSortEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sortEntryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass analyzerDefinitionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass analyzerParameterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum fieldUseEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum referenceStrategyEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum refreshModeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum facetKindEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum numericKindEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum rangeKindEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum rankFunctionEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum vectorSimilarityEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum suggesterKindEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum analyzerKindEEnum = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ESearchPackageImpl() {
		super(eNS_URI, ESearchFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link ESearchPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ESearchPackage init() {
		if (isInited) return (ESearchPackage)EPackage.Registry.INSTANCE.getEPackage(ESearchPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredESearchPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ESearchPackageImpl theESearchPackage = registeredESearchPackage instanceof ESearchPackageImpl ? (ESearchPackageImpl)registeredESearchPackage : new ESearchPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theESearchPackage.createPackageContents();

		// Initialize created meta-data
		theESearchPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theESearchPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ESearchPackage.eNS_URI, theESearchPackage);
		return theESearchPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSearchMappingRegistry() {
		return searchMappingRegistryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSearchMappingRegistry_Units() {
		return (EReference)searchMappingRegistryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSearchMappingRegistry_Analyzers() {
		return (EReference)searchMappingRegistryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIndexUnitMapping() {
		return indexUnitMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIndexUnitMapping_EPackage() {
		return (EReference)indexUnitMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIndexUnitMapping_Name() {
		return (EAttribute)indexUnitMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIndexUnitMapping_TypeField() {
		return (EAttribute)indexUnitMappingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIndexUnitMapping_AutoMap() {
		return (EAttribute)indexUnitMappingEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIndexUnitMapping_DefaultAnalyzer() {
		return (EReference)indexUnitMappingEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIndexUnitMapping_Refresh() {
		return (EReference)indexUnitMappingEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIndexUnitMapping_Commit() {
		return (EReference)indexUnitMappingEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIndexUnitMapping_Sort() {
		return (EReference)indexUnitMappingEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIndexUnitMapping_Documents() {
		return (EReference)indexUnitMappingEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDocumentMapping() {
		return documentMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDocumentMapping_EClass() {
		return (EReference)documentMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDocumentMapping_TypeName() {
		return (EAttribute)documentMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDocumentMapping_IdFeature() {
		return (EReference)documentMappingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDocumentMapping_AutoMap() {
		return (EAttribute)documentMappingEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDocumentMapping_Analyzer() {
		return (EReference)documentMappingEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDocumentMapping_Materialization() {
		return (EReference)documentMappingEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDocumentMapping_Fields() {
		return (EReference)documentMappingEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDocumentMapping_References() {
		return (EReference)documentMappingEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDocumentMapping_Suggestions() {
		return (EReference)documentMappingEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFieldMapping() {
		return fieldMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getFieldMapping_Feature() {
		return (EReference)fieldMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFieldMapping_Name() {
		return (EAttribute)fieldMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFieldMapping_Indexed() {
		return (EAttribute)fieldMappingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFieldMapping_Stored() {
		return (EAttribute)fieldMappingEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFieldMapping_DocValues() {
		return (EAttribute)fieldMappingEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFieldMapping_Boost() {
		return (EAttribute)fieldMappingEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getFieldMapping_Facet() {
		return (EReference)fieldMappingEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFieldMapping_Use() {
		return (EAttribute)fieldMappingEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getFieldMapping_SubFields() {
		return (EReference)fieldMappingEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTextFieldMapping() {
		return textFieldMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getTextFieldMapping_Analyzer() {
		return (EReference)textFieldMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTextFieldMapping_TermVectors() {
		return (EAttribute)textFieldMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getKeywordFieldMapping() {
		return keywordFieldMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getKeywordFieldMapping_Normalizer() {
		return (EReference)keywordFieldMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getNumericFieldMapping() {
		return numericFieldMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getNumericFieldMapping_Kind() {
		return (EAttribute)numericFieldMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGeoPointFieldMapping() {
		return geoPointFieldMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGeoPointFieldMapping_Latitude() {
		return (EReference)geoPointFieldMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGeoPointFieldMapping_Longitude() {
		return (EReference)geoPointFieldMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRangeFieldMapping() {
		return rangeFieldMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRangeFieldMapping_LowerBound() {
		return (EReference)rangeFieldMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRangeFieldMapping_UpperBound() {
		return (EReference)rangeFieldMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRangeFieldMapping_Kind() {
		return (EAttribute)rangeFieldMappingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRankSignalFieldMapping() {
		return rankSignalFieldMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRankSignalFieldMapping_Function() {
		return (EAttribute)rankSignalFieldMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRankSignalFieldMapping_Pivot() {
		return (EAttribute)rankSignalFieldMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRankSignalFieldMapping_Exponent() {
		return (EAttribute)rankSignalFieldMappingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getVectorFieldMapping() {
		return vectorFieldMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getVectorFieldMapping_Sources() {
		return (EReference)vectorFieldMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVectorFieldMapping_Provider() {
		return (EAttribute)vectorFieldMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVectorFieldMapping_Dimensions() {
		return (EAttribute)vectorFieldMappingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVectorFieldMapping_Similarity() {
		return (EAttribute)vectorFieldMappingEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVectorFieldMapping_ModelVersion() {
		return (EAttribute)vectorFieldMappingEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getReferenceMapping() {
		return referenceMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReferenceMapping_EReference() {
		return (EReference)referenceMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReferenceMapping_Strategy() {
		return (EAttribute)referenceMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReferenceMapping_Prefix() {
		return (EAttribute)referenceMappingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReferenceMapping_Depth() {
		return (EAttribute)referenceMappingEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReferenceMapping_Target() {
		return (EReference)referenceMappingEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReferenceMapping_Includes() {
		return (EReference)referenceMappingEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMaterialization() {
		return materializationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMaterialization_StoreObject() {
		return (EAttribute)materializationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMaterialization_FieldName() {
		return (EAttribute)materializationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMaterialization_Format() {
		return (EAttribute)materializationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFacetMapping() {
		return facetMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFacetMapping_Dimension() {
		return (EAttribute)facetMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFacetMapping_Kind() {
		return (EAttribute)facetMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFacetMapping_Hierarchical() {
		return (EAttribute)facetMappingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFacetMapping_MultiValued() {
		return (EAttribute)facetMappingEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSuggestSource() {
		return suggestSourceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSuggestSource_Name() {
		return (EAttribute)suggestSourceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSuggestSource_Feature() {
		return (EReference)suggestSourceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSuggestSource_Weight() {
		return (EReference)suggestSourceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSuggestSource_Contexts() {
		return (EReference)suggestSourceEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSuggestSource_Kind() {
		return (EAttribute)suggestSourceEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSuggestSource_Analyzer() {
		return (EReference)suggestSourceEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRefreshPolicy() {
		return refreshPolicyEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRefreshPolicy_Mode() {
		return (EAttribute)refreshPolicyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRefreshPolicy_IntervalMillis() {
		return (EAttribute)refreshPolicyEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCommitPolicy() {
		return commitPolicyEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCommitPolicy_MaxUncommittedDocs() {
		return (EAttribute)commitPolicyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCommitPolicy_MaxIntervalMillis() {
		return (EAttribute)commitPolicyEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCommitPolicy_CommitOnClose() {
		return (EAttribute)commitPolicyEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIndexSort() {
		return indexSortEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIndexSort_Entries() {
		return (EReference)indexSortEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSortEntry() {
		return sortEntryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSortEntry_Feature() {
		return (EReference)sortEntryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSortEntry_Descending() {
		return (EAttribute)sortEntryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSortEntry_MissingLast() {
		return (EAttribute)sortEntryEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAnalyzerDefinition() {
		return analyzerDefinitionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnalyzerDefinition_Name() {
		return (EAttribute)analyzerDefinitionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnalyzerDefinition_Kind() {
		return (EAttribute)analyzerDefinitionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnalyzerDefinition_Language() {
		return (EAttribute)analyzerDefinitionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnalyzerDefinition_ServiceFilter() {
		return (EAttribute)analyzerDefinitionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAnalyzerDefinition_Parameters() {
		return (EReference)analyzerDefinitionEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAnalyzerParameter() {
		return analyzerParameterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnalyzerParameter_Key() {
		return (EAttribute)analyzerParameterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAnalyzerParameter_Value() {
		return (EAttribute)analyzerParameterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getFieldUse() {
		return fieldUseEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getReferenceStrategy() {
		return referenceStrategyEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getRefreshMode() {
		return refreshModeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getFacetKind() {
		return facetKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getNumericKind() {
		return numericKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getRangeKind() {
		return rangeKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getRankFunction() {
		return rankFunctionEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getVectorSimilarity() {
		return vectorSimilarityEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getSuggesterKind() {
		return suggesterKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getAnalyzerKind() {
		return analyzerKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ESearchFactory getESearchFactory() {
		return (ESearchFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		searchMappingRegistryEClass = createEClass(SEARCH_MAPPING_REGISTRY);
		createEReference(searchMappingRegistryEClass, SEARCH_MAPPING_REGISTRY__UNITS);
		createEReference(searchMappingRegistryEClass, SEARCH_MAPPING_REGISTRY__ANALYZERS);

		indexUnitMappingEClass = createEClass(INDEX_UNIT_MAPPING);
		createEReference(indexUnitMappingEClass, INDEX_UNIT_MAPPING__EPACKAGE);
		createEAttribute(indexUnitMappingEClass, INDEX_UNIT_MAPPING__NAME);
		createEAttribute(indexUnitMappingEClass, INDEX_UNIT_MAPPING__TYPE_FIELD);
		createEAttribute(indexUnitMappingEClass, INDEX_UNIT_MAPPING__AUTO_MAP);
		createEReference(indexUnitMappingEClass, INDEX_UNIT_MAPPING__DEFAULT_ANALYZER);
		createEReference(indexUnitMappingEClass, INDEX_UNIT_MAPPING__REFRESH);
		createEReference(indexUnitMappingEClass, INDEX_UNIT_MAPPING__COMMIT);
		createEReference(indexUnitMappingEClass, INDEX_UNIT_MAPPING__SORT);
		createEReference(indexUnitMappingEClass, INDEX_UNIT_MAPPING__DOCUMENTS);

		documentMappingEClass = createEClass(DOCUMENT_MAPPING);
		createEReference(documentMappingEClass, DOCUMENT_MAPPING__ECLASS);
		createEAttribute(documentMappingEClass, DOCUMENT_MAPPING__TYPE_NAME);
		createEReference(documentMappingEClass, DOCUMENT_MAPPING__ID_FEATURE);
		createEAttribute(documentMappingEClass, DOCUMENT_MAPPING__AUTO_MAP);
		createEReference(documentMappingEClass, DOCUMENT_MAPPING__ANALYZER);
		createEReference(documentMappingEClass, DOCUMENT_MAPPING__MATERIALIZATION);
		createEReference(documentMappingEClass, DOCUMENT_MAPPING__FIELDS);
		createEReference(documentMappingEClass, DOCUMENT_MAPPING__REFERENCES);
		createEReference(documentMappingEClass, DOCUMENT_MAPPING__SUGGESTIONS);

		fieldMappingEClass = createEClass(FIELD_MAPPING);
		createEReference(fieldMappingEClass, FIELD_MAPPING__FEATURE);
		createEAttribute(fieldMappingEClass, FIELD_MAPPING__NAME);
		createEAttribute(fieldMappingEClass, FIELD_MAPPING__INDEXED);
		createEAttribute(fieldMappingEClass, FIELD_MAPPING__STORED);
		createEAttribute(fieldMappingEClass, FIELD_MAPPING__DOC_VALUES);
		createEAttribute(fieldMappingEClass, FIELD_MAPPING__BOOST);
		createEReference(fieldMappingEClass, FIELD_MAPPING__FACET);
		createEAttribute(fieldMappingEClass, FIELD_MAPPING__USE);
		createEReference(fieldMappingEClass, FIELD_MAPPING__SUB_FIELDS);

		textFieldMappingEClass = createEClass(TEXT_FIELD_MAPPING);
		createEReference(textFieldMappingEClass, TEXT_FIELD_MAPPING__ANALYZER);
		createEAttribute(textFieldMappingEClass, TEXT_FIELD_MAPPING__TERM_VECTORS);

		keywordFieldMappingEClass = createEClass(KEYWORD_FIELD_MAPPING);
		createEReference(keywordFieldMappingEClass, KEYWORD_FIELD_MAPPING__NORMALIZER);

		numericFieldMappingEClass = createEClass(NUMERIC_FIELD_MAPPING);
		createEAttribute(numericFieldMappingEClass, NUMERIC_FIELD_MAPPING__KIND);

		geoPointFieldMappingEClass = createEClass(GEO_POINT_FIELD_MAPPING);
		createEReference(geoPointFieldMappingEClass, GEO_POINT_FIELD_MAPPING__LATITUDE);
		createEReference(geoPointFieldMappingEClass, GEO_POINT_FIELD_MAPPING__LONGITUDE);

		rangeFieldMappingEClass = createEClass(RANGE_FIELD_MAPPING);
		createEReference(rangeFieldMappingEClass, RANGE_FIELD_MAPPING__LOWER_BOUND);
		createEReference(rangeFieldMappingEClass, RANGE_FIELD_MAPPING__UPPER_BOUND);
		createEAttribute(rangeFieldMappingEClass, RANGE_FIELD_MAPPING__KIND);

		rankSignalFieldMappingEClass = createEClass(RANK_SIGNAL_FIELD_MAPPING);
		createEAttribute(rankSignalFieldMappingEClass, RANK_SIGNAL_FIELD_MAPPING__FUNCTION);
		createEAttribute(rankSignalFieldMappingEClass, RANK_SIGNAL_FIELD_MAPPING__PIVOT);
		createEAttribute(rankSignalFieldMappingEClass, RANK_SIGNAL_FIELD_MAPPING__EXPONENT);

		vectorFieldMappingEClass = createEClass(VECTOR_FIELD_MAPPING);
		createEReference(vectorFieldMappingEClass, VECTOR_FIELD_MAPPING__SOURCES);
		createEAttribute(vectorFieldMappingEClass, VECTOR_FIELD_MAPPING__PROVIDER);
		createEAttribute(vectorFieldMappingEClass, VECTOR_FIELD_MAPPING__DIMENSIONS);
		createEAttribute(vectorFieldMappingEClass, VECTOR_FIELD_MAPPING__SIMILARITY);
		createEAttribute(vectorFieldMappingEClass, VECTOR_FIELD_MAPPING__MODEL_VERSION);

		referenceMappingEClass = createEClass(REFERENCE_MAPPING);
		createEReference(referenceMappingEClass, REFERENCE_MAPPING__EREFERENCE);
		createEAttribute(referenceMappingEClass, REFERENCE_MAPPING__STRATEGY);
		createEAttribute(referenceMappingEClass, REFERENCE_MAPPING__PREFIX);
		createEAttribute(referenceMappingEClass, REFERENCE_MAPPING__DEPTH);
		createEReference(referenceMappingEClass, REFERENCE_MAPPING__TARGET);
		createEReference(referenceMappingEClass, REFERENCE_MAPPING__INCLUDES);

		materializationEClass = createEClass(MATERIALIZATION);
		createEAttribute(materializationEClass, MATERIALIZATION__STORE_OBJECT);
		createEAttribute(materializationEClass, MATERIALIZATION__FIELD_NAME);
		createEAttribute(materializationEClass, MATERIALIZATION__FORMAT);

		facetMappingEClass = createEClass(FACET_MAPPING);
		createEAttribute(facetMappingEClass, FACET_MAPPING__DIMENSION);
		createEAttribute(facetMappingEClass, FACET_MAPPING__KIND);
		createEAttribute(facetMappingEClass, FACET_MAPPING__HIERARCHICAL);
		createEAttribute(facetMappingEClass, FACET_MAPPING__MULTI_VALUED);

		suggestSourceEClass = createEClass(SUGGEST_SOURCE);
		createEAttribute(suggestSourceEClass, SUGGEST_SOURCE__NAME);
		createEReference(suggestSourceEClass, SUGGEST_SOURCE__FEATURE);
		createEReference(suggestSourceEClass, SUGGEST_SOURCE__WEIGHT);
		createEReference(suggestSourceEClass, SUGGEST_SOURCE__CONTEXTS);
		createEAttribute(suggestSourceEClass, SUGGEST_SOURCE__KIND);
		createEReference(suggestSourceEClass, SUGGEST_SOURCE__ANALYZER);

		refreshPolicyEClass = createEClass(REFRESH_POLICY);
		createEAttribute(refreshPolicyEClass, REFRESH_POLICY__MODE);
		createEAttribute(refreshPolicyEClass, REFRESH_POLICY__INTERVAL_MILLIS);

		commitPolicyEClass = createEClass(COMMIT_POLICY);
		createEAttribute(commitPolicyEClass, COMMIT_POLICY__MAX_UNCOMMITTED_DOCS);
		createEAttribute(commitPolicyEClass, COMMIT_POLICY__MAX_INTERVAL_MILLIS);
		createEAttribute(commitPolicyEClass, COMMIT_POLICY__COMMIT_ON_CLOSE);

		indexSortEClass = createEClass(INDEX_SORT);
		createEReference(indexSortEClass, INDEX_SORT__ENTRIES);

		sortEntryEClass = createEClass(SORT_ENTRY);
		createEReference(sortEntryEClass, SORT_ENTRY__FEATURE);
		createEAttribute(sortEntryEClass, SORT_ENTRY__DESCENDING);
		createEAttribute(sortEntryEClass, SORT_ENTRY__MISSING_LAST);

		analyzerDefinitionEClass = createEClass(ANALYZER_DEFINITION);
		createEAttribute(analyzerDefinitionEClass, ANALYZER_DEFINITION__NAME);
		createEAttribute(analyzerDefinitionEClass, ANALYZER_DEFINITION__KIND);
		createEAttribute(analyzerDefinitionEClass, ANALYZER_DEFINITION__LANGUAGE);
		createEAttribute(analyzerDefinitionEClass, ANALYZER_DEFINITION__SERVICE_FILTER);
		createEReference(analyzerDefinitionEClass, ANALYZER_DEFINITION__PARAMETERS);

		analyzerParameterEClass = createEClass(ANALYZER_PARAMETER);
		createEAttribute(analyzerParameterEClass, ANALYZER_PARAMETER__KEY);
		createEAttribute(analyzerParameterEClass, ANALYZER_PARAMETER__VALUE);

		// Create enums
		fieldUseEEnum = createEEnum(FIELD_USE);
		referenceStrategyEEnum = createEEnum(REFERENCE_STRATEGY);
		refreshModeEEnum = createEEnum(REFRESH_MODE);
		facetKindEEnum = createEEnum(FACET_KIND);
		numericKindEEnum = createEEnum(NUMERIC_KIND);
		rangeKindEEnum = createEEnum(RANGE_KIND);
		rankFunctionEEnum = createEEnum(RANK_FUNCTION);
		vectorSimilarityEEnum = createEEnum(VECTOR_SIMILARITY);
		suggesterKindEEnum = createEEnum(SUGGESTER_KIND);
		analyzerKindEEnum = createEEnum(ANALYZER_KIND);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		textFieldMappingEClass.getESuperTypes().add(this.getFieldMapping());
		keywordFieldMappingEClass.getESuperTypes().add(this.getFieldMapping());
		numericFieldMappingEClass.getESuperTypes().add(this.getFieldMapping());
		geoPointFieldMappingEClass.getESuperTypes().add(this.getFieldMapping());
		rangeFieldMappingEClass.getESuperTypes().add(this.getFieldMapping());
		rankSignalFieldMappingEClass.getESuperTypes().add(this.getFieldMapping());
		vectorFieldMappingEClass.getESuperTypes().add(this.getFieldMapping());

		// Initialize classes, features, and operations; add parameters
		initEClass(searchMappingRegistryEClass, SearchMappingRegistry.class, "SearchMappingRegistry", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSearchMappingRegistry_Units(), this.getIndexUnitMapping(), null, "units", null, 0, -1, SearchMappingRegistry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSearchMappingRegistry_Analyzers(), this.getAnalyzerDefinition(), null, "analyzers", null, 0, -1, SearchMappingRegistry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(indexUnitMappingEClass, IndexUnitMapping.class, "IndexUnitMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIndexUnitMapping_EPackage(), ecorePackage.getEPackage(), null, "ePackage", null, 1, 1, IndexUnitMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIndexUnitMapping_Name(), ecorePackage.getEString(), "name", null, 1, 1, IndexUnitMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIndexUnitMapping_TypeField(), ecorePackage.getEString(), "typeField", "_type", 0, 1, IndexUnitMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIndexUnitMapping_AutoMap(), ecorePackage.getEBoolean(), "autoMap", "true", 0, 1, IndexUnitMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIndexUnitMapping_DefaultAnalyzer(), this.getAnalyzerDefinition(), null, "defaultAnalyzer", null, 0, 1, IndexUnitMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIndexUnitMapping_Refresh(), this.getRefreshPolicy(), null, "refresh", null, 0, 1, IndexUnitMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIndexUnitMapping_Commit(), this.getCommitPolicy(), null, "commit", null, 0, 1, IndexUnitMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIndexUnitMapping_Sort(), this.getIndexSort(), null, "sort", null, 0, 1, IndexUnitMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIndexUnitMapping_Documents(), this.getDocumentMapping(), null, "documents", null, 0, -1, IndexUnitMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(documentMappingEClass, DocumentMapping.class, "DocumentMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDocumentMapping_EClass(), ecorePackage.getEClass(), null, "eClass", null, 1, 1, DocumentMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDocumentMapping_TypeName(), ecorePackage.getEString(), "typeName", null, 0, 1, DocumentMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentMapping_IdFeature(), ecorePackage.getEAttribute(), null, "idFeature", null, 0, 1, DocumentMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDocumentMapping_AutoMap(), ecorePackage.getEBoolean(), "autoMap", "true", 0, 1, DocumentMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentMapping_Analyzer(), this.getAnalyzerDefinition(), null, "analyzer", null, 0, 1, DocumentMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentMapping_Materialization(), this.getMaterialization(), null, "materialization", null, 0, 1, DocumentMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentMapping_Fields(), this.getFieldMapping(), null, "fields", null, 0, -1, DocumentMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentMapping_References(), this.getReferenceMapping(), null, "references", null, 0, -1, DocumentMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDocumentMapping_Suggestions(), this.getSuggestSource(), null, "suggestions", null, 0, -1, DocumentMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(fieldMappingEClass, FieldMapping.class, "FieldMapping", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getFieldMapping_Feature(), ecorePackage.getEAttribute(), null, "feature", null, 0, 1, FieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFieldMapping_Name(), ecorePackage.getEString(), "name", null, 0, 1, FieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFieldMapping_Indexed(), ecorePackage.getEBoolean(), "indexed", "true", 0, 1, FieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFieldMapping_Stored(), ecorePackage.getEBoolean(), "stored", "false", 0, 1, FieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFieldMapping_DocValues(), ecorePackage.getEBoolean(), "docValues", "false", 0, 1, FieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFieldMapping_Boost(), ecorePackage.getEFloat(), "boost", "1.0", 0, 1, FieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFieldMapping_Facet(), this.getFacetMapping(), null, "facet", null, 0, 1, FieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFieldMapping_Use(), this.getFieldUse(), "use", null, 0, -1, FieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFieldMapping_SubFields(), this.getFieldMapping(), null, "subFields", null, 0, -1, FieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(textFieldMappingEClass, TextFieldMapping.class, "TextFieldMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTextFieldMapping_Analyzer(), this.getAnalyzerDefinition(), null, "analyzer", null, 0, 1, TextFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTextFieldMapping_TermVectors(), ecorePackage.getEBoolean(), "termVectors", "false", 0, 1, TextFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(keywordFieldMappingEClass, KeywordFieldMapping.class, "KeywordFieldMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getKeywordFieldMapping_Normalizer(), this.getAnalyzerDefinition(), null, "normalizer", null, 0, 1, KeywordFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(numericFieldMappingEClass, NumericFieldMapping.class, "NumericFieldMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getNumericFieldMapping_Kind(), this.getNumericKind(), "kind", "AUTO", 0, 1, NumericFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(geoPointFieldMappingEClass, GeoPointFieldMapping.class, "GeoPointFieldMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGeoPointFieldMapping_Latitude(), ecorePackage.getEAttribute(), null, "latitude", null, 0, 1, GeoPointFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getGeoPointFieldMapping_Longitude(), ecorePackage.getEAttribute(), null, "longitude", null, 0, 1, GeoPointFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(rangeFieldMappingEClass, RangeFieldMapping.class, "RangeFieldMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getRangeFieldMapping_LowerBound(), ecorePackage.getEAttribute(), null, "lowerBound", null, 1, 1, RangeFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRangeFieldMapping_UpperBound(), ecorePackage.getEAttribute(), null, "upperBound", null, 1, 1, RangeFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRangeFieldMapping_Kind(), this.getRangeKind(), "kind", "AUTO", 0, 1, RangeFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(rankSignalFieldMappingEClass, RankSignalFieldMapping.class, "RankSignalFieldMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRankSignalFieldMapping_Function(), this.getRankFunction(), "function", "SATURATION", 1, 1, RankSignalFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRankSignalFieldMapping_Pivot(), ecorePackage.getEDouble(), "pivot", null, 0, 1, RankSignalFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRankSignalFieldMapping_Exponent(), ecorePackage.getEDouble(), "exponent", "1.0", 0, 1, RankSignalFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(vectorFieldMappingEClass, VectorFieldMapping.class, "VectorFieldMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getVectorFieldMapping_Sources(), ecorePackage.getEAttribute(), null, "sources", null, 0, -1, VectorFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVectorFieldMapping_Provider(), ecorePackage.getEString(), "provider", null, 0, 1, VectorFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVectorFieldMapping_Dimensions(), ecorePackage.getEInt(), "dimensions", null, 0, 1, VectorFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVectorFieldMapping_Similarity(), this.getVectorSimilarity(), "similarity", "COSINE", 0, 1, VectorFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVectorFieldMapping_ModelVersion(), ecorePackage.getEString(), "modelVersion", null, 0, 1, VectorFieldMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(referenceMappingEClass, ReferenceMapping.class, "ReferenceMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getReferenceMapping_EReference(), ecorePackage.getEReference(), null, "eReference", null, 1, 1, ReferenceMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReferenceMapping_Strategy(), this.getReferenceStrategy(), "strategy", "ID_ONLY", 1, 1, ReferenceMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReferenceMapping_Prefix(), ecorePackage.getEString(), "prefix", null, 0, 1, ReferenceMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReferenceMapping_Depth(), ecorePackage.getEInt(), "depth", "1", 0, 1, ReferenceMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReferenceMapping_Target(), this.getDocumentMapping(), null, "target", null, 0, 1, ReferenceMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReferenceMapping_Includes(), ecorePackage.getEStructuralFeature(), null, "includes", null, 0, -1, ReferenceMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(materializationEClass, Materialization.class, "Materialization", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMaterialization_StoreObject(), ecorePackage.getEBoolean(), "storeObject", "true", 1, 1, Materialization.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMaterialization_FieldName(), ecorePackage.getEString(), "fieldName", "_source", 0, 1, Materialization.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMaterialization_Format(), ecorePackage.getEString(), "format", null, 0, 1, Materialization.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(facetMappingEClass, FacetMapping.class, "FacetMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFacetMapping_Dimension(), ecorePackage.getEString(), "dimension", null, 0, 1, FacetMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFacetMapping_Kind(), this.getFacetKind(), "kind", "SORTED_SET", 0, 1, FacetMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFacetMapping_Hierarchical(), ecorePackage.getEBoolean(), "hierarchical", "false", 0, 1, FacetMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFacetMapping_MultiValued(), ecorePackage.getEBoolean(), "multiValued", "false", 0, 1, FacetMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(suggestSourceEClass, SuggestSource.class, "SuggestSource", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSuggestSource_Name(), ecorePackage.getEString(), "name", null, 1, 1, SuggestSource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSuggestSource_Feature(), ecorePackage.getEAttribute(), null, "feature", null, 1, 1, SuggestSource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSuggestSource_Weight(), ecorePackage.getEAttribute(), null, "weight", null, 0, 1, SuggestSource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSuggestSource_Contexts(), ecorePackage.getEAttribute(), null, "contexts", null, 0, -1, SuggestSource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSuggestSource_Kind(), this.getSuggesterKind(), "kind", "ANALYZING", 1, 1, SuggestSource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSuggestSource_Analyzer(), this.getAnalyzerDefinition(), null, "analyzer", null, 0, 1, SuggestSource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(refreshPolicyEClass, RefreshPolicy.class, "RefreshPolicy", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRefreshPolicy_Mode(), this.getRefreshMode(), "mode", "NEAR_REAL_TIME", 1, 1, RefreshPolicy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRefreshPolicy_IntervalMillis(), ecorePackage.getELong(), "intervalMillis", "1000", 0, 1, RefreshPolicy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(commitPolicyEClass, CommitPolicy.class, "CommitPolicy", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getCommitPolicy_MaxUncommittedDocs(), ecorePackage.getEInt(), "maxUncommittedDocs", "0", 0, 1, CommitPolicy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCommitPolicy_MaxIntervalMillis(), ecorePackage.getELong(), "maxIntervalMillis", "0", 0, 1, CommitPolicy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCommitPolicy_CommitOnClose(), ecorePackage.getEBoolean(), "commitOnClose", "true", 0, 1, CommitPolicy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(indexSortEClass, IndexSort.class, "IndexSort", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIndexSort_Entries(), this.getSortEntry(), null, "entries", null, 1, -1, IndexSort.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sortEntryEClass, SortEntry.class, "SortEntry", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSortEntry_Feature(), ecorePackage.getEAttribute(), null, "feature", null, 1, 1, SortEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSortEntry_Descending(), ecorePackage.getEBoolean(), "descending", "false", 0, 1, SortEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSortEntry_MissingLast(), ecorePackage.getEBoolean(), "missingLast", "true", 0, 1, SortEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(analyzerDefinitionEClass, AnalyzerDefinition.class, "AnalyzerDefinition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAnalyzerDefinition_Name(), ecorePackage.getEString(), "name", null, 1, 1, AnalyzerDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnalyzerDefinition_Kind(), this.getAnalyzerKind(), "kind", "STANDARD", 1, 1, AnalyzerDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnalyzerDefinition_Language(), ecorePackage.getEString(), "language", null, 0, 1, AnalyzerDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnalyzerDefinition_ServiceFilter(), ecorePackage.getEString(), "serviceFilter", null, 0, 1, AnalyzerDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAnalyzerDefinition_Parameters(), this.getAnalyzerParameter(), null, "parameters", null, 0, -1, AnalyzerDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(analyzerParameterEClass, AnalyzerParameter.class, "AnalyzerParameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAnalyzerParameter_Key(), ecorePackage.getEString(), "key", null, 1, 1, AnalyzerParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAnalyzerParameter_Value(), ecorePackage.getEString(), "value", null, 0, 1, AnalyzerParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(fieldUseEEnum, FieldUse.class, "FieldUse");
		addEEnumLiteral(fieldUseEEnum, FieldUse.MATCH);
		addEEnumLiteral(fieldUseEEnum, FieldUse.EXACT);
		addEEnumLiteral(fieldUseEEnum, FieldUse.RANGE);
		addEEnumLiteral(fieldUseEEnum, FieldUse.SORT);
		addEEnumLiteral(fieldUseEEnum, FieldUse.FACET);
		addEEnumLiteral(fieldUseEEnum, FieldUse.HIGHLIGHT);
		addEEnumLiteral(fieldUseEEnum, FieldUse.SIMILARITY);

		initEEnum(referenceStrategyEEnum, ReferenceStrategy.class, "ReferenceStrategy");
		addEEnumLiteral(referenceStrategyEEnum, ReferenceStrategy.ID_ONLY);
		addEEnumLiteral(referenceStrategyEEnum, ReferenceStrategy.EMBED);
		addEEnumLiteral(referenceStrategyEEnum, ReferenceStrategy.NESTED);

		initEEnum(refreshModeEEnum, RefreshMode.class, "RefreshMode");
		addEEnumLiteral(refreshModeEEnum, RefreshMode.NEAR_REAL_TIME);
		addEEnumLiteral(refreshModeEEnum, RefreshMode.ON_COMMIT);
		addEEnumLiteral(refreshModeEEnum, RefreshMode.MANUAL);

		initEEnum(facetKindEEnum, FacetKind.class, "FacetKind");
		addEEnumLiteral(facetKindEEnum, FacetKind.SORTED_SET);
		addEEnumLiteral(facetKindEEnum, FacetKind.TAXONOMY);

		initEEnum(numericKindEEnum, NumericKind.class, "NumericKind");
		addEEnumLiteral(numericKindEEnum, NumericKind.AUTO);
		addEEnumLiteral(numericKindEEnum, NumericKind.INT);
		addEEnumLiteral(numericKindEEnum, NumericKind.LONG);
		addEEnumLiteral(numericKindEEnum, NumericKind.FLOAT);
		addEEnumLiteral(numericKindEEnum, NumericKind.DOUBLE);
		addEEnumLiteral(numericKindEEnum, NumericKind.DATE);

		initEEnum(rangeKindEEnum, RangeKind.class, "RangeKind");
		addEEnumLiteral(rangeKindEEnum, RangeKind.AUTO);
		addEEnumLiteral(rangeKindEEnum, RangeKind.LONG);
		addEEnumLiteral(rangeKindEEnum, RangeKind.DOUBLE);
		addEEnumLiteral(rangeKindEEnum, RangeKind.DATE);

		initEEnum(rankFunctionEEnum, RankFunction.class, "RankFunction");
		addEEnumLiteral(rankFunctionEEnum, RankFunction.SATURATION);
		addEEnumLiteral(rankFunctionEEnum, RankFunction.LOG);
		addEEnumLiteral(rankFunctionEEnum, RankFunction.SIGMOID);

		initEEnum(vectorSimilarityEEnum, VectorSimilarity.class, "VectorSimilarity");
		addEEnumLiteral(vectorSimilarityEEnum, VectorSimilarity.COSINE);
		addEEnumLiteral(vectorSimilarityEEnum, VectorSimilarity.DOT_PRODUCT);
		addEEnumLiteral(vectorSimilarityEEnum, VectorSimilarity.EUCLIDEAN);
		addEEnumLiteral(vectorSimilarityEEnum, VectorSimilarity.MAXIMUM_INNER_PRODUCT);

		initEEnum(suggesterKindEEnum, SuggesterKind.class, "SuggesterKind");
		addEEnumLiteral(suggesterKindEEnum, SuggesterKind.ANALYZING);
		addEEnumLiteral(suggesterKindEEnum, SuggesterKind.FUZZY);
		addEEnumLiteral(suggesterKindEEnum, SuggesterKind.COMPLETION);
		addEEnumLiteral(suggesterKindEEnum, SuggesterKind.FREE_TEXT);

		initEEnum(analyzerKindEEnum, AnalyzerKind.class, "AnalyzerKind");
		addEEnumLiteral(analyzerKindEEnum, AnalyzerKind.STANDARD);
		addEEnumLiteral(analyzerKindEEnum, AnalyzerKind.KEYWORD);
		addEEnumLiteral(analyzerKindEEnum, AnalyzerKind.SIMPLE);
		addEEnumLiteral(analyzerKindEEnum, AnalyzerKind.WHITESPACE);
		addEEnumLiteral(analyzerKindEEnum, AnalyzerKind.STOP);
		addEEnumLiteral(analyzerKindEEnum, AnalyzerKind.CUSTOM);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// Version
		createVersionAnnotations();
	}

	/**
	 * Initializes the annotations for <b>Version</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createVersionAnnotations() {
		String source = "Version";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "value", "1.0"
		   });
	}

} //ESearchPackageImpl
