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
package org.eclipse.fennec.search.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.lucene.document.Document;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * What this backend indexes for a reference whose target lives in <em>another resource</em>
 * (#33) — the question a sweep in {@code emf.codec} found nobody had asked, because no test
 * had ever set up two real files and read them back through a separate {@code ResourceSet}.
 * <p>
 * The answer, in one sentence: <b>this index stores ids, not URIs.</b> An {@code ID_ONLY}
 * reference writes the target's id whether the target sits in this resource, in another one,
 * or is still a proxy — and reading it back gives a proxy into <em>this unit</em>
 * ({@code lucene://<unit>/<Type>/<id>}), because that is the only address space the index
 * knows. A cross-resource reference therefore survives indexing exactly as far as the target
 * is indexed in the same unit; the original resource layout is not preserved and is not meant
 * to be.
 * <p>
 * Which leaves two shapes that cannot be written honestly, and both are refused rather than
 * indexed as something that looks right: a proxy whose URI addresses a <em>position</em>
 * instead of an id, and an unresolved child of a {@code NESTED} block.
 */
class CrossResourceReferenceTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;
	private static EClass product;

	@TempDir
	Path directory;

	@BeforeAll
	static void loadModel() {
		catalog = TestModels.load("catalog.ecore");
		product = catalog.eClass("Product");
	}

	@Test
	void aTargetInAnotherResourceIsIndexedByItsId() throws Exception {
		EObject machine = writeTwoResources();

		Document document = DocumentMapper.of(idOnlyMapping()).map(machine).documents().get(0);

		assertThat(document.get("manufacturer"))
				.as("the id, not the href it was written under in the file")
				.isEqualTo("m-1");
	}

	@Test
	void anUnresolvedProxyIsIndexedWithoutLoadingItsResource() throws Exception {
		writeTwoResources();
		// A separate ResourceSet, like emf.codec's own cross-resource test: the target has
		// not been loaded, so the reference holds a proxy.
		ResourceSet fresh = resourceSet();
		EObject machine = fresh.getResource(fileUri("products.xmi"), true).getContents().get(0);
		assertThat(((EObject) machine.eGet(manufacturer(), false)).eIsProxy())
				.as("the fixture is only meaningful while the target is unresolved")
				.isTrue();

		Document document = DocumentMapper.of(idOnlyMapping()).map(machine).documents().get(0);

		assertThat(document.get("manufacturer")).isEqualTo("m-1");
		assertThat(fresh.getResource(fileUri("makers.xmi"), false))
				.as("indexing must not pull the other resource in — an index is not a loader")
				.isNull();
	}

	@Test
	void aTargetDeepInAContainmentTreeIsIndexedByItsOwnId() throws Exception {
		// Not a root object: a review inside a product, referenced from another resource.
		ResourceSet set = resourceSet();
		Resource reviewed = set.createResource(fileUri("reviewed.xmi"));
		EObject reviewedProduct = catalog.create("Product", "id", "p-9", "name", "Kettle");
		EObject review = catalog.create("Review", "id", "r-42", "author", "ada");
		listOf(reviewedProduct, reviews()).add(review);
		reviewed.getContents().add(reviewedProduct);
		reviewed.save(null);

		Resource other = set.createResource(fileUri("other.xmi"));
		EObject machine = catalog.create("Product", "id", "p-1", "name", "Espresso");
		listOf(machine, reviews()).add(review);
		other.getContents().add(machine);

		Document document = DocumentMapper.of(idOnlyReviewsMapping()).map(machine).documents().get(0);

		assertThat(document.getValues("reviews"))
				.as("depth in the other document does not change what identifies the target")
				.containsExactly("r-42");
	}

	// --- what cannot be written honestly ---------------------------------------------------------

	@Test
	void aPositionalProxyIsRefusedRatherThanIndexedAsAnId() {
		// What a target without an EMF id attribute looks like from outside: the href is a
		// path into the other document. Writing it would put a string in the index that no
		// query matches and no read resolves — indexed-looking and gone.
		EObject machine = catalog.create("Product", "id", "p-1", "name", "Espresso");
		machine.eSet(manufacturer(), proxy("Manufacturer", "makers.xmi#//@manufacturers.0"));
		DocumentMapper mapper = DocumentMapper.of(idOnlyMapping());

		assertThatThrownBy(() -> mapper.map(machine))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("addresses a position")
				.hasMessageContaining("no EMF id attribute");
	}

	@Test
	void anUnresolvedNestedChildIsRefusedRatherThanWrittenEmpty() {
		// Cross-resource containment is a real model feature (resolveProxies), and a block is
		// written from the child's own values — which an unresolved child does not have. It
		// used to write a child document with an id and nothing else.
		EObject machine = catalog.create("Product", "id", "p-1", "name", "Espresso");
		listOf(machine, reviews()).add(proxy("Review", "reviews.xmi#r-9"));
		DocumentMapper mapper = DocumentMapper.of(nestedReviewsMapping());

		assertThatThrownBy(() -> mapper.map(machine))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("unresolved proxy")
				.hasMessageContaining("map the reference ID_ONLY");
	}

	// --- fixture -------------------------------------------------------------------------------

	/** Two real files: the maker in one, the product referencing it in the other. */
	private EObject writeTwoResources() throws IOException {
		ResourceSet set = resourceSet();
		Resource makers = set.createResource(fileUri("makers.xmi"));
		EObject acme = catalog.create("Manufacturer", "id", "m-1", "name", "ACME");
		makers.getContents().add(acme);
		makers.save(null);

		Resource products = set.createResource(fileUri("products.xmi"));
		EObject machine = catalog.create("Product", "id", "p-1", "name", "Espresso");
		machine.eSet(manufacturer(), acme);
		products.getContents().add(machine);
		products.save(null);
		assertThat(Files.readString(directory.resolve("products.xmi")))
				.as("the fixture is only meaningful if EMF wrote a cross-document href")
				.contains("makers.xmi#m-1");
		return machine;
	}

	private ResourceSet resourceSet() {
		ResourceSet set = new ResourceSetImpl();
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());
		set.getPackageRegistry().put(catalog.ePackage().getNsURI(), catalog.ePackage());
		return set;
	}

	private URI fileUri(String name) {
		return URI.createFileURI(directory.resolve(name).toString());
	}

	private EObject proxy(String className, String uri) {
		EObject target = catalog.create(className);
		((InternalEObject) target).eSetProxyURI(URI.createURI(directory.resolve(uri).toString()));
		return target;
	}

	private IndexUnitMapping idOnlyMapping() {
		return unit(reference(manufacturer(), ReferenceStrategy.ID_ONLY));
	}

	private IndexUnitMapping idOnlyReviewsMapping() {
		return unit(reference(reviews(), ReferenceStrategy.ID_ONLY));
	}

	private IndexUnitMapping nestedReviewsMapping() {
		return unit(reference(reviews(), ReferenceStrategy.NESTED));
	}

	private IndexUnitMapping unit(ReferenceMapping reference) {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog.ePackage());
		DocumentMapping document = ESEARCH.createDocumentMapping();
		document.setEClass(product);
		document.getReferences().add(reference);
		mapping.getDocuments().add(document);
		return mapping;
	}

	private ReferenceMapping reference(EReference eReference, ReferenceStrategy strategy) {
		ReferenceMapping mapping = ESEARCH.createReferenceMapping();
		mapping.setEReference(eReference);
		mapping.setStrategy(strategy);
		return mapping;
	}

	@SuppressWarnings("unchecked")
	private List<EObject> listOf(EObject object, EReference reference) {
		return (List<EObject>) object.eGet(reference);
	}

	private EReference manufacturer() {
		return (EReference) catalog.feature("Product", "manufacturer");
	}

	private EReference reviews() {
		return (EReference) catalog.feature("Product", "reviews");
	}
}
