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

import java.util.List;
import java.util.Optional;

import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The generating mapping source (#51): the fallback that answers for a unit nobody
 * authored a mapping for — and, more importantly, the two properties that keep it from
 * overruling anyone: an authored mapping wins, and a generated one never changes shape
 * between two lookups.
 */
class GeneratingMappingSourceTest {

	private static TestModels catalog;

	@BeforeAll
	static void loadModel() {
		catalog = TestModels.load("catalog.ecore");
	}

	@Test
	void itAnswersForAUnitNamedAfterItsPackage() {
		GeneratingMappingSource source = GeneratingMappingSource.of(List.of(catalog.ePackage()));

		Optional<IndexUnitMapping> mapping = source.mappingFor("catalog");

		assertThat(mapping).isPresent();
		assertThat(mapping.get().getEPackage()).isSameAs(catalog.ePackage());
		assertThat(mapping.get().getDocuments()).isNotEmpty();
	}

	@Test
	void itStaysSilentForAUnitItKnowsNothingAbout() {
		GeneratingMappingSource source = GeneratingMappingSource.of(List.of(catalog.ePackage()));

		assertThat(source.mappingFor("somewhere-else"))
				.as("a source that generated for every name would answer for typos too")
				.isEmpty();
	}

	@Test
	void theSameUnitKeepsTheSameMapping() {
		GeneratingMappingSource source = GeneratingMappingSource.of(List.of(catalog.ePackage()));

		IndexUnitMapping first = source.mappingFor("catalog").orElseThrow();
		IndexUnitMapping second = source.mappingFor("catalog").orElseThrow();

		assertThat(second).as("a mapping decides what a document looks like — answering twice "
				+ "differently would mean two shapes in one index").isSameAs(first);
	}

	@Test
	void anAuthoredMappingWins() {
		IndexUnitMapping authored = ESearchFactory.eINSTANCE.createIndexUnitMapping();
		authored.setName("catalog");
		authored.setEPackage(catalog.ePackage());
		MappingSource authoredSource = unit -> "catalog".equals(unit) ? Optional.of(authored)
				: Optional.empty();

		// The composition §4.1 prescribes: authored first, generated last.
		MappingSource composed = MappingSources.withPrecedence(authoredSource,
				GeneratingMappingSource.of(List.of(catalog.ePackage())));

		assertThat(composed.mappingFor("catalog")).contains(authored);
	}

	@Test
	void aRemovedPackageStopsAnswering() {
		GeneratingMappingSource source = GeneratingMappingSource.of(List.of(catalog.ePackage()));
		assertThat(source.mappingFor("catalog")).isPresent();

		source.removePackage(catalog.ePackage());

		assertThat(source.mappingFor("catalog"))
				.as("the model is gone, and so is what was generated from it")
				.isEmpty();
	}
}
