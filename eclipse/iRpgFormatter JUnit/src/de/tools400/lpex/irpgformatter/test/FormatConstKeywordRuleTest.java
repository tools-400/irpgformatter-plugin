/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.statements.FormatConstKeywordRule;
import de.tools400.lpex.irpgformatter.tokenizer.DeclToken;
import de.tools400.lpex.irpgformatter.tokenizer.EolToken;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.KeywordToken;
import de.tools400.lpex.irpgformatter.tokenizer.LiteralToken;
import de.tools400.lpex.irpgformatter.tokenizer.NameToken;
import de.tools400.lpex.irpgformatter.tokenizer.OtherToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;

public class FormatConstKeywordRuleTest extends AbstractTestCase {

    private FormatConstKeywordRule createRule(boolean useConstKeyword) {

        FormatterConfig config = getFormatterConfig();
        config.setUseConstKeyword(useConstKeyword);

        return new FormatConstKeywordRule(config);
    }

    // --- useConstKeyword = true: wrap missing const() ---

    @Test
    public void useConstTrue_plainNumeric_isWrappedInConst() {
        OtherToken value = new OtherToken("100", "100", 14);
        IToken[] tokens = dclC(value);

        IToken[] result = createRule(true).apply(tokens);

        IToken wrapped = result[2];
        assertEquals(TokenType.KEYWORD, wrapped.getType());
        assertEquals("const", wrapped.getValue());
        assertEquals("const(100)", wrapped.getRawValue());
        assertEquals(1, wrapped.getNumChildren());
        assertSame(value, wrapped.getChild(0));
    }

    @Test
    public void useConstTrue_literalValue_isWrappedInConst() {
        LiteralToken value = new LiteralToken("'hello'", "'hello'", 14);
        IToken[] tokens = dclC(value);

        IToken[] result = createRule(true).apply(tokens);

        assertEquals(TokenType.KEYWORD, result[2].getType());
        assertEquals("const('hello')", result[2].getRawValue());
    }

    @Test
    public void useConstTrue_alreadyWrapped_isLeftUntouched() {
        IToken value = constWrapping(new OtherToken("100", "100", 0));
        IToken[] tokens = dclC(value);

        IToken[] result = createRule(true).apply(tokens);

        assertSame(value, result[2]);
    }

    @Test
    public void useConstTrue_alreadyWrappedUpperCase_isLeftUntouched() {
        IToken value = new KeywordToken("CONST", "CONST(100)", 0);
        value.addChild(new OtherToken("100", "100", 6));
        IToken[] tokens = dclC(value);

        IToken[] result = createRule(true).apply(tokens);

        assertSame(value, result[2]);
    }

    // --- useConstKeyword = false: unwrap existing const() ---

    @Test
    public void useConstFalse_constWrapped_isUnwrapped() {
        IToken inner = new OtherToken("100", "100", 0);
        IToken value = constWrapping(inner);
        IToken[] tokens = dclC(value);

        IToken[] result = createRule(false).apply(tokens);

        assertSame(inner, result[2]);
    }

    @Test
    public void useConstFalse_alreadyPlainValue_isLeftUntouched() {
        OtherToken value = new OtherToken("100", "100", 14);
        IToken[] tokens = dclC(value);

        IToken[] result = createRule(false).apply(tokens);

        assertSame(value, result[2]);
    }

    @Test
    public void useConstFalse_constWithoutChild_isLeftUntouched() {
        // Defensive: a const keyword token with no children is malformed but
        // must not crash the rule.
        KeywordToken value = new KeywordToken("const", "const", 14);
        IToken[] tokens = dclC(value);

        IToken[] result = createRule(false).apply(tokens);

        assertSame(value, result[2]);
    }

    // --- defensive guards ---

    @Test
    public void nullTokens_returnsNull() {
        assertNull(createRule(true).apply(null));
    }

    @Test
    public void tooFewTokens_isLeftUntouched() {
        // dcl-c without value (incomplete) — must not crash.
        IToken[] tokens = new IToken[] { new DeclToken("dcl-c", "dcl-c ", 0), new NameToken("MY_CONST", "MY_CONST ", 6) };

        IToken[] result = createRule(true).apply(tokens);

        assertEquals(2, result.length);
    }

    // --- offset preservation when wrapping ---

    @Test
    public void useConstTrue_wrappedTokenInheritsOriginalOffset() {
        OtherToken value = new OtherToken("100", "100", 23);
        IToken[] tokens = dclC(value);

        IToken[] result = createRule(true).apply(tokens);

        assertEquals(23, result[2].getOffset());
    }

    // --- helpers ---

    private static IToken[] dclC(IToken value) {
        return new IToken[] { new DeclToken("dcl-c", "dcl-c ", 0), new NameToken("MY_CONST", "MY_CONST ", 6), value,
            new EolToken(";", ";", value.getOffset() + value.getRawLength()) };
    }

    private static IToken constWrapping(IToken inner) {
        IToken wrapped = new KeywordToken("const", "const(" + inner.getRawValue() + ")", 14);
        wrapped.addChild(inner);
        return wrapped;
    }
}
