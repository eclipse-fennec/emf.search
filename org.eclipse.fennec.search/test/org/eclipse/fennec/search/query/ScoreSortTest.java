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
package org.eclipse.fennec.search.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.fennec.model.query.builder.Expressions.path;
import static org.eclipse.fennec.model.query.builder.Expressions.score;

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.api.QueryResult;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.TestModels;
import org.eclipse.fennec.search.resource.SearchResource;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Relevance ordering (S6, #10): {@code orderBy(score())} sorts by the predicate's
 * relevance. The assertions are strictly <b>ordinal</b> — a higher-scoring document sorts
 * before a lower one on a corpus constructed to make the order unambiguous (same field
 * length, different term frequency) — because absolute score values are not a contract.
 */
class ScoreSortTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;
	private static EClass product;

	private IndexUnit unit;
	private SearchResource resource;

	@BeforeAll
	static void loadModel() {
		catalog = TestModels.load("catalog.ecore");
		product = catalog.eClass("Product");
	}

	@BeforeEach
	void indexCorpus() throws IOException {
		unit = IndexUnit.open(IndexUnitConfig.inMemory("catalog")
				.refresh(RefreshTrigger.manual())
				.build());
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog.ePackage());
		DocumentMapper mapper = DocumentMapper.of(mapping);
		resource = new SearchResource(URI.createURI("lucene://catalog/Product"), unit, mapper);
		// Same description length, different term frequency: the BM25 order is unambiguous.
		index(mapper, catalog.create("Product", "id", "double", "name", "double",
				"description", "coffee coffee kitchen"));
		index(mapper, catalog.create("Product", "id", "single", "name", "single",
				"description", "coffee grinder kitchen"));
		index(mapper, catalog.create("Product", "id", "none", "name", "none",
				"description", "tea pot"));
		unit.refresh();
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	@Test
	void descendingScoreSortsTheBetterMatchFirst() throws Exception {
		Query query = QueryBuilder.from(product)
				.where(path(description()).contains("coffee"))
				.orderByDesc(score().toExpression())
				.build();

		assertThat(names(query)).containsExactly("double", "single");
	}

	@Test
	void ascendingScoreInvertsTheOrder() throws Exception {
		Query query = QueryBuilder.from(product)
				.where(path(description()).contains("coffee"))
				.orderByAsc(score().toExpression())
				.build();

		assertThat(names(query)).containsExactly("single", "double");
	}

	@Test
	void aScoreSortValidatesClean() {
		Query query = QueryBuilder.from(product)
				.where(path(description()).contains("coffee"))
				.orderByDesc(score().toExpression())
				.build();
		LuceneQueryProcessor processor = LuceneQueryProcessor.of(
				IndexSchema.of(unitMapping()), null);

		assertThat(processor.validate(query, product).getSeverity()).isLessThan(Diagnostic.ERROR);
	}

	@Test
	void withScoresDeliversRankedHitsAndTheScoreView() throws Exception {
		Query query = QueryBuilder.from(product)
				.where(path(description()).contains("coffee"))
				.withScores()
				.build();

		try (var result = resource.query(query)) {
			var hits = result.hits().toList();
			assertThat(hits).extracting(hit -> hit.object().eGet(
					hit.object().eClass().getEStructuralFeature("name")))
					.as("without an explicit sort, iteration order is rank order")
					.containsExactly("double", "single");
			assertThat(hits.get(0).score()).isGreaterThan(hits.get(1).score());
			assertThat(result.scores())
					.as("the metadata view is complete and keyed by id")
					.containsKeys("double", "single");
		}
	}

	@Test
	void hitsWithoutTheFlagAreAHardAccessor() throws Exception {
		Query query = QueryBuilder.from(product)
				.where(path(description()).contains("coffee"))
				.build();

		try (var result = resource.query(query)) {
			assertThatThrownBy(result::hits).isInstanceOf(IllegalStateException.class);
			assertThat(result.scores()).as("the soft accessor is empty, never throwing").isEmpty();
		}
	}

	@Test
	void withScoresOnACountShapeIsRefused() {
		Query query = QueryBuilder.from(product).withScores().countOnly().build();

		assertThatThrownBy(() -> resource.query(query))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("withScores");
	}

	@Test
	void anyOtherSortExpressionIsRefusedByDeclaration() {
		// Since emf.persistence-jpa#165 a bare score key classifies as SCORE, so
		// SORT_EXPRESSION is honestly undeclared again — the validator refuses any other
		// key expression by feature name before translation would name score() as the way.
		Query query = QueryBuilder.from(product)
				.orderByDesc(path(price()).plus(1).toExpression())
				.build();

		assertThatThrownBy(() -> resource.query(query))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("SORT_EXPRESSION");
	}

	@Test
	void scoreAsAPredicateIsRefusedAsFalsePrecision() {
		Query query = QueryBuilder.from(product)
				.where(score().gt(0.5))
				.build();

		assertThatThrownBy(() -> resource.query(query))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("sort key");
	}

	// --- helpers --------------------------------------------------------------------------

	private List<Object> names(Query query) throws Exception {
		try (QueryResult result = resource.query(query)) {
			return result.objects()
					.map(hit -> hit.eGet(hit.eClass().getEStructuralFeature("name")))
					.toList();
		}
	}

	private void index(DocumentMapper mapper, EObject object) throws IOException {
		var mapped = mapper.map(object);
		unit.updateDocuments(mapped.term(), mapped.documents());
	}

	private static IndexUnitMapping unitMapping() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog.ePackage());
		return mapping;
	}

	private static EAttribute description() {
		return (EAttribute) catalog.feature("Product", "description");
	}

	private static EAttribute price() {
		return (EAttribute) catalog.feature("Product", "price");
	}
}
