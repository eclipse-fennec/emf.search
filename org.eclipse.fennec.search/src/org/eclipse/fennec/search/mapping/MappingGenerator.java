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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.FacetMapping;
import org.eclipse.fennec.search.esearch.GeoPointFieldMapping;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.esearch.SuggestSource;

/**
 * Derives a <em>suggested</em> {@link IndexUnitMapping} from an ecore — the starting point
 * a modeller edits, in the spirit of the eorm mapper of {@code emf.persistence} (#51).
 * <p>
 * <b>This is not the convention layer.</b> An unmapped class is already indexed by
 * convention at runtime ({@code IndexSchema}: ids and enums become keywords, strings
 * analyzed text, numerics points), and nothing here is needed to search a model. What the
 * generator adds is the layer above: the declarations a model usually wants and a
 * newcomer does not know to write — a sortable keyword beside a name, facet dimensions,
 * containment as a block, a suggestion source, a geographic position — written down as an
 * <em>editable document</em> rather than hidden in a runtime default.
 * <p>
 * The difference that licenses the whole design: <b>conventions never guess from a
 * name</b>, because a wrong guess would silently change what a query answers. A generator
 * may, because its output is a proposal a human reads, edits and commits — so
 * {@code name}, {@code title} and {@code label} are treated as the human-readable label of
 * a class, and every such guess is reported in {@link Suggestions#explanations()}.
 * <p>
 * What it deliberately does not do: it never reads an existing mapping to "update" it (a
 * mapping is authored, and merging generated over authored declarations is how generators
 * destroy work), and it declares nothing it cannot justify — a numeric attribute gets no
 * declaration at all, because convention already indexes it correctly.
 *
 * @author Data In Motion Consulting
 */
public final class MappingGenerator {

	/** Attribute names taken as the human-readable label of a class. */
	private static final Set<String> LABEL_NAMES = Set.of("name", "title", "label", "displayname");

	/** The generated mapping and, per declaration, why the generator proposed it. */
	public record Suggestions(IndexUnitMapping mapping, List<String> explanations) {

		public Suggestions {
			explanations = List.copyOf(explanations);
		}
	}

	private final List<EClass> classes;
	private final EPackage ePackage;
	private final String unitName;
	private final List<String> explanations = new ArrayList<>();

	private MappingGenerator(EPackage ePackage, List<EClass> classes, String unitName) {
		this.ePackage = ePackage;
		this.classes = classes;
		this.unitName = unitName;
	}

	/** A generator over every EClass of a package; the unit is named after the package. */
	public static MappingGenerator forPackage(EPackage ePackage) {
		Objects.requireNonNull(ePackage, "ePackage");
		List<EClass> classes = ePackage.getEClassifiers().stream()
				.filter(EClass.class::isInstance)
				.map(EClass.class::cast)
				.toList();
		return new MappingGenerator(ePackage, classes, ePackage.getName());
	}

	/** A generator over selected classes, which must share one package. */
	public static MappingGenerator forClasses(Collection<EClass> classes, String unitName) {
		Objects.requireNonNull(classes, "classes");
		Objects.requireNonNull(unitName, "unitName");
		if (classes.isEmpty()) {
			throw new IllegalArgumentException("No class to generate a mapping for.");
		}
		Set<EPackage> packages = new LinkedHashSet<>();
		classes.forEach(eClass -> packages.add(eClass.getEPackage()));
		if (packages.size() != 1) {
			throw new IllegalArgumentException("A unit indexes one EPackage universe, but these classes "
					+ "come from " + packages.size() + ": " + packages.stream().map(EPackage::getNsURI).toList()
					+ ". Generate one mapping per package.");
		}
		return new MappingGenerator(packages.iterator().next(), List.copyOf(classes), unitName);
	}

	/** Generates the suggested mapping, with one explanation per declaration it made. */
	public Suggestions generate() {
		explanations.clear();
		IndexUnitMapping mapping = ESearchFactory.eINSTANCE.createIndexUnitMapping();
		mapping.setName(unitName);
		mapping.setEPackage(ePackage);
		Set<EClass> geoPointClasses = geoPointClasses();
		for (EClass eClass : classes) {
			if (eClass.isAbstract() || eClass.isInterface() || geoPointClasses.contains(eClass)) {
				// An abstract class has no documents of its own — its concrete subtypes are
				// mapped, and a URI naming it reads them. A geo point class is a value inside
				// its owner's position field, never a document.
				continue;
			}
			DocumentMapping document = documentFor(eClass, geoPointClasses);
			if (document != null) {
				mapping.getDocuments().add(document);
			}
		}
		if (mapping.getDocuments().isEmpty()) {
			explanations.add("Nothing was declared: every class of '" + ePackage.getName()
					+ "' is served by convention as it is. That is a complete answer — an index "
					+ "unit needs no mapping to work.");
		}
		return new Suggestions(mapping, explanations);
	}

	private DocumentMapping documentFor(EClass eClass, Set<EClass> geoPointClasses) {
		DocumentMapping document = ESearchFactory.eINSTANCE.createDocumentMapping();
		document.setEClass(eClass);
		EAttribute label = labelAttributeOf(eClass);
		for (EAttribute attribute : eClass.getEAllAttributes()) {
			if (attribute.isDerived() || attribute.isTransient() || attribute.isID()) {
				// Derived and transient attributes are not indexed by convention and need a
				// deliberate decision; the id is already a stored keyword.
				continue;
			}
			fieldFor(eClass, attribute, attribute == label).ifPresent(document.getFields()::add);
		}
		for (EReference reference : eClass.getEAllReferences()) {
			if (reference.isDerived() || reference.isTransient()) {
				continue;
			}
			referenceFor(eClass, reference, geoPointClasses).ifPresent(declared -> {
				if (declared instanceof ReferenceMapping referenceMapping) {
					document.getReferences().add(referenceMapping);
				} else {
					document.getFields().add((GeoPointFieldMapping) declared);
				}
			});
		}
		if (label != null) {
			SuggestSource suggestions = ESearchFactory.eINSTANCE.createSuggestSource();
			suggestions.setName(pluralOf(label.getName()));
			suggestions.setFeature(label);
			document.getSuggestions().add(suggestions);
			explain(eClass, label, "a suggestion source '" + suggestions.getName() + "', because a "
					+ "human-readable label is what users complete. Declare a weight attribute on it "
					+ "(a view count, a rating) to rank popular values first.");
		}
		return document.getFields().isEmpty() && document.getReferences().isEmpty()
				&& document.getSuggestions().isEmpty()
						? null
						: document;
	}

	private Optional<KeywordFieldMapping> fieldFor(EClass owner, EAttribute attribute, boolean isLabel) {
		EClassifier type = attribute.getEAttributeType();
		if (type instanceof EEnum) {
			// An enum is a small closed set of values — the shape a facet dimension is for,
			// and convention already indexes it as a keyword.
			KeywordFieldMapping keyword = keyword(attribute, !attribute.isMany());
			keyword.setFacet(facet(attribute.isMany()));
			explain(owner, attribute, "a facet dimension, because an enum is a small closed set of "
					+ "values — exactly what a filter sidebar counts.");
			return Optional.of(keyword);
		}
		if (!isString(attribute)) {
			// Numerics, temporals and booleans are indexed correctly by convention: a point
			// with doc values, a keyword for booleans. Declaring that again would say the
			// same thing twice.
			return Optional.empty();
		}
		if (attribute.isMany()) {
			// A many-valued string is the tag shape: exact values, countable, and analyzed
			// text would answer neither question.
			KeywordFieldMapping keyword = keyword(attribute, false);
			keyword.setFacet(facet(true));
			explain(owner, attribute, "a many-valued keyword with a facet, the tag shape: exact "
					+ "values a query matches and a sidebar counts. Remove the facet if you only "
					+ "filter on it.");
			return Optional.of(keyword);
		}
		if (isLabel) {
			// The label is the one string a model sorts and matches exactly on. Convention
			// makes a string analyzed text, which can do neither.
			KeywordFieldMapping keyword = keyword(attribute, true);
			explain(owner, attribute, "a keyword projection with doc values, because a "
					+ "human-readable label is what a result list sorts by and what an exact match "
					+ "asks for — analyzed text can do neither. Declare it as a sub-field of a "
					+ "TextFieldMapping if you also want full-text search over it.");
			return Optional.of(keyword);
		}
		// Any other string stays analyzed text by convention: that is what full-text search
		// is for, and it is the right default for a description.
		return Optional.empty();
	}

	private Optional<Object> referenceFor(EClass owner, EReference reference, Set<EClass> geoPointClasses) {
		if (reference.isContainment() && geoPointClasses.contains(reference.getEReferenceType())) {
			GeoPointFieldMapping geo = ESearchFactory.eINSTANCE.createGeoPointFieldMapping();
			geo.setPointReference(reference);
			geo.setCoordinates(coordinatesOf(reference.getEReferenceType()));
			geo.setDocValues(true);
			explain(owner, reference, "a geographic position: the target class holds exactly two "
					+ "coordinates, which is the packed point shape. Check the order — this backend "
					+ "reads [lon, lat], longitude first.");
			return Optional.of(geo);
		}
		ReferenceMapping mapping = ESearchFactory.eINSTANCE.createReferenceMapping();
		mapping.setEReference(reference);
		if (reference.isContainment()) {
			mapping.setStrategy(ReferenceStrategy.NESTED);
			explain(owner, reference, "NESTED, so parent and children are indexed as one block: "
					+ "that is what makes a quantifier over the children ('any review rated 4 or "
					+ "more') answerable. Use EMBED instead if you only ever match the children's "
					+ "values without asking which child matched.");
		} else {
			mapping.setStrategy(ReferenceStrategy.ID_ONLY);
			explain(owner, reference, "ID_ONLY, which stores the target's id and nothing else: an "
					+ "index has no join, so a cross-document reference is an id a query compares. "
					+ "Use EMBED to copy the target's values into this document if you need to "
					+ "search them.");
		}
		return Optional.of(mapping);
	}

	/**
	 * Classes that look like a packed geographic position: exactly one many-valued numeric
	 * attribute bounded to two values. Recognised structurally, never by name.
	 */
	private Set<EClass> geoPointClasses() {
		Set<EClass> found = new LinkedHashSet<>();
		for (EClass eClass : classes) {
			if (coordinatesOf(eClass) != null && eClass.getEAllReferences().isEmpty()) {
				found.add(eClass);
			}
		}
		return found;
	}

	/** The two-valued numeric attribute of a packed point class, or null if it is not one. */
	private static EAttribute coordinatesOf(EClass eClass) {
		EAttribute candidate = null;
		for (EAttribute attribute : eClass.getEAllAttributes()) {
			if (attribute.isDerived() || attribute.isTransient()) {
				continue;
			}
			boolean isPair = attribute.isMany() && attribute.getUpperBound() == 2
					&& isFloatingPoint(attribute);
			if (isPair && candidate == null) {
				candidate = attribute;
			} else {
				// A second attribute means this class carries more than a position.
				return null;
			}
		}
		return candidate;
	}

	/** The attribute a class is labelled by, or null when it carries none. */
	private static EAttribute labelAttributeOf(EClass eClass) {
		for (EAttribute attribute : eClass.getEAllAttributes()) {
			if (!attribute.isMany() && !attribute.isDerived() && !attribute.isTransient()
					&& isString(attribute)
					&& LABEL_NAMES.contains(attribute.getName().toLowerCase(Locale.ROOT))) {
				return attribute;
			}
		}
		return null;
	}

	private static KeywordFieldMapping keyword(EAttribute attribute, boolean docValues) {
		KeywordFieldMapping keyword = ESearchFactory.eINSTANCE.createKeywordFieldMapping();
		keyword.setFeature(attribute);
		keyword.setDocValues(docValues);
		return keyword;
	}

	private static FacetMapping facet(boolean multiValued) {
		FacetMapping facet = ESearchFactory.eINSTANCE.createFacetMapping();
		facet.setMultiValued(multiValued);
		return facet;
	}

	private static boolean isString(EAttribute attribute) {
		return String.class.equals(attribute.getEAttributeType().getInstanceClass());
	}

	private static boolean isFloatingPoint(EAttribute attribute) {
		Class<?> type = attribute.getEAttributeType().getInstanceClass();
		return double.class.equals(type) || Double.class.equals(type)
				|| float.class.equals(type) || Float.class.equals(type);
	}

	private static String pluralOf(String name) {
		return name.endsWith("s") ? name : name + "s";
	}

	private void explain(EClass owner, org.eclipse.emf.ecore.EStructuralFeature feature, String reason) {
		explanations.add(owner.getName() + "." + feature.getName() + ": " + reason);
	}
}
