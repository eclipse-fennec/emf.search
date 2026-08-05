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

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;

/**
 * One index unit: one Lucene directory, one {@link IndexWriter}, one
 * {@link SearcherManager}, with the refresh and commit policy of its configuration.
 * <p>
 * The unit owns everything it opens and closes it again — that is why writes go through
 * the unit rather than through a handed-out writer: it is the only way the unit can honour
 * a document-count commit trigger, and it keeps the writer's lifecycle from escaping.
 * <p>
 * No static state and no framework: two units can run in one JVM without seeing each
 * other, which is both an OSGi requirement (a second configuration must not corrupt the
 * first) and what makes the whole class testable with plain JUnit.
 * <p>
 * Thread safety mirrors Lucene's: writes and searches may run concurrently from several
 * threads. A searcher acquired through {@link #search(SearchFunction)} is a stable
 * snapshot for the duration of the call, unaffected by concurrent writes.
 *
 * @author Data In Motion Consulting
 */
public final class IndexUnit implements AutoCloseable {

	/** What a caller does with an acquired searcher. */
	@FunctionalInterface
	public interface SearchFunction<T> {
		T apply(IndexSearcher searcher) throws IOException;
	}

	private final IndexUnitConfig config;
	private final IndexWriter writer;
	private final SearcherManager searcherManager;
	private final ControlledRealTimeReopenThread<IndexSearcher> reopenThread;
	private final ScheduledExecutorService commitScheduler;
	private final AtomicLong uncommitted = new AtomicLong();
	private final AtomicBoolean closed = new AtomicBoolean();

	private IndexUnit(IndexUnitConfig config, IndexWriter writer, SearcherManager searcherManager,
			ControlledRealTimeReopenThread<IndexSearcher> reopenThread,
			ScheduledExecutorService commitScheduler) {
		this.config = config;
		this.writer = writer;
		this.searcherManager = searcherManager;
		this.reopenThread = reopenThread;
		this.commitScheduler = commitScheduler;
	}

	/**
	 * Opens the unit described by {@code config}, creating the index if the directory is
	 * empty and appending to it otherwise.
	 *
	 * @throws IOException if the index cannot be opened
	 */
	public static IndexUnit open(IndexUnitConfig config) throws IOException {
		Objects.requireNonNull(config, "config");

		IndexWriterConfig writerConfig = new IndexWriterConfig(config.analyzers().defaultAnalyzer())
				.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)
				.setCommitOnClose(config.commit().commitOnClose());
		if (config.indexSort() != null) {
			writerConfig.setIndexSort(config.indexSort());
		}

		IndexWriter writer = new IndexWriter(config.directory(), writerConfig);
		SearcherManager searcherManager;
		try {
			// applyAllDeletes/writeAllDeletes: a unit is a store, so a delete must be
			// visible on the next refresh rather than at some later merge.
			searcherManager = new SearcherManager(writer, true, true, null);
		} catch (IOException | RuntimeException e) {
			closeQuietly(writer, e);
			throw e;
		}

		ControlledRealTimeReopenThread<IndexSearcher> reopenThread = null;
		ScheduledExecutorService scheduler = null;
		try {
			if (config.refresh().mode() == RefreshPolicy.Mode.NEAR_REAL_TIME) {
				double staleSeconds = config.refresh().interval().toNanos() / 1_000_000_000.0;
				reopenThread = new ControlledRealTimeReopenThread<>(writer, searcherManager, staleSeconds,
						staleSeconds);
				reopenThread.setName("fennec-search-reopen-" + config.name());
				reopenThread.setDaemon(true);
				reopenThread.start();
			}
			if (config.commit().hasIntervalTrigger()) {
				scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
					Thread thread = new Thread(runnable, "fennec-search-commit-" + config.name());
					thread.setDaemon(true);
					return thread;
				});
			}
		} catch (RuntimeException e) {
			closeQuietly(searcherManager, e);
			closeQuietly(writer, e);
			throw e;
		}

		IndexUnit unit = new IndexUnit(config, writer, searcherManager, reopenThread, scheduler);
		if (scheduler != null) {
			long millis = config.commit().maxInterval().toMillis();
			scheduler.scheduleWithFixedDelay(unit::commitQuietly, millis, millis, TimeUnit.MILLISECONDS);
		}
		return unit;
	}

	/** The unit name from its configuration. */
	public String name() {
		return config.name();
	}

	/** The configuration this unit was opened with. */
	public IndexUnitConfig config() {
		return config;
	}

	/** Adds a document. */
	public void addDocument(Iterable<? extends IndexableField> document) throws IOException {
		checkOpen();
		writer.addDocument(document);
		wrote(1);
	}

	/** Replaces the documents matching {@code id} with {@code document}, or adds it if none match. */
	public void updateDocument(Term id, Iterable<? extends IndexableField> document) throws IOException {
		checkOpen();
		writer.updateDocument(id, document);
		wrote(1);
	}

	/**
	 * Replaces the block matching {@code id} with {@code documents}, written as one
	 * contiguous block so that a block join can treat the last document as the parent of
	 * the preceding ones.
	 * <p>
	 * Two invariants belong to the caller, and getting either wrong corrupts the index
	 * quietly rather than loudly:
	 * <ul>
	 * <li>children precede their parent in the list;</li>
	 * <li><b>{@code id} must match every document of the previous block, not just its
	 * parent.</b> This method deletes by term and then appends, so a term matching only
	 * the parent leaves the previous children behind as orphans that no block join will
	 * ever reach — and that still count in a match-all. Give every document of a block a
	 * shared field carrying the parent id and delete on that.</li>
	 * </ul>
	 * Both are pinned by tests, including one that deliberately reproduces the orphan case.
	 */
	public void updateDocuments(Term id, List<? extends Iterable<? extends IndexableField>> documents)
			throws IOException {
		checkOpen();
		Objects.requireNonNull(documents, "documents");
		if (documents.isEmpty()) {
			throw new IllegalArgumentException("A block must contain at least the parent document");
		}
		writer.updateDocuments(id, documents);
		wrote(documents.size());
	}

	/** Deletes every document matching any of the terms. */
	public void deleteDocuments(Term... terms) throws IOException {
		checkOpen();
		writer.deleteDocuments(terms);
		wrote(terms.length);
	}

	/** Deletes everything in the unit. */
	public void deleteAll() throws IOException {
		checkOpen();
		writer.deleteAll();
		wrote(1);
	}

	/**
	 * Runs {@code function} against a searcher and releases it afterwards, also when the
	 * function throws — the release is the part callers forget, so the unit does it.
	 */
	public <T> T search(SearchFunction<T> function) throws IOException {
		checkOpen();
		Objects.requireNonNull(function, "function");
		IndexSearcher searcher = searcherManager.acquire();
		try {
			return function.apply(searcher);
		} finally {
			searcherManager.release(searcher);
		}
	}

	/**
	 * Reopens the searcher so that everything written so far is visible, and blocks until
	 * it is. This is what {@link RefreshPolicy.Mode#MANUAL} exists for, and what a test
	 * calls instead of sleeping.
	 */
	public void refresh() throws IOException {
		checkOpen();
		searcherManager.maybeRefreshBlocking();
	}

	/**
	 * Commits, resets the uncommitted-document count and, under
	 * {@link RefreshPolicy.Mode#ON_COMMIT}, refreshes the searcher.
	 *
	 * @return the commit sequence number, or {@code -1} if there was nothing to commit
	 */
	public long commit() throws IOException {
		checkOpen();
		long sequenceNumber = writer.commit();
		uncommitted.set(0);
		if (config.refresh().mode() == RefreshPolicy.Mode.ON_COMMIT) {
			searcherManager.maybeRefreshBlocking();
		}
		return sequenceNumber;
	}

	/** The number of writes since the last commit. */
	public long uncommittedDocuments() {
		return uncommitted.get();
	}

	/** Whether the unit has been closed. */
	public boolean isClosed() {
		return closed.get();
	}

	/**
	 * Closes reopen thread, commit scheduler, searcher manager, writer and directory, in
	 * that order. Idempotent. Whether the pending writes survive is
	 * {@link CommitPolicy#commitOnClose()}.
	 */
	@Override
	public void close() throws IOException {
		if (!closed.compareAndSet(false, true)) {
			return;
		}
		IOException failure = null;
		if (reopenThread != null) {
			try {
				reopenThread.close();
			} catch (RuntimeException e) {
				failure = new IOException("Failed to stop the reopen thread of unit " + config.name(), e);
			}
		}
		if (commitScheduler != null) {
			commitScheduler.shutdownNow();
		}
		failure = closeStep(searcherManager::close, failure, "searcher manager");
		failure = closeStep(writer::close, failure, "writer");
		failure = closeStep(config.directory()::close, failure, "directory");
		if (failure != null) {
			throw failure;
		}
	}

	private void wrote(long documents) throws IOException {
		long count = uncommitted.addAndGet(documents);
		if (config.commit().hasDocumentTrigger() && count >= config.commit().maxUncommittedDocuments()) {
			commit();
		}
	}

	private void commitQuietly() {
		try {
			if (!closed.get() && uncommitted.get() > 0) {
				commit();
			}
		} catch (IOException | RuntimeException e) {
			// A scheduled commit that throws must not kill the scheduler: the next write
			// or the close will commit again, and the failure surfaces there.
		}
	}

	private void checkOpen() {
		if (closed.get()) {
			throw new IllegalStateException("Index unit '" + config.name() + "' is closed");
		}
	}

	private interface CloseStep {
		void run() throws IOException;
	}

	private IOException closeStep(CloseStep step, IOException previous, String what) {
		try {
			step.run();
			return previous;
		} catch (IOException | RuntimeException e) {
			IOException failure = previous != null ? previous
					: new IOException("Failed to close the " + what + " of unit " + config.name(), e);
			if (previous != null) {
				previous.addSuppressed(e);
			}
			return failure;
		}
	}

	private static void closeQuietly(AutoCloseable closeable, Exception primary) {
		try {
			closeable.close();
		} catch (Exception e) {
			primary.addSuppressed(e);
		}
	}
}
