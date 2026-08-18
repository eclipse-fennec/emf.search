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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
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
import org.eclipse.fennec.model.query.Selection;
import org.eclipse.fennec.persistence.capabilities.CommandCapabilitiesBuilder;
import org.eclipse.fennec.persistence.capabilities.PersistenceCapabilities;
import org.eclipse.fennec.persistence.capabilities.StoreCapabilitiesBuilder;
import org.eclipse.fennec.persistence.diagnostic.PersistenceDiagnostic;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.api.QueryResult;
import org.eclipse.fennec.persistence.query.api.QueryResultRow;
import org.eclipse.fennec.persistence.query.api.QueryShape;
import org.eclipse.fennec.persistence.query.api.QueryableResource;
import org.eclipse.fennec.persistence.query.support.PersistedQueries;
import org.eclipse.fennec.persistence.query.support.QueryContexts;
import org.eclipse.fennec.persistence.query.support.QueryResultRows;
import org.eclipse.fennec.persistence.query.support.QueryResults;
import org.eclipse.fennec.persistence.resource.PersistenceResource;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.DocumentReader;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappedDocument;
import org.eclipse.fennec.search.mapping.MappingException;
import org.eclipse.fennec.search.mapping.SearchFields;
import org.eclipse.fennec.search.query.LuceneQueryPlan;
import org.eclipse.fennec.search.query.LuceneQueryProcessor;
import org.eclipse.fennec.search.unit.IndexUnit;

/**
 * An index unit behind the {@link PersistenceResource} and {@link QueryableResource}
 * contracts: save writes documents, delete removes them, count and exist answer from the
 * searcher, load reconstructs EObjects from the documents the URI addresses, and query
 * runs a canonical query through the {@link LuceneQueryProcessor} — hits come back through
 * the same three-tier materialization the load path uses.
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
public class SearchResource extends ResourceImpl implements PersistenceResource, QueryableResource {

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
	private final LuceneQueryProcessor processor;
	private final SearchUris address;
	private final Map<ActionType, Map<Object, Object>> defaultOptions = new EnumMap<>(ActionType.class);

	public SearchResource(URI uri, IndexUnit unit, DocumentMapper mapper) {
		super(uri);
		this.unit = Objects.requireNonNull(unit, "unit");
		this.mapper = Objects.requireNonNull(mapper, "mapper");
		this.processor = LuceneQueryProcessor.of(mapper.schema(),
				unit.config().analyzers().defaultAnalyzer());
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

	// --- querying -----------------------------------------------------------------------

	@Override
	public QueryResult query(org.eclipse.fennec.model.query.Query query) throws IOException {
		return query(query, null, null);
	}

	@Override
	public QueryResult query(String name, Map<String, Object> parameters, Map<?, ?> options)
			throws IOException {
		Objects.requireNonNull(name, "query name must not be null");
		return query(loadNamedQuery(name), parameters, options);
	}

	@Override
	public QueryResult query(org.eclipse.fennec.model.query.Query query, Map<String, Object> parameters,
			Map<?, ?> options) throws IOException {
		Objects.requireNonNull(query, "query must not be null");
		try {
			String catalogName = PersistedQueries.catalogName(query);
			if (catalogName != null) {
				saveNamedQuery(catalogName, query);
			}
			EClass root = query.getFrom() != null ? query.getFrom()
					: address.type() != null ? mapper.schema().eClassOf(address.type()) : null;
			if (root == null) {
				throw new QueryException("The query names no root type and this resource's URI '" + getURI()
						+ "' addresses no type either.");
			}
			LuceneQueryPlan plan = (LuceneQueryPlan) processor.translate(query,
					QueryContexts.of(root, null, parameters, options));
			return execute(plan, root);
		} catch (QueryException | MappingException e) {
			getErrors().add(PersistenceDiagnostic.error(LuceneQueryProcessor.DIAGNOSTIC_SOURCE,
					"Query rejected: " + e.getMessage(), getURI(), e));
			throw new IOException("Query rejected: " + e.getMessage(), e);
		}
	}

	/**
	 * Runs a plan and materializes the window inside the searcher lease — the streams a
	 * {@link QueryResult} hands out live longer than the searcher, so they are built over
	 * collected results. A server-side cursor would be the {@code SERVER_CURSORS} store
	 * feature, which this backend does not declare.
	 */
	private QueryResult execute(LuceneQueryPlan plan, EClass root) throws IOException, QueryException {
		if (plan.shape() == QueryShape.COUNT) {
			return QueryResults.count(unit.<Integer>search(searcher -> searcher.count(plan.query())));
		}
		DocumentReader reader = DocumentReader.of(mapper.schema(), mapper.serializers(), packages());
		if (plan.shape() == QueryShape.PROJECTION) {
			List<IndexSchema.Field> columns = new ArrayList<>();
			for (Selection selection : plan.source().getSelect()) {
				columns.add(resolveColumn(root, selection));
			}
			List<QueryResultRow> rows = unit.search(searcher -> {
				List<QueryResultRow> collected = new ArrayList<>();
				for (Document document : window(searcher, plan)) {
					List<Object> values = new ArrayList<>(columns.size());
					for (IndexSchema.Field column : columns) {
						values.add(columnValue(document, column, reader));
					}
					collected.add(QueryResultRows.of(plan.rowAliases(), values));
				}
				return collected;
			});
			return QueryResults.rows(plan.shape(), rows.stream());
		}
		List<EObject> objects = unit.search(searcher -> {
			List<EObject> collected = new ArrayList<>();
			StoredFields stored = searcher.storedFields();
			for (Document document : window(searcher, plan)) {
				collected.add(reader.read(document, childrenOf(searcher, stored, document)));
			}
			return collected;
		});
		return QueryResults.objects(objects.stream());
	}

	/** The plan's paging window, in plan order — sorted when the plan says so. */
	private List<Document> window(IndexSearcher searcher, LuceneQueryPlan plan) throws IOException {
		int limit = plan.limit() >= 0 ? plan.limit() : searcher.count(plan.query()) - plan.skip();
		int wanted = plan.skip() + Math.max(0, limit);
		if (wanted == 0) {
			return List.of();
		}
		TopDocs top = plan.sort() != null
				? searcher.search(plan.query(), wanted, plan.sort())
				: searcher.search(plan.query(), wanted);
		StoredFields stored = searcher.storedFields();
		List<Document> documents = new ArrayList<>();
		for (int i = plan.skip(); i < top.scoreDocs.length; i++) {
			documents.add(stored.document(top.scoreDocs[i].doc));
		}
		return documents;
	}

	private IndexSchema.Field resolveColumn(EClass root, Selection selection) throws QueryException {
		try {
			return mapper.schema().resolve(root, selection.getPath().getSegments());
		} catch (MappingException e) {
			throw new QueryException(e.getMessage(), e);
		}
	}

	/** A column's EMF-typed value: null when absent, the value, or a list for many. */
	private Object columnValue(Document document, IndexSchema.Field column, DocumentReader reader) {
		IndexableField[] stored = document.getFields(column.name());
		if (stored.length == 0) {
			return null;
		}
		if (!column.attribute().isMany()) {
			return reader.storedValue(stored[0], column.attribute());
		}
		List<Object> values = new ArrayList<>(stored.length);
		for (IndexableField field : stored) {
			values.add(reader.storedValue(field, column.attribute()));
		}
		return values;
	}

	// --- the persisted-query catalog ------------------------------------------------------

	/**
	 * The catalog analogue of Mongo's {@code fennec.queries} collection: one document per
	 * name, invisible to every plan (no root marker). The refresh after a save is
	 * deliberate: a persisted query promises read-your-writes by name, which the unit's
	 * refresh policy otherwise does not.
	 */
	private void saveNamedQuery(String name, org.eclipse.fennec.model.query.Query query)
			throws IOException, QueryException {
		Document document = new Document();
		document.add(new StringField(SearchFields.QUERY_NAME, name, Field.Store.YES));
		document.add(new StoredField(SearchFields.QUERY_XMI, PersistedQueries.toXmi(query)));
		unit.updateDocuments(new Term(SearchFields.QUERY_NAME, name), List.of(document));
		unit.refresh();
	}

	private org.eclipse.fennec.model.query.Query loadNamedQuery(String name) throws IOException {
		String xmi = unit.search(searcher -> {
			TopDocs top = searcher.search(new TermQuery(new Term(SearchFields.QUERY_NAME, name)), 1);
			if (top.scoreDocs.length == 0) {
				return null;
			}
			return searcher.storedFields().document(top.scoreDocs[0].doc).get(SearchFields.QUERY_XMI);
		});
		if (xmi == null) {
			throw new IOException("No persisted query named '" + name + "' in unit '" + unit.name() + "'");
		}
		try {
			return PersistedQueries.fromXmi(name, xmi, packages());
		} catch (QueryException e) {
			throw new IOException("Persisted query '" + name + "' cannot be read back: " + e.getMessage(), e);
		}
	}

	// --- lifecycle ----------------------------------------------------------------------

	@Override
	public void close() throws Exception {
		// The unit outlives the resource: several resources address one unit, and its
		// lifecycle belongs to whoever configured it.
		unload();
	}
}
