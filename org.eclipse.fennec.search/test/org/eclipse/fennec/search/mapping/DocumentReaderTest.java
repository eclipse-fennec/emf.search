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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.index.Term;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The read direction of the mapping — documents back to EObjects, through a real index so
 * only what Lucene actually stores comes back (an in-memory {@link Document} still carries
 * unstored values; the stored-fields reader is what forgets them).
 * <p>
 * The contract under test is §4.3's default tier: faithful for everything stored, silent
 * for nothing — what cannot come back is named by {@link DocumentReader#omissions}.
 */
class DocumentReaderTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;

	private IndexUnit unit;

	@BeforeAll
	static void loadModel() {
		catalog = TestModels.load("catalog.ecore");
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

	// --- attributes -----------------------------------------------------------------------

	@Test
	void scalarAttributesRoundTripThroughTheIndex() throws Exception {
		Date released = new Date(1_700_000_000_000L);
		EObject product = catalog.create("Product", "id", "p-1", "name", "Espresso Machine",
				"description", "makes coffee", "price", 499.0, "stock", 7, "available", true,
				"released", released);

		EObject back = roundTrip(unit(), product);

		assertThat(value(back, "id")).isEqualTo("p-1");
		assertThat(value(back, "name")).isEqualTo("Espresso Machine");
		assertThat(value(back, "description")).isEqualTo("makes coffee");
		assertThat(value(back, "price")).isEqualTo(499.0);
		assertThat(value(back, "stock")).isEqualTo(7);
		assertThat(value(back, "available")).isEqualTo(true);
		assertThat(value(back, "released")).isEqualTo(released);
	}

	@Test
	void enumAndMultiValuedAttributesRoundTripInOrder() throws Exception {
		EObject product = catalog.create("Product", "id", "p-2",
				"tags", List.of("coffee", "kitchen"), "condition", conditionLiteral("USED"));

		EObject back = roundTrip(unit(), product);

		assertThat(value(back, "condition").toString()).isEqualTo("USED");
		assertThat(value(back, "tags")).isEqualTo(List.of("coffee", "kitchen"));
	}

	@Test
	void aDefaultedNonUnsettableAttributeComesBackAtItsDefault() throws Exception {
		// available was never set; per #37 its effective value false is indexed and stored,
		// so the reconstruction carries it — same answer JPA and Mongo would give.
		EObject back = roundTrip(unit(), catalog.create("Product", "id", "p-3"));

		assertThat(value(back, "available")).isEqualTo(false);
	}

	@Test
	void aFieldDeclaredUnstoredStaysUnsetAndIsNamedAsOmission() throws Exception {
		IndexUnitMapping mapping = unit();
		DocumentMapping product = document(mapping, "Product");
		KeywordFieldMapping name = ESEARCH.createKeywordFieldMapping();
		name.setFeature((EAttribute) catalog.feature("Product", "name"));
		name.setStored(false);
		name.setDocValues(true);
		product.getFields().add(name);

		EObject back = roundTrip(mapping, catalog.create("Product", "id", "p-4", "name", "hidden"));

		assertThat(back.eIsSet(catalog.feature("Product", "name"))).isFalse();
		assertThat(DocumentReader.of(IndexSchema.of(mapping)).omissions(catalog.eClass("Product")))
				.contains("name");
	}

	// --- references -----------------------------------------------------------------------

	@Test
	void nestedChildrenAreReassembledInOrder() throws Exception {
		IndexUnitMapping mapping = unit();
		DocumentMapping product = document(mapping, "Product");
		ReferenceMapping reviews = ESEARCH.createReferenceMapping();
		reviews.setEReference((EReference) catalog.feature("Product", "reviews"));
		reviews.setStrategy(ReferenceStrategy.NESTED);
		product.getReferences().add(reviews);

		EObject withReviews = catalog.create("Product", "id", "p-5", "name", "Grinder");
		children(withReviews, "reviews").add(catalog.create("Review", "id", "r-1", "author", "ada", "rating", 5));
		children(withReviews, "reviews").add(catalog.create("Review", "id", "r-2", "author", "linus", "rating", 3));

		EObject back = roundTrip(mapping, withReviews);

		List<EObject> reviewsBack = children(back, "reviews");
		assertThat(reviewsBack).hasSize(2);
		assertThat(value(reviewsBack.get(0), "author")).isEqualTo("ada");
		assertThat(value(reviewsBack.get(0), "rating")).isEqualTo(5);
		assertThat(value(reviewsBack.get(1), "author")).isEqualTo("linus");
	}

	@Test
	void anIdOnlyReferenceComesBackAsAProxyIntoThisBackend() throws Exception {
		IndexUnitMapping mapping = unit();
		DocumentMapping product = document(mapping, "Product");
		ReferenceMapping manufacturer = ESEARCH.createReferenceMapping();
		manufacturer.setEReference((EReference) catalog.feature("Product", "manufacturer"));
		manufacturer.setStrategy(ReferenceStrategy.ID_ONLY);
		product.getReferences().add(manufacturer);

		EObject withManufacturer = catalog.create("Product", "id", "p-6");
		withManufacturer.eSet(catalog.feature("Product", "manufacturer"),
				catalog.create("Manufacturer", "id", "m-1", "name", "Acme"));

		EObject back = roundTrip(mapping, withManufacturer);

		InternalEObject proxy = (InternalEObject) back.eGet(catalog.feature("Product", "manufacturer"), false);
		assertThat(proxy.eIsProxy()).isTrue();
		assertThat(proxy.eProxyURI().toString()).isEqualTo("lucene://catalog/Manufacturer/m-1#m-1");
	}

	@Test
	void anEmbeddedReferenceIsNotReconstructedButNamed() throws Exception {
		IndexUnitMapping mapping = unit();
		DocumentMapping product = document(mapping, "Product");
		ReferenceMapping reviews = ESEARCH.createReferenceMapping();
		reviews.setEReference((EReference) catalog.feature("Product", "reviews"));
		reviews.setStrategy(ReferenceStrategy.EMBED);
		product.getReferences().add(reviews);

		EObject withReviews = catalog.create("Product", "id", "p-7");
		children(withReviews, "reviews").add(catalog.create("Review", "id", "r-1", "author", "ada"));

		EObject back = roundTrip(mapping, withReviews);

		assertThat(children(back, "reviews"))
				.as("a flattened embed cannot say which value belonged to which target")
				.isEmpty();
		assertThat(DocumentReader.of(IndexSchema.of(mapping)).omissions(catalog.eClass("Product")))
				.contains("reviews");
	}

	@Test
	void anUnmappedReferenceIsAnOmission() {
		IndexUnitMapping mapping = unit();
		DocumentReader reader = DocumentReader.of(IndexSchema.of(mapping));

		assertThat(reader.omissions(catalog.eClass("Product")))
				.as("unmapped references were never written")
				.contains("reviews", "manufacturer");
	}

	// --- refusals -------------------------------------------------------------------------

	@Test
	void aDocumentWithAForeignTypeNameIsRefusedByName() {
		DocumentReader reader = DocumentReader.of(IndexSchema.of(unit()));
		Document foreign = new Document();
		foreign.add(new StringField(SearchFields.TYPE, "Unknown", Field.Store.YES));

		assertThatThrownBy(() -> reader.read(foreign, List.of()))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("Unknown");
	}

	@Test
	void aDocumentWithoutATypeFieldIsRefused() {
		DocumentReader reader = DocumentReader.of(IndexSchema.of(unit()));

		assertThatThrownBy(() -> reader.read(new Document(), List.of()))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining(SearchFields.TYPE);
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

	/** Writes the object, refreshes, and reads it back through the stored-fields reader. */
	private EObject roundTrip(IndexUnitMapping mapping, EObject object) throws IOException {
		IndexSchema schema = IndexSchema.of(mapping);
		MappedDocument mapped = DocumentMapper.of(schema).map(object);
		unit.updateDocuments(mapped.term(), mapped.documents());
		unit.refresh();
		DocumentReader reader = DocumentReader.of(schema);
		return unit.search(searcher -> {
			StoredFields stored = searcher.storedFields();
			TopDocs roots = searcher.search(new TermQuery(
					new Term(SearchFields.PARENT, SearchFields.PARENT_VALUE)), 1);
			Document root = stored.document(roots.scoreDocs[0].doc);
			BooleanQuery childQuery = new BooleanQuery.Builder()
					.add(new TermQuery(new Term(SearchFields.ROOT, root.get(SearchFields.ROOT))), Occur.FILTER)
					.add(new TermQuery(new Term(SearchFields.PARENT, SearchFields.PARENT_VALUE)), Occur.MUST_NOT)
					.build();
			int count = searcher.count(childQuery);
			List<Document> children = new ArrayList<>();
			if (count > 0) {
				TopDocs top = searcher.search(childQuery, count, new Sort(SortField.FIELD_DOC));
				for (ScoreDoc hit : top.scoreDocs) {
					children.add(stored.document(hit.doc));
				}
			}
			return reader.read(root, children);
		});
	}

	private static Object value(EObject object, String feature) {
		return object.eGet(object.eClass().getEStructuralFeature(feature));
	}

	@SuppressWarnings("unchecked")
	private static List<EObject> children(EObject object, String feature) {
		return (List<EObject>) object.eGet(object.eClass().getEStructuralFeature(feature));
	}

	private static Object conditionLiteral(String name) {
		EEnum condition = (EEnum) catalog.ePackage()
				.getEClassifier("Condition");
		return condition.getEEnumLiteral(name).getInstance();
	}
}
