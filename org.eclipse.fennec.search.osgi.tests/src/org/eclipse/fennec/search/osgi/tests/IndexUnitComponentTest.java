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
package org.eclipse.fennec.search.osgi.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.eclipse.fennec.search.osgi.SearchConstants;
import org.eclipse.fennec.search.unit.AccessMode;
import org.eclipse.fennec.search.unit.IndexUnit;
import org.eclipse.fennec.search.unit.Visibility;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Wiring only, on purpose: that a factory configuration produces a unit, that the unit is
 * findable by its alias, that two configurations coexist, and that deleting a configuration
 * takes the unit away again.
 * <p>
 * What the unit <em>does</em> — visibility, commit triggers, access modes — is covered by
 * plain JUnit in the core bundle, where it runs in milliseconds without a framework. An
 * assertion about search behaviour has no business in here.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
class IndexUnitComponentTest {

	private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(10);

	private static Dictionary<String, Object> unitConfig(String alias) {
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("alias", alias);
		properties.put("location", "memory");
		properties.put("refresh", "MANUAL");
		return properties;
	}

	private static IndexUnit awaitUnit(BundleContext context, String alias) throws Exception {
		String filter = "(&(objectClass=" + IndexUnit.class.getName() + ")("
				+ SearchConstants.UNIT_ALIAS + "=" + alias + "))";
		long deadline = System.currentTimeMillis() + TIMEOUT;
		while (System.currentTimeMillis() < deadline) {
			ServiceReference<?>[] references = context.getAllServiceReferences(null, filter);
			if (references != null && references.length > 0) {
				return (IndexUnit) context.getService(references[0]);
			}
			Thread.sleep(20);
		}
		return null;
	}

	/**
	 * The component unregisters the service <em>before</em> closing the unit, so a consumer
	 * still holding it is never handed one that closes underneath. That ordering makes the
	 * withdrawal no proof of the close: deactivation is asynchronous, and the close lands
	 * some moments after the service is gone. So this waits for it rather than sampling it.
	 */
	private static boolean awaitClosed(IndexUnit unit) throws Exception {
		long deadline = System.currentTimeMillis() + TIMEOUT;
		while (System.currentTimeMillis() < deadline) {
			if (unit.isClosed()) {
				return true;
			}
			Thread.sleep(20);
		}
		return false;
	}

	private static boolean awaitGone(BundleContext context, String alias) throws Exception {
		String filter = "(&(objectClass=" + IndexUnit.class.getName() + ")("
				+ SearchConstants.UNIT_ALIAS + "=" + alias + "))";
		long deadline = System.currentTimeMillis() + TIMEOUT;
		while (System.currentTimeMillis() < deadline) {
			ServiceReference<?>[] references = context.getAllServiceReferences(null, filter);
			if (references == null || references.length == 0) {
				return true;
			}
			Thread.sleep(20);
		}
		return false;
	}

	@Test
	void aFactoryConfigurationPublishesAUnitUnderItsAlias(@InjectBundleContext BundleContext context,
			@InjectService ConfigurationAdmin admin) throws Exception {

		Configuration configuration = admin.createFactoryConfiguration(SearchConstants.UNIT_PID, "?");
		try {
			configuration.update(unitConfig("catalog"));

			IndexUnit unit = awaitUnit(context, "catalog");

			assertThat(unit).as("a configured unit is published").isNotNull();
			assertThat(unit.name()).isEqualTo("catalog");
			assertThat(unit.isClosed()).isFalse();
		} finally {
			configuration.delete();
		}
	}

	@Test
	void configuredModesReachTheUnit(@InjectBundleContext BundleContext context,
			@InjectService ConfigurationAdmin admin) throws Exception {

		Configuration configuration = admin.createFactoryConfiguration(SearchConstants.UNIT_PID, "?");
		try {
			Dictionary<String, Object> properties = unitConfig("readonly-unit");
			properties.put("access", "READ_ONLY");
			properties.put("visibility", "COMMITTED");
			configuration.update(properties);

			IndexUnit unit = awaitUnit(context, "readonly-unit");

			assertThat(unit).isNotNull();
			assertThat(unit.config().access()).isEqualTo(AccessMode.READ_ONLY);
			assertThat(unit.config().visibility()).isEqualTo(Visibility.COMMITTED);
		} finally {
			configuration.delete();
		}
	}

	@Test
	void twoConfigurationsProduceTwoIndependentUnits(@InjectBundleContext BundleContext context,
			@InjectService ConfigurationAdmin admin) throws Exception {

		Configuration first = admin.createFactoryConfiguration(SearchConstants.UNIT_PID, "?");
		Configuration second = admin.createFactoryConfiguration(SearchConstants.UNIT_PID, "?");
		try {
			first.update(unitConfig("alpha"));
			second.update(unitConfig("beta"));

			IndexUnit alpha = awaitUnit(context, "alpha");
			IndexUnit beta = awaitUnit(context, "beta");

			assertThat(alpha).isNotNull();
			assertThat(beta).isNotNull();
			assertThat(alpha).as("each configuration gets its own unit").isNotSameAs(beta);
			assertThat(alpha.name()).isEqualTo("alpha");
			assertThat(beta.name()).isEqualTo("beta");
		} finally {
			first.delete();
			second.delete();
		}
	}

	@Test
	void deletingTheConfigurationUnregistersAndClosesTheUnit(@InjectBundleContext BundleContext context,
			@InjectService ConfigurationAdmin admin) throws Exception {

		Configuration configuration = admin.createFactoryConfiguration(SearchConstants.UNIT_PID, "?");
		configuration.update(unitConfig("transient-unit"));
		IndexUnit unit = awaitUnit(context, "transient-unit");
		assertThat(unit).isNotNull();

		configuration.delete();

		assertThat(awaitGone(context, "transient-unit")).as("the service is withdrawn").isTrue();
		assertThat(awaitClosed(unit)).as("and the unit itself is closed, not just hidden").isTrue();
	}

	@Test
	void theServiceIsInjectableTheOrdinaryWay(@InjectService(cardinality = 0) ServiceAware<IndexUnit> units,
			@InjectService ConfigurationAdmin admin, @InjectBundleContext BundleContext context)
			throws Exception {

		Configuration configuration = admin.createFactoryConfiguration(SearchConstants.UNIT_PID, "?");
		try {
			configuration.update(unitConfig("injected"));
			awaitUnit(context, "injected");

			assertThat(units.waitForService(TIMEOUT)).as("a consumer can just @InjectService it").isNotNull();
		} finally {
			configuration.delete();
		}
	}
}
