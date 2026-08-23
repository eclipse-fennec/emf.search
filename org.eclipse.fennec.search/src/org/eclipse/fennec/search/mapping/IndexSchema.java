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

import java.util.Collection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.util.BytesRef;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.FieldMapping;
import org.eclipse.fennec.search.esearch.GeoPointFieldMapping;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.Materialization;
import org.eclipse.fennec.search.esearch.NumericFieldMapping;
import org.eclipse.fennec.search.esearch.NumericKind;
import org.eclipse.fennec.search.esearch.RankFunction;
import org.eclipse.fennec.search.esearch.RankSignalFieldMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.esearch.TextFieldMapping;

/**
 * What the index looks like, derived from an {@link IndexUnitMapping} — the single
 * answer to "which Lucene field carries this feature, and how is its value encoded".
 * <p>
 * Both directions need that answer and they must not disagree: {@link DocumentMapper}
 * uses it while writing, the query translation while reading. A field name computed
 * twice is a field name that eventually drifts, and the symptom would be a query that
 * silently matches nothing.
 * <p>
 * The rules are the ones {@code docs/search-access.md} §4 describes: an unmapped
 * attribute follows convention (id/enum/boolean → keyword, numeric and temporal →
 * point, everything else → analyzed text), a declaration overrides it, and a
 * declaration's {@code name} overrides the attribute name. Paths reach into a document
 * only as far as the mapping flattened it — {@link ReferenceStrategy#EMBED} contributes
 * a name prefix, and nothing else does.
 *
 * @author Data In Motion Consulting
 */
public final class IndexSchema {

	/** How a field's values are encoded, which decides what query can read them. */
	public enum FieldKind {
		/** Untokenized {@code StringField}; read with term and range-of-term queries. */
		KEYWORD,
		/** Tokenized {@code TextField}; read with analyzed queries, never a raw term. */
		TEXT,
		/** A point field of one {@link NumericKind}; read with point range queries. */
		NUMERIC
	}

	/**
	 * One resolved field.
	 *
	 * @param name the Lucene field name, prefixes included
	 * @param kind how values are encoded
	 * @param numericKind the point encoding for {@link FieldKind#NUMERIC}, else null
	 * @param attribute the attribute the field carries values of
	 * @param docValues whether the field also has doc values, so it can sort
	 */
	public record Field(String name, FieldKind kind, NumericKind numericKind, EAttribute attribute,
			boolean docValues) {

		/** Whether a term-level query may read this field directly. */
		public boolean isTermReadable() {
			return kind == FieldKind.KEYWORD;
		}
	}

	/**
	 * One resolved geographic point field (S9, #13).
	 * <p>
	 * A geo field is not a {@link Field}: it carries a position rather than a value of one
	 * attribute, and the query vocabulary reaches it through a {@code GeoSubject} binding
	 * rather than through a feature path. The three authoring shapes of §5.5 all end here,
	 * and after indexing they are indistinguishable — the same {@code LatLonPoint}.
	 *
	 * @param name the Lucene field name
	 * @param latitude the latitude attribute of the split shape, else null
	 * @param longitude the longitude attribute of the split shape, else null
	 * @param pointReference the reference holding the packed point object, else null
	 * @param coordinates the many-valued {@code [lon, lat]} attribute of a packed shape,
	 *        on the referenced class when {@code pointReference} is set and on the mapped
	 *        class itself when it is not; null for the split shape
	 * @param docValues whether the field also has doc values, so a distance sort can read it
	 */
	/**
	 * One declared rank signal (§5.3, S14): the feature name it is written under in
	 * {@link SearchFields#FEATURES}, the saturating function with its parameters, and the
	 * weight the mapping's {@code boost} sets. A pivot of zero means "not declared" — the
	 * value must be positive, so there is no ambiguity to resolve with an unsettable flag.
	 */
	public record RankSignal(String name, RankFunction function, double pivot, double exponent,
			float weight, EAttribute attribute) {

		/** Whether a pivot was declared; SATURATION derives one from index statistics without. */
		public boolean hasPivot() {
			return pivot > 0;
		}
	}

	public record GeoField(String name, EAttribute latitude, EAttribute longitude,
			EReference pointReference, EAttribute coordinates, boolean docValues) {

		/** Whether the position is authored as two separate attributes. */
		public boolean isSplit() {
			return latitude != null;
		}
	}

	private final IndexUnitMapping mapping;
	private final String typeField;
	private final Map<EClass, DocumentMapping> declared = new HashMap<>();
	private final Map<String, EClass> byTypeName = new HashMap<>();

	private IndexSchema(IndexUnitMapping mapping) {
		this.mapping = mapping;
		this.typeField = mapping.getTypeField() == null || mapping.getTypeField().isBlank()
				? SearchFields.TYPE
				: mapping.getTypeField();
		for (DocumentMapping document : mapping.getDocuments()) {
			if (document.getEClass() == null) {
				throw new MappingException("A document mapping in unit '" + mapping.getName()
						+ "' declares no EClass");
			}
			if (document.getEClass().getEPackage() != mapping.getEPackage()) {
				// A resolution-time configuration error (#32), not a per-document surprise:
				// a unit indexes one package universe, and a class from elsewhere means the
				// mapping and the unit disagree about which model this index is for.
				throw new MappingException("Unit '" + mapping.getName() + "' declares the package '"
						+ mapping.getEPackage().getNsURI() + "' but maps "
						+ document.getEClass().getName() + " from '"
						+ (document.getEClass().getEPackage() == null ? "no package"
								: document.getEClass().getEPackage().getNsURI())
						+ "'.");
			}
			DocumentMapping previous = declared.put(document.getEClass(), document);
			if (previous != null) {
				throw new MappingException("Unit '" + mapping.getName() + "' maps "
						+ document.getEClass().getName() + " twice");
			}
		}
		for (Object classifier : mapping.getEPackage().getEClassifiers()) {
			if (classifier instanceof EClass eClass) {
				String typeName = typeNameOf(eClass);
				EClass other = byTypeName.put(typeName, eClass);
				if (other != null) {
					throw new MappingException("Unit '" + mapping.getName() + "' writes the type name '"
							+ typeName + "' for both " + other.getName() + " and " + eClass.getName()
							+ ", so a document carrying it could not be read back as one class.");
				}
			}
		}
		// Everything about a computed field that can be known before a document exists is
		// checked here (S20): paths against the model, expressions parsed and type-checked.
		for (DocumentMapping document : mapping.getDocuments()) {
			for (FieldMapping field : document.getFields()) {
				ValueSources.validate(document.getEClass(), field, displayName(field));
				for (FieldMapping sub : field.getSubFields()) {
					ValueSources.refuseComputedSubField(sub, displayName(field) + "." + sub.getName());
				}
			}
		}
	}

	/** Derives the schema of a unit. */
	public static IndexSchema of(IndexUnitMapping mapping) {
		Objects.requireNonNull(mapping, "mapping");
		if (mapping.getEPackage() == null) {
			throw new MappingException("Index unit mapping '" + mapping.getName() + "' declares no EPackage");
		}
		return new IndexSchema(mapping);
	}

	/** The mapping this schema was derived from. */
	public IndexUnitMapping mapping() {
		return mapping;
	}

	/** A field's name for a message, where a composite mapping may have no attribute at all. */
	private static String displayName(FieldMapping field) {
		if (field.getName() != null && !field.getName().isBlank()) {
			return field.getName();
		}
		return field.getFeature() != null ? field.getFeature().getName() : field.eClass().getName();
	}

	/**
	 * What the computed fields of this class read beyond the object itself, as dotted paths
	 * (S20, §4.2). Declared for a {@code PathSource}, compiled out of the expression for an
	 * {@code OclSource}.
	 * <p>
	 * These are the documents' hidden dependencies made visible: the value is recomputed when
	 * <em>this</em> object is saved, so a change to something on one of these paths leaves the
	 * document stale until its owner is written again. A stream-fed index (S10) needs exactly
	 * this list to know better.
	 */
	public Set<String> dependencies(EClass owner) {
		DocumentMapping document = documentMapping(owner);
		if (document == null) {
			return Set.of();
		}
		Set<String> paths = new LinkedHashSet<>();
		for (FieldMapping field : document.getFields()) {
			paths.addAll(ValueSources.dependencies(field));
		}
		return paths;
	}

	/**
	 * A stable fingerprint of everything in this mapping that decides <em>what ends up in the
	 * index</em> (S20). Two schemas that would write the same documents share it; changing a
	 * field name, an analyzer declaration or the text of an OCL expression changes it.
	 * <p>
	 * It exists because a computed field makes the mapping interpretation-relevant metadata:
	 * an index written under one expression and read under another is silently wrong, and the
	 * only honest answer is a rebuild. Recording this next to the data — the unit's commit
	 * data is the place (S18) — is what lets a deployment notice.
	 */
	public String fingerprint() {
		StringBuilder text = new StringBuilder();
		for (Object content : (Iterable<Object>) () -> EcoreUtil.getAllContents(mapping, true)) {
			if (content instanceof EObject part) {
				text.append(part.eClass().getName()).append('{');
				for (EStructuralFeature feature : part.eClass().getEAllStructuralFeatures()) {
					if (feature instanceof EAttribute attribute && part.eIsSet(attribute)) {
						text.append(attribute.getName()).append('=').append(part.eGet(attribute))
								.append(';');
					}
					if (feature instanceof EReference reference && !reference.isContainment()
							&& part.eIsSet(reference)) {
						text.append(reference.getName()).append("->")
								.append(referenceText(part.eGet(reference))).append(';');
					}
				}
				text.append('}');
			}
		}
		return Integer.toHexString(text.toString().hashCode());
	}

	private static String referenceText(Object value) {
		if (value instanceof Collection<?> targets) {
			List<String> names = new ArrayList<>(targets.size());
			targets.forEach(target -> names.add(referenceText(target)));
			return String.join(",", names);
		}
		if (value instanceof ENamedElement named) {
			return named.getName();
		}
		return String.valueOf(value);
	}

	/** The name of the type discriminator field. */
	public String typeField() {
		return typeField;
	}

	/** The declared mapping for this class, or the nearest one inherited from a supertype. */
	public DocumentMapping documentMapping(EClass eClass) {
		DocumentMapping own = declared.get(eClass);
		if (own != null) {
			return own;
		}
		for (EClass superType : eClass.getEAllSuperTypes()) {
			DocumentMapping inherited = declared.get(superType);
			if (inherited != null) {
				return inherited;
			}
		}
		return null;
	}

	/** The type-discriminator value written for objects of this class. */
	public String typeNameOf(EClass eClass) {
		DocumentMapping documentMapping = documentMapping(eClass);
		if (documentMapping != null && documentMapping.getTypeName() != null
				&& !documentMapping.getTypeName().isBlank()
				&& documentMapping.getEClass() == eClass) {
			// A declared type name belongs to the class that declared it. A subclass
			// inheriting the mapping still writes its own name, otherwise a TYPE_FILTER
			// could never tell the two apart.
			return documentMapping.getTypeName();
		}
		return eClass.getName();
	}

	/**
	 * The class behind a type-discriminator value — the reverse of {@link #typeNameOf}, for
	 * reading documents back.
	 *
	 * @throws MappingException if no class of this unit's EPackage writes that name
	 */
	public EClass eClassOf(String typeName) {
		EClass eClass = eClassOfOrNull(typeName);
		if (eClass == null) {
			throw new MappingException("No class of unit '" + mapping.getName() + "' writes the type name '"
					+ typeName + "'. Either the document was written by another mapping, or the mapping "
					+ "changed since — both mean the index and this schema disagree.");
		}
		return eClass;
	}

	/**
	 * The class behind a type-discriminator value, or {@code null} when this unit writes no
	 * such name — the probing form of {@link #eClassOf}, for a caller that answers an unknown
	 * name with a diagnostic of its own instead of a failure.
	 */
	public EClass eClassOfOrNull(String typeName) {
		return byTypeName.get(typeName);
	}

	/**
	 * The materialization a class declares — inherited with its document mapping, like
	 * everything else — or null for the default tier, partial reconstruction (§4.3). This
	 * is also the static answer behind the per-EClass {@code UPDATE_BY_SELECTOR} gate:
	 * only a declared {@code STORED_OBJECT} makes a complete rewrite possible.
	 */
	public Materialization materialization(EClass eClass) {
		DocumentMapping documentMapping = documentMapping(eClass);
		return documentMapping == null ? null : documentMapping.getMaterialization();
	}

	/** The stored field a materialization writes into; {@link SearchFields#SOURCE} unless named. */
	public String materializationField(Materialization materialization) {
		String fieldName = materialization.getFieldName();
		return fieldName == null || fieldName.isBlank() ? SearchFields.SOURCE : fieldName;
	}

	/** The id attribute of a class, declared or intrinsic; null if it has neither. */
	public EAttribute idAttribute(EClass eClass) {
		DocumentMapping documentMapping = documentMapping(eClass);
		if (documentMapping != null && documentMapping.getIdFeature() != null) {
			return documentMapping.getIdFeature();
		}
		return eClass.getEIDAttribute();
	}

	/** The effective field name of an attribute under a mapping, prefix excluded. */
	public String fieldName(EAttribute attribute, FieldMapping field) {
		if (field != null && field.getName() != null && !field.getName().isBlank()) {
			return field.getName();
		}
		return attribute.getName();
	}

	/**
	 * The geographic point fields declared on a class (S9, #13), in declaration order.
	 * <p>
	 * Query-side matching of a {@code GeoSubject} against these is the translator's job —
	 * the schema only answers what was declared, and refuses a declaration that names no
	 * usable shape.
	 */
	public List<GeoField> geoFields(EClass owner) {
		DocumentMapping documentMapping = documentMapping(owner);
		if (documentMapping == null) {
			return List.of();
		}
		List<GeoField> fields = new ArrayList<>();
		for (FieldMapping field : documentMapping.getFields()) {
			if (field instanceof GeoPointFieldMapping geo) {
				fields.add(geoField(owner, geo));
			}
		}
		return List.copyOf(fields);
	}

	/**
	 * The attributes a geo declaration consumes as a packed position on the object itself.
	 * <p>
	 * Such an attribute is a position, not a value: indexing {@code [lon, lat]} a second
	 * time as a pair of plain numbers would put two field types under one name — which
	 * Lucene refuses outright — and would answer questions ("corner = 11.586") that mean
	 * nothing. A split pair is the opposite case: {@code lat} and {@code lon} are ordinary
	 * scalars that a query may compare on their own, so they stay ordinary fields.
	 */
	public Set<EAttribute> geoConsumedAttributes(EClass owner) {
		DocumentMapping documentMapping = documentMapping(owner);
		if (documentMapping == null) {
			return Set.of();
		}
		Set<EAttribute> consumed = new HashSet<>();
		for (FieldMapping field : documentMapping.getFields()) {
			if (field instanceof GeoPointFieldMapping geo && geo.getPointReference() == null) {
				if (geo.getCoordinates() != null) {
					consumed.add(geo.getCoordinates());
				} else if (geo.getFeature() != null && geo.getLatitude() == null) {
					consumed.add(geo.getFeature());
				}
			}
		}
		return Set.copyOf(consumed);
	}

	/**
	 * Resolves one geo declaration, refusing anything that is not exactly one of the three
	 * shapes. The name defaults to what the query side will name: the reference for a packed
	 * point, the attribute for a self-carried one. The split pair has no such natural name,
	 * so it must be named explicitly.
	 */
	private GeoField geoField(EClass owner, GeoPointFieldMapping geo) {
		boolean split = geo.getLatitude() != null || geo.getLongitude() != null;
		boolean packed = geo.getPointReference() != null;
		boolean selfPacked = !packed && geo.getCoordinates() != null;
		boolean combined = geo.getFeature() != null;
		int shapes = (split ? 1 : 0) + (packed ? 1 : 0) + (selfPacked ? 1 : 0) + (combined ? 1 : 0);
		if (shapes != 1) {
			throw new MappingException("The geo field on " + owner.getName() + " declares "
					+ (shapes == 0 ? "no coordinate shape" : "more than one coordinate shape")
					+ ". Use exactly one: latitude+longitude, pointReference+coordinates, or a single "
					+ "many-valued [lon, lat] attribute.");
		}
		if (split) {
			if (geo.getLatitude() == null || geo.getLongitude() == null) {
				throw new MappingException("The split geo field on " + owner.getName() + " declares only "
						+ (geo.getLatitude() == null ? "longitude" : "latitude")
						+ ". A position needs both.");
			}
			String name = geo.getName();
			if (name == null || name.isBlank()) {
				throw new MappingException("The geo field over '" + geo.getLatitude().getName() + "'/'"
						+ geo.getLongitude().getName() + "' on " + owner.getName() + " has no name. A split "
						+ "pair has no natural field name, so name it explicitly.");
			}
			return new GeoField(name, geo.getLatitude(), geo.getLongitude(), null, null, geo.isDocValues());
		}
		if (packed) {
			EAttribute coordinates = geo.getCoordinates();
			if (coordinates == null) {
				throw new MappingException("The packed geo field over '" + geo.getPointReference().getName()
						+ "' on " + owner.getName() + " names no coordinates attribute.");
			}
			String name = geo.getName() == null || geo.getName().isBlank()
					? geo.getPointReference().getName()
					: geo.getName();
			return new GeoField(name, null, null, geo.getPointReference(), coordinates, geo.isDocValues());
		}
		EAttribute coordinates = selfPacked ? geo.getCoordinates() : geo.getFeature();
		if (!coordinates.isMany()) {
			throw new MappingException("The geo field over '" + coordinates.getName() + "' on "
					+ owner.getName() + " reads one attribute, so that attribute must be many-valued and "
					+ "carry [lon, lat]. For two separate attributes declare latitude and longitude.");
		}
		String name = geo.getName() == null || geo.getName().isBlank()
				? coordinates.getName()
				: geo.getName();
		return new GeoField(name, null, null, null, coordinates, geo.isDocValues());
	}

	/** The declared field mapping of an attribute, or null if it follows convention. */
	/**
	 * Every rank signal a query rooted at this class can select (§5.3, S14) — declared on
	 * the class itself, inherited with its document mapping, or declared by one of its
	 * indexed subtypes, which is what a polymorphic query reads too. Keyed by feature name,
	 * in declaration order.
	 * <p>
	 * A signal is either a primary projection of its attribute — the attribute is then a
	 * feature weight and no longer a comparable field — or a sub-field beside an ordinary
	 * one, which is how an attribute stays sortable while also feeding the score.
	 *
	 * @throws MappingException if two declarations share a feature name but disagree about
	 *         what it means; one name is one signal, and picking a winner would silently
	 *         score half the corpus differently
	 */
	public Map<String, RankSignal> rankSignals(EClass root) {
		Map<String, RankSignal> signals = new LinkedHashMap<>();
		for (DocumentMapping document : mapping.getDocuments()) {
			EClass owner = document.getEClass();
			if (owner == null || !(root.isSuperTypeOf(owner) || owner.isSuperTypeOf(root))) {
				continue;
			}
			for (FieldMapping field : document.getFields()) {
				EAttribute attribute = field.getFeature();
				if (field instanceof RankSignalFieldMapping rank) {
					addRankSignal(signals, rank, attribute, fieldName(attribute, rank), owner);
				}
				for (FieldMapping sub : field.getSubFields()) {
					if (sub instanceof RankSignalFieldMapping rank) {
						addRankSignal(signals, rank, attribute,
								fieldName(attribute, field) + "." + sub.getName(), owner);
					}
				}
			}
		}
		return signals;
	}

	private void addRankSignal(Map<String, RankSignal> into, RankSignalFieldMapping mapping,
			EAttribute attribute, String name, EClass owner) {
		if (attribute == null) {
			throw new MappingException("The rank signal '" + name + "' on " + owner.getName()
					+ " declares no attribute. A signal is one number per document, read from one "
					+ "attribute.");
		}
		if (attribute.isMany()) {
			throw new MappingException("The rank signal '" + name + "' on " + owner.getName()
					+ " reads the many-valued attribute '" + attribute.getName() + "'. A signal is one "
					+ "number per document — Lucene keeps one weight per feature name.");
		}
		RankSignal signal = new RankSignal(name, mapping.getFunction(), mapping.getPivot(),
				mapping.getExponent(), mapping.getBoost(), attribute);
		RankSignal previous = into.putIfAbsent(name, signal);
		if (previous != null && !previous.equals(signal)) {
			throw new MappingException("Two rank signals are declared under the name '" + name
					+ "' with different parameters (" + previous + " and " + signal + "). One name is "
					+ "one signal — a query selecting it must mean the same thing for every document "
					+ "it can reach.");
		}
	}

	public FieldMapping fieldMapping(EClass owner, EAttribute attribute) {
		DocumentMapping documentMapping = documentMapping(owner);
		if (documentMapping == null) {
			return null;
		}
		for (FieldMapping field : documentMapping.getFields()) {
			if (field.getFeature() == attribute) {
				return field;
			}
		}
		return null;
	}

	/**
	 * Resolves a feature path against a root class.
	 * <p>
	 * The last segment must be an attribute; every segment before it must be a reference
	 * the mapping flattened into this document with {@link ReferenceStrategy#EMBED},
	 * because that is the only strategy whose values live under a prefixed name in the
	 * same document. {@code NESTED} children are separate documents (reachable only
	 * through a block join, S11) and {@code ID_ONLY} keeps no values at all.
	 *
	 * @throws MappingException if the path does not resolve to a readable field
	 */
	public Field resolve(EClass root, List<EStructuralFeature> segments) {
		Objects.requireNonNull(root, "root");
		if (segments == null || segments.isEmpty()) {
			throw new MappingException("An empty feature path resolves to no field");
		}
		EStructuralFeature last = segments.get(segments.size() - 1);
		if (!(last instanceof EAttribute attribute)) {
			throw new MappingException("Feature path '" + pathOf(segments) + "' ends on the reference '"
					+ last.getName() + "'. Only attribute values are indexed as fields; a reference is "
					+ "reachable through its strategy (ID_ONLY term, EMBED prefix, NESTED block).");
		}
		StringBuilder prefix = new StringBuilder();
		EClass owner = root;
		for (int i = 0; i < segments.size() - 1; i++) {
			EStructuralFeature segment = segments.get(i);
			if (!(segment instanceof EReference reference)) {
				throw new MappingException("Feature path '" + pathOf(segments) + "' traverses the attribute '"
						+ segment.getName() + "', which carries a value and not a target");
			}
			ReferenceMapping referenceMapping = referenceMapping(owner, reference);
			if (referenceMapping == null || referenceMapping.getStrategy() != ReferenceStrategy.EMBED) {
				throw new MappingException("Feature path '" + pathOf(segments) + "' crosses '"
						+ reference.getName() + "', which is "
						+ (referenceMapping == null ? "not mapped"
								: "mapped " + referenceMapping.getStrategy())
						+ ". A path can only continue through a reference mapped EMBED, whose values are "
						+ "flattened into this document.");
			}
			prefix.append(embedPrefix(referenceMapping, reference)).append('.');
			owner = reference.getEReferenceType();
		}
		return fieldOf(owner, attribute, prefix.toString());
	}

	/** Resolves a single local attribute of a class. */
	public Field resolve(EClass owner, EAttribute attribute) {
		return fieldOf(owner, attribute, "");
	}

	private Field fieldOf(EClass owner, EAttribute attribute, String prefix) {
		if (geoConsumedAttributes(owner).contains(attribute)) {
			throw new MappingException("Attribute '" + attribute.getName() + "' carries a geographic "
					+ "position, which is indexed as a point rather than as a comparable value. Read it "
					+ "with a geo predicate (geoWithin / geoDistance), not as a field.");
		}
		FieldMapping declaredField = fieldMapping(owner, attribute);
		String name = prefix + fieldName(attribute, declaredField);
		if (declaredField == null) {
			if (attribute.isDerived() || attribute.isTransient()) {
				throw new MappingException("Attribute '" + attribute.getName() + "' is "
						+ (attribute.isDerived() ? "derived" : "transient")
						+ " and therefore not indexed by convention. Declare a field mapping for it to "
						+ "make it queryable.");
			}
			return conventionField(attribute, name);
		}
		if (!declaredField.isIndexed()) {
			throw new MappingException("Field '" + name + "' is mapped with indexed=false, so it carries no "
					+ "searchable terms. It can be stored or sorted, but not filtered on.");
		}
		if (declaredField instanceof TextFieldMapping) {
			return new Field(name, FieldKind.TEXT, null, attribute, declaredField.isDocValues());
		}
		if (declaredField instanceof KeywordFieldMapping) {
			return new Field(name, FieldKind.KEYWORD, null, attribute, declaredField.isDocValues());
		}
		if (declaredField instanceof NumericFieldMapping numeric) {
			NumericKind kind = numeric.getKind() == null || numeric.getKind() == NumericKind.AUTO
					? numericKindOf(attribute)
					: numeric.getKind();
			return new Field(name, FieldKind.NUMERIC, kind, attribute, declaredField.isDocValues());
		}
		if (declaredField instanceof GeoPointFieldMapping) {
			throw new MappingException("Attribute '" + attribute.getName() + "' carries a geographic "
					+ "position, which is indexed as a point rather than as a comparable value. Read it "
					+ "with a geo predicate (geoWithin / geoDistance), not as a field.");
		}
		if (declaredField instanceof RankSignalFieldMapping) {
			throw new MappingException("Attribute '" + attribute.getName() + "' is declared as the rank "
					+ "signal '" + name + "', which is indexed as a quantized feature weight rather than "
					+ "as a value. Select it for a query's score instead of filtering or sorting on it; "
					+ "declare a numeric projection beside it if the number itself must be comparable.");
		}
		throw new MappingException("Field mapping " + declaredField.eClass().getName() + " on '" + name
				+ "' is not implemented, so no query can read it. See docs/search-access.md §7 for which "
				+ "task owns it.");
	}

	/** The field an unmapped attribute lands in: derived from its type, never from its name. */
	private Field conventionField(EAttribute attribute, String name) {
		Class<?> type = attribute.getEAttributeType().getInstanceClass();
		if (attribute.isID() || attribute.getEAttributeType() instanceof EEnum || isBoolean(type)) {
			return new Field(name, FieldKind.KEYWORD, null, attribute, true);
		}
		if (isNumeric(type) || Date.class.isAssignableFrom(nonNull(type))) {
			return new Field(name, FieldKind.NUMERIC, numericKindOf(attribute), attribute, true);
		}
		return new Field(name, FieldKind.TEXT, null, attribute, false);
	}

	/** The point encoding of a numeric or temporal attribute. */
	public NumericKind numericKindOf(EAttribute attribute) {
		Class<?> type = nonNull(attribute.getEAttributeType().getInstanceClass());
		if (Date.class.isAssignableFrom(type)) {
			return NumericKind.DATE;
		}
		if (type == int.class || type == Integer.class || type == short.class || type == Short.class
				|| type == byte.class || type == Byte.class) {
			return NumericKind.INT;
		}
		if (type == long.class || type == Long.class) {
			return NumericKind.LONG;
		}
		if (type == float.class || type == Float.class) {
			return NumericKind.FLOAT;
		}
		if (type == double.class || type == Double.class) {
			return NumericKind.DOUBLE;
		}
		throw new MappingException("Attribute '" + attribute.getName() + "' of type "
				+ attribute.getEAttributeType().getName() + " is mapped as numeric, but its Java type "
				+ type.getName() + " is not one this backend knows how to encode as a point.");
	}

	/** The declared mapping of a reference, or null. */
	public ReferenceMapping referenceMapping(EClass owner, EReference reference) {
		DocumentMapping documentMapping = documentMapping(owner);
		if (documentMapping == null) {
			return null;
		}
		for (ReferenceMapping candidate : documentMapping.getReferences()) {
			if (candidate.getEReference() == reference) {
				return candidate;
			}
		}
		return null;
	}

	/** The name prefix an EMBED reference contributes. */
	public String embedPrefix(ReferenceMapping reference, EReference eReference) {
		return referenceFieldName(reference, eReference);
	}

	/** The field name a reference writes under: its declared prefix, else the reference name. */
	public String referenceFieldName(ReferenceMapping reference, EReference eReference) {
		return reference.getPrefix() == null || reference.getPrefix().isBlank()
				? eReference.getName()
				: reference.getPrefix();
	}

	/**
	 * Every ID_ONLY reference field in this unit that could hold the id of an object of this
	 * class — what a delete has to look at before it leaves a reference pointing at nothing
	 * (§8, emf.persistence-jpa#195). ID_ONLY is the only strategy that can dangle:
	 * containment is ownership and goes with its parent, and an EMBED copy is a value.
	 * <p>
	 * "Could hold" is deliberately generous in both directions — a reference typed on a
	 * supertype of the deleted class can point at it, and a URI scoped on an abstract class
	 * covers the subtypes references are typed on. An id says nothing about its type, so the
	 * probe over-approximates rather than miss a dangling reference.
	 */
	public Set<String> incomingIdOnlyFields(EClass target) {
		Set<String> names = new LinkedHashSet<>();
		for (DocumentMapping document : mapping.getDocuments()) {
			for (ReferenceMapping reference : document.getReferences()) {
				EReference eReference = reference.getEReference();
				if (reference.getStrategy() != ReferenceStrategy.ID_ONLY || eReference == null) {
					continue;
				}
				EClass referenced = eReference.getEReferenceType();
				if (referenced != null
						&& (referenced.isSuperTypeOf(target) || target.isSuperTypeOf(referenced))) {
					names.add(referenceFieldName(reference, eReference));
				}
			}
		}
		return names;
	}

	private static String pathOf(List<EStructuralFeature> segments) {
		List<String> names = new ArrayList<>(segments.size());
		for (EStructuralFeature segment : segments) {
			names.add(segment.getName());
		}
		return String.join(".", names);
	}

	static boolean isBoolean(Class<?> type) {
		return type == boolean.class || type == Boolean.class;
	}

	static boolean isNumeric(Class<?> type) {
		Class<?> t = nonNull(type);
		return t.isPrimitive() && t != boolean.class && t != char.class || Number.class.isAssignableFrom(t);
	}

	static Class<?> nonNull(Class<?> type) {
		return type == null ? Object.class : type;
	}
	/**
	 * A term set over the discriminator values of a class and its indexed concrete
	 * subtypes — the "only this type's documents" filter every read and query applies.
	 *
	 * @throws MappingException if no indexed concrete class matches the type, so a read
	 *         from it could never have a hit — refused rather than silently empty
	 */
	public Query typeFilter(EClass type) {
		TreeSet<BytesRef> names = new TreeSet<>();
		if (!type.isAbstract() && !type.isInterface()) {
			names.add(new BytesRef(typeNameOf(type)));
		}
		for (EClassifier classifier : mapping.getEPackage().getEClassifiers()) {
			if (classifier instanceof EClass candidate && candidate != type && !candidate.isAbstract()
					&& !candidate.isInterface() && type.isSuperTypeOf(candidate)) {
				names.add(new BytesRef(typeNameOf(candidate)));
			}
		}
		if (names.isEmpty()) {
			throw new MappingException("No indexed concrete class matches type " + type.getName()
					+ ", so a read from it can never have a hit. Refused rather than silently empty.");
		}
		return new TermInSetQuery(typeField(), new ArrayList<>(names));
	}

}
