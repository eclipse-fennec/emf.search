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

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Ocl Source</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Rung three: an m2x OCL expression, stored as text and evaluated per document — the same shape the derivation annotation uses, so a modeller who knows one knows the other.
 * 
 * The expression is parsed and type-checked when the mapping is read, not when a document is written: a mapping carrying an invalid expression is refused at load, where a human is still watching. Changing the expression changes what is in the index, which makes it interpretation-relevant metadata — a changed expression means a rebuild.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.OclSource#getExpression <em>Expression</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.OclSource#getContext <em>Context</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getOclSource()
 * @model
 * @generated
 */
@ProviderType
public interface OclSource extends ValueSource {
	/**
	 * Returns the value of the '<em><b>Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The OCL expression text, for example self.firstName.concat(' ').concat(self.lastName).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Expression</em>' attribute.
	 * @see #setExpression(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getOclSource_Expression()
	 * @model required="true"
	 * @generated
	 */
	String getExpression();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.OclSource#getExpression <em>Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Expression</em>' attribute.
	 * @see #getExpression()
	 * @generated
	 */
	void setExpression(String value);

	/**
	 * Returns the value of the '<em><b>Context</b></em>' attribute.
	 * The default value is <code>"SELF"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.search.esearch.OclContextKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * What self is bound to when the expression runs.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Context</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.OclContextKind
	 * @see #setContext(OclContextKind)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getOclSource_Context()
	 * @model default="SELF"
	 * @generated
	 */
	OclContextKind getContext();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.OclSource#getContext <em>Context</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Context</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.OclContextKind
	 * @see #getContext()
	 * @generated
	 */
	void setContext(OclContextKind value);

} // OclSource
