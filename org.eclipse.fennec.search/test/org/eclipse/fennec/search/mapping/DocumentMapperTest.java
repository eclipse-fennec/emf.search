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

import java.util.Date;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.RangeFieldMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.esearch.TextFieldMapping;
import org.eclipse.fennec.search.esearch.VectorFieldMapping;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * What an EObject becomes. The fixtures are a small ecore loaded reflectively, so a new case
 * costs a few lines of model rather than a code-generation round.
 */
class DocumentMapperTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;

	@BeforeAll
	static void loadModel() {
		catalog = TestModels.load("catalog.ecore");
	}

	private static IndexUnitMapping unit() {
		IndexUnitMapping unit = ESEARCH.createIndexUnitMapping();
		unit.setName("catalog");
		unit.setEPackage(catalog.ePackage());
		return unit;
	}

	private static DocumentMapping document(IndexUnitMapping unit, String className) {
		DocumentMapping mapping = ESEARCH.createDocumentMapping();
		mapping.setEClass(catalog.eClass(className));
		unit.getDocuments().add(mapping);
		return mapping;
	}

	private static List<String> valuesOf(Document document, String field) {
		return List.of(document.getValues(field));
	}

	private static IndexableField field(Document document, String name) {
		return document.getField(name);
	}

	private static EObject product(String id, String name) {
		return catalog.create("Product", "id", id, "name", name);
	}

	// --- conventions ----------------------------------------------------------------------

	@Test
	void anUnmappedClassIsIndexedByConvention() {
		DocumentMapper mapper = DocumentMapper.of(unit());

		MappedDocument mapped = mapper.map(catalog.create("Product",
				"id", "p-1", "name", "Espresso Machine", "price", 499.0, "stock", 7, "available", true));

		Document document = mapped.root();
		assertThat(mapped.isBlock()).as("a flat object is a block of one").isFalse();
		assertThat(document.get(SearchFields.ID)).isEqualTo("p-1");
		assertThat(document.get(SearchFields.ROOT)).isEqualTo("p-1");
		assertThat(document.get(SearchFields.TYPE)).isEqualTo("Product");
		assertThat(document.get(SearchFields.PARENT)).isEqualTo(SearchFields.PARENT_VALUE);
		assertThat(field(document, "name")).as("a string becomes an analyzed text field")
				.isNotNull()
				.extracting(f -> f.fieldType().tokenized()).isEqualTo(true);
		assertThat(field(document, "price")).as("a numeric becomes a point field").isNotNull();
		assertThat(field(document, "available")).as("a boolean becomes a keyword")
				.extracting(f -> f.fieldType().tokenized()).isEqualTo(false);
	}

	@Test
	void theIdAttributeBecomesAKeywordAndNotAnalyzedText() {
		DocumentMapper mapper = DocumentMapper.of(unit());

		Document document = mapper.map(product("p-1", "x")).root();

		assertThat(field(document, "id").fieldType().tokenized())
				.as("an id must match exactly, so it is never analyzed").isFalse();
	}

	@Test
	void multiValuedAndEnumAttributesAreMapped() {
		DocumentMapper mapper = DocumentMapper.of(unit());
		EObject withTags = catalog.create("Product", "id", "p-2", "tags", List.of("coffee", "kitchen"),
				"condition", catalog.eClass("Product").getEPackage().getEClassifier("Condition")
						.eClass().getEPackage() == null ? null : conditionLiteral("USED"));

		Document document = mapper.map(withTags).root();

		assertThat(valuesOf(document, "tags")).containsExactly("coffee", "kitchen");
		assertThat(document.get("condition")).isEqualTo("USED");
	}

	private static Object conditionLiteral(String name) {
		org.eclipse.emf.ecore.EEnum condition = (org.eclipse.emf.ecore.EEnum) catalog.ePackage()
				.getEClassifier("Condition");
		return condition.getEEnumLiteral(name).getInstance();
	}

	@Test
	void aDateBecomesAPointOnEpochMillis() {
		DocumentMapper mapper = DocumentMapper.of(unit());
		Date released = new Date(1_700_000_000_000L);

		Document document = mapper.map(catalog.create("Product", "id", "p-3", "released", released)).root();

		assertThat(field(document, "released")).as("temporal attributes are indexed as points").isNotNull();
	}

	@Test
	void aSubclassKeepsItsOwnTypeName() {
		IndexUnitMapping unit = unit();
		document(unit, "Product");
		DocumentMapper mapper = DocumentMapper.of(unit);

		Document document = mapper.map(catalog.create("Bundle", "id", "b-1", "itemCount", 3)).root();

		assertThat(document.get(SearchFields.TYPE))
				.as("otherwise a type filter could not tell subclass from superclass")
				.isEqualTo("Bundle");
	}

	@Test
	void anObjectWithoutAnIdIsRefused() {
		DocumentMapper mapper = DocumentMapper.of(unit());

		assertThatThrownBy(() -> mapper.map(catalog.create("Product", "name", "no id")))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("no value for its id");
	}

	@Test
	void switchingOffAutoMapRefusesUndeclaredClasses() {
		IndexUnitMapping unit = unit();
		unit.setAutoMap(false);
		DocumentMapper mapper = DocumentMapper.of(unit);

		assertThatThrownBy(() -> mapper.map(product("p-1", "x")))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("does not index Product");
	}

	// --- declarations ---------------------------------------------------------------------

	@Test
	void aDeclaredFieldOverridesTheConvention() {
		IndexUnitMapping unit = unit();
		DocumentMapping product = document(unit, "Product");
		KeywordFieldMapping keyword = ESEARCH.createKeywordFieldMapping();
		keyword.setFeature((EAttribute) catalog.feature("Product", "name"));
		product.getFields().add(keyword);
		DocumentMapper mapper = DocumentMapper.of(unit);

		Document document = mapper.map(product("p-1", "Espresso Machine")).root();

		assertThat(field(document, "name").fieldType().tokenized())
				.as("declared keyword beats the string convention").isFalse();
		assertThat(document.get("name")).isEqualTo("Espresso Machine");
	}

	@Test
	void aFieldCanBeProjectedTwiceThroughASubField() {
		IndexUnitMapping unit = unit();
		DocumentMapping product = document(unit, "Product");
		TextFieldMapping text = ESEARCH.createTextFieldMapping();
		text.setFeature((EAttribute) catalog.feature("Product", "name"));
		KeywordFieldMapping sub = ESEARCH.createKeywordFieldMapping();
		sub.setName("keyword");
		sub.setDocValues(true);
		text.getSubFields().add(sub);
		product.getFields().add(text);
		DocumentMapper mapper = DocumentMapper.of(unit);

		Document document = mapper.map(product("p-1", "Espresso Machine")).root();

		assertThat(field(document, "name").fieldType().tokenized()).as("primary stays analyzed").isTrue();
		assertThat(field(document, "name.keyword")).as("the sub-field is named parent.child").isNotNull();
		assertThat(field(document, "name.keyword").fieldType().tokenized()).isFalse();
	}

	@Test
	void aSubFieldWithoutANameIsRefused() {
		IndexUnitMapping unit = unit();
		DocumentMapping product = document(unit, "Product");
		TextFieldMapping text = ESEARCH.createTextFieldMapping();
		text.setFeature((EAttribute) catalog.feature("Product", "name"));
		text.getSubFields().add(ESEARCH.createKeywordFieldMapping());
		product.getFields().add(text);
		DocumentMapper mapper = DocumentMapper.of(unit);

		assertThatThrownBy(() -> mapper.map(product("p-1", "x")))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("relative and mandatory");
	}

	@Test
	void anUnimplementedFieldKindIsRefusedRatherThanIndexedAsSomethingElse() {
		IndexUnitMapping unit = unit();
		DocumentMapping product = document(unit, "Product");
		VectorFieldMapping vector = ESEARCH.createVectorFieldMapping();
		vector.setFeature((EAttribute) catalog.feature("Product", "description"));
		product.getFields().add(vector);
		DocumentMapper mapper = DocumentMapper.of(unit);

		assertThatThrownBy(() -> mapper.map(catalog.create("Product", "id", "p-1", "description", "text")))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("reserved for wave 2");
	}

	@Test
	void anIntervalFieldIsRefusedAndNamesTheWayToAskToday() {
		// S15 (#17) waits on query vocabulary (emf.persistence-jpa#215): a range field nothing
		// can ask about would be dead weight. The refusal has to carry the fallback, because
		// that is the whole answer a modeller needs today.
		IndexUnitMapping unit = unit();
		DocumentMapping product = document(unit, "Product");
		RangeFieldMapping validity = ESEARCH.createRangeFieldMapping();
		validity.setLowerBound((EAttribute) catalog.feature("Product", "price"));
		validity.setUpperBound((EAttribute) catalog.feature("Product", "stock"));
		product.getFields().add(validity);
		DocumentMapper mapper = DocumentMapper.of(unit);

		assertThatThrownBy(() -> mapper.map(product("p-1", "x")))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("emf.persistence-jpa#215")
				.hasMessageContaining("two comparisons");
	}

	// --- references -------------------------------------------------------------------------

	@Test
	void anIdOnlyReferenceStoresTheTargetId() {
		IndexUnitMapping unit = unit();
		DocumentMapping product = document(unit, "Product");
		ReferenceMapping manufacturer = ESEARCH.createReferenceMapping();
		manufacturer.setEReference((EReference) catalog.feature("Product", "manufacturer"));
		manufacturer.setStrategy(ReferenceStrategy.ID_ONLY);
		product.getReferences().add(manufacturer);
		DocumentMapper mapper = DocumentMapper.of(unit);

		EObject object = catalog.create("Product", "id", "p-1", "manufacturer",
				catalog.create("Manufacturer", "id", "m-1", "name", "Acme"));

		Document document = mapper.map(object).root();

		assertThat(document.get("manufacturer")).isEqualTo("m-1");
		assertThat(document.get("manufacturer.name")).as("id only means id only").isNull();
	}

	@Test
	void anEmbeddedReferenceFlattensTheTargetUnderAPrefix() {
		IndexUnitMapping unit = unit();
		DocumentMapping product = document(unit, "Product");
		ReferenceMapping manufacturer = ESEARCH.createReferenceMapping();
		manufacturer.setEReference((EReference) catalog.feature("Product", "manufacturer"));
		manufacturer.setStrategy(ReferenceStrategy.EMBED);
		product.getReferences().add(manufacturer);
		DocumentMapper mapper = DocumentMapper.of(unit);

		EObject object = catalog.create("Product", "id", "p-1", "manufacturer",
				catalog.create("Manufacturer", "id", "m-1", "name", "Acme"));

		MappedDocument mapped = mapper.map(object);

		assertThat(mapped.isBlock()).as("embedding stays in one document").isFalse();
		assertThat(mapped.root().get("manufacturer.name")).isEqualTo("Acme");
	}

	@Test
	void aNestedReferenceProducesAChildDocumentPerTarget() {
		IndexUnitMapping unit = unit();
		DocumentMapping product = document(unit, "Product");
		ReferenceMapping reviews = ESEARCH.createReferenceMapping();
		reviews.setEReference((EReference) catalog.feature("Product", "reviews"));
		reviews.setStrategy(ReferenceStrategy.NESTED);
		product.getReferences().add(reviews);
		DocumentMapper mapper = DocumentMapper.of(unit);

		EObject object = catalog.create("Product", "id", "p-1", "reviews", List.of(
				catalog.create("Review", "id", "r-1", "author", "ada", "rating", 5),
				catalog.create("Review", "id", "r-2", "author", "linus", "rating", 3)));

		MappedDocument mapped = mapper.map(object);

		assertThat(mapped.documents()).hasSize(3);
		assertThat(mapped.isBlock()).isTrue();
		assertThat(mapped.root().get(SearchFields.ID)).as("the parent is last").isEqualTo("p-1");
		assertThat(mapped.documents().get(0).get("author")).isEqualTo("ada");
		assertThat(mapped.documents().get(0).get(SearchFields.PARENT))
				.as("only the root carries the parent marker").isNull();
		assertThat(mapped.documents()).allSatisfy(document ->
				assertThat(document.get(SearchFields.ROOT))
						.as("every document of the block carries the root id").isEqualTo("p-1"));
		assertThat(mapped.term().field()).isEqualTo(SearchFields.ROOT);
		assertThat(mapped.term().text()).isEqualTo("p-1");
	}

	@Test
	void nestingANonContainmentReferenceIsRefused() {
		IndexUnitMapping unit = unit();
		DocumentMapping product = document(unit, "Product");
		ReferenceMapping manufacturer = ESEARCH.createReferenceMapping();
		manufacturer.setEReference((EReference) catalog.feature("Product", "manufacturer"));
		manufacturer.setStrategy(ReferenceStrategy.NESTED);
		product.getReferences().add(manufacturer);
		DocumentMapper mapper = DocumentMapper.of(unit);

		EObject object = catalog.create("Product", "id", "p-1", "manufacturer",
				catalog.create("Manufacturer", "id", "m-1"));

		assertThatThrownBy(() -> mapper.map(object))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("not a containment reference");
	}

	@Test
	void aContainedChildWithoutAnIdIsAddressedByItsPosition() {
		IndexUnitMapping unit = unit();
		DocumentMapping product = document(unit, "Product");
		ReferenceMapping reviews = ESEARCH.createReferenceMapping();
		reviews.setEReference((EReference) catalog.feature("Product", "reviews"));
		reviews.setStrategy(ReferenceStrategy.NESTED);
		product.getReferences().add(reviews);
		DocumentMapper mapper = DocumentMapper.of(unit);

		EObject object = catalog.create("Product", "id", "p-1", "reviews",
				List.of(catalog.create("Review", "author", "anonymous")));

		MappedDocument mapped = mapper.map(object);

		assertThat(mapped.documents().get(0).get(SearchFields.ID)).isEqualTo("p-1#reviews.0");
	}
}
