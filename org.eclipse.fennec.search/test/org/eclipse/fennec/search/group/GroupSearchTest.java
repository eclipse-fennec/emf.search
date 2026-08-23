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
package org.eclipse.fennec.search.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.fennec.model.query.builder.Expressions.path;

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.group.GroupResults.Group;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappedDocument;
import org.eclipse.fennec.search.mapping.TestModels;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Grouping with representatives (S19, #21): the top documents <em>per group</em>, the shape
 * facets cannot answer.
 * <p>
 * Every description is four terms long, so the only thing that separates two documents is
 * how often "coffee" occurs in it — that makes both orders the assertions rely on (which
 * group comes first, which document leads a group) unambiguous without asserting score
 * values, which are never a contract.
 */
class GroupSearchTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;
	private static EClass product;

	private IndexUnit unit;
	private IndexSchema schema;

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
		schema = IndexSchema.of(conventionMapping());
		DocumentMapper mapper = DocumentMapper.of(schema);
		// tf 3 / 2 / 1 / 1 over four terms each: NEW leads USED, and inside each group the
		// order is fixed. The last one carries no condition and therefore no group.
		index(mapper, "new-best", "NEW", "coffee coffee coffee kitchen");
		index(mapper, "used-best", "USED", "coffee coffee kitchen tea");
		index(mapper, "new-second", "NEW", "coffee kitchen tea pot");
		index(mapper, "used-second", "USED", "coffee pot tea kitchen");
		index(mapper, "ungrouped", null, "coffee coffee coffee coffee");
		unit.refresh();
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	// --- what grouping answers -----------------------------------------------------------

	@Test
	void theBestOfEachGroupComesBack() throws Exception {
		GroupResults results = search(request().representatives(2));

		assertThat(results.groups()).extracting(Group::key)
				.as("groups are ordered by their best hit")
				.containsExactly("NEW", "USED");
		assertThat(names(results.group("NEW").orElseThrow()))
				.as("and inside a group, the best document leads")
				.containsExactly("new-best", "new-second");
		assertThat(names(results.group("USED").orElseThrow()))
				.containsExactly("used-best", "used-second");
	}

	@Test
	void oneRepresentativeIsTheDefaultAndTotalHitsStillCountsTheGroup() throws Exception {
		GroupResults results = search(request());

		Group group = results.group("NEW").orElseThrow();
		assertThat(names(group)).containsExactly("new-best");
		assertThat(group.totalHits())
				.as("the group has two objects even though one came back")
				.isEqualTo(2);
	}

	@Test
	void anObjectWithoutTheKeyBelongsToNoGroup() throws Exception {
		GroupResults results = search(request().representatives(5));

		// It is the best textual match in the corpus and still does not appear: a document
		// without a value for the key is in no group, not in a group of its own.
		assertThat(results.groups()).flatExtracting(GroupSearchTest::names)
				.doesNotContain("ungrouped");
		assertThat(results.totalGroups()).isEqualTo(2);
	}

	@Test
	void aTruncatedAnswerSaysHowManyGroupsThereWere() throws Exception {
		GroupResults results = search(request().topGroups(1));

		assertThat(results.groups()).extracting(Group::key).containsExactly("NEW");
		assertThat(results.totalGroups())
				.as("the number a caller cannot recompute from a truncated answer")
				.isEqualTo(2);
	}

	@Test
	void thePredicateScopesTheMatchSetLikeAnywhereElse() throws Exception {
		Query onlyTea = QueryBuilder.from(product)
				.where(path(description()).contains("tea")).build();

		GroupResults results = GroupSearch.of(unit, schema)
				.search(GroupRequest.over(onlyTea).by(condition()).representatives(5));

		assertThat(results.groups()).extracting(Group::key).containsExactly("USED", "NEW");
		assertThat(names(results.group("USED").orElseThrow()))
				.containsExactly("used-best", "used-second");
		assertThat(names(results.group("NEW").orElseThrow())).containsExactly("new-second");
	}

	@Test
	void representativesAreReconstructedObjects() throws Exception {
		GroupResults results = search(request());

		EObject best = results.group("NEW").orElseThrow().representatives().get(0);
		assertThat(best.eClass()).isEqualTo(product);
		assertThat(best.eGet(description())).isEqualTo("coffee coffee coffee kitchen");
	}

	// --- what it refuses -------------------------------------------------------------------

	@Test
	void groupingByAnalyzedTextIsRefused() {
		assertThatThrownBy(() -> GroupSearch.of(unit, schema)
				.search(GroupRequest.over(coffee()).by(description())))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("TEXT");
	}

	@Test
	void groupingByANumericIsRefused() {
		assertThatThrownBy(() -> GroupSearch.of(unit, schema)
				.search(GroupRequest.over(coffee()).by((EAttribute) catalog.feature("Product", "price"))))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("NUMERIC");
	}

	@Test
	void groupingByAManyValuedAttributeIsRefused() {
		assertThatThrownBy(() -> GroupSearch.of(unit, schema)
				.search(GroupRequest.over(coffee()).by((EAttribute) catalog.feature("Product", "tags"))))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("several groups at once");
	}

	@Test
	void aKeyWithoutDocValuesIsRefused() throws Exception {
		IndexUnitMapping mapping = conventionMapping();
		DocumentMapping document = ESEARCH.createDocumentMapping();
		document.setEClass(product);
		KeywordFieldMapping keyword = ESEARCH.createKeywordFieldMapping();
		keyword.setFeature(condition());
		keyword.setDocValues(false);
		document.getFields().add(keyword);
		mapping.getDocuments().add(document);

		assertThatThrownBy(() -> GroupSearch.of(unit, IndexSchema.of(mapping))
				.search(GroupRequest.over(coffee()).by(condition())))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("docValues");
	}

	@Test
	void aRequestWithoutAKeyIsRefused() {
		assertThatThrownBy(() -> GroupSearch.of(unit, schema).search(GroupRequest.over(coffee())))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("no attribute to group by");
	}

	// --- fixture ------------------------------------------------------------------------------

	private GroupRequest request() {
		return GroupRequest.over(coffee()).by(condition());
	}

	private GroupResults search(GroupRequest request) throws Exception {
		return GroupSearch.of(unit, schema).search(request);
	}

	private Query coffee() {
		return QueryBuilder.from(product).where(path(description()).contains("coffee")).build();
	}

	private IndexUnitMapping conventionMapping() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog.ePackage());
		return mapping;
	}

	private void index(DocumentMapper mapper, String name, String condition, String description)
			throws IOException {
		EObject object = catalog.create("Product", "id", name, "name", name,
				"description", description);
		if (condition != null) {
			object.eSet(condition(), catalog.ePackage().getEClassifier("Condition") instanceof
					org.eclipse.emf.ecore.EEnum enumeration
							? enumeration.getEEnumLiteral(condition).getInstance()
							: null);
		}
		MappedDocument mapped = mapper.map(object);
		unit.updateDocuments(mapped.term(), mapped.documents());
	}

	private static List<Object> names(Group group) {
		return group.representatives().stream()
				.map(object -> object.eGet(object.eClass().getEStructuralFeature("name")))
				.toList();
	}

	private EAttribute description() {
		return (EAttribute) catalog.feature("Product", "description");
	}

	private EAttribute condition() {
		return (EAttribute) catalog.feature("Product", "condition");
	}
}
