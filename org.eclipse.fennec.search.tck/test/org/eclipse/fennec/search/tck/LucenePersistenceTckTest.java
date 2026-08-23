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
package org.eclipse.fennec.search.tck;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistries;
import org.eclipse.fennec.persistence.capabilities.CommandCapabilitiesBuilder;
import org.eclipse.fennec.persistence.capabilities.CommandFeature;
import org.eclipse.fennec.persistence.capabilities.PersistenceCapabilities;
import org.eclipse.fennec.persistence.capabilities.StoreCapabilitiesBuilder;
import org.eclipse.fennec.persistence.query.support.NamedOperations;
import org.eclipse.fennec.persistence.query.support.RegistryNamedOperations;
import org.eclipse.fennec.persistence.tck.AbstractPersistenceTCK;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.GeoPointFieldMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.Materialization;
import org.eclipse.fennec.search.esearch.MaterializationKind;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.query.LuceneQueryProcessor;
import org.eclipse.fennec.search.resource.SearchResourceFactory;
import org.eclipse.fennec.search.resource.SearchUris;
import org.eclipse.fennec.search.unit.CommitPolicy;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.junit.jupiter.api.Test;

/**
 * The published persistence TCK bound to the Lucene backend — the acceptance test of the
 * whole wave: a fresh in-memory unit per test, the TCK's dynamic model mapped with the
 * declarations this backend needs to be honest (a keyword name for exact and
 * case-insensitive matching, containment as a NESTED block, non-containment as ID_ONLY
 * references), and the {@code CapabilityGate} reading exactly the processor's declaration —
 * so what skips here is what the backend refuses everywhere.
 * <p>
 * The visibility policy is deliberate TCK tuning, not a recommendation: commit after every
 * document plus refresh-on-commit buys the read-your-writes the suite assumes, at a write
 * cost no production configuration would accept (see the index-units guide).
 */
public class LucenePersistenceTckTest extends AbstractPersistenceTCK {

	private EPackage tckPackage;
	private IndexUnit unit;
	private DocumentMapper mapper;
	private NamedOperations queryCatalog;

	@Override
	protected void setUpBackend(EPackage tckPackage) throws Exception {
		this.tckPackage = tckPackage;
		this.unit = IndexUnit.open(IndexUnitConfig.inMemory("tck")
				.refresh(RefreshTrigger.onCommit())
				.commit(new CommitPolicy(1, Duration.ZERO, true))
				.build());
		this.mapper = DocumentMapper.of(IndexSchema.of(mapping(tckPackage)));
		// The stack-wide catalog contract (emf.persistence-jpa#203) over its default
		// implementation — the registry this backend used to read directly.
		this.queryCatalog = new RegistryNamedOperations(EObjectRegistries.createRegistry("tck-queries"));
	}

	@Override
	protected void tearDownBackend() throws Exception {
		if (unit != null) {
			unit.close();
		}
	}

	@Override
	protected ResourceSet createBackendResourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(tckPackage.getNsURI(), tckPackage);
		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(SearchUris.SCHEME,
				new SearchResourceFactory(unit, mapper, queryCatalog, null));
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());
		return resourceSet;
	}

	@Override
	protected URI uriFor(String typeName) {
		return URI.createURI(SearchUris.SCHEME + "://tck/" + typeName);
	}

	/**
	 * The write vocabulary is declared backend-wide (S21, #29): insert, delete-by-selector and
	 * update-by-selector. The live resource narrows the last one to the classes whose mapping
	 * declares {@code STORED_OBJECT} — which this binding's mapping does, so the narrowing
	 * takes nothing away here and the conformance cases run for real. The transaction bracket
	 * stays undeclared: Lucene has no rollback scoped to one caller (S22, #30).
	 */
	@Override
	protected PersistenceCapabilities declaredCapabilities() {
		return PersistenceCapabilities.of(LuceneQueryProcessor.declaredCapabilities(),
				CommandCapabilitiesBuilder.create()
						.support(CommandFeature.INSERT, CommandFeature.DELETE_BY_SELECTOR,
								CommandFeature.UPDATE_BY_SELECTOR)
						.build(),
				StoreCapabilitiesBuilder.create().build());
	}

	/**
	 * The trigger this backend can honestly produce (emf.persistence-jpa#197): a URI naming a
	 * type no document mapping of the unit writes. An index has no schema to refuse a write
	 * with — but it does know which types it maps, and answering an unknown one with silence
	 * is exactly the empty-looking success this case exists to catch. The read leaves an error
	 * diagnostic on the resource instead.
	 */
	@Override
	protected Resource provokeLoadDiagnostic() throws Exception {
		Resource resource = createBackendResourceSet().createResource(uriFor("NoSuchType"));
		resource.load(null);
		// This backend populates eagerly, unlike JPA and Mongo — the touch is here so the
		// trigger stays correct if that ever changes.
		resource.getContents();
		return resource;
	}

	/**
	 * The mapping the TCK model needs from this backend: {@code Person.name} as a keyword
	 * (exact equality and case-insensitive matching run on terms, not tokens), containment
	 * as a NESTED block (the quantifier cases), every non-containment reference ID_ONLY so
	 * it round-trips as a resolvable proxy. Everything else follows convention.
	 */
	private IndexUnitMapping mapping(EPackage tckPackage) {
		IndexUnitMapping mapping = ESearchFactory.eINSTANCE.createIndexUnitMapping();
		mapping.setName("tck");
		mapping.setEPackage(tckPackage);

		EClass person = (EClass) tckPackage.getEClassifier("Person");
		DocumentMapping personMapping = document(mapping, person);
		KeywordFieldMapping name = ESearchFactory.eINSTANCE.createKeywordFieldMapping();
		name.setFeature((EAttribute) person.getEStructuralFeature("name"));
		name.setDocValues(true);
		personMapping.getFields().add(name);
		nested(personMapping, person, "addresses");
		// EMaps (upstream #185) are containment references to entry objects, so they are
		// blocks like any other containment. Their key/value fields need names of their
		// own: one index holds one field type per name, and the two entry classes key on
		// a string and on an int.
		nested(personMapping, person, "attributes");
		nested(personMapping, person, "counts");
		idOnly(personMapping, person, "bestFriend");
		idOnly(personMapping, person, "friends");
		idOnly(personMapping, person, "employer");

		EClass company = (EClass) tckPackage.getEClassifier("Company");
		DocumentMapping companyMapping = document(mapping, company);
		// One index, one field type per name: Person.name is a keyword, so Company.name
		// must be one too — Lucene refuses a field that is analyzed in one document and
		// keyword in the next.
		KeywordFieldMapping companyName = ESearchFactory.eINSTANCE.createKeywordFieldMapping();
		companyName.setFeature((EAttribute) company.getEStructuralFeature("name"));
		companyName.setDocValues(true);
		companyMapping.getFields().add(companyName);
		idOnly(companyMapping, company, "employees");

		// Geo (S9, #13): the differential corpus queries Place through both coordinate
		// bindings, so both are declared — split over lat/lon and packed over the GeoJSON
		// point behind `location`. They resolve into two fields, which after indexing are
		// the same kind of thing; doc values so a distance sort can read them.
		EClass place = (EClass) tckPackage.getEClassifier("Place");
		DocumentMapping placeMapping = document(mapping, place);
		GeoPointFieldMapping pair = ESearchFactory.eINSTANCE.createGeoPointFieldMapping();
		pair.setName("position");
		pair.setLatitude((EAttribute) place.getEStructuralFeature("lat"));
		pair.setLongitude((EAttribute) place.getEStructuralFeature("lon"));
		pair.setDocValues(true);
		placeMapping.getFields().add(pair);
		EClass geoPoint = (EClass) tckPackage.getEClassifier("GeoPoint");
		GeoPointFieldMapping packed = ESearchFactory.eINSTANCE.createGeoPointFieldMapping();
		packed.setPointReference((EReference) place.getEStructuralFeature("location"));
		packed.setCoordinates((EAttribute) geoPoint.getEStructuralFeature("coordinates"));
		packed.setDocValues(true);
		placeMapping.getFields().add(packed);
		// The point is a containment child in its own right (§8 single-valued containment):
		// the packed geo field reads its coordinates as a position, the block writes the
		// object, and neither is the other — a position is not a value anyone reads back.
		nested(placeMapping, place, "location");

		entry(mapping, (EClass) tckPackage.getEClassifier("StringToStringMapEntry"), "stringMap");
		entry(mapping, (EClass) tckPackage.getEClassifier("IntToStringMapEntry"), "intMap");

		EClass address = (EClass) tckPackage.getEClassifier("Address");
		DocumentMapping addressMapping = document(mapping, address);
		// The quantifier cases anchor on street (startsWith), which needs terms with
		// value boundaries — a keyword, not analyzed text.
		KeywordFieldMapping street = ESearchFactory.eINSTANCE.createKeywordFieldMapping();
		street.setFeature((EAttribute) address.getEStructuralFeature("street"));
		street.setDocValues(true);
		addressMapping.getFields().add(street);

		return mapping;
	}

	/** One map-entry class, with key and value under names nobody else writes. */
	private void entry(IndexUnitMapping mapping, EClass entryClass, String prefix) {
		DocumentMapping document = document(mapping, entryClass);
		for (String feature : List.of("key", "value")) {
			KeywordFieldMapping field = ESearchFactory.eINSTANCE.createKeywordFieldMapping();
			field.setFeature((EAttribute) entryClass.getEStructuralFeature(feature));
			field.setName(prefix + "." + feature);
			document.getFields().add(field);
		}
	}

	/**
	 * Every document mapping of this binding declares {@code STORED_OBJECT} materialization
	 * (S16, #18) — the complete object beside its indexed fields. Two reasons, both about
	 * conformance rather than about taste: it is what makes {@code UPDATE_BY_SELECTOR} honest
	 * here (Lucene has no partial update, so a rewrite needs the whole object — §5.4), and it
	 * is the tier a store aiming at the persistence contract would choose, since the TCK reads
	 * back objects rather than search hits. A mapping without it is still legitimate and still
	 * covered — by the plain-JUnit tests, where the refusal is asserted.
	 */
	private DocumentMapping document(IndexUnitMapping mapping, EClass eClass) {
		DocumentMapping document = ESearchFactory.eINSTANCE.createDocumentMapping();
		document.setEClass(eClass);
		Materialization complete = ESearchFactory.eINSTANCE.createMaterialization();
		complete.setKind(MaterializationKind.STORED_OBJECT);
		document.setMaterialization(complete);
		mapping.getDocuments().add(document);
		return document;
	}

	private void nested(DocumentMapping document, EClass owner, String reference) {
		ReferenceMapping mapping = ESearchFactory.eINSTANCE.createReferenceMapping();
		mapping.setEReference((EReference) owner.getEStructuralFeature(reference));
		mapping.setStrategy(ReferenceStrategy.NESTED);
		document.getReferences().add(mapping);
	}

	private void idOnly(DocumentMapping document, EClass owner, String reference) {
		ReferenceMapping mapping = ESearchFactory.eINSTANCE.createReferenceMapping();
		mapping.setEReference((EReference) owner.getEStructuralFeature(reference));
		mapping.setStrategy(ReferenceStrategy.ID_ONLY);
		document.getReferences().add(mapping);
	}

	/**
	 * The documented divergence, the Mongo way (an overridden case asserting what this
	 * backend does instead): an index has no honest id counter, so <b>nothing is
	 * generated</b>. The effective id value is the id — for the numeric TCK id an unset
	 * {@code pid} is its default {@code 0} (the #37 semantics every backend shares), so
	 * the save succeeds under id 0, nothing is written back, and a second id-less object
	 * replaces the first: same id, same document. Callers own their ids.
	 */
	@Override
	@Test
	public void idGenerationOnSaveAssignsAndWritesBackId() throws Exception {
		ResourceSet resourceSet = createBackendResourceSet();
		EClass person = (EClass) tckPackage.getEClassifier("Person");
		EObject first = EcoreUtil.create(person);
		first.eSet(person.getEStructuralFeature("name"), "First");
		Resource resource = resourceSet.createResource(uriFor("Person"));
		resource.getContents().add(first);
		resource.save(null);

		assertThat(first.eGet(person.getEStructuralFeature("pid")))
				.as("no id is generated or written back — the effective default is the id")
				.isEqualTo(0);

		EObject second = EcoreUtil.create(person);
		second.eSet(person.getEStructuralFeature("name"), "Second");
		Resource other = createBackendResourceSet().createResource(uriFor("Person"));
		other.getContents().add(second);
		other.save(null);

		Resource read = createBackendResourceSet().createResource(uriFor("Person"));
		read.load(null);
		assertThat(read.getContents())
				.as("two id-less objects share id 0 — the second replaced the first")
				.hasSize(1);
	}
}
