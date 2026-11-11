/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.statements;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tools400.lpex.irpgformatter.parser.StatementType;
import de.tools400.lpex.irpgformatter.preferences.ConditionParentheses;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;
import de.tools400.lpex.irpgformatter.statement.CollectedStatement;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

/**
 * Adds or removes the outer parentheses of the condition of an
 * <code>IF</code>, <code>ELSEIF</code>, <code>WHEN</code>, <code>DOW</code> or
 * <code>DOU</code> statement, see preference "Outer parentheses of
 * conditions".
 * <p>
 * Example: {@code if a = 1;} becomes {@code if (a = 1);} and vice versa. All
 * outer parentheses are removed, {@code if ((a = 1));} becomes
 * {@code if a = 1;}. The parentheses of {@code if (a = 1) and (b = 2);} are no
 * outer parentheses.
 * </p>
 * <p>
 * Continued lines, that are indented at least as far as the condition, are
 * shifted with the condition, so that they stay aligned with it.
 * </p>
 * <p>
 * The lines are returned unchanged, if
 * <ul>
 * <li>the preference is "Off",</li>
 * <li>the condition does not start on the line of the operation code,</li>
 * <li>an opening parenthesis ends its line or a closing parenthesis starts its
 * line, so that removing it would leave a line without code,</li>
 * <li>a literal is continued on the next line,</li>
 * <li>a parenthesis follows the operation code without a blank: then it is an
 * operation extender, such as <code>IF(M)</code>, or the statement is
 * ambiguous.</li>
 * </ul>
 * </p>
 */
public class ConditionParenthesesRule implements RpgleSourceConstants {

    private static final Set<StatementType> CONDITION_TYPES = EnumSet.of(StatementType.IF, StatementType.ELSEIF, StatementType.WHEN,
        StatementType.DOW, StatementType.DOU);

    /** Operation code, optional operation extender and the blanks in front of the condition */
    private static final Pattern OPERATION_CODE_PATTERN = Pattern.compile("^\\s*(if|elseif|when|dow|dou)(\\([a-z]+\\))?\\s+",
        Pattern.CASE_INSENSITIVE);

    private static final char OPENING_PARENTHESIS = '(';
    private static final char CLOSING_PARENTHESIS = ')';
    private static final char QUOTE = '\'';
    private static final char SEMICOLON_CHAR = ';';

    private final FormatterConfig config;

    public ConditionParenthesesRule(FormatterConfig config) {
        this.config = config;
    }

    /**
     * Returns <code>true</code>, if the rule applies to statements of the
     * given type.
     */
    public static boolean isCondition(StatementType type) {
        return CONDITION_TYPES.contains(type);
    }

    /**
     * Adds or removes the outer parentheses of the condition of the
     * statement.
     *
     * @param statement - an <code>IF</code>, <code>ELSEIF</code>,
     *        <code>WHEN</code>, <code>DOW</code> or <code>DOU</code> statement
     * @return the changed lines or the original lines of the statement
     */
    public List<String> apply(CollectedStatement statement) {

        List<String> lines = statement.getOriginalStatements();

        ConditionParentheses style = config.getConditionParentheses();
        if (style == ConditionParentheses.OFF || statement.hasContinuedLiteral()) {
            return lines;
        }

        Matcher matcher = OPERATION_CODE_PATTERN.matcher(lines.get(0));
        if (!matcher.lookingAt()) {
            return lines;
        }

        Code code = Code.scan(lines, matcher.end());

        int start = code.skipBlanks(0, 1);
        int end = code.skipBlanks(code.findTerminatingSemicolon() - 1, -1);
        if (start < 0 || end < start || code.getLine(start) != 0) {
            return lines;
        }

        // Skip all outer parentheses
        int innerStart = start;
        int innerEnd = end;
        while (code.isParenthesis(innerStart, OPENING_PARENTHESIS) && code.findClosingParenthesis(innerStart) == innerEnd) {
            innerStart = code.skipBlanks(innerStart + 1, 1);
            innerEnd = code.skipBlanks(innerEnd - 1, -1);
            if (innerStart < 0 || innerEnd < innerStart) {
                return lines;
            }
        }

        List<String> result = new ArrayList<>(lines);
        int oldColumn;
        int shift;

        if (style == ConditionParentheses.WITH_PARENTHESES) {
            if (innerStart != start) {
                return lines;
            }
            replace(result, code.getLine(end), code.getColumn(end) + 1, code.getColumn(end) + 1, String.valueOf(CLOSING_PARENTHESIS));
            replace(result, 0, code.getColumn(start), code.getColumn(start), String.valueOf(OPENING_PARENTHESIS));
            oldColumn = code.getColumn(start);
            shift = 1;
        } else {
            if (innerStart == start || code.getLine(innerStart) != 0 || code.getLine(innerEnd) != code.getLine(end)) {
                return lines;
            }
            replace(result, code.getLine(end), code.getColumn(innerEnd) + 1, code.getColumn(end) + 1, "");
            replace(result, 0, code.getColumn(start), code.getColumn(innerStart), "");
            oldColumn = code.getColumn(innerStart);
            shift = code.getColumn(start) - code.getColumn(innerStart);
        }

        // Keep the continued lines aligned with the condition
        for (int i = 1; i < result.size(); i++) {
            String line = result.get(i);
            int indent = StringUtils.getIndent(line).length();
            if (!line.trim().isEmpty() && indent >= oldColumn) {
                result.set(i, StringUtils.spaces(indent + shift) + line.substring(indent));
            }
        }

        return result;
    }

    private void replace(List<String> lines, int lineIndex, int fromColumn, int toColumn, String replacement) {
        String line = lines.get(lineIndex);
        lines.set(lineIndex, line.substring(0, fromColumn) + replacement + line.substring(toColumn));
    }

    /**
     * The characters of the condition and of the semicolon behind it without
     * comments, with their positions in the lines.
     */
    private static class Code {

        private final StringBuilder chars = new StringBuilder();
        private final List<int[]> positions = new ArrayList<>();
        private final List<Boolean> isLiteral = new ArrayList<>();

        /**
         * Collects the code from the given column of the first line on. A
         * literal cannot span lines, because statements with continued
         * literals are skipped.
         */
        static Code scan(List<String> lines, int firstColumn) {

            Code code = new Code();
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                boolean insideLiteral = false;
                for (int column = lineIndex == 0 ? firstColumn : 0; column < line.length(); column++) {
                    char ch = line.charAt(column);
                    if (!insideLiteral && line.startsWith(COMMENT, column)) {
                        break;
                    }
                    boolean isQuote = ch == QUOTE;
                    if (isQuote) {
                        insideLiteral = !insideLiteral;
                    }
                    code.chars.append(ch);
                    code.positions.add(new int[] { lineIndex, column });
                    code.isLiteral.add(insideLiteral || isQuote);
                }
            }

            return code;
        }

        int getLine(int index) {
            return positions.get(index)[0];
        }

        int getColumn(int index) {
            return positions.get(index)[1];
        }

        boolean isParenthesis(int index, char parenthesis) {
            return chars.charAt(index) == parenthesis && !isLiteral.get(index);
        }

        /**
         * Returns the index of the semicolon, that ends the statement, or -1.
         */
        int findTerminatingSemicolon() {
            for (int i = chars.length() - 1; i >= 0; i--) {
                if (chars.charAt(i) == SEMICOLON_CHAR && !isLiteral.get(i)) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * Returns the index of the closing parenthesis, that matches the
         * opening parenthesis at the given index, or -1.
         */
        int findClosingParenthesis(int opening) {
            int depth = 0;
            for (int i = opening; i < chars.length(); i++) {
                if (isParenthesis(i, OPENING_PARENTHESIS)) {
                    depth++;
                } else if (isParenthesis(i, CLOSING_PARENTHESIS)) {
                    depth--;
                    if (depth == 0) {
                        return i;
                    }
                }
            }
            return -1;
        }

        /**
         * Returns the index of the first character, that is not a blank,
         * starting at the given index in the given direction, or -1.
         */
        int skipBlanks(int index, int direction) {
            for (int i = index; i >= 0 && i < chars.length(); i += direction) {
                if (!Character.isWhitespace(chars.charAt(i))) {
                    return i;
                }
            }
            return -1;
        }
    }
}
