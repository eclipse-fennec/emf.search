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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.fennec.emf.osgi.annotation.ConfiguratorType;
import org.eclipse.fennec.emf.osgi.annotation.provide.EMFConfigurator;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistry;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryWriter;
import org.eclipse.fennec.emf.osgi.metadata.MetadataService;
import org.eclipse.fennec.persistence.api.ConverterService;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.mapping.AspectMappingSource;
import org.eclipse.fennec.search.mapping.DocumentMapper;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappingSource;
import org.eclipse.fennec.search.mapping.MappingSources;
import org.eclipse.fennec.search.mapping.RegistryMappingSource;
import org.eclipse.fennec.search.materialization.EmfBinaryObjectSerializer;
import org.eclipse.fennec.search.materialization.ObjectSerializer;
import org.eclipse.fennec.search.materialization.ObjectSerializers;
import org.eclipse.fennec.search.resource.SearchResource;
import org.eclipse.fennec.search.resource.SearchUris;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * The OSGi front door of #7/#32 — the {@code MongoResourceFactoryComponent} shape: one
 * {@code Resource.Factory} for the {@code lucene} protocol, picked up by emf.osgi's
 * resource-factory registry through the {@link EMFConfigurator} properties, dispatching on
 * the URI's authority to the {@link IndexUnit} service of that alias.
 * <p>
 * The mapping comes through the {@link MappingSource} composition of #32: the mapping
 * registry (authored {@code *.esearch}) before the metadata aspect plane (what a model
 * bundle ships) — resolved lazily per unit and cached as a {@link DocumentMapper}. A
 * changed registry entry drops the cache, so the <em>next</em> resource sees the new
 * mapping; documents written under the previous shape are not rewritten — a shape change
 * is a rebuild, and the unit will carry the mapping identity in its commit data once #20
 * lands.
 * <p>
 * The whiteboards the core left open arrive here: {@link ObjectSerializer} services join
 * the defaults, an optional {@link ConverterService} and the named-query catalog registry
 * ride into every created resource.
 *
 * @author Data In Motion Consulting
 */
@Component(name = SearchConstants.RESOURCE_FACTORY_NAME, immediate = true, service = Resource.Factory.class)
@EMFConfigurator(configuratorName = "lucene", configuratorType = ConfiguratorType.RESOURCE_FACTORY,
		protocol = SearchUris.SCHEME)
public class SearchResourceFactoryComponent implements Resource.Factory {

	private static final Logger LOG = System.getLogger(SearchResourceFactoryComponent.class.getName());

	private final Map<String, IndexUnit> units = new ConcurrentHashMap<>();
	private final Map<String, DocumentMapper> mappers = new ConcurrentHashMap<>();
	private final List<ObjectSerializer> boundSerializers = new CopyOnWriteArrayList<>();
	// Kept here, not only in the aspect source: DS binds in arbitrary order, so packages
	// arriving before the MetadataService must be replayed into the source it creates.
	private final List<EPackage> boundPackages = new CopyOnWriteArrayList<>();

	private volatile RegistryMappingSource registrySource;
	private volatile AspectMappingSource aspectSource;
	private volatile ObjectSerializers serializers = ObjectSerializers.withDefaults();
	private volatile ConverterService converter;
	private volatile EObjectRegistryWriter queryCatalog;

	private final MappingSource.MappingListener invalidation = unit -> {
		if (mappers.remove(unit) != null) {
			LOG.log(Level.WARNING, "The mapping of unit ''{0}'' changed: new resources see the new "
					+ "mapping, documents written under the previous shape are not rewritten — a "
					+ "shape-relevant change means a rebuild of the index.", unit);
		}
	};

	// --- units ------------------------------------------------------------------------------

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
			policyOption = ReferencePolicyOption.GREEDY)
	void addUnit(IndexUnit unit, Map<String, Object> properties) {
		Object alias = properties.get(SearchConstants.UNIT_ALIAS);
		if (alias instanceof String name && !name.isBlank()) {
			units.put(name, unit);
		}
	}

	void removeUnit(IndexUnit unit, Map<String, Object> properties) {
		Object alias = properties.get(SearchConstants.UNIT_ALIAS);
		if (alias instanceof String name) {
			units.remove(name, unit);
			mappers.remove(name);
		}
	}

	// --- mapping delivery ---------------------------------------------------------------------

	@Reference(name = "mappingRegistry", cardinality = ReferenceCardinality.OPTIONAL,
			policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY,
			target = "(emf.eobject.registry.name=" + SearchConstants.MAPPING_REGISTRY_NAME + ")")
	void setMappingRegistry(EObjectRegistry registry) {
		RegistryMappingSource source = RegistryMappingSource.of(registry);
		source.addListener(invalidation);
		this.registrySource = source;
		mappers.clear();
	}

	void unsetMappingRegistry(EObjectRegistry registry) {
		this.registrySource = null;
		mappers.clear();
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC,
			policyOption = ReferencePolicyOption.GREEDY)
	void setMetadataService(MetadataService metadata) {
		AspectMappingSource source = AspectMappingSource.of(metadata);
		boundPackages.forEach(source::addPackage);
		this.aspectSource = source;
		mappers.clear();
	}

	void unsetMetadataService(MetadataService metadata) {
		this.aspectSource = null;
		mappers.clear();
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
			target = "(emf.name=*)")
	void addEPackage(EPackage ePackage) {
		boundPackages.add(ePackage);
		AspectMappingSource source = aspectSource;
		if (source != null) {
			source.addPackage(ePackage);
			mappers.clear();
		}
	}

	void removeEPackage(EPackage ePackage) {
		boundPackages.remove(ePackage);
		AspectMappingSource source = aspectSource;
		if (source != null) {
			source.removePackage(ePackage);
		}
	}

	// --- collaborators the core left open -------------------------------------------------------

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addObjectSerializer(ObjectSerializer serializer) {
		boundSerializers.add(serializer);
		rebuildSerializers();
	}

	void removeObjectSerializer(ObjectSerializer serializer) {
		boundSerializers.remove(serializer);
		rebuildSerializers();
	}

	private void rebuildSerializers() {
		ObjectSerializer[] all = boundSerializers.toArray(ObjectSerializer[]::new);
		ObjectSerializer[] withDefault = new ObjectSerializer[all.length + 1];
		withDefault[0] = new EmfBinaryObjectSerializer();
		System.arraycopy(all, 0, withDefault, 1, all.length);
		this.serializers = ObjectSerializers.of(withDefault);
		mappers.clear();
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC,
			policyOption = ReferencePolicyOption.GREEDY)
	void setConverterService(ConverterService converter) {
		this.converter = converter;
	}

	void unsetConverterService(ConverterService converter) {
		this.converter = null;
	}

	@Reference(name = "queryCatalog", cardinality = ReferenceCardinality.OPTIONAL,
			policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY,
			target = "(emf.eobject.registry.name=" + SearchConstants.QUERY_CATALOG_NAME + ")")
	void setQueryCatalog(EObjectRegistryWriter catalog) {
		this.queryCatalog = catalog;
	}

	void unsetQueryCatalog(EObjectRegistryWriter catalog) {
		this.queryCatalog = null;
	}

	// --- the factory ------------------------------------------------------------------------------

	@Override
	public Resource createResource(URI uri) {
		SearchUris address = SearchUris.parse(uri);
		IndexUnit unit = units.get(address.unit());
		if (unit == null) {
			throw new IllegalStateException("No index unit '" + address.unit() + "' is available. Configure "
					+ "one (factory PID " + SearchConstants.UNIT_PID + ") with "
					+ SearchConstants.UNIT_ALIAS + "=" + address.unit() + ".");
		}
		DocumentMapper mapper = mappers.computeIfAbsent(address.unit(), this::resolveMapper);
		return new SearchResource(uri, unit, mapper, queryCatalog, converter);
	}

	/** Resolution time (#32): the composed source answers, or the refusal names both roads. */
	private DocumentMapper resolveMapper(String unit) {
		RegistryMappingSource registry = registrySource;
		AspectMappingSource aspects = aspectSource;
		MappingSource source = registry != null && aspects != null
				? MappingSources.withPrecedence(registry, aspects)
				: registry != null ? registry
				: aspects != null ? aspects
				: name -> Optional.empty();
		IndexUnitMapping mapping = source.mappingFor(unit)
				.orElseThrow(() -> new IllegalStateException("No mapping for unit '" + unit + "': neither "
						+ "the mapping registry ('" + SearchConstants.MAPPING_REGISTRY_NAME
						+ "') carries an *.esearch declaring it, nor does a registered model ship one as "
						+ "its '" + AspectMappingSource.ASPECT_TYPE_ID + "' aspect."));
		return DocumentMapper.of(IndexSchema.of(mapping), serializers);
	}
}
