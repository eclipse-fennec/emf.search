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
 * A representation of the literals of the enumeration '<em><b>Facet Kind</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Facet implementation. The choice has a unit-level consequence, so it is not purely a field concern.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getFacetKind()
 * @model
 * @generated
 */
@ProviderType
public enum FacetKind implements Enumerator {
	/**
	 * The '<em><b>SORTED SET</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Sorted-set doc values. Needs no side index and therefore nothing extra to keep in sync — the default.
	 * <!-- end-model-doc -->
	 * @see #SORTED_SET_VALUE
	 * @generated
	 * @ordered
	 */
	SORTED_SET(0, "SORTED_SET", "SORTED_SET"),

	/**
	 * The '<em><b>TAXONOMY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Taxonomy index. Supports hierarchical dimensions, at the price of a second directory with its own lifecycle and commit.
	 * <!-- end-model-doc -->
	 * @see #TAXONOMY_VALUE
	 * @generated
	 * @ordered
	 */
	TAXONOMY(1, "TAXONOMY", "TAXONOMY");

	/**
	 * The '<em><b>SORTED SET</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Sorted-set doc values. Needs no side index and therefore nothing extra to keep in sync — the default.
	 * <!-- end-model-doc -->
	 * @see #SORTED_SET
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SORTED_SET_VALUE = 0;

	/**
	 * The '<em><b>TAXONOMY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Taxonomy index. Supports hierarchical dimensions, at the price of a second directory with its own lifecycle and commit.
	 * <!-- end-model-doc -->
	 * @see #TAXONOMY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int TAXONOMY_VALUE = 1;

	/**
	 * An array of all the '<em><b>Facet Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final FacetKind[] VALUES_ARRAY =
		new FacetKind[] {
			SORTED_SET,
			TAXONOMY,
		};

	/**
	 * A public read-only list of all the '<em><b>Facet Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<FacetKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Facet Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static FacetKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			FacetKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Facet Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static FacetKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			FacetKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Facet Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static FacetKind get(int value) {
		switch (value) {
			case SORTED_SET_VALUE: return SORTED_SET;
			case TAXONOMY_VALUE: return TAXONOMY;
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
	private FacetKind(int value, String name, String literal) {
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
	
} //FacetKind
