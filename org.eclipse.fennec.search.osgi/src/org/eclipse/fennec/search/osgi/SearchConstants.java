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
package org.eclipse.fennec.search.osgi;

/**
 * Names shared between the components here and the consumers that select them.
 *
 * @author Data In Motion Consulting
 */
public final class SearchConstants {

	/** Factory PID of an index unit configuration. */
	public static final String UNIT_PID = "SearchIndexUnit";

	/**
	 * Service property carrying an index unit's alias, mirroring
	 * {@code mongo.database.alias} — this is what a resource factory filters on to find
	 * the unit a URI names.
	 */
	public static final String UNIT_ALIAS = "search.unit.alias";

	/**
	 * Service property naming an {@code org.apache.lucene.analysis.Analyzer} service, so a
	 * mapping can reference an analyzer by name instead of by class.
	 */
	public static final String ANALYZER_NAME = "search.analyzer.name";

	/** Component name of the lucene {@code Resource.Factory}. */
	public static final String RESOURCE_FACTORY_NAME = "LuceneResourceFactory";

	/**
	 * Default name of the EObject registry carrying authored {@code *.esearch} mappings —
	 * the deployment delivery path of #32. Override with the {@code mappingRegistry.target}
	 * reference target.
	 */
	public static final String MAPPING_REGISTRY_NAME = "search-mappings";

	/**
	 * Default name of the EObject registry this backend falls back to when no
	 * {@code NamedOperations} service is bound — the stack-wide catalog contract of
	 * emf.persistence-jpa#203 comes first, and this registry is then wrapped in the
	 * contract's default implementation rather than read directly. Override with the
	 * {@code queryCatalog.target} reference target.
	 */
	public static final String QUERY_CATALOG_NAME = "search-queries";

	private SearchConstants() {
	}
}
