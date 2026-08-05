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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.persistence.resource.PersistenceResource;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The {@link PersistenceResource} contract over an index unit: what save, delete, count and
 * exist do, and what load honestly cannot do yet.
 */
class SearchResourceTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static EPackage catalog;

	private IndexUnit unit;
	private ResourceSet resourceSet;

	@BeforeAll
	static void loadModel() throws IOException {
		ResourceSet loader = new ResourceSetImpl();
		loader.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put("ecore", new XMIResourceFactoryImpl());
		try (InputStream stream = SearchResourceTest.class
				.getResourceAsStream("/org/eclipse/fennec/search/mapping/catalog.ecore")) {
			Resource resource = loader.createResource(URI.createURI("test:/catalog.ecore"));
			resource.load(stream, null);
			catalog = (EPackage) resource.getContents().get(0);
		}
	}

	@BeforeEach
	void openUnit() throws IOException {
		unit = IndexUnit.open(IndexUnitConfig.inMemory("catalog")
				.refresh(RefreshTrigger.manual())
				.build());
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap()
				.put(SearchUris.SCHEME, new SearchResourceFactory(unit, mapper(false)));
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	private static DocumentMapper mapper(boolean nestReviews) {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog);
		if (nestReviews) {
			DocumentMapping product = ESEARCH.createDocumentMapping();
			product.setEClass((EClass) catalog.getEClassifier("Product"));
			ReferenceMapping reviews = ESEARCH.createReferenceMapping();
			reviews.setEReference((EReference) product.getEClass().getEStructuralFeature("reviews"));
			reviews.setStrategy(ReferenceStrategy.NESTED);
			product.getReferences().add(reviews);
			mapping.getDocuments().add(product);
		}
		return DocumentMapper.of(mapping);
	}

	private static EObject product(String id, String name) {
		EClass eClass = (EClass) catalog.getEClassifier("Product");
		EObject object = EcoreUtil.create(eClass);
		object.eSet(eClass.getEStructuralFeature("id"), id);
		object.eSet(eClass.getEStructuralFeature("name"), name);
		return object;
	}

	private static EObject review(String id, String author) {
		EClass eClass = (EClass) catalog.getEClassifier("Review");
		EObject object = EcoreUtil.create(eClass);
		object.eSet(eClass.getEStructuralFeature("id"), id);
		object.eSet(eClass.getEStructuralFeature("author"), author);
		return object;
	}

	private PersistenceResource resource(String uri) {
		return (PersistenceResource) resourceSet.createResource(URI.createURI(uri));
	}

	// --- writing ------------------------------------------------------------------------

	@Test
	void savingWritesTheObjectAndCountFindsIt() throws Exception {
		PersistenceResource resource = resource("lucene://catalog/Product/p-1");
		resource.getContents().add(product("p-1", "Espresso Machine"));

		resource.save(Map.of());
		unit.refresh();

		assertThat(resource.count()).isEqualTo(1);
		assertThat(resource.exist()).isTrue();
	}

	@Test
	void savingTwiceReplacesRatherThanDuplicates() throws Exception {
		PersistenceResource resource = resource("lucene://catalog/Product/p-1");
		resource.getContents().add(product("p-1", "first"));
		resource.save(Map.of());

		resource.getContents().clear();
		resource.getContents().add(product("p-1", "second"));
		resource.save(Map.of());
		unit.refresh();

		assertThat(resource.count()).as("one object, not two").isEqualTo(1);
	}

	@Test
	void deletingByUriRemovesTheObject() throws Exception {
		PersistenceResource resource = resource("lucene://catalog/Product/p-1");
		resource.getContents().add(product("p-1", "Espresso Machine"));
		resource.save(Map.of());
		unit.refresh();

		resource.delete(Map.of());
		unit.refresh();

		assertThat(resource.exist()).isFalse();
	}

	@Test
	void deletingWithoutAnIdAndWithoutContentsIsRefused() {
		PersistenceResource resource = resource("lucene://catalog/Product");

		assertThatThrownBy(() -> resource.delete(Map.of()))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("nothing to delete");
	}

	// --- counting -----------------------------------------------------------------------

	@Test
	void countCountsObjectsNotDocuments() throws Exception {
		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap()
				.put(SearchUris.SCHEME, new SearchResourceFactory(unit, mapper(true)));

		EObject withReviews = product("p-1", "Espresso Machine");
		EClass productClass = (EClass) catalog.getEClassifier("Product");
		@SuppressWarnings("unchecked")
		List<EObject> reviews = (List<EObject>) withReviews
				.eGet((EStructuralFeature) productClass.getEStructuralFeature("reviews"));
		reviews.add(review("r-1", "ada"));
		reviews.add(review("r-2", "linus"));

		PersistenceResource resource = resource("lucene://catalog/Product/p-1");
		resource.getContents().add(withReviews);
		resource.save(Map.of());
		unit.refresh();

		// Three documents were written; one object was saved. Counting documents here
		// would be off by the number of nested children — silently, and only for models
		// that use NESTED.
		assertThat(resource.count()).isEqualTo(1);
	}

	@Test
	void countOverATypeCountsEveryObjectOfThatType() throws Exception {
		PersistenceResource first = resource("lucene://catalog/Product/p-1");
		first.getContents().add(product("p-1", "one"));
		first.save(Map.of());
		PersistenceResource second = resource("lucene://catalog/Product/p-2");
		second.getContents().add(product("p-2", "two"));
		second.save(Map.of());
		unit.refresh();

		assertThat(resource("lucene://catalog/Product").count()).isEqualTo(2);
		assertThat(resource("lucene://catalog/Product/p-1").count()).isEqualTo(1);
		assertThat(resource("lucene://catalog/Manufacturer").count()).as("a type nobody wrote").isZero();
	}

	// --- loading ------------------------------------------------------------------------

	@Test
	void loadingRefusesWithTheReasonRatherThanReturningAHalfObject() throws Exception {
		PersistenceResource resource = resource("lucene://catalog/Product/p-1");
		resource.getContents().add(product("p-1", "Espresso Machine"));
		resource.save(Map.of());
		unit.refresh();

		assertThatThrownBy(() -> resource("lucene://catalog/Product/p-1").load(Map.of()))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("not to rebuild it")
				.hasMessageContaining("#18");
	}

	// --- URIs ---------------------------------------------------------------------------

	@Test
	void theUriShapeIsParsedIntoUnitTypeAndId() {
		SearchUris address = SearchUris.parse(URI.createURI("lucene://catalog/Product/p-1"));

		assertThat(address.unit()).isEqualTo("catalog");
		assertThat(address.type()).isEqualTo("Product");
		assertThat(address.id()).isEqualTo("p-1");
		assertThat(address.isObject()).isTrue();
	}

	@Test
	void aUriWithoutAUnitOrWithTheWrongSchemeIsRefused() {
		assertThatThrownBy(() -> SearchUris.parse(URI.createURI("lucene:///Product/p-1")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("names no index unit");
		assertThatThrownBy(() -> SearchUris.parse(URI.createURI("mongodb://catalog/Product/p-1")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("is not a lucene URI");
	}

	@Test
	void aFactoryRefusesAUriForAnotherUnit() {
		SearchResourceFactory factory = new SearchResourceFactory(unit, mapper(false));

		assertThatThrownBy(() -> factory.createResource(URI.createURI("lucene://other/Product/p-1")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("this factory serves 'catalog'");
	}
}
