/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.casing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;
import de.tools400.lpex.irpgformatter.statement.CollectedStatement;

/**
 * Formats the operation code and the built-in functions of a calculation
 * statement in the keyword casing style, see preference "Apply keyword casing
 * to operation codes and built-in functions".
 * <p>
 * Example: {@code IF %EOF(file);} becomes {@code if %eof(file);} with "All
 * Lowercase". The operation code is the first word of the statement. Literals,
 * comments, operation extenders and words, that are not in the tables of
 * operation codes and built-in functions, stay unchanged.
 * </p>
 * <p>
 * RPG ignores the casing outside of literals. To make sure, that no literal is
 * changed, every changed line must equal its original line except for the
 * casing outside of literals. Otherwise the lines are returned unchanged, as
 * well as the lines of a statement with a literal, that is continued on the
 * next line.
 * </p>
 */
public class CalculationCasingRule implements RpgleSourceConstants {

    /** First word of the statement, followed by an extender, a blank or the semicolon */
    private static final Pattern OPERATION_CODE_PATTERN = Pattern.compile("^\\s*([A-Za-z][A-Za-z0-9]*(?:-[A-Za-z]+)?)(?=[\\s(;]|$)");

    private static final Pattern BUILT_IN_FUNCTION_PATTERN = Pattern.compile("%[A-Za-z][A-Za-z0-9]*");

    private static final Pattern LITERAL_PATTERN = Pattern.compile("'(?:[^']|'')*'");

    private static final char QUOTE = '\'';
    private static final char PERCENT = '%';

    private final FormatterConfig config;

    public CalculationCasingRule(FormatterConfig config) {
        this.config = config;
    }

    /**
     * Formats the operation code and the built-in functions of the statement.
     *
     * @param statement - the calculation statement
     * @param lines - the lines of the statement, unchanged or changed by
     *        another rule
     * @return the changed lines or the given lines
     */
    public List<String> apply(CollectedStatement statement, List<String> lines) {

        if (!config.isFormatOperationCodesAndBuiltInFunctions() || statement.hasContinuedLiteral()) {
            return lines;
        }

        List<String> result = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String formatted = formatLine(line, i == 0);
            if (!isSameExceptCasing(line, formatted)) {
                return lines;
            }
            result.add(formatted);
        }

        return result;
    }

    private String formatLine(String line, boolean isFirstLine) {

        StringBuilder buffer = new StringBuilder(line);

        if (isFirstLine) {
            Matcher matcher = OPERATION_CODE_PATTERN.matcher(line);
            if (matcher.lookingAt()) {
                replace(buffer, matcher.start(1), matcher.group(1), config.getOperationCodes());
            }
        }

        boolean insideLiteral = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == QUOTE) {
                insideLiteral = !insideLiteral;
            } else if (!insideLiteral) {
                if (line.startsWith(COMMENT, i)) {
                    break;
                }
                if (ch == PERCENT) {
                    Matcher matcher = BUILT_IN_FUNCTION_PATTERN.matcher(line);
                    if (matcher.find(i) && matcher.start() == i) {
                        replace(buffer, i, matcher.group(), config.getBuiltInFunctions());
                        i = matcher.end() - 1;
                    }
                }
            }
        }

        return buffer.toString();
    }

    /**
     * Replaces the word at the given position with its canonical spelling in
     * the keyword casing style, if it is in the table.
     */
    private void replace(StringBuilder buffer, int start, String word, Map<String, String> canonicalSpellings) {

        String canonical = canonicalSpellings.get(word.toUpperCase());
        if (canonical == null) {
            return;
        }

        String formatted = KeywordCasingUtils.apply(canonical, config.getKeywordCasingStyle());
        if (formatted.length() == word.length()) {
            buffer.replace(start, start + word.length(), formatted);
        }
    }

    /**
     * Returns <code>true</code>, if the formatted line differs from the
     * original line only in the casing outside of literals.
     */
    static boolean isSameExceptCasing(String original, String formatted) {

        if (!original.equalsIgnoreCase(formatted)) {
            return false;
        }

        Matcher originalLiterals = LITERAL_PATTERN.matcher(original);
        Matcher formattedLiterals = LITERAL_PATTERN.matcher(formatted);
        while (originalLiterals.find()) {
            if (!formattedLiterals.find() || !originalLiterals.group().equals(formattedLiterals.group())) {
                return false;
            }
        }

        return !formattedLiterals.find();
    }
}
