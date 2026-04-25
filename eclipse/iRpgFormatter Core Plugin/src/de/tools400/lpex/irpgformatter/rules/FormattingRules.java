/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.casing.FormatCompilerDirectiveRule;
import de.tools400.lpex.irpgformatter.rules.casing.FormatDataTypeRule;
import de.tools400.lpex.irpgformatter.rules.casing.FormatKeywordRule;
import de.tools400.lpex.irpgformatter.rules.casing.FormatSpecialWordsRule;

/**
 * Hybrid utility class containing RPGLE formatting rules.
 * <p>
 * Config-dependent methods are instance methods; pure utilities remain static.
 * </p>
 */
public class FormattingRules implements RpgleSourceConstants {

    public static final String SPACE = " ";

    private final FormatterConfig config;

    public FormattingRules(FormatterConfig config) {
        this.config = config;
    }

    // ---------------------------------------------------------------
    // Instance methods (config-dependent)
    // ---------------------------------------------------------------

    /**
     * Creates an indent string for the given level.
     */
    public String createIndent(int level) {

        int indentSize = config.getIndent();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level * indentSize; i++) {
            sb.append(" ");
        }

        return sb.toString();
    }

    /**
     * Formats known keywords and parameters to the configured casing style
     * while preserving identifier case.
     */
    public String formatKeyword(String text) {

        FormatKeywordRule formatKeywordRule = new FormatKeywordRule(config.getKeywords(), config.getKeywordCasingStyle());

        String formatted = applyRules(text, formatKeywordRule);

        return formatted;
    }

    /**
     * Formats special words to the configured casing style while preserving
     * identifier case.
     */
    public String formatSpecialWord(String text) {

        FormatSpecialWordsRule formatSpecialWordRule = new FormatSpecialWordsRule(config.getSpecialWords(), config.getKeywordCasingStyle());

        String formatted = applyRules(text, formatSpecialWordRule);

        return formatted;
    }

    /**
     * Formats the spacing of a parameter.
     */
    // public String formatParameter(String text) {
    //
    // FormatParameterRule formatParameterRule = new FormatParameterRule();
    //
    // String formatted = applyRules(text, formatParameterRule);
    //
    // return formatted;
    // }

    /**
     * Formats data-types to the configured casing style while preserving
     * identifier case.
     */
    public String formatDataType(String text) {

        FormatDataTypeRule formatDataTypeRule = new FormatDataTypeRule(config.getDataTypes(), config.getKeywordCasingStyle());

        String formatted = applyRules(text, formatDataTypeRule);

        return formatted;
    }

    /**
     * Formats compiler directive according to casing style (keeps parameters
     * as-is).
     */
    public String formatCompilerDirective(String value) {

        String formatted = applyRules(value, new FormatCompilerDirectiveRule(config.getKeywordCasingStyle()));

        return formatted;
    }

    // ---------------------------------------------------------------
    // Static methods (config-independent)
    // ---------------------------------------------------------------

    /**
     * Removes the const() wrapper from dcl-c values. Converts "const(1)" to "1"
     */
    public static String removeConstWrapper(String value) {

        Pattern pattern = Pattern.compile("const\\s*\\((.+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value.trim());
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }

        return value;
    }

    /**
     * Applies formatting rules to a given value. Literals inside the value are
     * ignored when applying the rules.
     *
     * @param value - value to format
     * @param rules - formatting rules to apply
     * @return the formatted value
     */
    public static String applyFormattingRule(String value, IFormattingRule... rules) {

        String trimmed = value.trim();
        if (!trimmed.contains(SINGLE_QUOTE)) {
            return applyRules(trimmed, rules);
        }

        StringBuilder formatted = new StringBuilder();
        StringBuilder nonLiteralPart = new StringBuilder();

        String currentChar;
        String nextChar;
        boolean insideLiteral = false;

        for (int i = 0; i < trimmed.length(); i++) {
            currentChar = trimmed.substring(i, i + 1);
            if (i < trimmed.length() - 1) {
                nextChar = trimmed.substring(i + 1, i + 2);
            } else {
                nextChar = null;
            }

            if (SINGLE_QUOTE.equals(currentChar)) {
                if (insideLiteral && SINGLE_QUOTE.equals(nextChar)) {
                    // Escaped quote inside literal (e.g., 'it''s') - preserve
                    // both quotes
                    formatted.append(currentChar);
                    i++;
                } else {
                    if (!insideLiteral) {
                        formatted.append(applyRules(nonLiteralPart.toString(), rules));
                        nonLiteralPart.setLength(0);
                    }
                    insideLiteral = !insideLiteral;
                }
            }

            if (!insideLiteral) {
                nonLiteralPart.append(currentChar);
            } else {
                formatted.append(currentChar);
            }
        }

        if (nonLiteralPart.length() > 0) {
            formatted.append(applyRules(nonLiteralPart.toString(), rules));
        }

        return formatted.toString();
    }

    /**
     * Applies a list of formatting rules to a given value.
     *
     * @param value - value to format
     * @param rules - formatting rules to apply
     * @return the formatted value
     */
    private static String applyRules(String value, IFormattingRule... rules) {

        String formatted = value;

        for (IFormattingRule iFormattingRule : rules) {
            formatted = iFormattingRule.format(formatted);
        }

        return formatted.toString();
    }
}
