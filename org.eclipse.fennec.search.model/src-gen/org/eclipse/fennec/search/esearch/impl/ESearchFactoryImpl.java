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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.search.esearch.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ESearchFactoryImpl extends EFactoryImpl implements ESearchFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ESearchFactory init() {
		try {
			ESearchFactory theESearchFactory = (ESearchFactory)EPackage.Registry.INSTANCE.getEFactory(ESearchPackage.eNS_URI);
			if (theESearchFactory != null) {
				return theESearchFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ESearchFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ESearchFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case ESearchPackage.SEARCH_MAPPING_REGISTRY: return createSearchMappingRegistry();
			case ESearchPackage.INDEX_UNIT_MAPPING: return createIndexUnitMapping();
			case ESearchPackage.DOCUMENT_MAPPING: return createDocumentMapping();
			case ESearchPackage.TEXT_FIELD_MAPPING: return createTextFieldMapping();
			case ESearchPackage.KEYWORD_FIELD_MAPPING: return createKeywordFieldMapping();
			case ESearchPackage.NUMERIC_FIELD_MAPPING: return createNumericFieldMapping();
			case ESearchPackage.GEO_POINT_FIELD_MAPPING: return createGeoPointFieldMapping();
			case ESearchPackage.RANGE_FIELD_MAPPING: return createRangeFieldMapping();
			case ESearchPackage.RANK_SIGNAL_FIELD_MAPPING: return createRankSignalFieldMapping();
			case ESearchPackage.VECTOR_FIELD_MAPPING: return createVectorFieldMapping();
			case ESearchPackage.REFERENCE_MAPPING: return createReferenceMapping();
			case ESearchPackage.MATERIALIZATION: return createMaterialization();
			case ESearchPackage.FACET_MAPPING: return createFacetMapping();
			case ESearchPackage.SUGGEST_SOURCE: return createSuggestSource();
			case ESearchPackage.REFRESH_POLICY: return createRefreshPolicy();
			case ESearchPackage.COMMIT_POLICY: return createCommitPolicy();
			case ESearchPackage.INDEX_SORT: return createIndexSort();
			case ESearchPackage.SORT_ENTRY: return createSortEntry();
			case ESearchPackage.ANALYZER_DEFINITION: return createAnalyzerDefinition();
			case ESearchPackage.ANALYZER_PARAMETER: return createAnalyzerParameter();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case ESearchPackage.FIELD_USE:
				return createFieldUseFromString(eDataType, initialValue);
			case ESearchPackage.REFERENCE_STRATEGY:
				return createReferenceStrategyFromString(eDataType, initialValue);
			case ESearchPackage.REFRESH_MODE:
				return createRefreshModeFromString(eDataType, initialValue);
			case ESearchPackage.FACET_KIND:
				return createFacetKindFromString(eDataType, initialValue);
			case ESearchPackage.NUMERIC_KIND:
				return createNumericKindFromString(eDataType, initialValue);
			case ESearchPackage.RANGE_KIND:
				return createRangeKindFromString(eDataType, initialValue);
			case ESearchPackage.RANK_FUNCTION:
				return createRankFunctionFromString(eDataType, initialValue);
			case ESearchPackage.VECTOR_SIMILARITY:
				return createVectorSimilarityFromString(eDataType, initialValue);
			case ESearchPackage.SUGGESTER_KIND:
				return createSuggesterKindFromString(eDataType, initialValue);
			case ESearchPackage.ANALYZER_KIND:
				return createAnalyzerKindFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case ESearchPackage.FIELD_USE:
				return convertFieldUseToString(eDataType, instanceValue);
			case ESearchPackage.REFERENCE_STRATEGY:
				return convertReferenceStrategyToString(eDataType, instanceValue);
			case ESearchPackage.REFRESH_MODE:
				return convertRefreshModeToString(eDataType, instanceValue);
			case ESearchPackage.FACET_KIND:
				return convertFacetKindToString(eDataType, instanceValue);
			case ESearchPackage.NUMERIC_KIND:
				return convertNumericKindToString(eDataType, instanceValue);
			case ESearchPackage.RANGE_KIND:
				return convertRangeKindToString(eDataType, instanceValue);
			case ESearchPackage.RANK_FUNCTION:
				return convertRankFunctionToString(eDataType, instanceValue);
			case ESearchPackage.VECTOR_SIMILARITY:
				return convertVectorSimilarityToString(eDataType, instanceValue);
			case ESearchPackage.SUGGESTER_KIND:
				return convertSuggesterKindToString(eDataType, instanceValue);
			case ESearchPackage.ANALYZER_KIND:
				return convertAnalyzerKindToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SearchMappingRegistry createSearchMappingRegistry() {
		SearchMappingRegistryImpl searchMappingRegistry = new SearchMappingRegistryImpl();
		return searchMappingRegistry;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IndexUnitMapping createIndexUnitMapping() {
		IndexUnitMappingImpl indexUnitMapping = new IndexUnitMappingImpl();
		return indexUnitMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DocumentMapping createDocumentMapping() {
		DocumentMappingImpl documentMapping = new DocumentMappingImpl();
		return documentMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TextFieldMapping createTextFieldMapping() {
		TextFieldMappingImpl textFieldMapping = new TextFieldMappingImpl();
		return textFieldMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public KeywordFieldMapping createKeywordFieldMapping() {
		KeywordFieldMappingImpl keywordFieldMapping = new KeywordFieldMappingImpl();
		return keywordFieldMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NumericFieldMapping createNumericFieldMapping() {
		NumericFieldMappingImpl numericFieldMapping = new NumericFieldMappingImpl();
		return numericFieldMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GeoPointFieldMapping createGeoPointFieldMapping() {
		GeoPointFieldMappingImpl geoPointFieldMapping = new GeoPointFieldMappingImpl();
		return geoPointFieldMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RangeFieldMapping createRangeFieldMapping() {
		RangeFieldMappingImpl rangeFieldMapping = new RangeFieldMappingImpl();
		return rangeFieldMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RankSignalFieldMapping createRankSignalFieldMapping() {
		RankSignalFieldMappingImpl rankSignalFieldMapping = new RankSignalFieldMappingImpl();
		return rankSignalFieldMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public VectorFieldMapping createVectorFieldMapping() {
		VectorFieldMappingImpl vectorFieldMapping = new VectorFieldMappingImpl();
		return vectorFieldMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReferenceMapping createReferenceMapping() {
		ReferenceMappingImpl referenceMapping = new ReferenceMappingImpl();
		return referenceMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Materialization createMaterialization() {
		MaterializationImpl materialization = new MaterializationImpl();
		return materialization;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FacetMapping createFacetMapping() {
		FacetMappingImpl facetMapping = new FacetMappingImpl();
		return facetMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SuggestSource createSuggestSource() {
		SuggestSourceImpl suggestSource = new SuggestSourceImpl();
		return suggestSource;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RefreshPolicy createRefreshPolicy() {
		RefreshPolicyImpl refreshPolicy = new RefreshPolicyImpl();
		return refreshPolicy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CommitPolicy createCommitPolicy() {
		CommitPolicyImpl commitPolicy = new CommitPolicyImpl();
		return commitPolicy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IndexSort createIndexSort() {
		IndexSortImpl indexSort = new IndexSortImpl();
		return indexSort;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SortEntry createSortEntry() {
		SortEntryImpl sortEntry = new SortEntryImpl();
		return sortEntry;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AnalyzerDefinition createAnalyzerDefinition() {
		AnalyzerDefinitionImpl analyzerDefinition = new AnalyzerDefinitionImpl();
		return analyzerDefinition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AnalyzerParameter createAnalyzerParameter() {
		AnalyzerParameterImpl analyzerParameter = new AnalyzerParameterImpl();
		return analyzerParameter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FieldUse createFieldUseFromString(EDataType eDataType, String initialValue) {
		FieldUse result = FieldUse.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertFieldUseToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ReferenceStrategy createReferenceStrategyFromString(EDataType eDataType, String initialValue) {
		ReferenceStrategy result = ReferenceStrategy.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertReferenceStrategyToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RefreshMode createRefreshModeFromString(EDataType eDataType, String initialValue) {
		RefreshMode result = RefreshMode.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertRefreshModeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FacetKind createFacetKindFromString(EDataType eDataType, String initialValue) {
		FacetKind result = FacetKind.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertFacetKindToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NumericKind createNumericKindFromString(EDataType eDataType, String initialValue) {
		NumericKind result = NumericKind.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertNumericKindToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RangeKind createRangeKindFromString(EDataType eDataType, String initialValue) {
		RangeKind result = RangeKind.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertRangeKindToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RankFunction createRankFunctionFromString(EDataType eDataType, String initialValue) {
		RankFunction result = RankFunction.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertRankFunctionToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public VectorSimilarity createVectorSimilarityFromString(EDataType eDataType, String initialValue) {
		VectorSimilarity result = VectorSimilarity.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertVectorSimilarityToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SuggesterKind createSuggesterKindFromString(EDataType eDataType, String initialValue) {
		SuggesterKind result = SuggesterKind.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertSuggesterKindToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AnalyzerKind createAnalyzerKindFromString(EDataType eDataType, String initialValue) {
		AnalyzerKind result = AnalyzerKind.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertAnalyzerKindToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ESearchPackage getESearchPackage() {
		return (ESearchPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static ESearchPackage getPackage() {
		return ESearchPackage.eINSTANCE;
	}

} //ESearchFactoryImpl
