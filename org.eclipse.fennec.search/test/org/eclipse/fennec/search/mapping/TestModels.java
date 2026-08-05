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
package org.eclipse.fennec.search.mapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * Loads a test {@code .ecore} from the classpath and creates instances dynamically — the
 * approach the m2x workspace uses for its example models.
 * <p>
 * No code generation for test fixtures on purpose: the mapper works on the reflective EMF
 * API anyway, so a generated model would test less, not more, and every new fixture would
 * cost a genmodel and a build step.
 */
final class TestModels {

	private final EPackage ePackage;

	private TestModels(EPackage ePackage) {
		this.ePackage = ePackage;
	}

	/** Loads the ecore next to this class and registers it in its own resource set. */
	static TestModels load(String fileName) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put("ecore", new XMIResourceFactoryImpl());
		try (InputStream stream = TestModels.class.getResourceAsStream(fileName)) {
			if (stream == null) {
				throw new IOException("No such ecore on the classpath: " + fileName);
			}
			Resource resource = resourceSet.createResource(
					URI.createURI("test:/" + TestModels.class.getPackageName().replace('.', '/') + "/" + fileName));
			resource.load(stream, null);
			EPackage loaded = (EPackage) resource.getContents().get(0);
			resourceSet.getPackageRegistry().put(loaded.getNsURI(), loaded);
			return new TestModels(loaded);
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot load " + fileName, e);
		}
	}

	EPackage ePackage() {
		return ePackage;
	}

	EClass eClass(String name) {
		EClass eClass = (EClass) ePackage.getEClassifier(name);
		if (eClass == null) {
			throw new IllegalArgumentException("No EClass '" + name + "' in " + ePackage.getName());
		}
		return eClass;
	}

	EStructuralFeature feature(String className, String featureName) {
		EStructuralFeature feature = eClass(className).getEStructuralFeature(featureName);
		if (feature == null) {
			throw new IllegalArgumentException("No feature '" + featureName + "' on " + className);
		}
		return feature;
	}

	/** Creates an instance and sets the given feature/value pairs. */
	EObject create(String className, Object... featureValuePairs) {
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
				java.util.List<Object> list = (java.util.List<Object>) object.eGet(feature);
				values.forEach(list::add);
			} else {
				object.eSet(feature, value);
			}
		}
		return object;
	}
}
