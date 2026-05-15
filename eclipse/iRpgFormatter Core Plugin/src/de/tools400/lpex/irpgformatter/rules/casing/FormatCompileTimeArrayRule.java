/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.casing;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.rules.IFormattingRule;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;

/**
 * Formats the optional <code>CTDATA</code> keyword on a compile-time array
 * marker line. The <code>**</code> marker, the whitespace around it and any
 * array-name parameter (e.g. <code>arrname</code> or <code>(arrname)</code>)
 * are preserved verbatim. Lines without the <code>CTDATA</code> keyword
 * (e.g. <code>**</code>, <code>**ALTSEQ</code>, <code>**FTRANS</code>) are
 * passed through unchanged.
 * <p>
 * Examples for casing style {@link KeywordCasingStyle#UPPERCASE}:
 * <ul>
 * <li><code>**ctdata arrname</code> &rarr; <code>**CTDATA arrname</code></li>
 * <li><code>** ctdata(arrname)</code> &rarr; <code>** CTDATA(arrname)</code></li>
 * <li><code>**</code> &rarr; <code>**</code></li>
 * <li><code>**ALTSEQ</code> &rarr; <code>**ALTSEQ</code></li>
 * </ul>
 */
public class FormatCompileTimeArrayRule extends AbstractCasingRule implements IFormattingRule, RpgleSourceConstants {

    /**
     * Matches the optional <code>CTDATA</code> keyword right after the
     * <code>**</code> marker. Group 1 captures the keyword in its original
     * casing so the formatted replacement can be spliced back in without
     * touching the surrounding characters.
     */
    private static final Pattern CTDATA_PATTERN = Pattern.compile("^\\s*\\*\\*\\s*(ctdata)\\b", Pattern.CASE_INSENSITIVE);

    public FormatCompileTimeArrayRule() {
        super(null);
    }

    public FormatCompileTimeArrayRule(KeywordCasingStyle keywordCasingStyle) {
        super(null, keywordCasingStyle);
    }

    @Override
    public String format(String line) {

        if (line == null || line.isEmpty()) {
            return line;
        }

        Matcher matcher = CTDATA_PATTERN.matcher(line);
        if (!matcher.find()) {
            return line;
        }

        String formattedKeyword = applyKeywordCasing(matcher.group(1), getKeywordCasingStyle());

        return line.substring(0, matcher.start(1)) + formattedKeyword + line.substring(matcher.end(1));
    }

    /**
     * Applies the configured casing style to the {@code CTDATA} keyword.
     */
    private static String applyKeywordCasing(String keyword, KeywordCasingStyle style) {

        switch (style) {
        case UPPERCASE:
            return keyword.toUpperCase();
        case UPPER_CAMEL:
        case FIRST_UPPER:
            return Character.toUpperCase(keyword.charAt(0)) + keyword.substring(1).toLowerCase();
        case LOWER_CAMEL:
        case LOWERCASE:
        default:
            return keyword.toLowerCase();
        }
    }

    @Override
    protected Map<String, String> getFormattingMap() {
        throw new IllegalAccessError("Not implemented.");
    }
}
