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

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectResolvingEList;

import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.VectorFieldMapping;
import org.eclipse.fennec.search.esearch.VectorSimilarity;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Vector Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.VectorFieldMappingImpl#getEmbeddingSources <em>Embedding Sources</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.VectorFieldMappingImpl#getProvider <em>Provider</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.VectorFieldMappingImpl#getDimensions <em>Dimensions</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.VectorFieldMappingImpl#getSimilarity <em>Similarity</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.impl.VectorFieldMappingImpl#getModelVersion <em>Model Version</em>}</li>
 * </ul>
 *
 * @generated
 */
public class VectorFieldMappingImpl extends FieldMappingImpl implements VectorFieldMapping {
	/**
	 * The cached value of the '{@link #getEmbeddingSources() <em>Embedding Sources</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEmbeddingSources()
	 * @generated
	 * @ordered
	 */
	protected EList<EAttribute> embeddingSources;

	/**
	 * The default value of the '{@link #getProvider() <em>Provider</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProvider()
	 * @generated
	 * @ordered
	 */
	protected static final String PROVIDER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getProvider() <em>Provider</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProvider()
	 * @generated
	 * @ordered
	 */
	protected String provider = PROVIDER_EDEFAULT;

	/**
	 * The default value of the '{@link #getDimensions() <em>Dimensions</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDimensions()
	 * @generated
	 * @ordered
	 */
	protected static final int DIMENSIONS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDimensions() <em>Dimensions</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDimensions()
	 * @generated
	 * @ordered
	 */
	protected int dimensions = DIMENSIONS_EDEFAULT;

	/**
	 * The default value of the '{@link #getSimilarity() <em>Similarity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSimilarity()
	 * @generated
	 * @ordered
	 */
	protected static final VectorSimilarity SIMILARITY_EDEFAULT = VectorSimilarity.COSINE;

	/**
	 * The cached value of the '{@link #getSimilarity() <em>Similarity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSimilarity()
	 * @generated
	 * @ordered
	 */
	protected VectorSimilarity similarity = SIMILARITY_EDEFAULT;

	/**
	 * The default value of the '{@link #getModelVersion() <em>Model Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getModelVersion()
	 * @generated
	 * @ordered
	 */
	protected static final String MODEL_VERSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getModelVersion() <em>Model Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getModelVersion()
	 * @generated
	 * @ordered
	 */
	protected String modelVersion = MODEL_VERSION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected VectorFieldMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ESearchPackage.Literals.VECTOR_FIELD_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<EAttribute> getEmbeddingSources() {
		if (embeddingSources == null) {
			embeddingSources = new EObjectResolvingEList<EAttribute>(EAttribute.class, this, ESearchPackage.VECTOR_FIELD_MAPPING__EMBEDDING_SOURCES);
		}
		return embeddingSources;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getProvider() {
		return provider;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setProvider(String newProvider) {
		String oldProvider = provider;
		provider = newProvider;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.VECTOR_FIELD_MAPPING__PROVIDER, oldProvider, provider));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getDimensions() {
		return dimensions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDimensions(int newDimensions) {
		int oldDimensions = dimensions;
		dimensions = newDimensions;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.VECTOR_FIELD_MAPPING__DIMENSIONS, oldDimensions, dimensions));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public VectorSimilarity getSimilarity() {
		return similarity;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSimilarity(VectorSimilarity newSimilarity) {
		VectorSimilarity oldSimilarity = similarity;
		similarity = newSimilarity == null ? SIMILARITY_EDEFAULT : newSimilarity;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.VECTOR_FIELD_MAPPING__SIMILARITY, oldSimilarity, similarity));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getModelVersion() {
		return modelVersion;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setModelVersion(String newModelVersion) {
		String oldModelVersion = modelVersion;
		modelVersion = newModelVersion;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ESearchPackage.VECTOR_FIELD_MAPPING__MODEL_VERSION, oldModelVersion, modelVersion));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ESearchPackage.VECTOR_FIELD_MAPPING__EMBEDDING_SOURCES:
				return getEmbeddingSources();
			case ESearchPackage.VECTOR_FIELD_MAPPING__PROVIDER:
				return getProvider();
			case ESearchPackage.VECTOR_FIELD_MAPPING__DIMENSIONS:
				return getDimensions();
			case ESearchPackage.VECTOR_FIELD_MAPPING__SIMILARITY:
				return getSimilarity();
			case ESearchPackage.VECTOR_FIELD_MAPPING__MODEL_VERSION:
				return getModelVersion();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ESearchPackage.VECTOR_FIELD_MAPPING__EMBEDDING_SOURCES:
				getEmbeddingSources().clear();
				getEmbeddingSources().addAll((Collection<? extends EAttribute>)newValue);
				return;
			case ESearchPackage.VECTOR_FIELD_MAPPING__PROVIDER:
				setProvider((String)newValue);
				return;
			case ESearchPackage.VECTOR_FIELD_MAPPING__DIMENSIONS:
				setDimensions((Integer)newValue);
				return;
			case ESearchPackage.VECTOR_FIELD_MAPPING__SIMILARITY:
				setSimilarity((VectorSimilarity)newValue);
				return;
			case ESearchPackage.VECTOR_FIELD_MAPPING__MODEL_VERSION:
				setModelVersion((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ESearchPackage.VECTOR_FIELD_MAPPING__EMBEDDING_SOURCES:
				getEmbeddingSources().clear();
				return;
			case ESearchPackage.VECTOR_FIELD_MAPPING__PROVIDER:
				setProvider(PROVIDER_EDEFAULT);
				return;
			case ESearchPackage.VECTOR_FIELD_MAPPING__DIMENSIONS:
				setDimensions(DIMENSIONS_EDEFAULT);
				return;
			case ESearchPackage.VECTOR_FIELD_MAPPING__SIMILARITY:
				setSimilarity(SIMILARITY_EDEFAULT);
				return;
			case ESearchPackage.VECTOR_FIELD_MAPPING__MODEL_VERSION:
				setModelVersion(MODEL_VERSION_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ESearchPackage.VECTOR_FIELD_MAPPING__EMBEDDING_SOURCES:
				return embeddingSources != null && !embeddingSources.isEmpty();
			case ESearchPackage.VECTOR_FIELD_MAPPING__PROVIDER:
				return PROVIDER_EDEFAULT == null ? provider != null : !PROVIDER_EDEFAULT.equals(provider);
			case ESearchPackage.VECTOR_FIELD_MAPPING__DIMENSIONS:
				return dimensions != DIMENSIONS_EDEFAULT;
			case ESearchPackage.VECTOR_FIELD_MAPPING__SIMILARITY:
				return similarity != SIMILARITY_EDEFAULT;
			case ESearchPackage.VECTOR_FIELD_MAPPING__MODEL_VERSION:
				return MODEL_VERSION_EDEFAULT == null ? modelVersion != null : !MODEL_VERSION_EDEFAULT.equals(modelVersion);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (provider: ");
		result.append(provider);
		result.append(", dimensions: ");
		result.append(dimensions);
		result.append(", similarity: ");
		result.append(similarity);
		result.append(", modelVersion: ");
		result.append(modelVersion);
		result.append(')');
		return result.toString();
	}

} //VectorFieldMappingImpl
