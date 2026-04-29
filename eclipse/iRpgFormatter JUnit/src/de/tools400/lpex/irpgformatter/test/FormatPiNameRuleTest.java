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

import de.tools400.lpex.irpgformatter.rules.statements.FormatPiNameRule;
import de.tools400.lpex.irpgformatter.tokenizer.DeclToken;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.NameToken;
import de.tools400.lpex.irpgformatter.tokenizer.OtherToken;
import de.tools400.lpex.irpgformatter.tokenizer.SpecialWordToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;

public class FormatPiNameRuleTest {

    private static final String PROC_NAME = "myProc";

    // --- replace = true ---

    @Test
    public void replaceTrue_nameToken_isReplacedWithStarN() {
        IToken[] tokens = piTokens(new NameToken("myProc", "myProc ", 8));

        IToken[] result = new FormatPiNameRule(PROC_NAME, true).apply(tokens);

        assertEquals(TokenType.SPECIAL_WORD, result[1].getType());
        assertEquals("*N", result[1].getValue());
    }

    @Test
    public void replaceTrue_starNToken_isLeftUntouched() {
        SpecialWordToken starN = new SpecialWordToken("*N", "*N ", 8);
        IToken[] tokens = piTokens(starN);

        IToken[] result = new FormatPiNameRule(PROC_NAME, true).apply(tokens);

        assertSame(starN, result[1]);
    }

    @Test
    public void replaceTrue_secondTokenIsKeyword_isLeftUntouched() {
        // dcl-pi extproc('foo') - no NAME token at index 1
        OtherToken keywordLike = new OtherToken("extproc", "extproc", 8);
        IToken[] tokens = piTokens(keywordLike);

        IToken[] result = new FormatPiNameRule(PROC_NAME, true).apply(tokens);

        assertSame(keywordLike, result[1]);
    }

    // --- replace = false (restore direction) ---

    @Test
    public void replaceFalse_starNToken_isReplacedWithProcName() {
        IToken[] tokens = piTokens(new SpecialWordToken("*N", "*N ", 8));

        IToken[] result = new FormatPiNameRule(PROC_NAME, false).apply(tokens);

        assertEquals(TokenType.NAME, result[1].getType());
        assertEquals(PROC_NAME, result[1].getValue());
    }

    @Test
    public void replaceFalse_starNTokenLowerCase_isReplacedWithProcName() {
        IToken[] tokens = piTokens(new SpecialWordToken("*n", "*n ", 8));

        IToken[] result = new FormatPiNameRule(PROC_NAME, false).apply(tokens);

        assertEquals(TokenType.NAME, result[1].getType());
        assertEquals(PROC_NAME, result[1].getValue());
    }

    @Test
    public void replaceFalse_nameToken_isLeftUntouched() {
        NameToken name = new NameToken("myProc", "myProc ", 8);
        IToken[] tokens = piTokens(name);

        IToken[] result = new FormatPiNameRule(PROC_NAME, false).apply(tokens);

        assertSame(name, result[1]);
    }

    @Test
    public void replaceFalse_otherSpecialWord_isLeftUntouched() {
        SpecialWordToken other = new SpecialWordToken("*OMIT", "*OMIT ", 8);
        IToken[] tokens = piTokens(other);

        IToken[] result = new FormatPiNameRule(PROC_NAME, false).apply(tokens);

        assertSame(other, result[1]);
    }

    // --- procedureName == null (no DCL-PROC context) ---

    @Test
    public void nullProcName_replaceTrue_isNoop_evenForNameToken() {
        NameToken name = new NameToken("myProc", "myProc ", 8);
        IToken[] tokens = piTokens(name);

        IToken[] result = new FormatPiNameRule(null, true).apply(tokens);

        assertSame(name, result[1]);
    }

    @Test
    public void nullProcName_replaceFalse_isNoop_evenForStarN() {
        SpecialWordToken starN = new SpecialWordToken("*N", "*N ", 8);
        IToken[] tokens = piTokens(starN);

        IToken[] result = new FormatPiNameRule(null, false).apply(tokens);

        assertSame(starN, result[1]);
    }

    // --- defensive guards ---

    @Test
    public void nullTokens_returnsNull() {
        assertNull(new FormatPiNameRule(PROC_NAME, true).apply(null));
    }

    @Test
    public void singleToken_isLeftUntouched() {
        IToken[] tokens = new IToken[] { new DeclToken("dcl-pi", "dcl-pi ", 0) };

        IToken[] result = new FormatPiNameRule(PROC_NAME, true).apply(tokens);

        assertEquals(1, result.length);
        assertSame(tokens[0], result[0]);
    }

    @Test
    public void emptyTokens_isLeftUntouched() {
        IToken[] tokens = new IToken[0];

        IToken[] result = new FormatPiNameRule(PROC_NAME, true).apply(tokens);

        assertEquals(0, result.length);
    }

    // --- offset preservation ---

    @Test
    public void replaceTrue_preservesOffsetOfReplacedToken() {
        IToken[] tokens = piTokens(new NameToken("myProc", "myProc ", 17));

        IToken[] result = new FormatPiNameRule(PROC_NAME, true).apply(tokens);

        assertEquals(17, result[1].getOffset());
    }

    @Test
    public void replaceFalse_preservesOffsetOfReplacedToken() {
        IToken[] tokens = piTokens(new SpecialWordToken("*N", "*N ", 17));

        IToken[] result = new FormatPiNameRule(PROC_NAME, false).apply(tokens);

        assertEquals(17, result[1].getOffset());
    }

    // --- helpers ---

    private IToken[] piTokens(IToken second) {
        return new IToken[] { new DeclToken("dcl-pi", "dcl-pi ", 0), second };
    }
}
