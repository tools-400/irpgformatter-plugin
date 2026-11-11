/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

import com.ibm.ibmi.sql.formatter.ISqlFormattingSettings.BREAK_SETTING;
import com.ibm.ibmi.sql.formatter.impl.SQLUpperCaseSettings;
import com.ibm.ibmi.sql.formatter.impl.SqlFormatter;
import com.ibm.ibmi.sql.formatter.impl.SqlFormattingSettings;
import com.ibm.ibmi.sql.formatter.impl.SqlFormattingSettings.UPPERCASE_SETTING;

import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.SqlBreakStyle;
import de.tools400.lpex.irpgformatter.preferences.SqlCasingStyle;

/**
 * Calls the SQL formatter of IBM i Access Client Solutions, that RDi uses for
 * the IBM preference "Use the Enhanced SQL Formatter".
 * <p>
 * This is the only class, that refers to the classes of the SQL formatter. RDi
 * 9.6 does not provide them in bundle <code>com.ibm.ibmi.sql.parser</code>,
 * therefore the callers catch the {@link LinkageError}.
 * </p>
 */
final class AcsSqlFormatter {

    private AcsSqlFormatter() {
        // Utility class
    }

    /**
     * Formats an SQL statement.
     *
     * @param sql - the SQL statement without <code>EXEC SQL</code>
     * @param config - the formatter configuration
     * @param initialIndent - the indent of the first line
     * @param lineLength - the maximum length of the lines
     * @param adjustCaseOnly - <code>true</code>, if only the casing is changed
     * @return the formatted statement or <code>null</code>, if the SQL
     *         formatter reports a syntax error
     * @throws Exception if the SQL formatter fails
     */
    static String format(String sql, FormatterConfig config, int initialIndent, int lineLength, boolean adjustCaseOnly) throws Exception {

        SqlFormattingSettings settings = new SqlFormattingSettings();
        settings.setIndent(config.getSqlIndent());
        settings.setInitialIndent(initialIndent);
        settings.setLineLength(lineLength);
        settings.setBreakOnComma(toBreakSetting(config.getSqlBreakOnComma()));
        settings.setBreakOnAndOr(toBreakSetting(config.getSqlBreakOnAndOr()));
        settings.setUppercasing(
            new SQLUpperCaseSettings(toUppercaseSetting(config.getSqlKeywordCasingStyle()), toUppercaseSetting(config.getSqlIdentifierCasingStyle())));
        settings.setAdjustCaseOnly(adjustCaseOnly);

        SqlFormatter formatter = new SqlFormatter(settings);
        formatter.setInitialIndent(initialIndent);

        String formatted = formatter.formatSQLStatement(sql);
        if (!formatter.getMessageHandler().getMessages().isEmpty()) {
            return null;
        }

        return formatted;
    }

    private static BREAK_SETTING toBreakSetting(SqlBreakStyle style) {

        switch (style) {
        case AFTER:
            return BREAK_SETTING.AFTER_IN_LIST;
        case BEFORE:
            return BREAK_SETTING.BEFORE_IN_LIST;
        default:
            return BREAK_SETTING.NO_BREAK;
        }
    }

    private static UPPERCASE_SETTING toUppercaseSetting(SqlCasingStyle style) {

        switch (style) {
        case UPPER:
            return UPPERCASE_SETTING.UPPER;
        case LOWER:
            return UPPERCASE_SETTING.LOWER;
        case INITIAL:
            return UPPERCASE_SETTING.INITIAL;
        default:
            return UPPERCASE_SETTING.IGNORE;
        }
    }
}
