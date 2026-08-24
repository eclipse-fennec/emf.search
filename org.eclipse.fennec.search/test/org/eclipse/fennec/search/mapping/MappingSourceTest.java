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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistries;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryWriter;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Mapping delivery (S23, #32): the registry-backed source answers by the mapping's own
 * unit name, notices changes through the registry's listener, composes with declared
 * precedence — and a mapping that disagrees with its unit's package universe fails at
 * resolution, not on the first document.
 */
class MappingSourceTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;

	@BeforeAll
	static void loadModel() {
		catalog = TestModels.load("catalog.ecore");
	}

	@Test
	void answersByTheMappingsOwnNameNotTheRegistryKey() {
		EObjectRegistryWriter writer = EObjectRegistries.createRegistry("mappings");
		writer.put("test", "some-file.esearch", mapping("catalog"), Map.of());
		RegistryMappingSource source = RegistryMappingSource.of(writer.getRegistry());

		assertThat(source.mappingFor("catalog")).isPresent();
		assertThat(source.mappingFor("some-file.esearch"))
				.as("the key a file was registered under is a delivery detail, not a unit")
				.isEmpty();
	}

	@Test
	void aChangedMappingIsNoticed() {
		EObjectRegistryWriter writer = EObjectRegistries.createRegistry("mappings");
		RegistryMappingSource source = RegistryMappingSource.of(writer.getRegistry());
		List<String> changed = new ArrayList<>();
		source.addListener(changed::add);

		writer.put("test", "catalog", mapping("catalog"), Map.of());
		writer.put("test", "catalog", mapping("catalog"), Map.of());
		writer.remove("test", "catalog");

		assertThat(changed).containsExactly("catalog", "catalog", "catalog", "catalog");
	}

	@Test
	void precedenceTakesTheFirstSourceThatAnswers() {
		EObjectRegistryWriter deployment = EObjectRegistries.createRegistry("deployment");
		deployment.put("deployment", "catalog", mapping("catalog"), Map.of());
		IndexUnitMapping shipped = mapping("catalog");
		MappingSource shippedSource = unit -> "catalog".equals(unit) ? Optional.of(shipped) : Optional.empty();

		MappingSource source = MappingSources.withPrecedence(
				RegistryMappingSource.of(deployment.getRegistry()), shippedSource);

		assertThat(source.mappingFor("catalog").orElseThrow())
				.as("the deployment overrides what the model ships with")
				.isNotSameAs(shipped);
		deployment.remove("deployment", "catalog");
		assertThat(source.mappingFor("catalog").orElseThrow())
				.as("without a deployment entry the shipped mapping serves")
				.isSameAs(shipped);
	}

	@Test
	void aMappingFromAForeignPackageFailsAtResolution() {
		EPackage other = EcoreFactory.eINSTANCE.createEPackage();
		other.setName("other");
		other.setNsURI("https://example.org/other");
		IndexUnitMapping mapping = mapping("catalog");
		DocumentMapping foreign = ESEARCH.createDocumentMapping();
		foreign.setEClass(EcorePackage.eINSTANCE.getEClass());
		mapping.getDocuments().add(foreign);

		assertThatThrownBy(() -> IndexSchema.of(mapping))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("package");
	}

	// --- helpers --------------------------------------------------------------------------

	private static IndexUnitMapping mapping(String unit) {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName(unit);
		mapping.setEPackage(catalog.ePackage());
		return mapping;
	}
}
