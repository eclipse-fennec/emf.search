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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EAttribute;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Vector Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * RESERVED for wave 2 — dense vector field for k-nearest-neighbour search.
 * 
 * Declared in the metamodel now so that adding vector search later is additive rather than a breaking change, but NOT implemented: a mapping that declares one is refused with a clear diagnostic until the wave-2 work lands. Three things are still open and deliberately not fixed here: whether the query IR carries a vector or a text to embed, how the embedding provider is selected, and the dimension ceiling of the chosen codec.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.search.esearch.VectorFieldMapping#getEmbeddingSources <em>Embedding Sources</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.VectorFieldMapping#getProvider <em>Provider</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.VectorFieldMapping#getDimensions <em>Dimensions</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.VectorFieldMapping#getSimilarity <em>Similarity</em>}</li>
 *   <li>{@link org.eclipse.fennec.search.esearch.VectorFieldMapping#getModelVersion <em>Model Version</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getVectorFieldMapping()
 * @model
 * @generated
 */
@ProviderType
public interface VectorFieldMapping extends FieldMapping {
	/**
	 * Returns the value of the '<em><b>Embedding Sources</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EAttribute}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Attributes whose values are concatenated into the text handed to the embedding provider. Renamed from sources when FieldMapping gained the extraction ladder (S20): every field mapping now has sources of its own, and a vector field's inputs are a different thing — free to rename while the slot is reserved and nothing writes it.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Embedding Sources</em>' reference list.
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getVectorFieldMapping_EmbeddingSources()
	 * @model
	 * @generated
	 */
	EList<EAttribute> getEmbeddingSources();

	/**
	 * Returns the value of the '<em><b>Provider</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Id of the embedding provider. Embeddings are never computed in this repository; the provider is a service in OSGi and a constructor argument in plain Java.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Provider</em>' attribute.
	 * @see #setProvider(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getVectorFieldMapping_Provider()
	 * @model
	 * @generated
	 */
	String getProvider();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.VectorFieldMapping#getProvider <em>Provider</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Provider</em>' attribute.
	 * @see #getProvider()
	 * @generated
	 */
	void setProvider(String value);

	/**
	 * Returns the value of the '<em><b>Dimensions</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Vector length. Must match what the provider produces and must not exceed the codec's dimension limit.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Dimensions</em>' attribute.
	 * @see #setDimensions(int)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getVectorFieldMapping_Dimensions()
	 * @model
	 * @generated
	 */
	int getDimensions();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.VectorFieldMapping#getDimensions <em>Dimensions</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dimensions</em>' attribute.
	 * @see #getDimensions()
	 * @generated
	 */
	void setDimensions(int value);

	/**
	 * Returns the value of the '<em><b>Similarity</b></em>' attribute.
	 * The default value is <code>"COSINE"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.search.esearch.VectorSimilarity}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Vector similarity function.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Similarity</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.VectorSimilarity
	 * @see #setSimilarity(VectorSimilarity)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getVectorFieldMapping_Similarity()
	 * @model default="COSINE"
	 * @generated
	 */
	VectorSimilarity getSimilarity();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.VectorFieldMapping#getSimilarity <em>Similarity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Similarity</em>' attribute.
	 * @see org.eclipse.fennec.search.esearch.VectorSimilarity
	 * @see #getSimilarity()
	 * @generated
	 */
	void setSimilarity(VectorSimilarity value);

	/**
	 * Returns the value of the '<em><b>Model Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Version of the embedding model. Recorded as index metadata because changing the embedding model invalidates every vector in the index — a version change is a rebuild, and the index must be able to say so.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Model Version</em>' attribute.
	 * @see #setModelVersion(String)
	 * @see org.eclipse.fennec.search.esearch.ESearchPackage#getVectorFieldMapping_ModelVersion()
	 * @model
	 * @generated
	 */
	String getModelVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.search.esearch.VectorFieldMapping#getModelVersion <em>Model Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model Version</em>' attribute.
	 * @see #getModelVersion()
	 * @generated
	 */
	void setModelVersion(String value);

} // VectorFieldMapping
