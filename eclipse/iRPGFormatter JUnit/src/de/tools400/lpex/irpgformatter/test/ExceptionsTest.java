/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.BlockStructureException;
import de.tools400.lpex.irpgformatter.formatter.CannotAddLineSegmentException;
import de.tools400.lpex.irpgformatter.formatter.IncompleteStatementException;
import de.tools400.lpex.irpgformatter.formatter.LineOverflowException;
import de.tools400.lpex.irpgformatter.formatter.TokenNotFoundException;
import de.tools400.lpex.irpgformatter.formatter.UnexpectedErrorException;
import de.tools400.lpex.irpgformatter.formatter.UnexpectedStatementTypeException;
import de.tools400.lpex.irpgformatter.formatter.UnexpectedTokenException;
import de.tools400.lpex.irpgformatter.parser.StatementType;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.SpecialWordToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;

public class ExceptionsTest {

    @BeforeClass
    public static void setupTestSuite() {
        Locale.setDefault(new Locale("en", "US"));
    }

    @Test
    public void testCannotAddLineSegmentException() {
        try {
            throw new CannotAddLineSegmentException(10);
        } catch (Exception e) {
            assertEquals("Error on line #10: Cannot add line segment. Line is already complete.", e.getLocalizedMessage());
        }
    }

    @Test
    public void testIncompleteStatementException() {
        try {
            throw new IncompleteStatementException(10, "dcl-c MY_CONSTANT");
        } catch (Exception e) {
            assertEquals("Error on line #10: Incomplete statement: dcl-c MY_CONSTANT", e.getLocalizedMessage());
        }
    }

    @Test
    public void testLineOverflowException() {
        try {
            throw new LineOverflowException("dim(10)", 10);
        } catch (Exception e) {
            assertEquals("Error on line #10: Line too small for value: dim(10)", e.getLocalizedMessage());
        }
    }

    /**
     * A long value, such as a hexadecimal literal, is shortened to 40
     * characters, so that the message fits into one line of the view
     * iRPGFormatter Problems.
     */
    @Test
    public void testLineOverflowException_longValueIsShortened() {
        String value = "x'000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F'";
        LineOverflowException e = new LineOverflowException(value, 566);
        assertEquals("Error on line #566: Line too small for value: x'000102030405060708090A0B0C0D0E0F101112…",
            e.getLocalizedMessage());
    }

    @Test
    public void testLineOverflowException_valueOf40CharactersIsNotShortened() {
        String value = "x'000102030405060708090A0B0C0D0E0F101112'";
        assertEquals(41, value.length());
        String value40 = value.substring(0, 40);
        LineOverflowException e = new LineOverflowException(value40, 1);
        assertEquals("Error on line #1: Line too small for value: " + value40, e.getLocalizedMessage());
    }

    @Test
    public void testBlockStructureException() {
        String[] messages = { "Error on line #2: DCL-DS ds is not closed before END-PROC of line #4.",
            "Error on line #10: DCL-PROC p2 is not closed by END-PROC." };
        BlockStructureException e = new BlockStructureException(messages, new int[] { 2, 10 });
        assertEquals(messages[0], e.getLocalizedMessage());
        assertEquals(2, e.getErrorCount());
        assertEquals(2, e.getFirstLineNumber());
        assertArrayEquals(messages, e.getErrorMessages());
        assertArrayEquals(new int[] { 2, 10 }, e.getLineNumbers());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlockStructureException_oneLineNumberPerMessage() {
        new BlockStructureException(new String[] { "message 1", "message 2" }, new int[] { 1 });
    }

    @Test
    public void testTokenNotFoundException() {
        try {
            throw new TokenNotFoundException(TokenType.KEYWORD.name(), 10);
        } catch (Exception e) {
            assertEquals("Error on line #10: Token of type KEYWORD not found.", e.getLocalizedMessage());
        }
    }

    @Test
    public void testUnexpectedErrorException() {
        try {
            throw new UnexpectedErrorException("Unexpeted error message", 10);
        } catch (Exception e) {
            assertEquals("Error on line #10: Unexpected error: Unexpeted error message", e.getLocalizedMessage());
        }
    }

    @Test
    public void testUnexpectedStatementTypeException() {
        try {
            throw new UnexpectedStatementTypeException(StatementType.DCL_PI, 10, "dcl-pi myFunction;");
        } catch (Exception e) {
            assertEquals("Error on line #10: Unexpected statement of type: DCL_PI -> dcl-pi myFunction;", e.getLocalizedMessage());
        }
    }

    @Test
    public void testUnexpectedTokenException() {
        try {
            IToken token = new SpecialWordToken("*Varsize", "*Varsize", 0);
            throw new UnexpectedTokenException(token, 10);
        } catch (Exception e) {
            assertEquals("Error on line #10: Unexpected token of type: SPECIAL_WORD -> *Varsize", e.getLocalizedMessage());
        }
    }
}
