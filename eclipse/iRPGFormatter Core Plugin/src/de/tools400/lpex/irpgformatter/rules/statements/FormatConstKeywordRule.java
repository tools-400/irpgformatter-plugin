/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.statements;

import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.IStatementRule;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.KeywordToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;

/**
 * Normalizes the value token of a {@code dcl-c} statement so it matches the
 * user's "Use const() keyword" preference.
 * <p>
 * Expected token layout: {@code DCL NAME <value> [...] EOL [COMMENT]}, where
 * {@code <value>} is either a plain value token (e.g. LITERAL, OTHER,
 * SPECIAL_WORD, FUNCTION) or a {@code const(...)} KEYWORD wrapping such a
 * value.
 * <ul>
 * <li>If the preference is enabled and {@code <value>} is not already a
 * {@code const} keyword, wraps it: {@code 100} → {@code const(100)}.</li>
 * <li>If the preference is disabled and {@code <value>} is a {@code const}
 * keyword with a single child, unwraps it: {@code const(100)} → {@code 100}.
 * </li>
 * <li>Otherwise the tokens are returned unchanged.</li>
 * </ul>
 */
public class FormatConstKeywordRule implements IStatementRule {

    private static final int VALUE_INDEX = 2;
    private static final String CONST = "const";

    private final FormatterConfig config;

    public FormatConstKeywordRule(FormatterConfig config) {
        this.config = config;
    }

    @Override
    public IToken[] apply(IToken[] tokens) {

        if (tokens == null || tokens.length <= VALUE_INDEX) {
            return tokens;
        }

        boolean useConst = config.isUseConstKeyword();

        IToken value = tokens[VALUE_INDEX];
        boolean isConstKeyword = isConst(value);

        if (useConst && !isConstKeyword) {
            tokens[VALUE_INDEX] = wrapInConst(value);
        } else if (!useConst && isConstKeyword && value.getNumChildren() == 1) {
            tokens[VALUE_INDEX] = value.getChild(0);
        }

        return tokens;
    }

    private static boolean isConst(IToken token) {
        return token.getType() == TokenType.KEYWORD && CONST.equalsIgnoreCase(token.getValue());
    }

    private static IToken wrapInConst(IToken value) {

        String rawValue = CONST + "(" + value.getRawValue() + ")";
        IToken wrapped = new KeywordToken(CONST, rawValue, value.getOffset());
        wrapped.addChild(value);

        return wrapped;
    }
}
