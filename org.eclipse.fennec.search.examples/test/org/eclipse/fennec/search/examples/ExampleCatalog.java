/********************************************************************
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
 ********************************************************************/
package org.eclipse.fennec.search.examples;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;

/**
 * The documentation's shared catalog model and its authored mapping, loaded from the two
 * files every EXAMPLES page shows: {@code catalog.ecore} and {@code catalog.esearch}.
 * The examples use the dynamic EMF API ({@link #create(String, Object...)}), so the
 * pages stay free of generated-code assumptions — with a generated model, every
 * {@code create("Product", ...)} is simply {@code CatalogFactory.eINSTANCE.createProduct()}.
 */
public final class ExampleCatalog {

	private final EPackage ePackage;
	private final IndexUnitMapping mapping;

	private ExampleCatalog(EPackage ePackage, IndexUnitMapping mapping) {
		this.ePackage = ePackage;
		this.mapping = mapping;
	}

	/** Loads catalog.ecore and catalog.esearch from beside this class. */
	public static ExampleCatalog load() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put("*", new XMIResourceFactoryImpl());
		// The mapping file references the model by nsURI and speaks the esearch
		// namespace — both resolve through the package registry.
		resourceSet.getPackageRegistry().put(ESearchPackage.eNS_URI, ESearchPackage.eINSTANCE);
		EPackage ePackage = (EPackage) load(resourceSet, "catalog.ecore");
		resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
		IndexUnitMapping mapping = (IndexUnitMapping) load(resourceSet, "catalog.esearch");
		return new ExampleCatalog(ePackage, mapping);
	}

	private static EObject load(ResourceSet resourceSet, String fileName) {
		try (InputStream stream = ExampleCatalog.class.getResourceAsStream(fileName)) {
			if (stream == null) {
				throw new IOException("No such file on the classpath: " + fileName);
			}
			Resource resource = resourceSet.createResource(URI.createURI(
					"example:/" + ExampleCatalog.class.getPackageName().replace('.', '/') + "/" + fileName));
			resource.load(stream, null);
			return resource.getContents().get(0);
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot load " + fileName, e);
		}
	}

	public EPackage ePackage() {
		return ePackage;
	}

	/** The authored mapping from catalog.esearch. */
	public IndexUnitMapping mapping() {
		return mapping;
	}

	public EClass eClass(String name) {
		EClass eClass = (EClass) ePackage.getEClassifier(name);
		if (eClass == null) {
			throw new IllegalArgumentException("No EClass '" + name + "' in " + ePackage.getName());
		}
		return eClass;
	}

	public EAttribute attribute(String className, String featureName) {
		return (EAttribute) feature(className, featureName);
	}

	public EReference reference(String className, String featureName) {
		return (EReference) feature(className, featureName);
	}

	public EStructuralFeature feature(String className, String featureName) {
		EStructuralFeature feature = eClass(className).getEStructuralFeature(featureName);
		if (feature == null) {
			throw new IllegalArgumentException("No feature '" + featureName + "' on " + className);
		}
		return feature;
	}

	/** Creates an instance and sets the given feature/value pairs. */
	public EObject create(String className, Object... featureValuePairs) {
		EClass eClass = eClass(className);
		EObject object = EcoreUtil.create(eClass);
		for (int i = 0; i < featureValuePairs.length; i += 2) {
			String featureName = (String) featureValuePairs[i];
			Object value = featureValuePairs[i + 1];
			EStructuralFeature feature = eClass.getEStructuralFeature(featureName);
			if (feature == null) {
				throw new IllegalArgumentException("No feature '" + featureName + "' on " + className);
			}
			if (feature.isMany() && value instanceof Iterable<?> values) {
				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) object.eGet(feature);
				values.forEach(list::add);
			} else {
				object.eSet(feature, value);
			}
		}
		return object;
	}
}
