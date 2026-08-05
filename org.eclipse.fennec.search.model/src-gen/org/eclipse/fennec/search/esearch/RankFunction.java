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
 * A representation of the literals of the enumeration '<em><b>Rank Function</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * How a rank signal is folded into the score. All three saturate, so a large signal cannot dominate textual relevance.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getRankFunction()
 * @model
 * @generated
 */
@ProviderType
public enum RankFunction implements Enumerator {
	/**
	 * The '<em><b>SATURATION</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * value / (value + pivot) — the general-purpose choice. The contribution approaches but never reaches its maximum, so a large signal cannot outweigh textual relevance.
	 * <!-- end-model-doc -->
	 * @see #SATURATION_VALUE
	 * @generated
	 * @ordered
	 */
	SATURATION(0, "SATURATION", "SATURATION"),

	/**
	 * The '<em><b>LOG</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Logarithmic — for signals spanning orders of magnitude, such as view counts.
	 * <!-- end-model-doc -->
	 * @see #LOG_VALUE
	 * @generated
	 * @ordered
	 */
	LOG(1, "LOG", "LOG"),

	/**
	 * The '<em><b>SIGMOID</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Sigmoid with the declared pivot and exponent — a sharper transition around the pivot than SATURATION.
	 * <!-- end-model-doc -->
	 * @see #SIGMOID_VALUE
	 * @generated
	 * @ordered
	 */
	SIGMOID(2, "SIGMOID", "SIGMOID");

	/**
	 * The '<em><b>SATURATION</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * value / (value + pivot) — the general-purpose choice. The contribution approaches but never reaches its maximum, so a large signal cannot outweigh textual relevance.
	 * <!-- end-model-doc -->
	 * @see #SATURATION
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SATURATION_VALUE = 0;

	/**
	 * The '<em><b>LOG</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Logarithmic — for signals spanning orders of magnitude, such as view counts.
	 * <!-- end-model-doc -->
	 * @see #LOG
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int LOG_VALUE = 1;

	/**
	 * The '<em><b>SIGMOID</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Sigmoid with the declared pivot and exponent — a sharper transition around the pivot than SATURATION.
	 * <!-- end-model-doc -->
	 * @see #SIGMOID
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SIGMOID_VALUE = 2;

	/**
	 * An array of all the '<em><b>Rank Function</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final RankFunction[] VALUES_ARRAY =
		new RankFunction[] {
			SATURATION,
			LOG,
			SIGMOID,
		};

	/**
	 * A public read-only list of all the '<em><b>Rank Function</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<RankFunction> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Rank Function</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static RankFunction get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			RankFunction result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Rank Function</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static RankFunction getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			RankFunction result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Rank Function</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static RankFunction get(int value) {
		switch (value) {
			case SATURATION_VALUE: return SATURATION;
			case LOG_VALUE: return LOG;
			case SIGMOID_VALUE: return SIGMOID;
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
	private RankFunction(int value, String name, String literal) {
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
	
} //RankFunction
