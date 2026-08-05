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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 * The analyzers an index unit may use, resolved by name.
 * <p>
 * Deliberately a plain object handed to the unit rather than a lookup the unit performs
 * itself: it is what keeps the core usable — and testable — without a framework. The OSGi
 * layer builds one from the analyzer services it sees and passes it in; a plain-Java
 * caller builds one directly. There is no {@code ServiceLoader} fallback and no global
 * registry, because an implicit lookup is exactly the kind of hidden state that makes two
 * units in one JVM interfere.
 * <p>
 * Instances are immutable. Analyzers appearing or disappearing at runtime therefore means
 * reconfiguring the unit, not mutating the registry underneath a live {@code IndexWriter}
 * — which would silently change how documents are analyzed halfway through an index.
 *
 * @author Data In Motion Consulting
 */
public final class AnalyzerRegistry {

	private final Analyzer defaultAnalyzer;
	private final Map<String, Analyzer> byName;

	private AnalyzerRegistry(Analyzer defaultAnalyzer, Map<String, Analyzer> byName) {
		this.defaultAnalyzer = defaultAnalyzer;
		this.byName = Map.copyOf(byName);
	}

	/** A registry whose default is a {@link StandardAnalyzer} and which knows no named analyzers. */
	public static AnalyzerRegistry standard() {
		return new AnalyzerRegistry(new StandardAnalyzer(), Map.of());
	}

	/** A builder starting from a {@link StandardAnalyzer} default. */
	public static Builder builder() {
		return new Builder();
	}

	/** The analyzer for fields that name none. */
	public Analyzer defaultAnalyzer() {
		return defaultAnalyzer;
	}

	/** The analyzer registered under {@code name}, if any. */
	public Optional<Analyzer> find(String name) {
		return Optional.ofNullable(byName.get(Objects.requireNonNull(name, "name")));
	}

	/**
	 * The analyzer registered under {@code name}.
	 *
	 * @throws IllegalArgumentException if no analyzer is registered under that name; the
	 *         message lists what <em>is</em> registered, because the usual cause is a typo
	 *         in a mapping or an analyzer bundle that is not present
	 */
	public Analyzer require(String name) {
		return find(name).orElseThrow(() -> new IllegalArgumentException(
				"No analyzer named '" + name + "'. Registered: " + names()));
	}

	/** The registered names, without the default. */
	public Set<String> names() {
		return byName.keySet();
	}

	/** Builder for an immutable registry. */
	public static final class Builder {

		private Analyzer defaultAnalyzer = new StandardAnalyzer();
		private final Map<String, Analyzer> byName = new LinkedHashMap<>();

		private Builder() {
		}

		/** Sets the analyzer used by fields that name none. */
		public Builder defaultAnalyzer(Analyzer analyzer) {
			this.defaultAnalyzer = Objects.requireNonNull(analyzer, "analyzer");
			return this;
		}

		/**
		 * Registers an analyzer under a name.
		 *
		 * @throws IllegalArgumentException if the name is already taken — a silent
		 *         overwrite would make the effective analysis depend on registration order
		 */
		public Builder register(String name, Analyzer analyzer) {
			Objects.requireNonNull(name, "name");
			Objects.requireNonNull(analyzer, "analyzer");
			Analyzer previous = byName.putIfAbsent(name, analyzer);
			if (previous != null) {
				throw new IllegalArgumentException("Duplicate analyzer name '" + name + "'");
			}
			return this;
		}

		/** Builds the registry. */
		public AnalyzerRegistry build() {
			return new AnalyzerRegistry(defaultAnalyzer, byName);
		}
	}
}
