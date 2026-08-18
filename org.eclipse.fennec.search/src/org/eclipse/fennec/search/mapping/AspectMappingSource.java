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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.metadata.MetadataService;
import org.eclipse.fennec.emf.osgi.model.metadata.AspectEntry;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;

/**
 * The shipped path of §4.1: a model bundle carries its own index mapping as a metadata
 * aspect — {@code MetadataService.getPackageAspect(ePackage, "esearch")} with the
 * {@link IndexUnitMapping} as the entry's content, the same slot the codec and orm aspects
 * use. Attaching is the model side's job (a {@code MetadataHandler} adds the entry when
 * the package registers); this source only reads.
 * <p>
 * An aspect hangs on a package, not on a unit name, so this source consults the packages
 * it was told about — the OSGi component feeds it the bound {@code EPackage} services, a
 * plain-Java caller lists them at construction. In a composed source
 * ({@link MappingSources#withPrecedence}) it comes after the registry: a deployment
 * overrides what a model ships with, never the other way around (#32).
 *
 * @author Data In Motion Consulting
 */
public final class AspectMappingSource implements MappingSource {

	/** The aspect type id an {@code IndexUnitMapping} rides the metadata plane under. */
	public static final String ASPECT_TYPE_ID = "esearch";

	private final MetadataService metadata;
	private final List<EPackage> packages = new CopyOnWriteArrayList<>();

	private AspectMappingSource(MetadataService metadata) {
		this.metadata = metadata;
	}

	/** A source reading the given service; packages arrive via {@link #addPackage}. */
	public static AspectMappingSource of(MetadataService metadata) {
		return new AspectMappingSource(Objects.requireNonNull(metadata, "metadata"));
	}

	/** A source over a fixed set of packages — the plain-Java construction. */
	public static AspectMappingSource of(MetadataService metadata, List<EPackage> packages) {
		AspectMappingSource source = of(metadata);
		packages.forEach(source::addPackage);
		return source;
	}

	public void addPackage(EPackage ePackage) {
		packages.add(Objects.requireNonNull(ePackage, "ePackage"));
	}

	public void removePackage(EPackage ePackage) {
		packages.remove(ePackage);
	}

	@Override
	public Optional<IndexUnitMapping> mappingFor(String unit) {
		Objects.requireNonNull(unit, "unit");
		for (EPackage ePackage : packages) {
			Optional<AspectEntry> aspect = metadata.getPackageAspect(ePackage, ASPECT_TYPE_ID);
			if (aspect.isPresent() && aspect.get().getContent() instanceof IndexUnitMapping mapping
					&& unit.equals(mapping.getName())) {
				return Optional.of(mapping);
			}
		}
		return Optional.empty();
	}
}
