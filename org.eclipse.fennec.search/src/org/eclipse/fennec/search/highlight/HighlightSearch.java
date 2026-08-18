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
package org.eclipse.fennec.search.highlight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.persistence.api.ConverterService;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.api.QueryShape;
import org.eclipse.fennec.persistence.query.support.QueryContexts;
import org.eclipse.fennec.search.esearch.FieldMapping;
import org.eclipse.fennec.search.mapping.DocumentReader;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappingException;
import org.eclipse.fennec.search.mapping.SearchFields;
import org.eclipse.fennec.search.query.LuceneQueryPlan;
import org.eclipse.fennec.search.query.LuceneQueryProcessor;
import org.eclipse.fennec.search.unit.IndexUnit;

/**
 * Highlighting over one index unit (S12, #14) — the §6.1 decision, option (a): a
 * search-local result type carrying object, score and passages, from an own entry point.
 * The shared IR stays free of search-only vocabulary, exactly as facets and suggest keep
 * it; unlike suggest this lives in the core bundle, because the {@code UnifiedHighlighter}
 * needs the executed query and the live searcher — the coupling §6.1 names.
 * <p>
 * A field is highlightable when it is analyzed text whose original value is stored (the
 * §4.3 default): the highlighter re-reads the stored value and fragments it around the
 * query's terms. Declared term vectors ({@code TextFieldMapping.termVectors}) make that
 * faster; they are never required.
 *
 * @author Data In Motion Consulting
 */
public final class HighlightSearch {

	/** One hit: the reconstructed object, its score, and a snippet per requested field. */
	public record HighlightedHit(EObject object, double score, Map<String, String> highlights) {

		public HighlightedHit {
			highlights = Map.copyOf(highlights);
		}

		/** The snippet of one field, when the query matched in it. */
		public Optional<String> highlight(String field) {
			return Optional.ofNullable(highlights.get(field));
		}
	}

	private final IndexUnit unit;
	private final IndexSchema schema;
	private final LuceneQueryProcessor processor;
	private final ConverterService converter;

	private HighlightSearch(IndexUnit unit, IndexSchema schema, ConverterService converter) {
		this.unit = unit;
		this.schema = schema;
		this.processor = LuceneQueryProcessor.of(schema, unit.config().analyzers().defaultAnalyzer());
		this.converter = converter;
	}

	/** Highlighting for one unit and its schema. */
	public static HighlightSearch of(IndexUnit unit, IndexSchema schema) {
		return of(unit, schema, null);
	}

	/** As {@link #of(IndexUnit, IndexSchema)}, with a converter for parameter values. */
	public static HighlightSearch of(IndexUnit unit, IndexSchema schema, ConverterService converter) {
		Objects.requireNonNull(unit, "unit");
		Objects.requireNonNull(schema, "schema");
		return new HighlightSearch(unit, schema, converter);
	}

	/**
	 * Runs the request's query and returns its hits in rank order, each with a snippet per
	 * requested field where the query matched in it.
	 *
	 * @throws QueryException if the query is refused, or a requested field is not
	 *         highlightable — refused by name with the way out
	 */
	public List<HighlightedHit> search(HighlightRequest request) throws IOException, QueryException {
		Objects.requireNonNull(request, "request");
		if (request.fields().isEmpty()) {
			throw new QueryException("The request names no field to highlight. Name at least one "
					+ "analyzed text attribute.");
		}
		if (request.query().getFrom() == null) {
			throw new QueryException("The query names no root type.");
		}
		String[] fields = new String[request.fields().size()];
		for (int i = 0; i < fields.length; i++) {
			fields[i] = highlightable(request.query().getFrom(), request.fields().get(i));
		}
		LuceneQueryPlan plan = (LuceneQueryPlan) processor.translate(request.query(),
				QueryContexts.of(request.query().getFrom(), converter, request.parameters(), null));
		if (plan.shape() != QueryShape.OBJECTS) {
			throw new QueryException("Highlighting rides on hits, and a " + plan.shape()
					+ " result has none — a count or a row carries no passages. Drop the "
					+ "projection or pipeline from the query.");
		}
		DocumentReader reader = DocumentReader.of(schema);
		int[] maxPassages = new int[fields.length];
		Arrays.fill(maxPassages, request.passages());
		return unit.search(searcher -> {
			int limit = plan.limit() >= 0 ? plan.limit() : searcher.count(plan.query()) - plan.skip();
			int wanted = plan.skip() + Math.max(0, limit);
			if (wanted == 0) {
				return List.of();
			}
			TopDocs top = plan.sort() != null
					? searcher.search(plan.query(), wanted, plan.sort(), true)
					: searcher.search(plan.query(), wanted);
			int count = top.scoreDocs.length - plan.skip();
			if (count <= 0) {
				return List.of();
			}
			int[] docIds = new int[count];
			for (int i = 0; i < count; i++) {
				docIds[i] = top.scoreDocs[plan.skip() + i].doc;
			}
			// No match in a field means no snippet — never the field's leading text, which
			// is the highlighter's summary fallback, not a highlight.
			UnifiedHighlighter highlighter = UnifiedHighlighter
					.builder(searcher, unit.config().analyzers().defaultAnalyzer())
					.withMaxNoHighlightPassages(0)
					.build();
			Map<String, String[]> snippets = highlighter.highlightFields(fields, plan.query(),
					docIds, maxPassages);
			StoredFields stored = searcher.storedFields();
			List<HighlightedHit> hits = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				Document root = stored.document(docIds[i]);
				EObject object = reader.read(root, childrenOf(searcher, stored, root));
				Map<String, String> highlights = new LinkedHashMap<>();
				for (String field : fields) {
					String snippet = snippets.get(field)[i];
					if (snippet != null) {
						highlights.put(field, snippet);
					}
				}
				hits.add(new HighlightedHit(object, top.scoreDocs[plan.skip() + i].score, highlights));
			}
			return hits;
		});
	}

	/** The child documents of one block, in write order — hits reconstruct like every read. */
	private List<Document> childrenOf(IndexSearcher searcher,
			StoredFields stored, Document root) throws IOException {
		String rootId = root.get(SearchFields.ROOT);
		Query children = new BooleanQuery.Builder()
				.add(new TermQuery(new Term(SearchFields.ROOT, rootId)),
						Occur.FILTER)
				.add(new TermQuery(
						new Term(SearchFields.PARENT, SearchFields.PARENT_VALUE)), Occur.MUST_NOT)
				.build();
		int count = searcher.count(children);
		if (count == 0) {
			return List.of();
		}
		TopDocs top = searcher.search(children, count, new Sort(SortField.FIELD_DOC));
		List<Document> block = new ArrayList<>(count);
		for (ScoreDoc hit : top.scoreDocs) {
			block.add(stored.document(hit.doc));
		}
		return block;
	}

	/** The effective field name of a highlightable attribute — refused by name otherwise. */
	private String highlightable(EClass root, EAttribute attribute)
			throws QueryException {
		IndexSchema.Field field;
		try {
			field = schema.resolve(root, attribute);
		} catch (MappingException e) {
			throw new QueryException(e.getMessage(), e);
		}
		if (field.kind() != IndexSchema.FieldKind.TEXT) {
			throw new QueryException("Field '" + field.name() + "' is not analyzed text — a keyword or "
					+ "point carries no token positions to fragment around. Highlighting reads analyzed "
					+ "text fields.");
		}
		FieldMapping declared = schema.fieldMapping(root, attribute);
		if (declared != null && !declared.isStored()) {
			throw new QueryException("Field '" + field.name() + "' is mapped stored=false, and the "
					+ "highlighter fragments the stored value. Remove the opt-out to highlight it.");
		}
		return field.name();
	}
}
