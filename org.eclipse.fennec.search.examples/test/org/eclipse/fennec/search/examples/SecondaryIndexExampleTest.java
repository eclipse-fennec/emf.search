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

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.fennec.model.query.builder.Expressions.path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.api.Hit;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.Materialization;
import org.eclipse.fennec.search.esearch.MaterializationKind;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.query.IndexSearch;
import org.eclipse.fennec.search.query.PrimaryStore;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The secondary-index example (docs: example-secondary-index.md): the originals live in a
 * primary store — here a plain EMF resource standing in for JPA or Mongo — and the index
 * answers search. The mapping delta is one declaration ({@code SOURCE_URI}); the read
 * side attaches a {@link PrimaryStore} and gets the originals back, one batch per search.
 */
class SecondaryIndexExampleTest {

	private ExampleCatalog catalog;
	private IndexUnit unit;
	private IndexSchema schema;
	private Resource primary;
	private EObject machine;
	private EObject grinder;

	@BeforeEach
	void indexTheCorpus() throws IOException {
		catalog = ExampleCatalog.load();
		unit = IndexUnit.open(IndexUnitConfig.inMemory("catalog")
				.refresh(RefreshTrigger.manual())
				.build());

		// The mapping delta against catalog.esearch: Product documents carry the
		// original's URI instead of a reconstruction.
		IndexUnitMapping mapping = EcoreUtil.copy(catalog.mapping());
		Materialization sourceUri = ESearchFactory.eINSTANCE.createMaterialization();
		sourceUri.setKind(MaterializationKind.SOURCE_URI);
		mapping.getDocuments().stream()
				.filter(document -> "Product".equals(document.getEClass().getName()))
				.findFirst().orElseThrow()
				.setMaterialization(sourceUri);
		schema = IndexSchema.of(mapping);

		// The primary store: where the objects actually live.
		primary = new ResourceImpl(URI.createURI("mongodb://shop/Product"));
		machine = catalog.create("Product", "id", "p-1", "name", "Espresso Machine",
				"description", "A compact espresso machine for fresh coffee");
		grinder = catalog.create("Product", "id", "p-2", "name", "Coffee Grinder",
				"description", "A manual burr grinder for filter coffee");
		primary.getContents().add(machine);
		primary.getContents().add(grinder);

		// Feed the index — in production this is the save path or the change stream.
		DocumentMapper mapper = DocumentMapper.of(schema);
		for (EObject object : List.of(machine, grinder)) {
			var mapped = mapper.map(object);
			unit.updateDocuments(mapped.term(), mapped.documents());
		}
		unit.refresh();
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	@Test
	void searchAnswersWithTheOriginals() throws Exception {
		// The store resolves one batch of URIs per search window — a JPA store would
		// answer with one query. The example records the batches to show exactly that.
		List<List<URI>> batches = new ArrayList<>();
		PrimaryStore store = uris -> {
			batches.add(uris);
			Map<URI, EObject> resolved = new LinkedHashMap<>();
			for (EObject original : primary.getContents()) {
				resolved.put(EcoreUtil.getURI(original), original);
			}
			return resolved;
		};

		List<Hit> hits = IndexSearch.of(unit, schema)
				.withPrimaryStore(store)
				.search(QueryBuilder.from(catalog.eClass("Product"))
						.where(path(catalog.attribute("Product", "description")).contains("coffee"))
						.build());

		assertThat(hits).extracting(Hit::object)
				.as("the hits are the primary store's originals, not reconstructions")
				.containsExactlyInAnyOrder(machine, grinder);
		assertThat(batches).as("one search, one batch").hasSize(1);
	}

	@Test
	void withoutTheStoreTheHitsSayWhereTheOriginalsLive() throws Exception {
		List<Hit> hits = IndexSearch.of(unit, schema)
				.search(QueryBuilder.from(catalog.eClass("Product"))
						.where(path(catalog.attribute("Product", "description")).contains("espresso"))
						.build());

		// Without a store, a hit is an EMF proxy carrying the original's URI —
		// exactly what the index knows, resolvable by whoever wants to.
		assertThat(hits).hasSize(1);
		assertThat(hits.get(0).object().eIsProxy()).isTrue();
	}
}
