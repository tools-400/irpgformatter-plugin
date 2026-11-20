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
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;
import de.tools400.lpex.irpgformatter.tokenizer.Tokenizer;

/**
 * Unit tests for date, time and timestamp literals, e.g.
 * <code>d'2026-11-20'</code>, <code>t'12:00:00'</code> and
 * <code>z'2026-11-20-12.00.00.000000'</code>. A literal of this kind is one
 * token, that keeps its text: the colons of a time literal are no separators
 * and the literal is never split.
 */
public class DateTimeLiteralTest extends AbstractTestCase {

    private static final int SOURCE_LENGTH = 100;

    // @formatter:off
    /**
     * Literals in every format and with every separator of the ILE RPG
     * Reference 7.3, tables 66 and 69, and the timestamp literal. The format
     * of a date or time literal is the one of DATFMT or TIMFMT of the control
     * specification. The separator <code>&amp;</code> is a blank.
     */
    private static final String[] LITERALS = {
        // *MDY with the separators / - . , and blank
        "d'01/15/96'", "d'01-15-96'", "d'01.15.96'", "d'01,15,96'", "d'01 15 96'",
        // *DMY, *YMD, *JUL
        "d'15/01/96'", "d'96/01/15'", "d'96/015'", "D'96 015'",
        // *ISO, *USA, *EUR, *JIS
        "d'1996-01-15'", "d'01/15/1996'", "d'15.01.1996'", "D'1996-01-15'",
        // *HMS with the separators : . , and blank
        "t'14:00:00'", "t'14.00.00'", "t'14,00,00'", "t'14 00 00'",
        // *ISO, *USA (AM and PM in any case), *EUR, *JIS
        "t'14.00.00'", "t'02:00 PM'", "T'02:00 am'", "t'14.00.00'", "T'14:00:00'",
        // timestamp with 0 and 12 fractional seconds
        "z'1996-01-15-14.00.00'", "Z'1996-01-15-14.00.00.123456789012'" };
    // @formatter:on

    private Tokenizer tokenizer;
    private RpgleFormatter formatter;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        tokenizer = new Tokenizer();
        formatter = new RpgleFormatter();
        formatter.setSourceLength(SOURCE_LENGTH);
    }

    // --- Tokenizer ---

    @Test
    public void tokenize_timeLiteral_isOneToken() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize("t'12:00:00'");

        assertEquals(1, tokens.length);
        assertEquals(TokenType.DATE_TIME_LITERAL, tokens[0].getType());
        assertEquals("t'12:00:00'", tokens[0].getValue());
    }

    @Test
    public void tokenize_timeLiteralWithBlank_isOneToken() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize("t'08:00 AM'");

        assertEquals(1, tokens.length);
        assertEquals(TokenType.DATE_TIME_LITERAL, tokens[0].getType());
        assertEquals("t'08:00 AM'", tokens[0].getValue());
    }

    @Test
    public void tokenize_dateAndTimestampLiteral_upperCasePrefix() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize("D'2026-11-20' Z'2026-11-20-12.00.00.000000'");

        assertEquals(2, tokens.length);
        assertEquals(TokenType.DATE_TIME_LITERAL, tokens[0].getType());
        assertEquals("D'2026-11-20'", tokens[0].getValue());
        assertEquals(TokenType.DATE_TIME_LITERAL, tokens[1].getType());
        assertEquals("Z'2026-11-20-12.00.00.000000'", tokens[1].getValue());
    }

    @Test
    public void tokenize_timeLiteralAsParameter() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize("inz(t'12:00:00')");

        assertEquals(1, tokens.length);
        assertEquals(TokenType.KEYWORD, tokens[0].getType());
        assertEquals(1, tokens[0].getNumChildren());
        assertEquals(TokenType.DATE_TIME_LITERAL, tokens[0].getChild(0).getType());
        assertEquals("t'12:00:00'", tokens[0].getChild(0).getValue());
    }

    @Test
    public void tokenize_everyFormat_isOneToken() throws RpgleFormatterException {

        for (String literal : LITERALS) {
            IToken[] tokens = tokenizer.tokenize(literal);

            assertEquals(literal, 1, tokens.length);
            assertEquals(literal, TokenType.DATE_TIME_LITERAL, tokens[0].getType());
            assertEquals(literal, literal, tokens[0].getValue());
        }
    }

    // --- Formatter ---

    @Test
    public void format_everyFormat_keepsTheLiteral() throws RpgleFormatterException {

        for (String literal : LITERALS) {
            assertFormatted("dcl-c LITERAL " + literal + ";", "dcl-c LITERAL " + literal + ";");

            String declaration = "dcl-s field " + getDataType(literal) + " inz(" + literal + ");";
            assertFormatted(declaration, declaration);
        }
    }

    @Test
    public void format_dclC_timeLiteral() throws RpgleFormatterException {
        assertFormatted("dcl-c NOON t'12:00:00';", "dcl-c NOON t'12:00:00';");
    }

    @Test
    public void format_dclC_timeLiteralWithBlank() throws RpgleFormatterException {
        assertFormatted("dcl-c START t'08:00 AM';", "dcl-c START t'08:00 AM';");
    }

    @Test
    public void format_dclS_inzTimeLiteral() throws RpgleFormatterException {
        assertFormatted("dcl-s start time inz(t'12:00:00');", "dcl-s start time inz(t'12:00:00');");
    }

    @Test
    public void format_dclS_inzDateAndTimestampLiteral() throws RpgleFormatterException {
        assertFormatted("dcl-s day date inz(D'2026-11-20');", "dcl-s day date inz(D'2026-11-20');");
        assertFormatted("dcl-s ts timestamp inz(z'2026-11-20-12.00.00.000000');", "dcl-s ts timestamp inz(z'2026-11-20-12.00.00.000000');");
    }

    @Test
    public void format_dclC_timeLiteral_useConstKeyword() throws RpgleFormatterException {

        FormatterConfig config = FormatterConfig.fromPreferences();
        config.setUseConstKeyword(true);
        formatter = new RpgleFormatter(config);
        formatter.setSourceLength(SOURCE_LENGTH);

        assertFormatted("dcl-c NOON t'12:00:00';", "dcl-c NOON const(t'12:00:00');");
        assertFormatted("dcl-c NOON const(t'12:00:00');", "dcl-c NOON const(t'12:00:00');");
    }

    @Test
    public void format_dclC_constTimeLiteral_withoutConstKeyword() throws RpgleFormatterException {
        assertFormatted("dcl-c NOON const(t'12:00:00');", "dcl-c NOON t'12:00:00';");
    }

    private void assertFormatted(String source, String expected) throws RpgleFormatterException {

        String[] result = formatter.format(new TextLinesInput(source), 0).toLines();

        assertEquals(source, 0, formatter.getErrorCount());
        assertArrayEquals(source, new String[] { expected }, result);
    }

    private static String getDataType(String literal) {

        switch (Character.toUpperCase(literal.charAt(0))) {
        case 'D':
            return "date";
        case 'T':
            return "time";
        default:
            return "timestamp";
        }
    }
}
