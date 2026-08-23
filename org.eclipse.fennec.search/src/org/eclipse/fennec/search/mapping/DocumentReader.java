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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.FieldMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.Materialization;
import org.eclipse.fennec.search.esearch.MaterializationKind;
import org.eclipse.fennec.search.esearch.NumericFieldMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.esearch.TextFieldMapping;
import org.eclipse.fennec.search.materialization.ObjectSerializers;
import org.eclipse.fennec.search.resource.SearchUris;

/**
 * Turns documents back into EObjects — the inverse of {@link DocumentMapper}, reading the
 * same {@link IndexSchema} so the two directions cannot disagree about a field.
 * <p>
 * This is the default tier of the load path (docs/search-access.md §4.3): the object is
 * <b>partial by design</b>. Only what the document stores comes back — a feature mapped
 * {@code stored=false} stays unset, an {@code EMBED} reference is not reconstructed at all
 * (a flattened multi-valued embed has lost which value belonged to which target, and
 * inventing that correlation would be worse than omitting it), and an unmapped reference
 * was never written. {@link #omissions} names these statically, so a caller can say what a
 * partial object is missing instead of letting it pass for a complete one.
 * <p>
 * What does come back is faithful: attribute values through the same EMF conversion the
 * writer used, {@code NESTED} children reassembled from their block in order, and
 * {@code ID_ONLY} references as EMF proxies under {@code lucene://<unit>/<Type>/<id>}, so
 * the caller's {@code ResourceSet} resolves them back through this backend.
 *
 * @author Data In Motion Consulting
 */
public final class DocumentReader {

	private final IndexSchema schema;
	private final ObjectSerializers serializers;
	private final EPackage.Registry packages;

	private DocumentReader(IndexSchema schema, ObjectSerializers serializers, EPackage.Registry packages) {
		this.schema = schema;
		this.serializers = serializers;
		this.packages = packages;
	}

	/**
	 * A reader over the same schema the mapper writes against, with the default
	 * serializers and a package registry holding exactly the unit's EPackage.
	 */
	public static DocumentReader of(IndexSchema schema) {
		Objects.requireNonNull(schema, "schema");
		EPackageRegistryImpl packages = new EPackageRegistryImpl();
		EPackage ePackage = schema.mapping().getEPackage();
		packages.put(ePackage.getNsURI(), ePackage);
		return new DocumentReader(schema, ObjectSerializers.withDefaults(), packages);
	}

	/** A reader with an explicit serializer registry and package registry. */
	public static DocumentReader of(IndexSchema schema, ObjectSerializers serializers,
			EPackage.Registry packages) {
		Objects.requireNonNull(schema, "schema");
		Objects.requireNonNull(serializers, "serializers");
		Objects.requireNonNull(packages, "packages");
		return new DocumentReader(schema, serializers, packages);
	}

	/**
	 * The object behind a root document, by the tier its class declares (§4.3): the
	 * complete tree from the stored bytes under {@code STORED_OBJECT}, a proxy carrying
	 * the primary store's URI under {@code SOURCE_URI}, and otherwise the partial
	 * reconstruction from stored fields.
	 *
	 * @param root the root document of the block
	 * @param children the block's child documents in index order; empty for a flat object
	 * @throws MappingException if the document carries no readable type, its type name
	 *         resolves to a class this reader cannot instantiate, or its class declares a
	 *         materialization the document does not carry
	 */
	public EObject read(Document root, List<Document> children) {
		Objects.requireNonNull(root, "root");
		Objects.requireNonNull(children, "children");
		EClass eClass = eClassOf(root);
		Materialization materialization = schema.materialization(eClass);
		if (materialization != null) {
			EObject complete = materialized(root, eClass, materialization);
			if (materialization.getKind() == MaterializationKind.STORED_OBJECT) {
				// The serialized tree cannot carry a cross-document reference: at write time
				// its target lives in another document, or in no resource at all, so the
				// serializer has no href to write and drops it. The ID_ONLY fields beside the
				// bytes do carry those ids, and they are what this backend hands back as
				// proxies it can resolve — so the complete object is completed with them.
				readIdOnlyReferences(root, complete, schema.documentMapping(eClass));
			}
			return complete;
		}
		EObject object = instantiate(eClass);
		DocumentMapping documentMapping = schema.documentMapping(eClass);
		readAttributes(root, object, eClass);
		readReferences(root, children, object, documentMapping);
		return object;
	}

	/**
	 * The declared upgrade. A document without the declared field predates the
	 * declaration and is refused: silently answering with the partial tier would break
	 * "declaration = behaviour" — the caller was promised complete objects.
	 */
	private EObject materialized(Document root, EClass eClass, Materialization materialization) {
		String fieldName = schema.materializationField(materialization);
		switch (materialization.getKind()) {
			case STORED_OBJECT -> {
				BytesRef bytes = root.getBinaryValue(fieldName);
				if (bytes == null) {
					throw new MappingException(eClass.getName() + " declares STORED_OBJECT but this "
							+ "document carries no '" + fieldName + "' field — it was written before the "
							+ "declaration. The index needs a rebuild.");
				}
				try {
					return serializers.forFormat(materialization.getFormat()).deserialize(
							Arrays.copyOfRange(bytes.bytes, bytes.offset, bytes.offset + bytes.length),
							packages);
				} catch (IOException e) {
					throw new MappingException("Deserializing the stored " + eClass.getName()
							+ " failed: " + e.getMessage() + ". If the mapping's format changed since "
							+ "this document was written, the index needs a rebuild.", e);
				}
			}
			case SOURCE_URI -> {
				String uri = root.get(fieldName);
				if (uri == null) {
					throw new MappingException(eClass.getName() + " declares SOURCE_URI but this document "
							+ "carries no '" + fieldName + "' field — it was written before the "
							+ "declaration. The index needs a rebuild.");
				}
				InternalEObject proxy = (InternalEObject) instantiate(eClass);
				proxy.eSetProxyURI(URI.createURI(uri));
				return proxy;
			}
		}
		throw new MappingException("Materialization kind " + materialization.getKind()
				+ " has no reader; the metamodel grew without this backend noticing.");
	}

	/**
	 * The features of a class that a reconstructed object cannot carry, by name — declared
	 * {@code stored=false} fields, {@code EMBED} references, unmapped references, and
	 * {@code ID_ONLY} references whose target class is abstract (no proxy can be created
	 * for a type that cannot be instantiated). Statically derived from the mapping, so a
	 * resource can warn once per class rather than guess per object. A class with a
	 * declared materialization omits nothing — its objects come back complete, or resolve
	 * complete through the primary store.
	 */
	public List<String> omissions(EClass eClass) {
		if (schema.materialization(eClass) != null) {
			return List.of();
		}
		List<String> omissions = new ArrayList<>();
		DocumentMapping documentMapping = schema.documentMapping(eClass);
		for (EAttribute attribute : eClass.getEAllAttributes()) {
			FieldMapping declared = schema.fieldMapping(eClass, attribute);
			if (declared != null && !declared.isStored()) {
				omissions.add(attribute.getName());
			}
		}
		for (EReference reference : eClass.getEAllReferences()) {
			ReferenceMapping referenceMapping = referenceMapping(documentMapping, reference);
			if (referenceMapping == null
					|| referenceMapping.getStrategy() == ReferenceStrategy.EMBED
					|| (referenceMapping.getStrategy() == ReferenceStrategy.ID_ONLY
							&& reference.getEReferenceType().isAbstract())) {
				omissions.add(reference.getName());
			}
		}
		return omissions;
	}

	// --- resolution ---------------------------------------------------------------------

	private EClass eClassOf(Document document) {
		String typeName = document.get(schema.typeField());
		if (typeName == null) {
			throw new MappingException("Document carries no '" + schema.typeField() + "' field, so its "
					+ "class is unknown. Either it was written without this mapper or the unit's type "
					+ "field changed since it was indexed.");
		}
		return schema.eClassOf(typeName);
	}

	private EObject instantiate(EClass eClass) {
		if (eClass.isAbstract() || eClass.isInterface()) {
			throw new MappingException("Type '" + eClass.getName() + "' is abstract and cannot be "
					+ "instantiated. A document carrying it as its type was written against a different "
					+ "version of the model.");
		}
		return EcoreUtil.create(eClass);
	}

	// --- attributes -----------------------------------------------------------------------

	private void readAttributes(Document document, EObject object, EClass eClass) {
		for (EAttribute attribute : eClass.getEAllAttributes()) {
			if (attribute.isDerived() || attribute.isTransient()) {
				continue;
			}
			FieldMapping declared = schema.fieldMapping(eClass, attribute);
			if (declared != null && !declared.isStored()) {
				continue;
			}
			String name = schema.fieldName(attribute, declared);
			IndexableField[] fields = document.getFields(name);
			if (fields.length == 0) {
				continue;
			}
			if (attribute.isMany()) {
				@SuppressWarnings("unchecked")
				List<Object> values = (List<Object>) object.eGet(attribute);
				for (IndexableField field : fields) {
					values.add(valueOf(field, attribute, declared, name));
				}
			} else {
				object.eSet(attribute, valueOf(fields[0], attribute, declared, name));
			}
		}
	}

	/**
	 * The EMF-typed value of one stored field occurrence — the projection path of the
	 * query execution reads row values through this, so rows and reconstructed objects
	 * cannot disagree about a value's type.
	 */
	public Object storedValue(IndexableField stored, EAttribute attribute) {
		FieldMapping declared = schema.fieldMapping(attribute.getEContainingClass(), attribute);
		return valueOf(stored, attribute, declared, stored.name());
	}

	/** Mirrors how {@link DocumentMapper} stored the value: by declaration, else by type. */
	private Object valueOf(IndexableField field, EAttribute attribute, FieldMapping declared, String name) {
		if (numericStored(attribute, declared)) {
			Number number = field.numericValue();
			if (number == null) {
				throw new MappingException("Field '" + name + "' should hold a stored number for '"
						+ attribute.getName() + "' but holds '" + field.stringValue() + "'. The mapping "
						+ "changed since this document was written; the index needs a rebuild.");
			}
			return numberFor(attribute, number, name);
		}
		String value = field.stringValue();
		if (value == null) {
			throw new MappingException("Field '" + name + "' holds no readable stored value for '"
					+ attribute.getName() + "'.");
		}
		return EcoreUtil.createFromString((EDataType) attribute.getEType(), value);
	}

	private boolean numericStored(EAttribute attribute, FieldMapping declared) {
		if (declared instanceof NumericFieldMapping) {
			return true;
		}
		if (declared instanceof TextFieldMapping || declared instanceof KeywordFieldMapping) {
			return false;
		}
		// Convention: same branching the writer used — id, enum and boolean are keywords.
		Class<?> type = attribute.getEAttributeType().getInstanceClass();
		if (attribute.isID() || attribute.getEAttributeType() instanceof EEnum || IndexSchema.isBoolean(type)) {
			return false;
		}
		return IndexSchema.isNumeric(type) || Date.class.isAssignableFrom(IndexSchema.nonNull(type));
	}

	private Object numberFor(EAttribute attribute, Number number, String name) {
		Class<?> type = IndexSchema.nonNull(attribute.getEAttributeType().getInstanceClass());
		if (Date.class.isAssignableFrom(type)) {
			return new Date(number.longValue());
		}
		if (type == int.class || type == Integer.class) {
			return number.intValue();
		}
		if (type == short.class || type == Short.class) {
			return number.shortValue();
		}
		if (type == byte.class || type == Byte.class) {
			return number.byteValue();
		}
		if (type == long.class || type == Long.class) {
			return number.longValue();
		}
		if (type == float.class || type == Float.class) {
			return number.floatValue();
		}
		if (type == double.class || type == Double.class) {
			return number.doubleValue();
		}
		throw new MappingException("Attribute '" + attribute.getName() + "' has the numeric field '" + name
				+ "' but its Java type " + type.getName() + " is not one this backend encodes as a point.");
	}

	// --- references -----------------------------------------------------------------------

	/** The ID_ONLY half of {@link #readReferences}, for the materialized tier. */
	private void readIdOnlyReferences(Document root, EObject object, DocumentMapping documentMapping) {
		if (documentMapping == null) {
			return;
		}
		for (ReferenceMapping reference : documentMapping.getReferences()) {
			if (reference.getStrategy() != ReferenceStrategy.ID_ONLY) {
				continue;
			}
			EReference eReference = reference.getEReference();
			if (eReference != null && object.eIsSet(eReference)) {
				// For an ID_ONLY reference the document's fields are the truth, and whatever
				// survived serialization is at best the same thing twice — a many-valued
				// reference would come back with every member doubled.
				object.eUnset(eReference);
			}
			readIdOnly(root, object, reference, eReference);
		}
	}

	private void readReferences(Document root, List<Document> children, EObject object,
			DocumentMapping documentMapping) {
		if (documentMapping == null) {
			return;
		}
		for (ReferenceMapping reference : documentMapping.getReferences()) {
			EReference eReference = reference.getEReference();
			switch (reference.getStrategy()) {
				case NESTED -> readNested(children, object, eReference);
				case ID_ONLY -> readIdOnly(root, object, reference, eReference);
				case EMBED -> {
					// Deliberately not reconstructed: the flattened fields no longer say
					// which value belonged to which target. Named by omissions().
				}
			}
		}
	}

	private void readNested(List<Document> children, EObject object, EReference eReference) {
		List<EObject> targets = new ArrayList<>();
		for (Document child : children) {
			if (!eReference.getName().equals(child.get(SearchFields.NESTED))) {
				continue;
			}
			EClass childClass = eClassOf(child);
			EObject target = instantiate(childClass);
			readAttributes(child, target, childClass);
			targets.add(target);
		}
		setTargets(object, eReference, targets);
	}

	private void readIdOnly(Document root, EObject object, ReferenceMapping reference,
			EReference eReference) {
		EClass targetClass = eReference.getEReferenceType();
		if (targetClass.isAbstract() || targetClass.isInterface()) {
			// A proxy needs an instance; omissions() names this reference.
			return;
		}
		String name = reference.getPrefix() == null || reference.getPrefix().isBlank()
				? eReference.getName()
				: reference.getPrefix();
		List<EObject> targets = new ArrayList<>();
		for (IndexableField field : root.getFields(name)) {
			targets.add(proxyFor(targetClass, field.stringValue()));
		}
		setTargets(object, eReference, targets);
	}

	/**
	 * An EMF proxy addressing the target through this backend. The URI names the target by
	 * its <em>declared</em> reference type; a target that was indexed as a subclass will
	 * not resolve under it — the price of storing only the id, carried by ID_ONLY itself.
	 */
	private EObject proxyFor(EClass targetClass, String id) {
		InternalEObject proxy = (InternalEObject) EcoreUtil.create(targetClass);
		proxy.eSetProxyURI(SearchUris.objectUri(schema.mapping().getName(),
				schema.typeNameOf(targetClass), id));
		return proxy;
	}

	private void setTargets(EObject object, EReference eReference, List<EObject> targets) {
		if (targets.isEmpty()) {
			return;
		}
		if (eReference.isMany()) {
			@SuppressWarnings("unchecked")
			List<EObject> values = (List<EObject>) object.eGet(eReference);
			values.addAll(targets);
		} else {
			object.eSet(eReference, targets.get(0));
		}
	}

	private static ReferenceMapping referenceMapping(DocumentMapping documentMapping, EReference reference) {
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

	/**
	 * The child documents of one block, in the order the mapper wrote them — the shared
	 * fetch for every reader that reconstructs a hit ({@code read(root, children)}).
	 */
	public static List<Document> blockChildren(IndexSearcher searcher, StoredFields stored,
			Document root) throws IOException {
		String rootId = root.get(SearchFields.ROOT);
		org.apache.lucene.search.Query query = new BooleanQuery.Builder()
				.add(new TermQuery(new Term(SearchFields.ROOT, rootId)), Occur.FILTER)
				.add(new TermQuery(new Term(SearchFields.PARENT, SearchFields.PARENT_VALUE)),
						Occur.MUST_NOT)
				.build();
		int count = searcher.count(query);
		if (count == 0) {
			return List.of();
		}
		TopDocs top = searcher.search(query, count, new Sort(SortField.FIELD_DOC));
		List<Document> children = new ArrayList<>(count);
		for (ScoreDoc hit : top.scoreDocs) {
			children.add(stored.document(hit.doc));
		}
		return children;
	}

}
