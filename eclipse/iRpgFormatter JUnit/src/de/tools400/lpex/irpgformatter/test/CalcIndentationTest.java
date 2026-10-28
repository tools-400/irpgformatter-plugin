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

/**
 * Unit tests for the indentation of calculation statements.
 * <p>
 * The formatter indents the calculations by the nesting of their blocks, but
 * does not change their content. Branches of a block, such as
 * <code>ELSE</code>, are placed on the level of the statement that starts the
 * block. The indentation follows the IBM preferences "Number of spaces to
 * indent by", "Indent WHEN/OTHER within SELECT statement" and "Preserve
 * relative indentation of continued lines".
 * </p>
 */
public class CalcIndentationTest extends AbstractTestCase {

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
    // Blocks
    // ----------------------------------------------------------------------

    @Test
    public void format_ifElseifElse_insideProcedure() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "if a = 1;",
            "x = 1;",
            "elseif a = 2;",
            "x = 2;",
            "else;",
            "if b = 1;",
            "x = 3;",
            "endif;",
            "endif;",
            "END-PROC;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "dcl-proc p;",
            "  if a = 1;",
            "    x = 1;",
            "  elseif a = 2;",
            "    x = 2;",
            "  else;",
            "    if b = 1;",
            "      x = 3;",
            "    endif;",
            "  endif;",
            "end-proc;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_loops() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "dow not %eof(f);",
            "read f;",
            "enddo;",
            "dou done;",
            "done = *on;",
            "enddo;",
            "for i = 1 to 10;",
            "x += i;",
            "endfor;",
            "for-each item in items;",
            "dsply item;",
            "endfor;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "dow not %eof(f);",
            "  read f;",
            "enddo;",
            "dou done;",
            "  done = *on;",
            "enddo;",
            "for i = 1 to 10;",
            "  x += i;",
            "endfor;",
            "for-each item in items;",
            "  dsply item;",
            "endfor;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_select() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "select;",
            "when a = 1;",
            "x = 1;",
            "other;",
            "x = 3;",
            "endsl;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "select;",
            "when a = 1;",
            "  x = 1;",
            "other;",
            "  x = 3;",
            "endsl;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_select_withExtraIndentOfWhenAndOther() throws RpgleFormatterException {

        formatter.getConfig().setSelectIndent(2);

        // @formatter:off
        String[] result = format(
            "select;",
            "when a = 1;",
            "select;",
            "when b = 1;",
            "x = 1;",
            "endsl;",
            "other;",
            "x = 3;",
            "endsl;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "select;",
            "  when a = 1;",
            "    select;",
            "      when b = 1;",
            "        x = 1;",
            "    endsl;",
            "  other;",
            "    x = 3;",
            "endsl;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_selectWithOperand() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "select a;",
            "when-is 1;",
            "x = 1;",
            "when-in %list(2: 3);",
            "x = 2;",
            "other;",
            "x = 3;",
            "endsl;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "select a;",
            "when-is 1;",
            "  x = 1;",
            "when-in %list(2: 3);",
            "  x = 2;",
            "other;",
            "  x = 3;",
            "endsl;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_monitor() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "monitor;",
            "x = 1 / y;",
            "on-excp 'MCH1211';",
            "x = 0;",
            "on-error;",
            "x = -1;",
            "endmon;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "monitor;",
            "  x = 1 / y;",
            "on-excp 'MCH1211';",
            "  x = 0;",
            "on-error;",
            "  x = -1;",
            "endmon;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_subroutine() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "begsr init;",
            "x = 0;",
            "endsr;"
        );
        // @formatter:on

        assertArrayEquals(new String[] { "begsr init;", "  x = 0;", "endsr;" }, result);
    }

    @Test
    public void format_onExitOfProcedure() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "DCL-PI *n;",
            "END-PI;",
            "x = 1;",
            "on-exit;",
            "x = 0;",
            "END-PROC;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "dcl-proc p;",
            "  dcl-pi *n;",
            "  end-pi;",
            "  x = 1;",
            "on-exit;",
            "  x = 0;",
            "end-proc;"
        }, result);
        // @formatter:on
    }

    // ----------------------------------------------------------------------
    // Continued lines
    // ----------------------------------------------------------------------

    @Test
    public void format_continuedLines_preserveRelativeIndent() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "x = a +",
            "    b +",
            "    c;",
            "END-PROC;"
        );
        // @formatter:on

        assertEquals("  x = a +", result[1]);
        assertEquals("      b +", result[2]);
        assertEquals("      c;", result[3]);
    }

    @Test
    public void format_continuedLines_lineUp() throws RpgleFormatterException {

        formatter.getConfig().setPreserveRelativeIndentOfContinuedLines(false);

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "x = a +",
            "          b +",
            "    c;",
            "END-PROC;"
        );
        // @formatter:on

        assertEquals("  x = a +", result[1]);
        assertEquals("    b +", result[2]);
        assertEquals("    c;", result[3]);
    }

    @Test
    public void format_literalContinuedWithHyphen_keepsLeadingBlanks() throws RpgleFormatterException {

        // The leading blanks of the continued line belong to the literal.
        for (boolean preserveRelativeIndent : new boolean[] { true, false }) {
            formatter.getConfig().setPreserveRelativeIndentOfContinuedLines(preserveRelativeIndent);

            // @formatter:off
            String[] result = format(
                "DCL-PROC p;",
                "dsply ('abc -",
                "   def');",
                "END-PROC;"
            );
            // @formatter:on

            assertEquals("  dsply ('abc -", result[1]);
            assertEquals("   def');", result[2]);
        }
    }

    @Test
    public void format_literalContinuedWithPlus_isIndented() throws RpgleFormatterException {

        // The leading blanks of the continued line are not part of the literal.
        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "dsply ('abc +",
            "   def');",
            "END-PROC;"
        );
        // @formatter:on

        assertEquals("  dsply ('abc +", result[1]);
        assertEquals("     def');", result[2]);
    }

    @Test
    public void format_commentBetweenContinuedLines_staysInPlace() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "x = a +",
            "  // second operand",
            "  b;",
            "END-PROC;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "dcl-proc p;",
            "  x = a +",
            "    // second operand",
            "    b;",
            "end-proc;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_execSql_shiftsContinuedLines() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "exec sql",
            "  select a",
            "    into :x",
            "    from t;",
            "END-PROC;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "dcl-proc p;",
            "  exec sql",
            "    select a",
            "      into :x",
            "      from t;",
            "end-proc;"
        }, result);
        // @formatter:on
    }

    // ----------------------------------------------------------------------
    // Comments
    // ----------------------------------------------------------------------

    @Test
    public void format_comments_indentedLikeNextStatement() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "// before if",
            "if a = 1;",
            "// inside if",
            "x = 1;",
            "    // before else",
            "else;",
            "x = 2;",
            "// before endif",
            "endif;",
            "END-PROC;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "dcl-proc p;",
            "  // before if",
            "  if a = 1;",
            "    // inside if",
            "    x = 1;",
            "  // before else",
            "  else;",
            "    x = 2;",
            "  // before endif",
            "  endif;",
            "end-proc;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_comments_withExtraIndentOfWhenAndOther() throws RpgleFormatterException {

        formatter.getConfig().setSelectIndent(2);

        // @formatter:off
        String[] result = format(
            "select;",
            "// first case",
            "when a = 1;",
            "// set x",
            "x = 1;",
            "endsl;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "select;",
            "  // first case",
            "  when a = 1;",
            "    // set x",
            "    x = 1;",
            "endsl;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_comment_skipsBlankLinesAndDirectives() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "if a = 1;",
            "// only for X",
            "",
            "/if defined(X)",
            "x = 1;",
            "/endif",
            "endif;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "if a = 1;",
            "  // only for X",
            "",
            "/if defined(X)",
            "  x = 1;",
            "/endif",
            "endif;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_commentAtEnd_indentedLikeEnclosingBlock() throws RpgleFormatterException {

        // Formatting the selected lines of a larger source
        // @formatter:off
        String[] result = format(
            "if a = 1;",
            "x = 1;",
            "// last line"
        );
        // @formatter:on

        assertArrayEquals(new String[] { "if a = 1;", "  x = 1;", "  // last line" }, result);
    }

    @Test
    public void format_commentTooLongAfterIndent_leavesCommentUnchanged() throws RpgleFormatterException {

        formatter.setSourceLength(20);

        // @formatter:off
        String[] result = format(
            "if a = 1;",
            "// 1234567890123456",
            "x = 1;",
            "endif;"
        );
        // @formatter:on

        assertArrayEquals(new String[] { "if a = 1;", "// 1234567890123456", "  x = 1;", "endif;" }, result);
        assertEquals(1, formatter.getErrorCount());
    }

    // ----------------------------------------------------------------------
    // Limits
    // ----------------------------------------------------------------------

    @Test
    public void format_lineTooLongAfterIndent_leavesStatementUnchanged() throws RpgleFormatterException {

        formatter.setSourceLength(20);

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "x = 'aaaaaaaaaaaa';",
            "END-PROC;"
        );
        // @formatter:on

        assertEquals("x = 'aaaaaaaaaaaa';", result[1]);
        assertEquals(1, formatter.getErrorCount());
    }

    @Test
    public void format_blockNotClosed_formatsSelectedLines() throws RpgleFormatterException {

        // Formatting the selected lines of a larger source
        // @formatter:off
        String[] result = format(
            "if a = 1;",
            "x = 1;"
        );
        // @formatter:on

        assertArrayEquals(new String[] { "if a = 1;", "  x = 1;" }, result);
        assertEquals(0, formatter.getErrorCount());
    }

    @Test
    public void format_blockEndWithoutStart_formatsSelectedLines() throws RpgleFormatterException {

        // Formatting the selected lines of a larger source
        // @formatter:off
        String[] result = format(
            "x = 1;",
            "endif;"
        );
        // @formatter:on

        assertArrayEquals(new String[] { "x = 1;", "endif;" }, result);
        assertEquals(0, formatter.getErrorCount());
    }

    // ----------------------------------------------------------------------
    // Conditional compilation
    // ----------------------------------------------------------------------

    @Test
    public void format_alternativeIfHeaders_indentsLikeOneHeader() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "/if defined(X)",
            "if a = 1;",
            "/else",
            "if a = 2;",
            "/endif",
            "b = 1;",
            "endif;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "/if defined(X)",
            "if a = 1;",
            "/else",
            "if a = 2;",
            "/endif",
            "  b = 1;",
            "endif;"
        }, result);
        // @formatter:on
        assertEquals(0, formatter.getWarningCount());
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------

    private String[] format(String... source) throws RpgleFormatterException {
        return formatter.format(new TextLinesInput(source), 0).toLines();
    }
}
