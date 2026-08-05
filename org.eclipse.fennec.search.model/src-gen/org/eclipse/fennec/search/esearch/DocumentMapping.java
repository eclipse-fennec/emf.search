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
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Document Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * How instances of one EClass become Lucene documents. Everything here is per document; physical index properties live on the owning IndexUnitMapping.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.DocumentMapping#getEClass <em>EClass</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.DocumentMapping#getTypeName <em>Type Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.DocumentMapping#getIdFeature <em>Id Feature</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.DocumentMapping#isAutoMap <em>Auto Map</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.DocumentMapping#getAnalyzer <em>Analyzer</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.DocumentMapping#getMaterialization <em>Materialization</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.DocumentMapping#getFields <em>Fields</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.DocumentMapping#getReferences <em>References</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.DocumentMapping#getSuggestions <em>Suggestions</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getDocumentMapping()
 * @model
 * @generated
 */
@ProviderType
public interface DocumentMapping extends EObject {
	/**
	 * Returns the value of the '<em><b>EClass</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The EClass this mapping applies to. Subclasses inherit it unless they declare their own mapping.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>EClass</em>' reference.
	 * @see #setEClass(EClass)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getDocumentMapping_EClass()
	 * @model required="true"
	 * @generated
	 */
	EClass getEClass();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.DocumentMapping#getEClass <em>EClass</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>EClass</em>' reference.
	 * @see #getEClass()
	 * @generated
	 */
	void setEClass(EClass value);

	/**
	 * Returns the value of the '<em><b>Type Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Value written into the unit's type discriminator field. Unset defaults to the EClass name; set it explicitly when the class is renamed but existing indexes must keep matching.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Type Name</em>' attribute.
	 * @see #setTypeName(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getDocumentMapping_TypeName()
	 * @model
	 * @generated
	 */
	String getTypeName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.DocumentMapping#getTypeName <em>Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type Name</em>' attribute.
	 * @see #getTypeName()
	 * @generated
	 */
	void setTypeName(String value);

	/**
	 * Returns the value of the '<em><b>Id Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The attribute carrying the stable document id. Unset resolves to the EMF id attribute of the EClass. An id is mandatory for the PersistenceResource contract — a class without one is refused at mapping time, not silently indexed.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Id Feature</em>' reference.
	 * @see #setIdFeature(EAttribute)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getDocumentMapping_IdFeature()
	 * @model
	 * @generated
	 */
	EAttribute getIdFeature();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.DocumentMapping#getIdFeature <em>Id Feature</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id Feature</em>' reference.
	 * @see #getIdFeature()
	 * @generated
	 */
	void setIdFeature(EAttribute value);

	/**
	 * Returns the value of the '<em><b>Auto Map</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * When true (the default), attributes without an explicit FieldMapping are mapped by convention. Set to false to index exactly the declared fields.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Auto Map</em>' attribute.
	 * @see #setAutoMap(boolean)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getDocumentMapping_AutoMap()
	 * @model default="true"
	 * @generated
	 */
	boolean isAutoMap();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.DocumentMapping#isAutoMap <em>Auto Map</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Auto Map</em>' attribute.
	 * @see #isAutoMap()
	 * @generated
	 */
	void setAutoMap(boolean value);

	/**
	 * Returns the value of the '<em><b>Analyzer</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Overrides the unit's default analyzer for this document's text fields.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Analyzer</em>' reference.
	 * @see #setAnalyzer(AnalyzerDefinition)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getDocumentMapping_Analyzer()
	 * @model
	 * @generated
	 */
	AnalyzerDefinition getAnalyzer();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.DocumentMapping#getAnalyzer <em>Analyzer</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Analyzer</em>' reference.
	 * @see #getAnalyzer()
	 * @generated
	 */
	void setAnalyzer(AnalyzerDefinition value);

	/**
	 * Returns the value of the '<em><b>Materialization</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Whether a hit can be turned back into a full EObject from the index alone. Unset means no — hits carry only the mapped fields.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Materialization</em>' containment reference.
	 * @see #setMaterialization(Materialization)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getDocumentMapping_Materialization()
	 * @model containment="true"
	 * @generated
	 */
	Materialization getMaterialization();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.DocumentMapping#getMaterialization <em>Materialization</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Materialization</em>' containment reference.
	 * @see #getMaterialization()
	 * @generated
	 */
	void setMaterialization(Materialization value);

	/**
	 * Returns the value of the '<em><b>Fields</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.search.esearch.FieldMapping}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Explicit field mappings — one entry per attribute, carrying its primary projection. Further projections of the same attribute are sub-fields of that entry, not additional entries here. Attributes not listed follow the conventions when autoMap is true.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Fields</em>' containment reference list.
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getDocumentMapping_Fields()
	 * @model containment="true"
	 * @generated
	 */
	EList<FieldMapping> getFields();

	/**
	 * Returns the value of the '<em><b>References</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.search.esearch.ReferenceMapping}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reference strategies. References not listed here default to ID_ONLY — embedding and nesting change document shape and cost, so they are always an explicit decision.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>References</em>' containment reference list.
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getDocumentMapping_References()
	 * @model containment="true"
	 * @generated
	 */
	EList<ReferenceMapping> getReferences();

	/**
	 * Returns the value of the '<em><b>Suggestions</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.search.esearch.SuggestSource}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Suggestion sources built from this document. Suggest has its own service API — it deliberately does not go through the query IR — but it shares this mapping model and the unit's index lifecycle.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Suggestions</em>' containment reference list.
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getDocumentMapping_Suggestions()
	 * @model containment="true"
	 * @generated
	 */
	EList<SuggestSource> getSuggestions();

} // DocumentMapping
