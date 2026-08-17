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

import java.util.List;

import org.apache.lucene.document.Document;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.Materialization;
import org.eclipse.fennec.search.esearch.MaterializationKind;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.materialization.ObjectSerializers;
import org.eclipse.fennec.search.resource.SearchResource;
import org.eclipse.fennec.search.resource.SearchUris;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The two declared upgrades of §4.3: {@code STORED_OBJECT} brings the whole tree back
 * where the partial tier could not, {@code SOURCE_URI} brings back a pointer into the
 * primary store — and both refuse drift by name instead of downgrading silently.
 */
class MaterializationTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;

	@BeforeAll
	static void loadModel() {
		catalog = TestModels.load("catalog.ecore");
	}

	// --- STORED_OBJECT --------------------------------------------------------------------

	@Test
	void aStoredObjectComesBackCompleteWhereThePartialTierCouldNot() {
		IndexUnitMapping mapping = unit();
		DocumentMapping product = document(mapping, "Product");
		// The pathological partial case on purpose: the one declared field is unstored,
		// and the containment reference is EMBED — partially reconstructed, this object
		// would come back holding nothing but its id.
		KeywordFieldMapping name = ESEARCH.createKeywordFieldMapping();
		name.setFeature((EAttribute) catalog.feature("Product", "name"));
		name.setStored(false);
		name.setDocValues(true);
		product.getFields().add(name);
		ReferenceMapping reviews = ESEARCH.createReferenceMapping();
		reviews.setEReference((EReference) catalog.feature("Product", "reviews"));
		reviews.setStrategy(ReferenceStrategy.EMBED);
		product.getReferences().add(reviews);
		product.setMaterialization(ESEARCH.createMaterialization());

		EObject original = catalog.create("Product", "id", "p-1", "name", "Espresso Machine");
		children(original, "reviews").add(catalog.create("Review", "id", "r-1", "author", "ada"));

		EObject back = roundTrip(mapping, original);

		assertThat(value(back, "name")).isEqualTo("Espresso Machine");
		List<EObject> reviewsBack = children(back, "reviews");
		assertThat(reviewsBack).as("the blob carries what the fields lost").hasSize(1);
		assertThat(value(reviewsBack.get(0), "author")).isEqualTo("ada");
		assertThat(DocumentReader.of(IndexSchema.of(mapping)).omissions(catalog.eClass("Product")))
				.as("a materialized class omits nothing")
				.isEmpty();
	}

	@Test
	void serializingLeavesTheLiveObjectWhereItWas() {
		IndexUnitMapping mapping = unit();
		document(mapping, "Product").setMaterialization(ESEARCH.createMaterialization());
		Resource primary = new ResourceImpl(URI.createURI("test://primary"));
		EObject original = catalog.create("Product", "id", "p-1");
		primary.getContents().add(original);

		DocumentMapper.of(IndexSchema.of(mapping)).map(original);

		assertThat(original.eResource()).isSameAs(primary);
	}

	@Test
	void aNonContainmentTargetSurvivesAsAProxyWithItsOriginalUri() {
		IndexUnitMapping mapping = unit();
		document(mapping, "Product").setMaterialization(ESEARCH.createMaterialization());
		Resource manufacturers = new ResourceImpl(URI.createURI("test://manufacturers"));
		EObject acme = catalog.create("Manufacturer", "id", "m-1", "name", "Acme");
		manufacturers.getContents().add(acme);
		EObject original = catalog.create("Product", "id", "p-1");
		original.eSet(catalog.feature("Product", "manufacturer"), acme);

		EObject back = roundTrip(mapping, original);

		InternalEObject proxy = (InternalEObject) back.eGet(catalog.feature("Product", "manufacturer"),
				false);
		assertThat(proxy.eIsProxy()).isTrue();
		assertThat(proxy.eProxyURI()).isEqualTo(EcoreUtil.getURI(acme));
	}

	@Test
	void aDocumentWrittenBeforeTheDeclarationIsRefusedByName() {
		IndexUnitMapping bare = unit();
		Document root = DocumentMapper.of(IndexSchema.of(bare))
				.map(catalog.create("Product", "id", "p-1")).root();

		IndexUnitMapping declared = unit();
		document(declared, "Product").setMaterialization(ESEARCH.createMaterialization());

		assertThatThrownBy(() -> DocumentReader.of(IndexSchema.of(declared)).read(root, List.of()))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("rebuild");
	}

	@Test
	void anUnknownFormatIsRefusedAtTheFirstWrite() {
		IndexUnitMapping mapping = unit();
		Materialization materialization = ESEARCH.createMaterialization();
		materialization.setFormat("json");
		document(mapping, "Product").setMaterialization(materialization);

		assertThatThrownBy(() -> DocumentMapper.of(IndexSchema.of(mapping))
				.map(catalog.create("Product", "id", "p-1")))
				.hasMessageContaining("json");

		assertThatThrownBy(() -> ObjectSerializers.withDefaults().forFormat("json"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("json");
	}

	// --- SOURCE_URI -----------------------------------------------------------------------

	@Test
	void aSourceUriHitIsAProxyIntoThePrimaryStore() {
		IndexUnitMapping mapping = unit();
		Materialization materialization = ESEARCH.createMaterialization();
		materialization.setKind(MaterializationKind.SOURCE_URI);
		document(mapping, "Product").setMaterialization(materialization);
		Resource primary = new ResourceImpl(URI.createURI("mongodb://demo/Product"));
		EObject original = catalog.create("Product", "id", "p-1", "name", "Espresso Machine");
		primary.getContents().add(original);
		IndexSchema schema = IndexSchema.of(mapping);

		Document root = DocumentMapper.of(schema).map(original).root();
		EObject back = DocumentReader.of(schema).read(root, List.of());

		assertThat(back.eIsProxy()).isTrue();
		assertThat(((InternalEObject) back).eProxyURI()).isEqualTo(EcoreUtil.getURI(original));
	}

	@Test
	void sourceUriRefusesAnObjectWithoutAPrimaryResource() {
		IndexUnitMapping mapping = unit();
		Materialization materialization = ESEARCH.createMaterialization();
		materialization.setKind(MaterializationKind.SOURCE_URI);
		document(mapping, "Product").setMaterialization(materialization);

		assertThatThrownBy(() -> DocumentMapper.of(IndexSchema.of(mapping))
				.map(catalog.create("Product", "id", "p-1")))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("no resource");
	}

	@Test
	void sourceUriRefusesAnObjectLivingInTheIndexItself() throws Exception {
		IndexUnitMapping mapping = unit();
		Materialization materialization = ESEARCH.createMaterialization();
		materialization.setKind(MaterializationKind.SOURCE_URI);
		document(mapping, "Product").setMaterialization(materialization);
		DocumentMapper mapper = DocumentMapper.of(IndexSchema.of(mapping));

		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("catalog").build())) {
			SearchResource resource = new SearchResource(
					URI.createURI(SearchUris.SCHEME + "://catalog/Product/p-1"), unit, mapper);
			resource.getContents().add(catalog.create("Product", "id", "p-1"));

			assertThatThrownBy(() -> mapper.map(resource.getContents().get(0)))
					.isInstanceOf(MappingException.class)
					.hasMessageContaining("primary store");
		}
	}

	// --- helpers --------------------------------------------------------------------------

	private static IndexUnitMapping unit() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog.ePackage());
		return mapping;
	}

	private static DocumentMapping document(IndexUnitMapping unit, String className) {
		DocumentMapping mapping = ESEARCH.createDocumentMapping();
		mapping.setEClass(catalog.eClass(className));
		unit.getDocuments().add(mapping);
		return mapping;
	}

	/** Maps and reads back directly — the stored-vs-indexed distinction is §4.3-irrelevant here. */
	private static EObject roundTrip(IndexUnitMapping mapping, EObject object) {
		IndexSchema schema = IndexSchema.of(mapping);
		Document root = DocumentMapper.of(schema).map(object).root();
		return DocumentReader.of(schema).read(root, List.of());
	}

	private static Object value(EObject object, String feature) {
		return object.eGet(object.eClass().getEStructuralFeature(feature));
	}

	@SuppressWarnings("unchecked")
	private static List<EObject> children(EObject object, String feature) {
		return (List<EObject>) object.eGet(object.eClass().getEStructuralFeature(feature));
	}
}
