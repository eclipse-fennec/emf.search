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
package org.eclipse.fennec.search.perf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.MappedDocument;
import org.eclipse.fennec.search.mapping.SearchFields;
import org.eclipse.fennec.search.unit.CommitPolicy;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * How indexing behaves as the corpus grows (issue #38).
 * <p>
 * Against a real directory, not {@code ByteBuffersDirectory}: the I/O and merge behaviour
 * is most of what makes throughput interesting, and an in-memory directory hides it.
 * <p>
 * <b>Only structure gates.</b> Document counts and block sizes are deterministic and are
 * asserted; every duration is logged and nothing more, because a busy CI machine must not
 * be able to turn a timing into a build failure.
 */
@Tag("perf")
class IndexingThroughputPerfTest {

	private static final int BASE = 5_000 * PerfCorpus.SCALE;
	private static final int FACTOR = 4;
	private static final int REVIEWS_PER_PRODUCT = 3;

	private static PerfCorpus corpus;

	@BeforeAll
	static void loadModel() {
		corpus = PerfCorpus.load();
	}

	@Test
	void plainDocumentThroughputAtTwoCorpusSizes(@TempDir Path directory) throws IOException {
		Measurement small = indexProducts(directory.resolve("small"), BASE, 0);
		Measurement large = indexProducts(directory.resolve("large"), BASE * FACTOR, 0);

		// Structural: every object became exactly one document, at both sizes.
		assertThat(small.documents).isEqualTo(BASE);
		assertThat(large.documents).isEqualTo(BASE * FACTOR);

		report("plain", small, large);
	}

	@Test
	void blockThroughputCostsWhatTheExtraDocumentsCost(@TempDir Path directory) throws IOException {
		Measurement small = indexProducts(directory.resolve("small"), BASE, REVIEWS_PER_PRODUCT);
		Measurement large = indexProducts(directory.resolve("large"), BASE * FACTOR, REVIEWS_PER_PRODUCT);

		// Structural: a block is the parent plus its children, and nothing else.
		int perObject = REVIEWS_PER_PRODUCT + 1;
		assertThat(small.documents).isEqualTo(BASE * perObject);
		assertThat(large.documents).isEqualTo(BASE * FACTOR * perObject);
		// The root marker is what every query filters on, so it must be on exactly the parents.
		assertThat(small.roots).isEqualTo(BASE);

		report("block", small, large);
	}

	@Test
	void commitPolicyChangesTheAnswerAndIsThereforeMeasuredSeparately(@TempDir Path directory)
			throws IOException {
		Measurement onClose = indexProducts(directory.resolve("on-close"), BASE, 0,
				CommitPolicy.onClose());
		Measurement perBatch = indexProducts(directory.resolve("per-batch"), BASE, 0,
				CommitPolicy.afterDocuments(1_000));

		assertThat(onClose.documents).isEqualTo(BASE);
		assertThat(perBatch.documents).isEqualTo(BASE);

		System.out.printf("[perf] commit on close   %s%n", onClose);
		System.out.printf("[perf] commit per 1000   %s%n", perBatch);
		System.out.printf("[perf] committing per batch costs %+.1f%% per document%n",
				100.0 * (perBatch.nanosPerDocument() - onClose.nanosPerDocument())
						/ onClose.nanosPerDocument());
	}

	// --- measurement -------------------------------------------------------------------------

	private Measurement indexProducts(Path path, int count, int reviews) throws IOException {
		return indexProducts(path, count, reviews, CommitPolicy.onClose());
	}

	private Measurement indexProducts(Path path, int count, int reviews, CommitPolicy commit)
			throws IOException {
		DocumentMapper mapper = DocumentMapper.of(corpus.mapping(reviews > 0, false));
		IndexUnitConfig config = IndexUnitConfig.builder("perf", path)
				.refresh(RefreshTrigger.manual())
				.commit(commit)
				.build();
		long mapNanos = 0;
		long writeNanos = 0;
		try (IndexUnit unit = IndexUnit.open(config)) {
			for (int i = 0; i < count; i++) {
				long beforeMap = System.nanoTime();
				MappedDocument mapped = mapper.map(corpus.product(i, reviews));
				long afterMap = System.nanoTime();
				if (mapped.isBlock()) {
					unit.updateDocuments(mapped.term(), mapped.documents());
				} else {
					unit.addDocument(mapped.root());
				}
				writeNanos += System.nanoTime() - afterMap;
				mapNanos += afterMap - beforeMap;
			}
			unit.commit();
			unit.refresh();
			long documents = unit.search(searcher -> (long) searcher.count(MatchAllDocsQuery.INSTANCE));
			long roots = unit.search(searcher -> (long) searcher
					.count(new TermQuery(new Term(SearchFields.PARENT, SearchFields.PARENT_VALUE))));
			long bytes = indexSize(path);
			return new Measurement(count, documents, roots, mapNanos, writeNanos, bytes);
		}
	}

	private static long indexSize(Path path) throws IOException {
		if (!java.nio.file.Files.isDirectory(path)) {
			return 0;
		}
		try (var files = java.nio.file.Files.list(path)) {
			return files.mapToLong(file -> {
				try {
					return java.nio.file.Files.size(file);
				} catch (IOException e) {
					return 0L;
				}
			}).sum();
		}
	}

	private static void report(String label, Measurement small, Measurement large) {
		System.out.printf("[perf] %-6s %s%n", label, small);
		System.out.printf("[perf] %-6s %s%n", label, large);
		// The number to watch: per-document cost should stay flat as the corpus grows. A
		// clearly superlinear factor points at merge or refresh behaviour, not at the mapper.
		System.out.printf("[perf] %-6s per-document cost at %dx corpus: %.2fx%n", label, FACTOR,
				large.nanosPerDocument() / small.nanosPerDocument());
	}

	private record Measurement(int objects, long documents, long roots, long mapNanos, long writeNanos,
			long bytes) {

		double nanosPerDocument() {
			return (double) (mapNanos + writeNanos) / objects;
		}

		@Override
		public String toString() {
			Duration total = Duration.ofNanos(mapNanos + writeNanos);
			double perSecond = objects / Math.max(1e-9, total.toNanos() / 1e9);
			return String.format(java.util.Locale.ROOT,
					"%,7d objects -> %,8d docs in %,5d ms (%,9.0f obj/s, map %2.0f%%, %,6d KiB, "
							+ "%,4d B/doc)",
					objects, documents, total.toMillis(), perSecond,
					100.0 * mapNanos / (mapNanos + writeNanos), bytes / 1024,
					documents == 0 ? 0 : bytes / documents);
		}
	}
}
