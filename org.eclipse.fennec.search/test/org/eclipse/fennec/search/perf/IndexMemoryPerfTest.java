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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.apache.lucene.search.MatchAllDocsQuery;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.unit.AccessMode;
import org.eclipse.fennec.search.unit.CommitPolicy;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * What a large index costs in memory and on disk (issue #38).
 * <p>
 * Heap figures are advisory — a JVM under a test runner will not give a reproducible peak,
 * so they are logged. What <b>is</b> asserted is the part that is deterministic and that
 * the convention defaults depend on: that storing field values costs more than not storing
 * them, and that a bulk load ends up with exactly the documents it was given. If the first
 * of those ever inverts, an assumption behind §4's conventions has broken.
 */
@Tag("perf")
class IndexMemoryPerfTest {

	private static final int DOCUMENTS = 20_000 * PerfCorpus.SCALE;

	private static PerfCorpus corpus;

	@BeforeAll
	static void loadModel() {
		corpus = PerfCorpus.load();
	}

	@Test
	void heapAndDiskCostOfABulkLoad(@TempDir Path directory) throws IOException {
		Path path = directory.resolve("bulk");
		DocumentMapper mapper = DocumentMapper.of(corpus.mapping(false, false));

		long before = usedHeap();
		long peak = before;
		// BULK_LOAD exists for exactly this case: no searcher, no reopen thread, nothing paying
		// for visibility while the corpus is being written.
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("perf", path)
				.access(AccessMode.BULK_LOAD)
				// BULK_LOAD opens no searcher, so MANUAL is the only refresh it can honour.
				.refresh(RefreshTrigger.manual())
				.commit(CommitPolicy.onClose())
				.build())) {
			for (int i = 0; i < DOCUMENTS; i++) {
				unit.addDocument(mapper.map(corpus.product(i, 0)).root());
				if (i % 2_000 == 0) {
					peak = Math.max(peak, usedHeap());
				}
			}
			unit.commit();
		}

		long bytes = indexSize(path);
		// Structural: a bulk load loses nothing. Reopen read-only to count.
		try (IndexUnit reader = IndexUnit.open(IndexUnitConfig.builder("perf", path)
				.access(AccessMode.READ_ONLY)
				.refresh(RefreshTrigger.manual())
				.build())) {
			long documents = reader.search(searcher -> (long) searcher.count(MatchAllDocsQuery.INSTANCE));
			assertThat(documents).isEqualTo(DOCUMENTS);
		}

		System.out.printf(Locale.ROOT,
				"[perf] bulk load %,d docs: %,d KiB on disk (%,d B/doc), heap grew by ~%,d MiB "
						+ "(peak sample)%n",
				DOCUMENTS, bytes / 1024, bytes / DOCUMENTS, (peak - before) / (1024 * 1024));
	}

	/**
	 * Stored fields are the usual suspect for index size, and the conventions deliberately do
	 * not store by default (§4). This measures what that decision is worth.
	 */
	@Test
	void storedFieldsAreWhatIndexSizeIsMostlyAbout(@TempDir Path directory) throws IOException {
		long withoutStored = writeAndMeasure(directory.resolve("lean"), corpus.mapping(false, false));
		long withStored = writeAndMeasure(directory.resolve("stored"), corpus.mapping(false, true));

		// Deterministic: storing a value cannot make the index smaller.
		assertThat(withStored).isGreaterThan(withoutStored);

		System.out.printf(Locale.ROOT,
				"[perf] %,d docs: %,d KiB without stored description, %,d KiB with (%+.0f%%)%n",
				DOCUMENTS, withoutStored / 1024, withStored / 1024,
				100.0 * (withStored - withoutStored) / withoutStored);
	}

	/** What a block costs on disk compared with the same objects flattened. */
	@Test
	void blocksCostTheirChildDocuments(@TempDir Path directory) throws IOException {
		long flat = writeAndMeasure(directory.resolve("flat"), corpus.mapping(false, false), 0);
		long blocks = writeAndMeasure(directory.resolve("blocks"), corpus.mapping(true, false), 3);

		assertThat(blocks).isGreaterThan(flat);
		System.out.printf(Locale.ROOT,
				"[perf] %,d objects: %,d KiB flat, %,d KiB as blocks of 4 documents (%.1fx)%n",
				DOCUMENTS, flat / 1024, blocks / 1024, (double) blocks / flat);
	}

	// --- helpers ------------------------------------------------------------------------------

	private long writeAndMeasure(Path path, org.eclipse.fennec.search.esearch.IndexUnitMapping mapping)
			throws IOException {
		return writeAndMeasure(path, mapping, 0);
	}

	private long writeAndMeasure(Path path, org.eclipse.fennec.search.esearch.IndexUnitMapping mapping,
			int reviews) throws IOException {
		DocumentMapper mapper = DocumentMapper.of(mapping);
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("perf", path)
				.access(AccessMode.BULK_LOAD)
				// BULK_LOAD opens no searcher, so MANUAL is the only refresh it can honour.
				.refresh(RefreshTrigger.manual())
				.commit(CommitPolicy.onClose())
				.build())) {
			for (int i = 0; i < DOCUMENTS; i++) {
				var mapped = mapper.map(corpus.product(i, reviews));
				if (mapped.isBlock()) {
					unit.updateDocuments(mapped.term(), mapped.documents());
				} else {
					unit.addDocument(mapped.root());
				}
			}
			unit.commit();
		}
		return indexSize(path);
	}

	private static long usedHeap() {
		Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}

	private static long indexSize(Path path) throws IOException {
		if (!Files.isDirectory(path)) {
			return 0;
		}
		try (var files = Files.list(path)) {
			return files.mapToLong(file -> {
				try {
					return Files.size(file);
				} catch (IOException e) {
					return 0L;
				}
			}).sum();
		}
	}
}
