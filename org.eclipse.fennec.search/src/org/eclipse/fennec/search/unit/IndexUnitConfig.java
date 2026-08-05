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
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.lucene.search.Sort;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Everything an {@link IndexUnit} needs to open, as one plain value.
 * <p>
 * This is the runtime half of a unit's configuration — where the index lives, which
 * analyzers it may use, how it refreshes and commits. The declarative half (which EClass
 * becomes which document, which attribute becomes which field) is the {@code esearch}
 * mapping model and is deliberately not repeated here.
 * <p>
 * In OSGi a Configuration Admin factory configuration is mapped onto this record; in plain
 * Java it is built directly. The record is the single shape the unit understands, so
 * neither path is privileged.
 *
 * @param name      the unit name — the alias a consumer selects
 * @param directory where the index lives; the unit takes ownership and closes it
 * @param analyzers the analyzers this unit may use
 * @param refresh   when writes become visible
 * @param commit    when the writer commits
 * @param indexSort optional physical index order enabling early termination, or {@code null};
 *                  fixed at index creation, so changing it later requires a rebuild
 * @author Data In Motion Consulting
 */
public record IndexUnitConfig(String name, Directory directory, AnalyzerRegistry analyzers,
		RefreshPolicy refresh, CommitPolicy commit, Sort indexSort) {

	public IndexUnitConfig {
		Objects.requireNonNull(name, "name");
		if (name.isBlank()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		Objects.requireNonNull(directory, "directory");
		Objects.requireNonNull(analyzers, "analyzers");
		Objects.requireNonNull(refresh, "refresh");
		Objects.requireNonNull(commit, "commit");
	}

	/** A builder for a unit on the given directory. */
	public static Builder builder(String name, Directory directory) {
		return new Builder(name, directory);
	}

	/** A builder for a unit on a file-system directory. */
	public static Builder builder(String name, Path path) {
		try {
			return new Builder(name, FSDirectory.open(Objects.requireNonNull(path, "path")));
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot open index directory " + path, e);
		}
	}

	/**
	 * A builder for an in-memory unit. Useful for tests and for indexes that are rebuilt
	 * on every start; nothing survives {@link IndexUnit#close()}.
	 */
	public static Builder inMemory(String name) {
		return new Builder(name, new ByteBuffersDirectory());
	}

	/** Builder with the defaults: standard analyzer, near-real-time refresh, commit on close. */
	public static final class Builder {

		private final String name;
		private final Directory directory;
		private AnalyzerRegistry analyzers = AnalyzerRegistry.standard();
		private RefreshPolicy refresh = RefreshPolicy.nearRealTime();
		private CommitPolicy commit = CommitPolicy.onClose();
		private Sort indexSort;

		private Builder(String name, Directory directory) {
			this.name = name;
			this.directory = directory;
		}

		public Builder analyzers(AnalyzerRegistry analyzers) {
			this.analyzers = Objects.requireNonNull(analyzers, "analyzers");
			return this;
		}

		public Builder refresh(RefreshPolicy refresh) {
			this.refresh = Objects.requireNonNull(refresh, "refresh");
			return this;
		}

		public Builder commit(CommitPolicy commit) {
			this.commit = Objects.requireNonNull(commit, "commit");
			return this;
		}

		/** Sets the physical index order. Fixed at index creation. */
		public Builder indexSort(Sort indexSort) {
			this.indexSort = indexSort;
			return this;
		}

		public IndexUnitConfig build() {
			return new IndexUnitConfig(name, directory, analyzers, refresh, commit, indexSort);
		}
	}
}
