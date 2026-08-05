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

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Range Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * An interval indexed as one range field, so that INTERSECTS / WITHIN / CONTAINS are single predicates instead of two hand-written comparisons. The typical case is a validity interval on a modelled entity.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.RangeFieldMapping#getLowerBound <em>Lower Bound</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.RangeFieldMapping#getUpperBound <em>Upper Bound</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.RangeFieldMapping#getKind <em>Kind</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getRangeFieldMapping()
 * @model
 * @generated
 */
@ProviderType
public interface RangeFieldMapping extends FieldMapping {
	/**
	 * Returns the value of the '<em><b>Lower Bound</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Attribute carrying the lower bound of the interval.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Lower Bound</em>' reference.
	 * @see #setLowerBound(EAttribute)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getRangeFieldMapping_LowerBound()
	 * @model required="true"
	 * @generated
	 */
	EAttribute getLowerBound();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.RangeFieldMapping#getLowerBound <em>Lower Bound</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Lower Bound</em>' reference.
	 * @see #getLowerBound()
	 * @generated
	 */
	void setLowerBound(EAttribute value);

	/**
	 * Returns the value of the '<em><b>Upper Bound</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Attribute carrying the upper bound of the interval.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Upper Bound</em>' reference.
	 * @see #setUpperBound(EAttribute)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getRangeFieldMapping_UpperBound()
	 * @model required="true"
	 * @generated
	 */
	EAttribute getUpperBound();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.RangeFieldMapping#getUpperBound <em>Upper Bound</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Upper Bound</em>' reference.
	 * @see #getUpperBound()
	 * @generated
	 */
	void setUpperBound(EAttribute value);

	/**
	 * Returns the value of the '<em><b>Kind</b></em>' attribute.
	 * The default value is <code>"AUTO"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.search.esearch.RangeKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Range encoding. AUTO derives it from the bound attributes' EDataType.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.RangeKind
	 * @see #setKind(RangeKind)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getRangeFieldMapping_Kind()
	 * @model default="AUTO"
	 * @generated
	 */
	RangeKind getKind();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.RangeFieldMapping#getKind <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.RangeKind
	 * @see #getKind()
	 * @generated
	 */
	void setKind(RangeKind value);

} // RangeFieldMapping
