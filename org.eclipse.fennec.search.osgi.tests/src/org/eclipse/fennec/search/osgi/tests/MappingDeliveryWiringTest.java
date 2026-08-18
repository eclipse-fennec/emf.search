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
package org.eclipse.fennec.search.osgi.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectProvider;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistry;
import org.eclipse.fennec.emf.osgi.eobject.registry.FileEObjectProvider;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.ESearchPackage;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.osgi.SearchConstants;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * The #32 wiring, end to end: an authored {@code *.esearch} file, loaded by a real
 * {@code FileEObjectProvider} into a real {@code EObjectRegistry} configuration, reaches
 * the {@code lucene} {@code Resource.Factory} — a resource created through it writes into
 * the configured unit and reads the object back. Wiring only; every behavioural assertion
 * lives in plain JUnit.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
class MappingDeliveryWiringTest {

	private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(10);

	@Test
	void anAuthoredEsearchReachesTheResourceFactory(@InjectBundleContext BundleContext context,
			@InjectService ConfigurationAdmin admin) throws Exception {
		// An authored *.esearch: a tiny model and its unit mapping, both roots in one file.
		EPackage tiny = EcoreFactory.eINSTANCE.createEPackage();
		tiny.setName("tiny");
		tiny.setNsPrefix("tiny");
		tiny.setNsURI("https://eclipse.org/fennec/search/test/tiny");
		EClass item = EcoreFactory.eINSTANCE.createEClass();
		item.setName("Item");
		EAttribute id = EcoreFactory.eINSTANCE.createEAttribute();
		id.setName("id");
		id.setID(true);
		id.setEType(EcorePackage.eINSTANCE.getEString());
		EAttribute label = EcoreFactory.eINSTANCE.createEAttribute();
		label.setName("label");
		label.setEType(EcorePackage.eINSTANCE.getEString());
		item.getEStructuralFeatures().add(id);
		item.getEStructuralFeatures().add(label);
		tiny.getEClassifiers().add(item);
		IndexUnitMapping mapping = ESearchFactory.eINSTANCE.createIndexUnitMapping();
		mapping.setName("wired");
		mapping.setEPackage(tiny);

		Path directory = Files.createTempDirectory("esearch-wiring");
		Path file = directory.resolve("wired.esearch");
		ResourceSet author = new ResourceSetImpl();
		author.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());
		Resource authored = author.createResource(URI.createFileURI(file.toString()));
		authored.getContents().add(mapping);
		authored.getContents().add(tiny);
		authored.save(null);

		// The real file provider as the registry's initial provider service.
		ResourceSet loader = new ResourceSetImpl();
		loader.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());
		// The authored file speaks the esearch namespace; the provider's ResourceSet must
		// be able to resolve it, or the file is skipped as unloadable.
		loader.getPackageRegistry().put(ESearchPackage.eNS_URI, ESearchPackage.eINSTANCE);
		FileEObjectProvider provider = new FileEObjectProvider("wiring-files", loader,
				List.of(directory), FileEObjectProvider.featureKeys("name"));
		ServiceRegistration<EObjectProvider> providerRegistration = context.registerService(
				EObjectProvider.class, provider, providerProperties());

		Configuration registryConfiguration = admin.createFactoryConfiguration("EObjectRegistry", "?");
		Configuration unitConfiguration = admin.createFactoryConfiguration(SearchConstants.UNIT_PID, "?");
		try {
			registryConfiguration.update(registryProperties());
			unitConfiguration.update(unitProperties());

			EObjectRegistry mappings = await(context, EObjectRegistry.class,
					"(emf.eobject.registry.name=" + SearchConstants.MAPPING_REGISTRY_NAME + ")");
			assertThat(mappings).as("the mapping registry loaded the authored file").isNotNull();
			assertThat(mappings.get("wired")).as("the mapping is registry content").isPresent();
			Resource.Factory factory = await(context, Resource.Factory.class, "(emf.protocol=lucene)");
			assertThat(factory).as("the lucene Resource.Factory is published").isNotNull();
			IndexUnit unit = await(context, IndexUnit.class,
					"(" + SearchConstants.UNIT_ALIAS + "=wired)");
			assertThat(unit).as("the configured unit is published").isNotNull();

			Resource out = factory.createResource(URI.createURI("lucene://wired/Item/i-1"));
			EObject object = EcoreUtil.create(item);
			object.eSet(id, "i-1");
			object.eSet(label, "through the registry");
			out.getContents().add(object);
			out.save(null);
			unit.refresh();

			Resource in = factory.createResource(URI.createURI("lucene://wired/Item/i-1"));
			in.load(null);

			assertThat(in.getContents()).hasSize(1);
			EObject back = in.getContents().get(0);
			assertThat(back.eGet(back.eClass().getEStructuralFeature("label")))
					.isEqualTo("through the registry");
		} finally {
			registryConfiguration.delete();
			unitConfiguration.delete();
			providerRegistration.unregister();
		}
	}

	private static Dictionary<String, Object> providerProperties() {
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("emf.eobject.provider.name", "wiring-files");
		return properties;
	}

	private static Dictionary<String, Object> registryProperties() {
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("name", SearchConstants.MAPPING_REGISTRY_NAME);
		properties.put("initialProvider.target", "(emf.eobject.provider.name=wiring-files)");
		return properties;
	}

	private static Dictionary<String, Object> unitProperties() {
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("alias", "wired");
		properties.put("location", "memory");
		properties.put("refresh", "MANUAL");
		return properties;
	}

	@SuppressWarnings("unchecked")
	private static <T> T await(BundleContext context, Class<T> type, String extraFilter)
			throws Exception {
		String filter = "(&(objectClass=" + type.getName() + ")" + extraFilter + ")";
		long deadline = System.currentTimeMillis() + TIMEOUT;
		while (System.currentTimeMillis() < deadline) {
			ServiceReference<?>[] references = context.getAllServiceReferences(null, filter);
			if (references != null && references.length > 0) {
				return (T) context.getService(references[0]);
			}
			Thread.sleep(20);
		}
		return null;
	}
}
