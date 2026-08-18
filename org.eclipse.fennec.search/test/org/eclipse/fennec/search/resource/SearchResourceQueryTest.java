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
package org.eclipse.fennec.search.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.fennec.model.query.builder.Expressions.any;
import static org.eclipse.fennec.model.query.builder.Expressions.param;
import static org.eclipse.fennec.model.query.builder.Expressions.path;
import static org.eclipse.fennec.model.query.builder.Expressions.propertyPath;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.api.QueryResult;
import org.eclipse.fennec.persistence.query.api.QueryShape;
import org.eclipse.fennec.persistence.query.api.QueryableResource;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.TestModels;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The {@link QueryableResource} contract over an index unit — the execution half of the
 * query path: canonical query in, {@code QueryResult} out, in all three shapes. Hits come
 * back through the same three-tier materialization the load path uses (§4.3).
 */
class SearchResourceQueryTest {

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
		resource = new SearchResource(URI.createURI("lucene://catalog/Product"), unit,
				DocumentMapper.of(mapping()));
		save(product("p-1", "Espresso Machine", 499.0, review("r-1", "ada", 5)));
		save(product("p-2", "Grinder", 129.0, review("r-2", "bob", 2)));
		save(product("p-3", "Kettle", 39.0));
		unit.refresh();
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	// --- the three shapes -------------------------------------------------------------------

	@Test
	void anObjectsQueryReturnsReconstructedHits() throws Exception {
		Query query = QueryBuilder.from(product)
				.where(path(price()).gt(100.0))
				.orderByDesc(price())
				.build();

		try (QueryResult result = resource.query(query)) {
			assertThat(result.shape()).isEqualTo(QueryShape.OBJECTS);
			assertThat(result.objects().map(hit -> hit.eGet(name())))
					.containsExactly("Espresso Machine", "Grinder");
		}
	}

	@Test
	void aCountQueryCountsWithoutFetching() throws Exception {
		Query query = QueryBuilder.from(product).where(path(price()).gt(100.0)).countOnly().build();

		try (QueryResult result = resource.query(query)) {
			assertThat(result.shape()).isEqualTo(QueryShape.COUNT);
			assertThat(result.count()).isEqualTo(2);
		}
	}

	@Test
	void aProjectionReturnsTypedRows() throws Exception {
		Query query = QueryBuilder.from(product)
				.selectAs("productName", name())
				.selectAs("price", price())
				.where(path(price()).gt(100.0))
				.orderByDesc(price())
				.build();

		try (QueryResult result = resource.query(query)) {
			assertThat(result.shape()).isEqualTo(QueryShape.PROJECTION);
			List<List<Object>> rows = result.rows()
					.map(row -> List.of(row.get("productName"), row.get(1)))
					.toList();
			assertThat(rows).containsExactly(
					List.of("Espresso Machine", 499.0),
					List.of("Grinder", 129.0));
		}
	}

	// --- paging and quantifiers ---------------------------------------------------------------

	@Test
	void topAndSkipWindowTheSortedResult() throws Exception {
		Query query = QueryBuilder.from(product).orderByDesc(price()).skip(1).top(1).build();

		try (QueryResult result = resource.query(query)) {
			assertThat(result.objects().map(hit -> hit.eGet(name()))).containsExactly("Grinder");
		}
	}

	@Test
	void aQuantifierRunsEndToEndThroughTheResource() throws Exception {
		Query query = QueryBuilder.from(product)
				.where(any(propertyPath(reviews()), it -> it.path(rating()).ge(4)))
				.build();

		try (QueryResult result = resource.query(query)) {
			assertThat(result.objects().map(hit -> hit.eGet(name())))
					.containsExactly("Espresso Machine");
		}
	}

	// --- persisted queries ----------------------------------------------------------------------

	@Test
	void aNamedQueryIsPersistedAndRunsByName() throws Exception {
		Query query = QueryBuilder.from(product).named("expensive")
				.where(path(price()).gt(100.0)).countOnly().build();
		try (QueryResult first = resource.query(query)) {
			assertThat(first.count()).isEqualTo(2);
		}

		try (QueryResult byName = resource.query("expensive", null, null)) {
			assertThat(byName.count()).isEqualTo(2);
		}
	}

	@Test
	void anUnknownQueryNameIsRefusedByName() {
		assertThatThrownBy(() -> resource.query("nobody-saved-this", null, null))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("nobody-saved-this");
	}

	@Test
	void aParameterizedQueryConvertsThroughThePlainConverter() throws Exception {
		// The credo: everything works without OSGi. The context carries a plain-constructed
		// DefaultConverterService, so an Integer parameter lands on a double field converted,
		// not refused.
		Query query = QueryBuilder.from(product)
				.where(path(price()).gt(param("floor")))
				.countOnly()
				.build();

		try (QueryResult result = resource.query(query, Map.of("floor", 100), null)) {
			assertThat(result.count()).isEqualTo(2);
		}
	}

	// --- refusals surface as errors on the resource ----------------------------------------------

	@Test
	void aRejectedQueryRaisesAndRecordsTheReason() {
		Query query = QueryBuilder.from(product)
				.where(path(price()).plus(1).gt(10))
				.build();

		assertThatThrownBy(() -> resource.query(query))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("Query rejected");
		assertThat(resource.getErrors()).isNotEmpty();
	}

	// --- helpers ----------------------------------------------------------------------------------

	private void save(EObject object) throws IOException {
		var mapped = DocumentMapper.of(mapping()).map(object);
		unit.updateDocuments(mapped.term(), mapped.documents());
	}

	private static EObject product(String id, String name, double price, EObject... reviews) {
		EObject object = catalog.create("Product", "id", id, "name", name, "price", price);
		@SuppressWarnings("unchecked")
		List<EObject> children = (List<EObject>) object.eGet(reviews());
		children.addAll(List.of(reviews));
		return object;
	}

	private static EObject review(String id, String author, int rating) {
		return catalog.create("Review", "id", id, "author", author, "rating", rating);
	}

	private static EAttribute price() {
		return (EAttribute) catalog.feature("Product", "price");
	}

	private static EAttribute name() {
		return (EAttribute) catalog.feature("Product", "name");
	}

	private static EAttribute rating() {
		return (EAttribute) catalog.feature("Review", "rating");
	}

	private static EReference reviews() {
		return (EReference) catalog.feature("Product", "reviews");
	}

	/** Conventions, plus a sortable keyword name and reviews as a block. */
	private static IndexUnitMapping mapping() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog.ePackage());
		DocumentMapping document = ESEARCH.createDocumentMapping();
		document.setEClass(product);
		KeywordFieldMapping name = ESEARCH.createKeywordFieldMapping();
		name.setFeature(name());
		name.setDocValues(true);
		document.getFields().add(name);
		ReferenceMapping reviews = ESEARCH.createReferenceMapping();
		reviews.setEReference(reviews());
		reviews.setStrategy(ReferenceStrategy.NESTED);
		document.getReferences().add(reviews);
		mapping.getDocuments().add(document);
		return mapping;
	}
}
