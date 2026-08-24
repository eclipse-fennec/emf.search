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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.Term;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The commit notification surface (#48): a listener hears every commit that made writes
 * durable — explicit or policy-driven — and nothing else. What listeners are <em>for</em>
 * (rebuilding derived structures) is pinned where those structures live; this is only the
 * contract of the surface itself.
 */
class CommitListenerTest {

	private IndexUnit unit;

	@BeforeEach
	void openUnit() throws IOException {
		unit = IndexUnit.open(IndexUnitConfig.inMemory("callbacks").build());
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	@Test
	void aListenerHearsAnExplicitCommit() throws Exception {
		List<Long> heard = new ArrayList<>();
		unit.onCommit(heard::add);

		write("d-1");
		long sequence = unit.commit();

		assertThat(heard).containsExactly(sequence);
	}

	@Test
	void aNoOpCommitTellsNobody() throws Exception {
		// The first commit of the fresh unit is real (it writes the initial commit point);
		// from then on a commit without pending writes is a no-op and stays silent —
		// a listener that rebuilds on commit must not rebuild over nothing.
		write("d-1");
		unit.commit();
		List<Long> heard = new ArrayList<>();
		unit.onCommit(heard::add);

		unit.commit();

		assertThat(heard).isEmpty();
	}

	@Test
	void aPolicyDrivenCommitIsHeardToo() throws Exception {
		try (IndexUnit triggered = IndexUnit.open(IndexUnitConfig.inMemory("triggered")
				.commit(CommitPolicy.afterDocuments(1))
				.build())) {
			List<Long> heard = new ArrayList<>();
			triggered.onCommit(heard::add);

			Document document = new Document();
			document.add(new StringField("id", "d-1", Store.YES));
			triggered.updateDocuments(new Term("id", "d-1"), List.of(document));

			assertThat(heard).as("the document trigger commits through the same path").hasSize(1);
		}
	}

	@Test
	void theHandleEndsTheSubscription() throws Exception {
		List<Long> heard = new ArrayList<>();
		AutoCloseable subscription = unit.onCommit(heard::add);
		subscription.close();

		write("d-1");
		unit.commit();

		assertThat(heard).isEmpty();
	}

	@Test
	void aFailingListenerNeitherFailsTheCommitNorStarvesTheNext() throws Exception {
		List<Long> heard = new ArrayList<>();
		unit.onCommit(sequence -> {
			throw new IllegalStateException("deliberately broken listener");
		});
		unit.onCommit(heard::add);

		write("d-1");
		long sequence = unit.commit();

		assertThat(sequence).isNotNegative();
		assertThat(heard).as("the listener after the broken one still hears the commit")
				.containsExactly(sequence);
	}

	private void write(String id) throws IOException {
		Document document = new Document();
		document.add(new StringField("id", id, Store.YES));
		unit.updateDocuments(new Term("id", id), List.of(document));
	}
}
