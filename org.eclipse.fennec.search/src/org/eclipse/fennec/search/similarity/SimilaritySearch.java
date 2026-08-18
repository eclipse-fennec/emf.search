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
package org.eclipse.fennec.search.similarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.search.esearch.FieldMapping;
import org.eclipse.fennec.search.esearch.TextFieldMapping;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.DocumentReader;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappingException;
import org.eclipse.fennec.search.mapping.SearchFields;
import org.eclipse.fennec.search.unit.IndexUnit;

/**
 * "Objects similar to this one" over one index unit (S13, #15) — {@code MoreLikeThis}
 * over the term statistics of the already-indexed corpus, the honest baseline that makes
 * wave-2 KNN measurable (§6.2). A search-local API for the same reason as highlighting:
 * engine-specific machinery stays here, the shared IR grows no search-only vocabulary.
 * <p>
 * The anchor must be indexed — similarity is a statement about the corpus's term
 * statistics, and an object the index has never seen has none. Fields feeding the
 * statistics are analyzed text whose terms are recoverable: declared term vectors
 * ({@code TextFieldMapping.termVectors}) are read directly, otherwise the stored value is
 * re-analyzed. Hits are objects of the anchor's type (subtypes included), never the
 * anchor itself, best match first.
 *
 * @author Data In Motion Consulting
 */
public final class SimilaritySearch {

	/** One neighbour: the reconstructed object and its similarity score. */
	public record SimilarHit(EObject object, double score) {
	}

	private final IndexUnit unit;
	private final IndexSchema schema;
	private final DocumentMapper mapper;

	private SimilaritySearch(IndexUnit unit, IndexSchema schema) {
		this.unit = unit;
		this.schema = schema;
		this.mapper = DocumentMapper.of(schema);
	}

	/** Similarity for one unit and its schema. */
	public static SimilaritySearch of(IndexUnit unit, IndexSchema schema) {
		Objects.requireNonNull(unit, "unit");
		Objects.requireNonNull(schema, "schema");
		return new SimilaritySearch(unit, schema);
	}

	/**
	 * Returns the anchor's nearest neighbours by term statistics, best match first. An
	 * anchor whose terms are all below the frequency thresholds has no neighbours — an
	 * empty list, not an error.
	 *
	 * @throws QueryException if the anchor is not indexed, or a requested field cannot
	 *         feed term statistics — refused by name with the way out
	 */
	public List<SimilarHit> search(SimilarityRequest request) throws IOException, QueryException {
		Objects.requireNonNull(request, "request");
		if (request.fields().isEmpty()) {
			throw new QueryException("The request names no field. Similarity needs at least one "
					+ "analyzed text attribute whose terms define what similar means.");
		}
		EClass type = request.anchor().eClass();
		String[] fields = new String[request.fields().size()];
		for (int i = 0; i < fields.length; i++) {
			fields[i] = statisticsSource(type, request.fields().get(i));
		}
		String anchorId;
		try {
			anchorId = mapper.documentId(request.anchor());
		} catch (MappingException e) {
			throw new QueryException(e.getMessage(), e);
		}
		Query sameType;
		try {
			sameType = schema.typeFilter(type);
		} catch (MappingException e) {
			throw new QueryException(e.getMessage(), e);
		}
		DocumentReader reader = DocumentReader.of(schema);
		List<SimilarHit> hits = unit.search(searcher -> {
			TopDocs anchor = searcher.search(new BooleanQuery.Builder()
					.add(new TermQuery(new Term(SearchFields.ID, anchorId)), Occur.FILTER)
					.add(sameType, Occur.FILTER)
					.build(), 1);
			if (anchor.scoreDocs.length == 0) {
				// The refusal is spoken outside: a SearchFunction only throws IOException.
				return null;
			}
			MoreLikeThis mlt = new MoreLikeThis(searcher.getIndexReader());
			mlt.setAnalyzer(unit.config().analyzers().defaultAnalyzer());
			mlt.setFieldNames(fields);
			mlt.setMinTermFreq(request.termFreq());
			mlt.setMinDocFreq(request.docFreq());
			Query like = mlt.like(anchor.scoreDocs[0].doc);
			Query neighbours = new BooleanQuery.Builder()
					.add(like, Occur.MUST)
					.add(SearchFields.rootFilter(), Occur.FILTER)
					.add(sameType, Occur.FILTER)
					.add(new TermQuery(new Term(SearchFields.ID, anchorId)), Occur.MUST_NOT)
					.build();
			TopDocs top = searcher.search(neighbours, request.hits());
			StoredFields stored = searcher.storedFields();
			List<SimilarHit> found = new ArrayList<>(top.scoreDocs.length);
			for (ScoreDoc scoreDoc : top.scoreDocs) {
				Document root = stored.document(scoreDoc.doc);
				EObject object = reader.read(root, DocumentReader.blockChildren(searcher, stored, root));
				found.add(new SimilarHit(object, scoreDoc.score));
			}
			return found;
		});
		if (hits == null) {
			throw new QueryException("The anchor (" + type.getName() + " '" + anchorId + "') is not "
					+ "in the index, so it has no term statistics to be similar to. Index it first.");
		}
		return hits;
	}

	/** The effective field name of a statistics-capable attribute — refused by name otherwise. */
	private String statisticsSource(EClass type, EAttribute attribute) throws QueryException {
		IndexSchema.Field field;
		try {
			field = schema.resolve(type, attribute);
		} catch (MappingException e) {
			throw new QueryException(e.getMessage(), e);
		}
		if (field.kind() != IndexSchema.FieldKind.TEXT) {
			throw new QueryException("Field '" + field.name() + "' is not analyzed text — a keyword or "
					+ "point has no term statistics to compare. Similarity reads analyzed text fields.");
		}
		FieldMapping declared = schema.fieldMapping(type, attribute);
		boolean vectored = declared instanceof TextFieldMapping text && text.isTermVectors();
		if (declared != null && !declared.isStored() && !vectored) {
			throw new QueryException("Field '" + field.name() + "' is mapped stored=false and carries no "
					+ "term vectors, so its terms cannot be recovered for the anchor. Keep the stored "
					+ "value or declare termVectors=\"true\" on the mapping.");
		}
		return field.name();
	}
}
