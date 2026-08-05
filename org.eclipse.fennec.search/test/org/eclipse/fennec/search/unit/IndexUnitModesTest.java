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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The three configuration axes — access mode, visibility and refresh trigger — and what
 * each of them actually changes. These are the assertions that distinguish a promise from
 * a wish: that {@code COMMITTED} really cannot see uncommitted writes, that
 * {@code READ_ONLY} really refuses, and that a read-only unit really does take the lock.
 */
class IndexUnitModesTest {

	private static Document document(String id) {
		Document doc = new Document();
		doc.add(new StringField("id", id, Store.YES));
		return doc;
	}

	private static int countAll(IndexUnit unit) throws IOException {
		return unit.<Integer>search(searcher -> searcher.count(MatchAllDocsQuery.INSTANCE));
	}

	// --- visibility ---------------------------------------------------------------------

	@Test
	void committedVisibilityHidesUncommittedWritesEvenAfterARefresh() throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("committed")
				.visibility(Visibility.COMMITTED)
				.refresh(RefreshTrigger.manual())
				.build())) {

			unit.addDocument(document("1"));
			unit.refresh();

			assertThat(countAll(unit)).as("a refresh cannot expose what was never committed").isZero();

			unit.commit();
			unit.refresh();

			assertThat(countAll(unit)).as("the commit made it visible").isEqualTo(1);
		}
	}

	@Test
	void nrtVisibilityExposesUncommittedWritesOnRefresh() throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("nrt")
				.visibility(Visibility.NRT)
				.refresh(RefreshTrigger.manual())
				.build())) {

			unit.addDocument(document("1"));
			unit.refresh();

			assertThat(countAll(unit)).as("near-real-time sees the write before any commit").isEqualTo(1);
			assertThat(unit.uncommittedDocuments()).as("and it is genuinely uncommitted").isEqualTo(1);
		}
	}

	@Test
	void committedVisibilityWorksOnAFreshDirectoryThatHasNoCommitYet() throws Exception {
		// A directory-based searcher needs a commit point to open on; the unit creates the
		// initial one rather than failing on an empty directory.
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("fresh")
				.visibility(Visibility.COMMITTED)
				.refresh(RefreshTrigger.manual())
				.build())) {

			assertThat(countAll(unit)).isZero();
		}
	}

	@Test
	void theBackgroundTriggerRefreshesACommittedSearcherToo() throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.inMemory("committed-background")
				.visibility(Visibility.COMMITTED)
				.refresh(RefreshTrigger.background(Duration.ofMillis(50)))
				.build())) {

			unit.addDocument(document("1"));
			unit.commit();

			long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
			int found = 0;
			while (System.nanoTime() < deadline && found == 0) {
				found = countAll(unit);
				if (found == 0) {
					Thread.sleep(10);
				}
			}
			assertThat(found).as("the scheduled reopen picked the commit up").isEqualTo(1);
		}
	}

	// --- access modes -------------------------------------------------------------------

	@Test
	void aReadOnlyUnitRefusesEveryWriteButReads(@TempDir Path tempDir) throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("seed", tempDir).build())) {
			unit.addDocument(document("1"));
		}

		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("read-only", tempDir)
				.access(AccessMode.READ_ONLY)
				.visibility(Visibility.COMMITTED)
				.refresh(RefreshTrigger.manual())
				.build())) {

			assertThat(countAll(unit)).as("existing content is readable").isEqualTo(1);

			assertThatThrownBy(() -> unit.addDocument(document("2")))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("READ_ONLY")
					.hasMessageContaining("refuses writes");
			assertThatThrownBy(() -> unit.deleteDocuments(new Term("id", "1")))
					.isInstanceOf(IllegalStateException.class);
			assertThatThrownBy(unit::commit).isInstanceOf(IllegalStateException.class);
		}
	}

	@Test
	void aReadOnlyUnitStillTakesTheWriteLockAndSaysSoWhenItCannot(@TempDir Path tempDir) throws Exception {
		try (IndexUnit holder = IndexUnit.open(IndexUnitConfig.builder("holder", tempDir).build())) {
			holder.addDocument(document("1"));
			holder.commit();

			// Documented consequence of opening a writer in every mode: a read-only unit
			// cannot share a directory with another writer.
			assertThatThrownBy(() -> IndexUnit.open(IndexUnitConfig.builder("second", tempDir)
					.access(AccessMode.READ_ONLY)
					.build()))
					.isInstanceOf(IOException.class)
					.hasMessageContaining("write lock is held elsewhere")
					.hasMessageContaining("READ_ONLY");
		}
	}

	@Test
	void aReadOnlyUnitDoesNotCommitOnClose(@TempDir Path tempDir) throws Exception {
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("seed", tempDir).build())) {
			unit.addDocument(document("1"));
		}

		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("read-only", tempDir)
				.access(AccessMode.READ_ONLY)
				.visibility(Visibility.COMMITTED)
				.refresh(RefreshTrigger.manual())
				.build())) {
			assertThat(countAll(unit)).isEqualTo(1);
		}

		try (IndexUnit reopened = IndexUnit.open(IndexUnitConfig.builder("check", tempDir)
				.visibility(Visibility.COMMITTED)
				.refresh(RefreshTrigger.manual())
				.build())) {
			assertThat(countAll(reopened)).as("the read-only unit changed nothing").isEqualTo(1);
		}
	}

	@Test
	void aBulkLoadUnitWritesButRefusesToSearch() throws Exception {
		Directory directory = new ByteBuffersDirectory();
		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("bulk", directory)
				.access(AccessMode.BULK_LOAD)
				.refresh(RefreshTrigger.manual())
				.build())) {

			unit.addDocument(document("1"));
			unit.addDocument(document("2"));
			unit.commit();

			assertThatThrownBy(() -> unit.search(searcher -> searcher.count(MatchAllDocsQuery.INSTANCE)))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("BULK_LOAD")
					.hasMessageContaining("no searcher");
		}

		// What it wrote is there — the point of the mode is that nothing pays for
		// visibility while the load runs, not that the writes are second class.
		try (IndexUnit reader = IndexUnit.open(IndexUnitConfig.builder("bulk-read", directory)
				.visibility(Visibility.COMMITTED)
				.refresh(RefreshTrigger.manual())
				.build())) {
			assertThat(countAll(reader)).isEqualTo(2);
		}
	}

	@Test
	void aBulkLoadUnitWithABackgroundTriggerIsRejectedRatherThanSilentlyIgnored() {
		assertThatThrownBy(() -> IndexUnitConfig.inMemory("bulk")
				.access(AccessMode.BULK_LOAD)
				.refresh(RefreshTrigger.background())
				.build())
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("BULK_LOAD opens no searcher");
	}

	// --- locations ----------------------------------------------------------------------

	@Test
	void theInMemoryLocationKeepsNothingAfterClose() throws Exception {
		IndexLocation location = IndexLocation.inMemory();

		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("volatile", location)
				.refresh(RefreshTrigger.manual()).build())) {
			unit.addDocument(document("1"));
			unit.commit();
		}

		// A fresh in-memory directory per open() — that is what makes the location a value
		// rather than a handle to shared state.
		try (IndexUnit second = IndexUnit.open(IndexUnitConfig.builder("volatile", location)
				.refresh(RefreshTrigger.manual()).build())) {
			assertThat(countAll(second)).isZero();
		}
	}

	@Test
	void theFileSystemLocationSurvivesTheUnit(@TempDir Path tempDir) throws Exception {
		IndexLocation location = IndexLocation.path(tempDir);

		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("durable", location).build())) {
			unit.addDocument(document("1"));
		}
		try (IndexUnit second = IndexUnit.open(IndexUnitConfig.builder("durable", location)
				.refresh(RefreshTrigger.manual()).build())) {
			assertThat(countAll(second)).isEqualTo(1);
		}
		assertThat(location.describe()).isEqualTo(tempDir.toString());
	}

	@Test
	void aProvidedDirectoryIsSharedBetweenUnitsThatUseIt() throws Exception {
		Directory shared = new ByteBuffersDirectory();
		IndexLocation location = IndexLocation.directory(shared);

		try (IndexUnit unit = IndexUnit.open(IndexUnitConfig.builder("provided", location)
				.refresh(RefreshTrigger.manual()).build())) {
			unit.addDocument(document("1"));
			unit.commit();
		}
		try (IndexUnit second = IndexUnit.open(IndexUnitConfig.builder("provided", location)
				.visibility(Visibility.COMMITTED)
				.refresh(RefreshTrigger.manual()).build())) {
			assertThat(countAll(second)).as("the same directory instance, so the content is there").isEqualTo(1);
		}
	}
}
