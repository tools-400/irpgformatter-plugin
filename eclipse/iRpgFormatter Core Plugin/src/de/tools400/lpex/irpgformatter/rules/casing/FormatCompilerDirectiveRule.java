/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.casing;

import java.util.Map;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.rules.IFormattingRule;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;
import de.tools400.lpex.irpgformatter.tokenizer.Tokenizer;

/**
 * Formats compiler directives, such as
 * <ul>
 * <code>/copy library/file.member</code>
 * <code>/inlcude library/file.member</code>
 * </ul>
 */
public class FormatCompilerDirectiveRule extends AbstractCasingRule implements IFormattingRule, RpgleSourceConstants {

    public FormatCompilerDirectiveRule() {
        super(null);
    }

    public FormatCompilerDirectiveRule(KeywordCasingStyle keywordCasingStyle) {
        super(null, keywordCasingStyle);
    }

    @Override
    public String format(String directive) {

        if (directive == null || directive.isEmpty()) {
            return directive;
        }

        String trimmed = directive.trim();
        if (!trimmed.startsWith(COMPILER_DIRECTIVE)) {
            return directive;
        }

        trimmed = trimmed.substring(1);

        KeywordCasingStyle style = getKeywordCasingStyle();

        Tokenizer tokenizer = new Tokenizer();
        try {
            IToken[] tokens = tokenizer.tokenize(trimmed);
            StringBuilder buffer = new StringBuilder();
            for (IToken token : tokens) {
                TokenType type = token.getType();
                if (buffer.length() == 0 || type == TokenType.FUNCTION || type == TokenType.KEYWORD) {
                    buffer.append(applyDirectiveCasing(token.getValue(), style));
                    if (token.haveChildren()) {
                        IToken[] children = token.getChildren();
                        buffer.append(OPEN_BRACKET);
                        for (int i = 0; i < children.length; i++) {
                            buffer.append(children[i].getValue());
                            if (i < children.length - 1) {
                                buffer.append(SPACE);
                            }
                        }
                        buffer.append(CLOSE_BRACKET);
                    }
                } else {
                    buffer.append(token.getValue());
                }
                buffer.append(SPACE);
            }
            return COMPILER_DIRECTIVE + buffer.toString().trim();
        } catch (RpgleFormatterException e) {
            IRpgleFormatterPlugin.logError("Could not tokenize compiler directive: " + directive, e);
            return directive;
        }
    }

    /**
     * Applies casing style to a compiler directive (e.g., /include, /copy).
     */
    private static String applyDirectiveCasing(String directive, KeywordCasingStyle style) {

        switch (style) {
        case UPPERCASE:
            return directive.toUpperCase();
        case UPPER_CAMEL:
        case FIRST_UPPER:
            // Capitalize first letter after /
            if (directive.length() > 1) {
                return Character.toUpperCase(directive.charAt(0)) + directive.substring(1).toLowerCase();
            }
            return directive;
        case LOWER_CAMEL:
        case LOWERCASE:
        default:
            return directive.toLowerCase();
        }
    }

    @Override
    protected Map<String, String> getFormattingMap() {
        throw new IllegalAccessError("Not implemented.");
    }
}
