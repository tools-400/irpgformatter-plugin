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
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.rules.FormattingRules;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;

/**
 * Unit tests for {@link FormattingRules}.
 */
public class FormattingRulesTest extends AbstractTestCase {

    // --- removeConstWrapper tests ---

    @Test
    public void removeConstWrapper_removeConst() {
        assertEquals("1", FormattingRules.removeConstWrapper("const(1)"));
    }

    @Test
    public void removeConstWrapper_removeConstWithSpaces() {
        assertEquals("100", FormattingRules.removeConstWrapper("const( 100 )"));
    }

    @Test
    public void removeConstWrapper_removeConstUppercase() {
        assertEquals("'hello'", FormattingRules.removeConstWrapper("CONST('hello')"));
    }

    @Test
    public void removeConstWrapper_removeConstMixedCase() {
        assertEquals("42", FormattingRules.removeConstWrapper("Const(42)"));
    }

    @Test
    public void removeConstWrapper_noConstWrapper() {
        assertEquals("123", FormattingRules.removeConstWrapper("123"));
    }

    @Test
    public void removeConstWrapper_stringLiteral() {
        assertEquals("'test'", FormattingRules.removeConstWrapper("'test'"));
    }

    @Test
    public void removeConstWrapper_expressionInConst() {
        assertEquals("1 + 2", FormattingRules.removeConstWrapper("const(1 + 2)"));
    }

    @Test
    public void removeConstWrapper_nestedParens() {
        assertEquals("func(x)", FormattingRules.removeConstWrapper("const(func(x))"));
    }

    // --- createIndent tests ---

    @Test
    public void createIndent_level0() {
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        assertEquals("", rules.createIndent(0));
    }

    @Test
    public void createIndent_level1() {
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        assertEquals("  ", rules.createIndent(1));
    }

    @Test
    public void createIndent_level2() {
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        assertEquals("    ", rules.createIndent(2));
    }

    @Test
    public void createIndent_level3() {
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        assertEquals("      ", rules.createIndent(3));
    }

    @Test
    public void createIndent_lengthIsLevelTimesIndentSize() {
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        for (int level = 0; level <= 5; level++) {
            String indent = rules.createIndent(level);
            assertEquals(level * getIndentSize(), indent.length());
        }
    }

    // --- format single parameter tests ---

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

    // --- format multiple parameters tests ---

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

    // --- INDENT_SIZE constant test ---

    @Test
    public void indentSizeIs2() {
        assertEquals(2, getIndentSize());
    }

    @Test
    public void combined_dclCWithConst() {
        String constValue = FormattingRules.removeConstWrapper("const(100)");
        assertEquals("100", constValue);
    }

    private int getIndentSize() {
        return Preferences.getInstance().getIndent();
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
