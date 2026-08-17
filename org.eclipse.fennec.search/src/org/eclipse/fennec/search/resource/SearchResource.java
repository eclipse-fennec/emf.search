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

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.persistence.capabilities.CommandCapabilitiesBuilder;
import org.eclipse.fennec.persistence.capabilities.PersistenceCapabilities;
import org.eclipse.fennec.persistence.capabilities.StoreCapabilitiesBuilder;
import org.eclipse.fennec.persistence.diagnostic.PersistenceDiagnostic;
import org.eclipse.fennec.persistence.resource.PersistenceResource;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.DocumentReader;
import org.eclipse.fennec.search.mapping.MappedDocument;
import org.eclipse.fennec.search.mapping.SearchFields;
import org.eclipse.fennec.search.query.LuceneQueryProcessor;
import org.eclipse.fennec.search.unit.IndexUnit;

/**
 * An index unit behind the {@link PersistenceResource} contract: save writes documents,
 * delete removes them, count and exist answer from the searcher, and load reconstructs
 * EObjects from the documents the URI addresses.
 * <p>
 * Two contract limits are stated rather than papered over.
 * <p>
 * <b>Loading is partial by default</b> (docs/search-access.md §4.3). Without a declared
 * materialization, an object is rebuilt from its documents' stored fields — the same tier
 * every hit gets: whatever the mapping stored comes back, everything else stays unset, and
 * the resource says so through a warning diagnostic naming the omissions per class. A
 * partial object is a legitimate read result and never a write source, which is why
 * {@code UpdateCommand} stays conditional on {@code STORED_OBJECT} materialization (#29).
 * <p>
 * <b>Visibility is the unit's, not the caller's.</b> A save becomes findable when the
 * unit's refresh policy says so; under {@code COMMITTED} visibility that means after a
 * commit. There is no read-your-writes guarantee, and asking for one would mean refreshing
 * on every save.
 *
 * @author Data In Motion Consulting
 */
public class SearchResource extends ResourceImpl implements PersistenceResource {

	/**
	 * The query view is the backend's one declaration; command and store stay empty until
	 * the command path exists (#29) and deliberately never gain the transaction bracket in
	 * v1 (#30) — Lucene has no isolation to bracket.
	 */
	private static final PersistenceCapabilities CAPABILITIES = PersistenceCapabilities.of(
			LuceneQueryProcessor.declaredCapabilities(),
			CommandCapabilitiesBuilder.create().build(),
			StoreCapabilitiesBuilder.create().build());

	private final IndexUnit unit;
	private final DocumentMapper mapper;
	private final SearchUris address;
	private final Map<ActionType, Map<Object, Object>> defaultOptions = new EnumMap<>(ActionType.class);

	public SearchResource(URI uri, IndexUnit unit, DocumentMapper mapper) {
		super(uri);
		this.unit = Objects.requireNonNull(unit, "unit");
		this.mapper = Objects.requireNonNull(mapper, "mapper");
		this.address = SearchUris.parse(uri);
	}

	/** The unit this resource writes to. */
	public IndexUnit unit() {
		return unit;
	}

	@Override
	public PersistenceCapabilities capabilities() {
		return CAPABILITIES;
	}

	@Override
	public void updateDefaultOptions(Map<Object, Object> options, ActionType... types) {
		Objects.requireNonNull(options, "options");
		ActionType[] targets = types == null || types.length == 0
				? new ActionType[] { ActionType.ALL }
				: types;
		for (ActionType type : targets) {
			defaultOptions.computeIfAbsent(type, key -> new HashMap<>()).putAll(options);
		}
	}

	// --- writing ------------------------------------------------------------------------

	@Override
	public void save(Map<?, ?> options) throws IOException {
		for (EObject object : getContents()) {
			MappedDocument mapped = mapper.map(object);
			// One term for the whole object: MappedDocument.term() is on the root marker,
			// so a re-save replaces every document of a previous block, children included.
			unit.updateDocuments(mapped.term(), mapped.documents());
		}
		setModified(false);
	}

	@Override
	public void delete(Map<?, ?> options) throws IOException {
		if (address.isObject()) {
			unit.deleteDocuments(new Term(SearchFields.ROOT, address.id()));
		} else if (!getContents().isEmpty()) {
			for (EObject object : getContents()) {
				unit.deleteDocuments(mapper.map(object).term());
			}
		} else {
			throw new IOException("URI '" + getURI() + "' addresses no object and the resource is empty, "
					+ "so there is nothing to delete. Use a URI of the form "
					+ SearchUris.SCHEME + "://<unit>/<type>/<id>.");
		}
		getContents().clear();
	}

	// --- reading ------------------------------------------------------------------------

	@Override
	public void load(Map<?, ?> options) throws IOException {
		if (isLoaded) {
			return;
		}
		DocumentReader reader = DocumentReader.of(mapper.schema(), mapper.serializers(), packages());
		List<EObject> loaded = unit.search(searcher -> {
			Query scope = scopeQuery();
			int total = searcher.count(scope);
			List<EObject> objects = new ArrayList<>(total);
			if (total == 0) {
				return objects;
			}
			TopDocs top = searcher.search(scope, total);
			StoredFields stored = searcher.storedFields();
			for (ScoreDoc hit : top.scoreDocs) {
				Document root = stored.document(hit.doc);
				objects.add(reader.read(root, childrenOf(searcher, stored, root)));
			}
			return objects;
		});
		// Objects already attached — say, by a save on this same resource — are kept, and an
		// incoming reconstruction of the same id is skipped, so identity survives for anyone
		// already holding a reference. Mirrors the JPA and Mongo backends.
		Set<String> present = new HashSet<>();
		for (EObject existing : getContents()) {
			String id = idOf(existing);
			if (id != null) {
				present.add(id);
			}
		}
		for (EObject object : loaded) {
			String id = idOf(object);
			if (id == null || present.add(id)) {
				getContents().add(object);
			}
		}
		warnAboutOmissions(reader, loaded);
		isLoaded = true;
		setModified(false);
	}

	/**
	 * The packages a stored object may deserialize against: the caller's ResourceSet
	 * registry when there is one, always including the unit's own EPackage.
	 */
	private EPackage.Registry packages() {
		EPackageRegistryImpl registry = getResourceSet() == null
				? new EPackageRegistryImpl()
				: new EPackageRegistryImpl(getResourceSet().getPackageRegistry());
		EPackage ePackage = mapper.schema().mapping().getEPackage();
		registry.put(ePackage.getNsURI(), ePackage);
		return registry;
	}

	private String idOf(EObject object) {
		EAttribute idAttribute = mapper.schema().idAttribute(object.eClass());
		if (idAttribute == null) {
			return null;
		}
		Object value = object.eGet(idAttribute);
		return value == null ? null
				: EcoreUtil.convertToString((EDataType) idAttribute.getEType(), value);
	}

	/** The child documents of one block, in the order the mapper wrote them. */
	private List<Document> childrenOf(IndexSearcher searcher, StoredFields stored, Document root)
			throws IOException {
		String rootId = root.get(SearchFields.ROOT);
		Query query = new BooleanQuery.Builder()
				.add(new TermQuery(new Term(SearchFields.ROOT, rootId)), Occur.FILTER)
				.add(new TermQuery(new Term(SearchFields.PARENT, SearchFields.PARENT_VALUE)), Occur.MUST_NOT)
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

	/** One warning per loaded class whose reconstruction is incomplete, naming what is not there. */
	private void warnAboutOmissions(DocumentReader reader, List<EObject> loaded) {
		Set<EClass> classes = new LinkedHashSet<>();
		for (EObject object : loaded) {
			classes.add(object.eClass());
		}
		for (EClass eClass : classes) {
			List<String> omissions = reader.omissions(eClass);
			if (!omissions.isEmpty()) {
				getWarnings().add(PersistenceDiagnostic.warning(LuceneQueryProcessor.DIAGNOSTIC_SOURCE,
						"Objects of " + eClass.getName() + " are reconstructed from stored fields and "
								+ "incomplete: " + String.join(", ", omissions) + " cannot come back. "
								+ "Declare STORED_OBJECT materialization for complete objects.",
						getURI()));
			}
		}
	}

	/**
	 * Resolves the fragment of an object URI ({@link SearchUris#objectUri}) — the id, under
	 * the mapping's id attribute, which need not be the intrinsic EMF ID the default
	 * implementation looks for.
	 */
	@Override
	public EObject getEObject(String uriFragment) {
		EObject byDefault = super.getEObject(uriFragment);
		if (byDefault != null) {
			return byDefault;
		}
		for (EObject object : getContents()) {
			if (uriFragment.equals(idOf(object))) {
				return object;
			}
		}
		return null;
	}

	@Override
	public long count() throws IOException {
		return count(Map.of());
	}

	@Override
	public long count(Map<?, ?> options) throws IOException {
		Query query = scopeQuery();
		return unit.<Integer>search(searcher -> searcher.count(query)).longValue();
	}

	@Override
	public boolean exist() throws IOException {
		return exist(Map.of());
	}

	@Override
	public boolean exist(Map<?, ?> options) throws IOException {
		return count(options) > 0;
	}

	/**
	 * What this resource's URI addresses, as a query.
	 * <p>
	 * Always restricted to root documents. A block writes one document per nested child,
	 * and counting those would answer "how many documents" where the caller asked "how many
	 * objects" — the sort of off-by-a-lot that only shows up once someone maps a
	 * containment reference as NESTED.
	 */
	private Query scopeQuery() {
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		builder.add(new TermQuery(new Term(SearchFields.PARENT, SearchFields.PARENT_VALUE)), Occur.FILTER);
		if (address.type() != null) {
			builder.add(new TermQuery(new Term(mapper.typeField(), address.type())), Occur.FILTER);
		}
		if (address.id() != null) {
			builder.add(new TermQuery(new Term(SearchFields.ROOT, address.id())), Occur.FILTER);
		}
		return builder.build();
	}

	// --- lifecycle ----------------------------------------------------------------------

	@Override
	public void close() throws Exception {
		// The unit outlives the resource: several resources address one unit, and its
		// lifecycle belongs to whoever configured it.
		unload();
	}
}
