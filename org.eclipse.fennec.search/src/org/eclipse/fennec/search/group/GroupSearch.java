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
package org.eclipse.fennec.search.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldExistsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.util.BytesRef;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.persistence.api.ConverterService;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.support.QueryContexts;
import org.eclipse.fennec.search.group.GroupResults.Group;
import org.eclipse.fennec.search.mapping.DocumentReader;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappingException;
import org.eclipse.fennec.search.query.LuceneQueryPlan;
import org.eclipse.fennec.search.query.LuceneQueryProcessor;
import org.eclipse.fennec.search.unit.IndexUnit;

/**
 * Grouping with representatives (S19, #21): the top documents <em>of each group</em>, which
 * is the shape facets cannot answer — a facet counts values, this returns objects.
 * <p>
 * <b>Why this is an own API and not a query.</b> "Top-N per group" is a result shape, and
 * the shared pipeline vocabulary has none for it: a {@code GroupByStage} produces one row of
 * key and aggregates per group, never documents. The shape is reserved upstream as a later
 * {@code BottomTop} stage, and inventing it here would be exactly the search-only vocabulary
 * §3 rules out. So grouping takes the road facets, suggest, highlighting and similarity
 * already take — an API of this backend's own, with the canonical {@link
 * org.eclipse.fennec.model.query.Query} as its base, so predicates, quantifiers, parameters
 * and every refusal behave identically to the query path. When the IR grows the stage, the
 * translator can serve it from the same machinery.
 * <p>
 * Groups are ordered by their best hit and representatives within a group by relevance, so
 * "the best three of every manufacturer" reads in the order it is meant. Objects come back
 * through the same reconstruction as every other read (docs/search-access.md §4.3), block
 * children included.
 *
 * @author Data In Motion Consulting
 */
public final class GroupSearch {

	private final IndexUnit unit;
	private final IndexSchema schema;
	private final LuceneQueryProcessor processor;
	private final ConverterService converter;

	private GroupSearch(IndexUnit unit, IndexSchema schema, ConverterService converter) {
		this.unit = unit;
		this.schema = schema;
		this.processor = LuceneQueryProcessor.of(schema, unit.config().analyzers().defaultAnalyzer());
		this.converter = converter;
	}

	/** Grouping for one unit and its schema. */
	public static GroupSearch of(IndexUnit unit, IndexSchema schema) {
		return of(unit, schema, null);
	}

	/** As {@link #of(IndexUnit, IndexSchema)}, with a converter for parameter values. */
	public static GroupSearch of(IndexUnit unit, IndexSchema schema, ConverterService converter) {
		Objects.requireNonNull(unit, "unit");
		Objects.requireNonNull(schema, "schema");
		return new GroupSearch(unit, schema, converter);
	}

	/**
	 * Groups the base query's matches and brings back the representatives of each group.
	 *
	 * @throws QueryException if the base query is refused, or the attribute cannot carry a
	 *         group key
	 */
	public GroupResults search(GroupRequest request) throws IOException, QueryException {
		Objects.requireNonNull(request, "request");
		if (request.by() == null) {
			throw new QueryException("The request names no attribute to group by. Grouping without a key "
					+ "is what an ordinary query already answers.");
		}
		EClass root = request.query().getFrom();
		if (root == null) {
			throw new QueryException("The base query names no root type, so there is nothing to group.");
		}
		String field = groupField(root, request.by());
		LuceneQueryPlan plan = (LuceneQueryPlan) processor.translate(request.query(),
				QueryContexts.of(root, converter, request.parameters(), null));
		DocumentReader reader = DocumentReader.of(schema);
		// Only documents that have the key take part. Lucene would otherwise collect the rest
		// into a group whose value is null — and count it, since the all-groups collector reads
		// values rather than the selector's verdict. "Everything without a manufacturer" is not
		// a group anyone asked for, so it is filtered out of the match set instead of explained
		// away afterwards. The filter reads the same doc values the grouping does.
		Query grouped = new BooleanQuery.Builder()
				.add(plan.query(), Occur.MUST)
				.add(new FieldExistsQuery(field), Occur.FILTER)
				.build();
		return unit.search(searcher -> {
			GroupingSearch grouping = new GroupingSearch(new KeywordGroupSelector(field));
			grouping.setGroupSort(Sort.RELEVANCE);
			grouping.setSortWithinGroup(Sort.RELEVANCE);
			grouping.setGroupDocsLimit(request.representatives());
			grouping.setIgnoreDocsWithoutGroupField(true);
			// The total group count is the one number a caller cannot recompute from a
			// truncated answer, and it is what tells them the answer is truncated at all.
			grouping.setAllGroups(true);
			TopGroups<BytesRef> found = grouping.search(searcher, grouped, 0, request.topGroups());
			if (found == null || found.groups.length == 0) {
				return new GroupResults(List.of(), 0);
			}
			StoredFields stored = searcher.storedFields();
			List<Group> groups = new ArrayList<>(found.groups.length);
			for (GroupDocs<BytesRef> group : found.groups) {
				if (group.groupValue() == null) {
					// Documents without a value form no group; the selector skips them, and
					// this is only the belt to the braces.
					continue;
				}
				List<EObject> representatives = new ArrayList<>(group.scoreDocs().length);
				for (ScoreDoc hit : group.scoreDocs()) {
					Document document = stored.document(hit.doc);
					representatives.add(reader.read(document,
							DocumentReader.blockChildren(searcher, stored, document)));
				}
				groups.add(new Group(group.groupValue().utf8ToString(), group.totalHits().value(),
						representatives));
			}
			long total = found.totalGroupCount == null ? groups.size() : found.totalGroupCount;
			return new GroupResults(groups, total);
		});
	}

	/**
	 * The field a group key reads, refused by name when it cannot carry one. Grouping reads
	 * doc values of one term per document, so the key has to be a keyword projection with
	 * doc values, and the attribute single-valued.
	 */
	private String groupField(EClass root, EAttribute attribute) throws QueryException {
		if (attribute.isMany()) {
			throw new QueryException("Attribute '" + attribute.getName() + "' is many-valued, so an "
					+ "object would belong to several groups at once. Grouping needs one key per object "
					+ "— group by a single-valued attribute, or count the values with a facet.");
		}
		IndexSchema.Field field;
		try {
			field = schema.resolve(root, attribute);
		} catch (MappingException e) {
			throw new QueryException(e.getMessage(), e);
		}
		if (field.kind() != IndexSchema.FieldKind.KEYWORD) {
			throw new QueryException("Field '" + field.name() + "' is a " + field.kind() + " field and "
					+ "cannot form group keys: grouping reads one exact term per document. Group by a "
					+ "keyword projection of the attribute — analyzed text has no single value, and a "
					+ "numeric one would need ranges nobody declared.");
		}
		if (!field.docValues()) {
			throw new QueryException("Field '" + field.name() + "' carries no doc values, and grouping "
					+ "reads the group key from them. Declare docValues=\"true\" on the mapping.");
		}
		return field.name();
	}
}
