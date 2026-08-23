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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.api.QueryResult;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.FieldUse;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.TextFieldMapping;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappedDocument;
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
 * Sub-field projections on the <em>read</em> side (#39): the mapper has always written
 * {@code title} plus {@code title.keyword}, and the translation could only ever address the
 * first — so an equality against an analyzed field either missed or forced the mapping author
 * to declare two sibling fields, which is what sub-fields exist to avoid.
 * <p>
 * What decides which projection answers is {@code FieldUse}: declared where a modeller needs
 * to say it, derived from the field kind where they do not (a text field matches and
 * highlights, a keyword field is exact, sorts and facets, a numeric field ranges and sorts).
 */
class SubFieldProjectionTest {

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

	// --- which projection answers ---------------------------------------------------------------

	@Test
	void anEqualityFindsTheKeywordSubFieldOfAnAnalyzedField() throws Exception {
		SearchResource resource = indexed(textWithKeywordSubField());

		// Before #39 this refused: the primary projection is analyzed, and an exact
		// comparison cannot be answered on tokens.
		assertThat(ids(resource, QueryBuilder.from(product)
				.where(path(name()).eq("Espresso Machine")).build()))
				.containsExactly("p-1");
	}

	@Test
	void aMatchStaysOnTheAnalyzedPrimary() throws Exception {
		SearchResource resource = indexed(textWithKeywordSubField());

		// One word of the value: only the analyzed projection can answer that, and the
		// keyword sub-field must not steal it.
		assertThat(ids(resource, QueryBuilder.from(product)
				.where(path(description()).contains("coffee")).build()))
				.containsExactlyInAnyOrder("p-1", "p-2");
	}

	@Test
	void aSortReadsTheProjectionThatHasDocValues() throws Exception {
		SearchResource resource = indexed(textWithKeywordSubField());

		// Sorting an analyzed field is impossible; its keyword sub-field carries doc values.
		assertThat(ids(resource, QueryBuilder.from(product)
				.where(path(description()).contains("coffee"))
				.orderByAsc(name())
				.build()))
				.containsExactly("p-1", "p-2");
	}

	@Test
	void aDeclaredUseOverridesWhatTheKindWouldImply() throws Exception {
		// Two keyword projections of one attribute: the primary is for sorting, the
		// sub-field for exact matching. Nothing about their kinds could tell them apart.
		IndexUnitMapping mapping = plain();
		DocumentMapping document = document(mapping);
		KeywordFieldMapping primary = ESEARCH.createKeywordFieldMapping();
		primary.setFeature(name());
		primary.setDocValues(true);
		primary.getUse().add(FieldUse.SORT);
		KeywordFieldMapping exact = ESEARCH.createKeywordFieldMapping();
		exact.setName("exact");
		exact.getUse().add(FieldUse.EXACT);
		primary.getSubFields().add(exact);
		document.getFields().add(primary);

		IndexSchema schema = IndexSchema.of(mapping);
		assertThat(schema.resolve(product, name(), FieldUse.EXACT).name()).isEqualTo("name.exact");
		assertThat(schema.resolve(product, name(), FieldUse.SORT).name()).isEqualTo("name");
	}

	@Test
	void anAttributeWithOneProjectionAnswersEverythingAsBefore() {
		IndexSchema schema = IndexSchema.of(plain());

		// The ordinary mapping: no sub-field, so every use resolves to the primary and
		// nothing about the old behaviour changes.
		for (FieldUse use : List.of(FieldUse.EXACT, FieldUse.MATCH, FieldUse.SORT, FieldUse.RANGE)) {
			assertThat(schema.resolve(product, name(), use).name()).isEqualTo("name");
		}
	}

	@Test
	void everyProjectionOfAnAttributeIsAddressable() {
		IndexSchema schema = IndexSchema.of(textWithKeywordSubField());

		assertThat(schema.projections(product, name())).extracting(IndexSchema.Field::name)
				.as("what the writer produces is what the reader may address")
				.containsExactly("name", "name.keyword");
	}

	// --- what the mapping may not say -------------------------------------------------------------

	@Test
	void twoProjectionsClaimingOneUseAreRefusedAtMappingTime() {
		IndexUnitMapping mapping = plain();
		DocumentMapping document = document(mapping);
		KeywordFieldMapping primary = ESEARCH.createKeywordFieldMapping();
		primary.setFeature(name());
		KeywordFieldMapping second = ESEARCH.createKeywordFieldMapping();
		second.setName("other");
		primary.getSubFields().add(second);
		document.getFields().add(primary);

		// Both are keyword projections, so both derive EXACT, SORT and FACET — and a query
		// would be answered by whichever comes first in the file.
		assertThatThrownBy(() -> IndexSchema.of(mapping))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("both serve EXACT")
				.hasMessageContaining("Declare `use`");
	}

	// --- fixture ------------------------------------------------------------------------------------

	/** The pair §4 describes: analyzed text for matching, a keyword sub-field for the rest. */
	private IndexUnitMapping textWithKeywordSubField() {
		IndexUnitMapping mapping = plain();
		DocumentMapping document = document(mapping);
		TextFieldMapping text = ESEARCH.createTextFieldMapping();
		text.setFeature(name());
		KeywordFieldMapping keyword = ESEARCH.createKeywordFieldMapping();
		keyword.setName("keyword");
		keyword.setDocValues(true);
		text.getSubFields().add(keyword);
		document.getFields().add(text);
		return mapping;
	}

	private IndexUnitMapping plain() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog.ePackage());
		return mapping;
	}

	private DocumentMapping document(IndexUnitMapping mapping) {
		DocumentMapping document = ESEARCH.createDocumentMapping();
		document.setEClass(product);
		mapping.getDocuments().add(document);
		return document;
	}

	private SearchResource indexed(IndexUnitMapping mapping) throws IOException {
		DocumentMapper mapper = DocumentMapper.of(IndexSchema.of(mapping));
		index(mapper, catalog.create("Product", "id", "p-1", "name", "Espresso Machine",
				"description", "coffee at home"));
		index(mapper, catalog.create("Product", "id", "p-2", "name", "Grinder",
				"description", "coffee beans"));
		unit.refresh();
		return new SearchResource(URI.createURI("lucene://catalog/Product"), unit, mapper);
	}

	private void index(DocumentMapper mapper, EObject object) throws IOException {
		MappedDocument mapped = mapper.map(object);
		unit.updateDocuments(mapped.term(), mapped.documents());
	}

	private List<Object> ids(SearchResource resource, Query query) throws Exception {
		try (QueryResult result = resource.query(query)) {
			return result.objects()
					.map(hit -> hit.eGet(hit.eClass().getEStructuralFeature("id")))
					.toList();
		}
	}

	private EAttribute name() {
		return (EAttribute) catalog.feature("Product", "name");
	}

	private EAttribute description() {
		return (EAttribute) catalog.feature("Product", "description");
	}
}
