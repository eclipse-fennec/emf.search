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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Vector Similarity</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Similarity function for k-nearest-neighbour search. Reserved for wave 2 together with VectorFieldMapping.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getVectorSimilarity()
 * @model
 * @generated
 */
@ProviderType
public enum VectorSimilarity implements Enumerator {
	/**
	 * The '<em><b>COSINE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Angle between the vectors; the usual choice for text embeddings.
	 * <!-- end-model-doc -->
	 * @see #COSINE_VALUE
	 * @generated
	 * @ordered
	 */
	COSINE(0, "COSINE", "COSINE"),

	/**
	 * The '<em><b>DOT PRODUCT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Inner product. Only meaningful for unit-length vectors.
	 * <!-- end-model-doc -->
	 * @see #DOT_PRODUCT_VALUE
	 * @generated
	 * @ordered
	 */
	DOT_PRODUCT(1, "DOT_PRODUCT", "DOT_PRODUCT"),

	/**
	 * The '<em><b>EUCLIDEAN</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * L2 distance.
	 * <!-- end-model-doc -->
	 * @see #EUCLIDEAN_VALUE
	 * @generated
	 * @ordered
	 */
	EUCLIDEAN(2, "EUCLIDEAN", "EUCLIDEAN"),

	/**
	 * The '<em><b>MAXIMUM INNER PRODUCT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Inner product without the unit-length requirement.
	 * <!-- end-model-doc -->
	 * @see #MAXIMUM_INNER_PRODUCT_VALUE
	 * @generated
	 * @ordered
	 */
	MAXIMUM_INNER_PRODUCT(3, "MAXIMUM_INNER_PRODUCT", "MAXIMUM_INNER_PRODUCT");

	/**
	 * The '<em><b>COSINE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Angle between the vectors; the usual choice for text embeddings.
	 * <!-- end-model-doc -->
	 * @see #COSINE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int COSINE_VALUE = 0;

	/**
	 * The '<em><b>DOT PRODUCT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Inner product. Only meaningful for unit-length vectors.
	 * <!-- end-model-doc -->
	 * @see #DOT_PRODUCT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DOT_PRODUCT_VALUE = 1;

	/**
	 * The '<em><b>EUCLIDEAN</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * L2 distance.
	 * <!-- end-model-doc -->
	 * @see #EUCLIDEAN
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int EUCLIDEAN_VALUE = 2;

	/**
	 * The '<em><b>MAXIMUM INNER PRODUCT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Inner product without the unit-length requirement.
	 * <!-- end-model-doc -->
	 * @see #MAXIMUM_INNER_PRODUCT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int MAXIMUM_INNER_PRODUCT_VALUE = 3;

	/**
	 * An array of all the '<em><b>Vector Similarity</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final VectorSimilarity[] VALUES_ARRAY =
		new VectorSimilarity[] {
			COSINE,
			DOT_PRODUCT,
			EUCLIDEAN,
			MAXIMUM_INNER_PRODUCT,
		};

	/**
	 * A public read-only list of all the '<em><b>Vector Similarity</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<VectorSimilarity> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Vector Similarity</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static VectorSimilarity get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			VectorSimilarity result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Vector Similarity</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static VectorSimilarity getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			VectorSimilarity result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Vector Similarity</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static VectorSimilarity get(int value) {
		switch (value) {
			case COSINE_VALUE: return COSINE;
			case DOT_PRODUCT_VALUE: return DOT_PRODUCT;
			case EUCLIDEAN_VALUE: return EUCLIDEAN;
			case MAXIMUM_INNER_PRODUCT_VALUE: return MAXIMUM_INNER_PRODUCT;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private VectorSimilarity(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getValue() {
	  return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
	  return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLiteral() {
	  return literal;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string representation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}
	
} //VectorSimilarity
