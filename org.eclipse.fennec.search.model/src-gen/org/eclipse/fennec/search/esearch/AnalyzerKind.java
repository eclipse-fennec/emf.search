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
 * A representation of the literals of the enumeration '<em><b>Analyzer Kind</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Which analyzer to use: one of Lucene's built-ins, or CUSTOM to resolve one from the registry by name.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getAnalyzerKind()
 * @model
 * @generated
 */
@ProviderType
public enum AnalyzerKind implements Enumerator {
	/**
	 * The '<em><b>STANDARD</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Unicode text segmentation with lowercasing — the default.
	 * <!-- end-model-doc -->
	 * @see #STANDARD_VALUE
	 * @generated
	 * @ordered
	 */
	STANDARD(0, "STANDARD", "STANDARD"),

	/**
	 * The '<em><b>KEYWORD</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The whole value becomes one token.
	 * <!-- end-model-doc -->
	 * @see #KEYWORD_VALUE
	 * @generated
	 * @ordered
	 */
	KEYWORD(1, "KEYWORD", "KEYWORD"),

	/**
	 * The '<em><b>SIMPLE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Split at non-letters, lowercased.
	 * <!-- end-model-doc -->
	 * @see #SIMPLE_VALUE
	 * @generated
	 * @ordered
	 */
	SIMPLE(2, "SIMPLE", "SIMPLE"),

	/**
	 * The '<em><b>WHITESPACE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Split at whitespace only, no lowercasing.
	 * <!-- end-model-doc -->
	 * @see #WHITESPACE_VALUE
	 * @generated
	 * @ordered
	 */
	WHITESPACE(3, "WHITESPACE", "WHITESPACE"),

	/**
	 * The '<em><b>STOP</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Simple analysis plus stop-word removal; language selects the stop list.
	 * <!-- end-model-doc -->
	 * @see #STOP_VALUE
	 * @generated
	 * @ordered
	 */
	STOP(4, "STOP", "STOP"),

	/**
	 * The '<em><b>CUSTOM</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Resolved from the analyzer registry by name, with the declared parameters.
	 * <!-- end-model-doc -->
	 * @see #CUSTOM_VALUE
	 * @generated
	 * @ordered
	 */
	CUSTOM(5, "CUSTOM", "CUSTOM");

	/**
	 * The '<em><b>STANDARD</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Unicode text segmentation with lowercasing — the default.
	 * <!-- end-model-doc -->
	 * @see #STANDARD
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int STANDARD_VALUE = 0;

	/**
	 * The '<em><b>KEYWORD</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The whole value becomes one token.
	 * <!-- end-model-doc -->
	 * @see #KEYWORD
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int KEYWORD_VALUE = 1;

	/**
	 * The '<em><b>SIMPLE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Split at non-letters, lowercased.
	 * <!-- end-model-doc -->
	 * @see #SIMPLE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SIMPLE_VALUE = 2;

	/**
	 * The '<em><b>WHITESPACE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Split at whitespace only, no lowercasing.
	 * <!-- end-model-doc -->
	 * @see #WHITESPACE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int WHITESPACE_VALUE = 3;

	/**
	 * The '<em><b>STOP</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Simple analysis plus stop-word removal; language selects the stop list.
	 * <!-- end-model-doc -->
	 * @see #STOP
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int STOP_VALUE = 4;

	/**
	 * The '<em><b>CUSTOM</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Resolved from the analyzer registry by name, with the declared parameters.
	 * <!-- end-model-doc -->
	 * @see #CUSTOM
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CUSTOM_VALUE = 5;

	/**
	 * An array of all the '<em><b>Analyzer Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final AnalyzerKind[] VALUES_ARRAY =
		new AnalyzerKind[] {
			STANDARD,
			KEYWORD,
			SIMPLE,
			WHITESPACE,
			STOP,
			CUSTOM,
		};

	/**
	 * A public read-only list of all the '<em><b>Analyzer Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<AnalyzerKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Analyzer Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AnalyzerKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			AnalyzerKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Analyzer Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AnalyzerKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			AnalyzerKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Analyzer Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AnalyzerKind get(int value) {
		switch (value) {
			case STANDARD_VALUE: return STANDARD;
			case KEYWORD_VALUE: return KEYWORD;
			case SIMPLE_VALUE: return SIMPLE;
			case WHITESPACE_VALUE: return WHITESPACE;
			case STOP_VALUE: return STOP;
			case CUSTOM_VALUE: return CUSTOM;
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
	private AnalyzerKind(int value, String name, String literal) {
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
	
} //AnalyzerKind
