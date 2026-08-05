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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Facet Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Declares a field as a facet dimension.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.FacetMapping#getDimension <em>Dimension</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.FacetMapping#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.FacetMapping#isHierarchical <em>Hierarchical</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.FacetMapping#isMultiValued <em>Multi Valued</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFacetMapping()
 * @model
 * @generated
 */
@ProviderType
public interface FacetMapping extends EObject {
	/**
	 * Returns the value of the '<em><b>Dimension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Facet dimension name. Unset defaults to the field name.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Dimension</em>' attribute.
	 * @see #setDimension(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFacetMapping_Dimension()
	 * @model
	 * @generated
	 */
	String getDimension();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.FacetMapping#getDimension <em>Dimension</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dimension</em>' attribute.
	 * @see #getDimension()
	 * @generated
	 */
	void setDimension(String value);

	/**
	 * Returns the value of the '<em><b>Kind</b></em>' attribute.
	 * The default value is <code>"SORTED_SET"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.search.esearch.FacetKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * SORTED_SET needs no side index and is the default. TAXONOMY supports hierarchical dimensions but requires a second directory with its own lifecycle — a unit-level cost paid for a field-level feature.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.FacetKind
	 * @see #setKind(FacetKind)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFacetMapping_Kind()
	 * @model default="SORTED_SET"
	 * @generated
	 */
	FacetKind getKind();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.FacetMapping#getKind <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.FacetKind
	 * @see #getKind()
	 * @generated
	 */
	void setKind(FacetKind value);

	/**
	 * Returns the value of the '<em><b>Hierarchical</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * TAXONOMY only: values are paths, so counts roll up along the hierarchy.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Hierarchical</em>' attribute.
	 * @see #setHierarchical(boolean)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFacetMapping_Hierarchical()
	 * @model default="false"
	 * @generated
	 */
	boolean isHierarchical();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.FacetMapping#isHierarchical <em>Hierarchical</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Hierarchical</em>' attribute.
	 * @see #isHierarchical()
	 * @generated
	 */
	void setHierarchical(boolean value);

	/**
	 * Returns the value of the '<em><b>Multi Valued</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A document contributes more than one value to this dimension.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Multi Valued</em>' attribute.
	 * @see #setMultiValued(boolean)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFacetMapping_MultiValued()
	 * @model default="false"
	 * @generated
	 */
	boolean isMultiValued();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.FacetMapping#isMultiValued <em>Multi Valued</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Multi Valued</em>' attribute.
	 * @see #isMultiValued()
	 * @generated
	 */
	void setMultiValued(boolean value);

} // FacetMapping
