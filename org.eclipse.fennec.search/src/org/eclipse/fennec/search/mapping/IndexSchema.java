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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.FieldMapping;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.Materialization;
import org.eclipse.fennec.search.esearch.NumericFieldMapping;
import org.eclipse.fennec.search.esearch.NumericKind;
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
		EClass eClass = byTypeName.get(typeName);
		if (eClass == null) {
			throw new MappingException("No class of unit '" + mapping.getName() + "' writes the type name '"
					+ typeName + "'. Either the document was written by another mapping, or the mapping "
					+ "changed since — both mean the index and this schema disagree.");
		}
		return eClass;
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

	/** The declared field mapping of an attribute, or null if it follows convention. */
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
		return reference.getPrefix() == null || reference.getPrefix().isBlank()
				? eReference.getName()
				: reference.getPrefix();
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
}
