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

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.model.expression.GeoSubject;
import org.eclipse.fennec.model.expression.PropertyPath;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.search.mapping.IndexSchema;

/**
 * Where a query's coordinate binding meets the index (S9, #13).
 * <p>
 * The IR names features — either a latitude/longitude pair or the path to a packed point
 * — while the index knows one field per declared position. Both the predicate translation
 * and the distance sort have to make that same match, and both have to refuse the same way
 * when the mapping declared no such position, which is why it lives here rather than in
 * either of them.
 *
 * @author Data In Motion Consulting
 */
final class GeoSubjects {

	private GeoSubjects() {
	}

	/**
	 * Matches the query's coordinate binding against the geo fields the mapping declared.
	 * <p>
	 * The IR names features, the index names one field per declared position, and this is
	 * where the two meet: a split subject must name the very attributes a split field was
	 * declared over, a packed subject the reference (or the {@code [lon, lat]} attribute)
	 * behind which the point sits. Anything else is a mapping gap, and it refuses by name
	 * rather than guessing which declared position was meant.
	 */
	static IndexSchema.GeoField resolve(IndexSchema schema, EClass root, GeoSubject subject,
			String predicate) throws QueryException {
		if (subject == null) {
			throw new QueryException("A " + predicate + " without a subject names no position.");
		}
		List<IndexSchema.GeoField> declared = schema.geoFields(root);
		if (declared.isEmpty()) {
			throw new QueryException("No geographic point is declared on "
					+ root.getName() + ", so " + predicate + " has no field to read. "
					+ "Declare a GeoPointFieldMapping for it (docs/geo.md).");
		}
		if (subject.getPathPoint() != null) {
			EStructuralFeature packed = single(subject.getPathPoint(), predicate);
			for (IndexSchema.GeoField field : declared) {
				if (field.pointReference() == packed || (field.pointReference() == null
						&& field.coordinates() == packed)) {
					return field;
				}
			}
			throw new QueryException("The packed geo subject '" + packed.getName() + "' is not a declared "
					+ "position on " + root.getName() + ". Declared: " + names(declared)
					+ ".");
		}
		if (subject.getPathLat() == null || subject.getPathLon() == null) {
			throw new QueryException("A geo subject binds either a latitude/longitude pair or a packed "
					+ "point path; this one binds neither.");
		}
		EStructuralFeature latitude = single(subject.getPathLat(), predicate);
		EStructuralFeature longitude = single(subject.getPathLon(), predicate);
		for (IndexSchema.GeoField field : declared) {
			if (field.latitude() == latitude && field.longitude() == longitude) {
				return field;
			}
		}
		throw new QueryException("The coordinate pair '" + latitude.getName() + "'/'" + longitude.getName()
				+ "' is not a declared position on " + root.getName() + ". Declared: "
				+ names(declared) + ".");
	}

	/** A geo binding reaches one feature of the queried class — not through a document boundary. */
	private static EStructuralFeature single(PropertyPath path, String predicate) throws QueryException {
		List<EStructuralFeature> segments = path.getSegments();
		if (segments.size() != 1) {
			throw new QueryException("The geo subject of " + predicate + " reaches through "
					+ segments.size() + " features. A position is declared on the queried class itself, "
					+ "so its binding is a single feature.");
		}
		return segments.get(0);
	}

	private static String names(List<IndexSchema.GeoField> declared) {
		return declared.stream().map(IndexSchema.GeoField::name).toList().toString();
	}
}
