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
 * A representation of the literals of the enumeration '<em><b>Materialization Kind</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * How a hit is upgraded beyond the partial reconstruction every document gets by default.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getMaterializationKind()
 * @model
 * @generated
 */
@ProviderType
public enum MaterializationKind implements Enumerator {
	/**
	 * The '<em><b>STORED OBJECT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The whole EObject tree is serialized into one binary stored field and comes back complete, without consulting a primary store. This is what makes the standalone role self-sufficient, and the only kind that gates UPDATE_BY_SELECTOR per EClass.
	 * <!-- end-model-doc -->
	 * @see #STORED_OBJECT_VALUE
	 * @generated
	 * @ordered
	 */
	STORED_OBJECT(0, "STORED_OBJECT", "STORED_OBJECT"),

	/**
	 * The '<em><b>SOURCE URI</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object's original URI is stored and a hit materializes by resolving it through the caller's ResourceSet against the primary store (JPA, Mongo, file). The secondary-index role made explicit: the index finds, the primary store materializes.
	 * <!-- end-model-doc -->
	 * @see #SOURCE_URI_VALUE
	 * @generated
	 * @ordered
	 */
	SOURCE_URI(1, "SOURCE_URI", "SOURCE_URI");

	/**
	 * The '<em><b>STORED OBJECT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The whole EObject tree is serialized into one binary stored field and comes back complete, without consulting a primary store. This is what makes the standalone role self-sufficient, and the only kind that gates UPDATE_BY_SELECTOR per EClass.
	 * <!-- end-model-doc -->
	 * @see #STORED_OBJECT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int STORED_OBJECT_VALUE = 0;

	/**
	 * The '<em><b>SOURCE URI</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object's original URI is stored and a hit materializes by resolving it through the caller's ResourceSet against the primary store (JPA, Mongo, file). The secondary-index role made explicit: the index finds, the primary store materializes.
	 * <!-- end-model-doc -->
	 * @see #SOURCE_URI
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SOURCE_URI_VALUE = 1;

	/**
	 * An array of all the '<em><b>Materialization Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final MaterializationKind[] VALUES_ARRAY =
		new MaterializationKind[] {
			STORED_OBJECT,
			SOURCE_URI,
		};

	/**
	 * A public read-only list of all the '<em><b>Materialization Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<MaterializationKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Materialization Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static MaterializationKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			MaterializationKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Materialization Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static MaterializationKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			MaterializationKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Materialization Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static MaterializationKind get(int value) {
		switch (value) {
			case STORED_OBJECT_VALUE: return STORED_OBJECT;
			case SOURCE_URI_VALUE: return SOURCE_URI;
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
	private MaterializationKind(int value, String name, String literal) {
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
	
} //MaterializationKind
