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
package org.eclipse.fennec.search.esearch.configuration;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;

import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;

import org.eclipse.fennec.search.esearch.ESearchPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>EPackageConfiguration</b> and <b>ResourceFactoryConfigurator</b> for the model.
 * The package will be registered into a OSGi base model registry.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Declarative EClass-to-Lucene-index mapping for emf.search — "eorm for the index" (docs/search-access.md §4). Instances declare how EObjects become Lucene documents: index unit policies, per-feature field mappings, reference strategies, facets, rank signals, suggest sources.
 * 
 * Two rules shape this metamodel:
 * 
 * 1. Conventions over declarations. An unmapped EClass or EAttribute is mapped by convention (id -> stored keyword, strings -> analyzed text, numerics -> point + doc values), so small models need no mapping instance at all. A declaration is an override, never a prerequisite.
 * 
 * 2. Physical properties belong to the index unit, logical ones to the document. One unit is one Lucene directory with one IndexWriter, so refresh policy, commit policy and index sort are declared once per unit — not per EClass, which could not be honoured independently. Everything that describes a single document (fields, references, materialization) hangs off DocumentMapping.
 * 
 * Delivery uses the two mechanisms emf.osgi already provides, not a third one: an authored XMI document (*.esearch) is loaded into a named EObject registry (org.eclipse.fennec.emf.osgi.eobject.registry, which has a non-OSGi bootstrap so the plain-Java path is identical), and a model bundle can ship its own mapping as an AspectEntry with typeId "esearch" on the package metadata — the same slot the codec and orm aspects use.
 * <!-- end-model-doc -->
 * @see EPackageConfigurator
 * @generated
 */
public class ESearchEPackageConfigurator implements EPackageConfigurator {
	
	/**
	 * The fingerprint of this model version, computed from the <code>.ecore</code> at build
	 * time. It identifies the model content, not the artifact - see the <code>emf.fingerprint</code>
	 * service property.
	 * @generated
	 */
	public static final String FINGERPRINT = "fp1:622f46015319bc800e39d29d0c2cd3fccc39bf2ae7c187e9f4c2701f3598c9c1";

	private ESearchPackage ePackage;

	protected ESearchEPackageConfigurator(ESearchPackage ePackage){
		this.ePackage = ePackage;
	}
	
	/**
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.emf.osgi.EPackageRegistryConfigurator#configureEPackage(org.eclipse.emf.ecore.EPackage.Registry)
	 * @generated
	 */
	@Override
	public void configureEPackage(org.eclipse.emf.ecore.EPackage.Registry registry) {
		registry.put(ESearchPackage.eNS_URI, ePackage);
	}
	
	/**
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.emf.osgi.EPackageRegistryConfigurator#unconfigureEPackage(org.eclipse.emf.ecore.EPackage.Registry)
	 * @generated
	 */
	@Override
	public void unconfigureEPackage(org.eclipse.emf.ecore.EPackage.Registry registry) {
		registry.remove(ESearchPackage.eNS_URI);
	}
	
	/**
	 * A method providing the Properties the services around this Model should be registered with.
	 * @generated
	 */
	public Map<String, Object> getServiceProperties() {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(EMFNamespaces.EMF_NAME, ESearchPackage.eNAME);
		properties.put(EMFNamespaces.EMF_MODEL_NSURI, ESearchPackage.eNS_URI);
		properties.put(EMFNamespaces.EMF_MODEL_REGISTRATION, EMFNamespaces.MODEL_REGISTRATION_PROVIDED);
		properties.put(EMFNamespaces.EMF_MODEL_FILE_EXT, "esearch");
		properties.put(EMFNamespaces.EMF_MODEL_VERSION, "1.0");
		properties.put(EMFNamespaces.EMF_MODEL_FINGERPRINT, FINGERPRINT);
		return properties;
	}
}