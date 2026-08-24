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
import org.eclipse.fennec.persistence.capabilities.CommandFeature;
import org.eclipse.fennec.persistence.capabilities.PersistenceCapabilities;
import org.eclipse.fennec.persistence.capabilities.StoreFeature;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.resource.PersistenceResource;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.query.LuceneQueryProcessor;
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

	/** The mapping the referential-integrity cases need: manufacturer as an ID_ONLY reference. */
	private static DocumentMapper mapperWithManufacturer() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog);
		DocumentMapping product = ESEARCH.createDocumentMapping();
		product.setEClass((EClass) catalog.getEClassifier("Product"));
		ReferenceMapping manufacturer = ESEARCH.createReferenceMapping();
		manufacturer.setEReference(
				(EReference) product.getEClass().getEStructuralFeature("manufacturer"));
		manufacturer.setStrategy(ReferenceStrategy.ID_ONLY);
		product.getReferences().add(manufacturer);
		mapping.getDocuments().add(product);
		return DocumentMapper.of(mapping);
	}

	private static EObject product(String id, String name) {
		EClass eClass = (EClass) catalog.getEClassifier("Product");
		EObject object = EcoreUtil.create(eClass);
		object.eSet(eClass.getEStructuralFeature("id"), id);
		object.eSet(eClass.getEStructuralFeature("name"), name);
		return object;
	}

	private static EObject bundle(String id, String name) {
		EClass eClass = (EClass) catalog.getEClassifier("Bundle");
		EObject object = EcoreUtil.create(eClass);
		object.eSet(eClass.getEStructuralFeature("id"), id);
		object.eSet(eClass.getEStructuralFeature("name"), name);
		return object;
	}

	private static EObject manufacturer(String id, String name) {
		EClass eClass = (EClass) catalog.getEClassifier("Manufacturer");
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
	void loadingReconstructsThePartialObjectAndSaysWhatIsMissing() throws Exception {
		PersistenceResource saved = resource("lucene://catalog/Product/p-1");
		saved.getContents().add(product("p-1", "Espresso Machine"));
		saved.save(Map.of());
		unit.refresh();

		PersistenceResource loaded = resource("lucene://catalog/Product/p-1");
		loaded.load(Map.of());

		assertThat(loaded.getContents()).hasSize(1);
		EObject back = loaded.getContents().get(0);
		assertThat(back.eGet(back.eClass().getEStructuralFeature("name"))).isEqualTo("Espresso Machine");
		assertThat(loaded.getWarnings())
				.as("partiality is stated, not hidden: the unmapped references cannot come back")
				.anySatisfy(warning -> assertThat(warning.getMessage())
						.contains("manufacturer").contains("STORED_OBJECT"));
	}

	@Test
	void loadingATypeUriLoadsEveryObjectOfTheType() throws Exception {
		PersistenceResource first = resource("lucene://catalog/Product/p-1");
		first.getContents().add(product("p-1", "one"));
		first.save(Map.of());
		PersistenceResource second = resource("lucene://catalog/Product/p-2");
		second.getContents().add(product("p-2", "two"));
		second.save(Map.of());
		unit.refresh();

		PersistenceResource all = resource("lucene://catalog/Product");
		all.load(Map.of());
		PersistenceResource one = resource("lucene://catalog/Product/p-2");
		one.load(Map.of());

		assertThat(all.getContents()).hasSize(2);
		assertThat(one.getContents()).hasSize(1);
	}

	@Test
	void loadingAfterASaveOnTheSameResourceKeepsTheAttachedObject() throws Exception {
		PersistenceResource resource = resource("lucene://catalog/Product/p-1");
		EObject original = product("p-1", "Espresso Machine");
		resource.getContents().add(original);
		resource.save(Map.of());
		unit.refresh();

		resource.load(Map.of());

		assertThat(resource.getContents()).hasSize(1);
		assertThat(resource.getContents().get(0)).as("identity survives for anyone holding it")
				.isSameAs(original);
	}

	@Test
	void nestedChildrenComeBackAsContainment() throws Exception {
		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap()
				.put(SearchUris.SCHEME, new SearchResourceFactory(unit, mapper(true)));
		EObject withReviews = product("p-1", "Espresso Machine");
		EClass productClass = (EClass) catalog.getEClassifier("Product");
		@SuppressWarnings("unchecked")
		List<EObject> reviews = (List<EObject>) withReviews
				.eGet((EStructuralFeature) productClass.getEStructuralFeature("reviews"));
		reviews.add(review("r-1", "ada"));
		reviews.add(review("r-2", "linus"));
		PersistenceResource saved = resource("lucene://catalog/Product/p-1");
		saved.getContents().add(withReviews);
		saved.save(Map.of());
		unit.refresh();

		PersistenceResource loaded = resource("lucene://catalog/Product/p-1");
		loaded.load(Map.of());

		EObject back = loaded.getContents().get(0);
		@SuppressWarnings("unchecked")
		List<EObject> reviewsBack = (List<EObject>) back
				.eGet((EStructuralFeature) productClass.getEStructuralFeature("reviews"));
		assertThat(reviewsBack).hasSize(2);
		assertThat(reviewsBack.get(0).eGet(reviewsBack.get(0).eClass().getEStructuralFeature("author")))
				.isEqualTo("ada");
	}

	@Test
	void anIdOnlyProxyResolvesThroughTheSameResourceSet() throws Exception {
		DocumentMapper mapper = idOnlyMapper();
		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap()
				.put(SearchUris.SCHEME, new SearchResourceFactory(unit, mapper));

		EObject acme = EcoreUtil.create((EClass) catalog.getEClassifier("Manufacturer"));
		acme.eSet(acme.eClass().getEStructuralFeature("id"), "m-1");
		acme.eSet(acme.eClass().getEStructuralFeature("name"), "Acme");
		PersistenceResource manufacturers = resource("lucene://catalog/Manufacturer/m-1");
		manufacturers.getContents().add(acme);
		manufacturers.save(Map.of());

		EObject withManufacturer = product("p-1", "Espresso Machine");
		withManufacturer.eSet(withManufacturer.eClass().getEStructuralFeature("manufacturer"), acme);
		PersistenceResource products = resource("lucene://catalog/Product/p-1");
		products.getContents().add(withManufacturer);
		products.save(Map.of());
		unit.refresh();

		PersistenceResource loaded = resource("lucene://catalog/Product/p-1");
		loaded.load(Map.of());
		EObject back = loaded.getContents().get(0);
		EObject resolved = (EObject) back.eGet(back.eClass().getEStructuralFeature("manufacturer"));

		assertThat(resolved.eIsProxy()).as("resolution went through the ResourceSet").isFalse();
		assertThat(resolved.eGet(resolved.eClass().getEStructuralFeature("name"))).isEqualTo("Acme");
	}

	@Test
	void aStoredObjectClassLoadsCompleteAndWarnsAboutNothing() throws Exception {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog);
		DocumentMapping productMapping = ESEARCH.createDocumentMapping();
		productMapping.setEClass((EClass) catalog.getEClassifier("Product"));
		productMapping.setMaterialization(ESEARCH.createMaterialization());
		mapping.getDocuments().add(productMapping);
		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap()
				.put(SearchUris.SCHEME, new SearchResourceFactory(unit, DocumentMapper.of(mapping)));

		EObject withReviews = product("p-1", "Espresso Machine");
		EClass productClass = (EClass) catalog.getEClassifier("Product");
		@SuppressWarnings("unchecked")
		List<EObject> reviews = (List<EObject>) withReviews
				.eGet((EStructuralFeature) productClass.getEStructuralFeature("reviews"));
		reviews.add(review("r-1", "ada"));
		PersistenceResource saved = resource("lucene://catalog/Product/p-1");
		saved.getContents().add(withReviews);
		saved.save(Map.of());
		unit.refresh();

		PersistenceResource loaded = resource("lucene://catalog/Product/p-1");
		loaded.load(Map.of());

		EObject back = loaded.getContents().get(0);
		@SuppressWarnings("unchecked")
		List<EObject> reviewsBack = (List<EObject>) back
				.eGet((EStructuralFeature) productClass.getEStructuralFeature("reviews"));
		assertThat(reviewsBack).as("the stored object carries its whole tree").hasSize(1);
		assertThat(loaded.getWarnings()).as("a complete object needs no partiality warning").isEmpty();
	}

	@Test
	void aLoadOverDriftedDocumentsRecordsTheDiagnosticAndFails() throws Exception {
		// Written without materialization, read with a mapping that declares STORED_OBJECT:
		// the reconstruction refuses (rebuild), and the resource carries that as an error
		// diagnostic plus a checked IOException — not a raw runtime exception.
		PersistenceResource saved = resource("lucene://catalog/Product/p-1");
		saved.getContents().add(product("p-1", "Espresso Machine"));
		saved.save(Map.of());
		unit.refresh();

		IndexUnitMapping declared = ESEARCH.createIndexUnitMapping();
		declared.setName("catalog");
		declared.setEPackage(catalog);
		DocumentMapping productMapping = ESEARCH.createDocumentMapping();
		productMapping.setEClass((EClass) catalog.getEClassifier("Product"));
		productMapping.setMaterialization(ESEARCH.createMaterialization());
		declared.getDocuments().add(productMapping);
		SearchResource drifted = new SearchResource(URI.createURI("lucene://catalog/Product/p-1"),
				unit, DocumentMapper.of(declared));

		assertThatThrownBy(() -> drifted.load(Map.of()))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("rebuild");
		assertThat(drifted.getErrors()).isNotEmpty();
	}

	/** Product with NESTED reviews and an ID_ONLY manufacturer — the reference round trip. */
	private DocumentMapper idOnlyMapper() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog);
		DocumentMapping product = ESEARCH.createDocumentMapping();
		product.setEClass((EClass) catalog.getEClassifier("Product"));
		ReferenceMapping manufacturer = ESEARCH.createReferenceMapping();
		manufacturer.setEReference((EReference) product.getEClass().getEStructuralFeature("manufacturer"));
		manufacturer.setStrategy(ReferenceStrategy.ID_ONLY);
		product.getReferences().add(manufacturer);
		mapping.getDocuments().add(product);
		return DocumentMapper.of(mapping);
	}

	// --- declaring ----------------------------------------------------------------------

	@Test
	void capabilitiesAreOneAggregateWithHonestViews() {
		PersistenceCapabilities capabilities = resource("lucene://catalog/Product").capabilities();

		assertThat(capabilities.query().supported())
				.as("the query view is the processor's declaration, not a second list")
				.isEqualTo(LuceneQueryProcessor.declaredCapabilities().supported());
		assertThat(capabilities.command().supported())
				.as("the write vocabulary of #29 — declared backend-wide, narrowed per class")
				.containsExactlyInAnyOrder(CommandFeature.INSERT, CommandFeature.DELETE_BY_SELECTOR,
						CommandFeature.UPDATE_BY_SELECTOR);
		EClass product = (EClass) catalog.getEClassifier("Product");
		assertThat(capabilities.command().supports(CommandFeature.UPDATE_BY_SELECTOR, product))
				.as("but not for a class whose mapping keeps no complete object")
				.isFalse();
		assertThat(capabilities.store().supports(StoreFeature.TRANSACTION_BRACKET))
				.as("no transaction bracket in v1 (#30)")
				.isFalse();
	}

	// --- streaming ------------------------------------------------------------------------

	@Test
	void streamingReturnsDetachedObjects() throws Exception {
		PersistenceResource first = resource("lucene://catalog/Product/p-1");
		first.getContents().add(product("p-1", "one"));
		first.save(Map.of());
		PersistenceResource second = resource("lucene://catalog/Product/p-2");
		second.getContents().add(product("p-2", "two"));
		second.save(Map.of());
		unit.refresh();

		SearchResource streaming = (SearchResource) resource("lucene://catalog/Product");
		List<EObject> streamed = streaming.stream().toList();

		assertThat(streamed).hasSize(2);
		assertThat(streamed.get(0).eResource())
				.as("a stream hands out values, not contents ownership")
				.isNull();
		assertThat(streaming.getContents()).as("streaming does not load the resource").isEmpty();
	}

	// --- composite ids ----------------------------------------------------------------------

	@Test
	void compositeIdsFollowTheKeyedFragmentContract() throws Exception {
		EClass orderLine = (EClass) catalog.getEClassifier("OrderLine");
		EObject line = EcoreUtil.create(orderLine);
		line.eSet(orderLine.getEStructuralFeature("orderId"), "A");
		line.eSet(orderLine.getEStructuralFeature("lineNo"), 2);
		line.eSet(orderLine.getEStructuralFeature("quantity"), 5);
		PersistenceResource saved = resource("lucene://catalog/OrderLine");
		saved.getContents().add(line);
		saved.save(Map.of());
		unit.refresh();

		SearchResource loaded = (SearchResource) resource("lucene://catalog/OrderLine");
		loaded.load(Map.of());

		// keyed access is order-free; the canonical form is the id-attribute order
		EObject found = loaded.getEObject("lineNo=2,orderId=A");
		assertThat(found).isNotNull();
		assertThat(found.eGet(orderLine.getEStructuralFeature("quantity"))).isEqualTo(5);
		assertThat(loaded.getURIFragment(found)).isEqualTo("orderId=A,lineNo=2");
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

	// --- what a URI addresses -------------------------------------------------------------

	@Test
	void aUriNamingASupertypeReadsItsSubtypes() throws Exception {
		PersistenceResource written = resource("lucene://catalog/Bundle/b-1");
		written.getContents().add(bundle("b-1", "Starter set"));
		written.save(Map.of());
		unit.refresh();

		// The type segment scopes by class, as a type predicate does — a Bundle is a
		// Product, and reading Products that skipped it would be a lie about the model.
		Resource read = resourceSet.createResource(URI.createURI("lucene://catalog/Product"));
		read.load(Map.of());

		assertThat(read.getContents()).hasSize(1);
		assertThat(read.getContents().get(0).eClass().getName()).isEqualTo("Bundle");
	}

	@Test
	void loadingATypeTheUnitDoesNotMapRefusesInsteadOfAnsweringEmpty() {
		Resource resource = resourceSet.createResource(URI.createURI("lucene://catalog/Sprocket"));

		// The refusal is loud (an empty view would bury the mistake), and it speaks the
		// command contract: IOException → cause QueryException → getDiagnostic()
		// (emf.persistence-jpa#197).
		assertThatThrownBy(() -> resource.load(Map.of()))
				.as("an unknown type is a refusal, not an empty result (emf.persistence-jpa#197)")
				.isInstanceOf(IOException.class)
				.hasMessageContaining("Sprocket")
				.cause()
				.isInstanceOf(QueryException.class)
				.satisfies(cause -> assertThat(((QueryException) cause).getDiagnostic())
						.as("the refusal carries its diagnostic in the exception").isNotNull());
		assertThat(resource.getContents()).isEmpty();
		assertThat(resource.getErrors())
				.as("the diagnostic also lands on the resource, where the TCK reads it")
				.isNotEmpty();
		assertThat(resource.getErrors().get(0).getMessage()).contains("Sprocket");
	}

	// --- referential integrity ------------------------------------------------------------

	@Test
	void deletingAnObjectSomethingStillReferencesIsRefused() throws Exception {
		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap()
				.put(SearchUris.SCHEME, new SearchResourceFactory(unit, mapperWithManufacturer()));
		EObject acme = manufacturer("m-1", "ACME");
		Resource makers = resourceSet.createResource(URI.createURI("lucene://catalog/Manufacturer/m-1"));
		makers.getContents().add(acme);
		makers.save(Map.of());
		EObject machine = product("p-1", "Espresso Machine");
		machine.eSet(((EClass) catalog.getEClassifier("Product")).getEStructuralFeature("manufacturer"),
				acme);
		PersistenceResource products = resource("lucene://catalog/Product/p-1");
		products.getContents().add(machine);
		products.save(Map.of());
		unit.refresh();

		assertThatThrownBy(() -> makers.delete(Map.of()))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("manufacturer");
		assertThat(makers.getErrors()).as("the refusal says why").isNotEmpty();
		unit.refresh();
		assertThat(((PersistenceResource) resourceSet
				.createResource(URI.createURI("lucene://catalog/Manufacturer/m-1"))).exist())
						.as("a refused delete changes nothing").isTrue();
	}

	@Test
	void deletingAnObjectNobodyReferencesStillWorks() throws Exception {
		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap()
				.put(SearchUris.SCHEME, new SearchResourceFactory(unit, mapperWithManufacturer()));
		Resource makers = resourceSet.createResource(URI.createURI("lucene://catalog/Manufacturer/m-2"));
		makers.getContents().add(manufacturer("m-2", "Unloved"));
		makers.save(Map.of());
		unit.refresh();

		makers.delete(Map.of());
		unit.refresh();

		assertThat(((PersistenceResource) resourceSet
				.createResource(URI.createURI("lucene://catalog/Manufacturer/m-2"))).exist()).isFalse();
	}
}
