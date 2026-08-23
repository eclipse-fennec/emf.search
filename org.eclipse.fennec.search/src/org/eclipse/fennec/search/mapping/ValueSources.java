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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.search.esearch.FeatureSource;
import org.eclipse.fennec.search.esearch.FieldMapping;
import org.eclipse.fennec.search.esearch.OclSource;
import org.eclipse.fennec.search.esearch.PathSource;
import org.eclipse.fennec.search.esearch.ValueSource;

/**
 * The extraction ladder of §4.2 (S20, #28): where the value written into a field comes from,
 * when it is not simply one attribute of the mapped object.
 * <p>
 * The ladder is ordered by what has to run before the value is known, and the rule is to use
 * the weakest sufficient rung, because the lower ones can be checked against the metamodel
 * without evaluating anything. Two of the three are implemented, and that is a deliberate
 * line rather than an unfinished one:
 * <ul>
 * <li>{@link FeatureSource} — one attribute, read with {@code eGet};</li>
 * <li>{@link PathSource} — a chain of features ({@code manufacturer.name}), every segment
 * checked against the model when the mapping is read;</li>
 * <li>{@link OclSource} — <b>refused</b>. It would put the m2x OCL engine into the load path
 * of every deployment, including the ones whose mapping computes nothing, and this backend
 * does not charge for features nobody declared. The refusal names both ways out, and the
 * second one is the better answer anyway: a <em>derived</em> {@code EStructuralFeature} with
 * the m2x derivation annotation is computed by EMF itself, is addressable by the canonical
 * query IR, and reaches this mapper as an ordinary feature with no special case at all.</li>
 * </ul>
 * <b>What a navigating source costs.</b> The document then depends on another object's state:
 * the value is recomputed when the <em>owner</em> is saved, and a change to the referenced
 * object does not refresh it — the same exposure {@code EMBED} and {@code NESTED} carry, and
 * the reason {@link IndexSchema#dependencies(EClass)} reports those paths rather than leaving
 * them to be discovered.
 *
 * @author Data In Motion Consulting
 */
final class ValueSources {

	private ValueSources() {
	}

	/** Whether this field takes its value from declared sources rather than from its feature. */
	static boolean isComputed(FieldMapping field) {
		return !field.getSources().isEmpty();
	}

	/**
	 * Checks a field's sources against the metamodel — everything that can be known before a
	 * document exists, which is the entire point of the two implemented rungs.
	 *
	 * @param owner the class whose documents this field is written into
	 * @param field the field mapping to check
	 * @param name the effective field name, for messages
	 * @throws MappingException if the field says where its value comes from twice, has no
	 *         name to be written under, or declares a source this backend does not serve
	 */
	static void validate(EClass owner, FieldMapping field, String name) {
		if (!isComputed(field)) {
			return;
		}
		if (field.getFeature() != null) {
			throw new MappingException("Field '" + name + "' on " + owner.getName() + " declares both a "
					+ "feature and sources. That says where the value comes from twice, and two "
					+ "declarations can disagree — keep the feature for the plain case, or move it into "
					+ "a FeatureSource beside the others.");
		}
		if (field.getName() == null || field.getName().isBlank()) {
			throw new MappingException("A computed field on " + owner.getName() + " has no name. A field "
					+ "fed by sources has no attribute to take its name from, so it has to carry one.");
		}
		for (ValueSource source : field.getSources()) {
			validate(owner, source, name);
		}
	}

	/**
	 * A sub-field is another projection of its parent's attribute — that is what makes it a
	 * sub-field. One with sources of its own would be a different value under a name that says
	 * otherwise, so it is refused and told where a computed field belongs.
	 */
	static void refuseComputedSubField(FieldMapping sub, String name) {
		if (isComputed(sub)) {
			throw new MappingException("Sub-field '" + name + "' declares sources. A sub-field is "
					+ "another projection of its parent's attribute; a value computed from somewhere "
					+ "else is a field of its own — declare it beside the parent, with a name.");
		}
	}

	private static void validate(EClass owner, ValueSource source, String name) {
		if (source instanceof FeatureSource feature) {
			if (feature.getFeature() == null) {
				throw new MappingException("A FeatureSource of '" + name + "' names no attribute.");
			}
			return;
		}
		if (source instanceof PathSource path) {
			validatePath(owner, path, name);
			return;
		}
		if (source instanceof OclSource) {
			throw new MappingException("Field '" + name + "' on " + owner.getName() + " is computed by "
					+ "an OclSource, which this backend does not serve: evaluating expressions would put "
					+ "the m2x OCL engine into the load path of every deployment, including those whose "
					+ "mapping computes nothing. Two ways out, both better here — declare a PathSource "
					+ "when the value is reachable by navigation, or put the expression on a derived "
					+ "EStructuralFeature (the m2x derivation annotation). A derived feature is computed "
					+ "by EMF, is addressable by the query IR, and arrives here as an ordinary feature.");
		}
		throw new MappingException("Value source " + source.eClass().getName() + " on '" + name
				+ "' has no extraction; the metamodel grew without this backend noticing.");
	}

	private static void validatePath(EClass owner, PathSource path, String name) {
		EClass current = owner;
		List<EStructuralFeature> segments = path.getSegments();
		for (int i = 0; i < segments.size(); i++) {
			EStructuralFeature segment = segments.get(i);
			if (segment == null) {
				throw new MappingException("The path of '" + name + "' has an unresolved segment at "
						+ "position " + i + " — the mapping references a feature its model does not have.");
			}
			if (segment.getEContainingClass() != null
					&& !segment.getEContainingClass().isSuperTypeOf(current)) {
				throw new MappingException("The path of '" + name + "' navigates '" + segment.getName()
						+ "' on " + current.getName() + ", which does not have it. A path is checked "
						+ "against the model, segment by segment.");
			}
			if (i == segments.size() - 1) {
				if (!(segment instanceof EAttribute)) {
					throw new MappingException("The path of '" + name + "' ends on the reference '"
							+ segment.getName() + "'. A field holds a value, so a path has to end on an "
							+ "attribute — add the attribute to read from it.");
				}
				return;
			}
			if (!(segment instanceof EReference reference)) {
				throw new MappingException("The path of '" + name + "' continues past the attribute '"
						+ segment.getName() + "'. Only a reference can be navigated through.");
			}
			current = reference.getEReferenceType();
		}
	}

	/**
	 * The values this field's sources yield for one object, in declaration order. A source
	 * that finds nothing — an unset attribute, a path through a null reference — contributes
	 * nothing rather than a null value.
	 */
	static List<Object> values(EClass owner, FieldMapping field, String name, EObject self) {
		List<Object> values = new ArrayList<>();
		for (ValueSource source : field.getSources()) {
			values.addAll(values(owner, source, name, self));
		}
		return values;
	}

	private static List<Object> values(EClass owner, ValueSource source, String name, EObject self) {
		if (source instanceof FeatureSource feature) {
			return valuesOf(self, feature.getFeature());
		}
		if (source instanceof PathSource path) {
			List<Object> reached = List.of(self);
			for (EStructuralFeature segment : path.getSegments()) {
				reached = step(reached, segment);
			}
			return reached;
		}
		// Anything else was refused when the mapping was read; reaching here would mean the
		// schema and the writer disagree about what is served.
		validate(owner, source, name);
		return List.of();
	}

	/** One navigation step over everything reached so far — a many-reference fans out. */
	private static List<Object> step(List<Object> reached, EStructuralFeature segment) {
		List<Object> next = new ArrayList<>();
		for (Object each : reached) {
			if (each instanceof EObject object
					&& object.eClass().getEAllStructuralFeatures().contains(segment)) {
				next.addAll(valuesOf(object, segment));
			}
		}
		return next;
	}

	private static List<Object> valuesOf(EObject object, EStructuralFeature feature) {
		if (feature == null || !object.eIsSet(feature)) {
			return List.of();
		}
		return flatten(object.eGet(feature));
	}

	private static List<Object> flatten(Object value) {
		if (value == null) {
			return List.of();
		}
		if (value instanceof Collection<?> collection) {
			List<Object> values = new ArrayList<>(collection.size());
			for (Object element : collection) {
				if (element != null) {
					values.add(element);
				}
			}
			return values;
		}
		return List.of(value);
	}

	/**
	 * What this field reads beyond the object itself, as dotted paths — the static half of the
	 * staleness answer, and the reason the navigating rung is a declaration rather than an
	 * expression. A stream-fed index (S10) needs exactly this list to know which change should
	 * have refreshed which document.
	 */
	static Set<String> dependencies(FieldMapping field) {
		Set<String> paths = new LinkedHashSet<>();
		for (ValueSource source : field.getSources()) {
			if (source instanceof PathSource path && path.getSegments().size() > 1) {
				paths.add(dotted(path.getSegments()));
			}
		}
		return paths;
	}

	private static String dotted(List<EStructuralFeature> segments) {
		List<String> names = new ArrayList<>(segments.size());
		for (EStructuralFeature segment : segments) {
			names.add(segment == null ? "?" : segment.getName());
		}
		return String.join(".", names);
	}
}
