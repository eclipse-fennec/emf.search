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
package org.eclipse.fennec.search.examples;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.fennec.model.query.builder.Expressions.geoBox;
import static org.eclipse.fennec.model.query.builder.Expressions.geoDistance;
import static org.eclipse.fennec.model.query.builder.Expressions.geoPoint;
import static org.eclipse.fennec.model.query.builder.Expressions.geoSubject;
import static org.eclipse.fennec.model.query.builder.Expressions.geoWithin;
import static org.eclipse.fennec.model.query.builder.Expressions.propertyPath;

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.expression.GeoSubject;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.api.Hit;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.query.IndexSearch;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The geo example (docs: example-geo.md): manufacturers with a position, found by box and
 * by distance — ordinary query IR, no geo API of this backend's own. The mapping is the
 * packed shape in {@code catalog.esearch}: {@code Manufacturer.location} is a GeoJSON-style
 * point object whose {@code coordinates} holds {@code [lon, lat]}.
 */
class GeoExampleTest {

	private ExampleCatalog catalog;
	private IndexUnit unit;
	private IndexSchema schema;

	@BeforeEach
	void indexTheCorpus() throws IOException {
		catalog = ExampleCatalog.load();
		unit = IndexUnit.open(IndexUnitConfig.inMemory("catalog")
				.refresh(RefreshTrigger.manual())
				.build());
		schema = IndexSchema.of(catalog.mapping());
		DocumentMapper mapper = DocumentMapper.of(schema);

		for (EObject manufacturer : List.of(
				manufacturer("m-1", "Jena Roasters", 11.586, 50.927),
				manufacturer("m-2", "Erfurt Kettles", 11.030, 50.980),
				manufacturer("m-3", "Munich Grinders", 11.580, 48.140))) {
			var mapped = mapper.map(manufacturer);
			unit.updateDocuments(mapped.term(), mapped.documents());
		}
		unit.refresh();
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	@Test
	void manufacturersInThuringia() throws Exception {
		List<Hit> hits = IndexSearch.of(unit, schema)
				.search(QueryBuilder.from(catalog.eClass("Manufacturer"))
						.where(geoWithin(location(),
								geoBox(geoPoint(10.5, 50.5), geoPoint(12.5, 51.5))))
						.build());

		assertThat(hits).extracting(hit -> hit.object().eGet(catalog.feature("Manufacturer", "name")))
				.containsExactlyInAnyOrder("Jena Roasters", "Erfurt Kettles");
	}

	@Test
	void manufacturersNearJena() throws Exception {
		// Jena↔Erfurt ≈ 39 km, Jena↔Munich ≈ 310 km: 50 km keeps Thuringia, drops Munich.
		List<Hit> hits = IndexSearch.of(unit, schema)
				.search(QueryBuilder.from(catalog.eClass("Manufacturer"))
						.where(geoDistance(location(), geoPoint(11.586, 50.927)).le(50_000))
						.build());

		assertThat(hits).extracting(hit -> hit.object().eGet(catalog.feature("Manufacturer", "name")))
				.containsExactlyInAnyOrder("Jena Roasters", "Erfurt Kettles");
	}

	// --- helpers ----------------------------------------------------------------------------

	/** The query's geo subject: the same location reference the mapping declares. */
	private GeoSubject location() {
		return geoSubject(propertyPath(catalog.feature("Manufacturer", "location")));
	}

	private EObject manufacturer(String id, String name, double lon, double lat) {
		EObject location = catalog.create("GeoPoint", "coordinates", List.of(lon, lat));
		return catalog.create("Manufacturer", "id", id, "name", name, "location", location);
	}
}
