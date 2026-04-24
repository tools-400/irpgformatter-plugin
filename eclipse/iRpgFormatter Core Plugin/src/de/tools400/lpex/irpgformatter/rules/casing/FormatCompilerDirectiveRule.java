/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.casing;

import java.util.Map;

import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.rules.IFormattingRule;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;

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

        KeywordCasingStyle keywordCasingStyle = getKeywordCasingStyle();

        String trimmed = directive.trim();
        if (!trimmed.startsWith(COMPILER_DIRECTIVE)) {
            return directive;
        }

        // Find first space to separate directive from parameters
        int spaceIndex = trimmed.indexOf(' ');
        KeywordCasingStyle style = keywordCasingStyle;
        if (spaceIndex > 0) {
            String directivePart = applyDirectiveCasing(trimmed.substring(0, spaceIndex), style);
            String parameterPart = trimmed.substring(spaceIndex);
            return directivePart + parameterPart;
        }
        return applyDirectiveCasing(trimmed, style);
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
                return COMPILER_DIRECTIVE + Character.toUpperCase(directive.charAt(1)) + directive.substring(2).toLowerCase();
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
