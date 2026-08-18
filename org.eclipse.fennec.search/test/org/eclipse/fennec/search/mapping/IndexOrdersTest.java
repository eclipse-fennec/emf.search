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
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedSetSortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHits;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexSort;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.SortEntry;
import org.eclipse.fennec.search.unit.IndexLocation;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The declared index order (S17, #19): derivation from the mapping with its refusals, and
 * the payoff at the other end — a query sorting the way the index is sorted terminates
 * early, and pages over the sorted index stay consistent.
 */
class IndexOrdersTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;

	@BeforeAll
	static void loadModel() {
		catalog = TestModels.load("catalog.ecore");
	}

	// --- derivation -------------------------------------------------------------------------

	@Test
	void derivesNumericAndKeywordEntriesWithMissingPinned() {
		IndexUnitMapping mapping = mapping();
		KeywordFieldMapping name = ESEARCH.createKeywordFieldMapping();
		name.setFeature(attribute("name"));
		name.setDocValues(true);
		document(mapping).getFields().add(name);
		addSort(mapping, entry("price", false, true), entry("name", true, false));

		Sort sort = IndexOrders.indexSort(IndexSchema.of(mapping));

		assertThat(sort.getSort()).hasSize(2);
		SortField price = sort.getSort()[0];
		assertThat(price.getType()).isEqualTo(SortField.Type.DOUBLE);
		assertThat(price.getReverse()).isFalse();
		assertThat(price.getMissingValue())
				.as("missing last on an ascending sort is the maximum")
				.isEqualTo(Double.POSITIVE_INFINITY);
		assertThat(sort.getSort()[1]).isInstanceOf(SortedSetSortField.class);
		assertThat(sort.getSort()[1].getMissingValue()).isEqualTo(SortField.STRING_FIRST);
	}

	@Test
	void aMappingWithoutASortDerivesNone() {
		assertThat(IndexOrders.indexSort(IndexSchema.of(mapping()))).isNull();
	}

	@Test
	void refusesWhatAPhysicalOrderCannotDo() {
		IndexUnitMapping tags = mapping();
		addSort(tags, entry("tags", false, false));
		assertThatThrownBy(() -> IndexOrders.indexSort(IndexSchema.of(tags)))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("multi-valued");

		IndexUnitMapping text = mapping();
		addSort(text, entry("description", false, false));
		assertThatThrownBy(() -> IndexOrders.indexSort(IndexSchema.of(text)))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("analyzed");

		IndexUnitMapping unsortable = mapping();
		KeywordFieldMapping name = ESEARCH.createKeywordFieldMapping();
		name.setFeature(attribute("name"));
		document(unsortable).getFields().add(name);
		addSort(unsortable, entry("name", false, false));
		assertThatThrownBy(() -> IndexOrders.indexSort(IndexSchema.of(unsortable)))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("docValues");
	}

	// --- the payoff -------------------------------------------------------------------------

	@Test
	void aMatchingSortTerminatesEarlyOnTheSortedIndex() throws Exception {
		IndexUnitMapping mapping = mapping();
		addSort(mapping, entry("price", false, true));
		IndexSchema schema = IndexSchema.of(mapping);
		try (IndexUnit unit = openSorted(schema)) {
			DocumentMapper mapper = DocumentMapper.of(schema);
			for (int i = 0; i < 2000; i++) {
				index(unit, mapper, product("p-" + i, i));
			}
			unit.refresh();

			Sort matching = new Sort(new SortField("price", SortField.Type.DOUBLE));
			TopDocs top = unit.search(searcher ->
					searcher.search(MatchAllDocsQuery.INSTANCE, 5, matching));

			// The relation is the contract signal: GREATER_THAN_OR_EQUAL_TO says the collector
			// was allowed to stop counting — the exact value it got to is its own business.
			assertThat(top.totalHits.relation())
					.as("a sort matching the index order stops collecting once the top is settled")
					.isEqualTo(TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO);
			assertThat(top.scoreDocs).hasSize(5);
		}
	}

	@Test
	void pagingOverTheSortedIndexIsConsistentAndComplete() throws Exception {
		IndexUnitMapping mapping = mapping();
		addSort(mapping, entry("price", false, true));
		IndexSchema schema = IndexSchema.of(mapping);
		try (IndexUnit unit = openSorted(schema)) {
			DocumentMapper mapper = DocumentMapper.of(schema);
			for (int i = 0; i < 100; i++) {
				index(unit, mapper, product("p-" + i, i));
			}
			unit.refresh();

			Sort matching = new Sort(new SortField("price", SortField.Type.DOUBLE));
			List<String> paged = new ArrayList<>();
			for (int offset = 0; offset < 100; offset += 7) {
				int start = offset;
				int wanted = start + 7;
				TopDocs page = unit.search(searcher ->
						searcher.search(MatchAllDocsQuery.INSTANCE, wanted, matching));
				var stored = unit.<List<String>>search(searcher -> {
					List<String> ids = new ArrayList<>();
					for (int i = start; i < page.scoreDocs.length; i++) {
						ids.add(searcher.storedFields().document(page.scoreDocs[i].doc)
								.get(SearchFields.ID));
					}
					return ids;
				});
				paged.addAll(stored);
			}

			assertThat(paged).hasSize(100).doesNotHaveDuplicates();
			for (int i = 0; i < 100; i++) {
				assertThat(paged.get(i)).isEqualTo("p-" + i);
			}
		}
	}

	// --- helpers --------------------------------------------------------------------------

	private static IndexUnit openSorted(IndexSchema schema) throws IOException {
		return IndexUnit.open(IndexUnitConfig.builder("catalog", IndexLocation.inMemory())
				.refresh(RefreshTrigger.manual())
				.indexSort(IndexOrders.indexSort(schema))
				.build());
	}

	private static void index(IndexUnit unit, DocumentMapper mapper, EObject object) throws IOException {
		var mapped = mapper.map(object);
		unit.updateDocuments(mapped.term(), mapped.documents());
	}

	private static EObject product(String id, double price) {
		return catalog.create("Product", "id", id, "name", id, "price", price);
	}

	private static IndexUnitMapping mapping() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog.ePackage());
		return mapping;
	}

	private static DocumentMapping document(IndexUnitMapping mapping) {
		if (mapping.getDocuments().isEmpty()) {
			var document = ESEARCH.createDocumentMapping();
			document.setEClass(catalog.eClass("Product"));
			mapping.getDocuments().add(document);
		}
		return mapping.getDocuments().get(0);
	}

	private static void addSort(IndexUnitMapping mapping, SortEntry... entries) {
		IndexSort sort = ESEARCH.createIndexSort();
		for (SortEntry entry : entries) {
			sort.getEntries().add(entry);
		}
		mapping.setSort(sort);
	}

	private static SortEntry entry(String feature, boolean descending, boolean missingLast) {
		SortEntry entry = ESEARCH.createSortEntry();
		entry.setFeature(attribute(feature));
		entry.setDescending(descending);
		entry.setMissingLast(missingLast);
		return entry;
	}

	private static EAttribute attribute(String name) {
		return (EAttribute) catalog.feature("Product", name);
	}
}
