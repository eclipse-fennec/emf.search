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
package org.eclipse.fennec.search.esearch.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.search.esearch.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.search.esearch.ESearchPackage
 * @generated
 */
public class ESearchAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ESearchPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ESearchAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = ESearchPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ESearchSwitch<Adapter> modelSwitch =
		new ESearchSwitch<Adapter>() {
			@Override
			public Adapter caseSearchMappingRegistry(SearchMappingRegistry object) {
				return createSearchMappingRegistryAdapter();
			}
			@Override
			public Adapter caseIndexUnitMapping(IndexUnitMapping object) {
				return createIndexUnitMappingAdapter();
			}
			@Override
			public Adapter caseDocumentMapping(DocumentMapping object) {
				return createDocumentMappingAdapter();
			}
			@Override
			public Adapter caseFieldMapping(FieldMapping object) {
				return createFieldMappingAdapter();
			}
			@Override
			public Adapter caseTextFieldMapping(TextFieldMapping object) {
				return createTextFieldMappingAdapter();
			}
			@Override
			public Adapter caseKeywordFieldMapping(KeywordFieldMapping object) {
				return createKeywordFieldMappingAdapter();
			}
			@Override
			public Adapter caseNumericFieldMapping(NumericFieldMapping object) {
				return createNumericFieldMappingAdapter();
			}
			@Override
			public Adapter caseGeoPointFieldMapping(GeoPointFieldMapping object) {
				return createGeoPointFieldMappingAdapter();
			}
			@Override
			public Adapter caseRangeFieldMapping(RangeFieldMapping object) {
				return createRangeFieldMappingAdapter();
			}
			@Override
			public Adapter caseRankSignalFieldMapping(RankSignalFieldMapping object) {
				return createRankSignalFieldMappingAdapter();
			}
			@Override
			public Adapter caseVectorFieldMapping(VectorFieldMapping object) {
				return createVectorFieldMappingAdapter();
			}
			@Override
			public Adapter caseReferenceMapping(ReferenceMapping object) {
				return createReferenceMappingAdapter();
			}
			@Override
			public Adapter caseMaterialization(Materialization object) {
				return createMaterializationAdapter();
			}
			@Override
			public Adapter caseFacetMapping(FacetMapping object) {
				return createFacetMappingAdapter();
			}
			@Override
			public Adapter caseSuggestSource(SuggestSource object) {
				return createSuggestSourceAdapter();
			}
			@Override
			public Adapter caseRefreshPolicy(RefreshPolicy object) {
				return createRefreshPolicyAdapter();
			}
			@Override
			public Adapter caseCommitPolicy(CommitPolicy object) {
				return createCommitPolicyAdapter();
			}
			@Override
			public Adapter caseIndexSort(IndexSort object) {
				return createIndexSortAdapter();
			}
			@Override
			public Adapter caseSortEntry(SortEntry object) {
				return createSortEntryAdapter();
			}
			@Override
			public Adapter caseAnalyzerDefinition(AnalyzerDefinition object) {
				return createAnalyzerDefinitionAdapter();
			}
			@Override
			public Adapter caseAnalyzerParameter(AnalyzerParameter object) {
				return createAnalyzerParameterAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.SearchMappingRegistry <em>Search Mapping Registry</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.SearchMappingRegistry
	 * @generated
	 */
	public Adapter createSearchMappingRegistryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping <em>Index Unit Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.IndexUnitMapping
	 * @generated
	 */
	public Adapter createIndexUnitMappingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.DocumentMapping <em>Document Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.DocumentMapping
	 * @generated
	 */
	public Adapter createDocumentMappingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.FieldMapping <em>Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.FieldMapping
	 * @generated
	 */
	public Adapter createFieldMappingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.TextFieldMapping <em>Text Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.TextFieldMapping
	 * @generated
	 */
	public Adapter createTextFieldMappingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.KeywordFieldMapping <em>Keyword Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.KeywordFieldMapping
	 * @generated
	 */
	public Adapter createKeywordFieldMappingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.NumericFieldMapping <em>Numeric Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.NumericFieldMapping
	 * @generated
	 */
	public Adapter createNumericFieldMappingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.GeoPointFieldMapping <em>Geo Point Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.GeoPointFieldMapping
	 * @generated
	 */
	public Adapter createGeoPointFieldMappingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.RangeFieldMapping <em>Range Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.RangeFieldMapping
	 * @generated
	 */
	public Adapter createRangeFieldMappingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.RankSignalFieldMapping <em>Rank Signal Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.RankSignalFieldMapping
	 * @generated
	 */
	public Adapter createRankSignalFieldMappingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.VectorFieldMapping <em>Vector Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.VectorFieldMapping
	 * @generated
	 */
	public Adapter createVectorFieldMappingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.ReferenceMapping <em>Reference Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.ReferenceMapping
	 * @generated
	 */
	public Adapter createReferenceMappingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.Materialization <em>Materialization</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.Materialization
	 * @generated
	 */
	public Adapter createMaterializationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.FacetMapping <em>Facet Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.FacetMapping
	 * @generated
	 */
	public Adapter createFacetMappingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.SuggestSource <em>Suggest Source</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.SuggestSource
	 * @generated
	 */
	public Adapter createSuggestSourceAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.RefreshPolicy <em>Refresh Policy</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.RefreshPolicy
	 * @generated
	 */
	public Adapter createRefreshPolicyAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.CommitPolicy <em>Commit Policy</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.CommitPolicy
	 * @generated
	 */
	public Adapter createCommitPolicyAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.IndexSort <em>Index Sort</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.IndexSort
	 * @generated
	 */
	public Adapter createIndexSortAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.SortEntry <em>Sort Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.SortEntry
	 * @generated
	 */
	public Adapter createSortEntryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.AnalyzerDefinition <em>Analyzer Definition</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.AnalyzerDefinition
	 * @generated
	 */
	public Adapter createAnalyzerDefinitionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.search.esearch.AnalyzerParameter <em>Analyzer Parameter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.search.esearch.AnalyzerParameter
	 * @generated
	 */
	public Adapter createAnalyzerParameterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //ESearchAdapterFactory
