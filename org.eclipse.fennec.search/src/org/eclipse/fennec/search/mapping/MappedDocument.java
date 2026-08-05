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

import java.util.List;
import java.util.Objects;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;

/**
 * What one EObject becomes: a block of documents plus the term that replaces it.
 * <p>
 * A flat object yields a block of one. A block always ends with the root document, because
 * that is the order a Lucene block join requires — children first, parent last.
 *
 * @param id        the root object's id
 * @param documents the block, children first and the root document last
 * @author Data In Motion Consulting
 */
public record MappedDocument(String id, List<Document> documents) {

	public MappedDocument {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(documents, "documents");
		if (documents.isEmpty()) {
			throw new IllegalArgumentException("A mapped document needs at least the root document");
		}
		documents = List.copyOf(documents);
	}

	/** The root document — the last one, per the block-join layout. */
	public Document root() {
		return documents.get(documents.size() - 1);
	}

	/** Whether this object mapped to more than one document. */
	public boolean isBlock() {
		return documents.size() > 1;
	}

	/**
	 * The term that identifies this object's documents. Passing it to
	 * {@code IndexUnit.updateDocuments} replaces the previous block completely, children
	 * included — see {@link SearchFields#ROOT}.
	 */
	public Term term() {
		return new Term(SearchFields.ROOT, id);
	}
}
