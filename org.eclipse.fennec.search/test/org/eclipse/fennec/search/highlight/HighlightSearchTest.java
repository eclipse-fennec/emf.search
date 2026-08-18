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
package org.eclipse.fennec.search.highlight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.fennec.model.query.builder.Expressions.and;
import static org.eclipse.fennec.model.query.builder.Expressions.path;

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.esearch.TextFieldMapping;
import org.eclipse.fennec.search.highlight.HighlightSearch.HighlightedHit;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.TestModels;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The highlight API (S12, #14): passages fragmented around the executed query's terms, on
 * hits in rank order, with the object reconstructed like every other read. Snippets carry
 * Lucene's default markers ({@code <b>…</b>}); fields the mapping cannot honestly
 * highlight — keywords, unstored text — are refused by name.
 */
class HighlightSearchTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;
	private static EClass product;

	private IndexUnit unit;
	private IndexSchema schema;
	private HighlightSearch highlights;

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
		IndexUnitMapping mapping = conventionMapping();
		schema = IndexSchema.of(mapping);
		DocumentMapper mapper = DocumentMapper.of(schema);
		highlights = HighlightSearch.of(unit, schema);
		// Same field length, different term frequency: the rank order is unambiguous.
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

	// --- passages ---------------------------------------------------------------------------

	@Test
	void snippetsWrapTheMatchedTerms() throws Exception {
		List<HighlightedHit> hits = highlights.search(HighlightRequest
				.over(match("coffee"))
				.field(description()));

		assertThat(hits).hasSize(2);
		assertThat(hits.get(0).highlight("description")).hasValueSatisfying(
				snippet -> assertThat(snippet).contains("<b>coffee</b>"));
	}

	@Test
	void hitsComeInRankOrderWithScores() throws Exception {
		List<HighlightedHit> hits = highlights.search(HighlightRequest
				.over(match("coffee"))
				.field(description()));

		assertThat(hits).extracting(hit -> name(hit.object())).containsExactly("double", "single");
		assertThat(hits.get(0).score()).isGreaterThan(hits.get(1).score());
	}

	@Test
	void aFieldTheQueryDidNotMatchInGetsNoSnippet() throws Exception {
		List<HighlightedHit> hits = highlights.search(HighlightRequest
				.over(match("coffee"))
				.field(description())
				.field(attribute("name")));

		assertThat(hits.get(0).highlight("description")).isPresent();
		assertThat(hits.get(0).highlight("name"))
				.as("the query matched in description, not in name")
				.isEmpty();
	}

	@Test
	void maxPassagesJoinsMoreOfTheField() throws Exception {
		DocumentMapper mapper = DocumentMapper.of(schema);
		index(mapper, catalog.create("Product", "id", "long", "name", "long", "description",
				"Coffee is the first sentence. Tea sits in the middle here. Coffee closes it."));
		unit.refresh();
		Query query = QueryBuilder.from(product)
				.where(and(path(description()).contains("coffee"), path(attribute("id")).eq("long")))
				.build();

		String one = highlights.search(HighlightRequest.over(query).field(description()))
				.get(0).highlight("description").orElseThrow();
		String two = highlights.search(HighlightRequest.over(query).field(description()).maxPassages(2))
				.get(0).highlight("description").orElseThrow();

		// The highlighter picks the best passage, not the first — so count, don't order.
		assertThat(one.split("<b>Coffee</b>", -1)).hasSize(2);
		assertThat(two.split("<b>Coffee</b>", -1)).hasSize(3);
	}

	@Test
	void skipAndTopWindowTheHits() throws Exception {
		Query query = QueryBuilder.from(product)
				.where(path(description()).contains("coffee"))
				.skip(1).top(1)
				.build();

		List<HighlightedHit> hits = highlights.search(HighlightRequest
				.over(query)
				.field(description()));

		assertThat(hits).extracting(hit -> name(hit.object())).containsExactly("single");
	}

	@Test
	void theHitObjectIsReconstructedWithItsChildren() throws Exception {
		DocumentMapper mapper = DocumentMapper.of(schema);
		EObject reviewed = catalog.create("Product", "id", "reviewed", "name", "reviewed",
				"description", "espresso machine");
		@SuppressWarnings("unchecked")
		List<EObject> reviews = (List<EObject>) reviewed.eGet(catalog.feature("Product", "reviews"));
		reviews.add(catalog.create("Review", "id", "r-1", "author", "ada"));
		index(mapper, reviewed);
		unit.refresh();

		List<HighlightedHit> hits = highlights.search(HighlightRequest
				.over(match("espresso"))
				.field(description()));

		assertThat(hits).hasSize(1);
		@SuppressWarnings("unchecked")
		List<EObject> readReviews = (List<EObject>) hits.get(0).object()
				.eGet(catalog.feature("Product", "reviews"));
		assertThat(readReviews).as("hits reconstruct like every other read").hasSize(1);
	}

	// --- refusals ---------------------------------------------------------------------------

	@Test
	void aKeywordFieldIsRefusedByName() {
		IndexUnitMapping mapping = conventionMapping();
		KeywordFieldMapping keyword = ESEARCH.createKeywordFieldMapping();
		keyword.setFeature(attribute("name"));
		mapping.getDocuments().get(0).getFields().add(keyword);
		HighlightSearch keyed = HighlightSearch.of(unit, IndexSchema.of(mapping));

		assertThatThrownBy(() -> keyed.search(HighlightRequest
				.over(match("coffee"))
				.field(attribute("name"))))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("not analyzed text");
	}

	@Test
	void anUnstoredTextFieldIsRefusedWithTheWayOut() {
		IndexUnitMapping mapping = conventionMapping();
		TextFieldMapping unstored = ESEARCH.createTextFieldMapping();
		unstored.setFeature(description());
		unstored.setStored(false);
		mapping.getDocuments().get(0).getFields().add(unstored);
		HighlightSearch bare = HighlightSearch.of(unit, IndexSchema.of(mapping));

		assertThatThrownBy(() -> bare.search(HighlightRequest
				.over(match("coffee"))
				.field(description())))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("stored=false");
	}

	@Test
	void aRequestWithoutFieldsIsRefused() {
		assertThatThrownBy(() -> highlights.search(HighlightRequest.over(match("coffee"))))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("no field");
	}

	@Test
	void aProjectionQueryIsRefused() {
		Query projection = QueryBuilder.from(product)
				.select(attribute("name"))
				.where(path(description()).contains("coffee"))
				.build();

		assertThatThrownBy(() -> highlights.search(HighlightRequest
				.over(projection)
				.field(description())))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("PROJECTION");
	}

	// --- helpers ----------------------------------------------------------------------------

	private static Query match(String term) {
		return QueryBuilder.from(product).where(path(description()).contains(term)).build();
	}

	private void index(DocumentMapper mapper, EObject object) throws IOException {
		var mapped = mapper.map(object);
		unit.updateDocuments(mapped.term(), mapped.documents());
	}

	private static String name(EObject object) {
		return (String) object.eGet(object.eClass().getEStructuralFeature("name"));
	}

	private static EAttribute description() {
		return attribute("description");
	}

	private static EAttribute attribute(String name) {
		return (EAttribute) catalog.feature("Product", name);
	}

	/** Convention only, plus reviews as a NESTED block for the reconstruction case. */
	private static IndexUnitMapping conventionMapping() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog.ePackage());
		DocumentMapping document = ESEARCH.createDocumentMapping();
		document.setEClass(product);
		ReferenceMapping reviews = ESEARCH.createReferenceMapping();
		reviews.setEReference((EReference) catalog.feature("Product", "reviews"));
		reviews.setStrategy(ReferenceStrategy.NESTED);
		document.getReferences().add(reviews);
		mapping.getDocuments().add(document);
		return mapping;
	}
}
