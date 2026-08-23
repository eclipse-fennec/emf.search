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
import static org.eclipse.fennec.model.query.builder.Expressions.path;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.api.QueryResult;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.NumericFieldMapping;
import org.eclipse.fennec.search.esearch.RankFunction;
import org.eclipse.fennec.search.esearch.RankSignalFieldMapping;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.MappingException;
import org.eclipse.fennec.search.mapping.TestModels;
import org.eclipse.fennec.search.resource.SearchResource;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Static rank signals (S14, #16, §5.3): a declared {@code FeatureField} folded into the
 * score of an ordinary query, selected by name through {@link SearchOptions#RANK_SIGNALS}.
 * <p>
 * The corpus is built so the text score cannot explain the order: every product carries the
 * <em>same</em> description, so with no signal selected they tie, and any ordering that
 * appears when one is selected comes from the signal alone. That is also why the assertions
 * are ordinal — a feature weight is quantized on the way in, so the absolute contribution
 * is not a contract.
 */
class RankSignalTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;
	private static EClass product;

	private IndexUnit unit;

	@BeforeAll
	static void loadModel() {
		catalog = TestModels.load("catalog.ecore");
		product = catalog.eClass("Product");
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

	// --- what a signal does ------------------------------------------------------------------

	@Test
	void aSelectedSignalRanksTheDocumentThatCarriesItFirst() throws Exception {
		SearchResource resource = indexed(saturation(10.0));

		assertThat(names(resource, coffee(), signals("stock")))
				.as("identical text, so the order is the signal's doing")
				.containsExactly("popular", "quiet", "unknown");
	}

	@Test
	void withoutTheOptionTheSignalDoesNothing() throws Exception {
		SearchResource resource = indexed(saturation(10.0));
		Query query = QueryBuilder.from(product)
				.where(path(description()).contains("coffee")).withScores().build();

		try (QueryResult result = resource.query(query, null, Map.of())) {
			assertThat(result.hits().map(hit -> hit.score()).distinct().count())
					.as("a declared signal nobody selected leaves the score to the text")
					.isEqualTo(1);
		}
	}

	@Test
	void aDocumentWithoutTheSignalStillMatches() throws Exception {
		SearchResource resource = indexed(saturation(10.0));

		// The signal joins as SHOULD: it can only add score, never take a hit away — and an
		// unset attribute is no signal, exactly like a value that is not positive.
		assertThat(names(resource, coffee(), signals("stock"))).contains("unknown");
	}

	@Test
	void aNonPositiveSignalIsNoSignalAtAll() throws Exception {
		SearchResource resource = indexed(saturation(10.0),
				catalog.create("Product", "id", "zero", "name", "zero", "stock", 0,
						"description", "coffee grinder kitchen"),
				catalog.create("Product", "id", "one", "name", "one", "stock", 1,
						"description", "coffee grinder kitchen"));

		assertThat(names(resource, coffee(), signals("stock")))
				.as("zero cannot be a weight, so that document ranks below the one that has one")
				.containsExactly("one", "zero");
	}

	@Test
	void theLogFunctionRanksTheSameWayForSignalsSpanningMagnitudes() throws Exception {
		RankSignalFieldMapping signal = signal(RankFunction.LOG);
		signal.setPivot(1.0);
		SearchResource resource = indexed(signal);

		assertThat(names(resource, coffee(), signals("stock")))
				.containsExactly("popular", "quiet", "unknown");
	}

	@Test
	void aSignalDeclaredAsASubFieldKeepsTheAttributeItself() throws Exception {
		SearchResource resource = indexed(numericWithRankSubField());

		// The primary projection is an ordinary numeric field, so the attribute is still
		// comparable — and the sub-field feeds the score under its compound name.
		Query filtered = QueryBuilder.from(product)
				.where(path(stock()).gt(5)).build();
		assertThat(names(resource, filtered, Map.of())).containsExactly("popular");
		assertThat(names(resource, coffee(), signals("stock.signal")))
				.containsExactly("popular", "quiet", "unknown");
	}

	// --- what it refuses ---------------------------------------------------------------------

	@Test
	void anUndeclaredSignalNameIsRefusedWithTheDeclaredOnes() throws Exception {
		SearchResource resource = indexed(saturation(10.0));

		assertThatThrownBy(() -> resource.query(coffee(), null, signals("virality")))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("virality")
				.hasMessageContaining("stock");
	}

	@Test
	void aCountCarriesNoScoreAndRefusesSignals() throws Exception {
		SearchResource resource = indexed(saturation(10.0));
		Query count = QueryBuilder.from(product)
				.where(path(description()).contains("coffee")).countOnly().build();

		assertThatThrownBy(() -> resource.query(count, null, signals("stock")))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("COUNT");
	}

	@Test
	void aSigmoidWithoutAPivotIsRefusedRatherThanGuessed() throws Exception {
		SearchResource resource = indexed(signal(RankFunction.SIGMOID));

		assertThatThrownBy(() -> resource.query(coffee(), null, signals("stock")))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("SIGMOID");
	}

	@Test
	void aRankSignalIsNotAComparableField() throws Exception {
		SearchResource resource = indexed(saturation(10.0));
		Query query = QueryBuilder.from(product).where(path(stock()).gt(5)).build();

		assertThatThrownBy(() -> resource.query(query, null, Map.of()))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("rank signal");
	}

	@Test
	void aManyValuedAttributeCannotCarryASignal() {
		RankSignalFieldMapping signal = ESEARCH.createRankSignalFieldMapping();
		signal.setFeature((EAttribute) catalog.feature("Product", "tags"));
		DocumentMapper mapper = DocumentMapper.of(mappingWith(signal));

		assertThatThrownBy(() -> mapper.map(catalog.create("Product", "id", "p-1", "name", "p-1")))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("one number per document");
	}

	@Test
	void twoSignalsUnderOneNameThatDisagreeAreRefused() throws Exception {
		IndexUnitMapping mapping = mappingWith(saturation(10.0));
		DocumentMapping bundle = ESEARCH.createDocumentMapping();
		bundle.setEClass(catalog.eClass("Bundle"));
		bundle.getFields().add(saturation(99.0));
		mapping.getDocuments().add(bundle);
		SearchResource resource = new SearchResource(URI.createURI("lucene://catalog/Product"), unit,
				DocumentMapper.of(mapping));

		assertThatThrownBy(() -> resource.query(coffee(), null, signals("stock")))
				.isInstanceOf(IOException.class)
				.hasMessageContaining("One name is one signal");
	}

	@Test
	void anOptionValueThatNamesNothingIsRefused() {
		assertThatThrownBy(() -> SearchOptions.rankSignals(Map.of(SearchOptions.RANK_SIGNALS, 42)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining(SearchOptions.RANK_SIGNALS);
	}

	// --- fixture ------------------------------------------------------------------------------

	/** The default corpus: one description for all three, so only the signal can order them. */
	private SearchResource indexed(RankSignalFieldMapping signal) throws IOException {
		return indexed(signal,
				catalog.create("Product", "id", "popular", "name", "popular", "stock", 1000,
						"description", "coffee grinder kitchen"),
				catalog.create("Product", "id", "quiet", "name", "quiet", "stock", 1,
						"description", "coffee grinder kitchen"),
				catalog.create("Product", "id", "unknown", "name", "unknown",
						"description", "coffee grinder kitchen"));
	}

	private SearchResource indexed(RankSignalFieldMapping signal, EObject... corpus) throws IOException {
		return indexed(mappingWith(signal), corpus);
	}

	private SearchResource indexed(NumericFieldMapping primary) throws IOException {
		return indexed(mappingWith(primary),
				catalog.create("Product", "id", "popular", "name", "popular", "stock", 1000,
						"description", "coffee grinder kitchen"),
				catalog.create("Product", "id", "quiet", "name", "quiet", "stock", 1,
						"description", "coffee grinder kitchen"),
				catalog.create("Product", "id", "unknown", "name", "unknown",
						"description", "coffee grinder kitchen"));
	}

	private SearchResource indexed(IndexUnitMapping mapping, EObject... corpus) throws IOException {
		DocumentMapper mapper = DocumentMapper.of(mapping);
		for (EObject object : corpus) {
			var mapped = mapper.map(object);
			unit.updateDocuments(mapped.term(), mapped.documents());
		}
		unit.refresh();
		return new SearchResource(URI.createURI("lucene://catalog/Product"), unit, mapper);
	}

	private IndexUnitMapping mappingWith(org.eclipse.fennec.search.esearch.FieldMapping field) {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog.ePackage());
		DocumentMapping document = ESEARCH.createDocumentMapping();
		document.setEClass(product);
		document.getFields().add(field);
		mapping.getDocuments().add(document);
		return mapping;
	}

	private RankSignalFieldMapping saturation(double pivot) {
		RankSignalFieldMapping signal = signal(RankFunction.SATURATION);
		signal.setPivot(pivot);
		return signal;
	}

	private RankSignalFieldMapping signal(RankFunction function) {
		RankSignalFieldMapping signal = ESEARCH.createRankSignalFieldMapping();
		signal.setFeature(stock());
		signal.setFunction(function);
		return signal;
	}

	/** stock as an ordinary numeric field, with the signal beside it as a sub-field. */
	private NumericFieldMapping numericWithRankSubField() {
		NumericFieldMapping primary = ESEARCH.createNumericFieldMapping();
		primary.setFeature(stock());
		RankSignalFieldMapping signal = ESEARCH.createRankSignalFieldMapping();
		signal.setName("signal");
		signal.setFunction(RankFunction.SATURATION);
		signal.setPivot(10.0);
		primary.getSubFields().add(signal);
		return primary;
	}

	private Query coffee() {
		return QueryBuilder.from(product).where(path(description()).contains("coffee")).build();
	}

	private static Map<String, Object> signals(String... names) {
		return Map.of(SearchOptions.RANK_SIGNALS, List.of(names));
	}

	private List<Object> names(SearchResource resource, Query query, Map<?, ?> options)
			throws Exception {
		try (QueryResult result = resource.query(query, null, options)) {
			return result.objects()
					.map(hit -> hit.eGet(hit.eClass().getEStructuralFeature("name")))
					.toList();
		}
	}

	private EAttribute description() {
		return (EAttribute) catalog.feature("Product", "description");
	}

	private EAttribute stock() {
		return (EAttribute) catalog.feature("Product", "stock");
	}
}
