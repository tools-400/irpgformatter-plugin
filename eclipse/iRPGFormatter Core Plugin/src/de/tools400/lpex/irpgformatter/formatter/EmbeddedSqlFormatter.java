/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.SqlCasingStyle;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;
import de.tools400.lpex.irpgformatter.statement.CollectedStatement;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

/**
 * Formats embedded SQL statements like RDi with the SQL formatter of IBM i
 * Access Client Solutions.
 * <p>
 * If the IBM preference "Formatting" of the ILE RPG SQL formatting is enabled,
 * <code>EXEC SQL</code> is put on a line of its own and the SQL statement is
 * formatted one SQL indent deeper. Otherwise only the casing is changed, if
 * the IBM preference "Automatic uppercasing" is enabled.
 * </p>
 * <p>
 * The methods return <code>null</code> and the caller only indents the
 * statement, if the SQL formatter is not available or reports a syntax error,
 * if it changed more than the blanks and the casing outside of the literals, if
 * a literal is continued on the next line or if a comment follows the
 * semicolon.
 * </p>
 */
public class EmbeddedSqlFormatter implements RpgleSourceConstants {

    private static final Pattern EXEC_SQL_PATTERN = Pattern.compile("^(\\s*)(exec)(\\s+)(sql)(\\s*)(.*)$",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern LITERAL_PATTERN = Pattern.compile("'(?:[^']|'')*'|\"(?:[^\"]|\"\")*\"");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private static final String NEW_LINE = "\n";

    private final FormatterConfig config;
    private boolean isAvailable;

    public EmbeddedSqlFormatter(FormatterConfig config) {
        this.config = config;
        this.isAvailable = true;
    }

    /**
     * Returns <code>true</code>, if embedded SQL statements are formatted or
     * their casing is changed.
     */
    public boolean isEnabled() {

        if (!isAvailable) {
            return false;
        }

        return config.isFormatEmbeddedSql() || config.getSqlKeywordCasingStyle() != SqlCasingStyle.IGNORE
            || config.getSqlIdentifierCasingStyle() != SqlCasingStyle.IGNORE;
    }

    /**
     * Formats an embedded SQL statement.
     *
     * @param statement - the <code>EXEC SQL</code> statement
     * @param indent - the indent of the statement
     * @param endColumn - the end column
     * @return the formatted lines or <code>null</code>, if the statement
     *         cannot be formatted
     */
    public List<String> format(CollectedStatement statement, int indent, int endColumn) {

        Matcher matcher = matchExecSql(statement);
        if (matcher == null) {
            return null;
        }

        int sqlIndent = indent + config.getSqlIndent();
        String formatted = formatSql(getSql(matcher), sqlIndent, endColumn, false);
        if (formatted == null) {
            return null;
        }

        List<String> lines = new ArrayList<>();
        lines.add(StringUtils.spaces(indent) + applyKeywordCasing(matcher.group(2)) + SPACE + applyKeywordCasing(matcher.group(4)));
        for (String line : formatted.split(NEW_LINE)) {
            // The SQL formatter does not indent a comment in front of the
            // first token.
            if (StringUtils.getIndent(line).length() < sqlIndent) {
                line = StringUtils.spaces(sqlIndent) + line.trim();
            }
            lines.add(StringUtils.trimR(line));
        }

        for (String line : lines) {
            if (line.length() > endColumn) {
                return null;
            }
        }

        return lines;
    }

    /**
     * Changes the casing of an embedded SQL statement and keeps its lines.
     *
     * @param statement - the <code>EXEC SQL</code> statement
     * @return the lines with the new casing or <code>null</code>, if the
     *         casing cannot be changed
     */
    public List<String> adjustCase(CollectedStatement statement) {

        Matcher matcher = matchExecSql(statement);
        if (matcher == null) {
            return null;
        }

        String sql = getSql(matcher);
        String adjusted = formatSql(sql, 0, Integer.MAX_VALUE, true);
        if (adjusted == null) {
            return null;
        }

        if (adjusted.endsWith(NEW_LINE) && !sql.endsWith(NEW_LINE)) {
            adjusted = adjusted.substring(0, adjusted.length() - NEW_LINE.length());
        }

        String text = matcher.group(1) + applyKeywordCasing(matcher.group(2)) + matcher.group(3) + applyKeywordCasing(matcher.group(4))
            + matcher.group(5) + adjusted;

        List<String> lines = Arrays.asList(text.split(NEW_LINE, -1));
        if (lines.size() != statement.numLines()) {
            return null;
        }

        return lines;
    }

    /**
     * Returns <code>true</code>, if the formatted SQL statement differs from
     * the original statement only in the blanks and in the casing outside of
     * the literals and the delimited identifiers.
     *
     * @param original - the original SQL statement
     * @param formatted - the formatted SQL statement
     */
    public static boolean isSameStatement(String original, String formatted) {

        if (!getLiterals(original).equals(getLiterals(formatted))) {
            return false;
        }

        return normalize(original).equals(normalize(formatted));
    }

    /**
     * Returns the matcher of the <code>EXEC SQL</code> statement, whose group
     * 2 and 4 are <code>EXEC</code> and <code>SQL</code> and group 6 is the
     * SQL statement, or <code>null</code>, if the statement must not be
     * formatted.
     */
    private Matcher matchExecSql(CollectedStatement statement) {

        if (statement.hasContinuedLiteral()) {
            return null;
        }

        Matcher matcher = EXEC_SQL_PATTERN.matcher(String.join(NEW_LINE, statement.getOriginalStatements()));
        if (!matcher.matches()) {
            return null;
        }

        // A comment behind the semicolon would become part of the SQL
        // statement.
        String sql = getSql(matcher);
        if (!sql.endsWith(SEMICOLON) || sql.length() == SEMICOLON.length()) {
            return null;
        }

        return matcher;
    }

    private String getSql(Matcher matcher) {
        return StringUtils.trimR(matcher.group(6));
    }

    private String formatSql(String sql, int initialIndent, int lineLength, boolean adjustCaseOnly) {

        String formatted;
        try {
            formatted = AcsSqlFormatter.format(sql, config, initialIndent, lineLength, adjustCaseOnly);
        } catch (LinkageError e) {
            // RDi 9.6 does not provide the SQL formatter.
            isAvailable = false;
            return null;
        } catch (Exception e) {
            IRpgleFormatterPlugin.logError("Unexpected error formatting SQL statement: " + sql, e); //$NON-NLS-1$
            return null;
        }

        if (formatted == null || !isSameStatement(sql, formatted)) {
            return null;
        }

        return formatted;
    }

    private String applyKeywordCasing(String keyword) {
        return config.getSqlKeywordCasingStyle().apply(keyword);
    }

    private static List<String> getLiterals(String text) {

        List<String> literals = new ArrayList<>();
        Matcher matcher = LITERAL_PATTERN.matcher(text);
        while (matcher.find()) {
            literals.add(matcher.group());
        }

        return literals;
    }

    private static String normalize(String text) {

        String withoutLiterals = LITERAL_PATTERN.matcher(text).replaceAll(SINGLE_QUOTE);

        return WHITESPACE_PATTERN.matcher(withoutLiterals).replaceAll("").toUpperCase();
    }
}
