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
 * Unit tests for graphic literals, e.g. <code>G'oK1K2i'</code>. A graphic
 * literal is one token, that keeps the text between its apostrophes, blanks and
 * colons included, and it is never split.
 */
public class GraphicLiteralTest extends AbstractTestCase {

    private static final int SOURCE_LENGTH = 100;

    /**
     * Three Japanese characters.
     */
    private static final String DBCS = "日本語";

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
    public void tokenize_graphicLiteral_isOneToken() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize("G'oAABBi'");

        assertEquals(1, tokens.length);
        assertEquals(TokenType.GRAPHIC_LITERAL, tokens[0].getType());
        assertEquals("G'oAABBi'", tokens[0].getValue());
    }

    @Test
    public void tokenize_graphicLiteralWithBlank_isOneToken() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize("g'ab cd'");

        assertEquals(1, tokens.length);
        assertEquals(TokenType.GRAPHIC_LITERAL, tokens[0].getType());
        assertEquals("g'ab cd'", tokens[0].getValue());
    }

    @Test
    public void tokenize_graphicLiteralAsParameter() throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize("inz(g'ab cd')");

        assertEquals(1, tokens.length);
        assertEquals(TokenType.KEYWORD, tokens[0].getType());
        assertEquals(1, tokens[0].getNumChildren());
        assertEquals(TokenType.GRAPHIC_LITERAL, tokens[0].getChild(0).getType());
        assertEquals("g'ab cd'", tokens[0].getChild(0).getValue());
    }

    // --- Formatter ---

    @Test
    public void format_dclC_graphicLiteralWithBlanks() throws RpgleFormatterException {
        assertFormatted("dcl-c GRAPHIC g'ab  cd';", "dcl-c GRAPHIC g'ab  cd';");
    }

    @Test
    public void format_dclC_graphicLiteralWithColon() throws RpgleFormatterException {
        assertFormatted("dcl-c GRAPHIC g'ab:cd';", "dcl-c GRAPHIC g'ab:cd';");
    }

    @Test
    public void format_dclS_inzGraphicLiteral() throws RpgleFormatterException {
        assertFormatted("dcl-s field graph(10) inz(g'" + DBCS + "');", "dcl-s field graph(10) inz(g'" + DBCS + "');");
        assertFormatted("dcl-s field graph(10) inz(g' " + DBCS + " ');", "dcl-s field graph(10) inz(g' " + DBCS + " ');");
    }

    @Test
    public void format_dclC_graphicLiteral_useConstKeyword() throws RpgleFormatterException {

        FormatterConfig config = FormatterConfig.fromPreferences();
        config.setUseConstKeyword(true);
        formatter = new RpgleFormatter(config);
        formatter.setSourceLength(SOURCE_LENGTH);

        assertFormatted("dcl-c GRAPHIC g'ab cd';", "dcl-c GRAPHIC const(g'ab cd');");
    }

    @Test
    public void format_dclC_constGraphicLiteral_withoutConstKeyword() throws RpgleFormatterException {
        assertFormatted("dcl-c GRAPHIC const(g'ab cd');", "dcl-c GRAPHIC g'ab cd';");
    }

    /**
     * A graphic literal, that does not fit on the line, is not split, but moved
     * to the next line.
     */
    @Test
    public void format_dclC_longGraphicLiteral_isNotSplit() throws RpgleFormatterException {

        formatter.setSourceLength(40);

        String literal = "g'" + DBCS + DBCS + DBCS + DBCS + DBCS + "'";
        String[] result = formatter.format(new TextLinesInput("dcl-c GRAPHIC_CONSTANT " + literal + ";"), 0).toLines();

        assertEquals(0, formatter.getErrorCount());
        assertArrayEquals(new String[] { "dcl-c GRAPHIC_CONSTANT", "  " + literal + ";" }, result);
    }

    private void assertFormatted(String source, String expected) throws RpgleFormatterException {

        String[] result = formatter.format(new TextLinesInput(source), 0).toLines();

        assertEquals(source, 0, formatter.getErrorCount());
        assertArrayEquals(source, new String[] { expected }, result);
    }
}
