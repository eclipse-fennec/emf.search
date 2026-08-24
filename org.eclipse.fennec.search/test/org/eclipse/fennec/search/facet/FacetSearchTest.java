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
package org.eclipse.fennec.search.facet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.fennec.model.query.builder.Expressions.path;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.api.QueryResult;
import org.eclipse.fennec.persistence.query.api.QueryResultRow;
import org.eclipse.fennec.persistence.query.api.QueryShape;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.FacetKind;
import org.eclipse.fennec.search.esearch.FacetMapping;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.facet.FacetResults.ValueCount;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappingException;
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
 * The facet API (S7, #11): counts over the match set of a canonical query, from SortedSet
 * doc values the mapper wrote — objects, never block children — with drill-down narrowing
 * the base, and refusals by name for everything the mapping did not declare.
 */
class FacetSearchTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;
	private static EClass product;

	private IndexUnit unit;
	private IndexSchema schema;
	private FacetSearch facets;

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
		schema = IndexSchema.of(mapping());
		DocumentMapper mapper = DocumentMapper.of(schema);
		facets = FacetSearch.of(unit, schema);
		index(mapper, product("p-1", 499.0, "NEW", List.of("coffee", "kitchen"),
				review("r-1", "ada"), review("r-2", "bob")));
		index(mapper, product("p-2", 129.0, "NEW", List.of("coffee")));
		index(mapper, product("p-3", 39.0, "USED", List.of("clearance")));
		unit.refresh();
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	// --- counting -------------------------------------------------------------------------

	@Test
	void countsObjectsPerValueOverTheWholeType() throws Exception {
		FacetResults results = facets.count(FacetRequest.over(all()).dimension("condition"));

		assertThat(results.dimension("condition").orElseThrow().values())
				.containsExactly(new ValueCount("NEW", 2), new ValueCount("USED", 1));
	}

	@Test
	void theBaseQueryNarrowsWhatIsCounted() throws Exception {
		Query expensive = QueryBuilder.from(product).where(path(price()).gt(100.0)).build();

		FacetResults results = facets.count(FacetRequest.over(expensive).dimension("condition"));

		assertThat(results.dimension("condition").orElseThrow().values())
				.containsExactly(new ValueCount("NEW", 2));
	}

	@Test
	void aMultiValuedDimensionCountsEveryValue() throws Exception {
		FacetResults results = facets.count(FacetRequest.over(all()).dimension("tags"));

		assertThat(results.dimension("tags").orElseThrow().values()).containsExactly(
				new ValueCount("coffee", 2), new ValueCount("clearance", 1),
				new ValueCount("kitchen", 1));
	}

	@Test
	void drillDownNarrowsTheOtherDimension() throws Exception {
		FacetResults results = facets.count(FacetRequest.over(all())
				.dimension("tags")
				.drillDown("condition", "USED"));

		assertThat(results.dimension("tags").orElseThrow().values())
				.containsExactly(new ValueCount("clearance", 1));
	}

	@Test
	void nestedChildrenNeverInflateACount() throws Exception {
		// p-1 wrote three documents (two reviews plus the root); condition still counts one.
		FacetResults results = facets.count(FacetRequest.over(all()).dimension("condition"));

		assertThat(results.dimension("condition").orElseThrow().values().stream()
				.mapToLong(ValueCount::count).sum()).isEqualTo(3);
	}

	@Test
	void anEmptyIndexHasEmptyCountsNotAnError() throws Exception {
		try (IndexUnit empty = IndexUnit.open(IndexUnitConfig.inMemory("catalog").build())) {
			FacetResults results = FacetSearch.of(empty, schema)
					.count(FacetRequest.over(all()).dimension("condition"));

			assertThat(results.dimension("condition").orElseThrow().values()).isEmpty();
		}
	}

	@Test
	void theGroupBySubsetAnswersThroughTheQueryPath() throws Exception {
		// The honest IR half of #11: one group key with a facet declaration, one COUNT —
		// the QueryableResource answers it from the same SortedSet doc values.
		try (SearchResource resource = new SearchResource(
				URI.createURI("lucene://catalog/Product"), unit, DocumentMapper.of(schema))) {
			Query query = QueryBuilder.from(product)
					.groupBy(catalog.feature("Product", "condition"))
					.countOf("n")
					.build();

			try (QueryResult result = resource.query(query);
					Stream<QueryResultRow> resultRows = result.rows()) {
				assertThat(result.shape()).isEqualTo(QueryShape.AGGREGATION);
				List<QueryResultRow> rows = resultRows.toList();
				assertThat(rows).hasSize(2);
				assertThat(rows.get(0).get("condition").toString()).isEqualTo("NEW");
				assertThat(rows.get(0).get("n")).isEqualTo(2L);
				assertThat(rows.get(1).get("condition").toString()).isEqualTo("USED");
				assertThat(rows.get(1).get("n")).isEqualTo(1L);
			}
		}
	}

	// --- refusals -------------------------------------------------------------------------

	@Test
	void anUndeclaredDimensionIsRefusedNamingTheDeclared() {
		assertThatThrownBy(() -> facets.count(FacetRequest.over(all()).dimension("brand")))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("brand")
				.hasMessageContaining("condition");
	}

	@Test
	void aRequestWithoutDimensionsIsRefused() {
		assertThatThrownBy(() -> facets.count(FacetRequest.over(all())))
				.hasMessageContaining("no dimension");
	}

	@Test
	void aMappingWithoutFacetsRefusesTheApiUpFront() {
		IndexUnitMapping bare = ESEARCH.createIndexUnitMapping();
		bare.setName("catalog");
		bare.setEPackage(catalog.ePackage());

		assertThatThrownBy(() -> FacetSearch.of(unit, IndexSchema.of(bare)))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("no facet dimension");
	}

	@Test
	void aTaxonomyDeclarationIsRefusedByName() {
		IndexUnitMapping mapping = mapping();
		((KeywordFieldMapping) mapping.getDocuments().get(0).getFields().get(0)).getFacet()
				.setKind(FacetKind.TAXONOMY);

		assertThatThrownBy(() -> DocumentMapper.of(mapping))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("TAXONOMY");
	}

	// --- helpers --------------------------------------------------------------------------

	private static Query all() {
		return QueryBuilder.from(product).build();
	}

	private void index(DocumentMapper mapper, EObject object) throws IOException {
		var mapped = mapper.map(object);
		unit.updateDocuments(mapped.term(), mapped.documents());
	}

	private EObject product(String id, double price, String condition, List<String> tags,
			EObject... reviews) {
		EEnum conditionType = (EEnum) catalog.ePackage().getEClassifier("Condition");
		EObject object = catalog.create("Product", "id", id, "name", id, "price", price,
				"condition", conditionType.getEEnumLiteral(condition).getInstance());
		@SuppressWarnings("unchecked")
		List<String> tagValues = (List<String>) object.eGet(catalog.feature("Product", "tags"));
		tagValues.addAll(tags);
		@SuppressWarnings("unchecked")
		List<EObject> children = (List<EObject>) object.eGet(catalog.feature("Product", "reviews"));
		children.addAll(List.of(reviews));
		return object;
	}

	private EObject review(String id, String author) {
		return catalog.create("Review", "id", id, "author", author);
	}

	private static EAttribute price() {
		return (EAttribute) catalog.feature("Product", "price");
	}

	/** Conventions plus: condition and tags carry facet dimensions, reviews are a block. */
	private static IndexUnitMapping mapping() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog.ePackage());
		DocumentMapping document = ESEARCH.createDocumentMapping();
		document.setEClass(product);
		KeywordFieldMapping condition = ESEARCH.createKeywordFieldMapping();
		condition.setFeature((EAttribute) catalog.feature("Product", "condition"));
		condition.setDocValues(true);
		FacetMapping conditionFacet = ESEARCH.createFacetMapping();
		condition.setFacet(conditionFacet);
		document.getFields().add(condition);
		KeywordFieldMapping tags = ESEARCH.createKeywordFieldMapping();
		tags.setFeature((EAttribute) catalog.feature("Product", "tags"));
		FacetMapping tagsFacet = ESEARCH.createFacetMapping();
		tagsFacet.setMultiValued(true);
		tags.setFacet(tagsFacet);
		document.getFields().add(tags);
		ReferenceMapping reviews = ESEARCH.createReferenceMapping();
		reviews.setEReference((EReference) catalog.feature("Product", "reviews"));
		reviews.setStrategy(ReferenceStrategy.NESTED);
		document.getReferences().add(reviews);
		mapping.getDocuments().add(document);
		return mapping;
	}
}
