/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

import java.util.ArrayList;
import java.util.List;

import de.tools400.lpex.irpgformatter.parser.StatementType;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.casing.CalculationCasingRule;
import de.tools400.lpex.irpgformatter.rules.statements.ConditionParenthesesRule;
import de.tools400.lpex.irpgformatter.statement.CollectedStatement;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

/**
 * Formats the calculation statements for the {@link RpgleFormatter}.
 * <p>
 * An embedded SQL statement is formatted with the SQL formatter of IBM. The
 * other statements get the outer parentheses of their condition and the
 * casing of their operation code and built-in functions, if the preferences
 * ask for it. Then the lines are indented by the nesting of the blocks. Apart
 * from that the content of a calculation statement is not changed.
 * </p>
 */
final class CalculationStatementFormatter {

    private final FormatterConfig config;
    private final EmbeddedSqlFormatter embeddedSqlFormatter;
    private final ConditionParenthesesRule conditionParenthesesRule;
    private final CalculationCasingRule calculationCasingRule;

    /**
     * Creates a formatter of the calculation statements.
     *
     * @param config - the formatter configuration
     */
    CalculationStatementFormatter(FormatterConfig config) {
        this.config = config;
        this.embeddedSqlFormatter = new EmbeddedSqlFormatter(config);
        this.conditionParenthesesRule = new ConditionParenthesesRule(config);
        this.calculationCasingRule = new CalculationCasingRule(config);
    }

    /**
     * Formats a calculation statement.
     *
     * @param statement - the statement
     * @param type - the type of the statement
     * @param indent - the indent of the statement
     * @param endColumn - the end column
     * @return the formatted lines
     * @throws LineOverflowException if a line exceeds the end column
     */
    List<String> format(CollectedStatement statement, StatementType type, int indent, int endColumn) throws RpgleFormatterException {

        List<String> lines = statement.getOriginalStatements();

        if (type == StatementType.EXEC_SQL && embeddedSqlFormatter.isEnabled()) {
            if (config.isFormatEmbeddedSql()) {
                List<String> formatted = embeddedSqlFormatter.format(statement, indent, endColumn);
                if (formatted != null) {
                    return formatted;
                }
            } else {
                List<String> adjusted = embeddedSqlFormatter.adjustCase(statement);
                if (adjusted != null) {
                    lines = adjusted;
                }
            }
        } else if (ConditionParenthesesRule.isCondition(type)) {
            lines = conditionParenthesesRule.apply(statement);
        }

        // The casing of embedded SQL follows the IBM settings.
        if (type != StatementType.EXEC_SQL) {
            lines = calculationCasingRule.apply(statement, lines);
        }

        return indent(statement, lines, indent, endColumn);
    }

    /**
     * Indents the lines of a calculation statement.
     * <p>
     * The first line gets the indent of the statement. Continued lines keep
     * their indent relative to the first line or are lined up one level deeper,
     * see IBM preference "Preserve relative indentation of continued lines".
     * Lines, whose leading blanks belong to a literal continued with a hyphen,
     * are left unchanged.
     * </p>
     *
     * @param statement - the statement
     * @param lines - the lines of the statement, unchanged or changed by a
     *        rule, such as the casing of SQL or the parentheses of a condition
     * @param indent - the indent of the statement
     * @param endColumn - the end column
     * @throws LineOverflowException if a line exceeds the end column
     */
    private List<String> indent(CollectedStatement statement, List<String> lines, int indent, int endColumn) throws RpgleFormatterException {

        List<String> results = new ArrayList<>();

        int shift = indent - StringUtils.getIndent(lines.get(0)).length();
        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);
            String formatted;
            if (i == 0) {
                formatted = StringUtils.spaces(indent) + line.trim();
            } else if (statement.isLeadingBlanksSignificant(i) || line.trim().isEmpty()) {
                formatted = line;
            } else if (config.isPreserveRelativeIndentOfContinuedLines()) {
                int lineIndent = Math.max(0, StringUtils.getIndent(line).length() + shift);
                formatted = StringUtils.spaces(lineIndent) + line.trim();
            } else {
                formatted = StringUtils.spaces(indent + config.getIndent()) + line.trim();
            }

            // A line, that is longer than the end column already, may keep its
            // length. Compared with the original line, because a rule may have
            // made the line longer.
            if (formatted.length() > endColumn && formatted.length() > statement.getLine(i).length()) {
                throw new LineOverflowException(line.trim(), statement.getStartLineNumber() + i);
            }

            results.add(formatted);
        }

        return results;
    }
}
