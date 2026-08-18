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
package org.eclipse.fennec.search.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.SortedSetSortField;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.model.expression.Expression;
import org.eclipse.fennec.model.expression.PropertyPath;
import org.eclipse.fennec.model.expression.Quantifier;
import org.eclipse.fennec.model.query.OrderBy;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.Selection;
import org.eclipse.fennec.model.query.SortDirection;
import org.eclipse.fennec.persistence.capabilities.QueryCapabilities;
import org.eclipse.fennec.persistence.capabilities.QueryCapabilitiesBuilder;
import org.eclipse.fennec.persistence.capabilities.QueryFeature;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.api.QueryContext;
import org.eclipse.fennec.persistence.query.api.QueryPlan;
import org.eclipse.fennec.persistence.query.api.QueryProcessor;
import org.eclipse.fennec.persistence.query.api.QueryShape;
import org.eclipse.fennec.persistence.query.support.QueryValidator;
import org.eclipse.fennec.search.esearch.FieldMapping;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappingException;

/**
 * The {@link QueryProcessor} for embedded Lucene: canonical query in, {@link
 * LuceneQueryPlan} out.
 * <p>
 * <b>What it declares is what it can do exactly.</b> The capability set below is the
 * honest surface of an inverted index (docs/search-access.md §5): term and range
 * predicates, string matching, three-valued negation (§5.1), sorting over doc values,
 * paging, counting, the type discriminator and scalar projections. Everything else —
 * query-time joins, arithmetic and function push-down, field-to-field comparisons,
 * pipelines, {@code EXPAND} — is left undeclared so a consumer can route around it
 * before executing anything, instead of discovering the gap through a wrong answer.
 * Several undeclared features are not "impossible" but "not yet": relevance arrives with
 * S6, facets with S7, geo with S9. Quantifiers are declared since S11 — the block join
 * over NESTED containment, the one join that is an index-time fact (§5.2).
 * <p>
 * Translation is pure: it reads the mapping and the query, never the index. One processor
 * belongs to one index unit, because which field carries a feature — and whether it is a
 * term, a point or an analyzed text — is a property of that unit's mapping.
 *
 * @author Data In Motion Consulting
 */
public final class LuceneQueryProcessor implements QueryProcessor {

	/** The backend id this processor serves. */
	public static final String BACKEND = "lucene";

	/** Diagnostic source for refusals this backend raises itself. */
	public static final String DIAGNOSTIC_SOURCE = "org.eclipse.fennec.search";

	/** A feature path does not resolve to a readable field in this unit's mapping. */
	public static final int CODE_UNMAPPED_PATH = 100;

	/** The projection asks for a field the mapping does not store. */
	public static final int CODE_NOT_STORED = 101;

	/** The sort asks for a field that has no doc values. */
	public static final int CODE_NOT_SORTABLE = 102;

	private static final QueryCapabilities CAPABILITIES = QueryCapabilitiesBuilder.create()
			.support(QueryFeature.WHERE_EQ, QueryFeature.WHERE_NE, QueryFeature.WHERE_COMPARISON,
					QueryFeature.WHERE_RANGE, QueryFeature.IS_NULL, QueryFeature.IN,
					QueryFeature.WHERE_STRING_MATCH, QueryFeature.STRING_MATCH_CASE_INSENSITIVE,
					QueryFeature.LOGICAL_AND, QueryFeature.LOGICAL_OR, QueryFeature.LOGICAL_NOT,
					QueryFeature.SORT, QueryFeature.LIMIT, QueryFeature.SKIP, QueryFeature.COUNT,
					QueryFeature.TYPE_CHECK, QueryFeature.TYPE_FILTER, QueryFeature.PROJECTION,
					QueryFeature.PARAMETERS, QueryFeature.FEATUREPATH_NESTED,
					// The block join (S11, #9): supported over NESTED containment only; a
					// quantifier over EMBED or ID_ONLY is still refused by name in translation.
					QueryFeature.EXISTS, QueryFeature.FOR_ALL)
			// Paths reach as far as the mapping flattened the document with EMBED; how far
			// that is depends on the mapping, not on the engine, so the depth stays open and
			// a path that leaves the document is refused by name during validation.
			.maxFeaturePathDepth(-1)
			.build();

	private final IndexSchema schema;
	private final Analyzer analyzer;

	private LuceneQueryProcessor(IndexSchema schema, Analyzer analyzer) {
		this.schema = schema;
		this.analyzer = analyzer;
	}

	/**
	 * A processor for one index unit's schema.
	 *
	 * @param analyzer the unit's query analyzer, used to analyze string-match patterns the
	 *        same way the indexed values were; a {@link StandardAnalyzer} when null
	 */
	public static LuceneQueryProcessor of(IndexSchema schema, Analyzer analyzer) {
		Objects.requireNonNull(schema, "schema");
		return new LuceneQueryProcessor(schema, analyzer == null ? new StandardAnalyzer() : analyzer);
	}

	@Override
	public String backend() {
		return BACKEND;
	}

	@Override
	public QueryCapabilities capabilities() {
		return CAPABILITIES;
	}

	/**
	 * The same declaration without a processor instance. The query vocabulary is a property
	 * of the backend, not of one unit's mapping, so the {@code PersistenceCapabilities}
	 * aggregate a resource answers (issue #134 upstream) reads it from here rather than
	 * restating it — two lists for one backend would drift.
	 */
	public static QueryCapabilities declaredCapabilities() {
		return CAPABILITIES;
	}

	/**
	 * Capability validation from the shared validator, plus the part only this unit can
	 * answer: whether the paths the query names resolve to readable fields here. A
	 * mapping-level miss is an error at validation time rather than a surprise during
	 * translation — the query never reaches an index either way, but the caller learns why
	 * from {@code validate} as the contract promises.
	 */
	@Override
	public Diagnostic validate(Query query, EClass rootEClass) {
		Diagnostic capabilities = QueryValidator.validate(query, rootEClass, CAPABILITIES);
		if (capabilities.getSeverity() >= Diagnostic.ERROR) {
			return capabilities;
		}
		List<Diagnostic> problems = new ArrayList<>();
		List<ScopedPath> paths = new ArrayList<>();
		collect(query.getPredicate(), rootEClass, paths, problems);
		for (ScopedPath scoped : paths) {
			try {
				schema.resolve(scoped.scope(), scoped.path().getSegments());
			} catch (MappingException e) {
				problems.add(new BasicDiagnostic(Diagnostic.ERROR, DIAGNOSTIC_SOURCE, CODE_UNMAPPED_PATH,
						e.getMessage(), new Object[] { scoped.path() }));
			}
		}
		for (Selection selection : query.getSelect()) {
			if (selection.getPath() == null) {
				continue;
			}
			try {
				storedField(rootEClass, selection);
			} catch (QueryException e) {
				problems.add(new BasicDiagnostic(Diagnostic.ERROR, DIAGNOSTIC_SOURCE, CODE_NOT_STORED,
						e.getMessage(), new Object[] { selection }));
			}
		}
		for (OrderBy orderBy : query.getOrderBy()) {
			if (orderBy.getPath() == null) {
				continue;
			}
			try {
				sortField(rootEClass, orderBy);
			} catch (QueryException e) {
				problems.add(new BasicDiagnostic(Diagnostic.ERROR, DIAGNOSTIC_SOURCE, CODE_NOT_SORTABLE,
						e.getMessage(), new Object[] { orderBy }));
			}
		}
		if (problems.isEmpty()) {
			return capabilities;
		}
		BasicDiagnostic all = new BasicDiagnostic(DIAGNOSTIC_SOURCE, CODE_UNMAPPED_PATH,
				"The query names fields this index unit cannot read", new Object[] { query });
		problems.forEach(all::add);
		return all;
	}

	@Override
	public QueryPlan translate(Query query, QueryContext context) throws QueryException {
		Objects.requireNonNull(query, "query");
		Objects.requireNonNull(context, "context");
		if (query.getFrom() == null && context.rootEClass() == null) {
			throw new QueryException("A query needs a root type, either on the query or in the context");
		}
		EClass root = context.rootEClass() != null ? context.rootEClass() : query.getFrom();
		refuseUndeclared(query);

		QueryTranslator translator = new QueryTranslator(schema, context, analyzer);
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		// Two guards on every plan: only root documents of a block are hits — otherwise a
		// NESTED child would count as a result and inflate a count — and only the requested
		// type, since one unit holds several.
		builder.add(QueryTranslator.rootFilter(), BooleanClause.Occur.MUST);
		builder.add(translator.typeFilter(root), BooleanClause.Occur.MUST);
		if (query.getPredicate() != null) {
			builder.add(translator.predicate(query.getPredicate()), BooleanClause.Occur.MUST);
		}

		QueryShape shape = shapeOf(query);
		Sort sort = sortOf(query, root);
		List<String> rowFields = new ArrayList<>();
		List<String> rowAliases = new ArrayList<>();
		if (shape == QueryShape.PROJECTION) {
			for (Selection selection : query.getSelect()) {
				IndexSchema.Field field = storedField(root, selection);
				rowFields.add(field.name());
				rowAliases.add(selection.getAlias() == null || selection.getAlias().isBlank()
						? field.name()
						: selection.getAlias());
			}
		}
		return new LuceneQueryPlan(query, shape, builder.build(), sort, Math.max(0, query.getSkip()),
				query.getTop() > 0 ? query.getTop() : -1, rowFields, rowAliases);
	}

	// --- shape, sort and projection --------------------------------------------------------

	private QueryShape shapeOf(Query query) throws QueryException {
		if (query.isCountOnly()) {
			return QueryShape.COUNT;
		}
		if (!query.getSelect().isEmpty()) {
			return QueryShape.PROJECTION;
		}
		return QueryShape.OBJECTS;
	}

	private Sort sortOf(Query query, EClass root) throws QueryException {
		if (query.getOrderBy().isEmpty()) {
			return null;
		}
		List<SortField> fields = new ArrayList<>(query.getOrderBy().size());
		for (OrderBy orderBy : query.getOrderBy()) {
			fields.add(sortField(root, orderBy));
		}
		return new Sort(fields.toArray(SortField[]::new));
	}

	private SortField sortField(EClass root, OrderBy orderBy) throws QueryException {
		if (orderBy.getPath() == null) {
			throw new QueryException("Sorting by a computed expression (SORT_EXPRESSION) is not declared: a "
					+ "doc-values sort reads a stored value, it does not evaluate. Index the value as its "
					+ "own field and sort on that. Relevance order arrives with S6 (issue #10).");
		}
		IndexSchema.Field field = resolve(root, orderBy.getPath());
		boolean reverse = orderBy.getDirection() == SortDirection.DESC;
		// The kind comes first: for analyzed text, "declare doc values" would be the wrong
		// advice — no doc values on a tokenized field would give the order of its tokens.
		if (field.kind() == IndexSchema.FieldKind.TEXT) {
			throw new QueryException("Field '" + field.name() + "' is analyzed text; sorting on it would "
					+ "order by its tokens, not by its value. Declare a keyword field for '"
					+ field.attribute().getName() + "' and sort on that.");
		}
		if (!field.docValues()) {
			throw new QueryException("Field '" + field.name() + "' has no doc values, so it cannot be sorted "
					+ "on. Declare docValues=true for '" + field.attribute().getName() + "'.");
		}
		return switch (field.kind()) {
			case KEYWORD -> new SortedSetSortField(field.name(), reverse);
			case NUMERIC -> numericSort(field, reverse);
			case TEXT -> throw new QueryException("unreachable: analyzed text is refused above");
		};
	}

	private SortField numericSort(IndexSchema.Field field, boolean reverse) throws QueryException {
		SortField.Type type = switch (field.numericKind()) {
			case INT -> SortField.Type.INT;
			case LONG, DATE -> SortField.Type.LONG;
			case FLOAT -> SortField.Type.FLOAT;
			case DOUBLE -> SortField.Type.DOUBLE;
			default -> throw new QueryException("Numeric kind " + field.numericKind() + " has no sort type");
		};
		// The mapper writes a sorted-numeric field for many-valued attributes and a plain
		// one otherwise; the sort has to read the same shape it was written in.
		return field.attribute().isMany()
				? new SortedNumericSortField(field.name(), type, reverse)
				: new SortField(field.name(), type, reverse);
	}

	private IndexSchema.Field storedField(EClass root, Selection selection) throws QueryException {
		IndexSchema.Field field = resolve(root, selection.getPath());
		FieldMapping declared = schema.fieldMapping(field.attribute().getEContainingClass(),
				field.attribute());
		boolean stored = declared != null ? declared.isStored() : field.attribute().isID();
		if (!stored) {
			throw new QueryException("Projection asks for '" + field.name() + "', which the mapping does not "
					+ "store. An inverted index can find a document by a value without being able to give "
					+ "the value back — declare stored=true for '" + field.attribute().getName()
					+ "' to project it.");
		}
		return field;
	}

	private IndexSchema.Field resolve(EClass root, PropertyPath path) throws QueryException {
		try {
			return schema.resolve(root, path.getSegments());
		} catch (MappingException e) {
			throw new QueryException(e.getMessage(), e);
		}
	}

	// --- refusals that are about the query as a whole ---------------------------------------

	private void refuseUndeclared(Query query) throws QueryException {
		if (query.getApply() != null && !query.getApply().getStages().isEmpty()) {
			throw new QueryException("Pipelines are not declared. The two stages Lucene can answer natively "
					+ "are grouping and counting, which arrive as facets in S7 (issue #11) and grouping in "
					+ "S19 (issue #21); a general compute/having pipeline it cannot answer at all.");
		}
		if (!query.getExpand().isEmpty()) {
			throw new QueryException("EXPAND prefetches along references, which means following them at "
					+ "query time — the join this backend does not offer (docs/search-access.md §5).");
		}
		if (query.isDistinct()) {
			throw new QueryException("DISTINCT is not declared: an inverted index can enumerate the terms of "
					+ "a field, but de-duplicating whole result rows is a post-processing step, not a "
					+ "query it answers.");
		}
	}

	/** A path together with the class its segments resolve against. */
	private record ScopedPath(EClass scope, PropertyPath path) {
	}

	/**
	 * Collects every property path with the class it resolves against. A quantifier is the
	 * one construct that changes scope: its source is checked here with the same rule the
	 * translator applies ({@link QueryTranslator#nestedReference}), and its predicate's
	 * paths resolve against the child class of the block.
	 */
	private void collect(Expression expression, EClass scope, List<ScopedPath> paths,
			List<Diagnostic> problems) {
		if (expression == null) {
			return;
		}
		if (expression instanceof Quantifier quantifier) {
			try {
				EReference reference = QueryTranslator.nestedReference(schema, scope, quantifier);
				collect(quantifier.getPredicate(), reference.getEReferenceType(), paths, problems);
			} catch (QueryException e) {
				problems.add(new BasicDiagnostic(Diagnostic.ERROR, DIAGNOSTIC_SOURCE, CODE_UNMAPPED_PATH,
						e.getMessage(), new Object[] { quantifier }));
			}
			return;
		}
		if (expression instanceof PropertyPath path) {
			if (!path.getSegments().isEmpty() && path.getSegments()
					.get(path.getSegments().size() - 1) instanceof EStructuralFeature) {
				paths.add(new ScopedPath(scope, path));
			}
			return;
		}
		expression.eContents().forEach(child -> {
			if (child instanceof Expression nested) {
				collect(nested, scope, paths, problems);
			}
		});
	}
}
