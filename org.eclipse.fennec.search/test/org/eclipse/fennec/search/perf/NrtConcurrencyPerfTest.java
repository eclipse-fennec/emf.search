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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.MappedDocument;
import org.eclipse.fennec.search.mapping.SearchFields;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.eclipse.fennec.search.unit.Visibility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Writers and searchers at the same time (issue #38) — the situation a search backend is
 * actually judged on, and the one the semantic tests never enter.
 * <p>
 * Two kinds of statement live here and they are treated differently. <b>Correctness under
 * concurrency is asserted</b>: a searcher must never observe half a block, and a document
 * written before a refresh must be visible after it. <b>Latency is measured and logged</b>:
 * how quickly NRT actually means "soon" is a number worth watching over time, but a slow
 * machine must not fail the build for it.
 */
@Tag("perf")
class NrtConcurrencyPerfTest {

	private static final int BLOCKS = 2_000 * PerfCorpus.SCALE;
	private static final int REVIEWS_PER_PRODUCT = 3;
	private static final int BLOCK_SIZE = REVIEWS_PER_PRODUCT + 1;
	private static final int SEARCHERS = 4;

	private static PerfCorpus corpus;

	@BeforeAll
	static void loadModel() {
		corpus = PerfCorpus.load();
	}

	/**
	 * The invariant that makes the block join trustworthy: Lucene writes a block as one
	 * contiguous unit, so a concurrent searcher sees either all of it or none of it. If the
	 * mapper or the unit ever wrote children separately from their parent, a reader would
	 * catch a child without its parent — and a block join would then silently produce a
	 * partial answer.
	 */
	@Test
	void noSearcherEverSeesHalfABlock() throws Exception {
		DocumentMapper mapper = DocumentMapper.of(corpus.mapping(true, false));
		Queue<String> violations = new ConcurrentLinkedQueue<>();
		AtomicLong observations = new AtomicLong();
		AtomicBoolean writing = new AtomicBoolean(true);

		try (IndexUnit unit = IndexUnit.open(nrtConfig())) {
			ExecutorService readers = Executors.newFixedThreadPool(SEARCHERS);
			List<CompletableFuture<Void>> watchers = new ArrayList<>();
			for (int reader = 0; reader < SEARCHERS; reader++) {
				watchers.add(CompletableFuture.runAsync(() -> {
					while (writing.get()) {
						for (int i = 0; i < BLOCKS; i += Math.max(1, BLOCKS / 200)) {
							String rootId = "p-" + i;
							try {
								long visible = unit.search(searcher -> (long) searcher
										.count(new TermQuery(new Term(SearchFields.ROOT, rootId))));
								observations.incrementAndGet();
								if (visible != 0 && visible != BLOCK_SIZE) {
									violations.add(rootId + " was visible as " + visible
											+ " documents, which is neither 0 nor a whole block of "
											+ BLOCK_SIZE);
								}
							} catch (IOException e) {
								violations.add("search failed: " + e);
							}
						}
					}
				}, readers));
			}

			long start = System.nanoTime();
			for (int i = 0; i < BLOCKS; i++) {
				MappedDocument mapped = mapper.map(corpus.product(i, REVIEWS_PER_PRODUCT));
				unit.updateDocuments(mapped.term(), mapped.documents());
			}
			long writeNanos = System.nanoTime() - start;
			writing.set(false);
			CompletableFuture.allOf(watchers.toArray(CompletableFuture[]::new)).get(2, TimeUnit.MINUTES);
			readers.shutdown();

			unit.refresh();
			long documents = unit.search(searcher -> (long) searcher.count(MatchAllDocsQuery.INSTANCE));

			assertThat(violations).isEmpty();
			assertThat(documents).isEqualTo((long) BLOCKS * BLOCK_SIZE);
			System.out.printf("[perf] %,d blocks written in %,d ms while %,d concurrent block "
					+ "observations found no partial block%n",
					BLOCKS, Duration.ofNanos(writeNanos).toMillis(), observations.get());
		}
	}

	/** How long "near real time" takes here, measured rather than assumed. */
	@Test
	void nearRealTimeVisibilityLatency() throws Exception {
		DocumentMapper mapper = DocumentMapper.of(corpus.mapping(false, false));
		List<Long> latencies = new ArrayList<>();
		int samples = 25;

		try (IndexUnit unit = IndexUnit.open(nrtConfig())) {
			for (int sample = 0; sample < samples; sample++) {
				String id = "p-" + sample;
				MappedDocument mapped = mapper.map(corpus.product(sample, 0));
				long written = System.nanoTime();
				unit.addDocument(mapped.root());
				Query query = new TermQuery(new Term(SearchFields.ID, id));
				long deadline = written + Duration.ofSeconds(30).toNanos();
				while (unit.search(searcher -> searcher.count(query)) == 0) {
					if (System.nanoTime() > deadline) {
						throw new AssertionError("Document " + id + " never became visible under "
								+ "NEAR_REAL_TIME within 30 s — that is a broken promise, not a slow "
								+ "machine.");
					}
					Thread.sleep(1);
				}
				latencies.add(System.nanoTime() - written);
			}
		}

		latencies.sort(Long::compare);
		System.out.printf("[perf] NRT visibility over %d samples: median %,d ms, p90 %,d ms, "
				+ "max %,d ms%n", samples,
				Duration.ofNanos(latencies.get(latencies.size() / 2)).toMillis(),
				Duration.ofNanos(latencies.get((int) (latencies.size() * 0.9))).toMillis(),
				Duration.ofNanos(latencies.get(latencies.size() - 1)).toMillis());
	}

	/** With MANUAL refresh nothing appears on its own — the promise in the other direction. */
	@Test
	void manualRefreshShowsNothingUntilItIsAsked() throws Exception {
		DocumentMapper mapper = DocumentMapper.of(corpus.mapping(false, false));
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("perf")
				.refresh(RefreshTrigger.manual())
				.visibility(Visibility.NRT)
				.build())) {
			for (int i = 0; i < 100; i++) {
				unit.addDocument(mapper.map(corpus.product(i, 0)).root());
			}
			Thread.sleep(200);
			long beforeRefresh = unit.search(searcher -> (long) searcher.count(MatchAllDocsQuery.INSTANCE));
			assertThat(beforeRefresh).isZero();
			unit.refresh();
			long afterRefresh = unit.search(searcher -> (long) searcher.count(MatchAllDocsQuery.INSTANCE));
			assertThat(afterRefresh).isEqualTo(100);
		}
	}

	/** Query latency while indexing continues, against the same query on a quiet index. */
	@Test
	void queryLatencyDegradationWhileIndexing() throws Exception {
		DocumentMapper mapper = DocumentMapper.of(corpus.mapping(false, false));
		Query query = new TermQuery(new Term("tags", "even"));

		try (IndexUnit unit = IndexUnit.open(nrtConfig())) {
			for (int i = 0; i < BLOCKS; i++) {
				unit.addDocument(mapper.map(corpus.product(i, 0)).root());
			}
			unit.refresh();
			// Warm up first: without it the "quiet" figure carries JIT and the comparison below
			// reads backwards.
			timeSearches(unit, query, 500);
			long quiet = timeSearches(unit, query, 500);

			AtomicBoolean writing = new AtomicBoolean(true);
			CompletableFuture<Void> writer = CompletableFuture.runAsync(() -> {
				int i = BLOCKS;
				while (writing.get()) {
					try {
						unit.addDocument(mapper.map(corpus.product(i++, 0)).root());
					} catch (IOException e) {
						throw new IllegalStateException(e);
					}
				}
			});
			long busy = timeSearches(unit, query, 500);
			writing.set(false);
			writer.get(1, TimeUnit.MINUTES);

			System.out.printf("[perf] query latency quiet %,d us, while indexing %,d us (%+.0f%%)%n",
					quiet / 1_000, busy / 1_000, 100.0 * (busy - quiet) / quiet);
		}
	}

	/** Closing a unit while searches are in flight must not corrupt or hang anything. */
	@Test
	void aUnitClosesCleanlyWithSearchesInFlight() throws Exception {
		DocumentMapper mapper = DocumentMapper.of(corpus.mapping(false, false));
		IndexUnit unit = IndexUnit.open(nrtConfig());
		for (int i = 0; i < 500; i++) {
			unit.addDocument(mapper.map(corpus.product(i, 0)).root());
		}
		unit.refresh();

		CountDownLatch started = new CountDownLatch(SEARCHERS);
		AtomicBoolean searching = new AtomicBoolean(true);
		Queue<String> unexpected = new ConcurrentLinkedQueue<>();
		ExecutorService readers = Executors.newFixedThreadPool(SEARCHERS);
		for (int reader = 0; reader < SEARCHERS; reader++) {
			readers.submit(() -> {
				started.countDown();
				while (searching.get()) {
					try {
						unit.search(searcher -> searcher.count(MatchAllDocsQuery.INSTANCE));
					} catch (IllegalStateException e) {
						// The documented answer once the unit is closed.
						return;
					} catch (Exception e) {
						unexpected.add(e.getClass().getName() + ": " + e.getMessage());
						return;
					}
				}
			});
		}
		started.await(30, TimeUnit.SECONDS);
		unit.close();
		searching.set(false);
		readers.shutdown();
		assertThat(readers.awaitTermination(1, TimeUnit.MINUTES)).isTrue();

		assertThat(unexpected).isEmpty();
		assertThat(unit.isClosed()).isTrue();
		assertThatThrownBy(() -> unit.search(searcher -> searcher.count(MatchAllDocsQuery.INSTANCE)))
				.isInstanceOf(IllegalStateException.class);
	}

	// --- helpers ------------------------------------------------------------------------------

	private static IndexUnitConfig nrtConfig() {
		return IndexUnitConfig.inMemory("perf")
				.refresh(RefreshTrigger.background(Duration.ofMillis(50)))
				.visibility(Visibility.NRT)
				.build();
	}

	private static long timeSearches(IndexUnit unit, Query query, int repetitions) throws IOException {
		long start = System.nanoTime();
		for (int i = 0; i < repetitions; i++) {
			unit.search(searcher -> searcher.count(query));
		}
		return (System.nanoTime() - start) / repetitions;
	}
}
