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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import org.eclipse.fennec.search.esearch.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.search.esearch.ESearchPackage
 * @generated
 */
public class ESearchSwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ESearchPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ESearchSwitch() {
		if (modelPackage == null) {
			modelPackage = ESearchPackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case ESearchPackage.SEARCH_MAPPING_REGISTRY: {
				SearchMappingRegistry searchMappingRegistry = (SearchMappingRegistry)theEObject;
				T result = caseSearchMappingRegistry(searchMappingRegistry);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.INDEX_UNIT_MAPPING: {
				IndexUnitMapping indexUnitMapping = (IndexUnitMapping)theEObject;
				T result = caseIndexUnitMapping(indexUnitMapping);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.DOCUMENT_MAPPING: {
				DocumentMapping documentMapping = (DocumentMapping)theEObject;
				T result = caseDocumentMapping(documentMapping);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.FIELD_MAPPING: {
				FieldMapping fieldMapping = (FieldMapping)theEObject;
				T result = caseFieldMapping(fieldMapping);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.TEXT_FIELD_MAPPING: {
				TextFieldMapping textFieldMapping = (TextFieldMapping)theEObject;
				T result = caseTextFieldMapping(textFieldMapping);
				if (result == null) result = caseFieldMapping(textFieldMapping);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.KEYWORD_FIELD_MAPPING: {
				KeywordFieldMapping keywordFieldMapping = (KeywordFieldMapping)theEObject;
				T result = caseKeywordFieldMapping(keywordFieldMapping);
				if (result == null) result = caseFieldMapping(keywordFieldMapping);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.NUMERIC_FIELD_MAPPING: {
				NumericFieldMapping numericFieldMapping = (NumericFieldMapping)theEObject;
				T result = caseNumericFieldMapping(numericFieldMapping);
				if (result == null) result = caseFieldMapping(numericFieldMapping);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.GEO_POINT_FIELD_MAPPING: {
				GeoPointFieldMapping geoPointFieldMapping = (GeoPointFieldMapping)theEObject;
				T result = caseGeoPointFieldMapping(geoPointFieldMapping);
				if (result == null) result = caseFieldMapping(geoPointFieldMapping);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.RANGE_FIELD_MAPPING: {
				RangeFieldMapping rangeFieldMapping = (RangeFieldMapping)theEObject;
				T result = caseRangeFieldMapping(rangeFieldMapping);
				if (result == null) result = caseFieldMapping(rangeFieldMapping);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.RANK_SIGNAL_FIELD_MAPPING: {
				RankSignalFieldMapping rankSignalFieldMapping = (RankSignalFieldMapping)theEObject;
				T result = caseRankSignalFieldMapping(rankSignalFieldMapping);
				if (result == null) result = caseFieldMapping(rankSignalFieldMapping);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.VECTOR_FIELD_MAPPING: {
				VectorFieldMapping vectorFieldMapping = (VectorFieldMapping)theEObject;
				T result = caseVectorFieldMapping(vectorFieldMapping);
				if (result == null) result = caseFieldMapping(vectorFieldMapping);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.REFERENCE_MAPPING: {
				ReferenceMapping referenceMapping = (ReferenceMapping)theEObject;
				T result = caseReferenceMapping(referenceMapping);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.MATERIALIZATION: {
				Materialization materialization = (Materialization)theEObject;
				T result = caseMaterialization(materialization);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.FACET_MAPPING: {
				FacetMapping facetMapping = (FacetMapping)theEObject;
				T result = caseFacetMapping(facetMapping);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.SUGGEST_SOURCE: {
				SuggestSource suggestSource = (SuggestSource)theEObject;
				T result = caseSuggestSource(suggestSource);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.REFRESH_POLICY: {
				RefreshPolicy refreshPolicy = (RefreshPolicy)theEObject;
				T result = caseRefreshPolicy(refreshPolicy);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.COMMIT_POLICY: {
				CommitPolicy commitPolicy = (CommitPolicy)theEObject;
				T result = caseCommitPolicy(commitPolicy);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.INDEX_SORT: {
				IndexSort indexSort = (IndexSort)theEObject;
				T result = caseIndexSort(indexSort);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.SORT_ENTRY: {
				SortEntry sortEntry = (SortEntry)theEObject;
				T result = caseSortEntry(sortEntry);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.ANALYZER_DEFINITION: {
				AnalyzerDefinition analyzerDefinition = (AnalyzerDefinition)theEObject;
				T result = caseAnalyzerDefinition(analyzerDefinition);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ESearchPackage.ANALYZER_PARAMETER: {
				AnalyzerParameter analyzerParameter = (AnalyzerParameter)theEObject;
				T result = caseAnalyzerParameter(analyzerParameter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Search Mapping Registry</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Search Mapping Registry</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSearchMappingRegistry(SearchMappingRegistry object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Index Unit Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Index Unit Mapping</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseIndexUnitMapping(IndexUnitMapping object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Document Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Document Mapping</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDocumentMapping(DocumentMapping object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Field Mapping</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFieldMapping(FieldMapping object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Text Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Text Field Mapping</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTextFieldMapping(TextFieldMapping object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Keyword Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Keyword Field Mapping</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseKeywordFieldMapping(KeywordFieldMapping object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Numeric Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Numeric Field Mapping</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseNumericFieldMapping(NumericFieldMapping object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Geo Point Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Geo Point Field Mapping</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGeoPointFieldMapping(GeoPointFieldMapping object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Range Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Range Field Mapping</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRangeFieldMapping(RangeFieldMapping object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Rank Signal Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Rank Signal Field Mapping</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRankSignalFieldMapping(RankSignalFieldMapping object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Vector Field Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Vector Field Mapping</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseVectorFieldMapping(VectorFieldMapping object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Reference Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Reference Mapping</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseReferenceMapping(ReferenceMapping object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Materialization</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Materialization</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMaterialization(Materialization object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Facet Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Facet Mapping</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFacetMapping(FacetMapping object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Suggest Source</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Suggest Source</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSuggestSource(SuggestSource object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Refresh Policy</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Refresh Policy</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRefreshPolicy(RefreshPolicy object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Commit Policy</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Commit Policy</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCommitPolicy(CommitPolicy object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Index Sort</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Index Sort</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseIndexSort(IndexSort object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Sort Entry</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Sort Entry</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSortEntry(SortEntry object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Analyzer Definition</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Analyzer Definition</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseAnalyzerDefinition(AnalyzerDefinition object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Analyzer Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Analyzer Parameter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseAnalyzerParameter(AnalyzerParameter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object) {
		return null;
	}

} //ESearchSwitch
