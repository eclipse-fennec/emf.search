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
package org.eclipse.fennec.search.mapping;

import java.util.Objects;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedSetSelector;
import org.apache.lucene.search.SortedSetSortField;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.fennec.search.esearch.IndexSort;
import org.eclipse.fennec.search.esearch.SortEntry;

/**
 * Derives the physical index order a mapping declares ({@code IndexUnitMapping.sort},
 * S17/#19) into the Lucene {@link Sort} an {@code IndexUnitConfig} takes — declared
 * rather than configured, because the order is fixed at index creation: changing it is a
 * rebuild, exactly like every other shape-relevant mapping change.
 * <p>
 * Validation happens here, at derivation time: an index sort can only run over
 * single-valued fields that carry doc values on <em>every</em> document, so a
 * multi-valued attribute, an analyzed text field or a field without doc values refuses by
 * name instead of failing at the first flush. Missing values are pinned explicitly
 * ({@code missingLast}), because a sorted index with unspecified absence ordering answers
 * paging differently per segment.
 *
 * @author Data In Motion Consulting
 */
public final class IndexOrders {

	private IndexOrders() {
	}

	/** The declared index order of a schema's mapping, or null when it declares none. */
	public static Sort indexSort(IndexSchema schema) {
		Objects.requireNonNull(schema, "schema");
		IndexSort declared = schema.mapping().getSort();
		if (declared == null || declared.getEntries().isEmpty()) {
			return null;
		}
		SortField[] fields = new SortField[declared.getEntries().size()];
		for (int i = 0; i < fields.length; i++) {
			fields[i] = sortField(schema, declared.getEntries().get(i));
		}
		return new Sort(fields);
	}

	private static SortField sortField(IndexSchema schema, SortEntry entry) {
		EAttribute attribute = entry.getFeature();
		if (attribute == null) {
			throw new MappingException("An index sort entry declares no feature — there is nothing to "
					+ "order by.");
		}
		if (attribute.isMany()) {
			throw new MappingException("Index sort over '" + attribute.getName() + "' is refused: the "
					+ "attribute is multi-valued, and a physical document order needs exactly one value "
					+ "per document.");
		}
		IndexSchema.Field field = schema.resolve(attribute.getEContainingClass(), attribute);
		if (field.kind() == IndexSchema.FieldKind.TEXT) {
			throw new MappingException("Index sort over '" + field.name() + "' is refused: an analyzed "
					+ "text field would order by its tokens, not by its value. Declare a keyword field "
					+ "for '" + attribute.getName() + "'.");
		}
		if (!field.docValues()) {
			throw new MappingException("Index sort over '" + field.name() + "' needs doc values on every "
					+ "document. Declare docValues=true for '" + attribute.getName() + "'.");
		}
		boolean descending = entry.isDescending();
		boolean missingLast = entry.isMissingLast();
		if (field.kind() == IndexSchema.FieldKind.KEYWORD) {
			return new SortedSetSortField(field.name(), descending, SortedSetSelector.Type.MIN,
					missingLast ? SortField.STRING_LAST : SortField.STRING_FIRST);
		}
		return numericSortField(field, descending, missingLast);
	}

	private static SortField numericSortField(IndexSchema.Field field, boolean descending,
			boolean missingLast) {
		// "missing last" means last in the delivered order, whatever the direction — so the
		// sentinel flips with it: ascending-last is the maximum, descending-last the minimum.
		boolean missingIsMax = missingLast != descending;
		return switch (field.numericKind()) {
			case INT -> new SortField(field.name(), SortField.Type.INT, descending,
					missingIsMax ? Integer.MAX_VALUE : Integer.MIN_VALUE);
			case LONG, DATE -> new SortField(field.name(), SortField.Type.LONG, descending,
					missingIsMax ? Long.MAX_VALUE : Long.MIN_VALUE);
			case FLOAT -> new SortField(field.name(), SortField.Type.FLOAT, descending,
					missingIsMax ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY);
			case DOUBLE -> new SortField(field.name(), SortField.Type.DOUBLE, descending,
					missingIsMax ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
			default -> throw new MappingException("Numeric kind " + field.numericKind()
					+ " has no index sort encoding");
		};
	}
}
