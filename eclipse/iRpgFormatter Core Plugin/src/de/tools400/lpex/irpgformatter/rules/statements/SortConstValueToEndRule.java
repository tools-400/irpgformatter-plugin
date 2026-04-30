/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.statements;

import java.util.LinkedList;
import java.util.List;

import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.IStatementRule;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;

/**
 * Moves any {@code CONST} or {@code VALUE} keyword token of a sub-field
 * statement (parameter / subfield) to the end of the keyword list, just
 * before the trailing {@code EOL}/{@code COMMENT} tokens.
 * <p>
 * Example: {@code myParam const char(10);} becomes
 * {@code myParam char(10) const;}.
 * <ul>
 * <li>If the preference {@code FormatterConfig#isSortConstValueToEnd()} is
 * disabled, the tokens are returned unchanged.</li>
 * <li>If no {@code CONST} or {@code VALUE} keyword is present, the tokens
 * are returned unchanged.</li>
 * <li>{@code EOL} and {@code COMMENT} tokens always remain at the end.</li>
 * </ul>
 */
public class SortConstValueToEndRule implements IStatementRule {

    private final FormatterConfig config;

    public SortConstValueToEndRule(FormatterConfig config) {
        this.config = config;
    }

    @Override
    public IToken[] apply(IToken[] tokens) {

        if (tokens == null || tokens.length == 0) {
            return tokens;
        }

        if (!config.isSortConstValueToEnd()) {
            return tokens;
        }

        List<IToken> constValueTokens = new LinkedList<>();
        List<IToken> otherTokens = new LinkedList<>();
        List<IToken> trailingTokens = new LinkedList<>();

        for (int i = 0; i < tokens.length; i++) {
            IToken token = tokens[i];
            if (token.getType() == TokenType.EOL || token.getType() == TokenType.COMMENT) {
                trailingTokens.add(token);
            } else if (token.getType() == TokenType.KEYWORD && isConstOrValue(token)) {
                constValueTokens.add(token);
            } else {
                otherTokens.add(token);
            }
        }

        if (constValueTokens.isEmpty()) {
            return tokens;
        }

        List<IToken> result = new LinkedList<>();
        result.addAll(otherTokens);
        result.addAll(constValueTokens);
        result.addAll(trailingTokens);
        return result.toArray(new IToken[0]);
    }

    private static boolean isConstOrValue(IToken token) {
        String upper = token.getValue().toUpperCase();
        return "CONST".equals(upper) || "VALUE".equals(upper);
    }
}
