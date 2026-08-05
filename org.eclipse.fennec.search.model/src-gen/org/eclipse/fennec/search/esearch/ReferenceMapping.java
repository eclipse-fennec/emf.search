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
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Reference Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * How a reference crosses the gap between a graph-shaped model and a flat Lucene document. The choice is a genuine trade-off, so there is no useful default beyond the honest one (ID_ONLY).
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getEReference <em>EReference</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getStrategy <em>Strategy</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getPrefix <em>Prefix</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getDepth <em>Depth</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getTarget <em>Target</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getIncludes <em>Includes</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getReferenceMapping()
 * @model
 * @generated
 */
@ProviderType
public interface ReferenceMapping extends EObject {
	/**
	 * Returns the value of the '<em><b>EReference</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The reference this mapping applies to.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>EReference</em>' reference.
	 * @see #setEReference(EReference)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getReferenceMapping_EReference()
	 * @model required="true"
	 * @generated
	 */
	EReference getEReference();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getEReference <em>EReference</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>EReference</em>' reference.
	 * @see #getEReference()
	 * @generated
	 */
	void setEReference(EReference value);

	/**
	 * Returns the value of the '<em><b>Strategy</b></em>' attribute.
	 * The default value is <code>"ID_ONLY"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.search.esearch.ReferenceStrategy}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Indexing strategy for the target.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Strategy</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.ReferenceStrategy
	 * @see #setStrategy(ReferenceStrategy)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getReferenceMapping_Strategy()
	 * @model default="ID_ONLY" required="true"
	 * @generated
	 */
	ReferenceStrategy getStrategy();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getStrategy <em>Strategy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Strategy</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.ReferenceStrategy
	 * @see #getStrategy()
	 * @generated
	 */
	void setStrategy(ReferenceStrategy value);

	/**
	 * Returns the value of the '<em><b>Prefix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * EMBED only: field-name prefix for the denormalized target fields. Unset defaults to the reference name.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Prefix</em>' attribute.
	 * @see #setPrefix(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getReferenceMapping_Prefix()
	 * @model
	 * @generated
	 */
	String getPrefix();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getPrefix <em>Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Prefix</em>' attribute.
	 * @see #getPrefix()
	 * @generated
	 */
	void setPrefix(String value);

	/**
	 * Returns the value of the '<em><b>Depth</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * EMBED and NESTED: how many reference hops to follow. Bounded because a model graph may be cyclic and a document may not.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Depth</em>' attribute.
	 * @see #setDepth(int)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getReferenceMapping_Depth()
	 * @model default="1"
	 * @generated
	 */
	int getDepth();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getDepth <em>Depth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Depth</em>' attribute.
	 * @see #getDepth()
	 * @generated
	 */
	void setDepth(int value);

	/**
	 * Returns the value of the '<em><b>Target</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The document mapping applied to the target. Unset uses the mapping declared for the target EClass in the same unit, or the conventions.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Target</em>' reference.
	 * @see #setTarget(DocumentMapping)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getReferenceMapping_Target()
	 * @model
	 * @generated
	 */
	DocumentMapping getTarget();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.ReferenceMapping#getTarget <em>Target</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Target</em>' reference.
	 * @see #getTarget()
	 * @generated
	 */
	void setTarget(DocumentMapping value);

	/**
	 * Returns the value of the '<em><b>Includes</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EStructuralFeature}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Narrows what is taken from the target. Empty means everything the target mapping produces.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Includes</em>' reference list.
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getReferenceMapping_Includes()
	 * @model
	 * @generated
	 */
	EList<EStructuralFeature> getIncludes();

} // ReferenceMapping
