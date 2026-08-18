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
package org.eclipse.fennec.search.suggest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.SuggestSource;
import org.eclipse.fennec.search.esearch.SuggesterKind;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappingException;
import org.eclipse.fennec.search.suggest.SuggestSearch.Suggestion;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Suggest over the unit's own documents (S8, #12): sources declared in the mapping,
 * suggesters built from the same searcher every query uses, snapshots swapped atomically
 * on rebuild — and refusals by name for what the mapping did not declare or this version
 * does not serve.
 */
class SuggestSearchTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;

	private static EPackage shop;
	private static EClass product;
	private static EAttribute id;
	private static EAttribute title;
	private static EAttribute popularity;
	private static EAttribute blurb;

	private IndexUnit unit;

	@BeforeAll
	static void buildModel() {
		shop = EcoreFactory.eINSTANCE.createEPackage();
		shop.setName("shop");
		shop.setNsPrefix("shop");
		shop.setNsURI("https://eclipse.org/fennec/search/test/shop");
		product = EcoreFactory.eINSTANCE.createEClass();
		product.setName("Product");
		id = attribute("id", EcorePackage.eINSTANCE.getEString());
		id.setID(true);
		title = attribute("title", EcorePackage.eINSTANCE.getEString());
		popularity = attribute("popularity", EcorePackage.eINSTANCE.getELong());
		blurb = attribute("blurb", EcorePackage.eINSTANCE.getEString());
		product.getEStructuralFeatures().add(id);
		product.getEStructuralFeatures().add(title);
		product.getEStructuralFeatures().add(popularity);
		product.getEStructuralFeatures().add(blurb);
		shop.getEClassifiers().add(product);
	}

	@BeforeEach
	void openUnit() throws IOException {
		unit = IndexUnit.open(IndexUnitConfig.inMemory("shop")
				.refresh(RefreshTrigger.manual())
				.build());
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	// --- lookups --------------------------------------------------------------------------

	@Test
	void completesFromTheUnitsOwnDocumentsWeightDescending() throws Exception {
		IndexSchema schema = schema(source("titles", title, popularity, SuggesterKind.ANALYZING));
		index(schema,
				product("p-1", "Espresso Machine", 10),
				product("p-2", "Espresso Grinder", 99),
				product("p-3", "Kettle", 5));
		SuggestSearch suggest = SuggestSearch.of(unit, schema);

		List<Suggestion> suggestions = suggest.suggest("titles", "espresso", 5);

		assertThat(suggestions).extracting(Suggestion::text)
				.containsExactly("Espresso Grinder", "Espresso Machine");
		assertThat(suggestions.get(0).weight()).isEqualTo(99);
	}

	@Test
	void fuzzyForgivesATypo() throws Exception {
		IndexSchema schema = schema(source("titles", title, null, SuggesterKind.FUZZY));
		index(schema, product("p-1", "Espresso Machine", 0));
		SuggestSearch suggest = SuggestSearch.of(unit, schema);

		assertThat(suggest.suggest("titles", "expresso", 5))
				.extracting(Suggestion::text)
				.containsExactly("Espresso Machine");
	}

	@Test
	void freeTextPredictsTheNextWord() throws Exception {
		IndexSchema schema = schema(source("blurbs", blurb, null, SuggesterKind.FREE_TEXT));
		index(schema,
				product("p-1", "x", 0, "makes great espresso every morning"),
				product("p-2", "y", 0, "great espresso needs great beans"));
		SuggestSearch suggest = SuggestSearch.of(unit, schema);

		assertThat(suggest.suggest("blurbs", "great e", 3))
				.extracting(Suggestion::text)
				.anySatisfy(text -> assertThat(text).contains("espresso"));
	}

	@Test
	void rebuildSeesNewDocuments() throws Exception {
		IndexSchema schema = schema(source("titles", title, null, SuggesterKind.ANALYZING));
		index(schema, product("p-1", "Espresso Machine", 0));
		SuggestSearch suggest = SuggestSearch.of(unit, schema);
		assertThat(suggest.suggest("titles", "kett", 5)).isEmpty();

		index(schema, product("p-2", "Kettle", 0));
		assertThat(suggest.suggest("titles", "kett", 5))
				.as("a lookup answers from the snapshot until rebuilt")
				.isEmpty();
		suggest.rebuild();

		assertThat(suggest.suggest("titles", "kett", 5))
				.extracting(Suggestion::text)
				.containsExactly("Kettle");
	}

	// --- refusals -------------------------------------------------------------------------

	@Test
	void anUnknownSourceIsRefusedNamingTheDeclared() throws Exception {
		IndexSchema schema = schema(source("titles", title, null, SuggesterKind.ANALYZING));
		SuggestSearch suggest = SuggestSearch.of(unit, schema);

		assertThatThrownBy(() -> suggest.suggest("names", "x", 5))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("names")
				.hasMessageContaining("titles");
	}

	@Test
	void aMappingWithoutSourcesRefusesTheApiUpFront() {
		assertThatThrownBy(() -> SuggestSearch.of(unit, schema()))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("no suggest source");
	}

	@Test
	void completionAndContextsAreRefusedByName() {
		assertThatThrownBy(() -> SuggestSearch.of(unit,
				schema(source("titles", title, null, SuggesterKind.COMPLETION))))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("COMPLETION");

		SuggestSource withContexts = source("titles", title, null, SuggesterKind.ANALYZING);
		withContexts.getContexts().add(blurb);
		assertThatThrownBy(() -> SuggestSearch.of(unit, schema(withContexts)))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("contexts");
	}

	@Test
	void anUnstoredSourceFieldIsRefusedWithTheWayOut() {
		IndexUnitMapping mapping = mapping();
		DocumentMapping document = document(mapping);
		var field = ESEARCH.createKeywordFieldMapping();
		field.setFeature(title);
		field.setStored(false);
		field.setDocValues(true);
		document.getFields().add(field);
		document.getSuggestions().add(source("titles", title, null, SuggesterKind.ANALYZING));

		assertThatThrownBy(() -> SuggestSearch.of(unit, IndexSchema.of(mapping)))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("stored=false");
	}

	// --- helpers --------------------------------------------------------------------------

	private static EAttribute attribute(String name, org.eclipse.emf.ecore.EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}

	private static SuggestSource source(String name, EAttribute feature, EAttribute weight,
			SuggesterKind kind) {
		SuggestSource source = ESEARCH.createSuggestSource();
		source.setName(name);
		source.setFeature(feature);
		source.setWeight(weight);
		source.setKind(kind);
		return source;
	}

	private static IndexUnitMapping mapping() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("shop");
		mapping.setEPackage(shop);
		return mapping;
	}

	private static DocumentMapping document(IndexUnitMapping mapping) {
		DocumentMapping document = ESEARCH.createDocumentMapping();
		document.setEClass(product);
		mapping.getDocuments().add(document);
		return document;
	}

	private static IndexSchema schema(SuggestSource... sources) {
		IndexUnitMapping mapping = mapping();
		DocumentMapping document = document(mapping);
		for (SuggestSource source : sources) {
			document.getSuggestions().add(source);
		}
		return IndexSchema.of(mapping);
	}

	private void index(IndexSchema schema, EObject... objects) throws IOException {
		DocumentMapper mapper = DocumentMapper.of(schema);
		for (EObject object : objects) {
			var mapped = mapper.map(object);
			unit.updateDocuments(mapped.term(), mapped.documents());
		}
		unit.refresh();
	}

	private static EObject product(String idValue, String titleValue, long popularityValue) {
		return product(idValue, titleValue, popularityValue, null);
	}

	private static EObject product(String idValue, String titleValue, long popularityValue,
			String blurbValue) {
		EObject object = EcoreUtil.create(product);
		object.eSet(id, idValue);
		object.eSet(title, titleValue);
		object.eSet(popularity, popularityValue);
		if (blurbValue != null) {
			object.eSet(blurb, blurbValue);
		}
		return object;
	}
}
