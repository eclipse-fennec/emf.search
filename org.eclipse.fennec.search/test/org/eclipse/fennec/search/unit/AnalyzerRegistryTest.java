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
package org.eclipse.fennec.search.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.jupiter.api.Test;

class AnalyzerRegistryTest {

	@Test
	void theStandardRegistryHasAStandardAnalyzerAndNoNames() {
		AnalyzerRegistry registry = AnalyzerRegistry.standard();

		assertThat(registry.defaultAnalyzer()).isInstanceOf(StandardAnalyzer.class);
		assertThat(registry.names()).isEmpty();
		assertThat(registry.find("anything")).isEmpty();
	}

	@Test
	void namedAnalyzersAreResolved() {
		Analyzer keyword = new KeywordAnalyzer();
		AnalyzerRegistry registry = AnalyzerRegistry.builder().register("keyword", keyword).build();

		assertThat(registry.find("keyword")).containsSame(keyword);
		assertThat(registry.require("keyword")).isSameAs(keyword);
		assertThat(registry.names()).containsExactly("keyword");
	}

	@Test
	void anUnknownNameFailsWithTheNamesThatDoExist() {
		AnalyzerRegistry registry = AnalyzerRegistry.builder()
				.register("keyword", new KeywordAnalyzer())
				.register("whitespace", new WhitespaceAnalyzer())
				.build();

		assertThatThrownBy(() -> registry.require("keywrod"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("keywrod")
				.hasMessageContaining("keyword")
				.hasMessageContaining("whitespace");
	}

	@Test
	void aDuplicateNameIsRejectedRatherThanSilentlyOverwritten() {
		AnalyzerRegistry.Builder builder = AnalyzerRegistry.builder().register("a", new KeywordAnalyzer());

		assertThatThrownBy(() -> builder.register("a", new WhitespaceAnalyzer()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Duplicate analyzer name 'a'");
	}

	@Test
	void theDefaultAnalyzerCanBeReplaced() {
		Analyzer whitespace = new WhitespaceAnalyzer();

		AnalyzerRegistry registry = AnalyzerRegistry.builder().defaultAnalyzer(whitespace).build();

		assertThat(registry.defaultAnalyzer()).isSameAs(whitespace);
	}

	@Test
	void theUnitUsesTheRegistryDefaultForAnalysis() throws Exception {
		// WhitespaceAnalyzer does not lowercase; StandardAnalyzer does. Indexing "Hello"
		// and searching the lowercase term therefore distinguishes the two.
		AnalyzerRegistry whitespace = AnalyzerRegistry.builder()
				.defaultAnalyzer(new WhitespaceAnalyzer()).build();

		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("analysis")
				.analyzers(whitespace)
				.refresh(RefreshTrigger.manual())
				.build())) {

			org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
			doc.add(new org.apache.lucene.document.TextField("text", "Hello",
					org.apache.lucene.document.Field.Store.NO));
			unit.addDocument(doc);
			unit.refresh();

			int lower = unit.<Integer>search(s -> s.count(new org.apache.lucene.search.TermQuery(
					new org.apache.lucene.index.Term("text", "hello"))));
			int exact = unit.<Integer>search(s -> s.count(new org.apache.lucene.search.TermQuery(
					new org.apache.lucene.index.Term("text", "Hello"))));

			assertThat(lower).as("whitespace analyzer does not lowercase").isZero();
			assertThat(exact).isEqualTo(1);
		}
	}
}
