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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistries;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.api.Hit;
import org.eclipse.fennec.persistence.query.support.RegistryNamedOperations;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.Materialization;
import org.eclipse.fennec.search.esearch.MaterializationKind;
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
 * The direct search API (#41): hits as objects, no resource mechanics — and the three
 * refusals that keep its promise small (OBJECTS only, a root type is required, a named
 * query needs a catalog). The primary-store cases pin the secondary-index deployment:
 * {@code SOURCE_URI} hits resolve through the attached store in one batch, and without
 * one the proxy is the honest answer.
 */
class IndexSearchTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;
	private static EClass product;

	private IndexUnit unit;

	@BeforeAll
	static void loadModel() {
		catalog = TestModels.load("catalog.ecore");
		product = catalog.eClass("Product");
	}

	@BeforeEach
	void openUnit() throws IOException {
		unit = IndexUnit.open(IndexUnitConfig.inMemory("catalog")
				.refresh(RefreshTrigger.manual())
				.build());
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	// --- hits without resource mechanics ----------------------------------------------------

	@Test
	void searchAnswersScoredHitsWithoutAResource() throws Exception {
		IndexSchema schema = index(mapping(),
				product("p-1", "Espresso Machine", 499.0),
				product("p-2", "Grinder", 129.0),
				product("p-3", "Kettle", 39.0));

		List<Hit> hits = IndexSearch.of(unit, schema)
				.search(QueryBuilder.from(product).where(path(price()).gt(100.0)).build());

		assertThat(hits).hasSize(2);
		assertThat(hits).allSatisfy(hit -> {
			assertThat(hit.object().eResource()).as("a hit is a plain object, owned by nobody").isNull();
			assertThat(hit.score()).isGreaterThan(0.0);
		});
	}

	@Test
	void anExplicitSortStillFillsTheScores() throws Exception {
		IndexSchema schema = index(mapping(),
				product("p-1", "Espresso Machine", 499.0),
				product("p-2", "Grinder", 129.0));

		List<Hit> hits = IndexSearch.of(unit, schema)
				.search(QueryBuilder.from(product).orderByDesc(price()).build());

		assertThat(hits).extracting(hit -> hit.object().eGet(name()))
				.containsExactly("Espresso Machine", "Grinder");
		// A field sort reads no scores by itself; the API asks for them (doDocScores),
		// because the Hit carrier promises one.
		assertThat(hits).allSatisfy(hit -> assertThat(hit.score()).isGreaterThan(0.0));
	}

	// --- the refusals that keep the promise small --------------------------------------------

	@Test
	void aShapeWithoutHitsIsRefused() throws Exception {
		IndexSchema schema = index(mapping(), product("p-1", "Espresso Machine", 499.0));

		assertThatThrownBy(() -> IndexSearch.of(unit, schema)
				.search(QueryBuilder.from(product).countOnly().build()))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("QueryableResource");
	}

	@Test
	void aQueryWithoutARootTypeIsRefused() throws Exception {
		IndexSchema schema = index(mapping(), product("p-1", "Espresso Machine", 499.0));
		// A deserialized query can arrive without a from; the builder refuses one upfront.
		Query query = QueryBuilder.from(product).build();
		query.setFrom(null);

		assertThatThrownBy(() -> IndexSearch.of(unit, schema).search(query))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("root type");
	}

	@Test
	void anUndeclaredFeatureIsRefusedWithTheDiagnostic() throws Exception {
		IndexSchema schema = index(mapping(), product("p-1", "Espresso Machine", 499.0));

		assertThatThrownBy(() -> IndexSearch.of(unit, schema)
				.search(QueryBuilder.from(product).where(path(price()).plus(1).gt(10)).build()))
				.isInstanceOf(QueryException.class)
				.satisfies(thrown -> assertThat(((QueryException) thrown).getDiagnostic())
						.as("the refusal carries the Diagnostic naming the construct").isNotNull());
	}

	// --- the named-query catalog --------------------------------------------------------------

	@Test
	void aNamedQueryRunsFromTheAttachedCatalog() throws Exception {
		IndexSchema schema = index(mapping(),
				product("p-1", "Espresso Machine", 499.0),
				product("p-2", "Grinder", 129.0));
		RegistryNamedOperations queries = new RegistryNamedOperations(
				EObjectRegistries.createRegistry("queries"));
		queries.store("expensive", QueryBuilder.from(product).where(path(price()).gt(100.0)).build());

		List<Hit> hits = IndexSearch.of(unit, schema).withCatalog(queries)
				.search("expensive", null, null);

		assertThat(hits).hasSize(2);
	}

	@Test
	void aNameWithoutACatalogIsRefused() throws Exception {
		IndexSchema schema = index(mapping(), product("p-1", "Espresso Machine", 499.0));

		assertThatThrownBy(() -> IndexSearch.of(unit, schema).search("expensive", null, null))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("withCatalog");
	}

	// --- the primary store ----------------------------------------------------------------------

	@Test
	void sourceUriHitsResolveThroughThePrimaryStoreInOneBatch() throws Exception {
		Resource primary = new ResourceImpl(URI.createURI("mongodb://demo/Product"));
		EObject machine = product("p-1", "Espresso Machine", 499.0);
		EObject grinder = product("p-2", "Grinder", 129.0);
		primary.getContents().add(machine);
		primary.getContents().add(grinder);
		IndexSchema schema = index(sourceUriMapping(), machine, grinder);
		List<List<URI>> batches = new ArrayList<>();
		PrimaryStore store = uris -> {
			batches.add(uris);
			Map<URI, EObject> resolved = new LinkedHashMap<>();
			for (EObject original : List.of(machine, grinder)) {
				resolved.put(EcoreUtil.getURI(original), original);
			}
			return resolved;
		};

		List<Hit> hits = IndexSearch.of(unit, schema).withPrimaryStore(store)
				.search(QueryBuilder.from(product).where(path(price()).gt(10.0)).build());

		assertThat(hits).extracting(Hit::object)
				.as("the hits are the originals, not reconstructions")
				.containsExactlyInAnyOrder(machine, grinder);
		assertThat(batches).as("one window, one batch — never one call per hit").hasSize(1);
		assertThat(batches.get(0)).hasSize(2);
	}

	@Test
	void withoutAPrimaryStoreASourceUriHitStaysAProxy() throws Exception {
		Resource primary = new ResourceImpl(URI.createURI("mongodb://demo/Product"));
		EObject machine = product("p-1", "Espresso Machine", 499.0);
		primary.getContents().add(machine);
		IndexSchema schema = index(sourceUriMapping(), machine);

		List<Hit> hits = IndexSearch.of(unit, schema)
				.search(QueryBuilder.from(product).where(path(price()).gt(10.0)).build());

		assertThat(hits).hasSize(1);
		EObject hit = hits.get(0).object();
		assertThat(hit.eIsProxy()).as("without a store, the proxy is the honest answer").isTrue();
		assertThat(((InternalEObject) hit).eProxyURI()).isEqualTo(EcoreUtil.getURI(machine));
	}

	@Test
	void aUriTheStoreCannotAnswerKeepsItsProxy() throws Exception {
		Resource primary = new ResourceImpl(URI.createURI("mongodb://demo/Product"));
		EObject machine = product("p-1", "Espresso Machine", 499.0);
		primary.getContents().add(machine);
		IndexSchema schema = index(sourceUriMapping(), machine);
		PrimaryStore empty = uris -> Map.of();

		List<Hit> hits = IndexSearch.of(unit, schema).withPrimaryStore(empty)
				.search(QueryBuilder.from(product).where(path(price()).gt(10.0)).build());

		assertThat(hits).hasSize(1);
		assertThat(hits.get(0).object().eIsProxy())
				.as("an unanswered URI keeps the proxy — the hit carries what the index knows")
				.isTrue();
	}

	// --- helpers ----------------------------------------------------------------------------------

	private IndexSchema index(IndexUnitMapping mapping, EObject... objects) throws IOException {
		IndexSchema schema = IndexSchema.of(mapping);
		DocumentMapper mapper = DocumentMapper.of(schema);
		for (EObject object : objects) {
			var mapped = mapper.map(object);
			unit.updateDocuments(mapped.term(), mapped.documents());
		}
		unit.refresh();
		return schema;
	}

	private static EObject product(String id, String name, double price) {
		return catalog.create("Product", "id", id, "name", name, "price", price);
	}

	private static EAttribute price() {
		return (EAttribute) catalog.feature("Product", "price");
	}

	private static EAttribute name() {
		return (EAttribute) catalog.feature("Product", "name");
	}

	/** Conventions, plus a keyword name with doc values (the sort cases read it). */
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
		mapping.getDocuments().add(document);
		return mapping;
	}

	/** As {@link #mapping()}, with Product declared SOURCE_URI — the secondary-index tier. */
	private static IndexUnitMapping sourceUriMapping() {
		IndexUnitMapping mapping = mapping();
		Materialization materialization = ESEARCH.createMaterialization();
		materialization.setKind(MaterializationKind.SOURCE_URI);
		mapping.getDocuments().get(0).setMaterialization(materialization);
		return mapping;
	}
}
