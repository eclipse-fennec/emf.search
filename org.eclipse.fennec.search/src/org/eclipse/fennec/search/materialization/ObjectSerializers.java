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
package org.eclipse.fennec.search.materialization;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The serializers a unit can materialize through, keyed by format id.
 * <p>
 * Plain-Java constructed and immutable — the OSGi layer builds one from whiteboard
 * services and hands it in, exactly like analyzers; the core never looks anything up
 * dynamically (docs/search-access.md §2.2). An unknown format is refused by name, never
 * silently replaced by the default: a mapping that says {@code format="json"} means json,
 * and falling back to binary would store something other than what it says.
 *
 * @author Data In Motion Consulting
 */
public final class ObjectSerializers {

	private final Map<String, ObjectSerializer> byFormat;

	private ObjectSerializers(Map<String, ObjectSerializer> byFormat) {
		this.byFormat = byFormat;
	}

	/** The backend defaults: EMF binary. */
	public static ObjectSerializers withDefaults() {
		return of(new EmfBinaryObjectSerializer());
	}

	/** A registry over exactly these serializers; the last one wins a format-id collision. */
	public static ObjectSerializers of(ObjectSerializer... serializers) {
		Map<String, ObjectSerializer> byFormat = new LinkedHashMap<>();
		for (ObjectSerializer serializer : serializers) {
			Objects.requireNonNull(serializer, "serializer");
			byFormat.put(serializer.format(), serializer);
		}
		return new ObjectSerializers(Map.copyOf(byFormat));
	}

	/**
	 * The serializer behind a mapping's {@code format} declaration; null or blank selects
	 * the backend default, {@value EmfBinaryObjectSerializer#FORMAT}.
	 *
	 * @throws IllegalArgumentException if no serializer serves the format — refused by
	 *         name so a typo in a mapping surfaces at the first write, not as unreadable
	 *         stored objects later
	 */
	public ObjectSerializer forFormat(String format) {
		String effective = format == null || format.isBlank() ? EmfBinaryObjectSerializer.FORMAT : format;
		ObjectSerializer serializer = byFormat.get(effective);
		if (serializer == null) {
			throw new IllegalArgumentException("No ObjectSerializer serves the format '" + effective
					+ "'; available: " + byFormat.keySet() + ". A mapping that names a format means it — "
					+ "storing another format instead would invalidate every object silently.");
		}
		return serializer;
	}
}
