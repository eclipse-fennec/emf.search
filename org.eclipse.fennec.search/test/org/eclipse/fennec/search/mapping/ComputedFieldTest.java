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
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.FeatureSource;
import org.eclipse.fennec.search.esearch.FieldMapping;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.NumericFieldMapping;
import org.eclipse.fennec.search.esearch.OclContextKind;
import org.eclipse.fennec.search.esearch.OclSource;
import org.eclipse.fennec.search.esearch.PathSource;
import org.eclipse.fennec.search.esearch.TextFieldMapping;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The extraction ladder of §4.2 (S20, #28): where a field's value comes from when it is not
 * one attribute of the mapped object — a navigation, or several sources at once.
 * <p>
 * The bias the ladder exists for is what most of these cases pin: a source is checked against
 * the metamodel when the mapping is read, so a mapping that cannot work is refused while a
 * human is still watching rather than per document at index time. The third rung, an OCL
 * expression, is declared in the metamodel and refused here — it is the one that could not be
 * checked that way, and it would put an expression engine into every deployment's load path.
 */
class ComputedFieldTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;
	private static EClass product;

	@BeforeAll
	static void loadModel() {
		catalog = TestModels.load("catalog.ecore");
		product = catalog.eClass("Product");
	}

	// --- the rungs ---------------------------------------------------------------------------

	@Test
	void aFeatureSourceReadsOneAttribute() {
		KeywordFieldMapping field = keyword("label");
		field.getSources().add(featureSource(name()));

		Document document = write(mapping(field), product("p-1", "Espresso", 499.0));

		assertThat(document.get("label")).isEqualTo("Espresso");
	}

	@Test
	void aPathSourceNavigatesToAnotherObjectsValue() {
		KeywordFieldMapping field = keyword("maker");
		field.getSources().add(pathSource(manufacturer(), manufacturerName()));
		EObject machine = product("p-1", "Espresso", 499.0);
		machine.eSet(manufacturer(), catalog.create("Manufacturer", "id", "m-1", "name", "ACME"));

		Document document = write(mapping(field), machine);

		assertThat(document.get("maker")).isEqualTo("ACME");
	}

	@Test
	void aPathThroughNothingContributesNothing() {
		KeywordFieldMapping field = keyword("maker");
		field.getSources().add(pathSource(manufacturer(), manufacturerName()));

		// The reference is unset: no value, and no null value either.
		Document document = write(mapping(field), product("p-1", "Espresso", 499.0));

		assertThat(document.get("maker")).isNull();
	}

	@Test
	void aPathOverAManyReferenceFansOut() {
		KeywordFieldMapping field = keyword("reviewers");
		field.getSources().add(pathSource(reviews(), reviewAuthor()));
		EObject machine = product("p-1", "Espresso", 499.0);
		listOf(machine, reviews()).add(catalog.create("Review", "id", "r-1", "author", "ada"));
		listOf(machine, reviews()).add(catalog.create("Review", "id", "r-2", "author", "linus"));

		Document document = write(mapping(field), machine);

		assertThat(document.getValues("reviewers")).containsExactly("ada", "linus");
	}



	@Test
	void aVirtualFieldExistsInTheIndexAndInNoEClass() {
		KeywordFieldMapping field = keyword("maker");
		field.getSources().add(pathSource(manufacturer(), manufacturerName()));
		EObject machine = product("p-1", "Espresso", 499.0);
		machine.eSet(manufacturer(), catalog.create("Manufacturer", "id", "m-1", "name", "ACME"));

		Document document = write(mapping(field), machine);

		assertThat(document.get("maker")).isEqualTo("ACME");
		assertThat(product.getEStructuralFeature("maker"))
				.as("nothing in the model carries it — which is why no canonical query can name it")
				.isNull();
	}

	@Test
	void aComputedNumericTakesItsEncodingFromTheValue() {
		NumericFieldMapping field = ESEARCH.createNumericFieldMapping();
		field.setName("reviewRating");
		field.setDocValues(true);
		field.getSources().add(pathSource(reviews(), reviewRating()));
		EObject machine = product("p-1", "Espresso", 499.0);
		listOf(machine, reviews()).add(catalog.create("Review", "id", "r-1", "author", "ada",
				"rating", 5));

		Document document = write(mapping(field), machine);

		assertThat(document.getField("reviewRating").numericValue().intValue()).isEqualTo(5);
	}

	// --- several sources ----------------------------------------------------------------------

	@Test
	void severalSourcesFeedOneFieldAsSeveralValues() {
		KeywordFieldMapping field = keyword("terms");
		field.getSources().add(featureSource(name()));
		field.getSources().add(featureSource(description()));

		Document document = write(mapping(field), product("p-1", "Espresso", 499.0));

		assertThat(document.getValues("terms")).containsExactly("Espresso", "coffee at home");
	}

	@Test
	void aSeparatorJoinsThemIntoOne() {
		TextFieldMapping field = text("searchable");
		field.getSources().add(featureSource(name()));
		field.getSources().add(featureSource(description()));
		field.setSeparator(" — ");

		Document document = write(mapping(field), product("p-1", "Espresso", 499.0));

		assertThat(document.getValues("searchable")).containsExactly("Espresso — coffee at home");
	}



	// --- what the mapping load refuses -----------------------------------------------------------




	@Test
	void anOclSourceIsRefusedAndNamesBothWaysOut() {
		// The third rung is declared in the metamodel and not served: evaluating expressions
		// would put the m2x OCL engine into the load path of every deployment, including the
		// ones whose mapping computes nothing (Mark's call, 2026-08-23).
		KeywordFieldMapping field = keyword("shelf");
		field.getSources().add(ocl("if self.price > 100.0 then 'premium' else 'standard' endif"));

		assertThatThrownBy(() -> IndexSchema.of(mapping(field)))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("PathSource")
				.hasMessageContaining("derived EStructuralFeature");
	}

	@Test
	void aPathThatDoesNotLineUpWithTheModelIsRefused() {
		KeywordFieldMapping field = keyword("maker");
		// name is an attribute, so nothing can be navigated through it
		field.getSources().add(pathSource(name(), manufacturerName()));

		assertThatThrownBy(() -> IndexSchema.of(mapping(field)))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("continues past the attribute");
	}

	@Test
	void aPathEndingOnAReferenceIsRefused() {
		KeywordFieldMapping field = keyword("maker");
		field.getSources().add(pathSource(manufacturer()));

		assertThatThrownBy(() -> IndexSchema.of(mapping(field)))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("ends on the reference");
	}

	@Test
	void aFieldSayingItTwiceIsRefused() {
		KeywordFieldMapping field = keyword("label");
		field.setFeature(name());
		field.getSources().add(featureSource(description()));

		assertThatThrownBy(() -> IndexSchema.of(mapping(field)))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("both a feature and sources");
	}

	@Test
	void aComputedFieldWithoutANameIsRefused() {
		KeywordFieldMapping field = ESEARCH.createKeywordFieldMapping();
		field.getSources().add(featureSource(name()));

		assertThatThrownBy(() -> IndexSchema.of(mapping(field)))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("has no name");
	}

	@Test
	void aComputedSubFieldIsRefusedAsTheWrongPlace() {
		TextFieldMapping field = ESEARCH.createTextFieldMapping();
		field.setFeature(name());
		KeywordFieldMapping sub = ESEARCH.createKeywordFieldMapping();
		sub.setName("computed");
		sub.getSources().add(featureSource(description()));
		field.getSubFields().add(sub);

		assertThatThrownBy(() -> IndexSchema.of(mapping(field)))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("another projection of its parent's attribute");
	}

	@Test
	void aComputedValueThatIsNotANumberIsRefusedByTheNumericField() {
		NumericFieldMapping field = ESEARCH.createNumericFieldMapping();
		field.setName("weight");
		field.getSources().add(featureSource(name()));
		DocumentMapper mapper = DocumentMapper.of(IndexSchema.of(mapping(field)));

		assertThatThrownBy(() -> mapper.map(product("p-1", "Espresso", 499.0)))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("A numeric field holds numbers");
	}

	// --- what a computed field depends on ------------------------------------------------------

	@Test
	void aPathSourceReportsWhatItReadsBeyondTheObject() {
		KeywordFieldMapping field = keyword("maker");
		field.getSources().add(pathSource(manufacturer(), manufacturerName()));

		assertThat(IndexSchema.of(mapping(field)).dependencies(product))
				.containsExactly("manufacturer.name");
	}


	@Test
	void aFieldReadingOnlyItsOwnObjectDependsOnNothing() {
		KeywordFieldMapping field = keyword("label");
		field.getSources().add(featureSource(name()));

		assertThat(IndexSchema.of(mapping(field)).dependencies(product)).isEmpty();
	}

	// --- the fingerprint that says "rebuild" ------------------------------------------------------

	@Test
	void theFingerprintFollowsWhatEndsUpInTheIndex() {
		KeywordFieldMapping first = keyword("maker");
		first.getSources().add(pathSource(manufacturer(), manufacturerName()));
		KeywordFieldMapping same = keyword("maker");
		same.getSources().add(pathSource(manufacturer(), manufacturerName()));
		KeywordFieldMapping other = keyword("maker");
		other.getSources().add(featureSource(name()));

		String fingerprint = IndexSchema.of(mapping(first)).fingerprint();
		assertThat(IndexSchema.of(mapping(same)).fingerprint())
				.as("the same mapping written twice is the same index")
				.isEqualTo(fingerprint);
		assertThat(IndexSchema.of(mapping(other)).fingerprint())
				.as("a field fed from somewhere else is a different index — which is what a "
						+ "rebuild is for")
				.isNotEqualTo(fingerprint);
	}

	// --- fixture --------------------------------------------------------------------------------

	private Document write(IndexUnitMapping mapping, EObject object) {
		return DocumentMapper.of(IndexSchema.of(mapping)).map(object).documents().get(0);
	}

	private IndexUnitMapping mapping(FieldMapping field) {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog.ePackage());
		DocumentMapping document = ESEARCH.createDocumentMapping();
		document.setEClass(product);
		document.getFields().add(field);
		mapping.getDocuments().add(document);
		return mapping;
	}

	private KeywordFieldMapping keyword(String name) {
		KeywordFieldMapping field = ESEARCH.createKeywordFieldMapping();
		field.setName(name);
		return field;
	}

	private TextFieldMapping text(String name) {
		TextFieldMapping field = ESEARCH.createTextFieldMapping();
		field.setName(name);
		return field;
	}

	private FeatureSource featureSource(EAttribute attribute) {
		FeatureSource source = ESEARCH.createFeatureSource();
		source.setFeature(attribute);
		return source;
	}

	private PathSource pathSource(EStructuralFeature... segments) {
		PathSource source = ESEARCH.createPathSource();
		source.getSegments().addAll(List.of(segments));
		return source;
	}

	private OclSource ocl(String expression) {
		OclSource source = ESEARCH.createOclSource();
		source.setExpression(expression);
		source.setContext(OclContextKind.SELF);
		return source;
	}

	private EObject product(String id, String name, double price) {
		return catalog.create("Product", "id", id, "name", name, "price", price,
				"description", "coffee at home");
	}

	@SuppressWarnings("unchecked")
	private List<EObject> listOf(EObject object, EStructuralFeature feature) {
		return (List<EObject>) object.eGet(feature);
	}

	private EAttribute name() {
		return (EAttribute) catalog.feature("Product", "name");
	}

	private EAttribute description() {
		return (EAttribute) catalog.feature("Product", "description");
	}

	private EReference manufacturer() {
		return (EReference) catalog.feature("Product", "manufacturer");
	}

	private EReference reviews() {
		return (EReference) catalog.feature("Product", "reviews");
	}

	private EAttribute manufacturerName() {
		return (EAttribute) catalog.feature("Manufacturer", "name");
	}

	private EAttribute reviewRating() {
		return (EAttribute) catalog.feature("Review", "rating");
	}

	private EAttribute reviewAuthor() {
		return (EAttribute) catalog.feature("Review", "author");
	}
}
