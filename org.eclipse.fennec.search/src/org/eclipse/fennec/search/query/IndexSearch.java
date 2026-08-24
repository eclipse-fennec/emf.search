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
package org.eclipse.fennec.search.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.TopDocs;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.persistence.api.ConverterService;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.api.Hit;
import org.eclipse.fennec.persistence.query.api.QueryShape;
import org.eclipse.fennec.persistence.query.support.NamedOperations;
import org.eclipse.fennec.persistence.query.support.PersistedQueries;
import org.eclipse.fennec.persistence.query.support.QueryContexts;
import org.eclipse.fennec.persistence.query.support.QueryResults;
import org.eclipse.fennec.search.mapping.DocumentReader;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappingException;
import org.eclipse.fennec.search.materialization.ObjectSerializers;
import org.eclipse.fennec.search.unit.IndexUnit;

/**
 * The direct search API over one index unit (#41): canonical query in, {@link Hit}s out —
 * objects without resource mechanics. No {@code getContents()} ownership that moves a hit
 * into a resource, no URI addressing, no ambient {@code ResourceSet}; the query-side anchor
 * of the own-API family (suggest, facets, highlighting, similarity, grouping).
 * <p>
 * Three deliberate limits, each with its way out:
 * <ul>
 * <li><b>OBJECTS only.</b> A count, a projection or an aggregation has no hits; those
 * shapes run through the {@code QueryableResource}, whose {@code QueryResult} is built for
 * them.</li>
 * <li><b>Results are materialized.</b> The list is collected inside the searcher lease;
 * a lazily paging answer is the {@code SERVER_CURSORS} story
 * (emf.persistence-jpa#162).</li>
 * <li><b>Scores are always filled.</b> The {@link Hit} carrier has a score slot, so the
 * search computes scores even under an explicit sort ({@code doDocScores}) — a direct
 * search asking for hits wants to know how good they are.</li>
 * </ul>
 * <p>
 * The collaborators are explicit and optional, attached with {@code with*} copies: a
 * {@link ConverterService} for parameter values, a {@link NamedOperations} catalog for
 * named queries, a {@link PrimaryStore} that resolves {@code SOURCE_URI} hits to their
 * originals in one batch (without one, such hits come back as EMF proxies — exactly what
 * the index knows), serializers and a package registry for {@code STORED_OBJECT}
 * deserialization.
 *
 * @author Data In Motion Consulting
 */
public final class IndexSearch {

	private final IndexUnit unit;
	private final IndexSchema schema;
	private final LuceneQueryProcessor processor;
	private final ConverterService converter;
	private final NamedOperations catalog;
	private final PrimaryStore primaryStore;
	private final ObjectSerializers serializers;
	private final EPackage.Registry packages;

	private IndexSearch(IndexUnit unit, IndexSchema schema, ConverterService converter,
			NamedOperations catalog, PrimaryStore primaryStore, ObjectSerializers serializers,
			EPackage.Registry packages) {
		this.unit = unit;
		this.schema = schema;
		this.processor = LuceneQueryProcessor.of(schema, unit.config().analyzers().defaultAnalyzer());
		this.converter = converter;
		this.catalog = catalog;
		this.primaryStore = primaryStore;
		this.serializers = serializers;
		this.packages = packages;
	}

	/** Direct search over one unit and its schema; collaborators attach via {@code with*}. */
	public static IndexSearch of(IndexUnit unit, IndexSchema schema) {
		Objects.requireNonNull(unit, "unit");
		Objects.requireNonNull(schema, "schema");
		return new IndexSearch(unit, schema, null, null, null, ObjectSerializers.withDefaults(), null);
	}

	/** A copy with a converter for parameter values. */
	public IndexSearch withConverter(ConverterService converter) {
		return new IndexSearch(unit, schema, converter, catalog, primaryStore, serializers, packages);
	}

	/** A copy with the named-operation catalog that resolves and keeps named queries. */
	public IndexSearch withCatalog(NamedOperations catalog) {
		return new IndexSearch(unit, schema, converter, catalog, primaryStore, serializers, packages);
	}

	/** A copy with the primary store that resolves {@code SOURCE_URI} hits to originals. */
	public IndexSearch withPrimaryStore(PrimaryStore primaryStore) {
		return new IndexSearch(unit, schema, converter, catalog, primaryStore, serializers, packages);
	}

	/** A copy with an explicit serializer registry for {@code STORED_OBJECT} documents. */
	public IndexSearch withSerializers(ObjectSerializers serializers) {
		Objects.requireNonNull(serializers, "serializers");
		return new IndexSearch(unit, schema, converter, catalog, primaryStore, serializers, packages);
	}

	/** A copy deserializing against the caller's package registry (plus the unit's own EPackage). */
	public IndexSearch withPackages(EPackage.Registry packages) {
		return new IndexSearch(unit, schema, converter, catalog, primaryStore, serializers, packages);
	}

	/**
	 * Runs the query and returns its hits in rank order — or in the query's sort order,
	 * scores filled either way.
	 *
	 * @throws QueryException if the query is refused — an undeclared feature, a shape
	 *         without hits, a name without a catalog — always naming the way out
	 */
	public List<Hit> search(Query query) throws IOException, QueryException {
		return search(query, null, null);
	}

	/**
	 * As {@link #search(Query)}, with parameter values and backend options
	 * ({@link SearchOptions}).
	 */
	public List<Hit> search(Query query, Map<String, Object> parameters, Map<?, ?> options)
			throws IOException, QueryException {
		Objects.requireNonNull(query, "query");
		String catalogName = PersistedQueries.catalogName(query);
		if (catalogName != null) {
			keep(catalogName, query);
		}
		EClass root = query.getFrom();
		if (root == null) {
			throw new QueryException("The query names no root type, and a direct search has no URI "
					+ "to take one from. Set the query's from.");
		}
		// Validation first: an undeclared feature is refused with the Diagnostic naming it —
		// the same §2B contract the resource path speaks.
		org.eclipse.emf.common.util.Diagnostic validation = processor.validate(query, root);
		if (validation.getSeverity() >= org.eclipse.emf.common.util.Diagnostic.ERROR) {
			throw new QueryException(flatten(validation), null, validation);
		}
		LuceneQueryPlan plan;
		try {
			plan = (LuceneQueryPlan) processor.translate(query,
					QueryContexts.of(root, converter, parameters, options));
		} catch (MappingException e) {
			throw new QueryException(e.getMessage(), e);
		}
		if (plan.shape() != QueryShape.OBJECTS) {
			throw new QueryException("A direct search answers hits, and a " + plan.shape()
					+ " result has none. Run counts, projections and aggregations through the "
					+ "QueryableResource, whose QueryResult carries those shapes.");
		}
		return resolveThroughPrimaryStore(window(plan));
	}

	/**
	 * Runs a named query from the attached catalog.
	 *
	 * @throws QueryException if no catalog is attached, the name resolves to nothing, or
	 *         the entry is not a query
	 */
	public List<Hit> search(String name, Map<String, Object> parameters, Map<?, ?> options)
			throws IOException, QueryException {
		Objects.requireNonNull(name, "name");
		if (catalog == null) {
			throw new QueryException("No named-operation catalog is attached, so the name '" + name
					+ "' cannot resolve. Attach one with withCatalog(...).");
		}
		EObject stored = catalog.lookup(name).orElseThrow(
				() -> new QueryException("No persisted query named '" + name + "' in the attached catalog"));
		if (!(stored instanceof Query query)) {
			throw new QueryException("Catalog entry '" + name + "' is a " + stored.eClass().getName()
					+ ", not a Query — the catalog is shared, and this name belongs to something else.");
		}
		// forExecution copies and clears saveQuery (#163): a query out of the catalog is not
		// asking to be put back.
		return search(PersistedQueries.forExecution(query), parameters, options);
	}

	/**
	 * A query carrying a catalog name wants to be kept; without a catalog that promise
	 * cannot be met, and a direct search has no warnings channel to note it on — so it
	 * refuses instead of quietly dropping the name.
	 */
	private void keep(String name, Query query) throws QueryException, IOException {
		if (catalog == null) {
			throw new QueryException("Query '" + name + "' asks to be persisted, and no named-operation "
					+ "catalog is attached to keep it. Attach one with withCatalog(...), or drop the "
					+ "query's saveQuery name.");
		}
		catalog.store(name, EcoreUtil.copy(query));
	}

	/** The plan's window as scored hits, collected inside the searcher lease. */
	private List<Hit> window(LuceneQueryPlan plan) throws IOException {
		DocumentReader reader = DocumentReader.of(schema, serializers, packageRegistry());
		return unit.search(searcher -> {
			int limit = plan.limit() >= 0 ? plan.limit() : searcher.count(plan.query()) - plan.skip();
			int wanted = plan.skip() + Math.max(0, limit);
			if (wanted == 0) {
				return List.of();
			}
			TopDocs top = plan.sort() != null
					? searcher.search(plan.query(), wanted, plan.sort(), true)
					: searcher.search(plan.query(), wanted);
			StoredFields stored = searcher.storedFields();
			List<Hit> hits = new ArrayList<>(Math.max(0, top.scoreDocs.length - plan.skip()));
			for (int i = plan.skip(); i < top.scoreDocs.length; i++) {
				Document root = stored.document(top.scoreDocs[i].doc);
				EObject object = reader.read(root, DocumentReader.blockChildren(searcher, stored, root));
				hits.add(QueryResults.hit(object, top.scoreDocs[i].score));
			}
			return hits;
		});
	}

	/**
	 * Resolves {@code SOURCE_URI} proxies through the attached primary store, one batch per
	 * window. Without a store — or for a URI the store cannot answer — the proxy stays: the
	 * hit then carries exactly what the index knows.
	 */
	private List<Hit> resolveThroughPrimaryStore(List<Hit> hits) throws IOException {
		if (primaryStore == null || hits.isEmpty()) {
			return hits;
		}
		List<URI> unresolved = new ArrayList<>();
		for (Hit hit : hits) {
			if (hit.object().eIsProxy()) {
				unresolved.add(((InternalEObject) hit.object()).eProxyURI());
			}
		}
		if (unresolved.isEmpty()) {
			return hits;
		}
		Map<URI, EObject> resolved = primaryStore.resolve(List.copyOf(unresolved));
		List<Hit> answered = new ArrayList<>(hits.size());
		for (Hit hit : hits) {
			EObject object = hit.object();
			if (object.eIsProxy()) {
				EObject original = resolved.get(((InternalEObject) object).eProxyURI());
				answered.add(original == null ? hit : QueryResults.hit(original, hit.score()));
			} else {
				answered.add(hit);
			}
		}
		return answered;
	}

	/** The packages a stored object may deserialize against: the caller's, plus the unit's own. */
	private EPackage.Registry packageRegistry() {
		EPackageRegistryImpl registry = packages == null
				? new EPackageRegistryImpl()
				: new EPackageRegistryImpl(packages);
		EPackage ePackage = schema.mapping().getEPackage();
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
}
