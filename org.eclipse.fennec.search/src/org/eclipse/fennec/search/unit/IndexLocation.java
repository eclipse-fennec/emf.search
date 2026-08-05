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
import java.nio.file.Path;
import java.util.Objects;

import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Where an index lives, expressed as data rather than as an open resource.
 * <p>
 * This matters beyond tidiness: a Configuration Admin configuration can carry a kind and a
 * path, never a live {@link Directory} object. Keeping the location declarative is what
 * lets the same {@link IndexUnitConfig} be built from a factory configuration in OSGi and
 * from code in plain Java, instead of the two paths needing different shapes.
 *
 * @author Data In Motion Consulting
 */
public sealed interface IndexLocation {

	/** Opens the directory. The caller — normally {@link IndexUnit} — owns and closes it. */
	Directory open() throws IOException;

	/** A human-readable description for diagnostics. */
	String describe();

	/**
	 * Whether the unit closes this directory when it closes.
	 * <p>
	 * You close what you opened: a location that creates its own directory hands ownership
	 * to the unit, while a directory passed in from outside stays the caller's — closing
	 * it would break the next unit that uses it, which is exactly what a test caught.
	 */
	default boolean ownsDirectory() {
		return true;
	}

	/** An index held in memory; nothing survives the unit. */
	static IndexLocation inMemory() {
		return new InMemory();
	}

	/** An index in a file-system directory, created if it does not exist. */
	static IndexLocation path(Path path) {
		return new FileSystem(Objects.requireNonNull(path, "path"));
	}

	/**
	 * An already-open directory. For tests and for embedding into an application that
	 * manages its own storage; cannot come from a factory configuration.
	 */
	static IndexLocation directory(Directory directory) {
		return new Provided(Objects.requireNonNull(directory, "directory"));
	}

	/** @see IndexLocation#inMemory() */
	record InMemory() implements IndexLocation {
		@Override
		public Directory open() {
			return new ByteBuffersDirectory();
		}

		@Override
		public String describe() {
			return "in-memory";
		}
	}

	/** @see IndexLocation#path(Path) */
	record FileSystem(Path path) implements IndexLocation {
		@Override
		public Directory open() throws IOException {
			return FSDirectory.open(path);
		}

		@Override
		public String describe() {
			return path.toString();
		}
	}

	/** @see IndexLocation#directory(Directory) */
	record Provided(Directory directory) implements IndexLocation {
		@Override
		public Directory open() {
			return directory;
		}

		@Override
		public String describe() {
			return directory.toString();
		}

		@Override
		public boolean ownsDirectory() {
			return false;
		}
	}
}
