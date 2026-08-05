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
 * A representation of the literals of the enumeration '<em><b>Reference Strategy</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * How a reference target is indexed.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getReferenceStrategy()
 * @model
 * @generated
 */
@ProviderType
public enum ReferenceStrategy implements Enumerator {
	/**
	 * The '<em><b>ID ONLY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Store the target id only. Queries over the target's own features are refused, exactly as cross-document paths are refused by the Mongo backend — the honest default.
	 * <!-- end-model-doc -->
	 * @see #ID_ONLY_VALUE
	 * @generated
	 * @ordered
	 */
	ID_ONLY(0, "ID_ONLY", "ID_ONLY"),

	/**
	 * The '<em><b>EMBED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Denormalize the target's mapped fields into the parent document under a prefix. Cheap and flat, but it loses per-target correlation for multi-valued references: a conjunction over two embedded fields can be satisfied by two different targets.
	 * <!-- end-model-doc -->
	 * @see #EMBED_VALUE
	 * @generated
	 * @ordered
	 */
	EMBED(1, "EMBED", "EMBED"),

	/**
	 * The '<em><b>NESTED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Index each target as a child document in the parent's block, queried by block join. Preserves correlation, and is the only reference strategy over which EXISTS/FOR_ALL are answerable. The price: a block is replaceable only as a whole, so any child change reindexes the parent and all its children. Containment only.
	 * <!-- end-model-doc -->
	 * @see #NESTED_VALUE
	 * @generated
	 * @ordered
	 */
	NESTED(2, "NESTED", "NESTED");

	/**
	 * The '<em><b>ID ONLY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Store the target id only. Queries over the target's own features are refused, exactly as cross-document paths are refused by the Mongo backend — the honest default.
	 * <!-- end-model-doc -->
	 * @see #ID_ONLY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ID_ONLY_VALUE = 0;

	/**
	 * The '<em><b>EMBED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Denormalize the target's mapped fields into the parent document under a prefix. Cheap and flat, but it loses per-target correlation for multi-valued references: a conjunction over two embedded fields can be satisfied by two different targets.
	 * <!-- end-model-doc -->
	 * @see #EMBED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int EMBED_VALUE = 1;

	/**
	 * The '<em><b>NESTED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Index each target as a child document in the parent's block, queried by block join. Preserves correlation, and is the only reference strategy over which EXISTS/FOR_ALL are answerable. The price: a block is replaceable only as a whole, so any child change reindexes the parent and all its children. Containment only.
	 * <!-- end-model-doc -->
	 * @see #NESTED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NESTED_VALUE = 2;

	/**
	 * An array of all the '<em><b>Reference Strategy</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final ReferenceStrategy[] VALUES_ARRAY =
		new ReferenceStrategy[] {
			ID_ONLY,
			EMBED,
			NESTED,
		};

	/**
	 * A public read-only list of all the '<em><b>Reference Strategy</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<ReferenceStrategy> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Reference Strategy</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ReferenceStrategy get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ReferenceStrategy result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Reference Strategy</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ReferenceStrategy getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ReferenceStrategy result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Reference Strategy</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ReferenceStrategy get(int value) {
		switch (value) {
			case ID_ONLY_VALUE: return ID_ONLY;
			case EMBED_VALUE: return EMBED;
			case NESTED_VALUE: return NESTED;
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
	private ReferenceStrategy(int value, String name, String literal) {
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
	
} //ReferenceStrategy
