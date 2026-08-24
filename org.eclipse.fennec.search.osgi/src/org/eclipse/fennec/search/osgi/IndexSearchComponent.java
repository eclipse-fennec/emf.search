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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistry;
import org.eclipse.fennec.persistence.api.ConverterService;
import org.eclipse.fennec.persistence.query.support.NamedOperations;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.RegistryMappingSource;
import org.eclipse.fennec.search.query.IndexSearch;
import org.eclipse.fennec.search.query.PrimaryStore;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Publishes one {@link IndexSearch} per mapped index unit — the "DS service per index
 * unit" shape of the own-API family (#41), keyed by {@code search.unit.alias}. Every
 * mapped unit gets the service: the direct API serves any OBJECTS query, so absence would
 * signal nothing.
 * <p>
 * Unlike the sibling components, a late-arriving collaborator <em>republishes</em>: a
 * {@link PrimaryStore} or {@link NamedOperations} that comes up after the units (the
 * ordinary case — JPA starts slower than an in-memory index) must reach consumers, and
 * {@code IndexSearch} instances are immutable copies, so the republish is the only road.
 *
 * @author Data In Motion Consulting
 */
@Component(name = "IndexSearch", immediate = true)
public class IndexSearchComponent {

	private final BundleContext context;
	private final Map<String, IndexUnit> units = new ConcurrentHashMap<>();
	private final Map<String, ServiceRegistration<IndexSearch>> registrations = new ConcurrentHashMap<>();
	private volatile EObjectRegistry mappingRegistry;
	private volatile ConverterService converter;
	private volatile NamedOperations catalog;
	private volatile PrimaryStore primaryStore;

	@Activate
	public IndexSearchComponent(BundleContext context) {
		this.context = context;
	}

	@Reference(name = "mappingRegistry", cardinality = ReferenceCardinality.OPTIONAL,
			policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY,
			target = "(emf.eobject.registry.name=" + SearchConstants.MAPPING_REGISTRY_NAME + ")")
	void setMappingRegistry(EObjectRegistry registry) {
		this.mappingRegistry = registry;
		republish();
	}

	void unsetMappingRegistry(EObjectRegistry registry) {
		this.mappingRegistry = null;
		registrations.values().forEach(ServiceRegistration::unregister);
		registrations.clear();
	}

	@Reference(name = "converter", cardinality = ReferenceCardinality.OPTIONAL,
			policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	void setConverter(ConverterService converter) {
		this.converter = converter;
		republish();
	}

	void unsetConverter(ConverterService converter) {
		this.converter = null;
		republish();
	}

	@Reference(name = "catalog", cardinality = ReferenceCardinality.OPTIONAL,
			policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	void setCatalog(NamedOperations catalog) {
		this.catalog = catalog;
		republish();
	}

	void unsetCatalog(NamedOperations catalog) {
		this.catalog = null;
		republish();
	}

	@Reference(name = "primaryStore", cardinality = ReferenceCardinality.OPTIONAL,
			policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	void setPrimaryStore(PrimaryStore primaryStore) {
		this.primaryStore = primaryStore;
		republish();
	}

	void unsetPrimaryStore(PrimaryStore primaryStore) {
		this.primaryStore = null;
		republish();
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
			policyOption = ReferencePolicyOption.GREEDY)
	void addUnit(IndexUnit unit, Map<String, Object> properties) {
		Object alias = properties.get(SearchConstants.UNIT_ALIAS);
		if (alias instanceof String name && !name.isBlank()) {
			units.put(name, unit);
			// A replaced unit (static-greedy reactivation upstream) binds before the old one
			// unbinds: rebuild, so the service never wraps a closed unit.
			ServiceRegistration<IndexSearch> stale = registrations.remove(name);
			if (stale != null) {
				stale.unregister();
			}
			publish(name, unit);
		}
	}

	void removeUnit(IndexUnit unit, Map<String, Object> properties) {
		Object alias = properties.get(SearchConstants.UNIT_ALIAS);
		// Only the departure of the published unit tears the service down — on a swap the
		// old unit's unbind arrives after the replacement and must not win.
		if (alias instanceof String name && units.remove(name, unit)) {
			ServiceRegistration<IndexSearch> registration = registrations.remove(name);
			if (registration != null) {
				registration.unregister();
			}
		}
	}

	/** Rebuilds every published service against the current collaborators. */
	private void republish() {
		registrations.values().forEach(ServiceRegistration::unregister);
		registrations.clear();
		units.forEach(this::publish);
	}

	private void publish(String name, IndexUnit unit) {
		EObjectRegistry registry = mappingRegistry;
		if (registry == null || registrations.containsKey(name)) {
			return;
		}
		Optional<IndexUnitMapping> mapping = RegistryMappingSource.of(registry).mappingFor(name);
		if (mapping.isEmpty()) {
			return;
		}
		IndexSearch search = IndexSearch.of(unit, IndexSchema.of(mapping.get()));
		ConverterService boundConverter = converter;
		if (boundConverter != null) {
			search = search.withConverter(boundConverter);
		}
		NamedOperations boundCatalog = catalog;
		if (boundCatalog != null) {
			search = search.withCatalog(boundCatalog);
		}
		PrimaryStore boundStore = primaryStore;
		if (boundStore != null) {
			search = search.withPrimaryStore(boundStore);
		}
		Dictionary<String, Object> serviceProperties = new Hashtable<>();
		serviceProperties.put(SearchConstants.UNIT_ALIAS, name);
		registrations.put(name, context.registerService(IndexSearch.class, search, serviceProperties));
	}

	@Deactivate
	void deactivate() {
		registrations.values().forEach(ServiceRegistration::unregister);
		registrations.clear();
	}
}
