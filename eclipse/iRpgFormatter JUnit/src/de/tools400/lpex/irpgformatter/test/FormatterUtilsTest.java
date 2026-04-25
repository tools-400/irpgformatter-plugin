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

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.preferences.ParameterSpacingStyle;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;

public class FormatterUtilsTest extends AbstractTestCase {

    @Test
    public void format_name_short() throws RpgleFormatterException {
        String startingLine = "";
        int defaultIndent = 0;
        String name = "myShortName";
        IToken[] tokens = getTokenizer().tokenize(name);
        String[] results = getFormatterUtils().formatTokens(startingLine, tokens, defaultIndent, 25);
        assertEquals(1, results.length);
        assertEquals("myShortName", results[0]);
    }

    @Test
    public void format_name_short_with_default_idention() throws RpgleFormatterException {
        String startingLine = "";
        int defaultIndent = 2;
        String name = "myShortName";
        IToken[] tokens = getTokenizer().tokenize(name);
        String[] results = getFormatterUtils().formatTokens(startingLine, tokens, defaultIndent, 25);
        assertEquals(1, results.length);
        assertEquals("  myShortName", results[0]);
    }

    @Test
    public void format_name_short_with_line() throws RpgleFormatterException {
        String startingLine = "foobar(";
        int defaultIndent = 0;
        String name = "myShortName";
        IToken[] tokens = getTokenizer().tokenize(name);
        String[] results = getFormatterUtils().formatTokens(startingLine, tokens, defaultIndent, 25);
        assertEquals(1, results.length);
        assertEquals("foobar(myShortName", results[0]);
    }

    @Test
    public void format_name_long() throws RpgleFormatterException {

        String startingLine = "";
        int defaultIndent = 0;

        String name = "myLongNameThatRequiresMultipleLinesBecauseItIsSoLong";
        IToken[] tokens = getTokenizer().tokenize(name);
        String[] results = getFormatterUtils().formatTokens(startingLine, tokens, defaultIndent, 25);

        assertEquals(3, results.length);
        assertEquals("myLongNameThatRequires...", results[0]);
        assertEquals("  MultipleLinesBecause...", results[1]);
        assertEquals("  ItIsSoLong", results[2]);
    }

    @Test
    public void format_name_long_with_line() throws RpgleFormatterException {

        getFormatterUtils().getConfig().setMinLiteralLength(10);

        String startingLine = "foobar(";
        int defaultIndent = 0;

        String name = "myLongNameThatRequiresMultipleLinesBecauseItIsSoLong";
        IToken[] tokens = getTokenizer().tokenize(name);
        String[] results = getFormatterUtils().formatTokens(startingLine, tokens, defaultIndent, 25);

        assertEquals(3, results.length);
        assertEquals("foobar(myLongNameThatR...", results[0]);
        assertEquals("  equiresMultipleLines...", results[1]);
        assertEquals("  BecauseItIsSoLong", results[2]);
    }

    @Test
    public void format_literal_long() throws RpgleFormatterException {

        String startingLine = "";
        int defaultIndent = 0;
        String literal = "'myLongLiteralThatRequiresMultipleLinesBecauseItIsSoLong'";
        IToken[] tokens = getTokenizer().tokenize(literal);
        String[] results = getFormatterUtils().formatTokens(startingLine, tokens, defaultIndent, 25);
        assertEquals(3, results.length);
        assertEquals("'myLongLiteralThatRequir+", results[0]);
        assertEquals("  esMultipleLinesBecause+", results[1]);
        assertEquals("  ItIsSoLong'", results[2]);
    }

    @Test
    public void format_literal_long_with_line() throws RpgleFormatterException {

        getFormatterUtils().getConfig().setUseConstKeyword(true);

        String startingLine = "";
        int defaultIndent = 0;

        String literal = "const('myLongLiteralThatRequiresMultipleLinesBecauseItIsSoLong')";
        IToken[] tokens = getTokenizer().tokenize(literal);
        String[] results = getFormatterUtils().formatTokens(startingLine, tokens, defaultIndent, 25);

        assertEquals(4, results.length);
        assertEquals("const(", results[0]);
        assertEquals("  'myLongLiteralThatRequ+", results[1]);
        assertEquals("  iresMultipleLinesBecau+", results[2]);
        assertEquals("  seItIsSoLong')", results[3]);
    }

    /* formatKeywordWithParameters() */

    @Test
    public void format_function_one_line() throws RpgleFormatterException {
        String startingLine = "before ";
        int defaultIndent = 0;
        String function = "myFunction('myLiteral' : myName)";
        IToken[] tokens = getTokenizer().tokenize(function);
        String[] results = getFormatterUtils().formatTokens(startingLine, tokens, defaultIndent, 80);
        assertEquals(1, results.length);
        assertEquals("before myFunction('myLiteral': myName)", results[0]);
    }

    @Test
    public void format_function_break_after_first_parameter() throws RpgleFormatterException {
        String startingLine = "before ";
        int defaultIndent = 0;
        String function = "myFunction('myLiteral' : myName)";
        IToken[] tokens = getTokenizer().tokenize(function);
        String[] results = getFormatterUtils().formatTokens(startingLine, tokens, defaultIndent, 32);
        assertEquals(2, results.length);
        assertEquals("before myFunction('myLiteral':", results[0]);
        assertEquals("  myName)", results[1]);
    }

    @Test
    public void format_function_break_before_keyword_start_literal_on_next_line() throws RpgleFormatterException {

        // The literal begins on the next line because the first part of the
        // literal is less than the minimum length of a literal.

        getFormatterUtils().getConfig().setMinLiteralLength(2);
        getFormatterUtils().getConfig().setBreakBeforeKeyword(true);

        String startingLine = "before ";
        int defaultIndent = 0;
        String function = "myFunction('myLiteral' : myName)";
        IToken[] tokens = getTokenizer().tokenize(function);
        String[] results = getFormatterUtils().formatTokens(startingLine, tokens, defaultIndent, 15);
        assertEquals(4, results.length);
        assertEquals("before", results[0]);
        assertEquals("  myFunction(", results[1]);
        assertEquals("  'myLiteral':", results[2]);
        assertEquals("  myName)", results[3]);
    }

    @Test
    public void format_function_break_before_keyword_semicolon_on_separate_line() throws RpgleFormatterException {
        String startingLine = "before ";
        int defaultIndent = 0;
        String function = "myFunction('myLiteral' : myLongerName);";
        IToken[] tokens = getTokenizer().tokenize(function);
        String[] results = getFormatterUtils().formatTokens(startingLine, tokens, defaultIndent, 15);
        assertEquals(5, results.length);
        assertEquals("before", results[0]);
        assertEquals("  myFunction(", results[1]);
        assertEquals("  'myLiteral':", results[2]);
        assertEquals("  myLongerName)", results[3]);
        assertEquals("  ;", results[4]);
    }

    @Test
    public void format_function_with_long_name_parameter() throws RpgleFormatterException {

        String startingLine = "before ";
        int defaultIndent = 0;

        String function = "myFunction('myLiteral' : myVeryLongNameThatMustBeSplittedAcrossSeveralLines : 123)";
        IToken[] tokens = getTokenizer().tokenize(function);
        String[] results = getFormatterUtils().formatTokens(startingLine, tokens, defaultIndent, 15);

        assertEquals(9, results.length);
        assertEquals("before", results[0]);
        assertEquals("  myFunction(", results[1]);
        assertEquals("  'myLiteral':", results[2]);
        assertEquals("  myVeryLong...", results[3]);
        assertEquals("  NameThatMu...", results[4]);
        assertEquals("  stBeSplitt...", results[5]);
        assertEquals("  edAcrossSe...", results[6]);
        assertEquals("  veralLines:", results[7]);
        assertEquals("  123)", results[8]);
    }

    @Test
    public void format_function_delimiter_beforeParameter() throws RpgleFormatterException {

        getFormatterConfig().setDelimiterBeforeParameter(true);

        String startingLine = "before ";
        int defaultIndent = 0;

        String function = "myFunction('myLiteral' : myVeryLongNameThatMustBeSplittedAcrossSeveralLines : 123)";
        IToken[] tokens = getTokenizer().tokenize(function);
        String[] results = getFormatterUtils().formatTokens(startingLine, tokens, defaultIndent, 15);

        assertEquals(9, results.length);
        assertEquals("before", results[0]);
        assertEquals("  myFunction(", results[1]);
        assertEquals("  'myLiteral'", results[2]);
        assertEquals("  : myVeryLo...", results[3]);
        assertEquals("  ngNameThat...", results[4]);
        assertEquals("  MustBeSpli...", results[5]);
        assertEquals("  ttedAcross...", results[6]);
        assertEquals("  SeveralLines", results[7]);
        assertEquals("  : 123)", results[8]);
    }

    @Test
    public void format_function_with_long_name_parameter_with_default_indent() throws RpgleFormatterException {

        String startingLine = "before ";
        int defaultIndent = 2;
        String function = "myFunction('myLitera' : myVeryLongNameThatMustBeSplittedAcrossSeveralLines : 123)";
        IToken[] tokens = getTokenizer().tokenize(function);
        String[] results = getFormatterUtils().formatTokens(startingLine, tokens, defaultIndent, 15);

        for (String line : results) {
            System.out.println(line);
        }

        assertEquals(10, results.length);
        assertEquals("  before", results[0]);
        assertEquals("    myFunction(", results[1]);
        assertEquals("    'myLitera':", results[2]);
        assertEquals("    myVeryLo...", results[3]);
        assertEquals("    ngNameTh...", results[4]);
        assertEquals("    atMustBe...", results[5]);
        assertEquals("    Splitted...", results[6]);
        assertEquals("    AcrossSe...", results[7]);
        assertEquals("    veralLin...", results[8]);
        assertEquals("    es: 123)", results[9]);
    }

    // --- buildParameters - single parameter tests ---

    @Test
    public void testSpaceBeforeSingleParameter_delimiterAfter() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(false);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.BEFORE);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals("*varsize", parameters);
    }

    @Test
    public void testSpaceAfterSingleParameter_delimiterAfter() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(false);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.AFTER);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals("*varsize", parameters);
    }

    @Test
    public void testSpaceBeforeAfterSingleParameter_delimiterAfter() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(false);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.BOTH);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals(" *varsize ", parameters);
    }

    @Test
    public void testSpaceBeforeSingleParameter_delimiterBefore() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(true);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.BEFORE);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals("*varsize", parameters);
    }

    @Test
    public void testSpaceAfterSingleParameter_delimiterBefore() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(true);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.AFTER);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals("*varsize", parameters);
    }

    @Test
    public void testSpaceBeforeAfterSingleParameter_delimiterBefore() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(true);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.BOTH);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals(" *varsize ", parameters);
    }

    @Test
    public void testNoSpaceSingleParameter_delimiterBefore() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(true);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.NONE);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals("*varsize", parameters);
    }

    @Test
    public void testNoSpaceSingleParameter_delimiterAfter() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(false);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.NONE);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals("*varsize", parameters);
    }

    @Test
    public void testNoSpaceParameters_delimiterBefore() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(true);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.NONE);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize:*trim:*string)");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals("*varsize:*trim:*string", parameters);
    }

    @Test
    public void testNoSpaceParameters_delimiterAfter() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(false);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.NONE);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize:*trim:*string)");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals("*varsize:*trim:*string", parameters);
    }

    // --- buildParameters - multiple parameters tests ---

    @Test
    public void testSpaceBeforeParameters_delimiterAfter() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(false);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.BEFORE);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize:*trim:*string)");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals("*varsize: *trim: *string", parameters);
    }

    @Test
    public void testSpaceAfterParameters_delimiterAfter() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(false);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.AFTER);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize:*trim:*string)");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals("*varsize :*trim :*string", parameters);
    }

    @Test
    public void testSpaceBeforeAfterParameters_delimiterAfter() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(false);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.BOTH);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize:*trim:*string)");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals(" *varsize : *trim : *string ", parameters);
    }

    @Test
    public void testSpaceBeforeParameters_delimiterBefore() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(true);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.BEFORE);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize:*trim:*string)");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals("*varsize: *trim: *string", parameters);
    }

    @Test
    public void testSpaceAfterParameters_delimiterBefore() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(true);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.AFTER);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize:*trim:*string)");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals("*varsize :*trim :*string", parameters);
    }

    @Test
    public void testSpaceBeforeAfterParameters_delimiterBefore() throws RpgleFormatterException {
        getFormatterConfig().setDelimiterBeforeParameter(true);
        getFormatterConfig().setParameterSpacingStyle(ParameterSpacingStyle.BOTH);

        IToken[] tokens = getTokenizer().tokenize("options(*varsize:*trim:*string)");
        String parameters = getFormatterUtils().buildParameters(tokens[0].getChildren());
        assertEquals(" *varsize : *trim : *string ", parameters);
    }

    // --- sortConstValueToEnd tests ---

    @Test
    public void testSortConstToEnd() throws RpgleFormatterException {
        getFormatterConfig().setSortConstValueToEnd(true);
        IToken[] tokens = getTokenizer().tokenize("myParam const char(10);");
        tokens = getFormatterUtils().sortConstValueToEnd(tokens);
        assertEquals(TokenType.NAME, tokens[0].getType());
        assertEquals(TokenType.DATA_TYPE, tokens[1].getType());
        assertEquals(TokenType.KEYWORD, tokens[2].getType());
        assertEquals("const", tokens[2].getValue().toLowerCase());
        assertEquals(TokenType.EOL, tokens[3].getType());
    }

    @Test
    public void testSortValueToEnd() throws RpgleFormatterException {
        getFormatterConfig().setSortConstValueToEnd(true);
        IToken[] tokens = getTokenizer().tokenize("myParam value packed(7:2);");
        tokens = getFormatterUtils().sortConstValueToEnd(tokens);
        assertEquals(TokenType.NAME, tokens[0].getType());
        assertEquals(TokenType.DATA_TYPE, tokens[1].getType());
        assertEquals(TokenType.KEYWORD, tokens[2].getType());
        assertEquals("value", tokens[2].getValue().toLowerCase());
        assertEquals(TokenType.EOL, tokens[3].getType());
    }

    @Test
    public void testSortConstToEnd_alreadyAtEnd() throws RpgleFormatterException {
        getFormatterConfig().setSortConstValueToEnd(true);
        IToken[] tokens = getTokenizer().tokenize("myParam char(10) const;");
        TokenType[] before = getTokenTypes(tokens);
        tokens = getFormatterUtils().sortConstValueToEnd(tokens);
        TokenType[] after = getTokenTypes(tokens);
        assertArrayEquals(before, after);
    }

    @Test
    public void testSortConstToEnd_beforeOtherKeywords() throws RpgleFormatterException {
        getFormatterConfig().setSortConstValueToEnd(true);
        IToken[] tokens = getTokenizer().tokenize("myParam const char(10) options(*nopass);");
        tokens = getFormatterUtils().sortConstValueToEnd(tokens);
        assertEquals(TokenType.NAME, tokens[0].getType());
        assertEquals(TokenType.DATA_TYPE, tokens[1].getType());
        assertEquals(TokenType.KEYWORD, tokens[2].getType());
        assertEquals("options", tokens[2].getValue().toLowerCase());
        assertEquals(TokenType.KEYWORD, tokens[3].getType());
        assertEquals("const", tokens[3].getValue().toLowerCase());
        assertEquals(TokenType.EOL, tokens[4].getType());
    }

    @Test
    public void testSortConstToEnd_disabled() throws RpgleFormatterException {
        getFormatterConfig().setSortConstValueToEnd(false);
        IToken[] tokens = getTokenizer().tokenize("myParam const char(10);");
        TokenType[] before = getTokenTypes(tokens);
        tokens = getFormatterUtils().sortConstValueToEnd(tokens);
        TokenType[] after = getTokenTypes(tokens);
        assertArrayEquals(before, after);
    }

    @Test
    public void testSortConstToEnd_noConstValue() throws RpgleFormatterException {
        getFormatterConfig().setSortConstValueToEnd(true);
        IToken[] tokens = getTokenizer().tokenize("myParam char(10);");
        TokenType[] before = getTokenTypes(tokens);
        tokens = getFormatterUtils().sortConstValueToEnd(tokens);
        TokenType[] after = getTokenTypes(tokens);
        assertArrayEquals(before, after);
    }

    private static TokenType[] getTokenTypes(IToken[] tokens) {
        TokenType[] types = new TokenType[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            types[i] = tokens[i].getType();
        }
        return types;
    }
}
