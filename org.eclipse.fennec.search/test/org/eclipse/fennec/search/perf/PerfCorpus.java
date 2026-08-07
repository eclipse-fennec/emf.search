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
package org.eclipse.fennec.search.perf;

import java.util.List;
import java.util.Locale;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.search.esearch.DocumentMapping;
import org.eclipse.fennec.search.esearch.ESearchFactory;
import org.eclipse.fennec.search.esearch.IndexUnitMapping;
import org.eclipse.fennec.search.esearch.KeywordFieldMapping;
import org.eclipse.fennec.search.esearch.ReferenceMapping;
import org.eclipse.fennec.search.esearch.ReferenceStrategy;
import org.eclipse.fennec.search.esearch.TextFieldMapping;
import org.eclipse.fennec.search.mapping.TestModels;

/**
 * Corpus generation and sizing shared by the performance suites.
 * <p>
 * Sizes are deliberately modest by default so {@code perfTest} stays usable during
 * development, and scale up through {@code -Dsearch.perf.scale=N}: the point of the
 * scaling tests is the <em>shape</em> of the curve, which is visible at a small factor and
 * only gets more expensive to measure at a large one.
 */
final class PerfCorpus {

	/** Multiplies every corpus size; raise it to look at a bigger index. */
	static final int SCALE = Integer.getInteger("search.perf.scale", 1);

	private static final ESearchFactory ESEARCH = ESearchFactory.eINSTANCE;
	private static final String[] WORDS = { "widget", "gadget", "sprocket", "flange", "bearing",
			"coupling", "gasket", "bracket", "spindle", "grommet" };
	private static final String[] CONDITIONS = { "USED", "REFURBISHED" };

	private final TestModels models;
	private final EClass product;
	private final EClass review;

	private PerfCorpus(TestModels models) {
		this.models = models;
		this.product = models.eClass("Product");
		this.review = models.eClass("Review");
	}

	static PerfCorpus load() {
		return new PerfCorpus(TestModels.load("catalog.ecore"));
	}

	EClass product() {
		return product;
	}

	TestModels models() {
		return models;
	}

	/** A product with every convention-mapped attribute filled, and {@code reviews} children. */
	EObject product(int index, int reviewCount) {
		EObject object = EcoreUtil.create(product);
		object.eSet(feature("Product", "id"), "p-" + index);
		object.eSet(feature("Product", "name"), WORDS[index % WORDS.length] + " " + index);
		object.eSet(feature("Product", "description"),
				String.format(Locale.ROOT, "A %s for %s use, part number %d",
						WORDS[index % WORDS.length], WORDS[(index / 7) % WORDS.length], index));
		object.eSet(feature("Product", "price"), 9.99 + index % 500);
		object.eSet(feature("Product", "stock"), index % 997 + 1);
		object.eSet(feature("Product", "available"), index % 3 != 0);
		EAttribute condition = (EAttribute) feature("Product", "condition");
		object.eSet(condition, condition.getEAttributeType().getEPackage().getEFactoryInstance()
				.createFromString((org.eclipse.emf.ecore.EDataType) condition.getEAttributeType(),
						CONDITIONS[index % CONDITIONS.length]));
		@SuppressWarnings("unchecked")
		List<String> tags = (List<String>) object.eGet(feature("Product", "tags"));
		tags.add(WORDS[index % WORDS.length]);
		tags.add(index % 2 == 0 ? "even" : "odd");
		if (reviewCount > 0) {
			@SuppressWarnings("unchecked")
			List<EObject> reviews = (List<EObject>) object.eGet(feature("Product", "reviews"));
			for (int i = 0; i < reviewCount; i++) {
				reviews.add(review(index, i));
			}
		}
		return object;
	}

	private EObject review(int productIndex, int index) {
		EObject object = EcoreUtil.create(review);
		object.eSet(feature("Review", "id"), "r-" + productIndex + "-" + index);
		object.eSet(feature("Review", "author"), WORDS[(productIndex + index) % WORDS.length]);
		object.eSet(feature("Review", "text"), "Works as described, number " + index);
		object.eSet(feature("Review", "rating"), index % 5 + 1);
		return object;
	}

	EStructuralFeature feature(String className, String featureName) {
		return models.feature(className, featureName);
	}

	/**
	 * @param nested whether {@code reviews} becomes a document block rather than being left
	 *        to the ID_ONLY convention
	 * @param stored whether the text fields are stored, which is what a projection needs and
	 *        what dominates index size
	 */
	IndexUnitMapping mapping(boolean nested, boolean stored) {
		IndexUnitMapping mapping = ESEARCH.createIndexUnitMapping();
		mapping.setName("perf");
		mapping.setEPackage(models.ePackage());
		DocumentMapping document = ESEARCH.createDocumentMapping();
		document.setEClass(product);
		KeywordFieldMapping id = ESEARCH.createKeywordFieldMapping();
		id.setFeature((EAttribute) feature("Product", "id"));
		id.setStored(true);
		id.setDocValues(true);
		document.getFields().add(id);
		TextFieldMapping description = ESEARCH.createTextFieldMapping();
		description.setFeature((EAttribute) feature("Product", "description"));
		description.setStored(stored);
		document.getFields().add(description);
		if (nested) {
			ReferenceMapping reviews = ESEARCH.createReferenceMapping();
			reviews.setEReference((EReference) feature("Product", "reviews"));
			reviews.setStrategy(ReferenceStrategy.NESTED);
			document.getReferences().add(reviews);
		}
		mapping.getDocuments().add(document);
		return mapping;
	}
}
