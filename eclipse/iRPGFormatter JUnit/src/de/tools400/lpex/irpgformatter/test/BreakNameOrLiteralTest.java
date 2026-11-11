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

import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.LineOverflowException;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.formatter.UnexpectedTokenException;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

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

    // --- no continuation character, that nothing continues ---

    /**
     * The literal fits into the line with "+", but not with the suffix: the
     * closing quote moves to the next line, instead of "'...'+" without the
     * suffix.
     */
    @Test
    public void breakLiteral_firstPartLeavesClosingQuoteForSuffix() throws RpgleFormatterException {

        // @formatter:off
        assertArrayEquals(new String[] {
            "inz('abcdefghijklmnopqr+",
            "  ');" },
            breakLiteral("inz(", "'abcdefghijklmnopqr'", 25, "", ");"));
        // @formatter:on
    }

    /**
     * The rest of the literal fits as a middle part, but not as a last part
     * limited by the maximum name length: it becomes the last part, instead of
     * ending with "'+".
     */
    @Test
    public void breakLiteral_restIsLastPart() throws RpgleFormatterException {

        String letters = letters(117);

        // @formatter:off
        assertArrayEquals(new String[] {
            "'" + letters.substring(0, 59) + "+",
            "  " + letters.substring(59) + "'" },
            breakLiteral("", "'" + letters + "'", 62, "", ""));
        // @formatter:on
    }

    /**
     * Same for a name: the rest becomes the last part with the suffix, instead
     * of ending with "..." and losing the suffix.
     */
    @Test
    public void breakName_restIsLastPart() throws RpgleFormatterException {

        String letters = letters(117);

        // @formatter:off
        assertArrayEquals(new String[] {
            letters.substring(0, 60) + "...",
            "  " + letters.substring(60) + " :" },
            breakName("", letters, 64, "", " :"));
        // @formatter:on
    }

    /**
     * For all lengths of the value, of the line and all suffixes: the last part
     * ends with the suffix, never with the continuation character.
     */
    @Test
    public void breakNameOrLiteral_lastPartNeverEndsWithContinuation() throws RpgleFormatterException {

        String[] lines = { "", "inz(", "  dcl-s x " };
        String[] prefixes = { "", " " };
        String[] suffixes = { "", ")", ");", " :", ": " };
        int[] maxLineLengths = { 20, 40, 62, 64, 80, 100 };

        for (boolean breakOnCaseChange : new boolean[] { false, true }) {
            getFormatterConfig().setBreakBetweenCaseChange(breakOnCaseChange);
            for (int length = 1; length <= 120; length++) {
                for (String line : lines) {
                    for (String prefix : prefixes) {
                        for (String suffix : suffixes) {
                            for (int maxLineLength : maxLineLengths) {
                                String literal = "'" + letters(length) + "'";
                                assertLastPart(breakLiteral(line, literal, maxLineLength, prefix, suffix), "+", suffix, literal);
                                String name = letters(length);
                                assertLastPart(breakName(line, name, maxLineLength, prefix, suffix), "...", suffix, name);
                            }
                        }
                    }
                }
            }
        }
    }

    private void assertLastPart(String[] parts, String continuation, String suffix, String value) {

        String lastPart = parts[parts.length - 1];
        assertFalse(value + " -> " + lastPart, StringUtils.trimR(lastPart).endsWith(continuation));
        assertTrue(value + " -> " + lastPart, StringUtils.trimR(lastPart).endsWith(StringUtils.trimR(suffix)));
    }

    /**
     * Returns lower-case letters, which neither contain a blank nor a case
     * change, so that a part ends at the maximum length.
     */
    private String letters(int length) {

        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < length; i++) {
            buffer.append((char)('a' + i % 26));
        }

        return buffer.toString();
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
