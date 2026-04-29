/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.statements;

import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.rules.IStatementRule;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.NameToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;

/**
 * Adjusts the NAME token of an END-PROC statement.
 * <ul>
 * <li>When {@code Preferences.isRemoveEndProcName()} is {@code true},
 * removes the NAME token (if present).</li>
 * <li>When {@code Preferences.isRemoveEndProcName()} is {@code false},
 * ensures the NAME token matches the procedure name supplied to the
 * constructor: missing names are inserted, divergent names are
 * overwritten.</li>
 * </ul>
 * If {@code procedureName} is {@code null}, the restore direction becomes a
 * no-op.
 */
public class FormatEndProcNameRule implements IStatementRule {

    private final String procedureName;
    private final Boolean removeEndProcName;

    public FormatEndProcNameRule(String procedureName) {
        this(procedureName, null);
    }

    public FormatEndProcNameRule(String procedureName, boolean removeEndProcName) {
        this(procedureName, Boolean.valueOf(removeEndProcName));
    }

    private FormatEndProcNameRule(String procedureName, Boolean removeEndProcName) {
        this.procedureName = procedureName;
        this.removeEndProcName = removeEndProcName;
    }

    @Override
    public IToken[] apply(IToken[] tokens) {

        if (tokens == null || tokens.length == 0) {
            return tokens;
        }

        boolean remove;
        if (removeEndProcName != null) {
            remove = removeEndProcName.booleanValue();
        } else {
            remove = Preferences.getInstance().isRemoveEndProcName();
        }

        if (remove) {
            return removeNameToken(tokens);
        } else {
            return ensureNameToken(tokens);
        }
    }

    private IToken[] removeNameToken(IToken[] tokens) {

        if (tokens.length >= 2 && tokens[1].getType() == TokenType.NAME) {
            IToken[] result = new IToken[tokens.length - 1];
            result[0] = tokens[0];
            System.arraycopy(tokens, 2, result, 1, tokens.length - 2);
            return result;
        }
        return tokens;
    }

    private IToken[] ensureNameToken(IToken[] tokens) {

        if (procedureName == null) {
            return tokens;
        }

        IToken endProcToken = tokens[0];
        int offset = endProcToken.getOffset() + endProcToken.getRawLength();

        if (tokens.length >= 2 && tokens[1].getType() == TokenType.NAME) {
            // Overwrite an existing (possibly divergent) name with the
            // canonical procedure name.
            tokens[1] = new NameToken(procedureName, procedureName, tokens[1].getOffset());
            return tokens;
        }

        // Insert the procedure name as the second token.
        IToken[] result = new IToken[tokens.length + 1];
        result[0] = tokens[0];
        result[1] = new NameToken(procedureName, procedureName, offset);
        System.arraycopy(tokens, 1, result, 2, tokens.length - 1);
        return result;
    }
}
