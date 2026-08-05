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
 * A representation of the model object '<em><b>Rank Signal Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A static, data-driven relevance signal — popularity, recency, a curated weight — indexed as a feature field and folded into the score by a saturating function.
 * 
 * This exists so that scoring stays declarative: a consumer selects a declared signal by name, it never sends a scoring formula. That is what keeps the refusal of arithmetic pushdown meaningful. The signal value must be strictly positive.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.RankSignalFieldMapping#getFunction <em>Function</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.RankSignalFieldMapping#getPivot <em>Pivot</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.RankSignalFieldMapping#getExponent <em>Exponent</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getRankSignalFieldMapping()
 * @model
 * @generated
 */
@ProviderType
public interface RankSignalFieldMapping extends FieldMapping {
	/**
	 * Returns the value of the '<em><b>Function</b></em>' attribute.
	 * The default value is <code>"SATURATION"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.search.esearch.RankFunction}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The saturating function applied to the signal.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Function</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.RankFunction
	 * @see #setFunction(RankFunction)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getRankSignalFieldMapping_Function()
	 * @model default="SATURATION" required="true"
	 * @generated
	 */
	RankFunction getFunction();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.RankSignalFieldMapping#getFunction <em>Function</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Function</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.RankFunction
	 * @see #getFunction()
	 * @generated
	 */
	void setFunction(RankFunction value);

	/**
	 * Returns the value of the '<em><b>Pivot</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Value at which the function reaches half its maximum contribution. A sensible starting point is the median of the signal over the corpus; unset lets the backend derive it.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Pivot</em>' attribute.
	 * @see #setPivot(double)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getRankSignalFieldMapping_Pivot()
	 * @model
	 * @generated
	 */
	double getPivot();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.RankSignalFieldMapping#getPivot <em>Pivot</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pivot</em>' attribute.
	 * @see #getPivot()
	 * @generated
	 */
	void setPivot(double value);

	/**
	 * Returns the value of the '<em><b>Exponent</b></em>' attribute.
	 * The default value is <code>"1.0"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * SIGMOID only: steepness of the curve.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Exponent</em>' attribute.
	 * @see #setExponent(double)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getRankSignalFieldMapping_Exponent()
	 * @model default="1.0"
	 * @generated
	 */
	double getExponent();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.RankSignalFieldMapping#getExponent <em>Exponent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Exponent</em>' attribute.
	 * @see #getExponent()
	 * @generated
	 */
	void setExponent(double value);

} // RankSignalFieldMapping
