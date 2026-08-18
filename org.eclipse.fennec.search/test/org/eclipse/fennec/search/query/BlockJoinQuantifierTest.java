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
import static org.eclipse.fennec.model.query.builder.Expressions.all;
import static org.eclipse.fennec.model.query.builder.Expressions.and;
import static org.eclipse.fennec.model.query.builder.Expressions.any;
import static org.eclipse.fennec.model.query.builder.Expressions.not;
import static org.eclipse.fennec.model.query.builder.Expressions.path;
import static org.eclipse.fennec.model.query.builder.Expressions.propertyPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.model.expression.Expression;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.support.QueryContexts;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappedDocument;
import org.eclipse.fennec.search.mapping.SearchFields;
import org.eclipse.fennec.search.mapping.TestModels;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * EXISTS/FOR_ALL over {@code NESTED} references through the block join (§5.2), and the
 * §5.1 duality at its boundary: ¬∃ ↔ ∀¬, where a parent with no children at all and a
 * parent with no <em>matching</em> child are different answers, and a child whose
 * predicate is UNKNOWN escapes a FOR_ALL and blocks a negated EXISTS.
 */
class BlockJoinQuantifierTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;

	private static TestModels models;
	private static EClass product;
	private static EClass review;

	private IndexUnit unit;
	private IndexSchema schema;

	@BeforeAll
	static void loadModel() {
		models = TestModels.load("catalog.ecore");
		product = models.eClass("Product");
		review = models.eClass("Review");
	}

	@BeforeEach
	void indexCorpus() throws IOException {
		unit = IndexUnit.open(IndexUnitConfig.inMemory("catalog")
				.refresh(RefreshTrigger.manual())
				.build());
		schema = IndexSchema.of(mapping(ReferenceStrategy.NESTED));
		DocumentMapper mapper = DocumentMapper.of(schema);
		index(mapper, product("all-good",
				review("r1", "ada", "great", 5), review("r2", "bob", "fine", 4)));
		index(mapper, product("mixed",
				review("r3", "ada", "great", 5), review("r4", "eve", null, 2)));
		index(mapper, product("bad", review("r5", "mallory", "meh", 1)));
		// One child, and its text is genuinely absent: the UNKNOWN case of the duality.
		index(mapper, product("silent", review("r6", "grace", null, 3)));
		index(mapper, product("childless"));
		unit.refresh();
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	// --- the four faces ------------------------------------------------------------------

	@Test
	void existsFindsParentsWithAtLeastOneMatchingChild() throws Exception {
		assertThat(ids(any(propertyPath(reviews()), it -> it.path(rating()).ge(4))))
				.containsExactlyInAnyOrder("all-good", "mixed");
	}

	@Test
	void forAllIsVacuouslyTrueOnTheChildlessParent() throws Exception {
		assertThat(ids(all(propertyPath(reviews()), it -> it.path(rating()).ge(4))))
				.containsExactlyInAnyOrder("all-good", "childless");
	}

	@Test
	void negatedExistsKeepsTheChildlessParent() throws Exception {
		// ¬∃ and "no children" agree: no matching child exists either way.
		assertThat(ids(not(any(propertyPath(reviews()), it -> it.path(rating()).ge(4)))))
				.containsExactlyInAnyOrder("bad", "silent", "childless");
	}

	@Test
	void negatedForAllIsAnExistsOverTheViolation() throws Exception {
		assertThat(ids(not(all(propertyPath(reviews()), it -> it.path(rating()).ge(4)))))
				.containsExactlyInAnyOrder("mixed", "bad", "silent");
	}

	// --- the 3VL wrinkle at the boundary ---------------------------------------------------

	@Test
	void aChildWithoutAValueEscapesForAll() throws Exception {
		// r4 and r6 have no text: the predicate is UNKNOWN for them, UNKNOWN is not TRUE,
		// so their parents fail the universal claim.
		assertThat(ids(all(propertyPath(reviews()), it -> it.path(text()).ne("meh"))))
				.containsExactlyInAnyOrder("all-good", "childless");
	}

	@Test
	void aChildWithoutAValueBlocksANegatedExists() throws Exception {
		// ¬∃p promises "no child for which p might hold". For r6 the predicate is UNKNOWN —
		// it might — so silent is excluded, while bad's child answers plainly FALSE.
		assertThat(ids(not(any(propertyPath(reviews()), it -> it.path(text()).eq("great")))))
				.containsExactlyInAnyOrder("bad", "childless");
	}

	@Test
	void correlationStaysPerChild() throws Exception {
		// mixed has ada(5) and eve(2): the two halves hold for different children, which
		// EMBED would conflate and NESTED must not.
		assertThat(ids(any(propertyPath(reviews()),
				it -> and(it.path(author()).eq("ada"), it.path(rating()).le(2)))))
				.isEmpty();
		assertThat(ids(any(propertyPath(reviews()),
				it -> and(it.path(author()).eq("ada"), it.path(rating()).ge(5)))))
				.containsExactlyInAnyOrder("all-good", "mixed");
	}

	// --- refusals ---------------------------------------------------------------------------

	@Test
	void aQuantifierOverEmbedIsRefusedByName() {
		IndexSchema embedded = IndexSchema.of(mapping(ReferenceStrategy.EMBED));

		assertThatThrownBy(() -> translate(embedded, any(propertyPath(reviews()),
				it -> it.path(rating()).ge(4))))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("EMBED");
	}

	@Test
	void aCorrelatedPathBackToTheRootIsRefused() {
		// A path without the iterator base reaches the root document from child scope.
		assertThatThrownBy(() -> translate(schema, any(propertyPath(reviews()),
				it -> path(nameAttribute()).eq("x"))))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("root object");
	}

	@Test
	void aQuantifierInsideAQuantifierIsRefused() {
		assertThatThrownBy(() -> translate(schema, any(propertyPath(reviews()),
				it -> any(propertyPath(reviews()), inner -> inner.path(rating()).ge(4)))))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("one level deep");
	}

	@Test
	void validationCarriesTheSameAnswerAsTranslation() {
		LuceneQueryProcessor processor = LuceneQueryProcessor.of(schema, null);
		Query fine = QueryBuilder.from(product)
				.where(any(propertyPath(reviews()), it -> it.path(rating()).ge(4))).build();
		assertThat(processor.validate(fine, product).getSeverity()).isEqualTo(Diagnostic.OK);

		LuceneQueryProcessor embedded = LuceneQueryProcessor.of(
				IndexSchema.of(mapping(ReferenceStrategy.EMBED)), null);
		assertThat(embedded.validate(fine, product).getSeverity()).isEqualTo(Diagnostic.ERROR);
	}

	// --- helpers -------------------------------------------------------------------------------

	private List<String> ids(Expression predicate) throws Exception {
		return translate(schema, predicate);
	}

	private List<String> translate(IndexSchema against, Expression predicate) throws Exception {
		Query query = QueryBuilder.from(product).where(predicate).build();
		LuceneQueryPlan plan = (LuceneQueryPlan) LuceneQueryProcessor.of(against, null)
				.translate(query, QueryContexts.of(product, null));
		return unit.search(searcher -> {
			TopDocs top = searcher.search(plan.query(), 100);
			List<String> ids = new ArrayList<>();
			for (ScoreDoc hit : top.scoreDocs) {
				Document document = searcher.storedFields().document(hit.doc);
				ids.add(document.get(SearchFields.ID));
			}
			return ids;
		});
	}

	private void index(DocumentMapper mapper, EObject object) throws IOException {
		MappedDocument mapped = mapper.map(object);
		unit.updateDocuments(mapped.term(), mapped.documents());
	}

	private EObject product(String id, EObject... reviews) {
		EObject object = models.create("Product", "id", id, "name", id);
		@SuppressWarnings("unchecked")
		List<EObject> children = (List<EObject>) object.eGet(reviews());
		children.addAll(List.of(reviews));
		return object;
	}

	private EObject review(String id, String author, String text, int rating) {
		EObject object = models.create("Review", "id", id, "author", author, "rating", rating);
		if (text != null) {
			object.eSet(models.feature("Review", "text"), text);
		}
		return object;
	}

	private static EReference reviews() {
		return (EReference) models.feature("Product", "reviews");
	}

	private static EAttribute rating() {
		return (EAttribute) models.feature("Review", "rating");
	}

	private static EAttribute author() {
		return (EAttribute) models.feature("Review", "author");
	}

	private static EAttribute text() {
		return (EAttribute) models.feature("Review", "text");
	}

	private static EStructuralFeature nameAttribute() {
		return models.feature("Product", "name");
	}

	private IndexUnitMapping mapping(ReferenceStrategy strategy) {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(models.ePackage());
		DocumentMapping productMapping = ESEARCH.createDocumentMapping();
		productMapping.setEClass(product);
		ReferenceMapping reviewsMapping = ESEARCH.createReferenceMapping();
		reviewsMapping.setEReference(reviews());
		reviewsMapping.setStrategy(strategy);
		productMapping.getReferences().add(reviewsMapping);
		mapping.getDocuments().add(productMapping);
		// The keyword projection for author: convention would analyze the string.
		DocumentMapping reviewMapping = ESEARCH.createDocumentMapping();
		reviewMapping.setEClass(review);
		KeywordFieldMapping author = ESEARCH.createKeywordFieldMapping();
		author.setFeature(author());
		reviewMapping.getFields().add(author);
		KeywordFieldMapping text = ESEARCH.createKeywordFieldMapping();
		text.setFeature(text());
		reviewMapping.getFields().add(text);
		mapping.getDocuments().add(reviewMapping);
		return mapping;
	}
}
