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
import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Common part of every field mapping. The subclass chooses the Lucene field type; tokenization is therefore not a flag but the difference between TextFieldMapping and KeywordFieldMapping.
 * 
 * Scalar mappings map exactly one attribute (feature). Composite mappings — GeoPointFieldMapping with separate latitude/longitude attributes, RangeFieldMapping, VectorFieldMapping — declare their sources instead, which is why feature is optional here. Every concrete mapping must resolve to at least one source attribute; the processor refuses one that does not.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.FieldMapping#getFeature <em>Feature</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.FieldMapping#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.FieldMapping#isIndexed <em>Indexed</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.FieldMapping#isStored <em>Stored</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.FieldMapping#isDocValues <em>Doc Values</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.FieldMapping#getBoost <em>Boost</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.FieldMapping#getFacet <em>Facet</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.FieldMapping#getUse <em>Use</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.FieldMapping#getSources <em>Sources</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.FieldMapping#getSeparator <em>Separator</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.FieldMapping#getSubFields <em>Sub Fields</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFieldMapping()
 * @model abstract="true"
 * @generated
 */
@ProviderType
public interface FieldMapping extends EObject {
	/**
	 * Returns the value of the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The mapped attribute. Required for scalar field kinds; composite kinds declare their own sources, and a sub-field inherits it from its parent and must leave it unset.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Feature</em>' reference.
	 * @see #setFeature(EAttribute)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFieldMapping_Feature()
	 * @model
	 * @generated
	 */
	EAttribute getFeature();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.FieldMapping#getFeature <em>Feature</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Feature</em>' reference.
	 * @see #getFeature()
	 * @generated
	 */
	void setFeature(EAttribute value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Index field name. On a primary projection it defaults to the feature name; set it when the model is renamed but the index must stay queryable. On a sub-field it is relative and mandatory: the effective field name is parent.child.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFieldMapping_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.FieldMapping#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Indexed</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Whether the field is searchable. False makes it stored-only or doc-values-only — the field can be returned or sorted on but not matched.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Indexed</em>' attribute.
	 * @see #setIndexed(boolean)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFieldMapping_Indexed()
	 * @model default="true"
	 * @generated
	 */
	boolean isIndexed();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.FieldMapping#isIndexed <em>Indexed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Indexed</em>' attribute.
	 * @see #isIndexed()
	 * @generated
	 */
	void setIndexed(boolean value);

	/**
	 * Returns the value of the '<em><b>Stored</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Whether the original value is stored for retrieval. True by default: the partial reconstruction of hits (docs/search-access.md §4.3) reads exactly these stored values, so opting out is a per-field declaration for values that are large or come back another way (STORED_OBJECT, SOURCE_URI).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Stored</em>' attribute.
	 * @see #setStored(boolean)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFieldMapping_Stored()
	 * @model default="true"
	 * @generated
	 */
	boolean isStored();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.FieldMapping#isStored <em>Stored</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stored</em>' attribute.
	 * @see #isStored()
	 * @generated
	 */
	void setStored(boolean value);

	/**
	 * Returns the value of the '<em><b>Doc Values</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Whether a doc-values column is written. Required for sorting, faceting and for the index sort; the conventions enable it for numerics.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Doc Values</em>' attribute.
	 * @see #setDocValues(boolean)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFieldMapping_DocValues()
	 * @model default="false"
	 * @generated
	 */
	boolean isDocValues();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.FieldMapping#isDocValues <em>Doc Values</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Doc Values</em>' attribute.
	 * @see #isDocValues()
	 * @generated
	 */
	void setDocValues(boolean value);

	/**
	 * Returns the value of the '<em><b>Boost</b></em>' attribute.
	 * The default value is <code>"1.0"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Query-time boost applied to clauses over this field. A value other than 1.0 is a curated relevance decision and belongs in the mapping, not in the query — see RankSignalFieldMapping for data-driven signals.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Boost</em>' attribute.
	 * @see #setBoost(float)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFieldMapping_Boost()
	 * @model default="1.0"
	 * @generated
	 */
	float getBoost();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.FieldMapping#getBoost <em>Boost</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Boost</em>' attribute.
	 * @see #getBoost()
	 * @generated
	 */
	void setBoost(float value);

	/**
	 * Returns the value of the '<em><b>Facet</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Makes this field available as a facet dimension. Only the shapes facets can actually answer are declared as capabilities: single group key, count aggregate.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Facet</em>' containment reference.
	 * @see #setFacet(FacetMapping)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFieldMapping_Facet()
	 * @model containment="true"
	 * @generated
	 */
	FacetMapping getFacet();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.FieldMapping#getFacet <em>Facet</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Facet</em>' containment reference.
	 * @see #getFacet()
	 * @generated
	 */
	void setFacet(FacetMapping value);

	/**
	 * Returns the value of the '<em><b>Use</b></em>' attribute list.
	 * The list contents are of type {@link org.eclipse.fennec.search.esearch.FieldUse}.
	 * The literals are from the enumeration {@link org.eclipse.fennec.search.esearch.FieldUse}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * What this projection is for. Empty — the normal case — derives it from the field kind: a text field serves MATCH and HIGHLIGHT, a keyword field EXACT, SORT and FACET, a numeric field RANGE and SORT.
	 * 
	 * Declare it explicitly when derivation would be ambiguous, which is precisely when one attribute has several projections that could serve the same purpose: two keyword projections with different normalizers, or two analyzed projections in different languages. The translator picks the projection whose use covers the predicate; two projections claiming the same use for one attribute are refused at mapping time rather than resolved by declaration order.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Use</em>' attribute list.
	 * @see org.eclipse.fennec.search.esearch.FieldUse
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFieldMapping_Use()
	 * @model
	 * @generated
	 */
	EList<FieldUse> getUse();

	/**
	 * Returns the value of the '<em><b>Sources</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.search.esearch.ValueSource}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Where the value written into this field comes from, when it is not simply one attribute of the mapped object (S20, docs/search-access.md §4.2). The rungs of the extraction ladder — FeatureSource, PathSource, OclSource — in ascending order of what has to run before the value is known; use the weakest one that suffices, because the lower ones can be checked against the metamodel without evaluating anything.
	 * 
	 * A field declaring sources must not also declare a feature: the two say the same thing in two places, and a mapping that says it twice can say it differently. A field with sources but no feature is a virtual field — it exists in the index and in no EClass, which means no canonical query can name it (the query IR addresses features). Virtual fields are for facets, suggest, highlighting and full-text matching; when a computed value has to be queryable, the carrier is a derived EStructuralFeature with the m2x derivation annotation, which the mapper then treats as an ordinary feature.
	 * 
	 * Several sources feed one field: each contributes its value, so the field is multi-valued unless separator joins them into one.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Sources</em>' containment reference list.
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFieldMapping_Sources()
	 * @model containment="true"
	 * @generated
	 */
	EList<ValueSource> getSources();

	/**
	 * Returns the value of the '<em><b>Separator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Joins the values of several sources into one value with this separator, instead of writing one value per source. Set it for a text field that concatenates title and subtitle into one searchable string; leave it unset where several values are what you mean, as for a keyword field.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Separator</em>' attribute.
	 * @see #setSeparator(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFieldMapping_Separator()
	 * @model
	 * @generated
	 */
	String getSeparator();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.FieldMapping#getSeparator <em>Separator</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Separator</em>' attribute.
	 * @see #getSeparator()
	 * @generated
	 */
	void setSeparator(String value);

	/**
	 * Returns the value of the '<em><b>Sub Fields</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.search.esearch.FieldMapping}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Additional projections of the same attribute into differently typed index fields — the common case being an analyzed text field for relevance matching plus an unanalyzed keyword projection for equality, sorting and faceting, but equally a second analyzer for another language.
	 * 
	 * The enclosing mapping is the primary projection; that is structural, so there is no primary flag to get wrong. A sub-field inherits the parent's attribute and must not declare one of its own, its name is relative, and the effective index field name is parent.child (title.keyword). Only one level of nesting is meaningful: a sub-field of a sub-field is refused.
	 * 
	 * Sub-fields multiply index size, so they are always declared, never derived — the conventions give one projection per attribute.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Sub Fields</em>' containment reference list.
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFieldMapping_SubFields()
	 * @model containment="true"
	 * @generated
	 */
	EList<FieldMapping> getSubFields();

} // FieldMapping
