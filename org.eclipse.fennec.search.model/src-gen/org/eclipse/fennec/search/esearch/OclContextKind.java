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
 * A representation of the literals of the enumeration '<em><b>Ocl Context Kind</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * The object an OclSource evaluates against.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getOclContextKind()
 * @model
 * @generated
 */
@ProviderType
public enum OclContextKind implements Enumerator {
	/**
	 * The '<em><b>SELF</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The mapped object itself — the document being written. The default, and the only context a flat document has.
	 * <!-- end-model-doc -->
	 * @see #SELF_VALUE
	 * @generated
	 * @ordered
	 */
	SELF(0, "SELF", "SELF"),

	/**
	 * The '<em><b>ROOT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The root object of the block. Meaningful on a NESTED child, whose field may need something from the parent it was written under; on a root document it is the same object as SELF.
	 * <!-- end-model-doc -->
	 * @see #ROOT_VALUE
	 * @generated
	 * @ordered
	 */
	ROOT(1, "ROOT", "ROOT");

	/**
	 * The '<em><b>SELF</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The mapped object itself — the document being written. The default, and the only context a flat document has.
	 * <!-- end-model-doc -->
	 * @see #SELF
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SELF_VALUE = 0;

	/**
	 * The '<em><b>ROOT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The root object of the block. Meaningful on a NESTED child, whose field may need something from the parent it was written under; on a root document it is the same object as SELF.
	 * <!-- end-model-doc -->
	 * @see #ROOT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ROOT_VALUE = 1;

	/**
	 * An array of all the '<em><b>Ocl Context Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final OclContextKind[] VALUES_ARRAY =
		new OclContextKind[] {
			SELF,
			ROOT,
		};

	/**
	 * A public read-only list of all the '<em><b>Ocl Context Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<OclContextKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Ocl Context Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static OclContextKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			OclContextKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Ocl Context Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static OclContextKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			OclContextKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Ocl Context Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static OclContextKind get(int value) {
		switch (value) {
			case SELF_VALUE: return SELF;
			case ROOT_VALUE: return ROOT;
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
	private OclContextKind(int value, String name, String literal) {
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
	
} //OclContextKind
