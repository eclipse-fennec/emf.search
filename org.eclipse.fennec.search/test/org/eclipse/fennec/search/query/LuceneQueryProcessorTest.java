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
import static org.eclipse.fennec.model.query.builder.Expressions.and;
import static org.eclipse.fennec.model.query.builder.Expressions.not;
import static org.eclipse.fennec.model.query.builder.Expressions.path;
import static org.eclipse.fennec.model.query.builder.Expressions.score;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.Expressions;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.capabilities.QueryFeature;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.api.QueryContext;
import org.eclipse.fennec.persistence.query.api.QueryShape;
import org.eclipse.fennec.persistence.query.support.QueryContexts;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.TestModels;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * What the processor declares, what it translates, and — at least as important — what it
 * refuses. Translation touches no index, so these assertions need no directory.
 */
class LuceneQueryProcessorTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;

	private static TestModels models;
	private static EClass product;

	@BeforeAll
	static void loadModel() {
		models = TestModels.load("catalog.ecore");
		product = models.eClass("Product");
	}

	// --- what it declares -------------------------------------------------------------------

	@Test
	void declaresWhatAnInvertedIndexCanAnswerExactly() {
		var capabilities = processor().capabilities();
		assertThat(capabilities.supported()).contains(QueryFeature.WHERE_EQ, QueryFeature.WHERE_NE,
				QueryFeature.WHERE_COMPARISON, QueryFeature.WHERE_RANGE, QueryFeature.IN,
				QueryFeature.IS_NULL, QueryFeature.WHERE_STRING_MATCH,
				QueryFeature.STRING_MATCH_CASE_INSENSITIVE, QueryFeature.LOGICAL_AND,
				QueryFeature.LOGICAL_OR, QueryFeature.LOGICAL_NOT, QueryFeature.SORT,
				QueryFeature.LIMIT, QueryFeature.SKIP, QueryFeature.COUNT, QueryFeature.TYPE_CHECK,
				QueryFeature.TYPE_FILTER, QueryFeature.PROJECTION, QueryFeature.PARAMETERS,
				// the block join (S11, #9): quantifiers over NESTED containment
				QueryFeature.EXISTS, QueryFeature.FOR_ALL);
	}

	@Test
	void doesNotDeclareWhatItCannotAnswerHonestly() {
		var capabilities = processor().capabilities();
		assertThat(capabilities.supported()).doesNotContain(
				// never: an inverted index has no per-document value pairs and no arithmetic
				QueryFeature.FIELD_TO_FIELD, QueryFeature.ARITHMETIC, QueryFeature.STRING_FUNCTIONS,
				QueryFeature.STRING_FUNCTIONS_EXTENDED, QueryFeature.NUMERIC_FUNCTIONS,
				QueryFeature.TEMPORAL_FUNCTIONS, QueryFeature.EXPAND, QueryFeature.DISTINCT,
				QueryFeature.PIPELINE, QueryFeature.PIPELINE_COMPUTE, QueryFeature.SORT_EXPRESSION,
				// not yet: each of these has a task
				QueryFeature.SCORE, QueryFeature.GROUP_BY,
				QueryFeature.AGG_COUNT, QueryFeature.GEO_WITHIN, QueryFeature.GEO_DISTANCE);
	}

	@Test
	void validationRefusesAnUndeclaredFeatureByName() {
		Query query = QueryBuilder.from(product)
				.where(path(feature("Product", "price")).plus(1).gt(10))
				.build();
		Diagnostic diagnostic = processor().validate(query, product);
		assertThat(diagnostic.getSeverity()).isEqualTo(Diagnostic.ERROR);
		assertThat(flatten(diagnostic)).contains("ARITHMETIC");
	}

	@Test
	void validationRefusesAPathTheMappingCannotRead() {
		// manufacturer is not mapped at all, so its name never became a field here.
		Query query = QueryBuilder.from(product)
				.where(path(feature("Product", "manufacturer"), feature("Manufacturer", "name")).eq("ACME"))
				.build();
		Diagnostic diagnostic = processor().validate(query, product);
		assertThat(diagnostic.getSeverity()).isEqualTo(Diagnostic.ERROR);
		assertThat(flatten(diagnostic)).contains("manufacturer").contains("not mapped");
	}

	@Test
	void aTranslatableQueryValidatesClean() {
		Query query = QueryBuilder.from(product)
				.where(and(path(feature("Product", "stock")).gt(3),
						path(feature("Product", "id")).eq("p-1")))
				.orderByDesc(feature("Product", "stock"))
				.top(10)
				.build();
		assertThat(processor().validate(query, product).getSeverity()).isLessThan(Diagnostic.ERROR);
	}

	// --- the guards every plan carries -----------------------------------------------------

	@Test
	void everyPlanFiltersOnTheRootMarkerAndTheType() throws Exception {
		LuceneQueryPlan plan = translate(QueryBuilder.from(product).build());
		String query = plan.query().toString();
		// Without the root marker a NESTED child would count as a hit and inflate a count.
		assertThat(query).contains("_parent:true");
		assertThat(query).contains("_type:");
	}

	@Test
	void aQueryFromASupertypeAlsoMatchesItsIndexedSubtypes() throws Exception {
		LuceneQueryPlan plan = translate(QueryBuilder.from(product).build());
		// Bundle extends Product, and the discriminator holds the concrete name.
		assertThat(plan.query().toString()).contains("Bundle").contains("Product");
	}

	@Test
	void aQueryFromTheSubtypeDoesNotWidenToItsSupertype() throws Exception {
		LuceneQueryPlan plan = translate(QueryBuilder.from(models.eClass("Bundle")).build());
		assertThat(plan.query().toString()).contains("Bundle").doesNotContain("_type:Product");
	}

	// --- shapes and paging ------------------------------------------------------------------

	@Test
	void countOnlyBecomesTheCountShape() throws Exception {
		assertThat(translate(QueryBuilder.from(product).countOnly().build()).shape())
				.isEqualTo(QueryShape.COUNT);
	}

	@Test
	void aSelectionBecomesTheProjectionShapeWithItsColumns() throws Exception {
		LuceneQueryPlan plan = translate(QueryBuilder.from(product)
				.selectAs("key", feature("Product", "id"))
				.build());
		assertThat(plan.shape()).isEqualTo(QueryShape.PROJECTION);
		assertThat(plan.rowFields()).containsExactly("id");
		assertThat(plan.rowAliases()).containsExactly("key");
	}

	@Test
	void projectingAFieldTheMappingDoesNotStoreIsRefused() {
		Query query = QueryBuilder.from(product).select(feature("Product", "stock")).build();
		assertThatThrownBy(() -> translate(query))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("does not store")
				.hasMessageContaining("stored=true");
	}

	@Test
	void topAndSkipBecomeThePagingWindow() throws Exception {
		LuceneQueryPlan plan = translate(QueryBuilder.from(product).top(25).skip(50).build());
		assertThat(plan.limit()).isEqualTo(25);
		assertThat(plan.skip()).isEqualTo(50);
	}

	@Test
	void withoutTopThePlanIsUnbounded() throws Exception {
		assertThat(translate(QueryBuilder.from(product).build()).limit()).isEqualTo(-1);
	}

	// --- sorting -----------------------------------------------------------------------------

	@Test
	void sortingUsesDocValuesOfTheRightShape() throws Exception {
		LuceneQueryPlan plan = translate(QueryBuilder.from(product)
				.orderByAsc(feature("Product", "stock"))
				.orderByDesc(feature("Product", "id"))
				.build());
		assertThat(plan.sort()).isNotNull();
		assertThat(plan.sort().getSort()).hasSize(2);
		assertThat(plan.sort().getSort()[0].getField()).isEqualTo("stock");
		assertThat(plan.sort().getSort()[1].getReverse()).isTrue();
	}

	@Test
	void sortingOnAnalyzedTextIsRefusedWithTheWayOut() {
		Query query = QueryBuilder.from(product).orderByAsc(feature("Product", "name")).build();
		assertThatThrownBy(() -> translate(query))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("analyzed text")
				.hasMessageContaining("keyword field");
	}

	@Test
	void sortingByAComputedExpressionIsRefused() {
		Query query = QueryBuilder.from(product).orderByDesc(score().toExpression()).build();
		assertThatThrownBy(() -> translate(query))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("SORT_EXPRESSION");
	}

	// --- refusals ----------------------------------------------------------------------------

	@Test
	void exactComparisonOnAnalyzedTextIsRefusedRatherThanApproximated() {
		Query query = QueryBuilder.from(product).where(path(feature("Product", "name")).eq("Widget")).build();
		assertThatThrownBy(() -> translate(query))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("analyzed text field")
				.hasMessageContaining("keyword");
	}

	@Test
	void comparingTwoFieldsIsRefused() {
		Query query = QueryBuilder.from(product)
				.where(path(feature("Product", "stock")).eq(path(feature("Product", "price")).toPath()))
				.build();
		assertThatThrownBy(() -> translate(query))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("FIELD_TO_FIELD");
	}

	@Test
	void orderingAnEnumIsRefusedBecauseItsIndexOrderIsNotTheModelsOrder() {
		Query query = QueryBuilder.from(product)
				.where(path(feature("Product", "condition")).gt("NEW"))
				.build();
		assertThatThrownBy(() -> translate(query))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("enum")
				.hasMessageContaining("alphabetically");
	}

	@Test
	void aQuantifierOverTheNestedBlockTranslates() throws Exception {
		// Declared since S11 (#9); the behaviour lives in BlockJoinQuantifierTest — here
		// only that the processor accepts what its capabilities now claim.
		EReference reviews = (EReference) feature("Product", "reviews");
		Query query = QueryBuilder.from(product)
				.where(Expressions.any(Expressions.propertyPath(reviews),
						it -> it.path(feature("Review", "rating")).gt(3)))
				.build();
		assertThat(translate(query).query().toString()).contains("ToParentBlockJoinQuery");
	}

	@Test
	void aQuantifierOverAnUnmappedReferenceIsRefusedByName() {
		EReference manufacturer = (EReference) feature("Product", "manufacturer");
		Query query = QueryBuilder.from(product)
				.where(Expressions.any(Expressions.propertyPath(manufacturer),
						it -> it.path(feature("Manufacturer", "name")).eq("Acme")))
				.build();
		assertThatThrownBy(() -> translate(query))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("NESTED");
	}

	@Test
	void aPipelineIsRefusedWithThePlannedAlternative() {
		Query query = QueryBuilder.from(product).groupBy(feature("Product", "condition"))
				.countOf("n").build();
		assertThatThrownBy(() -> translate(query))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("facets");
	}

	@Test
	void expandIsRefusedAsTheJoinItIs() {
		Query query = QueryBuilder.from(product).expand(feature("Product", "manufacturer")).build();
		assertThatThrownBy(() -> translate(query))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("EXPAND");
	}

	@Test
	void distinctIsRefused() {
		Query query = QueryBuilder.from(product).distinct().build();
		assertThatThrownBy(() -> translate(query))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("DISTINCT");
	}

	@Test
	void aNegationStillRefusesWhatTheLeafCannotDo() {
		// The push-down must not swallow a refusal on the way down.
		Query query = QueryBuilder.from(product)
				.where(not(path(feature("Product", "name")).eq("Widget")))
				.build();
		assertThatThrownBy(() -> translate(query)).isInstanceOf(QueryException.class);
	}

	// --- helpers -----------------------------------------------------------------------------

	private static EStructuralFeature feature(String className, String featureName) {
		return models.feature(className, featureName);
	}

	private static LuceneQueryProcessor processor() {
		return LuceneQueryProcessor.of(IndexSchema.of(mapping()), null);
	}

	private static LuceneQueryPlan translate(Query query) throws QueryException {
		QueryContext context = QueryContexts.of(product, null);
		return (LuceneQueryPlan) processor().translate(query, context);
	}

	/**
	 * A mapping that keeps the conventions but declares the two things the tests need to be
	 * explicit about: a stored, sortable keyword for the id, and reviews as a block.
	 */
	private static IndexUnitMapping mapping() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(models.ePackage());
		var document = ESEARCH.createDocumentMapping();
		document.setEClass(product);
		KeywordFieldMapping id = ESEARCH.createKeywordFieldMapping();
		id.setFeature((org.eclipse.emf.ecore.EAttribute) feature("Product", "id"));
		id.setStored(true);
		id.setDocValues(true);
		document.getFields().add(id);
		ReferenceMapping reviews = ESEARCH.createReferenceMapping();
		reviews.setEReference((EReference) feature("Product", "reviews"));
		reviews.setStrategy(ReferenceStrategy.NESTED);
		document.getReferences().add(reviews);
		mapping.getDocuments().add(document);
		return mapping;
	}

	private static String flatten(Diagnostic diagnostic) {
		StringBuilder text = new StringBuilder(diagnostic.getMessage() == null ? "" : diagnostic.getMessage());
		for (Diagnostic child : diagnostic.getChildren()) {
			text.append(' ').append(flatten(child));
		}
		return text.toString();
	}
}
