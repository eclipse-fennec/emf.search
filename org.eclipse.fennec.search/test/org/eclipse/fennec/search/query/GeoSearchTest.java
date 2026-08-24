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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.fennec.model.query.builder.Expressions.geoBox;
import static org.eclipse.fennec.model.query.builder.Expressions.geoDistance;
import static org.eclipse.fennec.model.query.builder.Expressions.geoPoint;
import static org.eclipse.fennec.model.query.builder.Expressions.geoPolygon;
import static org.eclipse.fennec.model.query.builder.Expressions.geoSubject;
import static org.eclipse.fennec.model.query.builder.Expressions.geoWithin;
import static org.eclipse.fennec.model.query.builder.Expressions.not;
import static org.eclipse.fennec.model.query.builder.Expressions.propertyPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.expression.Expression;
import org.eclipse.fennec.model.expression.GeoSubject;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.QueryFactory;
import org.eclipse.fennec.model.query.SortDirection;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.api.QueryResult;
import org.eclipse.fennec.persistence.query.memory.MemoryQueries;
import org.eclipse.fennec.persistence.query.support.QueryContexts;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.GeoPointFieldMapping;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappedDocument;
import org.eclipse.fennec.search.mapping.MappingException;
import org.eclipse.fennec.search.mapping.TestModels;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Geo predicates over Lucene (§5.5, S9/#13) — the place where this backend has the easiest
 * job of the three, because the mapper resolves all three authoring shapes into one
 * {@code LatLonPoint} and box, polygon and distance then read the same field.
 * <p>
 * The corpus is deliberately the published TCK's: the Thuringian triple that separates a
 * 37 km radius, a row without coordinates that every 3VL case is about, and the Fiji/Samoa
 * pair for the wrap-around box. Where the reference engine has semantics of its own — the
 * polygon of §5.5's first open point — the assertion is a differential against the memory
 * oracle rather than a hand-written expectation.
 */
class GeoSearchTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;

	private static TestModels models;
	private static EClass place;

	private IndexUnit unit;
	private IndexSchema schema;

	@BeforeAll
	static void loadModel() {
		models = TestModels.load("geo.ecore");
		place = models.eClass("Place");
	}

	@BeforeEach
	void indexCorpus() throws IOException {
		unit = IndexUnit.open(IndexUnitConfig.inMemory("geo").refresh(RefreshTrigger.manual()).build());
		schema = IndexSchema.of(mapping());
		DocumentMapper mapper = DocumentMapper.of(schema);
		for (EObject object : corpus()) {
			MappedDocument mapped = mapper.map(object);
			unit.addDocument(mapped.root());
		}
		unit.refresh();
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	// --- boxes --------------------------------------------------------------------------------

	@Test
	void aBoxReadsTheSameFieldWhicheverWayTheModelSpellsThePosition() throws Exception {
		// One shape, three bindings, one answer — that is the whole point of resolving the
		// authoring shape at index time.
		Expression thuringia = geoWithin(split(), geoBox(geoPoint(11.3, 50.5), geoPoint(12.5, 51.5)));
		assertThat(names(thuringia)).containsExactlyInAnyOrder("Jena", "Gera");
		assertThat(names(geoWithin(packed(), geoBox(geoPoint(11.3, 50.5), geoPoint(12.5, 51.5)))))
				.containsExactlyInAnyOrder("Jena", "Gera");
		assertThat(names(geoWithin(combined(), geoBox(geoPoint(11.3, 50.5), geoPoint(12.5, 51.5)))))
				.containsExactlyInAnyOrder("Jena", "Gera");
	}

	@Test
	void aWrapAroundBoxCrossesTheAntimeridianWithoutBeingSplitInTwo() throws Exception {
		// west > east is the legal wrap box (G2). Lucene's newBoxQuery takes it natively —
		// no $or of two boxes as the mongo translation needs.
		Expression pacific = geoWithin(split(), geoBox(geoPoint(170.0, -25.0), geoPoint(-165.0, -10.0)));
		assertThat(names(pacific)).containsExactlyInAnyOrder("Suva", "Apia");
		assertThat(names(geoWithin(packed(), geoBox(geoPoint(170.0, -25.0), geoPoint(-165.0, -10.0)))))
				.containsExactlyInAnyOrder("Suva", "Apia");
	}

	@Test
	void aBoxLeavesTheRowWithoutCoordinatesOut() throws Exception {
		assertThat(names(geoWithin(split(), geoBox(geoPoint(-180.0, -90.0), geoPoint(180.0, 90.0)))))
				.doesNotContain("Nowhere");
	}

	// --- polygons -----------------------------------------------------------------------------

	@Test
	void aPolygonAgreesWithTheMemoryOracle() throws Exception {
		// §5.5's first open point: the reference engine ray-casts planar, Lucene has its own
		// edge treatment. The instrument is the differential, not an assumption — so the
		// oracle's answer is what the backend is held to.
		Expression triangle = geoWithin(packed(), geoPolygon(
				geoPoint(11.3, 50.6), geoPoint(12.5, 50.6), geoPoint(11.9, 51.4)));
		assertThat(names(triangle)).containsExactlyInAnyOrderElementsOf(oracle(triangle));
		assertThat(names(triangle)).containsExactlyInAnyOrder("Jena", "Gera");
	}

	@Test
	void aPolygonOverASplitBindingIsAnswered() throws Exception {
		// Where mongo has to refuse (no index form for ray casting over two scalars), the
		// index-time resolution makes the split binding just another point field.
		Expression triangle = geoWithin(split(), geoPolygon(
				geoPoint(11.3, 50.6), geoPoint(12.5, 50.6), geoPoint(11.9, 51.4)));
		assertThat(names(triangle)).containsExactlyInAnyOrderElementsOf(oracle(triangle));
	}

	// --- distance -----------------------------------------------------------------------------

	@Test
	void aDistanceThresholdSplitsTheCorpusOnBothBindings() throws Exception {
		// Jena↔Gera ≈ 35 km, Jena↔Erfurt ≈ 39 km: 37 km separates them.
		assertThat(names(geoDistance(packed(), geoPoint(11.586, 50.927)).le(37_000)))
				.containsExactlyInAnyOrder("Jena", "Gera");
		assertThat(names(geoDistance(split(), geoPoint(11.586, 50.927)).le(37_000)))
				.containsExactlyInAnyOrder("Jena", "Gera");
	}

	@Test
	void theOutsideBandExcludesTheRowWithoutCoordinates() throws Exception {
		// "farther than 37 km" is a negation, and UNKNOWN is not "far": Nowhere stays out.
		assertThat(names(geoDistance(packed(), geoPoint(11.586, 50.927)).gt(37_000)))
				.containsExactlyInAnyOrder("Erfurt", "Suva", "Apia");
		assertThat(names(geoDistance(split(), geoPoint(11.586, 50.927)).gt(37_000)))
				.containsExactlyInAnyOrder("Erfurt", "Suva", "Apia");
	}

	@Test
	void aMirroredComparisonMeansTheSameThing() throws Exception {
		// `37000 >= distance` is `distance <= 37000`.
		assertThat(names(geoDistance(split(), geoPoint(11.586, 50.927)).le(37_000)))
				.containsExactlyInAnyOrderElementsOf(names(mirrored(37_000)));
	}

	// --- three-valued logic ---------------------------------------------------------------------

	@Test
	void negatingAGeoPredicateNeverSurfacesAPositionlessRow() throws Exception {
		Expression thuringia = geoWithin(split(), geoBox(geoPoint(11.3, 50.5), geoPoint(12.5, 51.5)));
		assertThat(names(not(thuringia))).containsExactlyInAnyOrder("Erfurt", "Suva", "Apia");
		Expression packedThuringia = geoWithin(packed(),
				geoBox(geoPoint(11.3, 50.5), geoPoint(12.5, 51.5)));
		assertThat(names(not(packedThuringia))).containsExactlyInAnyOrder("Erfurt", "Suva", "Apia");
	}

	@Test
	void aPredicateAndItsNegationDoNotAddUpToTheCorpus() throws Exception {
		Expression thuringia = geoWithin(split(), geoBox(geoPoint(11.3, 50.5), geoPoint(12.5, 51.5)));
		assertThat(names(thuringia).size() + names(not(thuringia)).size())
				.as("the positionless row belongs to neither side")
				.isEqualTo(5);
	}

	// --- the distance sort -----------------------------------------------------------------------

	@Test
	void aDistanceSortOrdersNearestFirstAndPutsPositionlessRowsLast() throws Exception {
		List<String> ordered = namesSorted(nearest(geoPoint(11.586, 50.927), SortDirection.ASC));

		assertThat(ordered).startsWith("Jena", "Gera", "Erfurt");
		assertThat(ordered).endsWith("Nowhere");
	}

	@Test
	void farthestFirstIsRefusedRatherThanFaked() {
		assertThatThrownBy(() -> namesSorted(nearest(geoPoint(11.586, 50.927), SortDirection.DESC)))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("nearest first");
	}

	@Test
	void aDistanceSortWithoutDocValuesIsRefusedWithTheWayOut() throws Exception {
		IndexUnitMapping withoutDocValues = mapping();
		((GeoPointFieldMapping) withoutDocValues.getDocuments().get(0).getFields().get(0))
				.setDocValues(false);
		IndexSchema bare = IndexSchema.of(withoutDocValues);

		assertThatThrownBy(() -> LuceneQueryProcessor.of(bare, null)
				.translate(nearest(geoPoint(11.586, 50.927), SortDirection.ASC),
						QueryContexts.of(place, null)))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("docValues=true");
	}

	// --- refusals --------------------------------------------------------------------------------

	@Test
	void anExactDistanceIsRefusedAsMeasureZero() {
		assertThatThrownBy(() -> names(geoDistance(split(), geoPoint(11.586, 50.927)).eq(37_000)))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("measure-zero");
	}

	@Test
	void aBareDistanceIsNotAPredicate() {
		assertThatThrownBy(() -> names(geoDistance(split(), geoPoint(11.586, 50.927)).toExpression()))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("sort key");
	}

	@Test
	void anUndeclaredBindingRefusesByNameInsteadOfGuessing() {
		// lon/lat swapped: not the pair any declared position was mapped over.
		GeoSubject swapped = geoSubject(propertyPath(models.feature("Place", "lon")),
				propertyPath(models.feature("Place", "lat")));

		assertThatThrownBy(() -> names(geoWithin(swapped,
				geoBox(geoPoint(11.3, 50.5), geoPoint(12.5, 51.5)))))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("not a declared position");
	}

	@Test
	void aMappingWithoutAnyPositionSaysSoRatherThanFailingToResolve() throws Exception {
		IndexUnitMapping bare = ESEARCH.createIndexUnitMapping();
		bare.setName("geo");
		bare.setEPackage(models.ePackage());
		var document = ESEARCH.createDocumentMapping();
		document.setEClass(place);
		bare.getDocuments().add(document);

		assertThatThrownBy(() -> LuceneQueryProcessor.of(IndexSchema.of(bare), null)
				.translate(QueryBuilder.from(place)
						.where(geoWithin(split(), geoBox(geoPoint(11.3, 50.5), geoPoint(12.5, 51.5))))
						.build(), QueryContexts.of(place, null)))
				.isInstanceOf(QueryException.class)
				.hasMessageContaining("No geographic point is declared");
	}

	@Test
	void aHalfDeclaredPositionIsAMappingError() {
		IndexUnitMapping half = mapping();
		GeoPointFieldMapping geo = (GeoPointFieldMapping) half.getDocuments().get(0).getFields().get(0);
		geo.setLongitude(null);

		assertThatThrownBy(() -> IndexSchema.of(half).geoFields(place))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("needs both");
	}

	@Test
	void anImpossiblePositionIsRefusedAtWriteTimeRatherThanIndexedWrong() {
		EObject broken = place("broken", "Broken", 500.0, 91.0);

		assertThatThrownBy(() -> DocumentMapper.of(schema).map(broken))
				.isInstanceOf(MappingException.class)
				.hasMessageContaining("impossible position");
	}

	@Test
	void aPackedPointOfTheWrongArityIsUnknownNotAnError() throws Exception {
		// One coordinate is the packed analogue of a null: the row is simply not there.
		EObject halfPoint = place("half", "Half", 11.5, 50.9);
		EObject point = (EObject) halfPoint.eGet(models.feature("Place", "location"));
		@SuppressWarnings("unchecked")
		List<Double> coordinates = (List<Double>) point.eGet(models.feature("GeoPoint", "coordinates"));
		coordinates.remove(1);
		MappedDocument mapped = DocumentMapper.of(schema).map(halfPoint);
		unit.addDocument(mapped.root());
		unit.refresh();

		assertThat(names(geoWithin(packed(), geoBox(geoPoint(-180.0, -90.0), geoPoint(180.0, 90.0)))))
				.doesNotContain("Half");
		// the split binding of the very same row still answers — the shapes are independent
		assertThat(names(geoWithin(split(), geoBox(geoPoint(-180.0, -90.0), geoPoint(180.0, 90.0)))))
				.contains("Half");
	}

	// --- helpers ----------------------------------------------------------------------------------

	private GeoSubject split() {
		return geoSubject(propertyPath(models.feature("Place", "lat")),
				propertyPath(models.feature("Place", "lon")));
	}

	private GeoSubject packed() {
		return geoSubject(propertyPath(models.feature("Place", "location")));
	}

	private GeoSubject combined() {
		return geoSubject(propertyPath(models.feature("Place", "corner")));
	}

	/** {@code threshold >= geoDistance(...)}, the mirrored spelling of the same predicate. */
	private Expression mirrored(int threshold) {
		var comparison = org.eclipse.fennec.model.expression.ExpressionFactory.eINSTANCE.createComparison();
		comparison.setOperator(org.eclipse.fennec.model.expression.ComparisonOperator.GE);
		var literal = org.eclipse.fennec.model.expression.ExpressionFactory.eINSTANCE.createIntegerLiteral();
		literal.setValue(threshold);
		comparison.setLeft(literal);
		comparison.setRight(geoDistance(split(), geoPoint(11.586, 50.927)).toExpression());
		return comparison;
	}

	private Query nearest(org.eclipse.fennec.model.expression.GeoPointLiteral point,
			SortDirection direction) {
		Query query = QueryBuilder.from(place).build();
		var orderBy = QueryFactory.eINSTANCE.createOrderBy();
		orderBy.setKey(geoDistance(split(), point).toExpression());
		orderBy.setDirection(direction);
		query.getOrderBy().add(orderBy);
		return query;
	}

	private List<String> names(Expression predicate) throws Exception {
		return search(plan(QueryBuilder.from(place).where(predicate).build()), null);
	}

	private List<String> namesSorted(Query query) throws Exception {
		LuceneQueryPlan plan = plan(query);
		return search(plan, plan.sort());
	}

	private LuceneQueryPlan plan(Query query) throws QueryException {
		return (LuceneQueryPlan) LuceneQueryProcessor.of(schema, null)
				.translate(query, QueryContexts.of(place, null));
	}

	private List<String> search(LuceneQueryPlan plan, Sort sort) throws IOException {
		return unit.search(searcher -> {
			TopDocs top = sort == null
					? searcher.search(plan.query(), 100)
					: searcher.search(plan.query(), 100, sort);
			List<String> found = new ArrayList<>();
			for (ScoreDoc hit : top.scoreDocs) {
				Document document = searcher.storedFields().document(hit.doc);
				found.add(document.get("name"));
			}
			return found;
		});
	}

	/** What the IR's reference engine answers for the same query over the same corpus. */
	private List<String> oracle(Expression predicate) throws Exception {
		Query query = QueryBuilder.from(place).where(predicate).build();
		try (QueryResult result = MemoryQueries.execute(query, corpus(), null);
				var objects = result.objects()) {
			return objects.map(found -> (String) found.eGet(models.feature("Place", "name")))
					.toList();
		}
	}

	private static List<EObject> corpus() {
		return List.of(
				place("1", "Jena", 11.586, 50.927),
				place("2", "Gera", 12.083, 50.880),
				place("3", "Erfurt", 11.029, 50.984),
				place("4", "Nowhere", null, null),
				place("5", "Suva", 178.442, -18.141),
				place("6", "Apia", -171.760, -13.833));
	}

	/** One place carrying its position in all three authoring shapes at once. */
	@SuppressWarnings("unchecked")
	private static EObject place(String id, String name, Double lon, Double lat) {
		EObject object = EcoreUtil.create(place);
		object.eSet(models.feature("Place", "id"), id);
		object.eSet(models.feature("Place", "name"), name);
		if (lat == null) {
			return object;
		}
		object.eSet(models.feature("Place", "lat"), lat);
		object.eSet(models.feature("Place", "lon"), lon);
		((List<Double>) object.eGet(models.feature("Place", "corner"))).addAll(List.of(lon, lat));
		EObject point = EcoreUtil.create(models.eClass("GeoPoint"));
		point.eSet(models.feature("GeoPoint", "gid"), "p" + id);
		point.eSet(models.feature("GeoPoint", "type"), "Point");
		((List<Double>) point.eGet(models.feature("GeoPoint", "coordinates"))).addAll(List.of(lon, lat));
		object.eSet(models.feature("Place", "location"), point);
		return object;
	}

	/** All three authoring shapes declared side by side, so every binding resolves. */
	private static IndexUnitMapping mapping() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("geo");
		mapping.setEPackage(models.ePackage());
		var document = ESEARCH.createDocumentMapping();
		document.setEClass(place);

		GeoPointFieldMapping pair = ESEARCH.createGeoPointFieldMapping();
		pair.setName("position");
		pair.setLatitude((EAttribute) models.feature("Place", "lat"));
		pair.setLongitude((EAttribute) models.feature("Place", "lon"));
		pair.setDocValues(true);
		document.getFields().add(pair);

		GeoPointFieldMapping packed = ESEARCH.createGeoPointFieldMapping();
		packed.setPointReference((EReference) models.feature("Place", "location"));
		packed.setCoordinates((EAttribute) models.feature("GeoPoint", "coordinates"));
		document.getFields().add(packed);

		GeoPointFieldMapping combined = ESEARCH.createGeoPointFieldMapping();
		combined.setCoordinates((EAttribute) models.feature("Place", "corner"));
		document.getFields().add(combined);

		mapping.getDocuments().add(document);
		return mapping;
	}
}
