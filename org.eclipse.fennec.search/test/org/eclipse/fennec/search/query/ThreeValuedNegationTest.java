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
import static org.eclipse.fennec.model.query.builder.Expressions.and;
import static org.eclipse.fennec.model.query.builder.Expressions.not;
import static org.eclipse.fennec.model.query.builder.Expressions.or;
import static org.eclipse.fennec.model.query.builder.Expressions.path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.expression.ExpressionFactory;
import org.eclipse.fennec.model.expression.In;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.Expressions;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.support.QueryContexts;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.NumericFieldMapping;
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
 * The one semantic rule that a two-valued engine gets wrong by default
 * (docs/search-access.md §5.1), asserted where it actually shows: against an index that
 * holds documents <em>without</em> a value for the field under test.
 * <p>
 * Every case here would pass with a naive {@code MUST_NOT} translation if all documents
 * had values, and fail as soon as one does not. SQL says a comparison against null is
 * UNKNOWN and that negating UNKNOWN leaves it UNKNOWN, so the document with no value must
 * be absent from both the predicate's result and its negation's.
 */
class ThreeValuedNegationTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;

	private static TestModels models;
	private static EClass product;

	private IndexUnit unit;
	private IndexSchema schema;

	@BeforeAll
	static void loadModel() {
		models = TestModels.load("catalog.ecore");
		product = models.eClass("Product");
	}

	@BeforeEach
	void indexCorpus() throws IOException {
		unit = IndexUnit.open(IndexUnitConfig.inMemory("catalog")
				.refresh(RefreshTrigger.manual())
				.build());
		schema = IndexSchema.of(mapping());
		DocumentMapper mapper = DocumentMapper.of(schema);
		// "no-stock" deliberately has no value for stock, condition or tags: it is the
		// document every assertion below is really about.
		index(mapper, product("high", 10, "REFURBISHED", "sale"));
		index(mapper, product("low", 2, "USED", "clearance"));
		index(mapper, product("no-stock", null, null, null));
		unit.refresh();
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	// --- comparisons ------------------------------------------------------------------------

	@Test
	void aComparisonSkipsTheDocumentWithoutAValue() throws Exception {
		assertThat(ids(path(stock()).gt(5))).containsExactly("high");
	}

	@Test
	void negatingAComparisonAlsoSkipsIt() throws Exception {
		// NOT(stock > 5) is stock <= 5 — and UNKNOWN for a document that has no stock.
		// A MUST_NOT wrapper would have returned no-stock here.
		assertThat(ids(not(path(stock()).gt(5)))).containsExactly("low");
	}

	@Test
	void aNotEqualsSkipsIt() throws Exception {
		assertThat(ids(path(stock()).ne(2))).containsExactly("high");
	}

	@Test
	void negatingNotEqualsIsPlainEquality() throws Exception {
		assertThat(ids(not(path(stock()).ne(2)))).containsExactly("low");
	}

	@Test
	void bothAComparisonAndItsNegationLeaveTheValuelessDocumentOut() throws Exception {
		List<String> matching = ids(path(stock()).gt(5));
		List<String> notMatching = ids(not(path(stock()).gt(5)));
		assertThat(matching).doesNotContain("no-stock");
		assertThat(notMatching).doesNotContain("no-stock");
		// The two together are not the whole corpus — that is the point of three-valued logic.
		assertThat(matching.size() + notMatching.size()).isEqualTo(2);
	}

	// --- de Morgan over junctions ------------------------------------------------------------

	@Test
	void negationDistributesOverAnAndAsAnOrOfNegations() throws Exception {
		// NOT(stock > 5 AND condition = REFURBISHED) is stock <= 5 OR condition != REFURBISHED,
		// and both disjuncts are UNKNOWN for no-stock, so it stays out.
		assertThat(ids(not(and(path(stock()).gt(5), path(condition()).eq("REFURBISHED")))))
				.containsExactly("low");
	}

	@Test
	void negationDistributesOverAnOrAsAnAndOfNegations() throws Exception {
		assertThat(ids(not(or(path(stock()).gt(5), path(condition()).eq("USED")))))
				.isEmpty();
	}

	@Test
	void aDoubleNegationIsTheOriginalPredicate() throws Exception {
		assertThat(ids(not(not(path(stock()).gt(5))))).isEqualTo(ids(path(stock()).gt(5)));
	}

	// --- the leaves that need an explicit existence guard ------------------------------------

	@Test
	void aNegatedInIsGuardedByExistence() throws Exception {
		assertThat(ids(not(path(condition()).in("REFURBISHED")))).containsExactly("low");
	}

	@Test
	void aNegatedStringMatchIsGuardedByExistence() throws Exception {
		assertThat(ids(not(path(tags()).contains("sale")))).containsExactly("low");
	}

	@Test
	void anEmptyInMatchesNothingAndItsNegationOnlyDocumentsWithAValue() throws Exception {
		// The builder refuses to construct this, but the IR permits it and a deserialized
		// query can carry it — so the translation has to have an answer.
		In empty = ExpressionFactory.eINSTANCE.createIn();
		empty.setSource(Expressions.propertyPath(condition()));
		assertThat(ids(empty)).isEmpty();
		assertThat(ids(not(EcoreUtil.copy(empty)))).containsExactlyInAnyOrder("high", "low");
	}

	// --- isNull is the one predicate that is *about* the missing value ------------------------

	@Test
	void isNullFindsExactlyTheDocumentWithoutAValue() throws Exception {
		assertThat(ids(path(stock()).isNull())).containsExactly("no-stock");
	}

	@Test
	void isNotNullIsItsComplement() throws Exception {
		assertThat(ids(path(stock()).isNotNull())).containsExactlyInAnyOrder("high", "low");
	}

	@Test
	void negatingIsNullGivesIsNotNull() throws Exception {
		assertThat(ids(not(path(stock()).isNull()))).containsExactlyInAnyOrder("high", "low");
	}

	// --- what "missing" means after #37 --------------------------------------------------------

	@Test
	void anAttributeSetToItsDefaultValueIsIndexed() throws Exception {
		// The #37 fix: eIsSet answers "differs from the default" for a feature that is not
		// unsettable, so skipping on it made "condition = NEW" answer differently from JPA,
		// Mongo and the memory oracle. condition is unsettable in the test model, which makes
		// setting it to the default literal a real set — and indexed; no-stock never set it,
		// so it alone stays missing.
		EObject atDefault = product("at-default", 7, "NEW", null);
		index(DocumentMapper.of(schema), atDefault);
		unit.refresh();

		assertThat(ids(path(condition()).eq("NEW"))).containsExactly("at-default");
		assertThat(ids(path(condition()).isNull())).containsExactly("no-stock");
	}

	@Test
	void aNonUnsettableAttributeIsNeverMissing() throws Exception {
		// available is EBoolean and not unsettable: like a NOT NULL column, its effective
		// value is defined on every object — false unless someone set it. Every document
		// carries it, and IS NULL over it is honestly empty.
		assertThat(ids(path(available()).eq(Boolean.FALSE)))
				.containsExactlyInAnyOrder("high", "low", "no-stock");
		assertThat(ids(path(available()).isNull())).isEmpty();
	}

	// --- between ------------------------------------------------------------------------------

	@Test
	void betweenAndItsNegationBothSkipTheValuelessDocument() throws Exception {
		assertThat(ids(path(stock()).between(1, 5))).containsExactly("low");
		assertThat(ids(not(path(stock()).between(1, 5)))).containsExactly("high");
	}

	// --- the existence probe itself ---------------------------------------------------------

	@Test
	void aNegationOverAPointFieldWithoutDocValuesStillAnswers() throws Exception {
		// The guard has to probe "has a value" on a field whose only structure is a BKD
		// tree: FieldExistsQuery reads doc values, norms or vectors and would throw on it,
		// so the probe is an unbounded range over the same points. Convention numerics
		// always carry doc values, which is why only a declared field reaches this path.
		IndexUnitMapping declared = mapping();
		NumericFieldMapping stock = ESEARCH.createNumericFieldMapping();
		stock.setFeature(stock());
		stock.setDocValues(false);
		declared.getDocuments().get(0).getFields().add(stock);
		IndexSchema pointOnly = IndexSchema.of(declared);

		try (IndexUnit own = IndexUnit.open(IndexUnitConfig.inMemory("catalog")
				.refresh(RefreshTrigger.manual()).build())) {
			DocumentMapper mapper = DocumentMapper.of(pointOnly);
			for (EObject object : List.of(product("high", 10, "REFURBISHED", "sale"),
					product("low", 2, "USED", "clearance"), product("no-stock", null, null, null))) {
				own.addDocument(mapper.map(object).root());
			}
			own.refresh();

			Query query = QueryBuilder.from(product).where(not(path(stock()).gt(5))).build();
			LuceneQueryPlan plan = (LuceneQueryPlan) LuceneQueryProcessor.of(pointOnly, null)
					.translate(query, QueryContexts.of(product, null));
			List<String> ids = own.search(searcher -> {
				TopDocs top = searcher.search(plan.query(), 100);
				List<String> found = new ArrayList<>();
				for (ScoreDoc hit : top.scoreDocs) {
					found.add(searcher.storedFields().document(hit.doc).get(SearchFields.ID));
				}
				return found;
			});

			assertThat(ids).containsExactly("low");
		}
	}

	// --- helpers -------------------------------------------------------------------------------

	private List<String> ids(org.eclipse.fennec.model.expression.Expression predicate) throws Exception {
		Query query = QueryBuilder.from(product).where(predicate).build();
		LuceneQueryPlan plan = (LuceneQueryPlan) LuceneQueryProcessor.of(schema, null)
				.translate(query, QueryContexts.of(product, null));
		return search(plan);
	}

	private List<String> search(LuceneQueryPlan plan) throws IOException {
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
		unit.addDocument(mapped.root());
	}

	private static EObject product(String id, Integer stock, String condition, String tag) {
		EObject object = EcoreUtil.create(product);
		object.eSet(feature("id"), id);
		if (stock != null) {
			object.eSet(feature("stock"), stock);
		}
		if (condition != null) {
			EAttribute attribute = (EAttribute) feature("condition");
			object.eSet(attribute, attribute.getEAttributeType().getEPackage().getEFactoryInstance()
					.createFromString((org.eclipse.emf.ecore.EDataType) attribute.getEAttributeType(),
							condition));
		}
		if (tag != null) {
			@SuppressWarnings("unchecked")
			List<String> tags = (List<String>) object.eGet(feature("tags"));
			tags.add(tag);
		}
		return object;
	}

	private static EStructuralFeature feature(String name) {
		return models.feature("Product", name);
	}

	private static EAttribute stock() {
		return (EAttribute) feature("stock");
	}

	private static EAttribute condition() {
		return (EAttribute) feature("condition");
	}

	private static EAttribute tags() {
		return (EAttribute) feature("tags");
	}

	private static EAttribute available() {
		return (EAttribute) feature("available");
	}

	/** Conventions throughout, except a stored id so the assertions can name the hits. */
	private static IndexUnitMapping mapping() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(models.ePackage());
		var document = ESEARCH.createDocumentMapping();
		document.setEClass(product);
		KeywordFieldMapping tags = ESEARCH.createKeywordFieldMapping();
		tags.setFeature(tags());
		// A keyword projection of tags, so the string match runs on terms rather than tokens.
		document.getFields().add(tags);
		mapping.getDocuments().add(document);
		return mapping;
	}
}
