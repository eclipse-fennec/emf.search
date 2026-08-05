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
import org.eclipse.emf.ecore.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Index Unit Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Mapping for one index unit — one Lucene directory, one IndexWriter, one SearcherManager. Everything declared here is a property of the physical index and therefore cannot be varied per EClass: all documents in a unit share its refresh policy, commit policy and index sort.
 * 
 * v1 indexes a single EPackage universe per unit (docs/search-access.md §9, non-goals); cross-unit federation is out of scope. The runtime configuration of the unit (directory location, analyzer service wiring) is NOT part of this model — it is the unit's own configuration object, mapped from a Configuration Admin factory configuration in OSGi and constructed directly in plain Java. This model says what the documents look like, not where they live.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getEPackage <em>EPackage</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getTypeField <em>Type Field</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#isAutoMap <em>Auto Map</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getDefaultAnalyzer <em>Default Analyzer</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getRefresh <em>Refresh</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getCommit <em>Commit</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getSort <em>Sort</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getDocuments <em>Documents</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getIndexUnitMapping()
 * @model
 * @generated
 */
@ProviderType
public interface IndexUnitMapping extends EObject {
	/**
	 * Returns the value of the '<em><b>EPackage</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The EPackage universe this unit indexes.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>EPackage</em>' reference.
	 * @see #setEPackage(EPackage)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getIndexUnitMapping_EPackage()
	 * @model required="true"
	 * @generated
	 */
	EPackage getEPackage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getEPackage <em>EPackage</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>EPackage</em>' reference.
	 * @see #getEPackage()
	 * @generated
	 */
	void setEPackage(EPackage value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Unit name. The join between this mapping and the runtime unit configuration — the alias a consumer selects (the JPAUnit / mongo.database.alias pattern).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getIndexUnitMapping_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Type Field</b></em>' attribute.
	 * The default value is <code>"_type"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Name of the type discriminator field written by the mapper and used by TYPE_CHECK/TYPE_FILTER translation. Default follows the Fennec codec convention (ConfigProperty.TYPE_KEY).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Type Field</em>' attribute.
	 * @see #setTypeField(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getIndexUnitMapping_TypeField()
	 * @model default="_type"
	 * @generated
	 */
	String getTypeField();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getTypeField <em>Type Field</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type Field</em>' attribute.
	 * @see #getTypeField()
	 * @generated
	 */
	void setTypeField(String value);

	/**
	 * Returns the value of the '<em><b>Auto Map</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * When true (the default), EClasses of the package without an explicit DocumentMapping are indexed by convention. Set to false to index exactly the declared classes and nothing else.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Auto Map</em>' attribute.
	 * @see #setAutoMap(boolean)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getIndexUnitMapping_AutoMap()
	 * @model default="true"
	 * @generated
	 */
	boolean isAutoMap();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#isAutoMap <em>Auto Map</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Auto Map</em>' attribute.
	 * @see #isAutoMap()
	 * @generated
	 */
	void setAutoMap(boolean value);

	/**
	 * Returns the value of the '<em><b>Default Analyzer</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Analyzer for text fields that declare none. Unset means the backend default (standard analyzer).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Default Analyzer</em>' reference.
	 * @see #setDefaultAnalyzer(AnalyzerDefinition)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getIndexUnitMapping_DefaultAnalyzer()
	 * @model
	 * @generated
	 */
	AnalyzerDefinition getDefaultAnalyzer();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getDefaultAnalyzer <em>Default Analyzer</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Default Analyzer</em>' reference.
	 * @see #getDefaultAnalyzer()
	 * @generated
	 */
	void setDefaultAnalyzer(AnalyzerDefinition value);

	/**
	 * Returns the value of the '<em><b>Refresh</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * When writes become visible to searchers. Unset means near-real-time with the backend default interval.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Refresh</em>' containment reference.
	 * @see #setRefresh(RefreshPolicy)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getIndexUnitMapping_Refresh()
	 * @model containment="true"
	 * @generated
	 */
	RefreshPolicy getRefresh();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getRefresh <em>Refresh</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Refresh</em>' containment reference.
	 * @see #getRefresh()
	 * @generated
	 */
	void setRefresh(RefreshPolicy value);

	/**
	 * Returns the value of the '<em><b>Commit</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * When the writer commits to disk. Unset means commit on close only.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Commit</em>' containment reference.
	 * @see #setCommit(CommitPolicy)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getIndexUnitMapping_Commit()
	 * @model containment="true"
	 * @generated
	 */
	CommitPolicy getCommit();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getCommit <em>Commit</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Commit</em>' containment reference.
	 * @see #getCommit()
	 * @generated
	 */
	void setCommit(CommitPolicy value);

	/**
	 * Returns the value of the '<em><b>Sort</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optional index sort, enabling early termination for queries sorted the same way. Fixed at index creation: changing it later requires a full rebuild, which is why it is declared and not configured.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Sort</em>' containment reference.
	 * @see #setSort(IndexSort)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getIndexUnitMapping_Sort()
	 * @model containment="true"
	 * @generated
	 */
	IndexSort getSort();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.IndexUnitMapping#getSort <em>Sort</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sort</em>' containment reference.
	 * @see #getSort()
	 * @generated
	 */
	void setSort(IndexSort value);

	/**
	 * Returns the value of the '<em><b>Documents</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.search.esearch.DocumentMapping}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Per-EClass document mappings of this unit.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Documents</em>' containment reference list.
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getIndexUnitMapping_Documents()
	 * @model containment="true"
	 * @generated
	 */
	EList<DocumentMapping> getDocuments();

} // IndexUnitMapping
