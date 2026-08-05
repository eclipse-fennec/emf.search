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
 * A representation of the literals of the enumeration '<em><b>Suggester Kind</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Suggester implementation backing a suggestion source.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getSuggesterKind()
 * @model
 * @generated
 */
@ProviderType
public enum SuggesterKind implements Enumerator {
	/**
	 * The '<em><b>ANALYZING</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Analyzed prefix completion: suggestions match what the analyzer produced, so case and token differences are tolerated.
	 * <!-- end-model-doc -->
	 * @see #ANALYZING_VALUE
	 * @generated
	 * @ordered
	 */
	ANALYZING(0, "ANALYZING", "ANALYZING"),

	/**
	 * The '<em><b>FUZZY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Analyzing completion with edit-distance tolerance, for typo-tolerant input.
	 * <!-- end-model-doc -->
	 * @see #FUZZY_VALUE
	 * @generated
	 * @ordered
	 */
	FUZZY(1, "FUZZY", "FUZZY"),

	/**
	 * The '<em><b>COMPLETION</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Prefix completion over a weighted FST — the fastest option, and the one that supports filter contexts.
	 * <!-- end-model-doc -->
	 * @see #COMPLETION_VALUE
	 * @generated
	 * @ordered
	 */
	COMPLETION(2, "COMPLETION", "COMPLETION"),

	/**
	 * The '<em><b>FREE TEXT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * N-gram based next-word prediction rather than completion of a known entry.
	 * <!-- end-model-doc -->
	 * @see #FREE_TEXT_VALUE
	 * @generated
	 * @ordered
	 */
	FREE_TEXT(3, "FREE_TEXT", "FREE_TEXT");

	/**
	 * The '<em><b>ANALYZING</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Analyzed prefix completion: suggestions match what the analyzer produced, so case and token differences are tolerated.
	 * <!-- end-model-doc -->
	 * @see #ANALYZING
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ANALYZING_VALUE = 0;

	/**
	 * The '<em><b>FUZZY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Analyzing completion with edit-distance tolerance, for typo-tolerant input.
	 * <!-- end-model-doc -->
	 * @see #FUZZY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FUZZY_VALUE = 1;

	/**
	 * The '<em><b>COMPLETION</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Prefix completion over a weighted FST — the fastest option, and the one that supports filter contexts.
	 * <!-- end-model-doc -->
	 * @see #COMPLETION
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int COMPLETION_VALUE = 2;

	/**
	 * The '<em><b>FREE TEXT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * N-gram based next-word prediction rather than completion of a known entry.
	 * <!-- end-model-doc -->
	 * @see #FREE_TEXT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FREE_TEXT_VALUE = 3;

	/**
	 * An array of all the '<em><b>Suggester Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final SuggesterKind[] VALUES_ARRAY =
		new SuggesterKind[] {
			ANALYZING,
			FUZZY,
			COMPLETION,
			FREE_TEXT,
		};

	/**
	 * A public read-only list of all the '<em><b>Suggester Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<SuggesterKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Suggester Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static SuggesterKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			SuggesterKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Suggester Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static SuggesterKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			SuggesterKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Suggester Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static SuggesterKind get(int value) {
		switch (value) {
			case ANALYZING_VALUE: return ANALYZING;
			case FUZZY_VALUE: return FUZZY;
			case COMPLETION_VALUE: return COMPLETION;
			case FREE_TEXT_VALUE: return FREE_TEXT;
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
	private SuggesterKind(int value, String name, String literal) {
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
	
} //SuggesterKind
