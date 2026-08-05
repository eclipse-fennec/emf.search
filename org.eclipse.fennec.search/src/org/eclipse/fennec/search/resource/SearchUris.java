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
package org.eclipse.fennec.search.resource;

import java.util.Objects;

import org.eclipse.emf.common.util.URI;

/**
 * The URI shape this backend answers: {@code lucene://<unit>/<type>[/<id>]}.
 * <p>
 * Deliberately the same shape the Mongo backend uses — {@code mongodb://<alias>/<collection>
 * [/<id>]} — with the index unit where the database goes and the type discriminator where
 * the collection goes. A consumer that knows one knows the other, and the scheme keeps
 * naming the engine, as {@code backend=lucene} does on the query processor.
 *
 * @param unit the index unit alias; never {@code null}
 * @param type the type name, or {@code null} when the URI addresses the whole unit
 * @param id   the object id, or {@code null} when the URI addresses a type or the unit
 * @author Data In Motion Consulting
 */
public record SearchUris(String unit, String type, String id) {

	/** URI scheme handled by this backend. */
	public static final String SCHEME = "lucene";

	public SearchUris {
		Objects.requireNonNull(unit, "unit");
	}

	/**
	 * Parses a URI of this backend.
	 *
	 * @throws IllegalArgumentException if the scheme is wrong or no unit is named — both
	 *         are configuration mistakes that are cheaper to catch here than to debug as an
	 *         empty result later
	 */
	public static SearchUris parse(URI uri) {
		Objects.requireNonNull(uri, "uri");
		if (!SCHEME.equals(uri.scheme())) {
			throw new IllegalArgumentException(
					"URI '" + uri + "' is not a " + SCHEME + " URI (expected " + SCHEME + "://<unit>/<type>[/<id>])");
		}
		String unit = uri.authority();
		if (unit == null || unit.isBlank()) {
			throw new IllegalArgumentException(
					"URI '" + uri + "' names no index unit (expected " + SCHEME + "://<unit>/<type>[/<id>])");
		}
		String[] segments = uri.segments();
		String type = segments.length > 0 && !segments[0].isBlank() ? segments[0] : null;
		String id = segments.length > 1 && !segments[1].isBlank() ? segments[1] : null;
		return new SearchUris(unit, type, id);
	}

	/** Whether this URI addresses a single object. */
	public boolean isObject() {
		return id != null;
	}
}
