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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsCollectorManager;
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
import org.eclipse.fennec.persistence.api.ConverterService;
import org.eclipse.fennec.persistence.capabilities.StoreCapabilitiesBuilder;
import org.eclipse.fennec.persistence.converter.DefaultConverterService;
import org.eclipse.fennec.persistence.diagnostic.PersistenceDiagnostic;
import org.eclipse.fennec.persistence.helper.CompositeIds;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.api.Hit;
import org.eclipse.fennec.persistence.query.api.QueryResult;
import org.eclipse.fennec.persistence.query.api.QueryResultRow;
import org.eclipse.fennec.persistence.query.api.QueryShape;
import org.eclipse.fennec.persistence.query.api.QueryableResource;
import org.eclipse.fennec.persistence.query.support.NamedOperations;
import org.eclipse.fennec.persistence.query.support.PersistedQueries;
import org.eclipse.fennec.persistence.query.support.QueryContexts;
import org.eclipse.fennec.persistence.query.support.QueryResultRows;
import org.eclipse.fennec.persistence.query.support.QueryResults;
import org.eclipse.fennec.persistence.resource.PersistenceResource;
import org.eclipse.fennec.persistence.resource.StreamingResource;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.DocumentReader;
import org.eclipse.fennec.search.mapping.FacetFields;
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
public class SearchResource extends ResourceImpl
		implements PersistenceResource, QueryableResource, StreamingResource {

	/**
	 * The query view is the backend's one declaration; command and store stay empty until
	 * the command path exists (#29) and deliberately never gain the transaction bracket in
	 * v1 (#30) — Lucene has no isolation to bracket.
	 */
	private static final PersistenceCapabilities CAPABILITIES = PersistenceCapabilities.of(
			LuceneQueryProcessor.declaredCapabilities(),
			CommandCapabilitiesBuilder.create().build(),
			StoreCapabilitiesBuilder.create().build());

	/** The stack's plain default — concrete and exported since emf.persistence-jpa#164. */
	private static final ConverterService DEFAULT_CONVERTERS = new DefaultConverterService();

	private final IndexUnit unit;
	private final DocumentMapper mapper;
	private final LuceneQueryProcessor processor;
	private final NamedOperations catalog;
	private final ConverterService converter;
	private final SearchUris address;
	private final Map<ActionType, Map<Object, Object>> defaultOptions = new EnumMap<>(ActionType.class);

	public SearchResource(URI uri, IndexUnit unit, DocumentMapper mapper) {
		this(uri, unit, mapper, null, null);
	}

	/**
	 * @param catalog where named operations live — the stack-wide contract since
	 *        emf.persistence-jpa#203, because the index itself does not persist queries;
	 *        null means named queries execute but are not persisted, and lookup by name
	 *        refuses
	 * @param converter converts parameter and literal values into the persistence
	 *        representation; null means the stack's plain default (#164 made it concrete,
	 *        exported and nullable-contracted) — the OSGi layer (#32) injects the shared
	 *        service instead.
	 */
	public SearchResource(URI uri, IndexUnit unit, DocumentMapper mapper,
			NamedOperations catalog, ConverterService converter) {
		super(uri);
		this.unit = Objects.requireNonNull(unit, "unit");
		this.mapper = Objects.requireNonNull(mapper, "mapper");
		this.processor = LuceneQueryProcessor.of(mapper.schema(),
				unit.config().analyzers().defaultAnalyzer());
		this.catalog = catalog;
		this.converter = converter == null ? DEFAULT_CONVERTERS : converter;
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
		try {
			for (EObject object : getContents()) {
				MappedDocument mapped = mapper.map(object);
				// One term for the whole object: MappedDocument.term() is on the root
				// marker, so a re-save replaces every document of a previous block,
				// children included.
				unit.updateDocuments(mapped.term(), mapped.documents());
			}
		} catch (MappingException e) {
			getErrors().add(PersistenceDiagnostic.error(LuceneQueryProcessor.DIAGNOSTIC_SOURCE,
					"Failed to save resource: " + e.getMessage(), getURI(), e));
			throw new IOException("Cannot save '" + getURI() + "': " + e.getMessage(), e);
		}
		setModified(false);
	}

	@Override
	public void delete(Map<?, ?> options) throws IOException {
		if (address.isObject()) {
			refuseIfReferenced(mapper.schema().eClassOfOrNull(address.type()), address.id());
			unit.deleteDocuments(new Term(SearchFields.ROOT, address.id()));
		} else if (!getContents().isEmpty()) {
			// Every object is checked before the first one is deleted: a refused delete
			// changes nothing, and a half-applied one would be worse than either outcome.
			for (EObject object : getContents()) {
				refuseIfReferenced(object.eClass(), idOf(object));
			}
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

	/**
	 * Refuses a delete that would leave a reference pointing at nothing (§8,
	 * emf.persistence-jpa#195). An index has no foreign key, so it looks before it deletes:
	 * every ID_ONLY reference field that could carry this id is probed, and the object's own
	 * documents are left out so a self-reference does not block its owner. Containment
	 * children are owned and go with the parent, an EMBED copy is a value, and neither can
	 * dangle.
	 * <p>
	 * One read per delete is the price of the guarantee. It is paid where the guarantee is
	 * asked for — no mapped ID_ONLY reference can point here, no probe.
	 */
	private void refuseIfReferenced(EClass type, String id) throws IOException {
		if (type == null || id == null) {
			return;
		}
		Set<String> fields = mapper.schema().incomingIdOnlyFields(type);
		if (fields.isEmpty()) {
			return;
		}
		BooleanQuery.Builder incoming = new BooleanQuery.Builder();
		for (String field : fields) {
			incoming.add(new TermQuery(new Term(field, id)), Occur.SHOULD);
		}
		incoming.setMinimumNumberShouldMatch(1);
		// The object's own block, children included — a reference an object holds to itself
		// disappears with it. The exclusion is by root id, which is the same identity the
		// delete itself uses.
		incoming.add(new TermQuery(new Term(SearchFields.ROOT, id)), Occur.MUST_NOT);
		Query query = incoming.build();
		long referencing = unit.<Integer>search(searcher -> searcher.count(query)).longValue();
		if (referencing == 0) {
			return;
		}
		String message = "Refusing to delete " + type.getName() + " '" + id + "': " + referencing
				+ " document(s) still reference it through " + String.join(", ", fields)
				+ ". A reference this index wrote must not end up pointing at nothing — clear the "
				+ "referencing objects first, or delete them together with this one.";
		getErrors().add(PersistenceDiagnostic.error(LuceneQueryProcessor.DIAGNOSTIC_SOURCE, message,
				getURI()));
		throw new IOException(message);
	}

	// --- reading ------------------------------------------------------------------------

	@Override
	public void load(Map<?, ?> options) throws IOException {
		if (isLoaded) {
			return;
		}
		List<EObject> loaded = readScope();
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
		warnAboutOmissions(loaded);
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

	/** Every message of a diagnostic tree in one line — refusals travel whole. */
	private static String flatten(org.eclipse.emf.common.util.Diagnostic diagnostic) {
		StringBuilder text = new StringBuilder(
				diagnostic.getMessage() == null ? "" : diagnostic.getMessage());
		for (org.eclipse.emf.common.util.Diagnostic child : diagnostic.getChildren()) {
			text.append("; ").append(flatten(child));
		}
		return text.toString();
	}

	private String idOf(EObject object) {
		if (CompositeIds.isComposite(object.eClass())) {
			return CompositeIds.fragment(object);
		}
		EAttribute idAttribute = mapper.schema().idAttribute(object.eClass());
		if (idAttribute == null) {
			return null;
		}
		Object value = object.eGet(idAttribute);
		return value == null ? null
				: EcoreUtil.convertToString((EDataType) idAttribute.getEType(), value);
	}

	@Override
	public Stream<EObject> stream() throws IOException {
		return stream(Map.of());
	}

	/**
	 * The objects this resource's URI addresses, reconstructed but not attached — a stream
	 * consumer wants values, not contents ownership. Collected inside the searcher lease
	 * like every result here; a lazily paging stream is the {@code SERVER_CURSORS} story
	 * (emf.persistence-jpa#162).
	 */
	@Override
	public Stream<EObject> stream(Map<?, ?> options) throws IOException {
		return readScope().stream();
	}

	/** Reconstructs everything the URI addresses; the shared read of load and stream. */
	private List<EObject> readScope() throws IOException {
		if (!scopeIsMapped()) {
			return List.of();
		}
		DocumentReader reader = DocumentReader.of(mapper.schema(), mapper.serializers(), packages());
		try {
			return unit.search(searcher -> {
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
					objects.add(reader.read(root, DocumentReader.blockChildren(searcher, stored, root)));
				}
				return objects;
			});
		} catch (MappingException e) {
			// Index and mapping disagree — reconstruction refused (§4.3). The resource
			// contract wants that as a diagnostic plus a checked failure, not a raw
			// RuntimeException.
			getErrors().add(PersistenceDiagnostic.error(LuceneQueryProcessor.DIAGNOSTIC_SOURCE,
					"Failed to read resource: " + e.getMessage(), getURI(), e));
			throw new IOException("Cannot read '" + getURI() + "': " + e.getMessage(), e);
		}
	}


	/**
	 * Whether this unit maps the type the URI names at all. A type nobody indexes is not an
	 * empty result: the read cannot answer for it, and a caller has to be able to tell the
	 * two apart — so the refusal goes on the resource as an error diagnostic
	 * (emf.persistence-jpa#197) and the read stays empty rather than throwing, because the
	 * resource is still a perfectly good, and empty, view of what the URI addresses.
	 */
	private boolean scopeIsMapped() {
		if (address.type() == null || mapper.schema().eClassOfOrNull(address.type()) != null) {
			return true;
		}
		getErrors().add(PersistenceDiagnostic.error(LuceneQueryProcessor.DIAGNOSTIC_SOURCE,
				"Unit '" + address.unit() + "' maps no type named '" + address.type() + "', so '"
						+ getURI() + "' addresses nothing this index can read. The answer is empty "
						+ "because the type is unknown here, not because the index holds no such "
						+ "objects.",
				getURI()));
		return false;
	}

	/** One warning per loaded class whose reconstruction is incomplete, naming what is not there. */
	private void warnAboutOmissions(List<EObject> loaded) {
		DocumentReader reader = DocumentReader.of(mapper.schema());
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
	 * implementation looks for — and the keyed-access contract for composite ids: a
	 * {@code k1=v1,k2=v2} fragment resolves order-free.
	 */
	@Override
	public EObject getEObject(String uriFragment) {
		EObject byDefault = super.getEObject(uriFragment);
		if (byDefault != null) {
			return byDefault;
		}
		for (EObject object : getContents()) {
			if (matchesId(object, uriFragment)) {
				return object;
			}
		}
		return null;
	}

	private boolean matchesId(EObject object, String fragment) {
		String own = idOf(object);
		if (own == null) {
			return false;
		}
		if (CompositeIds.isComposite(object.eClass()) && CompositeIds.isCompositeFragment(fragment)) {
			try {
				// parse both sides: the incoming pairs may arrive in any order, the
				// canonical order is the class's id-attribute order
				return CompositeIds.parse(object.eClass(), fragment)
						.equals(CompositeIds.parse(object.eClass(), own));
			} catch (IllegalArgumentException malformedOrForeign) {
				return false;
			}
		}
		return fragment.equals(own);
	}

	/**
	 * The keyed fragment of an object: the id (composite ids in their canonical
	 * {@code k1=v1,k2=v2} shape), so references serialize keyed rather than positional —
	 * a positional path breaks the moment the contents order changes.
	 */
	@Override
	public String getURIFragment(EObject eObject) {
		String id = idOf(eObject);
		return id != null ? id : super.getURIFragment(eObject);
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
			// The type segment scopes by class, not by discriminator value: a URI naming a
			// supertype — an abstract one included — reads the concrete subtypes too, the
			// widening every type predicate already applies. A name this unit does not know
			// stays a term nothing matches; saying so is the read path's job.
			EClass type = mapper.schema().eClassOfOrNull(address.type());
			builder.add(type == null
					? new TermQuery(new Term(mapper.typeField(), address.type()))
					: mapper.schema().typeFilter(type), Occur.FILTER);
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
			// Validation first: an undeclared feature is refused with the Diagnostic naming
			// it — the §2B contract, and what the TCK's refusal probes read the name from.
			// Fully qualified: the inherited Resource.Diagnostic shadows the common one
			// inside a Resource implementation.
			org.eclipse.emf.common.util.Diagnostic validation = processor.validate(query, root);
			if (validation.getSeverity() >= org.eclipse.emf.common.util.Diagnostic.ERROR) {
				throw new QueryException(flatten(validation));
			}
			LuceneQueryPlan plan = (LuceneQueryPlan) processor.translate(query,
					QueryContexts.of(root, converter, parameters, options));
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
		if (plan.shape() == QueryShape.AGGREGATION) {
			return QueryResults.rows(plan.shape(), aggregationRows(plan).stream());
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
		if (plan.withScores()) {
			return scoredHits(plan, reader);
		}
		List<EObject> objects = unit.search(searcher -> {
			List<EObject> collected = new ArrayList<>();
			StoredFields stored = searcher.storedFields();
			for (Document document : window(searcher, plan)) {
				collected.add(reader.read(document, DocumentReader.blockChildren(searcher, stored, document)));
			}
			return collected;
		});
		return QueryResults.objects(objects.stream());
	}

	/**
	 * The scored form of the OBJECTS shape (emf.persistence-jpa#165): the same window, but
	 * every hit keeps the score Lucene had before the object existed. Without an explicit
	 * sort the natural order already is the rank order — the contract sentence on the
	 * envelope flag — and with one, {@code doDocScores} fills the scores the sort would
	 * otherwise skip.
	 */
	private QueryResult scoredHits(LuceneQueryPlan plan, DocumentReader reader) throws IOException {
		List<Hit> hits = new ArrayList<>();
		Map<String, Double> scores = new LinkedHashMap<>();
		unit.search(searcher -> {
			int limit = plan.limit() >= 0 ? plan.limit() : searcher.count(plan.query()) - plan.skip();
			int wanted = plan.skip() + Math.max(0, limit);
			if (wanted == 0) {
				return null;
			}
			TopDocs top = plan.sort() != null
					? searcher.search(plan.query(), wanted, plan.sort(), true)
					: searcher.search(plan.query(), wanted);
			StoredFields stored = searcher.storedFields();
			for (int i = plan.skip(); i < top.scoreDocs.length; i++) {
				Document root = stored.document(top.scoreDocs[i].doc);
				EObject object = reader.read(root, DocumentReader.blockChildren(searcher, stored, root));
				hits.add(QueryResults.hit(object, top.scoreDocs[i].score));
				scores.put(root.get(SearchFields.ID), (double) top.scoreDocs[i].score);
			}
			return null;
		});
		return QueryResults.hits(hits.stream(), Map.copyOf(scores));
	}

	/**
	 * The group-by subset's rows: every value of the plan's facet dimension with its
	 * object count, converted back to the key attribute's EMF type — count-descending,
	 * ties by value, the paging window applied to the rows.
	 */
	private List<QueryResultRow> aggregationRows(LuceneQueryPlan plan) throws IOException {
		LuceneQueryPlan.Aggregation aggregation = plan.aggregation();
		FacetFields facets = FacetFields.of(mapper.schema());
		List<FacetFields.Count> counts = unit.search(searcher -> {
			FacetsCollector collected = FacetsCollectorManager
					.search(searcher, plan.query(), 1, new FacetsCollectorManager()).facetsCollector();
			return facets.countAll(searcher, collected, aggregation.dimension());
		});
		int from = Math.min(plan.skip(), counts.size());
		int to = plan.limit() >= 0 ? Math.min(from + plan.limit(), counts.size()) : counts.size();
		List<QueryResultRow> rows = new ArrayList<>(to - from);
		EDataType keyType = aggregation.key().getEAttributeType();
		for (FacetFields.Count count : counts.subList(from, to)) {
			rows.add(QueryResultRows.of(plan.rowAliases(),
					List.of(EcoreUtil.createFromString(keyType, count.value()), count.count())));
		}
		return rows;
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
	 * Named queries live in the stack's named-operation catalog, not in the index: a query
	 * is IR metadata, and an index that stored it would be the third home for the same
	 * thing — which is the convention-per-backend emf.persistence-jpa#203 replaced with
	 * {@link NamedOperations}. Where the catalog itself keeps them is its business; the
	 * default implementation is the {@code emf.osgi} EObject registry this backend used
	 * directly before. A copy is deposited, so the caller's query instance stays the
	 * caller's whatever the catalog does with it.
	 * <p>
	 * Without a catalog attached the query still runs, and the un-kept promise is stated:
	 * a warning names the query that was not persisted.
	 */
	private void saveNamedQuery(String name, org.eclipse.fennec.model.query.Query query)
			throws IOException {
		if (catalog == null) {
			getWarnings().add(PersistenceDiagnostic.warning(LuceneQueryProcessor.DIAGNOSTIC_SOURCE,
					"Query '" + name + "' is named but not persisted: no named-operation catalog is "
							+ "attached to this resource. Bind a NamedOperations service, or hand the "
							+ "factory a catalog, to keep named queries.",
					getURI()));
			return;
		}
		catalog.store(name, EcoreUtil.copy(query));
	}

	private org.eclipse.fennec.model.query.Query loadNamedQuery(String name) throws IOException {
		if (catalog == null) {
			throw new IOException("No named-operation catalog is attached to this resource, so the name '"
					+ name + "' cannot resolve. Bind a NamedOperations service, or hand the factory a "
					+ "catalog.");
		}
		EObject stored = catalog.lookup(name).orElseThrow(
				() -> new IOException("No persisted query named '" + name + "' in the attached catalog"));
		if (!(stored instanceof org.eclipse.fennec.model.query.Query query)) {
			throw new IOException("Catalog entry '" + name + "' is a " + stored.eClass().getName()
					+ ", not a Query — the catalog is shared, and this name belongs to something else.");
		}
		// forExecution copies and clears saveQuery (#163): a query that came out of a catalog
		// is not asking to be put back, and execution must not touch the entry everyone reads.
		return PersistedQueries.forExecution(query);
	}

	// --- lifecycle ----------------------------------------------------------------------

	@Override
	public void close() throws Exception {
		// The unit outlives the resource: several resources address one unit, and its
		// lifecycle belongs to whoever configured it.
		unload();
	}
}
