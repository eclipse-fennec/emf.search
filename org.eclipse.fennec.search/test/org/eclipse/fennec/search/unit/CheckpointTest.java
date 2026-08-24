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
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Checkpointing (S18, #20): the index carries the position its content was derived from,
 * inside the very commit that made the content durable.
 * <p>
 * That coupling is the whole feature. A feed that stores its offset anywhere else has two
 * things that can disagree after a crash, and no way to tell which one is right; a
 * checkpoint in the commit point cannot drift, because a commit is all-or-nothing. The
 * cases below therefore care less about the API surface than about what survives a
 * simulated crash — a unit closed without committing — and what a fresh unit reads back
 * from the same directory afterwards.
 */
class CheckpointTest {

	@Test
	void aCommittedCheckpointIsReadBackByTheNextUnitOnTheSameDirectory(@TempDir Path path)
			throws IOException {
		try (IndexUnit unit = open(path)) {
			unit.addDocument(document("1"));
			unit.commit(Map.of("stream.offset", "4711", "source", "orders"));
		}

		try (IndexUnit reopened = open(path)) {
			assertThat(reopened.checkpoint())
					.containsEntry("stream.offset", "4711")
					.containsEntry("source", "orders");
		}
	}

	@Test
	void anIndexThatWasNeverCommittedToHasNoCheckpointRatherThanAFailure(@TempDir Path path)
			throws IOException {
		try (IndexUnit unit = open(path)) {
			assertThat(unit.checkpoint()).as("resume from the beginning").isEmpty();
		}
	}

	@Test
	void stagingDoesNotPublish(@TempDir Path path) throws IOException {
		try (IndexUnit unit = open(path)) {
			unit.commit(Map.of("stream.offset", "1"));
			unit.addDocument(document("2"));
			unit.checkpoint(Map.of("stream.offset", "2"));

			assertThat(unit.pendingCheckpoint()).containsEntry("stream.offset", "2");
			assertThat(unit.checkpoint()).as("what is durable is still the older position")
					.containsEntry("stream.offset", "1");
		}
	}

	@Test
	void theLastStagedValueBeforeACommitIsTheOneThatLands(@TempDir Path path) throws IOException {
		try (IndexUnit unit = open(path)) {
			unit.checkpoint(Map.of("stream.offset", "1"));
			unit.checkpoint(Map.of("stream.offset", "2"));
			unit.checkpoint(Map.of("stream.offset", "3"));
			unit.commit();

			assertThat(unit.checkpoint()).containsExactly(Map.entry("stream.offset", "3"));
		}
	}

	@Test
	void aCheckpointAloneIsCommittable(@TempDir Path path) throws IOException {
		// "I read up to here and it produced nothing to index" has to be recordable, or the
		// feed replays the empty stretch after every restart.
		try (IndexUnit unit = open(path)) {
			unit.addDocument(document("1"));
			unit.commit(Map.of("stream.offset", "1"));
			unit.commit(Map.of("stream.offset", "9"));

			assertThat(unit.checkpoint()).containsEntry("stream.offset", "9");
		}
		try (IndexUnit reopened = open(path)) {
			assertThat(reopened.checkpoint()).containsEntry("stream.offset", "9");
			assertThat(count(reopened)).isEqualTo(1);
		}
	}

	// --- what a crash leaves behind ---------------------------------------------------------

	@Test
	void whatWasNotCommittedIsGoneAndTheCheckpointSaysSo(@TempDir Path path) throws IOException {
		// The simulated crash: writes and a staged position that never reached a commit.
		try (IndexUnit unit = crashable(path)) {
			unit.addDocument(document("1"));
			unit.commit(Map.of("stream.offset", "100"));
			unit.addDocument(document("2"));
			unit.addDocument(document("3"));
			unit.checkpoint(Map.of("stream.offset", "300"));
		}

		try (IndexUnit reopened = open(path)) {
			assertThat(count(reopened)).as("only the committed document survived").isEqualTo(1);
			assertThat(reopened.checkpoint())
					.as("and the position matches exactly that content, so the feed resumes at 100")
					.containsEntry("stream.offset", "100");
		}
	}

	@Test
	void contentAndPositionCannotDisagreeAfterACrash(@TempDir Path path) throws IOException {
		// Several commits, then a crash mid-batch: whatever the checkpoint says, the index
		// holds exactly the documents of that same commit — never more, never fewer.
		try (IndexUnit unit = crashable(path)) {
			for (int i = 1; i <= 5; i++) {
				unit.addDocument(document(String.valueOf(i)));
				unit.commit(Map.of("stream.offset", String.valueOf(i)));
			}
			unit.addDocument(document("6"));
			unit.checkpoint(Map.of("stream.offset", "6"));
		}

		try (IndexUnit reopened = open(path)) {
			int offset = Integer.parseInt(reopened.checkpoint().get("stream.offset"));
			assertThat(count(reopened)).isEqualTo(offset);
		}
	}

	@Test
	void aStagedCheckpointLandsWhenTheUnitCommitsOnClose(@TempDir Path path) throws IOException {
		try (IndexUnit unit = open(path)) {
			unit.addDocument(document("1"));
			unit.checkpoint(Map.of("stream.offset", "42"));
		}

		try (IndexUnit reopened = open(path)) {
			assertThat(reopened.checkpoint()).containsEntry("stream.offset", "42");
			assertThat(count(reopened)).isEqualTo(1);
		}
	}

	// --- who may do what ---------------------------------------------------------------------

	@Test
	void aReadOnlyUnitReadsTheCheckpointButCannotWriteOne(@TempDir Path path) throws IOException {
		try (IndexUnit unit = open(path)) {
			unit.addDocument(document("1"));
			unit.commit(Map.of("stream.offset", "7"));
		}

		try (IndexUnit inspector = IndexUnit.open(IndexUnitConfig
				.builder("checkpoints", path.resolve("index"))
				.access(AccessMode.READ_ONLY)
				.refresh(RefreshTrigger.manual())
				.build())) {
			assertThat(inspector.checkpoint()).as("inspecting a suspect index is a read")
					.containsEntry("stream.offset", "7");
			assertThatThrownBy(() -> inspector.checkpoint(Map.of("stream.offset", "8")))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("refuses writes");
		}
	}

	@Test
	void aNullInTheCheckpointIsRefusedRatherThanWritten(@TempDir Path path) throws IOException {
		try (IndexUnit unit = open(path)) {
			Map<String, String> withNull = new HashMap<>();
			withNull.put("stream.offset", null);

			assertThatThrownBy(() -> unit.checkpoint(withNull))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("null value");
		}
	}

	// --- helpers -------------------------------------------------------------------------------

	private static IndexUnit open(Path path) throws IOException {
		return IndexUnit.open(IndexUnitConfig.builder("checkpoints", path.resolve("index"))
				.refresh(RefreshTrigger.manual())
				.build());
	}

	/** A unit that does not commit on close — the closest a test gets to pulling the plug. */
	private static IndexUnit crashable(Path path) throws IOException {
		return IndexUnit.open(IndexUnitConfig.builder("checkpoints", path.resolve("index"))
				.refresh(RefreshTrigger.manual())
				.commit(new CommitPolicy(0, Duration.ZERO, false))
				.build());
	}

	private static int count(IndexUnit unit) throws IOException {
		unit.refresh();
		return unit.search(searcher -> searcher.count(MatchAllDocsQuery.INSTANCE));
	}

	private static Iterable<IndexableField> document(String id) {
		Document document = new Document();
		document.add(new StringField("id", id, Store.YES));
		return document;
	}
}
