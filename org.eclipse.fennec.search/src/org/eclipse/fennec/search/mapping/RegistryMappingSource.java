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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistry;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryEntry;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryListener;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;

/**
 * The deployment path of §4.1: authored {@code *.esearch} documents in an emf.osgi
 * {@link EObjectRegistry} — a {@code FileEObjectProvider} loads them, this source finds
 * them, and the registry's listener makes a changed mapping observable instead of
 * requiring a restart. The non-OSGi bootstrap is the same mechanism
 * ({@code EObjectRegistries.createRegistry(name, provider)}), so plain-JUnit tests
 * exercise exactly the code OSGi runs.
 * <p>
 * Lookup is by the mapping's own {@code name}: the key a file happened to be registered
 * under is a delivery detail, and two agreeing sources of truth are one too many.
 *
 * @author Data In Motion Consulting
 */
public final class RegistryMappingSource implements MappingSource {

	private final EObjectRegistry registry;
	private final List<MappingListener> listeners = new CopyOnWriteArrayList<>();

	private RegistryMappingSource(EObjectRegistry registry) {
		this.registry = registry;
		registry.addListener(new EObjectRegistryListener() {
			@Override
			public void entryAdded(EObjectRegistryEntry entry) {
				notifyUnit(entry.object());
			}

			@Override
			public void entryUpdated(EObjectRegistryEntry entry, EObjectRegistryEntry oldEntry) {
				notifyUnit(oldEntry.object());
				notifyUnit(entry.object());
			}

			@Override
			public void entryRemoved(EObjectRegistryEntry entry) {
				notifyUnit(entry.object());
			}
		});
	}

	/** A source over the given registry. */
	public static RegistryMappingSource of(EObjectRegistry registry) {
		return new RegistryMappingSource(Objects.requireNonNull(registry, "registry"));
	}

	@Override
	public Optional<IndexUnitMapping> mappingFor(String unit) {
		Objects.requireNonNull(unit, "unit");
		// The common case: the entry is keyed by the unit name it declares.
		Optional<EObject> keyed = registry.get(unit);
		if (keyed.isPresent() && keyed.get() instanceof IndexUnitMapping mapping
				&& unit.equals(mapping.getName())) {
			return Optional.of(mapping);
		}
		for (EObjectRegistryEntry entry : registry.entries()) {
			if (entry.object() instanceof IndexUnitMapping mapping && unit.equals(mapping.getName())) {
				return Optional.of(mapping);
			}
		}
		return Optional.empty();
	}

	@Override
	public void addListener(MappingListener listener) {
		listeners.add(Objects.requireNonNull(listener, "listener"));
	}

	@Override
	public void removeListener(MappingListener listener) {
		listeners.remove(listener);
	}

	private void notifyUnit(EObject object) {
		if (!(object instanceof IndexUnitMapping mapping) || mapping.getName() == null) {
			return;
		}
		for (MappingListener listener : listeners) {
			listener.mappingChanged(mapping.getName());
		}
	}
}
