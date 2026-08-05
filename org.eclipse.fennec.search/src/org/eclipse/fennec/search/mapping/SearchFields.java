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

/**
 * The fields the mapper writes on its own, beside the mapped ones.
 * <p>
 * They are prefixed with an underscore following the codec's {@code _type} convention, and
 * they are what makes a document addressable at all: without an id there is nothing to
 * update or delete, and without a root marker a block cannot be replaced as a whole.
 *
 * @author Data In Motion Consulting
 */
public final class SearchFields {

	/** Type discriminator; the codec's {@code _type} analogue. Overridable per unit. */
	public static final String TYPE = "_type";

	/** This document's own id. */
	public static final String ID = "_id";

	/**
	 * The id of the root document this one belongs to — written on every document,
	 * including a flat one, where it equals {@link #ID}.
	 * <p>
	 * Every delete and every update goes through this field. That is the direct answer to
	 * the trap Lucene sets with {@code updateDocuments}: it deletes by term and then
	 * appends, so a term matching only the parent would leave the previous children behind
	 * as orphans. A root marker on every document of a block makes "replace this object"
	 * one term, whether the object maps to one document or to twenty.
	 */
	public static final String ROOT = "_root";

	/** Marks the root document of a block; the parent filter of a block join selects on it. */
	public static final String PARENT = "_parent";

	/** Value written into {@link #PARENT}. */
	public static final String PARENT_VALUE = "true";

	private SearchFields() {
	}
}
