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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.statements.SortConstValueToEndRule;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;

public class SortConstValueToEndRuleTest extends AbstractTestCase {

    private SortConstValueToEndRule createRule(boolean enabled) {

        FormatterConfig config = getFormatterConfig();
        config.setSortConstValueToEnd(enabled);

        return new SortConstValueToEndRule(config);
    }

    @Test
    public void testSortConstToEnd() throws RpgleFormatterException {
        SortConstValueToEndRule rule = createRule(true);
        IToken[] tokens = getTokenizer().tokenize("myParam const char(10);");
        tokens = rule.apply(tokens);
        assertEquals(TokenType.NAME, tokens[0].getType());
        assertEquals(TokenType.DATA_TYPE, tokens[1].getType());
        assertEquals(TokenType.KEYWORD, tokens[2].getType());
        assertEquals("const", tokens[2].getValue().toLowerCase());
        assertEquals(TokenType.EOL, tokens[3].getType());
    }

    @Test
    public void testSortValueToEnd() throws RpgleFormatterException {
        SortConstValueToEndRule rule = createRule(true);
        IToken[] tokens = getTokenizer().tokenize("myParam value packed(7:2);");
        tokens = rule.apply(tokens);
        assertEquals(TokenType.NAME, tokens[0].getType());
        assertEquals(TokenType.DATA_TYPE, tokens[1].getType());
        assertEquals(TokenType.KEYWORD, tokens[2].getType());
        assertEquals("value", tokens[2].getValue().toLowerCase());
        assertEquals(TokenType.EOL, tokens[3].getType());
    }

    @Test
    public void testSortConstToEnd_alreadyAtEnd() throws RpgleFormatterException {
        SortConstValueToEndRule rule = createRule(true);
        IToken[] tokens = getTokenizer().tokenize("myParam char(10) const;");
        TokenType[] before = getTokenTypes(tokens);
        tokens = rule.apply(tokens);
        TokenType[] after = getTokenTypes(tokens);
        assertArrayEquals(before, after);
    }

    @Test
    public void testSortConstToEnd_beforeOtherKeywords() throws RpgleFormatterException {
        SortConstValueToEndRule rule = createRule(true);
        IToken[] tokens = getTokenizer().tokenize("myParam const char(10) options(*nopass);");
        tokens = rule.apply(tokens);
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
        SortConstValueToEndRule rule = createRule(false);
        IToken[] tokens = getTokenizer().tokenize("myParam const char(10);");
        TokenType[] before = getTokenTypes(tokens);
        tokens = rule.apply(tokens);
        TokenType[] after = getTokenTypes(tokens);
        assertArrayEquals(before, after);
    }

    @Test
    public void testSortConstToEnd_noConstValue() throws RpgleFormatterException {
        SortConstValueToEndRule rule = createRule(true);
        IToken[] tokens = getTokenizer().tokenize("myParam char(10);");
        TokenType[] before = getTokenTypes(tokens);
        tokens = rule.apply(tokens);
        TokenType[] after = getTokenTypes(tokens);
        assertArrayEquals(before, after);
    }

    @Test
    public void nullTokens_areReturnedUnchanged() {
        SortConstValueToEndRule rule = createRule(true);
        assertSame(null, rule.apply(null));
    }

    @Test
    public void emptyTokens_areReturnedUnchanged() {
        SortConstValueToEndRule rule = createRule(true);
        IToken[] empty = new IToken[0];
        IToken[] result = rule.apply(empty);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    private static TokenType[] getTokenTypes(IToken[] tokens) {
        TokenType[] types = new TokenType[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            types[i] = tokens[i].getType();
        }
        return types;
    }
}
