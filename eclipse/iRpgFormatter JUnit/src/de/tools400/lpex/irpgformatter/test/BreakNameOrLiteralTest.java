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

import de.tools400.lpex.irpgformatter.formatter.LineOverflowException;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.formatter.UnexpectedTokenException;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;

/**
 * Pins the behaviour of {@code FormatterUtils.breakName()} and
 * {@code breakLiteral()}: a name or literal that does not fit into the line is
 * split into parts, that end with "..." or "+". Default settings: maximum name
 * length 60, minimum length 10, no break on case change.
 */
public class BreakNameOrLiteralTest extends AbstractTestCase {

    @Test
    public void breakName_fitsIntoLine() throws RpgleFormatterException {
        assertArrayEquals(new String[] { "foo(myName)" }, breakName("foo(", "myName", 25, "", ")"));
    }

    @Test
    public void breakName_atMaximumLength() throws RpgleFormatterException {

        // @formatter:off
        assertArrayEquals(new String[] {
            "myLongNameThatRequires...",
            "  MultipleLines" },
            breakName("", "myLongNameThatRequiresMultipleLines", 25, "", ""));
        // @formatter:on
    }

    @Test
    public void breakName_onCaseChange() throws RpgleFormatterException {

        getFormatterConfig().setBreakBetweenCaseChange(true);

        // @formatter:off
        assertArrayEquals(new String[] {
            "myLongNameThat...",
            "  Requires...",
            "  MultipleLines" },
            breakName("", "myLongNameThatRequiresMultipleLines", 20, "", ""));
        // @formatter:on
    }

    @Test
    public void breakName_onCaseChangeWithoutCaseChange() throws RpgleFormatterException {

        getFormatterConfig().setBreakBetweenCaseChange(true);

        // @formatter:off
        assertArrayEquals(new String[] {
            "abcdefghijklmnopq...",
            "  rstuvwxyz" },
            breakName("", "abcdefghijklmnopqrstuvwxyz", 20, "", ""));
        // @formatter:on
    }

    /**
     * No room for the prefix and the continuation: the name starts on a new
     * line, without the leading blank of the prefix.
     */
    @Test
    public void breakName_noRoomStartsNewLine() throws RpgleFormatterException {

        // @formatter:off
        assertArrayEquals(new String[] {
            "dcl-s abcdefghijklmnopqrst",
            "  myName;" },
            breakName("dcl-s abcdefghijklmnopqrst", "myName", 25, " ", ";"));
        // @formatter:on
    }

    /**
     * The first part would be shorter than the minimum length: the name starts
     * on a new line.
     */
    @Test
    public void breakName_firstPartTooShortStartsNewLine() throws RpgleFormatterException {

        // @formatter:off
        assertArrayEquals(new String[] {
            "foobar(abcdefghij",
            "  myLongNameThatRequir...",
            "  esMultipleLines)" },
            breakName("foobar(abcdefghij", "myLongNameThatRequiresMultipleLines", 25, "", ")"));
        // @formatter:on
    }

    /**
     * A part is not longer than the maximum name length, even if the line has
     * room for more.
     */
    @Test
    public void breakName_limitedByMaximumNameLength() throws RpgleFormatterException {

        String name = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";

        // @formatter:off
        assertArrayEquals(new String[] {
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefgh...",
            "  ijklmnopqrstuvwxyz;" },
            breakName("", name, 100, "", ";"));
        // @formatter:on
    }

    @Test(expected = LineOverflowException.class)
    public void breakName_lineTooShort() throws RpgleFormatterException {
        breakName("", "myLongName", 4, "", "");
    }

    @Test
    public void breakLiteral_fitsIntoLine() throws RpgleFormatterException {
        assertArrayEquals(new String[] { "inz('abc')" }, breakLiteral("inz(", "'abc'", 25, "", ")"));
    }

    @Test
    public void breakLiteral_atBlank() throws RpgleFormatterException {

        // @formatter:off
        assertArrayEquals(new String[] {
            "x = 'abc def ghi +",
            "  jkl mno pqr stu';" },
            breakLiteral("x = ", "'abc def ghi jkl mno pqr stu'", 20, "", ";"));
        // @formatter:on
    }

    @Test
    public void breakLiteral_lastPartEndsWithSuffix() throws RpgleFormatterException {

        // @formatter:off
        assertArrayEquals(new String[] {
            "inz('abcdefghijklmnopqrs+",
            "  tuvwxyzabcdefgh');" },
            breakLiteral("inz(", "'abcdefghijklmnopqrstuvwxyzabcdefgh'", 25, "", ");"));
        // @formatter:on
    }

    @Test(expected = UnexpectedTokenException.class)
    public void breakLiteral_rejectsKeyword() throws RpgleFormatterException {
        breakLiteral("", "const", 25, "", "");
    }

    private String[] breakName(String line, String name, int maxLineLength, String prefix, String suffix) throws RpgleFormatterException {
        return getFormatterUtils().breakName(line, token(name, TokenType.NAME), maxLineLength, prefix, suffix);
    }

    private String[] breakLiteral(String line, String value, int maxLineLength, String prefix, String suffix) throws RpgleFormatterException {
        return getFormatterUtils().breakLiteral(line, token(value, null), maxLineLength, prefix, suffix);
    }

    /**
     * Returns the first token of the value; checks its type, if one is given.
     */
    private IToken token(String value, TokenType expectedType) throws RpgleFormatterException {

        IToken token = getTokenizer().tokenize(value)[0];
        if (expectedType != null) {
            assertEquals(expectedType, token.getType());
        }

        return token;
    }
}
