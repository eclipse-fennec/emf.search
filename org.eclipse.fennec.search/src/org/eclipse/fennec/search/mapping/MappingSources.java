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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.fennec.search.esearch.IndexUnitMapping;

/**
 * Composition of {@link MappingSource}s with declared precedence: the first source that
 * answers wins — the registry before the aspect plane (#32), because a deployment
 * overriding what a model ships with is the normal direction, and the reverse would make
 * a shipped mapping impossible to correct without rebuilding the model bundle.
 * <p>
 * Which source served is <b>logged at resolution time</b>: "which mapping am I actually
 * running" is the first question when an index looks wrong, and it deserves an answer
 * that does not require a debugger.
 *
 * @author Data In Motion Consulting
 */
public final class MappingSources {

	private static final Logger LOG = System.getLogger(MappingSources.class.getName());

	private MappingSources() {
	}

	/** The sources in precedence order — the first that answers a unit wins. */
	public static MappingSource withPrecedence(MappingSource... sources) {
		List<MappingSource> ordered = List.of(sources);
		if (ordered.isEmpty()) {
			throw new IllegalArgumentException("At least one mapping source is needed");
		}
		return new MappingSource() {
			@Override
			public Optional<IndexUnitMapping> mappingFor(String unit) {
				Objects.requireNonNull(unit, "unit");
				for (int i = 0; i < ordered.size(); i++) {
					Optional<IndexUnitMapping> mapping = ordered.get(i).mappingFor(unit);
					if (mapping.isPresent()) {
						if (ordered.size() > 1) {
							LOG.log(Level.INFO, "Unit ''{0}'' resolves its mapping from source {1} of {2}"
									+ " ({3}); later sources are shadowed.",
									unit, i + 1, ordered.size(), ordered.get(i).getClass().getSimpleName());
						}
						return mapping;
					}
				}
				return Optional.empty();
			}

			@Override
			public void addListener(MappingListener listener) {
				ordered.forEach(source -> source.addListener(listener));
			}

			@Override
			public void removeListener(MappingListener listener) {
				ordered.forEach(source -> source.removeListener(listener));
			}
		};
	}
}
