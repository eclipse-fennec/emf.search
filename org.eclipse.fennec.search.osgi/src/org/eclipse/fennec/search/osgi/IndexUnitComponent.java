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

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Sort;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistry;
import org.eclipse.fennec.search.mapping.IndexOrders;
import org.eclipse.fennec.search.mapping.IndexSchema;
import org.eclipse.fennec.search.mapping.RegistryMappingSource;
import org.eclipse.fennec.search.unit.AccessMode;
import org.eclipse.fennec.search.unit.AnalyzerRegistry;
import org.eclipse.fennec.search.unit.CommitPolicy;
import org.eclipse.fennec.search.unit.IndexLocation;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.IndexUnitConfig;
import org.eclipse.fennec.search.unit.RefreshTrigger;
import org.eclipse.fennec.search.unit.Visibility;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Opens one {@link IndexUnit} per Configuration Admin factory configuration and publishes
 * it under its alias — the {@code MongoDatabaseComponent} pattern, one configuration to one
 * backing store to one service.
 * <p>
 * The component contains no search logic. Its whole job is to turn configuration into the
 * plain {@link IndexUnitConfig} the core already understands, which is what keeps the two
 * worlds from drifting: whatever a plain-Java caller can build, a configuration can express,
 * and neither has a shape of its own.
 * <p>
 * Analyzer services are bound statically and greedily, so a new or departing analyzer
 * reactivates the component and the unit reopens with a fresh registry. That is deliberate:
 * {@link AnalyzerRegistry} is immutable because swapping an analyzer under a live
 * {@code IndexWriter} would change how documents are analyzed halfway through an index.
 *
 * @author Data In Motion Consulting
 */
@Component(name = SearchConstants.UNIT_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = IndexUnitComponent.UnitConfig.class, factory = true)
public class IndexUnitComponent {

	/** Configuration of one index unit. */
	@ObjectClassDefinition(name = "Fennec Search Index Unit",
			description = "One Lucene index: where it lives, what may be done with it, "
					+ "what a searcher sees and when it is reopened and committed.")
	public @interface UnitConfig {

		@AttributeDefinition(name = "Alias",
				description = "Name of this unit, published as service property "
						+ SearchConstants.UNIT_ALIAS + " and used to select it")
		String alias();

		@AttributeDefinition(name = "Location", required = false,
				description = "'memory' for an in-memory index, otherwise a filesystem path")
		String location() default "memory";

		@AttributeDefinition(name = "Access mode", required = false,
				description = "READ_WRITE, READ_ONLY or BULK_LOAD. Note that READ_ONLY still "
						+ "takes the directory's write lock",
				options = { @org.osgi.service.metatype.annotations.Option(label = "Read/write", value = "READ_WRITE"),
						@org.osgi.service.metatype.annotations.Option(label = "Read only", value = "READ_ONLY"),
						@org.osgi.service.metatype.annotations.Option(label = "Bulk load", value = "BULK_LOAD") })
		String access() default "READ_WRITE";

		@AttributeDefinition(name = "Visibility", required = false,
				description = "NRT sees uncommitted writes, COMMITTED sees only commits",
				options = { @org.osgi.service.metatype.annotations.Option(label = "Near real-time", value = "NRT"),
						@org.osgi.service.metatype.annotations.Option(label = "Committed only", value = "COMMITTED") })
		String visibility() default "NRT";

		@AttributeDefinition(name = "Refresh trigger", required = false,
				description = "BACKGROUND, ON_COMMIT or MANUAL",
				options = { @org.osgi.service.metatype.annotations.Option(label = "Background", value = "BACKGROUND"),
						@org.osgi.service.metatype.annotations.Option(label = "On commit", value = "ON_COMMIT"),
						@org.osgi.service.metatype.annotations.Option(label = "Manual", value = "MANUAL") })
		String refresh() default "BACKGROUND";

		@AttributeDefinition(name = "Refresh interval (ms)", required = false,
				description = "Longest a write may stay invisible under BACKGROUND")
		long refresh_interval_ms() default 1000;

		@AttributeDefinition(name = "Commit after documents", required = false,
				description = "Commit once this many writes are uncommitted; 0 disables the trigger")
		int commit_max_documents() default 0;

		@AttributeDefinition(name = "Commit interval (ms)", required = false,
				description = "Commit at most this long after the previous commit; 0 disables the trigger")
		long commit_interval_ms() default 0;

		@AttributeDefinition(name = "Commit on close", required = false)
		boolean commit_on_close() default true;

		@AttributeDefinition(name = "Default analyzer", required = false,
				description = "Name of an Analyzer service (property " + SearchConstants.ANALYZER_NAME
						+ ") to use for fields that declare none; empty means the standard analyzer")
		String default_analyzer() default "";
	}

	private final IndexUnit unit;
	private final ServiceRegistration<IndexUnit> registration;

	@Activate
	public IndexUnitComponent(BundleContext context, UnitConfig config,
			@Reference(name = "analyzers", service = Analyzer.class,
					cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.STATIC,
					policyOption = ReferencePolicyOption.GREEDY) List<ServiceReference<Analyzer>> analyzers,
			@Reference(name = "mappingRegistry", cardinality = ReferenceCardinality.OPTIONAL,
					policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY,
					target = "(emf.eobject.registry.name=" + SearchConstants.MAPPING_REGISTRY_NAME + ")")
					EObjectRegistry mappingRegistry)
			throws IOException {

		AnalyzerRegistry registry = buildRegistry(context, config, analyzers);
		this.unit = IndexUnit.open(toUnitConfig(config, registry, indexSort(config, mappingRegistry)));

		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(SearchConstants.UNIT_ALIAS, config.alias());
		this.registration = context.registerService(IndexUnit.class, unit, properties);
	}

	@Deactivate
	public void deactivate() throws IOException {
		// Unregister first: a consumer that is still holding the unit must not be handed
		// one that is about to close underneath it.
		if (registration != null) {
			registration.unregister();
		}
		if (unit != null) {
			unit.close();
		}
	}

	/** The unit this component opened — for tests and for components that embed it. */
	public IndexUnit unit() {
		return unit;
	}

	private static AnalyzerRegistry buildRegistry(BundleContext context, UnitConfig config,
			List<ServiceReference<Analyzer>> references) {
		AnalyzerRegistry.Builder builder = AnalyzerRegistry.builder();
		Analyzer defaultAnalyzer = null;
		for (ServiceReference<Analyzer> reference : references) {
			Object name = reference.getProperty(SearchConstants.ANALYZER_NAME);
			if (name == null) {
				// An analyzer nobody can name is not usable in a mapping; skipping it is
				// better than inventing a name that no configuration would ever match.
				continue;
			}
			Analyzer analyzer = context.getService(reference);
			if (analyzer == null) {
				continue;
			}
			builder.register(name.toString(), analyzer);
			if (name.toString().equals(config.default_analyzer())) {
				defaultAnalyzer = analyzer;
			}
		}
		if (!config.default_analyzer().isBlank() && defaultAnalyzer == null) {
			throw new IllegalArgumentException("Index unit '" + config.alias() + "' names '"
					+ config.default_analyzer() + "' as its default analyzer, but no Analyzer service carries "
					+ SearchConstants.ANALYZER_NAME + "=" + config.default_analyzer());
		}
		if (defaultAnalyzer != null) {
			builder.defaultAnalyzer(defaultAnalyzer);
		}
		return builder.build();
	}

	/**
	 * The declared index order of this unit's mapping (S17, #19) — static and greedy like
	 * the analyzers: the order is fixed at {@code IndexWriter} creation, so a mapping
	 * registry appearing or changing reopens the unit rather than mutating a live writer.
	 * No registry or no declared sort simply means index order.
	 */
	private static Sort indexSort(UnitConfig config, EObjectRegistry mappingRegistry) {
		if (mappingRegistry == null) {
			return null;
		}
		return RegistryMappingSource.of(mappingRegistry).mappingFor(config.alias())
				.map(mapping -> IndexOrders.indexSort(IndexSchema.of(mapping)))
				.orElse(null);
	}

	private static IndexUnitConfig toUnitConfig(UnitConfig config, AnalyzerRegistry registry) {
		return toUnitConfig(config, registry, null);
	}

	private static IndexUnitConfig toUnitConfig(UnitConfig config, AnalyzerRegistry registry, Sort indexSort) {
		IndexLocation location = "memory".equalsIgnoreCase(config.location())
				? IndexLocation.inMemory()
				: IndexLocation.path(Path.of(config.location()));

		RefreshTrigger.Mode refreshMode = parse(RefreshTrigger.Mode.class, config.refresh(), "refresh");
		RefreshTrigger refresh = switch (refreshMode) {
			case BACKGROUND -> RefreshTrigger.background(Duration.ofMillis(config.refresh_interval_ms()));
			case ON_COMMIT -> RefreshTrigger.onCommit();
			case MANUAL -> RefreshTrigger.manual();
		};

		return IndexUnitConfig.builder(config.alias(), location)
				.analyzers(registry)
				.access(parse(AccessMode.class, config.access(), "access"))
				.visibility(parse(Visibility.class, config.visibility(), "visibility"))
				.refresh(refresh)
				.commit(new CommitPolicy(config.commit_max_documents(),
						Duration.ofMillis(config.commit_interval_ms()), config.commit_on_close()))
				.indexSort(indexSort)
				.build();
	}

	private static <E extends Enum<E>> E parse(Class<E> type, String value, String what) {
		try {
			return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unknown " + what + " '" + value + "'. Expected one of "
					+ List.of(type.getEnumConstants()), e);
		}
	}
}
