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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Value Source</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * One rung of the extraction ladder: where a field's value comes from (S20, docs/search-access.md §4.2). The ladder is ordered by what has to run before the value is known — an attribute read, a navigation, an evaluated expression — and the rule is to use the weakest rung that suffices, because the lower ones are verifiable against the metamodel alone.
 * <!-- end-model-doc -->
 *
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getValueSource()
 * @model abstract="true"
 * @generated
 */
@ProviderType
public interface ValueSource extends EObject {
} // ValueSource
