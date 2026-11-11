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

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;
import de.tools400.lpex.irpgformatter.preferences.ConditionParentheses;

/**
 * Unit tests for the outer parentheses of the conditions of IF, ELSEIF, WHEN,
 * DOW and DOU, see preference "Outer parentheses of conditions".
 */
public class ConditionParenthesesTest extends AbstractTestCase {

    private static int MAX_LINE_LENGTH = 90;

    private RpgleFormatter formatter;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        formatter = new RpgleFormatter();
        formatter.setSourceLength(MAX_LINE_LENGTH);
    }

    // ----------------------------------------------------------------------
    // Off
    // ----------------------------------------------------------------------

    @Test
    public void off_leavesConditionsUnchanged() throws RpgleFormatterException {

        assertArrayEquals(new String[] { "if a = 1;", "elseif (b = 2);", "endif;" }, format("if a = 1;", "elseif (b = 2);", "endif;"));
    }

    // ----------------------------------------------------------------------
    // With parentheses
    // ----------------------------------------------------------------------

    @Test
    public void with_addsParentheses() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITH_PARENTHESES);

        assertArrayEquals(new String[] { "if (a = 1);", "endif;" }, format("if a = 1;", "endif;"));
    }

    @Test
    public void with_allOperationCodes() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITH_PARENTHESES);

        // @formatter:off
        String[] result = format(
            "if a = 1;",
            "elseif a = 2;",
            "endif;",
            "dow a < 9;",
            "enddo;",
            "dou a > 9;",
            "enddo;",
            "select;",
            "when a = 3;",
            "endsl;"
        );
        // @formatter:on

        assertEquals("if (a = 1);", result[0]);
        assertEquals("elseif (a = 2);", result[1]);
        assertEquals("dow (a < 9);", result[3]);
        assertEquals("dou (a > 9);", result[5]);
        assertEquals("when (a = 3);", result[8]);
    }

    @Test
    public void with_keepsExistingParentheses() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITH_PARENTHESES);

        assertArrayEquals(new String[] { "if (a = 1);", "endif;" }, format("if (a = 1);", "endif;"));
    }

    @Test
    public void with_parenthesesOfPartsAreNoOuterParentheses() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITH_PARENTHESES);

        assertArrayEquals(new String[] { "if ((a = 1) and (b = 2));", "endif;" }, format("if (a = 1) and (b = 2);", "endif;"));
    }

    @Test
    public void with_operationExtender() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITH_PARENTHESES);

        assertArrayEquals(new String[] { "IF(M) (a > b);", "endif;" }, format("IF(M) a > b;", "endif;"));
    }

    @Test
    public void with_parenthesesOfFunctionsLiteralsAndComments() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITH_PARENTHESES);

        // @formatter:off
        String[] result = format(
            "if %parms() >= 2;",
            "elseif x = ')'; // (comment)",
            "elseif *in(10);",
            "endif;"
        );
        // @formatter:on

        assertEquals("if (%parms() >= 2);", result[0]);
        assertEquals("elseif (x = ')'); // (comment)", result[1]);
        assertEquals("elseif (*in(10));", result[2]);
    }

    @Test
    public void with_continuedLinesStayAligned() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITH_PARENTHESES);

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "if a = 1",
            "   and b = 2;",
            "endif;",
            "END-PROC;"
        );
        // @formatter:on

        assertEquals("  if (a = 1", result[1]);
        assertEquals("      and b = 2);", result[2]);
    }

    @Test
    public void with_whenIsHasNoCondition() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITH_PARENTHESES);

        assertArrayEquals(new String[] { "select a;", "when-is 1;", "endsl;" }, format("select a;", "when-is 1;", "endsl;"));
    }

    @Test
    public void with_lineTooLong_leavesStatementUnchanged() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITH_PARENTHESES);
        formatter.setSourceLength(10);

        assertArrayEquals(new String[] { "if a = 1;", "endif;" }, format("if a = 1;", "endif;"));
        assertEquals(1, formatter.getErrorCount());
    }

    // ----------------------------------------------------------------------
    // Without parentheses
    // ----------------------------------------------------------------------

    @Test
    public void without_removesParentheses() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITHOUT_PARENTHESES);

        assertArrayEquals(new String[] { "dow not %eof(file);", "enddo;" }, format("dow (not %eof(file));", "enddo;"));
    }

    @Test
    public void without_removesAllOuterParenthesesAndTheirBlanks() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITHOUT_PARENTHESES);

        assertArrayEquals(new String[] { "if a = 1;", "elseif a = 2;", "endif;" }, format("if ((a = 1));", "elseif ( a = 2 );", "endif;"));
    }

    @Test
    public void without_parenthesesOfPartsAreNoOuterParentheses() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITHOUT_PARENTHESES);

        assertArrayEquals(new String[] { "if (a = 1) and (b = 2);", "endif;" }, format("if (a = 1) and (b = 2);", "endif;"));
    }

    @Test
    public void without_parenthesisWithoutBlank_leavesStatementUnchanged() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITHOUT_PARENTHESES);

        assertArrayEquals(new String[] { "if(a = 1);", "endif;" }, format("if(a = 1);", "endif;"));
    }

    @Test
    public void without_continuedLinesStayAligned() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITHOUT_PARENTHESES);

        // @formatter:off
        String[] result = format(
            "if (a = 1",
            "    and b = 2);",
            "endif;"
        );
        // @formatter:on

        assertArrayEquals(new String[] { "if a = 1", "   and b = 2;", "endif;" }, result);
    }

    @Test
    public void without_closingParenthesisStartsLine_leavesStatementUnchanged() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITHOUT_PARENTHESES);

        // @formatter:off
        String[] result = format(
            "if (a = 1",
            ");",
            "endif;"
        );
        // @formatter:on

        assertArrayEquals(new String[] { "if (a = 1", ");", "endif;" }, result);
    }

    @Test
    public void without_continuedLiteral_leavesStatementUnchanged() throws RpgleFormatterException {

        setStyle(ConditionParentheses.WITHOUT_PARENTHESES);

        // @formatter:off
        String[] result = format(
            "if (x = 'abc+",
            "     def');",
            "endif;"
        );
        // @formatter:on

        assertEquals("if (x = 'abc+", result[0]);
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------

    private void setStyle(ConditionParentheses style) {
        formatter.getConfig().setConditionParentheses(style);
    }

    private String[] format(String... source) throws RpgleFormatterException {
        return formatter.format(new TextLinesInput(source), 0).toLines();
    }
}
