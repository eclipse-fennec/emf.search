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
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.FieldMapping;
import org.eclipse.fennec.search.esearch.GeoPointFieldMapping;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.esearch.SuggestSource;
import org.eclipse.fennec.search.mapping.MappingGenerator.Suggestions;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The suggested-mapping generator (#51): what it proposes beyond convention, what it
 * deliberately leaves alone, and — the promise that matters — that its output is a
 * mapping which actually indexes and answers.
 */
class MappingGeneratorTest {

	private static TestModels catalog;

	@BeforeAll
	static void loadModel() {
		catalog = TestModels.load("catalog.ecore");
	}

	// --- what it proposes ---------------------------------------------------------------------

	@Test
	void anEnumBecomesAFacetDimension() {
		DocumentMapping product = documentFor("Product");

		KeywordFieldMapping condition = (KeywordFieldMapping) field(product, "condition");
		assertThat(condition.getFacet()).as("an enum is a small closed set — a filter sidebar")
				.isNotNull();
		assertThat(condition.getFacet().isMultiValued()).isFalse();
		assertThat(condition.isDocValues()).isTrue();
	}

	@Test
	void aManyValuedStringBecomesAMultiValuedFacetedKeyword() {
		DocumentMapping product = documentFor("Product");

		KeywordFieldMapping tags = (KeywordFieldMapping) field(product, "tags");
		assertThat(tags.getFacet()).isNotNull();
		assertThat(tags.getFacet().isMultiValued()).as("the tag shape").isTrue();
	}

	@Test
	void theLabelBecomesASortableKeywordAndASuggestionSource() {
		DocumentMapping product = documentFor("Product");

		KeywordFieldMapping name = (KeywordFieldMapping) field(product, "name");
		assertThat(name.isDocValues()).as("a result list sorts by the label").isTrue();
		assertThat(product.getSuggestions()).extracting(SuggestSource::getName).containsExactly("names");
		assertThat(product.getSuggestions()).extracting(source -> source.getFeature().getName())
				.containsExactly("name");
	}

	@Test
	void containmentIsABlockAndACrossReferenceIsAnId() {
		DocumentMapping product = documentFor("Product");

		assertThat(reference(product, "reviews").getStrategy())
				.as("a block is what makes a quantifier over the children answerable")
				.isEqualTo(ReferenceStrategy.NESTED);
		assertThat(reference(product, "manufacturer").getStrategy())
				.as("an index has no join: a cross-document reference is an id")
				.isEqualTo(ReferenceStrategy.ID_ONLY);
	}

	@Test
	void aPackedPointClassBecomesAGeographicPosition() {
		// A class holding exactly one two-valued floating-point attribute is a position —
		// recognised structurally, never by the name "GeoPoint".
		var model = geoModel();
		Suggestions suggestions = MappingGenerator.forPackage(model).generate();

		DocumentMapping site = documentFor(suggestions.mapping(), "Site");
		GeoPointFieldMapping position = (GeoPointFieldMapping) site.getFields().stream()
				.filter(GeoPointFieldMapping.class::isInstance).findFirst().orElseThrow();
		assertThat(position.getPointReference().getName()).isEqualTo("position");
		assertThat(position.getCoordinates().getName()).isEqualTo("values");
		assertThat(suggestions.mapping().getDocuments())
				.as("the point class itself is a value inside the field, not a document of its own")
				.extracting(document -> document.getEClass().getName())
				.doesNotContain("Position");
	}

	// --- what it leaves alone -----------------------------------------------------------------

	@Test
	void conventionIsNotRestated() {
		DocumentMapping product = documentFor("Product");
		List<String> declared = product.getFields().stream()
				.map(field -> field.getFeature().getName())
				.toList();

		assertThat(declared)
				.as("numerics, temporals and booleans are already indexed correctly by convention")
				.doesNotContain("price", "stock", "available", "released")
				.as("a plain string is analyzed text, which is what full-text search needs")
				.doesNotContain("description")
				.as("the id is already a stored keyword")
				.doesNotContain("id");
	}

	@Test
	void anAbstractClassGetsNoDocument() {
		var model = EcoreFactory.eINSTANCE.createEPackage();
		model.setName("shapes");
		model.setNsURI("https://eclipse.org/fennec/search/test/shapes");
		EClass abstractBase = EcoreFactory.eINSTANCE.createEClass();
		abstractBase.setName("Base");
		abstractBase.setAbstract(true);
		var name = EcoreFactory.eINSTANCE.createEAttribute();
		name.setName("name");
		name.setEType(EcorePackage.eINSTANCE.getEString());
		abstractBase.getEStructuralFeatures().add(name);
		model.getEClassifiers().add(abstractBase);

		Suggestions suggestions = MappingGenerator.forPackage(model).generate();

		assertThat(suggestions.mapping().getDocuments())
				.as("an abstract class has no documents; a URI naming it reads its subtypes")
				.isEmpty();
	}

	@Test
	void everySuggestionCarriesItsReason() {
		Suggestions suggestions = MappingGenerator.forPackage(catalog.ePackage()).generate();

		assertThat(suggestions.explanations())
				.as("a generated file has to teach the mapping model, not just fill it")
				.isNotEmpty();
		assertThat(suggestions.explanations())
				.allSatisfy(explanation -> assertThat(explanation).contains(":").hasSizeGreaterThan(40));
		assertThat(suggestions.explanations())
				.anySatisfy(explanation -> assertThat(explanation).contains("Product.condition"));
	}

	@Test
	void classesFromTwoPackagesAreRefused() {
		var other = EcoreFactory.eINSTANCE.createEPackage();
		other.setName("other");
		other.setNsURI("https://eclipse.org/fennec/search/test/other");
		EClass stray = EcoreFactory.eINSTANCE.createEClass();
		stray.setName("Stray");
		other.getEClassifiers().add(stray);

		assertThatThrownBy(() -> MappingGenerator
				.forClasses(List.of(catalog.eClass("Product"), stray), "mixed"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("one EPackage universe");
	}

	// --- the promise that matters -------------------------------------------------------------

	@Test
	void theGeneratedMappingIndexesAndAnswers() throws IOException {
		IndexUnitMapping mapping = MappingGenerator.forPackage(catalog.ePackage()).generate().mapping();

		// It resolves — every declaration the generator made is one this backend serves.
		IndexSchema schema = IndexSchema.of(mapping);

		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("catalog")
				.refresh(RefreshTrigger.manual())
				.build())) {
			EObject product = catalog.create("Product", "id", "p-1", "name", "Espresso Machine",
					"description", "A compact espresso machine", "price", 499.0);
			MappedDocument mapped = DocumentMapper.of(schema).map(product);
			unit.updateDocuments(mapped.term(), mapped.documents());
			unit.refresh();

			// And it answers: the label is readable as an exact keyword, which is exactly
			// what the generator declared it for.
			IndexSchema.Field name = schema.resolve(catalog.eClass("Product"),
					(org.eclipse.emf.ecore.EAttribute) catalog.feature("Product", "name"));
			assertThat(name.kind()).isEqualTo(IndexSchema.FieldKind.KEYWORD);
			assertThat(unit.<Integer>search(searcher -> searcher.count(
					new org.apache.lucene.search.TermQuery(
							new org.apache.lucene.index.Term(name.name(), "Espresso Machine")))))
					.isEqualTo(1);
		}
	}

	// --- helpers ------------------------------------------------------------------------------

	private static DocumentMapping documentFor(String className) {
		return documentFor(MappingGenerator.forPackage(catalog.ePackage()).generate().mapping(), className);
	}

	private static DocumentMapping documentFor(IndexUnitMapping mapping, String className) {
		return mapping.getDocuments().stream()
				.filter(document -> className.equals(document.getEClass().getName()))
				.findFirst().orElseThrow(() -> new AssertionError("No document mapping for " + className));
	}

	private static FieldMapping field(DocumentMapping document, String featureName) {
		return document.getFields().stream()
				.filter(field -> field.getFeature() != null
						&& featureName.equals(field.getFeature().getName()))
				.findFirst().orElseThrow(() -> new AssertionError("No field for " + featureName));
	}

	private static ReferenceMapping reference(DocumentMapping document, String referenceName) {
		Optional<ReferenceMapping> found = document.getReferences().stream()
				.filter(mapping -> referenceName.equals(mapping.getEReference().getName()))
				.findFirst();
		return found.orElseThrow(() -> new AssertionError("No reference mapping for " + referenceName));
	}

	/** A model whose Position class is a packed point — named so the recognition cannot cheat. */
	private static org.eclipse.emf.ecore.EPackage geoModel() {
		var model = EcoreFactory.eINSTANCE.createEPackage();
		model.setName("sites");
		model.setNsURI("https://eclipse.org/fennec/search/test/sites");

		EClass position = EcoreFactory.eINSTANCE.createEClass();
		position.setName("Position");
		var values = EcoreFactory.eINSTANCE.createEAttribute();
		values.setName("values");
		values.setEType(EcorePackage.eINSTANCE.getEDouble());
		values.setLowerBound(2);
		values.setUpperBound(2);
		position.getEStructuralFeatures().add(values);

		EClass site = EcoreFactory.eINSTANCE.createEClass();
		site.setName("Site");
		var id = EcoreFactory.eINSTANCE.createEAttribute();
		id.setName("id");
		id.setID(true);
		id.setEType(EcorePackage.eINSTANCE.getEString());
		var reference = EcoreFactory.eINSTANCE.createEReference();
		reference.setName("position");
		reference.setEType(position);
		reference.setContainment(true);
		site.getEStructuralFeatures().add(id);
		site.getEStructuralFeatures().add(reference);

		model.getEClassifiers().add(position);
		model.getEClassifiers().add(site);
		return model;
	}
}
