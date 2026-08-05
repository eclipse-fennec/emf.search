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

import java.nio.file.Path;
import java.util.Objects;

import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;

/**
 * Everything an {@link IndexUnit} needs to open, as one plain value.
 * <p>
 * This is the runtime half of a unit's configuration: where the index lives, what may be
 * done with it, what a searcher sees, when it is reopened and when the writer commits. The
 * declarative half — which EClass becomes which document, which attribute becomes which
 * field — is the {@code esearch} mapping model and is deliberately not repeated here.
 * <p>
 * Three axes are kept apart on purpose, because collapsing them is how index
 * configurations end up promising something they do not do:
 * <ul>
 * <li>{@link AccessMode} — may this unit write, and does it open a searcher at all;</li>
 * <li>{@link Visibility} — can a searcher see uncommitted writes, or only commits;</li>
 * <li>{@link RefreshTrigger} — when is the searcher reopened.</li>
 * </ul>
 *
 * @param name       the unit name — the alias a consumer selects
 * @param location   where the index lives
 * @param analyzers  the analyzers this unit may use
 * @param access     what the unit may do with the index
 * @param visibility what a searcher of this unit sees
 * @param refresh    when the searcher is reopened
 * @param commit     when the writer commits
 * @param indexSort  optional physical index order enabling early termination, or {@code null};
 *                   fixed at index creation, so changing it later requires a rebuild
 * @author Data In Motion Consulting
 */
public record IndexUnitConfig(String name, IndexLocation location, AnalyzerRegistry analyzers,
		AccessMode access, Visibility visibility, RefreshTrigger refresh, CommitPolicy commit,
		Sort indexSort) {

	public IndexUnitConfig {
		Objects.requireNonNull(name, "name");
		if (name.isBlank()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		Objects.requireNonNull(location, "location");
		Objects.requireNonNull(analyzers, "analyzers");
		Objects.requireNonNull(access, "access");
		Objects.requireNonNull(visibility, "visibility");
		Objects.requireNonNull(refresh, "refresh");
		Objects.requireNonNull(commit, "commit");
		if (access == AccessMode.BULK_LOAD && refresh.mode() != RefreshTrigger.Mode.MANUAL) {
			throw new IllegalArgumentException(
					"BULK_LOAD opens no searcher, so a refresh trigger other than MANUAL cannot be honoured");
		}
	}

	/** A builder with the defaults: read/write, near-real-time, background refresh, commit on close. */
	public static Builder builder(String name, IndexLocation location) {
		return new Builder(name, location);
	}

	/** Shorthand for a unit on a file-system directory. */
	public static Builder builder(String name, Path path) {
		return new Builder(name, IndexLocation.path(path));
	}

	/** Shorthand for a unit on an already-open directory. */
	public static Builder builder(String name, Directory directory) {
		return new Builder(name, IndexLocation.directory(directory));
	}

	/** Shorthand for an in-memory unit; nothing survives {@link IndexUnit#close()}. */
	public static Builder inMemory(String name) {
		return new Builder(name, IndexLocation.inMemory());
	}

	/** Builder for {@link IndexUnitConfig}. */
	public static final class Builder {

		private final String name;
		private final IndexLocation location;
		private AnalyzerRegistry analyzers = AnalyzerRegistry.standard();
		private AccessMode access = AccessMode.READ_WRITE;
		private Visibility visibility = Visibility.NRT;
		private RefreshTrigger refresh = RefreshTrigger.background();
		private CommitPolicy commit = CommitPolicy.onClose();
		private Sort indexSort;

		private Builder(String name, IndexLocation location) {
			this.name = name;
			this.location = location;
		}

		public Builder analyzers(AnalyzerRegistry analyzers) {
			this.analyzers = Objects.requireNonNull(analyzers, "analyzers");
			return this;
		}

		public Builder access(AccessMode access) {
			this.access = Objects.requireNonNull(access, "access");
			return this;
		}

		public Builder visibility(Visibility visibility) {
			this.visibility = Objects.requireNonNull(visibility, "visibility");
			return this;
		}

		public Builder refresh(RefreshTrigger refresh) {
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
			return new IndexUnitConfig(name, location, analyzers, access, visibility, refresh, commit,
					indexSort);
		}
	}
}
