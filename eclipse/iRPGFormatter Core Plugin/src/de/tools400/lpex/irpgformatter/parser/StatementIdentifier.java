/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.parser;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;
import de.tools400.lpex.irpgformatter.statement.CollectedStatement;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;
import de.tools400.lpex.irpgformatter.tokenizer.Tokenizer;

public class StatementIdentifier implements RpgleSourceConstants {

    /**
     * Identifies statement type with block context awareness. If inside a block
     * (dcl-ds, dcl-pr, dcl-pi, dcl-enum) and statement is OTHER, it is treated
     * as a sub-statement (DCL_SUBF).
     */
    public static StatementType identifyStatementTypeInContext(CollectedStatement statement) throws RpgleFormatterException {

        String line = statement.getStatement();

        CollectedStatement parent = statement.getParent();

        StatementType parentType;
        StatementType parentEndOfBLockType;
        if (parent == null) {
            parentType = null;
            parentEndOfBLockType = null;
        } else {
            parentType = parent.getType();
            parentEndOfBLockType = parentType.getEndOfBlockType();
        }

        StatementType type = identifyStatementType(line);
        if (parent != null) {

            if (type == parentEndOfBLockType) {
                // Replace parent with super parent on END-* statements
                parent = parent.getParent();
            }

            if (StatementType.OTHER == type) {
                if (parent.getType().hasSubFields()) {
                    type = StatementType.DCL_SUBF;
                }
            }
        }

        return type;
    }

    /**
     * Checks if a DCL-* statement is implicitly closed (no <code>END-*</code>)
     * or <code>END-*</code> in the same statement.
     * <p>
     * <ul>
     * <li>A <code>DCL-DS</code> with a <code>LIKEDS</code> keyword needs no
     * <code>END-DS</code>.</li>
     * <li>A <code>DCL-PI</code> with a <code>END-PI</code> in the same
     * statement has no parameters.</li>
     * <li>A <code>DCL-PR</code> with a <code>END-PR</code> in the same
     * statement has no parameters.</li>
     * </ul>
     * 
     * @throws RpgleFormatterException
     */
    public static boolean isSingleLineStatement(String line) throws RpgleFormatterException {

        StatementType type = identifyStatementType(line);
        if (type == StatementType.DCL_DS && containsToken(line, TokenType.KEYWORD, "LIKEDS")) {
            return true;
        } else if (type == StatementType.DCL_DS && containsToken(line, TokenType.KEYWORD, "LIKEREC")) {
            return true;
        } else if (type == StatementType.DCL_PI && containsToken(line, TokenType.DCL, "END-PI")) {
            return true;
        } else if (type == StatementType.DCL_PR && containsToken(line, TokenType.DCL, "END-PR")) {
            return true;
        }

        return false;
    }

    private static boolean containsToken(String line, TokenType tokenType, String endStatement) throws RpgleFormatterException {

        Tokenizer tokenizer = new Tokenizer();
        IToken[] tokens = tokenizer.tokenize(line);

        for (IToken token : tokens) {
            if (token.getType() == tokenType) {
                if (endStatement.equals(token.getValue().toUpperCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Identifies the type of RPGLE statement by the first statement type, whose
     * pattern matches, in the order of declaration. Note: Sub-statements
     * (DCL_SUBF) are identified based on block context, not by leading
     * whitespace. Use identifyStatementTypeInContext() for context-aware
     * identification.
     */
    public static StatementType identifyStatementType(String line) {

        if (line == null || line.trim().isEmpty()) {
            return StatementType.BLANK;
        }

        for (StatementType type : StatementType.values()) {
            if (type.pattern() != null && type.pattern().matcher(line).find()) {
                return type;
            }
        }

        return StatementType.OTHER;
    }

}
