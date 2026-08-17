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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.fennec.persistence.capabilities.CommandCapabilitiesBuilder;
import org.eclipse.fennec.persistence.capabilities.PersistenceCapabilities;
import org.eclipse.fennec.persistence.capabilities.StoreCapabilitiesBuilder;
import org.eclipse.fennec.persistence.resource.PersistenceResource;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.MappedDocument;
import org.eclipse.fennec.search.mapping.SearchFields;
import org.eclipse.fennec.search.query.LuceneQueryProcessor;
import org.eclipse.fennec.search.unit.IndexUnit;

/**
 * An index unit behind the {@link PersistenceResource} contract: save writes documents,
 * delete removes them, count and exist answer from the searcher.
 * <p>
 * Two contract limits are stated rather than papered over.
 * <p>
 * <b>Loading refuses without materialization.</b> An index holds the fields a mapping
 * declared, which is enough to find an object and never enough to rebuild it. Returning a
 * partially populated EObject would be a lie that nothing downstream could detect, so
 * {@code load} fails with a diagnostic until the mapping stores the serialized object
 * (issue #18). The same reasoning makes {@code UpdateCommand} conditional (#29) — this is
 * one limitation, not two.
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
		throw new IOException("Cannot load '" + getURI() + "': this index stores the fields its mapping "
				+ "declares, which is enough to find an object but not to rebuild it. Loading needs the "
				+ "mapping to materialize the serialized object (issue #18); until then a load would have "
				+ "to invent the unmapped state.");
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
