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

	public SearchResourceFactory(IndexUnit unit, DocumentMapper mapper) {
		this.unit = Objects.requireNonNull(unit, "unit");
		this.mapper = Objects.requireNonNull(mapper, "mapper");
	}

	@Override
	public Resource createResource(URI uri) {
		SearchUris address = SearchUris.parse(uri);
		if (!address.unit().equals(unit.name())) {
			throw new IllegalArgumentException("URI '" + uri + "' names index unit '" + address.unit()
					+ "', but this factory serves '" + unit.name() + "'");
		}
		return new SearchResource(uri, unit, mapper);
	}
}
