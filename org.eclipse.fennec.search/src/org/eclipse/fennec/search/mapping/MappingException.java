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

/**
 * A mapping that cannot be honoured — either because it is inconsistent with the model, or
 * because it asks for something this backend does not implement yet.
 * <p>
 * Deliberately raised while building or applying the mapping rather than swallowed: a
 * document that is silently indexed differently from what the mapping says is worse than a
 * failure, because nothing downstream can detect it.
 *
 * @author Data In Motion Consulting
 */
public class MappingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MappingException(String message) {
		super(message);
	}

	public MappingException(String message, Throwable cause) {
		super(message, cause);
	}
}
