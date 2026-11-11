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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.EmbeddedSqlFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.SqlBreakStyle;
import de.tools400.lpex.irpgformatter.preferences.SqlCasingStyle;

/**
 * Unit tests for the formatting of embedded SQL statements with the SQL
 * formatter of IBM i Access Client Solutions.
 * <p>
 * The formatter follows the IBM preferences of the ILE RPG SQL formatting:
 * "Formatting" with "Indent" and "New line", and "Automatic uppercasing".
 * </p>
 */
public class EmbeddedSqlTest extends AbstractTestCase {

    private static int MAX_LINE_LENGTH = 80;

    private RpgleFormatter formatter;
    private FormatterConfig config;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        formatter = new RpgleFormatter();
        formatter.setSourceLength(MAX_LINE_LENGTH);
        config = formatter.getConfig();
    }

    // ----------------------------------------------------------------------
    // Formatting
    // ----------------------------------------------------------------------

    @Test
    public void format_disabled_onlyIndents() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "exec sql select a into :x from t;",
            "END-PROC;"
        );
        // @formatter:on

        assertArrayEquals(new String[] { "dcl-proc p;", "  exec sql select a into :x from t;", "end-proc;" }, result);
    }

    @Test
    public void format_putsExecSqlOnLineOfItsOwn() throws RpgleFormatterException {

        config.setFormatEmbeddedSql(true);

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "exec sql select a, b into :x, :y from t where c = :z and d = 1;",
            "END-PROC;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "dcl-proc p;",
            "  exec sql",
            "    select a,",
            "           b",
            "      into :x,",
            "           :y",
            "      from t",
            "      where c = :z and",
            "            d = 1;",
            "end-proc;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_multipleLines_insideBlock() throws RpgleFormatterException {

        config.setFormatEmbeddedSql(true);

        // @formatter:off
        String[] result = format(
            "if a = 1;",
            "Exec Sql",
            "      Update mytable set col1 = :val1, col2 = current timestamp",
            "  where id = :id;",
            "endif;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "if a = 1;",
            "  Exec Sql",
            "    Update mytable",
            "      set col1 = :val1,",
            "          col2 = current timestamp",
            "      where id = :id;",
            "endif;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_newLineBeforeAndOr() throws RpgleFormatterException {

        config.setFormatEmbeddedSql(true);
        config.setSqlBreakOnComma(SqlBreakStyle.NONE);
        config.setSqlBreakOnAndOr(SqlBreakStyle.BEFORE);

        String[] result = format("exec sql select a, b into :x, :y from t where c = :z and d = 1;");

        // @formatter:off
        assertArrayEquals(new String[] {
            "exec sql",
            "  select a, b",
            "    into :x, :y",
            "    from t",
            "    where c = :z",
            "          and d = 1;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_sqlIndent() throws RpgleFormatterException {

        config.setFormatEmbeddedSql(true);
        config.setSqlIndent(4);

        String[] result = format("exec sql update t set a = 1 where b = 2;");

        assertArrayEquals(new String[] { "exec sql", "    update t", "        set a = 1", "        where b = 2;" }, result);
    }

    @Test
    public void format_casing() throws RpgleFormatterException {

        config.setFormatEmbeddedSql(true);
        config.setSqlKeywordCasingStyle(SqlCasingStyle.UPPER);
        config.setSqlIdentifierCasingStyle(SqlCasingStyle.LOWER);

        String[] result = format("exec sql select A, B into :X from T;");

        assertArrayEquals(new String[] { "EXEC SQL", "  SELECT a,", "         b", "    INTO :x", "    FROM t;" }, result);
    }

    @Test
    public void adjustCase_keepsLines() throws RpgleFormatterException {

        config.setSqlKeywordCasingStyle(SqlCasingStyle.UPPER);

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "exec sql select a,",
            "            b into :x",
            "  from t;",
            "END-PROC;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "dcl-proc p;",
            "  EXEC SQL SELECT a,",
            "              b INTO :x",
            "    FROM t;",
            "end-proc;"
        }, result);
        // @formatter:on
    }

    // ----------------------------------------------------------------------
    // Comments
    // ----------------------------------------------------------------------

    @Test
    public void format_commentInFrontOfFirstToken_isIndented() throws RpgleFormatterException {

        config.setFormatEmbeddedSql(true);

        // @formatter:off
        String[] result = format(
            "exec sql",
            "  -- the first",
            "  select a into :x from t;"
        );
        // @formatter:on

        assertArrayEquals(new String[] { "exec sql", "  -- the first", "  select a", "    into :x", "    from t;" }, result);
    }

    @Test
    public void format_sqlCommentWithSemicolonAndQuote_isPartOfStatement() throws RpgleFormatterException {

        config.setFormatEmbeddedSql(true);

        // @formatter:off
        String[] result = format(
            "exec sql",
            "  select a -- don't; really",
            "    into :x from t;",
            "x = 1;"
        );
        // @formatter:on

        assertArrayEquals(new String[] { "exec sql", "  select a -- don't; really", "    into :x", "    from t;", "x = 1;" }, result);
    }

    @Test
    public void format_lineStartingWithBlockComment_isNotADirective() throws RpgleFormatterException {

        config.setFormatEmbeddedSql(true);

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "exec sql",
            "  select a",
            "  /* the source */ from t;",
            "END-PROC;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "dcl-proc p;",
            "  exec sql",
            "    select a",
            "      /* the source */",
            "      from t;",
            "end-proc;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_blockCommentWithSemicolonAndQuote_isPartOfStatement() throws RpgleFormatterException {

        config.setFormatEmbeddedSql(true);

        // The second line of the comment looks like a compiler directive.
        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "exec sql",
            "  select a /* don't;",
            "  /if stop */ into :x from t;",
            "x = 1;",
            "END-PROC;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "dcl-proc p;",
            "  exec sql",
            "    select a",
            "      /* don't;",
            "    /if stop */",
            "      into :x",
            "      from t;",
            "  x = 1;",
            "end-proc;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_delimitedIdentifierWithCommentAndSemicolon_isPartOfStatement() throws RpgleFormatterException {

        config.setFormatEmbeddedSql(true);

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "exec sql select \"A--B\", \"C;\"\"D\" into :x, :y from t;",
            "x = 1;",
            "END-PROC;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "dcl-proc p;",
            "  exec sql",
            "    select \"A--B\",",
            "           \"C;\"\"D\"",
            "      into :x,",
            "           :y",
            "      from t;",
            "  x = 1;",
            "end-proc;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_doubleQuoteInLiteralOfRpg_isNoDelimitedIdentifier() throws RpgleFormatterException {

        String[] result = format("DCL-PROC p;", "dsply ('a\"b');", "x = 1;", "END-PROC;");

        assertArrayEquals(new String[] { "dcl-proc p;", "  dsply ('a\"b');", "  x = 1;", "end-proc;" }, result);
    }

    @Test
    public void format_commentBehindSemicolon_onlyIndents() throws RpgleFormatterException {

        config.setFormatEmbeddedSql(true);

        String[] result = format("if a = 1;", "exec sql select a into :x from t; // get a", "endif;");

        assertArrayEquals(new String[] { "if a = 1;", "  exec sql select a into :x from t; // get a", "endif;" }, result);
    }

    // ----------------------------------------------------------------------
    // Statements, that are only indented
    // ----------------------------------------------------------------------

    @Test
    public void format_syntaxError_onlyIndents() throws RpgleFormatterException {

        config.setFormatEmbeddedSql(true);

        String[] result = format("if a = 1;", "exec sql selec a frm t;", "endif;");

        assertArrayEquals(new String[] { "if a = 1;", "  exec sql selec a frm t;", "endif;" }, result);
        assertEquals(0, formatter.getErrorCount());
    }

    @Test
    public void format_continuedLiteral_onlyIndents() throws RpgleFormatterException {

        config.setFormatEmbeddedSql(true);

        // @formatter:off
        String[] result = format(
            "if a = 1;",
            "exec sql select a into :x from t where b = 'abc+",
            "  def';",
            "endif;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "if a = 1;",
            "  exec sql select a into :x from t where b = 'abc+",
            "    def';",
            "endif;"
        }, result);
        // @formatter:on
    }

    @Test
    public void format_lineTooLong_onlyIndents() throws RpgleFormatterException {

        config.setFormatEmbeddedSql(true);
        formatter.setSourceLength(30);

        String[] result = format("exec sql select 'a literal longer than the end column' into :x from t;");

        assertArrayEquals(new String[] { "exec sql select 'a literal longer than the end column' into :x from t;" }, result);
        assertEquals(0, formatter.getErrorCount());
    }

    // ----------------------------------------------------------------------
    // Safety check
    // ----------------------------------------------------------------------

    @Test
    public void isSameStatement_blanksAndCasing() {
        assertTrue(EmbeddedSqlFormatter.isSameStatement("select a  from t where b = 'x'", "SELECT A\n  FROM T\n  WHERE B = 'x'"));
    }

    @Test
    public void isSameStatement_changedLiteral() {
        assertFalse(EmbeddedSqlFormatter.isSameStatement("select a from t where b = 'a  b'", "select a from t where b = 'a b'"));
        assertFalse(EmbeddedSqlFormatter.isSameStatement("select a from t where b = 'x'", "select a from t where b = 'X'"));
    }

    @Test
    public void isSameStatement_changedDelimitedIdentifier() {
        assertFalse(EmbeddedSqlFormatter.isSameStatement("select \"MyCol\" from t", "select \"MYCOL\" from t"));
    }

    @Test
    public void isSameStatement_changedToken() {
        assertFalse(EmbeddedSqlFormatter.isSameStatement("select a, b from t", "select a b from t"));
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------

    private String[] format(String... source) throws RpgleFormatterException {
        return formatter.format(new TextLinesInput(source), 0).toLines();
    }
}
