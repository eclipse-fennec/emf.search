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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * One index unit: one Lucene directory with the writer and searcher sides its configuration
 * asks for.
 * <p>
 * The unit owns everything it opens and closes it again — which is also why writes go
 * through the unit rather than through a handed-out writer: it is the only way a
 * document-count commit trigger can be honoured, and it keeps the writer's lifecycle from
 * escaping.
 * <p>
 * Three configured axes decide the shape of what is opened:
 * <ul>
 * <li>{@link AccessMode} — {@code BULK_LOAD} opens no searcher at all, {@code READ_ONLY}
 * refuses every write;</li>
 * <li>{@link Visibility} — {@code NRT} builds the searcher from the writer and therefore
 * sees uncommitted writes, {@code COMMITTED} builds it from the directory and sees exactly
 * what has been committed;</li>
 * <li>{@link RefreshTrigger} — background thread, on commit, or only on demand.</li>
 * </ul>
 * A writer is opened in every mode, including {@code READ_ONLY}: one code path is easier to
 * trust than two, and a near-real-time searcher needs the writer anyway. The price is that
 * a read-only unit still takes the directory's {@code write.lock}, which is called out
 * where it bites rather than left to be discovered.
 * <p>
 * No static state and no framework: several units run in one JVM without seeing each other,
 * which is both the OSGi requirement and what makes the class testable with plain JUnit.
 * Writes and searches may run concurrently; a searcher acquired through
 * {@link #search(SearchFunction)} is a stable snapshot for the duration of the call.
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
	private final Directory directory;
	private final IndexWriter writer;
	private final SearcherManager searcherManager;
	private final ControlledRealTimeReopenThread<IndexSearcher> reopenThread;
	private final ScheduledExecutorService scheduler;
	private final AtomicLong uncommitted = new AtomicLong();
	private final AtomicBoolean closed = new AtomicBoolean();

	private IndexUnit(IndexUnitConfig config, Directory directory, IndexWriter writer,
			SearcherManager searcherManager, ControlledRealTimeReopenThread<IndexSearcher> reopenThread,
			ScheduledExecutorService scheduler) {
		this.config = config;
		this.directory = directory;
		this.writer = writer;
		this.searcherManager = searcherManager;
		this.reopenThread = reopenThread;
		this.scheduler = scheduler;
	}

	/**
	 * Opens the unit described by {@code config}, creating the index if the directory is
	 * empty and appending to it otherwise.
	 *
	 * @throws IOException if the index cannot be opened; a failure to take the write lock
	 *         is reported with what it means for this access mode rather than as a bare
	 *         Lucene exception
	 */
	public static IndexUnit open(IndexUnitConfig config) throws IOException {
		Objects.requireNonNull(config, "config");

		Directory directory = config.location().open();
		IndexWriter writer = null;
		SearcherManager searcherManager = null;
		ControlledRealTimeReopenThread<IndexSearcher> reopenThread = null;
		ScheduledExecutorService scheduler = null;
		try {
			writer = openWriter(config, directory);
			if (config.access().allowsSearch()) {
				searcherManager = openSearcherManager(config, directory, writer);
				reopenThread = startReopenThread(config, writer, searcherManager);
				if (needsRefreshScheduler(config)) {
					scheduler = newScheduler(config, "refresh");
				}
			}
			if (config.commit().hasIntervalTrigger() && scheduler == null) {
				scheduler = newScheduler(config, "commit");
			}
		} catch (IOException | RuntimeException e) {
			closeQuietly(searcherManager, e);
			closeQuietly(writer, e);
			if (config.location().ownsDirectory()) {
				closeQuietly(directory, e);
			}
			throw e;
		}

		IndexUnit unit = new IndexUnit(config, directory, writer, searcherManager, reopenThread, scheduler);
		unit.scheduleBackgroundWork();
		return unit;
	}

	private static IndexWriter openWriter(IndexUnitConfig config, Directory directory) throws IOException {
		IndexWriterConfig writerConfig = new IndexWriterConfig(config.analyzers().defaultAnalyzer())
				.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)
				.setCommitOnClose(config.access().allowsWrites() && config.commit().commitOnClose());
		if (config.indexSort() != null) {
			writerConfig.setIndexSort(config.indexSort());
		}
		try {
			return new IndexWriter(directory, writerConfig);
		} catch (LockObtainFailedException e) {
			throw new IOException("Cannot open index unit '" + config.name() + "' at "
					+ config.location().describe() + ": the write lock is held elsewhere. Note that "
					+ config.access() + " opens a writer too — a unit that only reads still needs the lock, "
					+ "so it cannot share a directory with another writer.", e);
		}
	}

	private static SearcherManager openSearcherManager(IndexUnitConfig config, Directory directory,
			IndexWriter writer) throws IOException {
		if (config.visibility() == Visibility.NRT) {
			// applyAllDeletes/writeAllDeletes: a unit is a store, so a delete has to be
			// visible on the next refresh rather than at some later merge.
			return new SearcherManager(writer, true, true, null);
		}
		// A directory-based searcher needs a commit to open on; a brand-new index has none
		// yet, so create the initial commit point rather than failing on an empty directory.
		if (!DirectoryReader.indexExists(directory)) {
			writer.commit();
		}
		return new SearcherManager(directory, null);
	}

	private static ControlledRealTimeReopenThread<IndexSearcher> startReopenThread(IndexUnitConfig config,
			IndexWriter writer, SearcherManager searcherManager) {
		// The controlled reopen thread tracks the writer's generation, so it is only the
		// right tool for a near-real-time searcher. A committed-visibility searcher is
		// reopened by the scheduler instead — see scheduleBackgroundWork().
		if (config.refresh().mode() != RefreshTrigger.Mode.BACKGROUND || config.visibility() != Visibility.NRT) {
			return null;
		}
		double staleSeconds = config.refresh().interval().toNanos() / 1_000_000_000.0;
		ControlledRealTimeReopenThread<IndexSearcher> thread = new ControlledRealTimeReopenThread<>(writer,
				searcherManager, staleSeconds, staleSeconds);
		thread.setName("fennec-search-reopen-" + config.name());
		thread.setDaemon(true);
		thread.start();
		return thread;
	}

	private static boolean needsRefreshScheduler(IndexUnitConfig config) {
		return config.refresh().mode() == RefreshTrigger.Mode.BACKGROUND
				&& config.visibility() == Visibility.COMMITTED;
	}

	private static ScheduledExecutorService newScheduler(IndexUnitConfig config, String purpose) {
		return Executors.newSingleThreadScheduledExecutor(runnable -> {
			Thread thread = new Thread(runnable, "fennec-search-" + purpose + "-" + config.name());
			thread.setDaemon(true);
			return thread;
		});
	}

	private void scheduleBackgroundWork() {
		if (scheduler == null) {
			return;
		}
		if (needsRefreshScheduler(config)) {
			long millis = config.refresh().interval().toMillis();
			scheduler.scheduleWithFixedDelay(this::refreshQuietly, millis, millis, TimeUnit.MILLISECONDS);
		}
		if (config.commit().hasIntervalTrigger()) {
			long millis = config.commit().maxInterval().toMillis();
			scheduler.scheduleWithFixedDelay(this::commitQuietly, millis, millis, TimeUnit.MILLISECONDS);
		}
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
		checkWritable();
		writer.addDocument(document);
		wrote(1);
	}

	/** Replaces the documents matching {@code id} with {@code document}, or adds it if none match. */
	public void updateDocument(Term id, Iterable<? extends IndexableField> document) throws IOException {
		checkWritable();
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
		checkWritable();
		Objects.requireNonNull(documents, "documents");
		if (documents.isEmpty()) {
			throw new IllegalArgumentException("A block must contain at least the parent document");
		}
		writer.updateDocuments(id, documents);
		wrote(documents.size());
	}

	/** Deletes every document matching any of the terms. */
	public void deleteDocuments(Term... terms) throws IOException {
		checkWritable();
		writer.deleteDocuments(terms);
		wrote(terms.length);
	}

	/** Deletes everything in the unit. */
	public void deleteAll() throws IOException {
		checkWritable();
		writer.deleteAll();
		wrote(1);
	}

	/**
	 * Runs {@code function} against a searcher and releases it afterwards, also when the
	 * function throws — the release is the part callers forget, so the unit does it.
	 *
	 * @throws IllegalStateException if the unit is closed, or opened for
	 *         {@link AccessMode#BULK_LOAD} and therefore has no searcher
	 */
	public <T> T search(SearchFunction<T> function) throws IOException {
		checkOpen();
		if (!config.access().allowsSearch()) {
			throw new IllegalStateException("Index unit '" + config.name() + "' is opened for "
					+ config.access() + " and has no searcher. Reopen it in another access mode to search.");
		}
		Objects.requireNonNull(function, "function");
		IndexSearcher searcher = searcherManager.acquire();
		try {
			return function.apply(searcher);
		} finally {
			searcherManager.release(searcher);
		}
	}

	/**
	 * Reopens the searcher and blocks until it is done. What becomes visible depends on
	 * {@link Visibility}: everything written so far under {@code NRT}, everything
	 * committed so far under {@code COMMITTED}.
	 */
	public void refresh() throws IOException {
		checkOpen();
		if (!config.access().allowsSearch()) {
			return;
		}
		searcherManager.maybeRefreshBlocking();
	}

	/**
	 * Commits, resets the uncommitted-document count and, under
	 * {@link RefreshTrigger.Mode#ON_COMMIT}, reopens the searcher.
	 *
	 * @return the commit sequence number, or {@code -1} if there was nothing to commit.
	 *         The first commit of a fresh index is never a no-op: it writes the initial
	 *         commit point.
	 */
	public long commit() throws IOException {
		checkWritable();
		long sequenceNumber = writer.commit();
		uncommitted.set(0);
		if (config.refresh().mode() == RefreshTrigger.Mode.ON_COMMIT && config.access().allowsSearch()) {
			searcherManager.maybeRefreshBlocking();
		}
		return sequenceNumber;
	}

	/**
	 * Commits, carrying a checkpoint into the same commit point (S18, #20).
	 * <p>
	 * Atomic in the way that matters for resuming: the documents of this commit and the
	 * position they were derived from become durable together, so a reader of the commit
	 * can never see the one without the other.
	 *
	 * @param checkpoint what the feed needs to resume — a stream offset, a change-log
	 *        position, a source timestamp; keys and values are the caller's vocabulary
	 * @return the commit sequence number
	 */
	public long commit(Map<String, String> checkpoint) throws IOException {
		checkpoint(checkpoint);
		return commit();
	}

	/**
	 * Stages the checkpoint that the <em>next</em> commit will carry.
	 * <p>
	 * Nothing becomes durable here — that is {@link #commit()}, and the two are separate so
	 * that a feed can record its position as it goes and let the unit's own commit policy
	 * (interval, document count, close) decide when to write it. Staging replaces: the last
	 * value set before a commit is the one that lands.
	 */
	public void checkpoint(Map<String, String> checkpoint) throws IOException {
		checkWritable();
		Objects.requireNonNull(checkpoint, "checkpoint");
		List<Map.Entry<String, String>> entries = new ArrayList<>(checkpoint.size());
		for (Map.Entry<String, String> entry : checkpoint.entrySet()) {
			if (entry.getKey() == null || entry.getValue() == null) {
				throw new IllegalArgumentException("A checkpoint of unit '" + config.name()
						+ "' carries a null " + (entry.getKey() == null ? "key" : "value")
						+ "; Lucene commit data is string to string.");
			}
			entries.add(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue()));
		}
		// Lucene applies live commit data at the next commit and counts setting it as a
		// change, so a checkpoint alone is committable — which is what lets a feed record
		// "I read up to here and it produced nothing".
		writer.setLiveCommitData(entries);
	}

	/**
	 * The checkpoint of the newest commit in the directory — what a restart resumes from.
	 * <p>
	 * Read from the directory rather than from the writer on purpose: this is the question
	 * "what did the index actually apply", and after a crash the answer is whatever reached
	 * disk, not whatever was staged. It is therefore also answerable on a
	 * {@link AccessMode#READ_ONLY} unit, which is how an operator inspects a suspect index.
	 *
	 * @return the committed checkpoint, empty when the index carries none (including a
	 *         directory that has no commit yet)
	 */
	public Map<String, String> checkpoint() throws IOException {
		checkOpen();
		try {
			Map<String, String> data = SegmentInfos.readLatestCommit(directory).getUserData();
			return data == null ? Map.of() : Map.copyOf(data);
		} catch (IndexNotFoundException e) {
			// A directory nothing has committed to yet has no checkpoint, which is an
			// answer ("resume from the beginning"), not a failure.
			return Map.of();
		}
	}

	/**
	 * The checkpoint staged for the next commit, which may differ from {@link #checkpoint()}
	 * while writes are pending.
	 */
	public Map<String, String> pendingCheckpoint() {
		checkOpen();
		Iterable<Map.Entry<String, String>> live = writer.getLiveCommitData();
		if (live == null) {
			return Map.of();
		}
		Map<String, String> staged = new LinkedHashMap<>();
		live.forEach(entry -> staged.put(entry.getKey(), entry.getValue()));
		return Map.copyOf(staged);
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
	 * Closes reopen thread, scheduler, searcher manager and writer in that order, and the
	 * directory too when the unit opened it — a directory handed in through
	 * {@link IndexLocation#directory(Directory)} belongs to the caller. Idempotent.
	 * Whether pending writes survive is
	 * {@link CommitPolicy#commitOnClose()} — and never, in {@link AccessMode#READ_ONLY}.
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
		if (scheduler != null) {
			scheduler.shutdownNow();
		}
		if (searcherManager != null) {
			failure = closeStep(searcherManager::close, failure, "searcher manager");
		}
		failure = closeStep(writer::close, failure, "writer");
		if (config.location().ownsDirectory()) {
			failure = closeStep(directory::close, failure, "directory");
		}
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
			// or the close commits again, and the failure surfaces there.
		}
	}

	private void refreshQuietly() {
		try {
			if (!closed.get()) {
				searcherManager.maybeRefresh();
			}
		} catch (IOException | RuntimeException e) {
			// Same reasoning as commitQuietly: a failed reopen must not stop the schedule.
		}
	}

	private void checkOpen() {
		if (closed.get()) {
			throw new IllegalStateException("Index unit '" + config.name() + "' is closed");
		}
	}

	private void checkWritable() {
		checkOpen();
		if (!config.access().allowsWrites()) {
			throw new IllegalStateException(
					"Index unit '" + config.name() + "' is opened " + config.access() + " and refuses writes");
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
			if (previous != null) {
				previous.addSuppressed(e);
				return previous;
			}
			return new IOException("Failed to close the " + what + " of unit " + config.name(), e);
		}
	}

	private static void closeQuietly(AutoCloseable closeable, Exception primary) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (Exception e) {
			primary.addSuppressed(e);
		}
	}
}
