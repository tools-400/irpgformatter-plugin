/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.statements;

import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.NameToken;
import de.tools400.lpex.irpgformatter.tokenizer.SpecialWordToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;

/**
 * Adjusts the NAME token of a DCL-PI statement that belongs to a DCL-PROC
 * parent.
 * <ul>
 * <li>When {@code Preferences.isReplacePiName()} is {@code true}, replaces
 * the NAME token with the special word {@code *N}.</li>
 * <li>When {@code Preferences.isReplacePiName()} is {@code false}, replaces
 * the special word {@code *N} with the procedure name supplied to the
 * constructor.</li>
 * </ul>
 * If {@code procedureName} is {@code null} (no parent DCL-PROC), the rule is
 * a no-op for the restore direction.
 */
public class ReplacePiNameRule implements IStatementRule {

    private static final String STAR_N = "*N";

    private final String procedureName;
    private final Boolean replacePiName;

    public ReplacePiNameRule(String procedureName) {
        this(procedureName, null);
    }

    public ReplacePiNameRule(String procedureName, boolean replacePiName) {
        this(procedureName, Boolean.valueOf(replacePiName));
    }

    private ReplacePiNameRule(String procedureName, Boolean replacePiName) {
        this.procedureName = procedureName;
        this.replacePiName = replacePiName;
    }

    @Override
    public IToken[] apply(IToken[] tokens) {

        if (tokens == null || tokens.length < 2) {
            return tokens;
        }
        if (procedureName == null) {
            // No DCL-PROC context -> nothing to do in either direction.
            return tokens;
        }

        boolean replace;
        if (replacePiName != null) {
            replace = replacePiName.booleanValue();
        } else {
            replace = Preferences.getInstance().isReplacePiName();
        }

        IToken nameToken = tokens[1];
        if (replace) {
            if (nameToken.getType() == TokenType.NAME) {
                tokens[1] = new SpecialWordToken(STAR_N, STAR_N, nameToken.getOffset());
            }
        } else {
            if (nameToken.getType() == TokenType.SPECIAL_WORD && STAR_N.equalsIgnoreCase(nameToken.getValue())) {
                tokens[1] = new NameToken(procedureName, procedureName, nameToken.getOffset());
            }
        }

        return tokens;
    }
}
