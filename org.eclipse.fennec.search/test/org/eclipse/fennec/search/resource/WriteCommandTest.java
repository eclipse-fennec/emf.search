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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.fennec.model.query.builder.Expressions.path;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.command.CommandFactory;
import org.eclipse.fennec.model.command.DeleteCommand;
import org.eclipse.fennec.model.command.InsertCommand;
import org.eclipse.fennec.model.command.UpdateCommand;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.Expressions;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.model.stream.ChangeEntry;
import org.eclipse.fennec.model.stream.ChangeSet;
import org.eclipse.fennec.model.stream.DeltaKind;
import org.eclipse.fennec.model.stream.StreamFactory;
import org.eclipse.fennec.persistence.query.api.QueryResult;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.Materialization;
import org.eclipse.fennec.search.esearch.MaterializationKind;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.TestModels;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The write commands over an index unit (S21, #29) and the bracket that is refused (S22,
 * #30): insert writes copies, delete-by-selector removes whole blocks, and update rewrites
 * documents — but only for classes whose mapping keeps the complete object, because Lucene
 * has no partial update and a rewrite from mapped fields alone would silently drop
 * everything else.
 */
class WriteCommandTest {

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static TestModels catalog;
	private static EClass product;

	private IndexUnit unit;

	@BeforeAll
	static void loadModel() {
		catalog = TestModels.load("catalog.ecore");
		product = catalog.eClass("Product");
	}

	@BeforeEach
	void openUnit() throws IOException {
		unit = IndexUnit.open(IndexUnitConfig.inMemory("catalog")
				.refresh(RefreshTrigger.onCommit())
				.build());
	}

	@AfterEach
	void closeUnit() throws IOException {
		unit.close();
	}

	// --- insert ------------------------------------------------------------------------------

	@Test
	void insertWritesThePayloadAndLeavesItWithTheCommand() throws Exception {
		try (SearchResource resource = resource(complete())) {
			InsertCommand insert = CommandFactory.eINSTANCE.createInsertCommand();
			EObject espresso = product("p-1", "Espresso Machine", 499.0);
			insert.getObjects().add(espresso);
			insert.getObjects().add(product("p-2", "Grinder", 129.0));

			assertThat(resource.execute(insert)).isEqualTo(2);
			unit.refresh();

			assertThat(names(resource, all())).containsExactlyInAnyOrder("Espresso Machine", "Grinder");
			assertThat(espresso.eResource()).as("execution works on copies — the payload stays the "
					+ "command's").isNull();
			assertThat(insert.getObjects()).hasSize(2);
		}
	}

	@Test
	void insertBindsAnExistingReferenceTargetById() throws Exception {
		try (SearchResource resource = resource(complete())) {
			EObject maker = manufacturer("m-1", "ACME");
			InsertCommand makers = CommandFactory.eINSTANCE.createInsertCommand();
			makers.getObjects().add(maker);
			resource.execute(makers);
			unit.refresh();

			InsertCommand insert = CommandFactory.eINSTANCE.createInsertCommand();
			EObject machine = product("p-1", "Espresso Machine", 499.0);
			machine.eSet(manufacturerReference(), EcoreUtil.copy(maker));
			insert.getObjects().add(machine);

			assertThat(resource.execute(insert)).isEqualTo(1);
			unit.refresh();

			EObject stored = single(resource, QueryBuilder.from(product)
					.where(path(id()).eq("p-1")).build());
			EObject bound = (EObject) stored.eGet(manufacturerReference(), false);
			assertThat(bound).as("the reference comes back as a proxy into this unit").isNotNull();
			assertThat(bound.eIsProxy()).isTrue();
		}
	}

	@Test
	void insertRefusesAReferenceTargetThatIsNotThere() throws Exception {
		try (SearchResource resource = resource(complete())) {
			InsertCommand insert = CommandFactory.eINSTANCE.createInsertCommand();
			EObject machine = product("p-1", "Espresso Machine", 499.0);
			machine.eSet(manufacturerReference(), manufacturer("ghost", "Nobody"));
			insert.getObjects().add(machine);

			assertThatThrownBy(() -> resource.execute(insert))
					.isInstanceOf(IOException.class)
					.hasMessageContaining("Insert rejected");
			unit.refresh();
			assertThat(names(resource, all())).as("a refused insert writes nothing").isEmpty();
		}
	}

	// --- delete ------------------------------------------------------------------------------

	@Test
	void deleteBySelectorRemovesWhatItMatches() throws Exception {
		try (SearchResource resource = indexed(complete())) {
			DeleteCommand delete = CommandFactory.eINSTANCE.createDeleteCommand();
			delete.setSelector(QueryBuilder.from(product).where(path(price()).gt(100.0)).build());

			assertThat(resource.execute(delete)).isEqualTo(2);
			unit.refresh();

			assertThat(names(resource, all())).containsExactly("Kettle");
		}
	}

	@Test
	void deleteBySelectorTakesTheWholeBlockWithIt() throws Exception {
		try (SearchResource resource = resource(nestedReviews())) {
			EObject machine = product("p-1", "Espresso Machine", 499.0);
			reviews(machine).add(review("r-1", "ada"));
			reviews(machine).add(review("r-2", "linus"));
			InsertCommand insert = CommandFactory.eINSTANCE.createInsertCommand();
			insert.getObjects().add(machine);
			resource.execute(insert);
			unit.refresh();

			DeleteCommand delete = CommandFactory.eINSTANCE.createDeleteCommand();
			delete.setSelector(QueryBuilder.from(product).build());
			assertThat(resource.execute(delete)).as("one object, not three documents").isEqualTo(1);
			unit.refresh();

			// Deleting the matched roots alone would leave the children behind as orphans; the
			// block goes as a whole, which is what the document count proves.
			assertThat(unit.<Integer>search(searcher -> searcher.getIndexReader().numDocs())).isZero();
		}
	}

	@Test
	void aSelectorTheProcessorRefusesDoesNotDegradeIntoAFullDelete() throws Exception {
		try (SearchResource resource = indexed(complete())) {
			DeleteCommand delete = CommandFactory.eINSTANCE.createDeleteCommand();
			delete.setSelector(QueryBuilder.from(product)
					.where(path(price()).plus(1).gt(10)).build());

			assertThatThrownBy(() -> resource.execute(delete))
					.isInstanceOf(IOException.class)
					.hasMessageContaining("ARITHMETIC");
			unit.refresh();
			assertThat(names(resource, all())).as("nothing was deleted").hasSize(3);
		}
	}

	@Test
	void aSelectorThatIsNotAPlainFilterIsRefused() throws Exception {
		try (SearchResource resource = indexed(complete())) {
			DeleteCommand delete = CommandFactory.eINSTANCE.createDeleteCommand();
			delete.setSelector(QueryBuilder.from(product).top(1).build());

			assertThatThrownBy(() -> resource.execute(delete))
					.isInstanceOf(IOException.class)
					.hasMessageContaining("plain filter");
			unit.refresh();
			assertThat(names(resource, all())).hasSize(3);
		}
	}

	@Test
	void deleteBySelectorRefusesToOrphanAReference() throws Exception {
		try (SearchResource resource = resource(withManufacturer())) {
			EObject maker = manufacturer("m-1", "ACME");
			EObject machine = product("p-1", "Espresso Machine", 499.0);
			machine.eSet(manufacturerReference(), maker);
			InsertCommand insert = CommandFactory.eINSTANCE.createInsertCommand();
			insert.getObjects().add(maker);
			insert.getObjects().add(machine);
			resource.execute(insert);
			unit.refresh();

			DeleteCommand delete = CommandFactory.eINSTANCE.createDeleteCommand();
			delete.setSelector(QueryBuilder.from(catalog.eClass("Manufacturer")).build());

			assertThatThrownBy(() -> resource.execute(delete))
					.isInstanceOf(IOException.class)
					.hasMessageContaining("still reference");
			unit.refresh();
			assertThat(unit.<Integer>search(searcher -> searcher.getIndexReader().numDocs()))
					.as("the refused delete changed nothing — the maker is still there")
					.isEqualTo(2);
		}
	}

	@Test
	void aSelectorParameterIsBoundByTheCall() throws Exception {
		try (SearchResource resource = indexed(complete())) {
			DeleteCommand delete = CommandFactory.eINSTANCE.createDeleteCommand();
			delete.setSelector(QueryBuilder.from(product)
					.where(path(price()).gt(Expressions.param("floor")))
					.parameter("floor", null)
					.build());

			assertThat(resource.execute(delete, Map.of("floor", 100.0), null)).isEqualTo(2);
			unit.refresh();
			assertThat(names(resource, all())).containsExactly("Kettle");
		}
	}

	// --- update ------------------------------------------------------------------------------

	@Test
	void updateAppliesTheTemplateToEveryMatch() throws Exception {
		try (SearchResource resource = indexed(complete())) {
			UpdateCommand update = update(QueryBuilder.from(product).where(path(price()).gt(100.0)).build(),
					entry(DeltaKind.SET, name(), "Reduced"));

			assertThat(resource.execute(update)).isEqualTo(2);
			unit.refresh();

			assertThat(names(resource, all()))
					.containsExactlyInAnyOrder("Reduced", "Reduced", "Kettle");
		}
	}

	@Test
	void anUpdateKeepsWhatTheMappingNeverIndexed() throws Exception {
		try (SearchResource resource = indexed(complete())) {
			UpdateCommand update = update(QueryBuilder.from(product).where(path(id()).eq("p-1")).build(),
					entry(DeltaKind.SET, name(), "Renamed"));
			resource.execute(update);
			unit.refresh();

			EObject stored = single(resource, QueryBuilder.from(product).where(path(id()).eq("p-1")).build());
			assertThat(stored.eGet(name())).isEqualTo("Renamed");
			assertThat(stored.eGet(description()))
					.as("the rewrite read the whole object, so an untouched value survives it")
					.isEqualTo("coffee at home");
		}
	}

	@Test
	void updateIsRefusedWithoutStoredObjectMaterialization() throws Exception {
		try (SearchResource resource = indexed(partial())) {
			UpdateCommand update = update(QueryBuilder.from(product).build(),
					entry(DeltaKind.SET, name(), "Renamed"));

			assertThatThrownBy(() -> resource.execute(update))
					.isInstanceOf(IOException.class)
					.hasMessageContaining("UPDATE_BY_SELECTOR")
					.hasMessageContaining("STORED_OBJECT");
			unit.refresh();
			assertThat(names(resource, all()))
					.as("a refused update leaves every document as it was")
					.containsExactlyInAnyOrder("Espresso Machine", "Grinder", "Kettle");
		}
	}

	@Test
	void aTemplateTheTypeCannotTakeIsRefusedBeforeAnythingIsWritten() throws Exception {
		try (SearchResource resource = indexed(complete())) {
			ChangeEntry alien = StreamFactory.eINSTANCE.createChangeEntry();
			alien.setKind(DeltaKind.SET);
			alien.setFeatureId(4711);
			alien.setValueNew("nonsense");
			UpdateCommand update = update(QueryBuilder.from(product).build(), alien);

			assertThatThrownBy(() -> resource.execute(update))
					.isInstanceOf(IOException.class)
					.hasMessageContaining("Update rejected");
			unit.refresh();
			assertThat(names(resource, all()))
					.containsExactlyInAnyOrder("Espresso Machine", "Grinder", "Kettle");
		}
	}

	// --- the bracket that is not there ---------------------------------------------------------

	@Test
	void beginRefusesWithADiagnosticRatherThanPretendingToBracket() throws Exception {
		try (SearchResource resource = indexed(complete())) {
			assertThatThrownBy(resource::begin)
					.isInstanceOf(IOException.class)
					.hasMessageContaining("TRANSACTION_BRACKET");
			assertThat(resource.getErrors())
					.as("a refusal is a diagnostic, not a generic exception")
					.anySatisfy(error -> assertThat(error.getMessage()).contains("TRANSACTION_BRACKET"));
			assertThat(resource.capabilities().store()
					.supports(org.eclipse.fennec.persistence.capabilities.StoreFeature.TRANSACTION_BRACKET))
					.as("and it is declared, not discovered")
					.isFalse();
		}
	}

	// --- fixture ----------------------------------------------------------------------------------

	private SearchResource indexed(IndexUnitMapping mapping) throws IOException {
		SearchResource resource = resource(mapping);
		InsertCommand insert = CommandFactory.eINSTANCE.createInsertCommand();
		insert.getObjects().add(product("p-1", "Espresso Machine", 499.0));
		insert.getObjects().add(product("p-2", "Grinder", 129.0));
		insert.getObjects().add(product("p-3", "Kettle", 39.0));
		try {
			resource.execute(insert);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		unit.refresh();
		return resource;
	}

	private SearchResource resource(IndexUnitMapping mapping) {
		return new SearchResource(URI.createURI("lucene://catalog/Product"), unit,
				DocumentMapper.of(mapping));
	}

	/** The complete-object tier: what makes an update possible at all. */
	private IndexUnitMapping complete() {
		IndexUnitMapping mapping = plain();
		for (String className : List.of("Product", "Manufacturer")) {
			DocumentMapping document = ESEARCH.createDocumentMapping();
			document.setEClass(catalog.eClass(className));
			Materialization stored = ESEARCH.createMaterialization();
			stored.setKind(MaterializationKind.STORED_OBJECT);
			document.setMaterialization(stored);
			mapping.getDocuments().add(document);
		}
		return mapping;
	}

	/** The default tier: fields only, so a rewrite could not put the object back together. */
	private IndexUnitMapping partial() {
		return plain();
	}

	private IndexUnitMapping withManufacturer() {
		IndexUnitMapping mapping = complete();
		DocumentMapping document = mapping.getDocuments().get(0);
		ReferenceMapping reference = ESEARCH.createReferenceMapping();
		reference.setEReference(manufacturerReference());
		reference.setStrategy(ReferenceStrategy.ID_ONLY);
		document.getReferences().add(reference);
		return mapping;
	}

	private IndexUnitMapping nestedReviews() {
		IndexUnitMapping mapping = complete();
		DocumentMapping document = mapping.getDocuments().get(0);
		ReferenceMapping reference = ESEARCH.createReferenceMapping();
		reference.setEReference((EReference) catalog.feature("Product", "reviews"));
		reference.setStrategy(ReferenceStrategy.NESTED);
		document.getReferences().add(reference);
		return mapping;
	}

	private IndexUnitMapping plain() {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("catalog");
		mapping.setEPackage(catalog.ePackage());
		return mapping;
	}

	private UpdateCommand update(Query selector, ChangeEntry... entries) {
		UpdateCommand update = CommandFactory.eINSTANCE.createUpdateCommand();
		update.setSelector(selector);
		ChangeSet template = StreamFactory.eINSTANCE.createChangeSet();
		for (ChangeEntry entry : entries) {
			template.getEntries().add(entry);
		}
		update.setTemplate(template);
		return update;
	}

	private ChangeEntry entry(DeltaKind kind, EStructuralFeature feature, String value) {
		ChangeEntry entry = StreamFactory.eINSTANCE.createChangeEntry();
		entry.setKind(kind);
		entry.setFeatureId(product.getFeatureID(feature));
		entry.setValueNew(value);
		return entry;
	}

	private EObject product(String id, String name, double price) {
		return catalog.create("Product", "id", id, "name", name, "price", price,
				"description", "coffee at home");
	}

	private EObject manufacturer(String id, String name) {
		return catalog.create("Manufacturer", "id", id, "name", name);
	}

	private EObject review(String id, String author) {
		return catalog.create("Review", "id", id, "author", author);
	}

	@SuppressWarnings("unchecked")
	private List<EObject> reviews(EObject object) {
		return (List<EObject>) object.eGet(catalog.feature("Product", "reviews"));
	}

	private Query all() {
		return QueryBuilder.from(product).build();
	}

	private List<Object> names(SearchResource resource, Query query) throws Exception {
		try (QueryResult result = resource.query(query);
				Stream<EObject> objects = result.objects()) {
			return objects.map(object -> object.eGet(name())).toList();
		}
	}

	private EObject single(SearchResource resource, Query query) throws Exception {
		try (QueryResult result = resource.query(query);
				Stream<EObject> objects = result.objects()) {
			return objects.findFirst().orElseThrow();
		}
	}

	private EAttribute name() {
		return (EAttribute) catalog.feature("Product", "name");
	}

	private EAttribute id() {
		return (EAttribute) catalog.feature("Product", "id");
	}

	private EAttribute price() {
		return (EAttribute) catalog.feature("Product", "price");
	}

	private EAttribute description() {
		return (EAttribute) catalog.feature("Product", "description");
	}

	private EReference manufacturerReference() {
		return (EReference) catalog.feature("Product", "manufacturer");
	}
}
