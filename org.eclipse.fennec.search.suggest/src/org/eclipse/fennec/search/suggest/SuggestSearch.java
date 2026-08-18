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
package org.eclipse.fennec.search.suggest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.suggest.DocumentDictionary;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.suggest.analyzing.FreeTextSuggester;
import org.apache.lucene.search.suggest.analyzing.FuzzySuggester;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.SuggestSource;
import org.eclipse.fennec.search.esearch.SuggesterKind;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappingException;
import org.eclipse.fennec.search.unit.IndexUnit;

/**
 * Suggest over one index unit (S8, #12) — an own API in the §6 family, next to
 * {@code FacetSearch}: completion is not query vocabulary, so it does not pretend to be.
 * <p>
 * The predecessor's mistake is corrected structurally, not by care: the old stack kept a
 * <em>parallel</em> suggest index (own directory, own writer, commit per entry, weights
 * hardcoded). Here a suggester is <b>derived from the unit's own documents</b> — a
 * {@link DocumentDictionary} over the same searcher every query uses reads the declared
 * source field's stored values and, when declared, the weight attribute's doc values.
 * One mapping, one index, one lifecycle.
 * <p>
 * The price, stated: the FST suggesters are <b>snapshots</b>. A lookup answers from the
 * last {@link #rebuild()} (the first lookup builds lazily), not from NRT — rebuilding on
 * every write would be the old stack's commit-per-entry amplification wearing a new hat.
 * Callers own the cadence: rebuild after bulk loads, on commit, or on a schedule.
 * <p>
 * What is refused, each by name: {@code COMPLETION} (index-time suggest fields change the
 * document shape — a follow-up, and the kind the metamodel ties filter contexts to),
 * {@code contexts} (they arrive with COMPLETION), sources over unstored fields, weights
 * without doc values.
 *
 * @author Data In Motion Consulting
 */
public final class SuggestSearch {

	/** One suggestion: the completed text and its rank weight. */
	public record Suggestion(String text, long weight) {
	}

	private record Source(SuggestSource declared, String field, String weightField, Analyzer analyzer) {
	}

	private final IndexUnit unit;
	private final Map<String, Source> sources;
	private final Object buildLock = new Object();
	private volatile Map<String, Lookup> lookups;

	private SuggestSearch(IndexUnit unit, Map<String, Source> sources) {
		this.unit = unit;
		this.sources = sources;
	}

	/**
	 * Suggest for one unit and its schema.
	 *
	 * @throws MappingException if the mapping declares no suggest source at all, or a
	 *         declared source asks for something this backend refuses — at resolution
	 *         time, not on the first lookup
	 */
	public static SuggestSearch of(IndexUnit unit, IndexSchema schema) {
		Objects.requireNonNull(unit, "unit");
		Objects.requireNonNull(schema, "schema");
		Map<String, Source> sources = new LinkedHashMap<>();
		for (DocumentMapping document : schema.mapping().getDocuments()) {
			for (SuggestSource declared : document.getSuggestions()) {
				sources.put(validateName(declared, sources, unit),
						resolve(declared, document.getEClass(), schema, unit));
			}
		}
		if (sources.isEmpty()) {
			throw new MappingException("Unit '" + unit.name() + "' declares no suggest source, so there "
					+ "is nothing this API could complete. Declare a SuggestSource on a document first.");
		}
		return new SuggestSearch(unit, sources);
	}

	/**
	 * The top completions of one source for the given input, rank-descending.
	 *
	 * @throws MappingException if no source of that name is declared
	 */
	public List<Suggestion> suggest(String source, String input, int max) throws IOException {
		Objects.requireNonNull(input, "input");
		if (max < 1) {
			throw new IllegalArgumentException("max must be at least 1, was " + max);
		}
		Source declared = sources.get(source);
		if (declared == null) {
			throw new MappingException("No suggest source '" + source + "' is declared; declared: "
					+ sources.keySet() + ".");
		}
		Map<String, Lookup> current = lookups;
		if (current == null) {
			current = rebuild();
		}
		List<LookupResult> results = current.get(source).lookup(input, false, max);
		List<Suggestion> suggestions = new ArrayList<>(results.size());
		for (LookupResult result : results) {
			suggestions.add(new Suggestion(result.key.toString(), result.value));
		}
		return suggestions;
	}

	/**
	 * Rebuilds every source from the unit's current searcher and swaps atomically —
	 * lookups in flight keep answering from the previous snapshot, never from a
	 * half-built one (the old stack's unguarded destructive build, done right).
	 */
	public Map<String, Lookup> rebuild() throws IOException {
		synchronized (buildLock) {
			Map<String, Lookup> fresh = new LinkedHashMap<>();
			for (Source source : sources.values()) {
				fresh.put(source.declared().getName(), build(source));
			}
			this.lookups = Map.copyOf(fresh);
			return this.lookups;
		}
	}

	private Lookup build(Source source) throws IOException {
		Lookup lookup = newLookup(source);
		unit.search(searcher -> {
			DocumentDictionary dictionary = source.weightField() == null
					? new DocumentDictionary(searcher.getIndexReader(), source.field(), null)
					: new DocumentDictionary(searcher.getIndexReader(), source.field(),
							source.weightField());
			lookup.build(dictionary);
			return null;
		});
		return lookup;
	}

	private Lookup newLookup(Source source) throws IOException {
		Analyzer analyzer = source.analyzer();
		return switch (source.declared().getKind()) {
			case ANALYZING -> new AnalyzingSuggester(new ByteBuffersDirectory(), "suggest", analyzer);
			case FUZZY -> new FuzzySuggester(new ByteBuffersDirectory(), "suggest", analyzer);
			case FREE_TEXT -> new FreeTextSuggester(analyzer);
			case COMPLETION -> throw new MappingException("unreachable: COMPLETION is refused at resolution");
		};
	}

	// --- resolution -----------------------------------------------------------------------

	private static String validateName(SuggestSource declared, Map<String, Source> sources, IndexUnit unit) {
		String name = declared.getName();
		if (name == null || name.isBlank()) {
			throw new MappingException("A suggest source of unit '" + unit.name() + "' declares no name — "
					+ "the name is what a lookup selects the source by.");
		}
		if (sources.containsKey(name)) {
			throw new MappingException("Unit '" + unit.name() + "' declares the suggest source '" + name
					+ "' twice.");
		}
		return name;
	}

	private static Source resolve(SuggestSource declared, EClass owner, IndexSchema schema, IndexUnit unit) {
		String name = declared.getName();
		if (declared.getKind() == SuggesterKind.COMPLETION) {
			throw new MappingException("Suggest source '" + name + "' declares COMPLETION, which needs "
					+ "index-time suggest fields — a document-shape change this backend does not make "
					+ "yet (#12 follow-up). ANALYZING, FUZZY and FREE_TEXT build from the documents as "
					+ "they are.");
		}
		if (!declared.getContexts().isEmpty()) {
			throw new MappingException("Suggest source '" + name + "' declares contexts, which arrive "
					+ "with COMPLETION's filterable suggest fields (#12 follow-up). Until then a source "
					+ "per context value is the honest workaround.");
		}
		EAttribute feature = declared.getFeature();
		if (feature == null) {
			throw new MappingException("Suggest source '" + name + "' declares no feature — there is "
					+ "nothing to complete from.");
		}
		IndexSchema.Field field = schema.resolve(owner, feature);
		if (!isStored(schema, owner, feature)) {
			throw new MappingException("Suggest source '" + name + "' reads '" + field.name() + "', which "
					+ "the mapping does not store — the suggester is built from stored values. Remove "
					+ "stored=false for '" + feature.getName() + "'.");
		}
		String weightField = null;
		if (declared.getWeight() != null) {
			IndexSchema.Field weight = schema.resolve(owner, declared.getWeight());
			if (weight.kind() != IndexSchema.FieldKind.NUMERIC || !weight.docValues()
					|| declared.getWeight().isMany()) {
				throw new MappingException("Suggest source '" + name + "' weights by '" + weight.name()
						+ "', which must be a single-valued numeric field with doc values — the weight is "
						+ "read per document at build time.");
			}
			weightField = weight.name();
		}
		Analyzer analyzer = declared.getAnalyzer() == null
				? unit.config().analyzers().defaultAnalyzer()
				: unit.config().analyzers().require(declared.getAnalyzer().getName());
		return new Source(declared, field.name(), weightField, analyzer);
	}

	private static boolean isStored(IndexSchema schema, EClass owner, EAttribute attribute) {
		var declared = schema.fieldMapping(owner, attribute);
		return declared == null || declared.isStored();
	}
}
