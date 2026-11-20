/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;
import de.tools400.lpex.irpgformatter.parser.BlockFinder;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;

/**
 * Unit tests for statements, that are not complete: the last statement of a
 * source, that has no semicolon, stays unchanged and is reported as a warning;
 * a selection, that ends inside a statement, is expanded to its end.
 */
public class IncompleteStatementTest extends AbstractTestCase {

    // --- RpgleFormatter: the last statement of the source is not complete ---

    @Test
    public void format_lastStatementIncomplete_isKeptAndReported() throws RpgleFormatterException {
        assertFormattedWithWarning(new RpgleFormatter());
    }

    @Test
    public void format_lastStatementIncomplete_withRulesOfEmptyLines() throws RpgleFormatterException {

        FormatterConfig config = FormatterConfig.fromPreferences();
        config.setRemoveEmptyCommentLines(true);
        config.setRemoveEmptyLinesBeforeDclPi(true);

        assertFormattedWithWarning(new RpgleFormatter(config));
    }

    /**
     * Formats a source, whose last statement on lines 3 and 4 has no semicolon:
     * the first statements are formatted, the last one stays unchanged and is
     * reported as a warning on line 3.
     */
    private void assertFormattedWithWarning(RpgleFormatter formatter) throws RpgleFormatterException {

        formatter.setSourceLength(100);

        String[] result = formatter.format(new TextLinesInput("**FREE", "dcl-s   x   char(10);", "x =   'a' +", "// comment"), 0).toLines();

        assertArrayEquals(new String[] { "**free", "dcl-s x char(10);", "x =   'a' +", "// comment" }, result);
        assertEquals(0, formatter.getErrorCount());
        assertEquals(1, formatter.getWarningCount());
        assertEquals(3, formatter.getWarnings().get(0).getStartLineNumber());
    }

    // --- BlockFinder: a selection, that ends inside a statement ---

    private static final String[] SOURCE = { "**FREE", "dcl-s x char(10);", "x =   'a' +", "    'b' +", "    'c';", "dcl-s y char(10);" };

    @Test
    public void expandSelection_endsInsideStatement_isExpandedToItsEnd() throws RpgleFormatterException {
        assertArrayEquals(new int[] { 3, 5 }, BlockFinder.expandSelection(SOURCE, 3, 4));
    }

    @Test
    public void expandSelection_endsBehindStatement_staysUnchanged() throws RpgleFormatterException {
        assertArrayEquals(new int[] { 2, 5 }, BlockFinder.expandSelection(SOURCE, 2, 5));
    }

    @Test
    public void expandSelection_incompleteStatementAtEnd_isExpandedToLastLine() throws RpgleFormatterException {

        String[] source = { "**FREE", "dcl-s x char(10);", "x =   'a' +", "    'b' +" };

        assertArrayEquals(new int[] { 3, 4 }, BlockFinder.expandSelection(source, 3, 3));
    }
}
