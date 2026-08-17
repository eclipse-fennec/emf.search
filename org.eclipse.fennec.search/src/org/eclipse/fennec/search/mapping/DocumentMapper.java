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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.util.BytesRef;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.FieldMapping;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.NumericFieldMapping;
import org.eclipse.fennec.search.esearch.NumericKind;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.esearch.TextFieldMapping;

/**
 * Turns EObjects into Lucene documents according to an {@code esearch} mapping.
 * <p>
 * Two rules shape the whole class. <b>Conventions over declarations</b>: an EClass or an
 * attribute with no mapping is still indexed, by a default derived from its type, so a
 * small model needs no mapping instance at all and a declaration is always an override.
 * <b>Refuse rather than approximate</b>: a mapping that asks for something not implemented
 * — a geo field, an interval, a rank signal, a vector — fails with a {@link
 * MappingException} naming it, instead of quietly indexing something else.
 * <p>
 * The mapper is immutable and stateless per call, so one instance serves an index unit for
 * its lifetime and can be used from several threads.
 *
 * @author Data In Motion Consulting
 */
public final class DocumentMapper {

	private final IndexUnitMapping mapping;
	private final IndexSchema schema;

	private DocumentMapper(IndexSchema schema) {
		this.schema = schema;
		this.mapping = schema.mapping();
	}

	/** Compiles a mapping into a mapper. */
	public static DocumentMapper of(IndexUnitMapping mapping) {
		return new DocumentMapper(IndexSchema.of(mapping));
	}

	/** Maps against an already derived schema, which the query side shares. */
	public static DocumentMapper of(IndexSchema schema) {
		Objects.requireNonNull(schema, "schema");
		return new DocumentMapper(schema);
	}

	/** The schema this mapper writes against; the query translation reads the same one. */
	public IndexSchema schema() {
		return schema;
	}

	/** The name of the type discriminator field this mapper writes. */
	public String typeField() {
		return schema.typeField();
	}

	/**
	 * Maps one object.
	 *
	 * @throws MappingException if the object's class is not indexed by this unit, has no
	 *         usable id, or its mapping asks for something not implemented
	 */
	public MappedDocument map(EObject object) {
		Objects.requireNonNull(object, "object");
		EClass eClass = object.eClass();
		DocumentMapping documentMapping = resolve(eClass);
		if (documentMapping == null && !mapping.isAutoMap()) {
			throw new MappingException("Unit '" + mapping.getName() + "' does not index "
					+ eClass.getName() + ", and automatic mapping is switched off");
		}
		String id = idOf(object, documentMapping, eClass);

		List<Document> children = new ArrayList<>();
		Document root = new Document();
		writeSystemFields(root, id, id, typeNameOf(documentMapping, eClass), true);
		writeAttributes(root, object, documentMapping, "");
		writeReferences(root, children, object, documentMapping, id, 1);

		List<Document> block = new ArrayList<>(children);
		block.add(root);
		return new MappedDocument(id, block);
	}

	// --- resolution ---------------------------------------------------------------------

	/** The declared mapping for this class, or the nearest one inherited from a supertype. */
	private DocumentMapping resolve(EClass eClass) {
		return schema.documentMapping(eClass);
	}

	private String typeNameOf(DocumentMapping documentMapping, EClass eClass) {
		return schema.typeNameOf(eClass);
	}

	private String idOf(EObject object, DocumentMapping documentMapping, EClass eClass) {
		EAttribute idAttribute = schema.idAttribute(eClass);
		if (idAttribute == null) {
			throw new MappingException(eClass.getName() + " has no id attribute and its mapping declares "
					+ "none. Every indexed object needs a stable id — without one it cannot be updated "
					+ "or deleted.");
		}
		Object value = object.eGet(idAttribute);
		if (value == null || value.toString().isBlank()) {
			throw new MappingException("Object of type " + eClass.getName() + " has no value for its id "
					+ "attribute '" + idAttribute.getName() + "'");
		}
		return EcoreUtil.convertToString((EDataType) idAttribute.getEType(), value);
	}

	// --- writing ------------------------------------------------------------------------

	private void writeSystemFields(Document document, String id, String rootId, String typeName,
			boolean isRoot) {
		document.add(new StringField(SearchFields.ID, id, Field.Store.YES));
		document.add(new StringField(SearchFields.ROOT, rootId, Field.Store.YES));
		document.add(new StringField(schema.typeField(), typeName, Field.Store.YES));
		if (isRoot) {
			document.add(new StringField(SearchFields.PARENT, SearchFields.PARENT_VALUE, Field.Store.NO));
		}
	}

	private void writeAttributes(Document document, EObject object, DocumentMapping documentMapping,
			String prefix) {
		Map<EAttribute, FieldMapping> byFeature = new HashMap<>();
		if (documentMapping != null) {
			for (FieldMapping field : documentMapping.getFields()) {
				if (field.getFeature() == null) {
					throw new MappingException("A field mapping of " + documentMapping.getEClass().getName()
							+ " declares no feature. Computed values without a feature are not implemented "
							+ "yet (see issue #28).");
				}
				byFeature.put(field.getFeature(), field);
			}
		}

		boolean autoMap = documentMapping == null || documentMapping.isAutoMap();
		for (EAttribute attribute : object.eClass().getEAllAttributes()) {
			FieldMapping field = byFeature.get(attribute);
			if (field == null) {
				if (!autoMap || attribute.isDerived() || attribute.isTransient()) {
					continue;
				}
				writeConvention(document, object, attribute, prefix);
				continue;
			}
			writeDeclared(document, object, attribute, field, prefix, prefix + schema.fieldName(attribute, field));
		}
	}

	/** The default for an attribute nobody mapped: derived from its type, never from its name. */
	private void writeConvention(Document document, EObject object, EAttribute attribute, String prefix) {
		if (isAbsent(object, attribute)) {
			return;
		}
		String name = prefix + attribute.getName();
		for (Object value : valuesOf(object, attribute)) {
			if (value == null) {
				continue;
			}
			Class<?> type = attribute.getEAttributeType().getInstanceClass();
			if (attribute.isID() || attribute.getEAttributeType() instanceof EEnum || IndexSchema.isBoolean(type)) {
				addKeyword(document, name, stringOf(attribute, value), attribute.isID(), true);
			} else if (IndexSchema.isNumeric(type) || Date.class.isAssignableFrom(IndexSchema.nonNull(type))) {
				addNumeric(document, name, attribute, value, NumericKind.AUTO, false, true,
						attribute.isMany());
			} else {
				addText(document, name, stringOf(attribute, value), false, false);
			}
		}
	}

	private void writeDeclared(Document document, EObject object, EAttribute attribute, FieldMapping field,
			String prefix, String name) {
		refuseUnimplemented(field, attribute);
		if (!isAbsent(object, attribute)) {
			for (Object value : valuesOf(object, attribute)) {
				if (value == null) {
					continue;
				}
				writeValue(document, name, attribute, field, value);
			}
		}
		for (FieldMapping sub : field.getSubFields()) {
			if (sub.getFeature() != null && sub.getFeature() != attribute) {
				throw new MappingException("Sub-field '" + sub.getName() + "' of '" + name
						+ "' declares its own feature. A sub-field is another projection of its parent's "
						+ "attribute and inherits it.");
			}
			if (sub.getName() == null || sub.getName().isBlank()) {
				throw new MappingException("A sub-field of '" + name + "' has no name. Sub-field names are "
						+ "relative and mandatory — the effective field name is parent.child.");
			}
			if (!sub.getSubFields().isEmpty()) {
				throw new MappingException("Sub-field '" + name + "." + sub.getName()
						+ "' declares sub-fields of its own; only one level of nesting is meaningful.");
			}
			writeDeclared(document, object, attribute, sub, prefix, name + "." + sub.getName());
		}
	}

	private void writeValue(Document document, String name, EAttribute attribute, FieldMapping field,
			Object value) {
		if (!field.isIndexed() && !field.isStored() && !field.isDocValues()) {
			return;
		}
		if (field instanceof TextFieldMapping text) {
			addText(document, name, stringOf(attribute, value), field.isStored(), text.isTermVectors());
		} else if (field instanceof KeywordFieldMapping) {
			addKeyword(document, name, stringOf(attribute, value), field.isStored(), field.isDocValues());
		} else if (field instanceof NumericFieldMapping numeric) {
			addNumeric(document, name, attribute, value, numeric.getKind(), field.isStored(),
					field.isDocValues(), attribute.isMany());
		} else {
			// The abstract FieldMapping is not instantiable, so this is a kind that exists
			// in the metamodel but has no writer yet — refuseUnimplemented named it already.
			throw new MappingException("No writer for field mapping " + field.eClass().getName());
		}
	}

	private void refuseUnimplemented(FieldMapping field, EAttribute attribute) {
		String kind = field.eClass().getName();
		String issue = switch (kind) {
			case "GeoPointFieldMapping" -> "geo mapping is S9";
			case "RangeFieldMapping" -> "interval fields are S15 (#17)";
			case "RankSignalFieldMapping" -> "rank signals are S14 (#16)";
			case "VectorFieldMapping" -> "vector fields are reserved for wave 2 and deliberately not implemented";
			default -> null;
		};
		if (issue != null) {
			throw new MappingException("Field mapping " + kind + " on '"
					+ (attribute == null ? "?" : attribute.getName()) + "' is not implemented: " + issue
					+ ". Declaring it now would index something other than what it says.");
		}
	}

	private void writeReferences(Document root, List<Document> children, EObject object,
			DocumentMapping documentMapping, String rootId, int depth) {
		if (documentMapping == null) {
			return;
		}
		for (ReferenceMapping reference : documentMapping.getReferences()) {
			EReference eReference = reference.getEReference();
			if (eReference == null) {
				throw new MappingException("A reference mapping of "
						+ documentMapping.getEClass().getName() + " declares no EReference");
			}
			if (!object.eIsSet(eReference)) {
				continue;
			}
			ReferenceStrategy strategy = reference.getStrategy();
			List<EObject> targets = targetsOf(object, eReference);
			switch (strategy) {
				case ID_ONLY -> writeIdOnly(root, reference, eReference, targets);
				case EMBED -> writeEmbedded(root, reference, eReference, targets, depth);
				case NESTED -> writeNested(children, reference, eReference, targets, rootId);
			}
		}
	}

	private void writeIdOnly(Document root, ReferenceMapping reference, EReference eReference,
			List<EObject> targets) {
		String name = reference.getPrefix() == null || reference.getPrefix().isBlank()
				? eReference.getName()
				: reference.getPrefix();
		for (EObject target : targets) {
			String id = idOf(target, resolve(target.eClass()), target.eClass());
			addKeyword(root, name, id, true, true);
		}
	}

	private void writeEmbedded(Document root, ReferenceMapping reference, EReference eReference,
			List<EObject> targets, int depth) {
		if (depth > Math.max(1, reference.getDepth())) {
			return;
		}
		String prefix = (reference.getPrefix() == null || reference.getPrefix().isBlank()
				? eReference.getName()
				: reference.getPrefix()) + ".";
		for (EObject target : targets) {
			DocumentMapping targetMapping = reference.getTarget() != null
					? reference.getTarget()
					: resolve(target.eClass());
			writeAttributes(root, target, targetMapping, prefix);
		}
	}

	private void writeNested(List<Document> children, ReferenceMapping reference, EReference eReference,
			List<EObject> targets, String rootId) {
		if (!eReference.isContainment()) {
			throw new MappingException("Reference '" + eReference.getName() + "' is mapped NESTED but is not "
					+ "a containment reference. A block is written and replaced as a whole, which only "
					+ "makes sense where the parent owns the children.");
		}
		for (EObject target : targets) {
			DocumentMapping targetMapping = reference.getTarget() != null
					? reference.getTarget()
					: resolve(target.eClass());
			Document child = new Document();
			String childId = childId(target, targetMapping, rootId, eReference, children.size());
			writeSystemFields(child, childId, rootId, typeNameOf(targetMapping, target.eClass()), false);
			// The reference name lets a block join restrict to children of one reference.
			child.add(new StringField("_nested", eReference.getName(), Field.Store.YES));
			writeAttributes(child, target, targetMapping, "");
			children.add(child);
		}
	}

	private String childId(EObject target, DocumentMapping targetMapping, String rootId,
			EReference eReference, int position) {
		EAttribute idAttribute = targetMapping != null && targetMapping.getIdFeature() != null
				? targetMapping.getIdFeature()
				: target.eClass().getEIDAttribute();
		if (idAttribute != null && target.eGet(idAttribute) != null) {
			return idOf(target, targetMapping, target.eClass());
		}
		// A contained child without an id of its own is still addressable, through its
		// position under its parent — which is exactly how EMF identifies it too.
		return rootId + "#" + eReference.getName() + "." + position;
	}

	// --- field helpers --------------------------------------------------------------------

	private void addText(Document document, String name, String value, boolean stored,
			boolean termVectors) {
		if (!termVectors) {
			document.add(new TextField(name, value, stored ? Field.Store.YES : Field.Store.NO));
			return;
		}
		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		type.setTokenized(true);
		type.setStored(stored);
		type.setStoreTermVectors(true);
		type.setStoreTermVectorPositions(true);
		type.setStoreTermVectorOffsets(true);
		type.freeze();
		document.add(new Field(name, value, type));
	}

	private void addKeyword(Document document, String name, String value, boolean stored,
			boolean docValues) {
		document.add(new StringField(name, value, stored ? Field.Store.YES : Field.Store.NO));
		if (docValues) {
			document.add(new SortedSetDocValuesField(name, new BytesRef(value)));
		}
	}

	private void addNumeric(Document document, String name, EAttribute attribute, Object value,
			NumericKind declaredKind, boolean stored, boolean docValues, boolean many) {
		NumericKind kind = declaredKind == null || declaredKind == NumericKind.AUTO
				? deriveNumericKind(attribute)
				: declaredKind;
		Number number = toNumber(attribute, value, kind);
		switch (kind) {
			case INT -> {
				document.add(new IntPoint(name, number.intValue()));
				if (docValues) {
					addNumericDocValues(document, name, number.intValue(), many);
				}
				if (stored) {
					document.add(new StoredField(name, number.intValue()));
				}
			}
			case FLOAT -> {
				document.add(new FloatPoint(name, number.floatValue()));
				if (docValues) {
					addNumericDocValues(document, name, Float.floatToRawIntBits(number.floatValue()), many);
				}
				if (stored) {
					document.add(new StoredField(name, number.floatValue()));
				}
			}
			case DOUBLE -> {
				document.add(new DoublePoint(name, number.doubleValue()));
				if (docValues) {
					addNumericDocValues(document, name, Double.doubleToRawLongBits(number.doubleValue()), many);
				}
				if (stored) {
					document.add(new StoredField(name, number.doubleValue()));
				}
			}
			case LONG, DATE -> {
				document.add(new LongPoint(name, number.longValue()));
				if (docValues) {
					addNumericDocValues(document, name, number.longValue(), many);
				}
				if (stored) {
					document.add(new StoredField(name, number.longValue()));
				}
			}
			default -> throw new MappingException("Unsupported numeric kind " + kind + " on " + name);
		}
	}

	private void addNumericDocValues(Document document, String name, long value, boolean many) {
		document.add(many ? new SortedNumericDocValuesField(name, value)
				: new NumericDocValuesField(name, value));
	}

	private NumericKind deriveNumericKind(EAttribute attribute) {
		return schema.numericKindOf(attribute);
	}

	private Number toNumber(EAttribute attribute, Object value, NumericKind kind) {
		if (value instanceof Date date) {
			return date.getTime();
		}
		if (value instanceof Number number) {
			return number;
		}
		throw new MappingException("Value '" + value + "' of attribute '" + attribute.getName()
				+ "' cannot be encoded as " + kind);
	}

	// --- small helpers ----------------------------------------------------------------------

	/**
	 * Whether the attribute has no effective value to index. {@code eIsSet} is the wrong
	 * question for a feature that is not unsettable: EMF defines it there as "differs from
	 * the default", so an attribute sitting at its type's default would vanish from the
	 * index and {@code condition = NEW} would answer differently from JPA, Mongo and the
	 * memory oracle, whose effective value is defined even at the default (#37). Only a
	 * genuine absence stays absent — an unsettable feature never set, or a null value.
	 */
	private static boolean isAbsent(EObject object, EAttribute attribute) {
		if (attribute.isUnsettable()) {
			return !object.eIsSet(attribute);
		}
		return object.eGet(attribute) == null;
	}

	@SuppressWarnings("unchecked")
	private static List<Object> valuesOf(EObject object, EStructuralFeature feature) {
		Object value = object.eGet(feature);
		if (feature.isMany()) {
			return new ArrayList<>((List<Object>) value);
		}
		return value == null ? List.of() : List.of(value);
	}

	@SuppressWarnings("unchecked")
	private static List<EObject> targetsOf(EObject object, EReference reference) {
		Object value = object.eGet(reference);
		if (reference.isMany()) {
			return new ArrayList<>((List<EObject>) value);
		}
		return value == null ? List.of() : List.of((EObject) value);
	}

	private static String stringOf(EAttribute attribute, Object value) {
		return EcoreUtil.convertToString((EDataType) attribute.getEType(), value);
	}

}
