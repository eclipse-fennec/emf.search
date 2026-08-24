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
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.api.Hit;
import org.eclipse.fennec.persistence.resource.PersistenceResource;
import org.eclipse.fennec.search.facet.FacetRequest;
import org.eclipse.fennec.search.facet.FacetResults;
import org.eclipse.fennec.search.facet.FacetSearch;
import org.eclipse.fennec.search.highlight.HighlightRequest;
import org.eclipse.fennec.search.highlight.HighlightSearch;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.query.IndexSearch;
import org.eclipse.fennec.search.resource.SearchResourceFactory;
import org.eclipse.fennec.search.suggest.SuggestSearch;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The search-box example (docs: example-search-box.md): one small corpus, and the four
 * things every product search box eventually needs — scored hits, facet counts to filter
 * by, completions while typing, and highlighted snippets. Everything runs over the
 * authored mapping in {@code catalog.esearch}; this test is the page's code, verified.
 */
class SearchBoxExampleTest {

	private ExampleCatalog catalog;
	private IndexUnit unit;
	private IndexSchema schema;

	@BeforeEach
	void indexTheCorpus() throws IOException {
		catalog = ExampleCatalog.load();
		unit = IndexUnit.open(IndexUnitConfig.inMemory("catalog")
				.refresh(RefreshTrigger.manual())
				.build());
		schema = IndexSchema.of(catalog.mapping());

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap()
				.put("lucene", new SearchResourceFactory(unit, DocumentMapper.of(schema)));

		save(resourceSet, "p-1", product("p-1", "Espresso Machine",
				"A compact espresso machine with a built-in coffee grinder", "NEW", 800,
				List.of("kitchen", "coffee")));
		save(resourceSet, "p-2", product("p-2", "Coffee Grinder",
				"A manual burr grinder for filter coffee", "USED", 120,
				List.of("kitchen", "coffee")));
		save(resourceSet, "p-3", product("p-3", "Electric Kettle",
				"An electric kettle with temperature control", "NEW", 40,
				List.of("kitchen")));

		// An index is near-real-time: the write is visible after the next refresh.
		unit.refresh();
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	@Test
	void theSearchBox() throws Exception {
		Query query = QueryBuilder.from(catalog.eClass("Product"))
				.where(path(description()).contains("coffee"))
				.build();

		// Hits: plain objects with scores, best first.
		List<Hit> hits = IndexSearch.of(unit, schema).search(query);
		assertThat(hits).hasSize(2);
		assertThat(hits).allSatisfy(hit -> assertThat(hit.score()).isGreaterThan(0.0));

		// Facets: what the filter sidebar shows, counted over the same match set.
		FacetResults facets = FacetSearch.of(unit, schema)
				.count(FacetRequest.over(query).dimension("condition").dimension("tags"));
		assertThat(facets.dimension("condition").orElseThrow().values())
				.extracting(FacetResults.ValueCount::value, FacetResults.ValueCount::count)
				.containsExactlyInAnyOrder(
						org.assertj.core.groups.Tuple.tuple("NEW", 1L),
						org.assertj.core.groups.Tuple.tuple("USED", 1L));
		assertThat(facets.dimension("tags").orElseThrow().values())
				.extracting(FacetResults.ValueCount::value)
				.contains("coffee", "kitchen");

		// Suggest: completions while typing, popular products first (weight = views).
		SuggestSearch suggest = SuggestSearch.of(unit, schema);
		assertThat(suggest.suggest("names", "e", 5))
				.extracting(SuggestSearch.Suggestion::text)
				.containsExactly("Espresso Machine", "Electric Kettle"); // 800 views before 40

		// Highlighting: the snippet under each hit, matches marked.
		HighlightSearch highlights = HighlightSearch.of(unit, schema);
		var highlighted = highlights.search(HighlightRequest.over(query).field(description()));
		assertThat(highlighted).hasSize(2);
		assertThat(highlighted.get(0).highlight(schema
				.resolve(catalog.eClass("Product"), description()).name()).orElseThrow())
				.contains("<b>coffee</b>");
	}

	// --- helpers ----------------------------------------------------------------------------

	private EAttribute description() {
		return catalog.attribute("Product", "description");
	}

	private EObject product(String id, String name, String description, String condition,
			long views, List<String> tags) {
		EEnum conditionType = (EEnum) catalog.ePackage().getEClassifier("Condition");
		return catalog.create("Product",
				"id", id, "name", name, "description", description,
				"condition", conditionType.getEEnumLiteral(condition).getInstance(),
				"views", views, "tags", tags);
	}

	private void save(ResourceSet resourceSet, String id, EObject object) throws IOException {
		try (PersistenceResource resource = (PersistenceResource) resourceSet
				.createResource(URI.createURI("lucene://catalog/Product/" + id))) {
			resource.getContents().add(object);
			resource.save(Map.of());
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
