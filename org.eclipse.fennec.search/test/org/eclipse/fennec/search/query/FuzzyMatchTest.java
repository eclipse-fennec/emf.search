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
import static org.eclipse.fennec.model.query.builder.Expressions.not;
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
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.expression.Expression;
import org.eclipse.fennec.model.expression.StringMatch;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.support.QueryContexts;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.TextFieldMapping;
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
 * Edit-distance matching, {@code StringMatchKind.FUZZY} from emf.persistence-jpa#167.
 * <p>
 * The kind is defined over the <em>whole value</em>, which makes a keyword projection the
 * only honest source for it: there the indexed term is the value, so Lucene's
 * optimal-string-alignment automaton counts the same edits the IR's in-memory oracle does
 * — that agreement is what the published TCK asserts, and what the cases here pin down
 * one property at a time. On analyzed text the kind is refused with the way out, and the
 * two knobs that Lucene cannot honour (case folding, a budget beyond two) are refusals
 * rather than silent approximations.
 */
class FuzzyMatchTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;

	private static TestModels models;
	private static EClass product;

	private IndexUnit unit;
	private IndexSchema schema;
	private DocumentMapper mapper;

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
		mapper = DocumentMapper.of(schema);
		index(product("alice", "Alice", "a fine roast"));
		index(product("bob", "Bob", "a fine grinder"));
		index(product("carol", "Carol", "a fine kettle"));
		// The document without a name, so every negation has to answer for it.
		index(product("nameless", null, "a fine mystery"));
		unit.refresh();
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	// --- the budget ---------------------------------------------------------------------------

	@Test
	void theDefaultBudgetIsTwoEdits() throws Exception {
		// "Alicia" -> "Alice" is a substitution plus a deletion.
		assertThat(ids(path(name()).fuzzy("Alicia"))).containsExactly("alice");
	}

	@Test
	void aBudgetOfOneExcludesTheTwoEditNeighbour() throws Exception {
		assertThat(ids(path(name()).fuzzy("Alicia", 1))).isEmpty();
	}

	@Test
	void anExactValueMatchesWithinAnyBudget() throws Exception {
		assertThat(ids(path(name()).fuzzy("Bob", 1))).containsExactly("bob");
	}

	@Test
	void anAdjacentTranspositionCostsOneEdit() throws Exception {
		// Damerau, not plain Levenshtein: "oBb" is "Bob" with the first two characters swapped,
		// which classic Levenshtein would have priced at two.
		assertThat(ids(path(name()).fuzzy("oBb", 1))).containsExactly("bob");
	}

	// --- the exact prefix ---------------------------------------------------------------------

	@Test
	void anExactPrefixExcludesTheNeighbourThatDiffersInIt() throws Exception {
		// "Karol" is one substitution from "Carol", but the substitution is in the character
		// the prefix requires exactly.
		assertThat(ids(path(name()).fuzzy("Karol", 2, 1))).isEmpty();
	}

	@Test
	void anExactPrefixKeepsTheNeighbourThatDiffersBehindIt() throws Exception {
		assertThat(ids(path(name()).fuzzy("Caral", 2, 1))).containsExactly("carol");
	}

	// --- case ---------------------------------------------------------------------------------

	@Test
	void fuzzinessIsCaseSensitiveLikeEveryOtherKeywordMatch() throws Exception {
		// One case difference is one edit and stays inside the budget; three do not.
		assertThat(ids(path(name()).fuzzy("bob", 1))).containsExactly("bob");
		assertThat(ids(path(name()).fuzzy("BOB", 1))).isEmpty();
	}

	@Test
	void aCaseInsensitiveFuzzyMatchIsRefusedWithTheWayOut() {
		StringMatch folded = path(name()).fuzzy("alice");
		folded.setCaseInsensitive(true);

		assertThatThrownBy(() -> ids(folded))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("case-insensitive FUZZY")
				.hasMessageContaining("lowercasing keyword field");
	}

	// --- three-valued logic -------------------------------------------------------------------

	@Test
	void aNegatedFuzzyMatchIsGuardedByExistence() throws Exception {
		// NOT(name ~ "Alicia") is UNKNOWN for the document that has no name at all.
		assertThat(ids(not(path(name()).fuzzy("Alicia")))).containsExactlyInAnyOrder("bob", "carol");
	}

	// --- refusals -----------------------------------------------------------------------------

	@Test
	void anAnalyzedFieldRefusesFuzzinessWithTheWayOut() {
		// Term-level fuzziness over tokens is a different question from whole-value edit
		// distance, so it is refused by name like the anchored kinds are.
		assertThatThrownBy(() -> ids(path(description()).fuzzy("finer")))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("analyzed")
				.hasMessageContaining("keyword field");
	}

	@Test
	void aBudgetBeyondTwoIsRefusedRatherThanClamped() {
		// The IR validator refuses this shape upstream; a deserialized query can still carry
		// it into translation, and Lucene's automaton stops at two edits.
		assertThatThrownBy(() -> ids(path(name()).fuzzy("Alice", 3)))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("maxEdits");
	}

	@Test
	void aNumericFieldRefusesFuzzinessLikeEveryOtherStringMatch() {
		assertThatThrownBy(() -> ids(path(models.feature("Product", "stock")).fuzzy("7")))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("numeric");
	}

	// --- completeness -------------------------------------------------------------------------

	@Test
	void everyNeighbourComesBackNotOnlyTheFiftyClosest() throws Exception {
		// Lucene's default fuzzy rewrite keeps the 50 closest terms and drops the rest, which
		// would make a predicate answer less than it was asked without saying so. This
		// backend rewrites to constant score instead, so all 60 neighbours are hits.
		for (int i = 0; i < 60; i++) {
			index(product("neighbour-" + i, "zzzz" + (char) ('0' + i), null));
		}
		unit.refresh();

		assertThat(ids(path(name()).fuzzy("zzzzz", 1))).hasSize(60);
	}

	// --- helpers ------------------------------------------------------------------------------

	private List<String> ids(Expression predicate) throws Exception {
		Query query = QueryBuilder.from(product).where(predicate).build();
		LuceneQueryPlan plan = (LuceneQueryPlan) LuceneQueryProcessor.of(schema, null)
				.translate(query, QueryContexts.of(product, null));
		return unit.search(searcher -> {
			TopDocs top = searcher.search(plan.query(), 200);
			List<String> hits = new ArrayList<>();
			for (ScoreDoc hit : top.scoreDocs) {
				Document document = searcher.storedFields().document(hit.doc);
				hits.add(document.get(SearchFields.ID));
			}
			return hits;
		});
	}

	private void index(EObject object) throws IOException {
		MappedDocument mapped = mapper.map(object);
		unit.addDocument(mapped.root());
	}

	private static EObject product(String id, String name, String description) {
		EObject object = EcoreUtil.create(product);
		object.eSet(models.feature("Product", "id"), id);
		if (name != null) {
			object.eSet(models.feature("Product", "name"), name);
		}
		if (description != null) {
			object.eSet(models.feature("Product", "description"), description);
		}
		return object;
	}

	private static EAttribute name() {
		return (EAttribute) models.feature("Product", "name");
	}

	private static EAttribute description() {
		return (EAttribute) models.feature("Product", "description");
	}

	/** A keyword projection of name — the honest source for whole-value edit distance. */
	private static IndexUnitMapping mapping() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(models.ePackage());
		var document = ESEARCH.createDocumentMapping();
		document.setEClass(product);
		KeywordFieldMapping name = ESEARCH.createKeywordFieldMapping();
		name.setFeature(name());
		document.getFields().add(name);
		TextFieldMapping description = ESEARCH.createTextFieldMapping();
		description.setFeature(description());
		document.getFields().add(description);
		mapping.getDocuments().add(document);
		return mapping;
	}
}
