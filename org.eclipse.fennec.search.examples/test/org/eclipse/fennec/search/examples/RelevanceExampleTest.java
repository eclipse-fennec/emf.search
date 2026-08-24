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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.api.Hit;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.query.IndexSearch;
import org.eclipse.fennec.search.query.SearchOptions;
import org.eclipse.fennec.search.similarity.SimilarityRequest;
import org.eclipse.fennec.search.similarity.SimilaritySearch;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The relevance example (docs: example-relevance.md): the same match quality, ordered by
 * what the data knows — a declared rank signal folds view counts into the score — and
 * "more like this" over the corpus's own term statistics. The signal is declared in
 * {@code catalog.esearch} as a sub-field (`views.signal`), so `views` itself stays an
 * ordinary comparable number.
 */
class RelevanceExampleTest {

	private ExampleCatalog catalog;
	private IndexUnit unit;
	private IndexSchema schema;
	private EObject espresso;

	@BeforeEach
	void indexTheCorpus() throws IOException {
		catalog = ExampleCatalog.load();
		unit = IndexUnit.open(IndexUnitConfig.inMemory("catalog")
				.refresh(RefreshTrigger.manual())
				.build());
		schema = IndexSchema.of(catalog.mapping());
		DocumentMapper mapper = DocumentMapper.of(schema);

		// Two products whose descriptions match "coffee" equally well — only the
		// signal separates them — and one similar to the first.
		espresso = product("p-1", "Espresso Machine",
				"A compact espresso machine for fresh coffee", 800);
		EObject grinder = product("p-2", "Coffee Grinder",
				"A manual burr grinder for fresh coffee", 120);
		EObject portable = product("p-3", "Travel Press",
				"A compact espresso machine for travel", 60);
		for (EObject object : List.of(espresso, grinder, portable)) {
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
	void popularProductsRankFirst() throws Exception {
		Query query = QueryBuilder.from(catalog.eClass("Product"))
				.where(path(description()).contains("coffee"))
				.build();
		IndexSearch search = IndexSearch.of(unit, schema);

		// Without the signal the two hits are textual equals; with it, the data decides.
		List<Hit> ranked = search.search(query, null,
				Map.of(SearchOptions.RANK_SIGNALS, List.of("views.signal")));

		assertThat(ranked).extracting(hit -> hit.object().eGet(name()))
				.containsExactly("Espresso Machine", "Coffee Grinder"); // 800 views beat 120
		assertThat(ranked.get(0).score())
				.as("the signal added score, it never decides what matches")
				.isGreaterThan(ranked.get(1).score());
	}

	@Test
	void moreLikeThisFindsTheNeighbour() throws Exception {
		// The anchor must be indexed: similarity reads the corpus's term statistics.
		List<SimilaritySearch.SimilarHit> similar = SimilaritySearch.of(unit, schema)
				.search(SimilarityRequest.to(espresso).field(description()));

		assertThat(similar).extracting(hit -> hit.object().eGet(name()))
				.as("the travel press shares the rare terms (compact, espresso, machine); "
						+ "the grinder only the common ones")
				.startsWith("Travel Press");
	}

	// --- helpers ----------------------------------------------------------------------------

	private EAttribute description() {
		return catalog.attribute("Product", "description");
	}

	private EAttribute name() {
		return catalog.attribute("Product", "name");
	}

	private EObject product(String id, String name, String description, long views) {
		return catalog.create("Product",
				"id", id, "name", name, "description", description, "views", views);
	}
}
