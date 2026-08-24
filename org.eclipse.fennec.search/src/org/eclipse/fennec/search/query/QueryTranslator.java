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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldExistsQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.join.BitSetProducer;
import org.apache.lucene.search.join.QueryBitSetProducer;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.search.join.ToParentBlockJoinQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.automaton.LevenshteinAutomata;
import org.apache.lucene.util.automaton.Operations;
import org.apache.lucene.util.automaton.RegExp;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.model.expression.Arithmetic;
import org.eclipse.fennec.model.expression.Between;
import org.eclipse.fennec.model.expression.CollectionCount;
import org.eclipse.fennec.model.expression.Comparison;
import org.eclipse.fennec.model.expression.ComparisonOperator;
import org.eclipse.fennec.model.expression.Exists;
import org.eclipse.fennec.model.expression.Expression;
import org.eclipse.fennec.model.expression.GeoBox;
import org.eclipse.fennec.model.expression.GeoDistance;
import org.eclipse.fennec.model.expression.GeoPointLiteral;
import org.eclipse.fennec.model.expression.GeoPolygon;
import org.eclipse.fennec.model.expression.GeoShape;
import org.eclipse.fennec.model.expression.GeoWithin;
import org.eclipse.fennec.model.expression.In;
import org.eclipse.fennec.model.expression.IsNull;
import org.eclipse.fennec.model.expression.Junction;
import org.eclipse.fennec.model.expression.Not;
import org.eclipse.fennec.model.expression.NumericFunction;
import org.eclipse.fennec.model.expression.Or;
import org.eclipse.fennec.model.expression.PropertyPath;
import org.eclipse.fennec.model.expression.Quantifier;
import org.eclipse.fennec.model.expression.Score;
import org.eclipse.fennec.model.expression.StringFunction;
import org.eclipse.fennec.model.expression.StringMatch;
import org.eclipse.fennec.model.expression.StringMatchKind;
import org.eclipse.fennec.model.expression.TemporalFunction;
import org.eclipse.fennec.model.expression.TypeCheck;
import org.eclipse.fennec.model.expression.Variable;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.api.QueryContext;
import org.eclipse.fennec.persistence.query.expr.ExpressionValues;
import org.eclipse.fennec.persistence.query.support.QueryContexts;
import org.eclipse.fennec.search.esearch.FieldUse;
import org.eclipse.fennec.search.esearch.NumericKind;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.IndexSchema.FieldKind;
import org.eclipse.fennec.search.mapping.MappingException;
import org.eclipse.fennec.search.mapping.SearchFields;

/**
 * Translates an expression-IR predicate into a Lucene {@link Query}.
 * <p>
 * <b>Negation is pushed down, never wrapped</b> (docs/search-access.md §5.1). Lucene's
 * {@code MUST_NOT} is two-valued: it matches every document the positive clause did not,
 * including those that have no value for the field at all. SQL's three-valued logic says
 * a comparison over null is UNKNOWN and {@code NOT UNKNOWN} is still UNKNOWN, so such a
 * document must <em>not</em> match either way. Wrapping a translated clause in
 * {@code MUST_NOT} would therefore change the answer. Instead a negation flag travels
 * down the tree — De Morgan across junctions, operator inversion at comparisons — and the
 * leaves that genuinely need one carry an explicit {@link FieldExistsQuery} guard.
 * <p>
 * Whatever cannot be answered honestly is refused with a {@link QueryException} naming
 * what and why, rather than approximated.
 *
 * @author Data In Motion Consulting
 */
final class QueryTranslator {

	/**
	 * The one parent filter every block join in this backend uses. {@code
	 * QueryBitSetProducer} caches the bit set per leaf reader (weakly, so closed readers
	 * release it), and the root-marker query is identical for every unit — one shared
	 * producer keeps the cache shared too.
	 */
	private static final BitSetProducer PARENT_FILTER = new QueryBitSetProducer(rootFilter());

	/**
	 * FUZZY is the one string-match kind with no regular-expression form: it is translated
	 * into an automaton before either regexp path is reached, so both switches over
	 * {@code StringMatchKind} name it only to stay exhaustive.
	 */
	private static final String FUZZY_HAS_NO_REGEXP =
			"FUZZY is matched by an edit-distance automaton, not by a regular expression";

	private final IndexSchema schema;
	private final QueryContext context;
	private final Analyzer analyzer;
	/** The iterator variable when this translator runs inside a quantifier; null at root. */
	private final Variable scope;

	QueryTranslator(IndexSchema schema, QueryContext context, Analyzer analyzer) {
		this(schema, context, analyzer, null);
	}

	private QueryTranslator(IndexSchema schema, QueryContext context, Analyzer analyzer, Variable scope) {
		this.schema = schema;
		this.context = context;
		this.analyzer = analyzer;
		this.scope = scope;
	}

	/** Translates a predicate; the entry point, with negation not yet applied. */
	Query predicate(Expression expression) throws QueryException {
		return translate(expression, false);
	}

	/**
	 * @param negated whether an odd number of {@code Not} nodes encloses this expression
	 */
	private Query translate(Expression expression, boolean negated) throws QueryException {
		if (expression instanceof Not not) {
			if (not.getOperand() == null) {
				throw new QueryException("A Not without an operand cannot be translated");
			}
			return translate(not.getOperand(), !negated);
		}
		if (expression instanceof Junction junction) {
			return junction(junction, negated);
		}
		if (expression instanceof Comparison comparison) {
			return comparison(comparison, negated);
		}
		if (expression instanceof IsNull isNull) {
			// The node carries its own polarity (negated = IS NOT NULL), and an enclosing Not
			// flips it once more.
			boolean isNotNull = isNull.isNegated() != negated;
			IndexSchema.Field field = path(isNull.getSource());
			return isNotNull ? existsQuery(field) : missingQuery(field);
		}
		if (expression instanceof Between between) {
			return between(between, negated);
		}
		if (expression instanceof In in) {
			return in(in, negated);
		}
		if (expression instanceof StringMatch match) {
			return stringMatch(match, negated);
		}
		if (expression instanceof TypeCheck typeCheck) {
			return typeCheck(typeCheck, negated);
		}
		if (expression instanceof Quantifier quantifier) {
			return quantifier(quantifier, negated);
		}
		if (expression instanceof Score) {
			throw new QueryException("score() is a sort key, not a predicate: absolute score values carry "
					+ "no reference semantics (emf.persistence-jpa#100), so comparing against them would "
					+ "put numbers under contract that are not one. Order by score instead.");
		}
		if (expression instanceof GeoWithin within) {
			return geoWithin(within, negated);
		}
		if (expression instanceof GeoDistance) {
			throw new QueryException("A distance is a value, not a predicate (emf.persistence-jpa#101, "
					+ "decision G3): compare it against a threshold — geoDistance(...) <= 500 — or use it "
					+ "as a sort key for nearest-first.");
		}
		if (expression instanceof Arithmetic || expression instanceof StringFunction
				|| expression instanceof NumericFunction || expression instanceof TemporalFunction
				|| expression instanceof CollectionCount) {
			throw new QueryException("This backend does not push down computation: " + name(expression)
					+ " has no Lucene equivalent that stays exact. Compute it as an indexed field "
					+ "instead (see docs/search-access.md §4.2), or route the query to the primary store.");
		}
		throw new QueryException("No Lucene translation for " + name(expression));
	}

	// --- junctions ------------------------------------------------------------------------

	/** De Morgan: a negated AND becomes an OR of negated operands, and vice versa. */
	private Query junction(Junction junction, boolean negated) throws QueryException {
		if (junction.getOperands().isEmpty()) {
			throw new QueryException("An empty " + name(junction) + " has no truth value to translate");
		}
		boolean disjunction = junction instanceof Or;
		if (negated) {
			disjunction = !disjunction;
		}
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		for (Expression operand : junction.getOperands()) {
			builder.add(translate(operand, negated),
					disjunction ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST);
		}
		if (disjunction) {
			builder.setMinimumNumberShouldMatch(1);
		}
		return builder.build();
	}

	// --- comparisons ----------------------------------------------------------------------

	private Query comparison(Comparison comparison, boolean negated) throws QueryException {
		Expression left = comparison.getLeft();
		Expression right = comparison.getRight();
		if (left instanceof Score || right instanceof Score) {
			throw new QueryException("score() is a sort key, not a predicate: absolute score values carry "
					+ "no reference semantics (emf.persistence-jpa#100), so comparing against them would "
					+ "put numbers under contract that are not one. Order by score instead.");
		}
		if (left instanceof GeoDistance || right instanceof GeoDistance) {
			return geoDistance(comparison, negated);
		}
		if (left instanceof PropertyPath && right instanceof PropertyPath) {
			throw new QueryException("Comparing two fields of the same document (FIELD_TO_FIELD) is not "
					+ "something an inverted index can answer — a term dictionary knows values, not "
					+ "per-document pairs. Not declared; route it to the primary store.");
		}
		ComparisonOperator operator = comparison.getOperator();
		PropertyPath subject;
		Expression value;
		if (left instanceof PropertyPath leftPath) {
			subject = leftPath;
			value = right;
		} else if (right instanceof PropertyPath rightPath) {
			// `30 < age` is `age > 30`: mirroring keeps one code path for the operators.
			subject = rightPath;
			value = left;
			operator = mirror(operator);
		} else {
			throw new QueryException("A comparison needs a feature path on one side; " + name(left) + " vs "
					+ name(right) + " compares two computed values, which this backend cannot answer.");
		}
		if (negated) {
			operator = invert(operator);
		}
		IndexSchema.Field field = path(subject, operator == ComparisonOperator.EQ
				|| operator == ComparisonOperator.NE ? FieldUse.EXACT : FieldUse.RANGE);
		Object typed = value(value, field.attribute());
		return comparisonQuery(field, operator, typed);
	}

	private Query comparisonQuery(IndexSchema.Field field, ComparisonOperator operator, Object value)
			throws QueryException {
		return switch (operator) {
			case EQ -> equality(field, value);
			// NE must not match a document without a value: UNKNOWN is not true. Hence the
			// explicit existence guard around the negated equality.
			case NE -> guarded(field, equality(field, value));
			case LT, LE, GT, GE -> range(field, operator, value);
		};
	}

	private Query equality(IndexSchema.Field field, Object value) throws QueryException {
		return switch (field.kind()) {
			case KEYWORD -> new TermQuery(new Term(field.name(), string(field, value)));
			case NUMERIC -> exactPoint(field, value);
			case TEXT -> throw new QueryException("Field '" + field.name() + "' is an analyzed text field, so "
					+ "an exact comparison cannot be answered on it: the index holds its tokens, not its "
					+ "value. Declare a keyword field (or a keyword sub-field) for '"
					+ field.attribute().getName() + "' to filter on equality, or use a string match.");
		};
	}

	private Query range(IndexSchema.Field field, ComparisonOperator operator, Object value)
			throws QueryException {
		if (field.kind() == FieldKind.TEXT) {
			throw new QueryException("Field '" + field.name() + "' is an analyzed text field and carries no "
					+ "ordered values, so " + operator + " cannot be answered on it. Declare a keyword or "
					+ "numeric field for '" + field.attribute().getName() + "'.");
		}
		if (field.kind() == FieldKind.KEYWORD) {
			if (field.attribute().getEAttributeType() instanceof EEnum) {
				throw new QueryException("Attribute '" + field.attribute().getName() + "' is an enum, indexed "
						+ "by literal name. Ordering it with " + operator + " would compare names "
						+ "alphabetically, which is not the order the model means — refused instead of "
						+ "answered differently from the primary store.");
			}
			BytesRef term = new BytesRef(string(field, value));
			return switch (operator) {
				case LT -> new TermRangeQuery(field.name(), null, term, true, false);
				case LE -> new TermRangeQuery(field.name(), null, term, true, true);
				case GT -> new TermRangeQuery(field.name(), term, null, false, true);
				case GE -> new TermRangeQuery(field.name(), term, null, true, true);
				default -> throw new QueryException("Not a range operator: " + operator);
			};
		}
		return numericRange(field, operator, value);
	}

	/** Point ranges are half-open by construction: Lucene has no exclusive bound. */
	private Query numericRange(IndexSchema.Field field, ComparisonOperator operator, Object value)
			throws QueryException {
		NumericKind kind = field.numericKind();
		Number number = number(field, value);
		switch (kind) {
			case INT -> {
				int v = number.intValue();
				return switch (operator) {
					case LT -> IntPoint.newRangeQuery(field.name(), Integer.MIN_VALUE, addExact(v, -1));
					case LE -> IntPoint.newRangeQuery(field.name(), Integer.MIN_VALUE, v);
					case GT -> IntPoint.newRangeQuery(field.name(), addExact(v, 1), Integer.MAX_VALUE);
					case GE -> IntPoint.newRangeQuery(field.name(), v, Integer.MAX_VALUE);
					default -> throw new QueryException("Not a range operator: " + operator);
				};
			}
			case LONG, DATE -> {
				long v = number.longValue();
				return switch (operator) {
					case LT -> LongPoint.newRangeQuery(field.name(), Long.MIN_VALUE, addExact(v, -1));
					case LE -> LongPoint.newRangeQuery(field.name(), Long.MIN_VALUE, v);
					case GT -> LongPoint.newRangeQuery(field.name(), addExact(v, 1), Long.MAX_VALUE);
					case GE -> LongPoint.newRangeQuery(field.name(), v, Long.MAX_VALUE);
					default -> throw new QueryException("Not a range operator: " + operator);
				};
			}
			case FLOAT -> {
				float v = number.floatValue();
				return switch (operator) {
					case LT -> FloatPoint.newRangeQuery(field.name(), Float.NEGATIVE_INFINITY,
							Math.nextDown(v));
					case LE -> FloatPoint.newRangeQuery(field.name(), Float.NEGATIVE_INFINITY, v);
					case GT -> FloatPoint.newRangeQuery(field.name(), Math.nextUp(v),
							Float.POSITIVE_INFINITY);
					case GE -> FloatPoint.newRangeQuery(field.name(), v, Float.POSITIVE_INFINITY);
					default -> throw new QueryException("Not a range operator: " + operator);
				};
			}
			case DOUBLE -> {
				double v = number.doubleValue();
				return switch (operator) {
					case LT -> DoublePoint.newRangeQuery(field.name(), Double.NEGATIVE_INFINITY,
							Math.nextDown(v));
					case LE -> DoublePoint.newRangeQuery(field.name(), Double.NEGATIVE_INFINITY, v);
					case GT -> DoublePoint.newRangeQuery(field.name(), Math.nextUp(v),
							Double.POSITIVE_INFINITY);
					case GE -> DoublePoint.newRangeQuery(field.name(), v, Double.POSITIVE_INFINITY);
					default -> throw new QueryException("Not a range operator: " + operator);
				};
			}
			default -> throw new QueryException("Numeric kind " + kind + " has no point range query");
		}
	}

	private Query exactPoint(IndexSchema.Field field, Object value) throws QueryException {
		Number number = number(field, value);
		return switch (field.numericKind()) {
			case INT -> IntPoint.newExactQuery(field.name(), number.intValue());
			case LONG, DATE -> LongPoint.newExactQuery(field.name(), number.longValue());
			case FLOAT -> FloatPoint.newExactQuery(field.name(), number.floatValue());
			case DOUBLE -> DoublePoint.newExactQuery(field.name(), number.doubleValue());
			default -> throw new QueryException("Numeric kind " + field.numericKind()
					+ " has no exact point query");
		};
	}

	// --- between / in ---------------------------------------------------------------------

	private Query between(Between between, boolean negated) throws QueryException {
		IndexSchema.Field field = path(between.getSource(), FieldUse.RANGE);
		Object lower = value(between.getLower(), field.attribute());
		Object upper = value(between.getUpper(), field.attribute());
		ComparisonOperator lowerOp = between.isLowerIncluded() ? ComparisonOperator.GE : ComparisonOperator.GT;
		ComparisonOperator upperOp = between.isUpperIncluded() ? ComparisonOperator.LE : ComparisonOperator.LT;
		if (!negated) {
			return new BooleanQuery.Builder()
					.add(comparisonQuery(field, lowerOp, lower), BooleanClause.Occur.MUST)
					.add(comparisonQuery(field, upperOp, upper), BooleanClause.Occur.MUST)
					.build();
		}
		// Outside the interval, still only for documents that have a value: each disjunct
		// is a range query, and a range query never matches a missing field.
		return new BooleanQuery.Builder()
				.add(comparisonQuery(field, invert(lowerOp), lower), BooleanClause.Occur.SHOULD)
				.add(comparisonQuery(field, invert(upperOp), upper), BooleanClause.Occur.SHOULD)
				.setMinimumNumberShouldMatch(1)
				.build();
	}

	private Query in(In in, boolean negated) throws QueryException {
		IndexSchema.Field field = path(in.getSource(), FieldUse.EXACT);
		if (in.getValues().isEmpty()) {
			// `x IN ()` is false for every document; negated it is true for those that have a
			// value and UNKNOWN — so still not a match — for those that do not.
			return negated ? existsQuery(field) : new BooleanQuery.Builder().build();
		}
		Query positive;
		if (field.kind() == FieldKind.KEYWORD) {
			// A sorted set keeps the query stable regardless of the order the IR listed them in.
			TreeSet<BytesRef> terms = new TreeSet<>();
			for (Expression value : in.getValues()) {
				terms.add(new BytesRef(string(field, value(value, field.attribute()))));
			}
			positive = new TermInSetQuery(field.name(), new ArrayList<>(terms));
		} else if (field.kind() == FieldKind.NUMERIC) {
			positive = numericSet(field, in);
		} else {
			throw new QueryException("Field '" + field.name() + "' is an analyzed text field, so IN cannot be "
					+ "answered exactly on it. Declare a keyword field for '"
					+ field.attribute().getName() + "'.");
		}
		return negated ? guarded(field, positive) : positive;
	}

	private Query numericSet(IndexSchema.Field field, In in) throws QueryException {
		List<Number> numbers = new ArrayList<>(in.getValues().size());
		for (Expression value : in.getValues()) {
			numbers.add(number(field, value(value, field.attribute())));
		}
		switch (field.numericKind()) {
			case INT -> {
				int[] values = new int[numbers.size()];
				for (int i = 0; i < values.length; i++) {
					values[i] = numbers.get(i).intValue();
				}
				return IntPoint.newSetQuery(field.name(), values);
			}
			case LONG, DATE -> {
				long[] values = new long[numbers.size()];
				for (int i = 0; i < values.length; i++) {
					values[i] = numbers.get(i).longValue();
				}
				return LongPoint.newSetQuery(field.name(), values);
			}
			case FLOAT -> {
				float[] values = new float[numbers.size()];
				for (int i = 0; i < values.length; i++) {
					values[i] = numbers.get(i).floatValue();
				}
				return FloatPoint.newSetQuery(field.name(), values);
			}
			case DOUBLE -> {
				double[] values = new double[numbers.size()];
				for (int i = 0; i < values.length; i++) {
					values[i] = numbers.get(i).doubleValue();
				}
				return DoublePoint.newSetQuery(field.name(), values);
			}
			default -> throw new QueryException("Numeric kind " + field.numericKind()
					+ " has no point set query");
		}
	}

	// --- string matching ------------------------------------------------------------------

	private Query stringMatch(StringMatch match, boolean negated) throws QueryException {
		// A string match runs on whole terms wherever a keyword projection exists — that is
		// what EXACT means here — and falls back to the analyzed field when it does not.
		IndexSchema.Field field = path(match.getSource(), FieldUse.EXACT);
		Object patternValue = value(match.getPattern(), field.attribute());
		if (patternValue == null) {
			// A null pattern is UNKNOWN, and NOT UNKNOWN stays UNKNOWN: no document matches.
			return new BooleanQuery.Builder().build();
		}
		String pattern = patternValue.toString();
		StringMatchKind kind = match.getKind() == null ? StringMatchKind.CONTAINS : match.getKind();
		Query positive = field.kind() == FieldKind.TEXT
				? analyzedMatch(field, kind, pattern, match.isCaseInsensitive())
				: keywordMatch(field, match, kind, pattern);
		return negated ? guarded(field, positive) : positive;
	}

	private Query keywordMatch(IndexSchema.Field field, StringMatch match, StringMatchKind kind,
			String pattern) throws QueryException {
		if (field.kind() == FieldKind.NUMERIC) {
			throw new QueryException("Field '" + field.name() + "' is numeric, so the string match "
					+ kind + " has nothing to match against.");
		}
		boolean caseInsensitive = match.isCaseInsensitive();
		if (kind == StringMatchKind.FUZZY) {
			return fuzzyMatch(field, match, pattern);
		}
		Term term = new Term(field.name(), regexOf(kind, pattern));
		if (caseInsensitive) {
			return new RegexpQuery(term, RegExp.ALL, RegExp.CASE_INSENSITIVE,
					Operations.DEFAULT_DETERMINIZE_WORK_LIMIT);
		}
		// The case-sensitive forms have cheaper query types than a regexp.
		return switch (kind) {
			case STARTS_WITH -> new PrefixQuery(new Term(field.name(), pattern));
			case CONTAINS -> new WildcardQuery(new Term(field.name(), "*" + escapeWildcard(pattern) + "*"));
			case ENDS_WITH -> new WildcardQuery(new Term(field.name(), "*" + escapeWildcard(pattern)));
			case LIKE -> new RegexpQuery(new Term(field.name(), likeToRegex(pattern)));
			case FUZZY -> throw new IllegalStateException(FUZZY_HAS_NO_REGEXP);
		};
	}

	/**
	 * Edit distance over the whole keyword term (emf.persistence-jpa#167). Lucene's
	 * {@link FuzzyQuery} counts optimal-string-alignment Damerau-Levenshtein with adjacent
	 * transpositions as one edit and the {@code prefixLength} leading characters required
	 * exactly — the same distance the IR's in-memory oracle computes, so a keyword
	 * projection agrees with it term for term.
	 * <p>
	 * Two deliberate departures from Lucene's defaults: the rewrite is the constant-score
	 * one every other multi-term form here uses, because the default top-terms rewrite
	 * silently drops everything past the 50 closest terms and a predicate that quietly
	 * answers less than it was asked is worse than a slow one; and the budget stays at the
	 * IR's 1..2 rather than Lucene's 0..2, since {@code maxEdits = 0} is an exact match
	 * spelled the long way and the validator refuses it upstream anyway.
	 */
	private Query fuzzyMatch(IndexSchema.Field field, StringMatch match, String pattern)
			throws QueryException {
		if (match.isCaseInsensitive()) {
			// The other kinds fold case in a regexp automaton; a fuzzy automaton has no such
			// flag, and folding the pattern alone would not fold the indexed terms.
			throw new QueryException("Field '" + field.name() + "': a case-insensitive FUZZY match is "
					+ "not expressible — the edit-distance automaton runs over the indexed terms "
					+ "verbatim. Declare a lowercasing keyword field for '"
					+ field.attribute().getName() + "', or drop caseInsensitive.");
		}
		int maxEdits = match.getMaxEdits();
		if (maxEdits < 1 || maxEdits > LevenshteinAutomata.MAXIMUM_SUPPORTED_DISTANCE) {
			throw new QueryException("Field '" + field.name() + "': a FUZZY edit budget of " + maxEdits
					+ " is out of range — Lucene matches up to "
					+ LevenshteinAutomata.MAXIMUM_SUPPORTED_DISTANCE + " edits, so maxEdits is 1 or 2.");
		}
		int prefixLength = match.getPrefixLength();
		if (prefixLength < 0) {
			throw new QueryException("Field '" + field.name() + "': a FUZZY prefixLength of "
					+ prefixLength + " is negative.");
		}
		return new FuzzyQuery(new Term(field.name(), pattern), maxEdits, prefixLength,
				FuzzyQuery.defaultMaxExpansions, FuzzyQuery.defaultTranspositions,
				MultiTermQuery.CONSTANT_SCORE_BLENDED_REWRITE);
	}

	/**
	 * On an analyzed field only CONTAINS is honest: the index holds tokens, so the pattern
	 * is analyzed the same way and matched as a phrase. Anchoring at the value's start or
	 * end is not expressible — token positions know order, not the value's boundaries.
	 */
	private Query analyzedMatch(IndexSchema.Field field, StringMatchKind kind, String pattern,
			boolean caseInsensitive) throws QueryException {
		if (kind != StringMatchKind.CONTAINS) {
			throw new QueryException("Field '" + field.name() + "' is analyzed, so " + kind + " cannot be "
					+ "answered on it: its tokens carry no value boundaries. CONTAINS works; for "
					+ kind + " declare a keyword field for '" + field.attribute().getName() + "'.");
		}
		List<String> tokens = analyze(field.name(), pattern);
		if (tokens.isEmpty()) {
			// The pattern is all stop words or punctuation: nothing to look for.
			return new BooleanQuery.Builder().build();
		}
		if (tokens.size() == 1) {
			return new TermQuery(new Term(field.name(), tokens.get(0)));
		}
		PhraseQuery.Builder phrase = new PhraseQuery.Builder();
		for (String token : tokens) {
			phrase.add(new Term(field.name(), token));
		}
		return phrase.build();
	}

	private List<String> analyze(String field, String text) throws QueryException {
		List<String> tokens = new ArrayList<>();
		try (TokenStream stream = analyzer.tokenStream(field, text)) {
			CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
			stream.reset();
			while (stream.incrementToken()) {
				tokens.add(term.toString());
			}
			stream.end();
		} catch (Exception e) {
			throw new QueryException("Could not analyze the pattern for field '" + field + "'", e);
		}
		return tokens;
	}

	// --- geo ----------------------------------------------------------------------------

	/**
	 * Containment in a box or polygon (§5.5, S9/#13). Both come out of {@code lucene-core}
	 * as point queries over the one {@code LatLonPoint} the mapper wrote, which is why the
	 * authoring shape of the subject no longer matters here — and why the wrap-around box
	 * needs no special case: {@code newBoxQuery} documents the crossed dateline as the
	 * meaning of {@code minLongitude > maxLongitude}, exactly what G2 declares legal.
	 */
	private Query geoWithin(GeoWithin within, boolean negated) throws QueryException {
		IndexSchema.GeoField field = GeoSubjects.resolve(schema, context.rootEClass(),
				within.getSubject(), "geoWithin");
		GeoShape shape = within.getShape();
		Query positive;
		if (shape instanceof GeoBox box) {
			GeoPointLiteral southWest = corner(box.getSouthWest(), "southWest");
			GeoPointLiteral northEast = corner(box.getNorthEast(), "northEast");
			positive = box(field, southWest, northEast);
		} else if (shape instanceof GeoPolygon polygon) {
			positive = polygon(field, polygon);
		} else {
			throw new QueryException("No Lucene translation for the shape "
					+ (shape == null ? "null" : shape.eClass().getName()));
		}
		return negated ? geoGuarded(field, positive) : positive;
	}

	private Query box(IndexSchema.GeoField field, GeoPointLiteral southWest, GeoPointLiteral northEast)
			throws QueryException {
		try {
			return LatLonPoint.newBoxQuery(field.name(), southWest.getLat(), northEast.getLat(),
					southWest.getLon(), northEast.getLon());
		} catch (IllegalArgumentException e) {
			throw new QueryException("The box for geo field '" + field.name() + "' is not a box Lucene can "
					+ "match: " + e.getMessage(), e);
		}
	}

	/**
	 * The IR's polygon is implicitly closed; Lucene's {@link Polygon} wants the ring spelled
	 * out, so the first vertex is repeated. Holes are not part of the vocabulary.
	 */
	private Query polygon(IndexSchema.GeoField field, GeoPolygon polygon) throws QueryException {
		List<GeoPointLiteral> points = polygon.getPoints();
		if (points.size() < 3) {
			throw new QueryException("A polygon needs at least three points; this one has " + points.size()
					+ ".");
		}
		double[] latitudes = new double[points.size() + 1];
		double[] longitudes = new double[points.size() + 1];
		for (int i = 0; i < points.size(); i++) {
			GeoPointLiteral point = corner(points.get(i), "point " + i);
			latitudes[i] = point.getLat();
			longitudes[i] = point.getLon();
		}
		latitudes[points.size()] = latitudes[0];
		longitudes[points.size()] = longitudes[0];
		try {
			return LatLonPoint.newPolygonQuery(field.name(), new Polygon(latitudes, longitudes));
		} catch (IllegalArgumentException e) {
			throw new QueryException("The polygon for geo field '" + field.name() + "' is not one Lucene "
					+ "can match: " + e.getMessage(), e);
		}
	}

	/**
	 * A distance compared against a threshold (§5.5). Only the bounded side is a query:
	 * {@code <=} is Lucene's own distance query, and {@code >} is its negation with the
	 * existence guard every other negation here carries — a document without a position is
	 * UNKNOWN, not "far away".
	 * <p>
	 * {@code <} is served as {@code <=}. The two differ only for a point sitting exactly on
	 * the radius, and the vocabulary itself declares distances only to within the G5 band
	 * (1e-3 relative above a metre) — below which Lucene's own encoding already rounds. What
	 * is refused instead is {@code =} and {@code !=}: those are measure-zero on a continuum,
	 * where no tolerance argument saves the answer.
	 */
	private Query geoDistance(Comparison comparison, boolean negated) throws QueryException {
		Expression left = comparison.getLeft();
		Expression right = comparison.getRight();
		ComparisonOperator operator = comparison.getOperator();
		GeoDistance distance;
		Expression threshold;
		if (left instanceof GeoDistance leftDistance) {
			distance = leftDistance;
			threshold = right;
		} else {
			// `500 >= geoDistance(...)` is `geoDistance(...) <= 500`.
			distance = (GeoDistance) right;
			threshold = left;
			operator = mirror(operator);
		}
		if (threshold instanceof GeoDistance) {
			throw new QueryException("Comparing two distances is not something the index can answer — "
					+ "neither side is a stored value.");
		}
		if (negated) {
			operator = invert(operator);
		}
		IndexSchema.GeoField field = GeoSubjects.resolve(schema, context.rootEClass(),
				distance.getSubject(), "geoDistance");
		GeoPointLiteral point = corner(distance.getPoint(), "point");
		Object value = value(threshold, null);
		if (!(value instanceof Number radius)) {
			throw new QueryException("The distance threshold for geo field '" + field.name()
					+ "' is not a number but " + (value == null ? "null" : value.getClass().getSimpleName())
					+ ". Distances are compared in metres.");
		}
		Query within;
		try {
			within = LatLonPoint.newDistanceQuery(field.name(), point.getLat(), point.getLon(),
					radius.doubleValue());
		} catch (IllegalArgumentException e) {
			throw new QueryException("The distance query for geo field '" + field.name()
					+ "' is not one Lucene can match: " + e.getMessage(), e);
		}
		return switch (operator) {
			case LE, LT -> within;
			case GE, GT -> geoGuarded(field, within);
			case EQ, NE -> throw new QueryException("A distance is a continuous measure, so '" + operator
					+ "' against " + radius + " m asks which documents sit exactly on a circle — a "
					+ "measure-zero comparison no backend can answer honestly (the same refusal the "
					+ "mongo backend makes). Compare with <= or >= instead.");
		};
	}

	/** Negation over a geo predicate: UNKNOWN for a document that has no position (§5.5 rule 2). */
	private Query geoGuarded(IndexSchema.GeoField field, Query positive) {
		return new BooleanQuery.Builder()
				.add(geoExistsQuery(field), BooleanClause.Occur.MUST)
				.add(positive, BooleanClause.Occur.MUST_NOT)
				.build();
	}

	/**
	 * Documents that have a position at all. With doc values that is the cheap probe; a
	 * point-only geo field has nothing {@link FieldExistsQuery} can read, so the probe is
	 * the whole earth — every position is inside it, and only a document with one matches.
	 */
	private Query geoExistsQuery(IndexSchema.GeoField field) {
		if (field.docValues()) {
			return new FieldExistsQuery(field.name());
		}
		return LatLonPoint.newBoxQuery(field.name(), -90, 90, -180, 180);
	}

	private static GeoPointLiteral corner(GeoPointLiteral point, String what) throws QueryException {
		if (point == null) {
			throw new QueryException("A geo shape is missing its " + what + ".");
		}
		return point;
	}

	// --- quantifiers ----------------------------------------------------------------------

	/**
	 * EXISTS/FOR_ALL over a {@code NESTED} reference, through the index-time block join
	 * (docs/search-access.md §5.2) — the one join this backend offers, because parent and
	 * children were written as one block and the "join" is a fact of the index, not of the
	 * query.
	 * <p>
	 * The four faces reduce to two shapes, mirroring the Mongo backend's
	 * {@code $elemMatch}/{@code $nor} recipe. With {@code inner} translated in child scope
	 * under the same negation flag: EXISTS and ¬FOR_ALL ask for <em>a child that matches
	 * inner</em> — one {@code ToParentBlockJoinQuery}. FOR_ALL and ¬EXISTS ask for <em>no
	 * child escaping inner</em> — root documents minus a block join over the escaping
	 * children. That last shape is where the §5.1 duality lives: the {@code MUST_NOT} is
	 * guarded by the root filter as its positive clause, so a parent with no children at
	 * all stays a match for FOR_ALL (vacuously true) and for ¬EXISTS, while a child whose
	 * predicate is UNKNOWN escapes both — not TRUE blocks FOR_ALL, not FALSE blocks
	 * ¬EXISTS.
	 */
	private Query quantifier(Quantifier quantifier, boolean negated) throws QueryException {
		if (scope != null) {
			throw new QueryException("A quantifier inside a quantifier asks about children of children, "
					+ "and a block is one level deep: the mapper indexes a NESTED child's attributes, not "
					+ "its own references. Restructure the mapping or route to the primary store.");
		}
		EReference reference = nestedReference(quantifier);
		QueryTranslator child = new QueryTranslator(schema,
				QueryContexts.of(reference.getEReferenceType(), context.converter(), context.parameters(),
						context.options()),
				analyzer, quantifier.getVariable());
		Query inner = child.translate(quantifier.getPredicate(), negated);
		Query childScope = new TermQuery(new Term(SearchFields.NESTED, reference.getName()));
		boolean someChildMatches = (quantifier instanceof Exists) != negated;
		if (someChildMatches) {
			return new ToParentBlockJoinQuery(new BooleanQuery.Builder()
					.add(childScope, BooleanClause.Occur.FILTER)
					.add(inner, BooleanClause.Occur.MUST)
					.build(), PARENT_FILTER, ScoreMode.None);
		}
		Query escaping = new BooleanQuery.Builder()
				.add(childScope, BooleanClause.Occur.FILTER)
				.add(inner, BooleanClause.Occur.MUST_NOT)
				.build();
		return new BooleanQuery.Builder()
				.add(rootFilter(), BooleanClause.Occur.MUST)
				.add(new ToParentBlockJoinQuery(escaping, PARENT_FILTER, ScoreMode.None),
						BooleanClause.Occur.MUST_NOT)
				.build();
	}

	/**
	 * The NESTED reference a quantifier ranges over; everything else is refused by name.
	 * Shared with {@code LuceneQueryProcessor.validate}, so validation and translation
	 * cannot disagree about what a quantifier may quantify.
	 */
	static EReference nestedReference(IndexSchema schema, EClass root, Quantifier quantifier)
			throws QueryException {
		if (!(quantifier.getSource() instanceof PropertyPath source) || source.getSegments().isEmpty()) {
			throw new QueryException("A quantifier needs a reference path as its source");
		}
		if (source.getSegments().size() > 1) {
			throw new QueryException("The quantifier source '" + pathName(source) + "' navigates more than "
					+ "one step. A block holds the root's own children; anything deeper lives in another "
					+ "document, and following it would be the query-time join this backend refuses (§5.2).");
		}
		if (!(source.getSegments().get(0) instanceof EReference reference)) {
			throw new QueryException("The quantifier source '" + pathName(source) + "' is an attribute. "
					+ "Quantifying over the values of one attribute is COLLECTION_COUNT territory, which "
					+ "is not declared; over children it needs a reference.");
		}
		ReferenceMapping referenceMapping = schema.referenceMapping(root, reference);
		if (referenceMapping == null || referenceMapping.getStrategy() != ReferenceStrategy.NESTED) {
			throw new QueryException("EXISTS/FOR_ALL over '" + reference.getName() + "' needs the reference "
					+ "mapped NESTED, and it is "
					+ (referenceMapping == null ? "not mapped" : "mapped " + referenceMapping.getStrategy())
					+ ". EMBED flattens children into parallel values and loses which child carried which — "
					+ "two different children could satisfy the two halves of a predicate; ID_ONLY keeps no "
					+ "child values at all.");
		}
		return reference;
	}

	private EReference nestedReference(Quantifier quantifier) throws QueryException {
		return nestedReference(schema, context.rootEClass(), quantifier);
	}

	// --- type checks ----------------------------------------------------------------------

	/**
	 * The discriminator holds the concrete class name, so a check against a supertype
	 * expands to every indexed concrete subtype — otherwise it would silently miss them.
	 */
	private Query typeCheck(TypeCheck typeCheck, boolean negated) throws QueryException {
		if (typeCheck.getType() == null) {
			throw new QueryException("A type check without a type cannot be translated");
		}
		if (typeCheck.getSource() != null && !typeCheck.getSource().getSegments().isEmpty()) {
			throw new QueryException("A type check on the path '"
					+ pathName(typeCheck.getSource()) + "' asks about the type of a referenced object, "
					+ "which lives in another document. Only the root type is checkable here.");
		}
		Query positive = typeFilterOf(typeCheck.getType());
		return negated
				? new BooleanQuery.Builder()
						.add(MatchAllDocsQuery.INSTANCE, BooleanClause.Occur.MUST)
						.add(positive, BooleanClause.Occur.MUST_NOT)
						.build()
				: positive;
	}


	/** The schema's type filter, with its refusal spoken in query vocabulary. */
	private Query typeFilterOf(EClass type) throws QueryException {
		try {
			return schema.typeFilter(type);
		} catch (MappingException e) {
			throw new QueryException(e.getMessage(), e);
		}
	}

	// --- guards and helpers ---------------------------------------------------------------

	/** {@code exists(field) AND NOT positive} — the three-valued reading of a negated leaf. */
	private Query guarded(IndexSchema.Field field, Query positive) {
		return new BooleanQuery.Builder()
				.add(existsQuery(field), BooleanClause.Occur.MUST)
				.add(positive, BooleanClause.Occur.MUST_NOT)
				.build();
	}

	/**
	 * Documents that have a value for the field.
	 * <p>
	 * {@link FieldExistsQuery} reads norms, doc values, points or vectors — none of which a
	 * plain keyword field has, since {@code StringField} omits norms and doc values are
	 * declared per mapping. For those the existence probe is an unbounded term range, which
	 * matches exactly the documents carrying any term for the field. It enumerates terms and
	 * is therefore the slower form, so it is used only where the cheap one has no signal to
	 * read.
	 */
	private Query existsQuery(IndexSchema.Field field) {
		if (field.docValues()) {
			return new FieldExistsQuery(field.name());
		}
		if (field.kind() == FieldKind.NUMERIC) {
			// A point field without doc values indexes no structure FieldExistsQuery can
			// read — it wants doc values, norms or vectors, and a BKD tree is none of them.
			// An unbounded range over the same points matches exactly the documents that
			// have one.
			return allPointsQuery(field);
		}
		return new TermRangeQuery(field.name(), null, null, true, true);
	}

	/** Every document carrying a value for a point field: the unbounded range over it. */
	private Query allPointsQuery(IndexSchema.Field field) {
		return switch (field.numericKind()) {
			case INT -> IntPoint.newRangeQuery(field.name(), Integer.MIN_VALUE, Integer.MAX_VALUE);
			case LONG, DATE -> LongPoint.newRangeQuery(field.name(), Long.MIN_VALUE, Long.MAX_VALUE);
			case FLOAT -> FloatPoint.newRangeQuery(field.name(), Float.NEGATIVE_INFINITY,
					Float.POSITIVE_INFINITY);
			case DOUBLE -> DoublePoint.newRangeQuery(field.name(), Double.NEGATIVE_INFINITY,
					Double.POSITIVE_INFINITY);
			default -> new FieldExistsQuery(field.name());
		};
	}

	/** Documents that have no value for the field — what {@code IsNull} asks for. */
	private Query missingQuery(IndexSchema.Field field) {
		return new BooleanQuery.Builder()
				.add(MatchAllDocsQuery.INSTANCE, BooleanClause.Occur.MUST)
				.add(existsQuery(field), BooleanClause.Occur.MUST_NOT)
				.build();
	}

	/** Resolves a path to its primary projection. */
	IndexSchema.Field path(Expression expression) throws QueryException {
		return path(expression, null);
	}

	/**
	 * Resolves a path to the projection that serves {@code use} (#39): an equality asks for an
	 * exact field, a comparison for a range one, and an attribute that declares a sub-field
	 * beside its primary projection is answered from whichever of the two says it can. Where
	 * no projection claims the use — the ordinary mapping with one field per attribute — the
	 * primary answers, exactly as before.
	 */
	IndexSchema.Field path(Expression expression, FieldUse use) throws QueryException {
		if (!(expression instanceof PropertyPath propertyPath)) {
			throw new QueryException("Expected a feature path but found " + name(expression));
		}
		if (scope == null && propertyPath.getBase() != null) {
			throw new QueryException("The path '" + pathName(propertyPath) + "' is scoped to an iterator "
					+ "variable, but no quantifier encloses it here.");
		}
		if (scope != null && propertyPath.getBase() == null) {
			throw new QueryException("The path '" + pathName(propertyPath) + "' reaches back to the root "
					+ "object from inside a quantifier over a NESTED block. A correlated predicate compares "
					+ "across two documents, which is the query-time join this backend refuses (§5.2).");
		}
		if (scope != null && propertyPath.getBase() != scope) {
			throw new QueryException("The path '" + pathName(propertyPath) + "' is scoped to a different "
					+ "iterator variable than the enclosing quantifier's — nested scopes end at the block "
					+ "boundary.");
		}
		try {
			return schema.resolve(context.rootEClass(), propertyPath.getSegments(), use);
		} catch (MappingException e) {
			throw new QueryException(e.getMessage(), e);
		}
	}

	private Object value(Expression expression, EAttribute target) throws QueryException {
		if (expression == null) {
			return null;
		}
		Map<String, Object> parameters = context.parameters() == null ? Map.of() : context.parameters();
		return ExpressionValues.resolve(expression, target, parameters, context.converter());
	}

	/** The term text of a value, encoded exactly as the mapper wrote it. */
	private String string(IndexSchema.Field field, Object value) throws QueryException {
		if (value == null) {
			throw new QueryException("A null value cannot be compared as a term on field '" + field.name()
					+ "'; use IsNull for that.");
		}
		EStructuralFeature target = field.attribute();
		if (target instanceof EAttribute attribute) {
			return org.eclipse.emf.ecore.util.EcoreUtil.convertToString(attribute.getEAttributeType(), value);
		}
		return value.toString();
	}

	private Number number(IndexSchema.Field field, Object value) throws QueryException {
		if (value instanceof Date date) {
			return date.getTime();
		}
		if (value instanceof Number number) {
			return number;
		}
		if (value == null) {
			throw new QueryException("A null value cannot be compared numerically on field '" + field.name()
					+ "'; use IsNull for that.");
		}
		throw new QueryException("Value '" + value + "' cannot be compared against the numeric field '"
				+ field.name() + "'");
	}

	private static int addExact(int value, int delta) throws QueryException {
		if (value == Integer.MIN_VALUE && delta < 0 || value == Integer.MAX_VALUE && delta > 0) {
			throw new QueryException("An exclusive bound at the edge of the value range has no "
					+ "representable neighbour");
		}
		return value + delta;
	}

	private static long addExact(long value, long delta) throws QueryException {
		if (value == Long.MIN_VALUE && delta < 0 || value == Long.MAX_VALUE && delta > 0) {
			throw new QueryException("An exclusive bound at the edge of the value range has no "
					+ "representable neighbour");
		}
		return value + delta;
	}

	private static ComparisonOperator mirror(ComparisonOperator operator) {
		return switch (operator) {
			case EQ -> ComparisonOperator.EQ;
			case NE -> ComparisonOperator.NE;
			case LT -> ComparisonOperator.GT;
			case LE -> ComparisonOperator.GE;
			case GT -> ComparisonOperator.LT;
			case GE -> ComparisonOperator.LE;
		};
	}

	/**
	 * The inverse operator, which is what makes the push-down three-valued: every result
	 * form here is itself a query that only matches documents having a value.
	 */
	private static ComparisonOperator invert(ComparisonOperator operator) {
		return switch (operator) {
			case EQ -> ComparisonOperator.NE;
			case NE -> ComparisonOperator.EQ;
			case LT -> ComparisonOperator.GE;
			case LE -> ComparisonOperator.GT;
			case GT -> ComparisonOperator.LE;
			case GE -> ComparisonOperator.LT;
		};
	}

	private static String regexOf(StringMatchKind kind, String pattern) {
		String quoted = quoteRegex(pattern);
		return switch (kind) {
			case CONTAINS -> ".*" + quoted + ".*";
			case STARTS_WITH -> quoted + ".*";
			case ENDS_WITH -> ".*" + quoted;
			case LIKE -> likeToRegex(pattern);
			case FUZZY -> throw new IllegalStateException(FUZZY_HAS_NO_REGEXP);
		};
	}

	/** SQL {@code LIKE}: {@code %} is any run, {@code _} a single character. */
	static String likeToRegex(String pattern) {
		StringBuilder regex = new StringBuilder();
		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			switch (c) {
				case '%' -> regex.append(".*");
				case '_' -> regex.append('.');
				default -> appendQuoted(regex, c);
			}
		}
		return regex.toString();
	}

	private static String quoteRegex(String literal) {
		StringBuilder quoted = new StringBuilder(literal.length());
		for (int i = 0; i < literal.length(); i++) {
			appendQuoted(quoted, literal.charAt(i));
		}
		return quoted.toString();
	}

	private static void appendQuoted(StringBuilder target, char c) {
		if ("\\.[]{}()*+?^$|<>&~\"#@".indexOf(c) >= 0) {
			target.append('\\');
		}
		target.append(c);
	}

	private static String escapeWildcard(String literal) {
		StringBuilder escaped = new StringBuilder(literal.length());
		for (int i = 0; i < literal.length(); i++) {
			char c = literal.charAt(i);
			if (c == '*' || c == '?' || c == '\\') {
				escaped.append('\\');
			}
			escaped.append(c);
		}
		return escaped.toString();
	}

	private static String pathName(PropertyPath path) {
		List<String> names = new ArrayList<>();
		for (EStructuralFeature segment : path.getSegments()) {
			names.add(segment.getName());
		}
		return String.join(".", names);
	}

	private static String name(Expression expression) {
		return expression == null ? "nothing" : expression.eClass().getName();
	}

	/** The root-document marker every plan filters on, so block children never count. */
	static Query rootFilter() {
		return SearchFields.rootFilter();
	}
}
