/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.casing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;
import de.tools400.lpex.irpgformatter.statement.CollectedStatement;

/**
 * Formats the operation code, the built-in functions and the special words of
 * a calculation statement in the keyword casing style, see preferences "Apply
 * keyword casing to operation codes and built-in functions" and "Apply keyword
 * casing to special words of the calculations".
 * <p>
 * Example: {@code IF %EOF(file) AND *IN03 = *ON;} becomes
 * {@code if %eof(file) AND *IN03 = *on;} with "All Lowercase". The operation
 * code is the first word of the statement. Literals, comments, operation
 * extenders and words, that are not in the tables of operation codes, built-in
 * functions and special words, stay unchanged.
 * </p>
 * <p>
 * An asterisk after an operand, such as {@code price*days}, is a
 * multiplication. The name after it stays unchanged, even if it is a special
 * word, such as {@code *DAYS}.
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

    /** Operation extender, such as (e) or (he), directly after the operation code */
    private static final Pattern OPERATION_EXTENDER_PATTERN = Pattern.compile("\\(\\s*[A-Za-z]+\\s*\\)");

    private static final Pattern BUILT_IN_FUNCTION_PATTERN = Pattern.compile("%[A-Za-z][A-Za-z0-9]*");

    private static final Pattern SPECIAL_WORD_PATTERN = Pattern.compile("\\*[A-Za-z][A-Za-z0-9]*");

    /** Name, qualified name or number */
    private static final Pattern OPERAND_PATTERN = Pattern.compile("[A-Za-z0-9_#@$.]+");

    private static final Pattern LITERAL_PATTERN = Pattern.compile("'(?:[^']|'')*'");

    /** Operators, that are words, such as in {@code IF a AND *IN03;} */
    private static final Set<String> WORD_OPERATORS = new HashSet<>(Arrays.asList("AND", "OR", "NOT", "TO", "DOWNTO", "BY"));

    private static final char QUOTE = '\'';
    private static final String EXPONENTIATION = "**";

    private final FormatterConfig config;

    public CalculationCasingRule(FormatterConfig config) {
        this.config = config;
    }

    /**
     * Formats the operation code, the built-in functions and the special words
     * of the statement.
     *
     * @param statement - the calculation statement
     * @param lines - the lines of the statement, unchanged or changed by
     *        another rule
     * @return the changed lines or the given lines
     */
    public List<String> apply(CollectedStatement statement, List<String> lines) {

        if (!config.isFormatOperationCodesAndBuiltInFunctions() && !config.isFormatSpecialWordsOfCalculations()
            || statement.hasContinuedLiteral()) {
            return lines;
        }

        Scanner scanner = new Scanner();
        List<String> result = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String formatted = scanner.formatLine(line, i == 0);
            if (!isSameExceptCasing(line, formatted)) {
                return lines;
            }
            result.add(formatted);
        }

        return result;
    }

    /**
     * Scans the lines of a statement. Remembers across the lines, whether the
     * last token was an operand, because then an asterisk is a multiplication.
     */
    private class Scanner {

        private boolean isAfterOperand;

        private String formatLine(String line, boolean isFirstLine) {

            StringBuilder buffer = new StringBuilder(line);

            int i = 0;
            if (isFirstLine) {
                i = skipOperationCode(line, buffer);
            }

            while (i < line.length()) {
                char ch = line.charAt(i);
                if (Character.isWhitespace(ch)) {
                    i++;
                } else if (line.startsWith(COMMENT, i)) {
                    break;
                } else if (ch == QUOTE) {
                    i = skipLiteral(line, i);
                    isAfterOperand = true;
                } else if (matches(BUILT_IN_FUNCTION_PATTERN, line, i)) {
                    i = format(line, i, BUILT_IN_FUNCTION_PATTERN, buffer, config.isFormatOperationCodesAndBuiltInFunctions(),
                        config.getBuiltInFunctions());
                    isAfterOperand = true;
                } else if (!isAfterOperand && matches(SPECIAL_WORD_PATTERN, line, i)) {
                    i = format(line, i, SPECIAL_WORD_PATTERN, buffer, config.isFormatSpecialWordsOfCalculations(), config.getSpecialWords());
                    isAfterOperand = false;
                } else if (line.startsWith(ASTERISK, i)) {
                    i += line.startsWith(EXPONENTIATION, i) ? EXPONENTIATION.length() : 1;
                    isAfterOperand = false;
                } else if (matches(OPERAND_PATTERN, line, i)) {
                    Matcher operand = matchAt(OPERAND_PATTERN, line, i);
                    isAfterOperand = !WORD_OPERATORS.contains(operand.group().toUpperCase());
                    i = operand.end();
                } else {
                    isAfterOperand = line.startsWith(CLOSE_BRACKET, i);
                    i++;
                }
            }

            return buffer.toString();
        }

        /**
         * Formats the operation code at the start of the line and returns the
         * position after it and its extender. Returns 0, if the line does not
         * start with an operation code, such as an assignment.
         */
        private int skipOperationCode(String line, StringBuilder buffer) {

            Matcher matcher = OPERATION_CODE_PATTERN.matcher(line);
            if (!matcher.lookingAt() || !config.getOperationCodes().containsKey(matcher.group(1).toUpperCase())) {
                return 0;
            }

            if (config.isFormatOperationCodesAndBuiltInFunctions()) {
                replace(buffer, matcher.start(1), matcher.group(1), config.getOperationCodes());
            }

            // Operands follow the operation code, such as in RETURN *ON;
            isAfterOperand = false;

            Matcher extender = matchAt(OPERATION_EXTENDER_PATTERN, line, matcher.end(1));
            if (extender != null) {
                return extender.end();
            }

            return matcher.end(1);
        }

        /**
         * Formats the word at the given position, if the option is enabled, and
         * returns the position after it.
         */
        private int format(String line, int start, Pattern pattern, StringBuilder buffer, boolean isEnabled,
            Map<String, String> canonicalSpellings) {

            Matcher matcher = matchAt(pattern, line, start);
            if (isEnabled) {
                replace(buffer, start, matcher.group(), canonicalSpellings);
            }

            return matcher.end();
        }
    }

    private static boolean matches(Pattern pattern, String line, int start) {
        return matchAt(pattern, line, start) != null;
    }

    /**
     * Returns the match of the pattern at the given position or
     * <code>null</code>, if the text at the position does not match.
     */
    private static Matcher matchAt(Pattern pattern, String line, int start) {
        Matcher matcher = pattern.matcher(line);
        matcher.region(start, line.length());
        return matcher.lookingAt() ? matcher : null;
    }

    /**
     * Returns the position after the literal, that starts at the given
     * position. Two quotes are a quote within the literal.
     */
    private static int skipLiteral(String line, int start) {

        int i = start + 1;
        while (i < line.length()) {
            if (line.charAt(i) != QUOTE) {
                i++;
            } else if (i + 1 < line.length() && line.charAt(i + 1) == QUOTE) {
                i += 2;
            } else {
                return i + 1;
            }
        }

        return i;
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
