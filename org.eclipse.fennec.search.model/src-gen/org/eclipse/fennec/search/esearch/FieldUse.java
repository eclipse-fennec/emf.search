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
 * A representation of the literals of the enumeration '<em><b>Field Use</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * The purpose a field projection serves during query translation. Used to disambiguate between several projections of one attribute; when a mapping declares none, the field kind decides.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFieldUse()
 * @model
 * @generated
 */
@ProviderType
public enum FieldUse implements Enumerator {
	/**
	 * The '<em><b>MATCH</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Analyzed full-text matching and relevance scoring.
	 * <!-- end-model-doc -->
	 * @see #MATCH_VALUE
	 * @generated
	 * @ordered
	 */
	MATCH(0, "MATCH", "MATCH"),

	/**
	 * The '<em><b>EXACT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Equality, IN, and prefix/wildcard/regexp matching over the unanalyzed value.
	 * <!-- end-model-doc -->
	 * @see #EXACT_VALUE
	 * @generated
	 * @ordered
	 */
	EXACT(1, "EXACT", "EXACT"),

	/**
	 * The '<em><b>RANGE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Comparisons and ranges over a point-encoded value.
	 * <!-- end-model-doc -->
	 * @see #RANGE_VALUE
	 * @generated
	 * @ordered
	 */
	RANGE(2, "RANGE", "RANGE"),

	/**
	 * The '<em><b>SORT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Sorting. Requires doc values.
	 * <!-- end-model-doc -->
	 * @see #SORT_VALUE
	 * @generated
	 * @ordered
	 */
	SORT(3, "SORT", "SORT"),

	/**
	 * The '<em><b>FACET</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Facet counting. Requires a facet declaration.
	 * <!-- end-model-doc -->
	 * @see #FACET_VALUE
	 * @generated
	 * @ordered
	 */
	FACET(4, "FACET", "FACET"),

	/**
	 * The '<em><b>HIGHLIGHT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Passage extraction. Cheapest with term vectors, and only meaningful on an analyzed projection.
	 * <!-- end-model-doc -->
	 * @see #HIGHLIGHT_VALUE
	 * @generated
	 * @ordered
	 */
	HIGHLIGHT(5, "HIGHLIGHT", "HIGHLIGHT"),

	/**
	 * The '<em><b>SIMILARITY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Source of term statistics for MoreLikeThis.
	 * <!-- end-model-doc -->
	 * @see #SIMILARITY_VALUE
	 * @generated
	 * @ordered
	 */
	SIMILARITY(6, "SIMILARITY", "SIMILARITY");

	/**
	 * The '<em><b>MATCH</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Analyzed full-text matching and relevance scoring.
	 * <!-- end-model-doc -->
	 * @see #MATCH
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int MATCH_VALUE = 0;

	/**
	 * The '<em><b>EXACT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Equality, IN, and prefix/wildcard/regexp matching over the unanalyzed value.
	 * <!-- end-model-doc -->
	 * @see #EXACT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int EXACT_VALUE = 1;

	/**
	 * The '<em><b>RANGE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Comparisons and ranges over a point-encoded value.
	 * <!-- end-model-doc -->
	 * @see #RANGE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int RANGE_VALUE = 2;

	/**
	 * The '<em><b>SORT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Sorting. Requires doc values.
	 * <!-- end-model-doc -->
	 * @see #SORT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SORT_VALUE = 3;

	/**
	 * The '<em><b>FACET</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Facet counting. Requires a facet declaration.
	 * <!-- end-model-doc -->
	 * @see #FACET
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FACET_VALUE = 4;

	/**
	 * The '<em><b>HIGHLIGHT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Passage extraction. Cheapest with term vectors, and only meaningful on an analyzed projection.
	 * <!-- end-model-doc -->
	 * @see #HIGHLIGHT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int HIGHLIGHT_VALUE = 5;

	/**
	 * The '<em><b>SIMILARITY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Source of term statistics for MoreLikeThis.
	 * <!-- end-model-doc -->
	 * @see #SIMILARITY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SIMILARITY_VALUE = 6;

	/**
	 * An array of all the '<em><b>Field Use</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final FieldUse[] VALUES_ARRAY =
		new FieldUse[] {
			MATCH,
			EXACT,
			RANGE,
			SORT,
			FACET,
			HIGHLIGHT,
			SIMILARITY,
		};

	/**
	 * A public read-only list of all the '<em><b>Field Use</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<FieldUse> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Field Use</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static FieldUse get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			FieldUse result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Field Use</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static FieldUse getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			FieldUse result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Field Use</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static FieldUse get(int value) {
		switch (value) {
			case MATCH_VALUE: return MATCH;
			case EXACT_VALUE: return EXACT;
			case RANGE_VALUE: return RANGE;
			case SORT_VALUE: return SORT;
			case FACET_VALUE: return FACET;
			case HIGHLIGHT_VALUE: return HIGHLIGHT;
			case SIMILARITY_VALUE: return SIMILARITY;
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
	private FieldUse(int value, String name, String literal) {
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
	
} //FieldUse
