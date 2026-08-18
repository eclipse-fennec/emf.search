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
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistry;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.MappingException;
import org.eclipse.fennec.search.mapping.RegistryMappingSource;
import org.eclipse.fennec.search.suggest.SuggestSearch;
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
 * Publishes one {@link SuggestSearch} per index unit whose mapping declares suggest
 * sources — the §6 "DS service per index unit", and nothing else: the mechanics live in
 * the plain suggest bundle. A unit whose mapping declares no source gets no service;
 * absence is the signal, exactly like the registry components upstream.
 *
 * @author Data In Motion Consulting
 */
@Component(name = "SearchSuggest", immediate = true)
public class SuggestComponent {

	private static final Logger LOG = System.getLogger(SuggestComponent.class.getName());

	private final BundleContext context;
	private final Map<String, IndexUnit> units = new ConcurrentHashMap<>();
	private final Map<String, ServiceRegistration<SuggestSearch>> registrations = new ConcurrentHashMap<>();
	private volatile EObjectRegistry mappingRegistry;

	@Activate
	public SuggestComponent(BundleContext context) {
		this.context = context;
	}

	@Reference(name = "mappingRegistry", cardinality = ReferenceCardinality.OPTIONAL,
			policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY,
			target = "(emf.eobject.registry.name=" + SearchConstants.MAPPING_REGISTRY_NAME + ")")
	void setMappingRegistry(EObjectRegistry registry) {
		this.mappingRegistry = registry;
		// DS binds in arbitrary order: units that arrived before the registry get their
		// service now.
		units.forEach(this::publish);
	}

	void unsetMappingRegistry(EObjectRegistry registry) {
		this.mappingRegistry = null;
		registrations.values().forEach(ServiceRegistration::unregister);
		registrations.clear();
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
			policyOption = ReferencePolicyOption.GREEDY)
	void addUnit(IndexUnit unit, Map<String, Object> properties) {
		Object alias = properties.get(SearchConstants.UNIT_ALIAS);
		if (alias instanceof String name && !name.isBlank()) {
			units.put(name, unit);
			publish(name, unit);
		}
	}

	void removeUnit(IndexUnit unit, Map<String, Object> properties) {
		Object alias = properties.get(SearchConstants.UNIT_ALIAS);
		if (alias instanceof String name) {
			units.remove(name, unit);
			ServiceRegistration<SuggestSearch> registration = registrations.remove(name);
			if (registration != null) {
				registration.unregister();
			}
		}
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
		try {
			SuggestSearch suggest = SuggestSearch.of(unit, IndexSchema.of(mapping.get()));
			Dictionary<String, Object> serviceProperties = new Hashtable<>();
			serviceProperties.put(SearchConstants.UNIT_ALIAS, name);
			registrations.put(name, context.registerService(SuggestSearch.class, suggest, serviceProperties));
		} catch (MappingException noSources) {
			// The mapping declares no suggest source (or refuses one): no service — the
			// refusal is resolution-time and belongs to whoever authored the mapping.
			LOG.log(Level.DEBUG, "Unit ''{0}'' publishes no suggest service: {1}", name,
					noSources.getMessage());
		}
	}

	@Deactivate
	void deactivate() {
		registrations.values().forEach(ServiceRegistration::unregister);
		registrations.clear();
	}
}
