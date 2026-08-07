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
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldExistsQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.automaton.Operations;
import org.apache.lucene.util.automaton.RegExp;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.model.expression.Arithmetic;
import org.eclipse.fennec.model.expression.Between;
import org.eclipse.fennec.model.expression.CollectionCount;
import org.eclipse.fennec.model.expression.Comparison;
import org.eclipse.fennec.model.expression.ComparisonOperator;
import org.eclipse.fennec.model.expression.Expression;
import org.eclipse.fennec.model.expression.GeoDistance;
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
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.api.QueryContext;
import org.eclipse.fennec.persistence.query.expr.ExpressionValues;
import org.eclipse.fennec.search.esearch.NumericKind;
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

	private final IndexSchema schema;
	private final QueryContext context;
	private final Analyzer analyzer;

	QueryTranslator(IndexSchema schema, QueryContext context, Analyzer analyzer) {
		this.schema = schema;
		this.context = context;
		this.analyzer = analyzer;
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
		if (expression instanceof Quantifier) {
			throw new QueryException("EXISTS/FOR_ALL over a reference needs the index-time block join, "
					+ "which is task S11 (issue #9). Until then quantifiers are not declared and this "
					+ "query is refused rather than answered on flattened values, where two different "
					+ "children could satisfy the two halves of a predicate.");
		}
		if (expression instanceof Score) {
			throw new QueryException("Relevance score as query vocabulary is task S6 (issue #10) and not "
					+ "declared yet.");
		}
		if (expression instanceof GeoWithin || expression instanceof GeoDistance) {
			throw new QueryException("Geo predicates are task S9 (issue #13) — the vocabulary exists "
					+ "upstream, the Lucene translation does not yet.");
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
		IndexSchema.Field field = path(subject);
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
		IndexSchema.Field field = path(between.getSource());
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
		IndexSchema.Field field = path(in.getSource());
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
		IndexSchema.Field field = path(match.getSource());
		Object patternValue = value(match.getPattern(), field.attribute());
		if (patternValue == null) {
			// A null pattern is UNKNOWN, and NOT UNKNOWN stays UNKNOWN: no document matches.
			return new BooleanQuery.Builder().build();
		}
		String pattern = patternValue.toString();
		StringMatchKind kind = match.getKind() == null ? StringMatchKind.CONTAINS : match.getKind();
		Query positive = field.kind() == FieldKind.TEXT
				? analyzedMatch(field, kind, pattern, match.isCaseInsensitive())
				: keywordMatch(field, kind, pattern, match.isCaseInsensitive());
		return negated ? guarded(field, positive) : positive;
	}

	private Query keywordMatch(IndexSchema.Field field, StringMatchKind kind, String pattern,
			boolean caseInsensitive) throws QueryException {
		if (field.kind() == FieldKind.NUMERIC) {
			throw new QueryException("Field '" + field.name() + "' is numeric, so the string match "
					+ kind + " has nothing to match against.");
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
		};
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
		Query positive = typeFilter(typeCheck.getType());
		return negated
				? new BooleanQuery.Builder()
						.add(MatchAllDocsQuery.INSTANCE, BooleanClause.Occur.MUST)
						.add(positive, BooleanClause.Occur.MUST_NOT)
						.build()
				: positive;
	}

	/** A term set over the discriminator values of a class and its indexed concrete subtypes. */
	Query typeFilter(EClass type) throws QueryException {
		TreeSet<BytesRef> names = new TreeSet<>();
		if (!type.isAbstract() && !type.isInterface()) {
			names.add(new BytesRef(schema.typeNameOf(type)));
		}
		for (EClassifier classifier : schema.mapping().getEPackage().getEClassifiers()) {
			if (classifier instanceof EClass candidate && candidate != type && !candidate.isAbstract()
					&& !candidate.isInterface() && type.isSuperTypeOf(candidate)) {
				names.add(new BytesRef(schema.typeNameOf(candidate)));
			}
		}
		if (names.isEmpty()) {
			throw new QueryException("No indexed concrete class matches type " + type.getName()
					+ ", so a query from it can never have a hit. Refused rather than silently empty.");
		}
		return new TermInSetQuery(schema.typeField(), new ArrayList<>(names));
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
		if (field.kind() == FieldKind.NUMERIC || field.docValues()) {
			return new FieldExistsQuery(field.name());
		}
		return new TermRangeQuery(field.name(), null, null, true, true);
	}

	/** Documents that have no value for the field — what {@code IsNull} asks for. */
	private Query missingQuery(IndexSchema.Field field) {
		return new BooleanQuery.Builder()
				.add(MatchAllDocsQuery.INSTANCE, BooleanClause.Occur.MUST)
				.add(existsQuery(field), BooleanClause.Occur.MUST_NOT)
				.build();
	}

	/** Resolves a path to a field, turning a mapping problem into a query refusal. */
	IndexSchema.Field path(Expression expression) throws QueryException {
		if (!(expression instanceof PropertyPath propertyPath)) {
			throw new QueryException("Expected a feature path but found " + name(expression));
		}
		try {
			return schema.resolve(context.rootEClass(), propertyPath.getSegments());
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
		return new TermQuery(new Term(SearchFields.PARENT, SearchFields.PARENT_VALUE));
	}
}
