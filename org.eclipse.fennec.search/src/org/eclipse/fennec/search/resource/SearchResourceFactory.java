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

import java.util.Objects;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.fennec.persistence.api.ConverterService;
import org.eclipse.fennec.persistence.query.support.NamedOperations;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.unit.IndexUnit;

/**
 * Creates {@link SearchResource}s for one index unit.
 * <p>
 * Plain Java: an application constructs one per unit and registers it in its
 * {@code ResourceSet}. In OSGi the same factory is what a component publishes, so the two
 * differ in who does the registering and in nothing else.
 *
 * @author Data In Motion Consulting
 */
public class SearchResourceFactory extends ResourceFactoryImpl {

	private final IndexUnit unit;
	private final DocumentMapper mapper;
	private final NamedOperations catalog;
	private final ConverterService converter;

	public SearchResourceFactory(IndexUnit unit, DocumentMapper mapper) {
		this(unit, mapper, null, null);
	}

	/**
	 * A fully equipped factory. The catalog is where named operations live — the stack-wide
	 * {@link NamedOperations} contract (emf.persistence-jpa#203), because the index does not
	 * persist queries; null means named queries execute but are not persisted, and lookup by
	 * name refuses. The converter turns parameter values into their persistence
	 * representation; null means identity, like the JPA and Mongo backends pass today
	 * (emf.persistence-jpa#164).
	 */
	public SearchResourceFactory(IndexUnit unit, DocumentMapper mapper,
			NamedOperations catalog, ConverterService converter) {
		this.unit = Objects.requireNonNull(unit, "unit");
		this.mapper = Objects.requireNonNull(mapper, "mapper");
		this.catalog = catalog;
		this.converter = converter;
	}

	@Override
	public Resource createResource(URI uri) {
		SearchUris address = SearchUris.parse(uri);
		if (!address.unit().equals(unit.name())) {
			throw new IllegalArgumentException("URI '" + uri + "' names index unit '" + address.unit()
					+ "', but this factory serves '" + unit.name() + "'");
		}
		return new SearchResource(uri, unit, mapper, catalog, converter);
	}
}
