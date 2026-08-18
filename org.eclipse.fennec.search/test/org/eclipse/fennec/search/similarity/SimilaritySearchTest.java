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
package org.eclipse.fennec.search.similarity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.TextFieldMapping;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.TestModels;
import org.eclipse.fennec.search.similarity.SimilaritySearch.SimilarHit;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The similarity API (S13, #15): {@code MoreLikeThis} over the term statistics of the
 * unit's own corpus. Assertions are ordinal — more shared terms sort before fewer on a
 * corpus built to make the order unambiguous — because absolute scores are not a
 * contract. Fields that cannot feed term statistics are refused by name.
 */
class SimilaritySearchTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;
	private static EClass product;

	private IndexUnit unit;
	private IndexSchema schema;
	private DocumentMapper mapper;
	private SimilaritySearch similarity;
	private EObject anchor;

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
		mapper = DocumentMapper.of(schema);
		similarity = SimilaritySearch.of(unit, schema);
		anchor = product("anchor", "espresso grinder with steel burrs");
		index(anchor);
		index(product("close", "steel espresso grinder burrs"));
		index(product("mid", "espresso cups"));
		index(product("far", "ceramic tea pot"));
		unit.refresh();
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	// --- neighbours -------------------------------------------------------------------------

	@Test
	void neighboursRankByTermOverlap() throws Exception {
		List<SimilarHit> hits = similarity.search(SimilarityRequest.to(anchor).field(description()));

		assertThat(hits).extracting(hit -> id(hit.object())).startsWith("close");
		assertThat(hits).extracting(hit -> id(hit.object()))
				.as("no shared term, no neighbour")
				.doesNotContain("far");
		assertThat(hits.get(0).score()).isGreaterThan(0);
	}

	@Test
	void theAnchorItselfIsNeverAHit() throws Exception {
		List<SimilarHit> hits = similarity.search(SimilarityRequest.to(anchor).field(description()));

		assertThat(hits).extracting(hit -> id(hit.object())).doesNotContain("anchor");
	}

	@Test
	void maxHitsCapsTheNeighbourhood() throws Exception {
		List<SimilarHit> hits = similarity.search(SimilarityRequest.to(anchor)
				.field(description())
				.maxHits(1));

		assertThat(hits).extracting(hit -> id(hit.object())).containsExactly("close");
	}

	@Test
	void onlyTheAnchorsTypeIsSearched() throws Exception {
		// A Manufacturer sharing the anchor's name terms is never a Product's neighbour.
		EObject maker = catalog.create("Manufacturer", "id", "m-1", "name", "espresso grinder works");
		index(maker);
		EObject named = product("named", "unrelated");
		named.eSet(catalog.feature("Product", "name"), "espresso grinder deluxe");
		index(named);
		anchor.eSet(catalog.feature("Product", "name"), "espresso grinder");
		index(anchor);
		unit.refresh();

		List<SimilarHit> hits = similarity.search(SimilarityRequest.to(anchor)
				.field((EAttribute) catalog.feature("Product", "name")));

		assertThat(hits).extracting(hit -> hit.object().eClass().getName())
				.containsOnly("Product");
		assertThat(hits).extracting(hit -> id(hit.object())).contains("named");
	}

	@Test
	void frequencyThresholdsPruneRareTerms() throws Exception {
		// No anchor term occurs in more than three documents; minDocFreq=4 silences them all.
		List<SimilarHit> hits = similarity.search(SimilarityRequest.to(anchor)
				.field(description())
				.minDocFreq(4));

		assertThat(hits).as("below the thresholds there are no neighbours, not an error").isEmpty();
	}

	@Test
	void termVectorsAloneFeedTheStatistics() throws Exception {
		// stored=false would normally refuse — declared term vectors are the second source.
		IndexUnitMapping mapping = conventionMapping();
		TextFieldMapping vectored = ESEARCH.createTextFieldMapping();
		vectored.setFeature(description());
		vectored.setStored(false);
		vectored.setTermVectors(true);
		mapping.getDocuments().get(0).getFields().add(vectored);
		IndexSchema vectoredSchema = IndexSchema.of(mapping);
		try (IndexUnit own = IndexUnit.open(IndexUnitConfig.inMemory("catalog")
				.refresh(RefreshTrigger.manual()).build())) {
			DocumentMapper vectoredMapper = DocumentMapper.of(vectoredSchema);
			EObject a = product("anchor", "espresso grinder with steel burrs");
			var mapped = vectoredMapper.map(a);
			own.updateDocuments(mapped.term(), mapped.documents());
			mapped = vectoredMapper.map(product("close", "steel espresso grinder burrs"));
			own.updateDocuments(mapped.term(), mapped.documents());
			own.refresh();

			List<SimilarHit> hits = SimilaritySearch.of(own, vectoredSchema)
					.search(SimilarityRequest.to(a).field(description()));

			assertThat(hits).extracting(hit -> id(hit.object())).containsExactly("close");
		}
	}

	// --- refusals ---------------------------------------------------------------------------

	@Test
	void anUnindexedAnchorIsRefused() {
		EObject stranger = product("stranger", "never indexed");

		assertThatThrownBy(() -> similarity.search(SimilarityRequest.to(stranger).field(description())))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("not")
				.hasMessageContaining("index");
	}

	@Test
	void aKeywordFieldIsRefusedByName() {
		IndexUnitMapping mapping = conventionMapping();
		KeywordFieldMapping keyword = ESEARCH.createKeywordFieldMapping();
		keyword.setFeature((EAttribute) catalog.feature("Product", "name"));
		mapping.getDocuments().get(0).getFields().add(keyword);
		SimilaritySearch keyed = SimilaritySearch.of(unit, IndexSchema.of(mapping));

		assertThatThrownBy(() -> keyed.search(SimilarityRequest.to(anchor)
				.field((EAttribute) catalog.feature("Product", "name"))))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("not analyzed text");
	}

	@Test
	void unstoredTextWithoutVectorsIsRefusedWithTheWayOut() {
		IndexUnitMapping mapping = conventionMapping();
		TextFieldMapping unstored = ESEARCH.createTextFieldMapping();
		unstored.setFeature(description());
		unstored.setStored(false);
		mapping.getDocuments().get(0).getFields().add(unstored);
		SimilaritySearch bare = SimilaritySearch.of(unit, IndexSchema.of(mapping));

		assertThatThrownBy(() -> bare.search(SimilarityRequest.to(anchor).field(description())))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("termVectors");
	}

	@Test
	void aRequestWithoutFieldsIsRefused() {
		assertThatThrownBy(() -> similarity.search(SimilarityRequest.to(anchor)))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("no field");
	}

	// --- helpers ----------------------------------------------------------------------------

	private void index(EObject object) throws IOException {
		var mapped = mapper.map(object);
		unit.updateDocuments(mapped.term(), mapped.documents());
	}

	private static EObject product(String id, String description) {
		return catalog.create("Product", "id", id, "name", id, "description", description);
	}

	private static String id(EObject object) {
		return (String) object.eGet(object.eClass().getEStructuralFeature("id"));
	}

	private static EAttribute description() {
		return (EAttribute) catalog.feature("Product", "description");
	}

	/** Convention only: Product and Manufacturer as document types. */
	private static IndexUnitMapping conventionMapping() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog.ePackage());
		DocumentMapping productMapping = ESEARCH.createDocumentMapping();
		productMapping.setEClass(product);
		mapping.getDocuments().add(productMapping);
		DocumentMapping makerMapping = ESEARCH.createDocumentMapping();
		makerMapping.setEClass(catalog.eClass("Manufacturer"));
		mapping.getDocuments().add(makerMapping);
		return mapping;
	}
}
