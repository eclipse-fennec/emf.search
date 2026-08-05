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
 * A representation of the literals of the enumeration '<em><b>Refresh Mode</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * When index writes become visible to searchers.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getRefreshMode()
 * @model
 * @generated
 */
@ProviderType
public enum RefreshMode implements Enumerator {
	/**
	 * The '<em><b>NEAR REAL TIME</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Searchers are refreshed on an interval; writes become visible within it.
	 * <!-- end-model-doc -->
	 * @see #NEAR_REAL_TIME_VALUE
	 * @generated
	 * @ordered
	 */
	NEAR_REAL_TIME(0, "NEAR_REAL_TIME", "NEAR_REAL_TIME"),

	/**
	 * The '<em><b>ON COMMIT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Searchers are refreshed only when the writer commits.
	 * <!-- end-model-doc -->
	 * @see #ON_COMMIT_VALUE
	 * @generated
	 * @ordered
	 */
	ON_COMMIT(1, "ON_COMMIT", "ON_COMMIT"),

	/**
	 * The '<em><b>MANUAL</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The consumer triggers refreshes. Useful for deterministic tests and for bulk loads.
	 * <!-- end-model-doc -->
	 * @see #MANUAL_VALUE
	 * @generated
	 * @ordered
	 */
	MANUAL(2, "MANUAL", "MANUAL");

	/**
	 * The '<em><b>NEAR REAL TIME</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Searchers are refreshed on an interval; writes become visible within it.
	 * <!-- end-model-doc -->
	 * @see #NEAR_REAL_TIME
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NEAR_REAL_TIME_VALUE = 0;

	/**
	 * The '<em><b>ON COMMIT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Searchers are refreshed only when the writer commits.
	 * <!-- end-model-doc -->
	 * @see #ON_COMMIT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ON_COMMIT_VALUE = 1;

	/**
	 * The '<em><b>MANUAL</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The consumer triggers refreshes. Useful for deterministic tests and for bulk loads.
	 * <!-- end-model-doc -->
	 * @see #MANUAL
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int MANUAL_VALUE = 2;

	/**
	 * An array of all the '<em><b>Refresh Mode</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final RefreshMode[] VALUES_ARRAY =
		new RefreshMode[] {
			NEAR_REAL_TIME,
			ON_COMMIT,
			MANUAL,
		};

	/**
	 * A public read-only list of all the '<em><b>Refresh Mode</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<RefreshMode> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Refresh Mode</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static RefreshMode get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			RefreshMode result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Refresh Mode</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static RefreshMode getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			RefreshMode result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Refresh Mode</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static RefreshMode get(int value) {
		switch (value) {
			case NEAR_REAL_TIME_VALUE: return NEAR_REAL_TIME;
			case ON_COMMIT_VALUE: return ON_COMMIT;
			case MANUAL_VALUE: return MANUAL;
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
	private RefreshMode(int value, String name, String literal) {
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
	
} //RefreshMode
