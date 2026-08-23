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
package org.eclipse.fennec.search.esearch.impl;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.ValueSource;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Value Source</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public abstract class ValueSourceImpl extends MinimalEObjectImpl.Container implements ValueSource {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ValueSourceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.VALUE_SOURCE;
	}

} //ValueSourceImpl
