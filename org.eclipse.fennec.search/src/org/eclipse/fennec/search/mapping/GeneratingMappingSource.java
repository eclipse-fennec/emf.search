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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;

/**
 * A {@link MappingSource} that <b>generates</b> the mapping of a unit nobody authored one
 * for (#51) — {@link MappingGenerator}'s suggestions, served at runtime.
 * <p>
 * <b>Opt-in by construction, and last in precedence.</b> Nothing composes this source by
 * default, and where it is composed it belongs behind the authored ones
 * ({@link MappingSources#withPrecedence}): an authored mapping always wins, because a
 * generated one is a proposal and a proposal must never overrule a decision.
 * <p>
 * The reason for that care is the line §4.1 rests on: conventions never guess from a name,
 * a generator may — which is safe while a human reads the proposal before it runs, and is
 * exactly what serving a proposal at runtime removes. So this source states loudly what it
 * did: the first time it answers for a unit it logs the mapping's provenance and every
 * explanation the generator gave, at INFO, because "where does this index's mapping come
 * from" must have an answer in the log rather than in someone's memory.
 * <p>
 * Use it where the cost of a wrong guess is low and the cost of authoring is high: a
 * development setup, a model explored for the first time, a demo. Author the mapping
 * before the index carries anything you would miss.
 *
 * @author Data In Motion Consulting
 */
public final class GeneratingMappingSource implements MappingSource {

	private static final Logger LOG = System.getLogger(GeneratingMappingSource.class.getName());

	private final List<EPackage> packages = new CopyOnWriteArrayList<>();
	private final Map<String, IndexUnitMapping> generated = new ConcurrentHashMap<>();

	private GeneratingMappingSource(List<EPackage> packages) {
		this.packages.addAll(packages);
	}

	/** A generating source over the given packages. */
	public static GeneratingMappingSource of(List<EPackage> packages) {
		Objects.requireNonNull(packages, "packages");
		return new GeneratingMappingSource(packages);
	}

	/** Adds a package this source may generate for. */
	public void addPackage(EPackage ePackage) {
		Objects.requireNonNull(ePackage, "ePackage");
		packages.add(ePackage);
	}

	/** Removes a package, and the mapping generated from it. */
	public void removePackage(EPackage ePackage) {
		packages.remove(ePackage);
		generated.values().removeIf(mapping -> mapping.getEPackage() == ePackage);
	}

	/**
	 * The generated mapping of a unit named after one of this source's packages.
	 * <p>
	 * Generated once and kept: a mapping decides what a document looks like, so answering
	 * the same unit differently on a second call would mean two shapes in one index.
	 */
	@Override
	public Optional<IndexUnitMapping> mappingFor(String unit) {
		Objects.requireNonNull(unit, "unit");
		IndexUnitMapping known = generated.get(unit);
		if (known != null) {
			return Optional.of(known);
		}
		for (EPackage ePackage : packages) {
			if (!unit.equals(ePackage.getName())) {
				continue;
			}
			MappingGenerator.Suggestions suggestions = MappingGenerator.forPackage(ePackage).generate();
			IndexUnitMapping mapping = generated.computeIfAbsent(unit, key -> suggestions.mapping());
			if (mapping == suggestions.mapping()) {
				LOG.log(Level.INFO, "Unit ''{0}'' has no authored mapping; serving one generated from "
						+ "''{1}''. Author it before the index carries data you would miss. The "
						+ "generator''s reasoning:", unit, ePackage.getNsURI());
				suggestions.explanations().forEach(explanation -> LOG.log(Level.INFO, "  {0}", explanation));
			}
			return Optional.of(mapping);
		}
		return Optional.empty();
	}
}
