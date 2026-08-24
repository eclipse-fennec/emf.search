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
import java.util.Set;
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
 * <p>
 * The rebuild cadence stays caller-owned by default (#48). A deployment opts into the
 * automatic one per unit, through this component's configuration:
 *
 * <pre>
 * rebuildOnCommit = ["catalog", "docs"]
 * </pre>
 *
 * Named units then rebuild their suggesters after every commit — on the committing
 * thread, so a unit tuned to commit per document should not be named here.
 *
 * @author Data In Motion Consulting
 */
@Component(name = "SearchSuggest", immediate = true)
public class SuggestComponent {

	private static final Logger LOG = System.getLogger(SuggestComponent.class.getName());

	/** Configuration of this component: which units rebuild their suggesters on commit. */
	public @interface SuggestConfig {

		/** Unit aliases that opt into the commit-driven rebuild; none by default. */
		String[] rebuildOnCommit() default {};
	}

	private final BundleContext context;
	private final Map<String, IndexUnit> units = new ConcurrentHashMap<>();
	private final Map<String, ServiceRegistration<SuggestSearch>> registrations = new ConcurrentHashMap<>();
	private final Map<String, AutoCloseable> subscriptions = new ConcurrentHashMap<>();
	private final Set<String> rebuildOnCommit;
	private volatile EObjectRegistry mappingRegistry;

	@Activate
	public SuggestComponent(BundleContext context, SuggestConfig config) {
		this.context = context;
		this.rebuildOnCommit = Set.of(config.rebuildOnCommit());
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
		unpublishAll();
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
			policyOption = ReferencePolicyOption.GREEDY)
	void addUnit(IndexUnit unit, Map<String, Object> properties) {
		Object alias = properties.get(SearchConstants.UNIT_ALIAS);
		if (alias instanceof String name && !name.isBlank()) {
			units.put(name, unit);
			// A replaced unit (static-greedy reactivation upstream) binds before the old one
			// unbinds: rebuild, so the service never wraps a closed unit.
			unpublish(name);
			publish(name, unit);
		}
	}

	void removeUnit(IndexUnit unit, Map<String, Object> properties) {
		Object alias = properties.get(SearchConstants.UNIT_ALIAS);
		// Only the departure of the published unit tears the service down — on a swap the
		// old unit's unbind arrives after the replacement and must not win.
		if (alias instanceof String name && units.remove(name, unit)) {
			unpublish(name);
		}
	}

	/** Unregisters one unit's service and ends its commit subscription, if any. */
	private void unpublish(String name) {
		ServiceRegistration<SuggestSearch> registration = registrations.remove(name);
		if (registration != null) {
			registration.unregister();
		}
		AutoCloseable subscription = subscriptions.remove(name);
		if (subscription != null) {
			try {
				subscription.close();
			} catch (Exception e) {
				// Unsubscribing from a unit that is already gone is not a failure worth
				// propagating — the listener list dies with the unit either way.
				LOG.log(Level.DEBUG, "Ending the commit subscription of ''{0}'' failed: {1}", name,
						e.getMessage());
			}
		}
	}

	private void unpublishAll() {
		Set.copyOf(registrations.keySet()).forEach(this::unpublish);
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
			if (rebuildOnCommit.contains(name)) {
				subscriptions.put(name, suggest.rebuildOnCommit());
			}
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
		unpublishAll();
	}
}
