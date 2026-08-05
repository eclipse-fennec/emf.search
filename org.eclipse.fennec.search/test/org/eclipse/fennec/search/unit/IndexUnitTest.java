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

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Lifecycle behaviour of {@link IndexUnit} — plain JUnit against an in-memory directory,
 * no framework involved. What is asserted here is the contract the backend promises:
 * when writes become visible, when they become durable, and that closing is clean.
 */
class IndexUnitTest {

	private static Document document(String id, String text) {
		Document doc = new Document();
		doc.add(new StringField("id", id, Store.YES));
		doc.add(new TextField("text", text, Store.NO));
		return doc;
	}

	/** A block whose documents all carry the same block field, children first, parent last. */
	private static List<Document> block(String parentId, String... childIds) {
		List<Document> documents = new java.util.ArrayList<>();
		for (String childId : childIds) {
			Document child = document(childId, "child of " + parentId);
			child.add(new StringField("block", parentId, Store.NO));
			documents.add(child);
		}
		Document parent = document(parentId, "parent");
		parent.add(new StringField("block", parentId, Store.NO));
		documents.add(parent);
		return documents;
	}

	private static Term blockTerm(String parentId) {
		return new Term("block", parentId);
	}

	private static int countAll(IndexUnit unit) throws IOException {
		return unit.<Integer>search(searcher -> searcher.count(MatchAllDocsQuery.INSTANCE));
	}

	// --- visibility -------------------------------------------------------------------

	@Test
	void manualRefreshMakesWritesVisibleOnlyWhenAsked() throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("manual")
				.refresh(RefreshPolicy.manual()).build())) {

			unit.addDocument(document("1", "hello"));

			assertThat(countAll(unit)).as("write is not visible before a refresh").isZero();

			unit.refresh();

			assertThat(countAll(unit)).as("write is visible after the refresh").isEqualTo(1);
		}
	}

	@Test
	void nearRealTimeMakesWritesVisibleWithoutAnExplicitRefresh() throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("nrt")
				.refresh(RefreshPolicy.nearRealTime(Duration.ofMillis(50))).build())) {

			unit.addDocument(document("1", "hello"));

			// The reopen thread runs on its own schedule; poll instead of sleeping once,
			// so the test is neither flaky nor slower than it has to be.
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
			int found = 0;
			while (System.nanoTime() < deadline && found == 0) {
				found = countAll(unit);
				if (found == 0) {
					Thread.sleep(10);
				}
			}
			assertThat(found).as("near-real-time refresh made the write visible").isEqualTo(1);
		}
	}

	@Test
	void onCommitRefreshMakesWritesVisibleWhenTheWriterCommits() throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("on-commit")
				.refresh(RefreshPolicy.onCommit()).build())) {

			unit.addDocument(document("1", "hello"));
			assertThat(countAll(unit)).isZero();

			unit.commit();

			assertThat(countAll(unit)).isEqualTo(1);
		}
	}

	@Test
	void anAcquiredSearcherIsNotDisturbedByConcurrentWrites() throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("snapshot")
				.refresh(RefreshPolicy.manual()).build())) {

			unit.addDocument(document("1", "first"));
			unit.refresh();

			CountDownLatch inside = new CountDownLatch(1);
			CountDownLatch written = new CountDownLatch(1);
			Thread writerThread = new Thread(() -> {
				try {
					inside.await();
					unit.addDocument(document("2", "second"));
					unit.refresh();
					written.countDown();
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			});
			writerThread.start();

			int seen = unit.<Integer>search(searcher -> {
				inside.countDown();
				try {
					written.await(10, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IOException("interrupted while waiting for the concurrent write", e);
				}
				// The write and its refresh have happened; this searcher is still the
				// snapshot acquired before them.
				return searcher.count(MatchAllDocsQuery.INSTANCE);
			});
			writerThread.join(TimeUnit.SECONDS.toMillis(10));

			assertThat(seen).as("the acquired searcher kept its snapshot").isEqualTo(1);
			assertThat(countAll(unit)).as("a newly acquired searcher sees both").isEqualTo(2);
		}
	}

	// --- durability -------------------------------------------------------------------

	@Test
	void writesSurviveReopeningTheDirectoryWhenCommittedOnClose(@TempDir Path tempDir) throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("fs", tempDir).build())) {
			unit.addDocument(document("1", "hello"));
		}

		try (Directory directory = FSDirectory.open(tempDir); DirectoryReader reader = DirectoryReader.open(directory)) {
			assertThat(new IndexSearcher(reader).count(MatchAllDocsQuery.INSTANCE)).isEqualTo(1);
		}
	}

	@Test
	void theDocumentTriggerCommitsAndResetsTheCounter() throws Exception {
		Directory directory = new ByteBuffersDirectory();
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("count-trigger", directory)
				.refresh(RefreshPolicy.manual())
				.commit(CommitPolicy.afterDocuments(3))
				.build())) {

			unit.addDocument(document("1", "a"));
			unit.addDocument(document("2", "b"));
			assertThat(unit.uncommittedDocuments()).isEqualTo(2);
			assertThat(DirectoryReader.indexExists(directory)).as("nothing committed yet").isFalse();

			unit.addDocument(document("3", "c"));

			assertThat(unit.uncommittedDocuments()).as("counter reset by the automatic commit").isZero();
			try (DirectoryReader reader = DirectoryReader.open(directory)) {
				assertThat(new IndexSearcher(reader).count(MatchAllDocsQuery.INSTANCE)).isEqualTo(3);
			}
		}
	}

	@Test
	void aSecondCommitWithoutWritesReportsNothingToDo() throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("idle").build())) {
			// The first commit of a fresh index is never a no-op: it writes the initial
			// commit point, so it returns a real sequence number even with no documents.
			assertThat(unit.commit()).as("initial commit point").isNotEqualTo(-1L);

			assertThat(unit.commit()).as("nothing changed since the previous commit").isEqualTo(-1L);
		}
	}

	// --- write operations ---------------------------------------------------------------

	@Test
	void updateReplacesTheMatchingDocument() throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("update")
				.refresh(RefreshPolicy.manual()).build())) {

			unit.addDocument(document("1", "before"));
			unit.updateDocument(new Term("id", "1"), document("1", "after"));
			unit.refresh();

			assertThat(countAll(unit)).isEqualTo(1);
			assertThat(unit.<Integer>search(s -> s.count(new TermQuery(new Term("text", "after"))))).isEqualTo(1);
			assertThat(unit.<Integer>search(s -> s.count(new TermQuery(new Term("text", "before"))))).isZero();
		}
	}

	@Test
	void aBlockIsWrittenAndReplacedAsAWhole() throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("block")
				.refresh(RefreshPolicy.manual()).build())) {

			// children first, parent last — the block-join layout. The delete term has to
			// match EVERY document of the block, not just the parent: updateDocuments
			// deletes by term and then appends, so a parent-only term would leave the old
			// children behind as orphans. Hence the shared block field.
			unit.updateDocuments(blockTerm("parent"), block("parent", "child-1", "child-2"));
			unit.refresh();
			assertThat(countAll(unit)).isEqualTo(3);

			unit.updateDocuments(blockTerm("parent"), block("parent", "child-1"));
			unit.refresh();

			assertThat(countAll(unit)).as("the whole previous block was replaced").isEqualTo(2);
		}
	}

	@Test
	void aBlockDeleteTermThatOnlyMatchesTheParentOrphansTheChildren() throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("orphans")
				.refresh(RefreshPolicy.manual()).build())) {

			unit.updateDocuments(new Term("id", "parent"),
					List.of(document("child-1", "a"), document("child-2", "b"), document("parent", "p")));
			unit.refresh();

			unit.updateDocuments(new Term("id", "parent"),
					List.of(document("child-1", "a"), document("parent", "p")));
			unit.refresh();

			// Pinned deliberately: this is Lucene's behaviour, not a bug in the unit, and
			// it is the trap the block-join mapping has to avoid (see S11).
			assertThat(countAll(unit)).as("old children survive a parent-only delete term").isEqualTo(4);
		}
	}

	@Test
	void anEmptyBlockIsRejected() throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("empty-block").build())) {
			assertThatThrownBy(() -> unit.updateDocuments(new Term("id", "x"), List.<Document>of()))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("at least the parent");
		}
	}

	@Test
	void deleteRemovesTheMatchingDocument() throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("delete")
				.refresh(RefreshPolicy.manual()).build())) {

			unit.addDocument(document("1", "a"));
			unit.addDocument(document("2", "b"));
			unit.refresh();
			assertThat(countAll(unit)).isEqualTo(2);

			unit.deleteDocuments(new Term("id", "1"));
			unit.refresh();

			assertThat(countAll(unit)).as("deletes are applied on refresh, not at merge time").isEqualTo(1);
		}
	}

	// --- lifecycle ----------------------------------------------------------------------

	@Test
	void closeIsIdempotentAndFurtherUseIsRejected() throws Exception {
		IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("closing").build());
		unit.addDocument(document("1", "a"));

		unit.close();
		unit.close();

		assertThat(unit.isClosed()).isTrue();
		assertThatThrownBy(() -> unit.addDocument(document("2", "b")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("closing");
		assertThatThrownBy(() -> unit.<Integer>search(searcher -> searcher.count(MatchAllDocsQuery.INSTANCE)))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void anExistingIndexIsAppendedToRatherThanOverwritten(@TempDir Path tempDir) throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("append", tempDir).build())) {
			unit.addDocument(document("1", "first"));
		}
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("append", tempDir)
				.refresh(RefreshPolicy.manual()).build())) {
			unit.addDocument(document("2", "second"));
			unit.refresh();

			assertThat(countAll(unit)).isEqualTo(2);
		}
	}

	@Test
	void twoUnitsInOneJvmDoNotSeeEachOther() throws Exception {
		try (IndexUnit first = IndexUnit.open(IndexUnitConfig.inMemory("one")
				.refresh(RefreshPolicy.manual()).build());
				IndexUnit second = IndexUnit.open(IndexUnitConfig.inMemory("two")
						.refresh(RefreshPolicy.manual()).build())) {

			first.addDocument(document("1", "a"));
			first.refresh();
			second.refresh();

			assertThat(countAll(first)).isEqualTo(1);
			assertThat(countAll(second)).as("no shared static state between units").isZero();
			assertThat(first.name()).isEqualTo("one");
			assertThat(second.name()).isEqualTo("two");
		}
	}

	@Test
	void theConfiguredIndexSortIsAppliedToTheIndex(@TempDir Path tempDir) throws Exception {
		org.apache.lucene.search.Sort sort = new org.apache.lucene.search.Sort(
				new org.apache.lucene.search.SortField("rank", org.apache.lucene.search.SortField.Type.LONG));

		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("sorted", tempDir)
				.indexSort(sort)
				.refresh(RefreshPolicy.manual())
				.build())) {

			for (long rank : new long[] { 3, 1, 2 }) {
				Document doc = document("id-" + rank, "x");
				doc.add(new org.apache.lucene.document.NumericDocValuesField("rank", rank));
				unit.addDocument(doc);
			}
			unit.commit();
			unit.refresh();

			List<Long> ranks = unit.search(searcher -> {
				var leaves = searcher.getIndexReader().leaves();
				var values = leaves.get(0).reader().getNumericDocValues("rank");
				var result = new java.util.ArrayList<Long>();
				for (int doc = values.nextDoc(); doc != org.apache.lucene.search.DocIdSetIterator.NO_MORE_DOCS;
						doc = values.nextDoc()) {
					result.add(values.longValue());
				}
				return result;
			});

			assertThat(ranks).as("documents are stored in index-sort order").containsExactly(1L, 2L, 3L);
		}
	}

	// --- documents are what the caller passed --------------------------------------------

	@Test
	void storedFieldsComeBackFromTheSearcher() throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("stored")
				.refresh(RefreshPolicy.manual()).build())) {

			unit.addDocument(document("42", "hello"));
			unit.refresh();

			String id = unit.search(searcher -> {
				var hits = searcher.search(new TermQuery(new Term("id", "42")), 1);
				IndexableField field = searcher.storedFields().document(hits.scoreDocs[0].doc).getField("id");
				return field.stringValue();
			});

			assertThat(id).isEqualTo("42");
		}
	}
}
