/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules;

import de.tools400.lpex.irpgformatter.tokenizer.IToken;

/**
 * Token-array based formatting rule for a single RPGLE statement.
 * <p>
 * Implementations receive the tokens of one statement and return a
 * (possibly modified) token array. Used for transformations that operate
 * on the token sequence as a whole (e.g. inserting, removing, or rewriting
 * positional tokens within a statement).
 * </p>
 */
public interface IStatementRule {

    IToken[] apply(IToken[] tokens);
}
