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

import de.tools400.lpex.irpgformatter.rules.statements.FormatEndProcNameRule;
import de.tools400.lpex.irpgformatter.tokenizer.DeclToken;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.NameToken;
import de.tools400.lpex.irpgformatter.tokenizer.OtherToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;

public class FormatEndProcNameRuleTest {

    private static final String PROC_NAME = "myProc";

    // --- remove = true ---

    @Test
    public void removeTrue_nameToken_isRemoved() {
        IToken[] tokens = endProcTokens(new NameToken("myProc", "myProc", 9));

        IToken[] result = new FormatEndProcNameRule(PROC_NAME, true).apply(tokens);

        assertEquals(1, result.length);
        assertEquals(TokenType.DCL, result[0].getType());
    }

    @Test
    public void removeTrue_noNameToken_isLeftUntouched() {
        IToken[] tokens = new IToken[] { new DeclToken("end-proc", "end-proc", 0) };

        IToken[] result = new FormatEndProcNameRule(PROC_NAME, true).apply(tokens);

        assertEquals(1, result.length);
        assertSame(tokens[0], result[0]);
    }

    @Test
    public void removeTrue_secondTokenIsNotName_isLeftUntouched() {
        OtherToken other = new OtherToken("xyz", "xyz", 9);
        IToken[] tokens = endProcTokens(other);

        IToken[] result = new FormatEndProcNameRule(PROC_NAME, true).apply(tokens);

        assertEquals(2, result.length);
        assertSame(other, result[1]);
    }

    @Test
    public void removeTrue_keepsTokensAfterName() {
        OtherToken trailing = new OtherToken(";", ";", 16);
        IToken[] tokens = new IToken[] { new DeclToken("end-proc", "end-proc ", 0), new NameToken("myProc", "myProc", 9), trailing };

        IToken[] result = new FormatEndProcNameRule(PROC_NAME, true).apply(tokens);

        assertEquals(2, result.length);
        assertEquals(TokenType.DCL, result[0].getType());
        assertSame(trailing, result[1]);
    }

    // --- remove = false (ensure direction) ---

    @Test
    public void removeFalse_nameMissing_isInserted() {
        DeclToken endProc = new DeclToken("end-proc", "end-proc ", 0);
        IToken[] tokens = new IToken[] { endProc };

        IToken[] result = new FormatEndProcNameRule(PROC_NAME, false).apply(tokens);

        assertEquals(2, result.length);
        assertEquals(TokenType.NAME, result[1].getType());
        assertEquals(PROC_NAME, result[1].getValue());
    }

    @Test
    public void removeFalse_nameMissing_insertedTokenOffsetIsAfterEndProc() {
        DeclToken endProc = new DeclToken("end-proc", "end-proc ", 0);
        IToken[] tokens = new IToken[] { endProc };

        IToken[] result = new FormatEndProcNameRule(PROC_NAME, false).apply(tokens);

        assertEquals(endProc.getOffset() + endProc.getRawLength(), result[1].getOffset());
    }

    @Test
    public void removeFalse_divergentName_isOverwrittenWithProcName() {
        IToken[] tokens = endProcTokens(new NameToken("otherName", "otherName", 9));

        IToken[] result = new FormatEndProcNameRule(PROC_NAME, false).apply(tokens);

        assertEquals(2, result.length);
        assertEquals(TokenType.NAME, result[1].getType());
        assertEquals(PROC_NAME, result[1].getValue());
    }

    @Test
    public void removeFalse_matchingName_keepsValueAndType() {
        IToken[] tokens = endProcTokens(new NameToken("myProc", "myProc", 9));

        IToken[] result = new FormatEndProcNameRule(PROC_NAME, false).apply(tokens);

        assertEquals(2, result.length);
        assertEquals(TokenType.NAME, result[1].getType());
        assertEquals(PROC_NAME, result[1].getValue());
    }

    @Test
    public void removeFalse_overwrite_preservesOffsetOfReplacedToken() {
        IToken[] tokens = endProcTokens(new NameToken("otherName", "otherName", 23));

        IToken[] result = new FormatEndProcNameRule(PROC_NAME, false).apply(tokens);

        assertEquals(23, result[1].getOffset());
    }

    @Test
    public void removeFalse_keepsTokensAfterName_whenInserting() {
        DeclToken endProc = new DeclToken("end-proc", "end-proc ", 0);
        OtherToken trailing = new OtherToken(";", ";", 9);
        IToken[] tokens = new IToken[] { endProc, trailing };

        IToken[] result = new FormatEndProcNameRule(PROC_NAME, false).apply(tokens);

        assertEquals(3, result.length);
        assertEquals(TokenType.NAME, result[1].getType());
        assertSame(trailing, result[2]);
    }

    // --- procedureName == null (no matching DCL-PROC) ---

    @Test
    public void nullProcName_removeTrue_stillRemovesName() {
        IToken[] tokens = endProcTokens(new NameToken("orphan", "orphan", 9));

        IToken[] result = new FormatEndProcNameRule(null, true).apply(tokens);

        assertEquals(1, result.length);
        assertEquals(TokenType.DCL, result[0].getType());
    }

    @Test
    public void nullProcName_removeFalse_isNoop_whenNameMissing() {
        IToken[] tokens = new IToken[] { new DeclToken("end-proc", "end-proc", 0) };

        IToken[] result = new FormatEndProcNameRule(null, false).apply(tokens);

        assertEquals(1, result.length);
        assertSame(tokens[0], result[0]);
    }

    @Test
    public void nullProcName_removeFalse_isNoop_whenDivergentName() {
        NameToken existing = new NameToken("orphan", "orphan", 9);
        IToken[] tokens = endProcTokens(existing);

        IToken[] result = new FormatEndProcNameRule(null, false).apply(tokens);

        assertEquals(2, result.length);
        assertSame(existing, result[1]);
    }

    // --- defensive guards ---

    @Test
    public void nullTokens_returnsNull() {
        assertNull(new FormatEndProcNameRule(PROC_NAME, true).apply(null));
        assertNull(new FormatEndProcNameRule(PROC_NAME, false).apply(null));
    }

    @Test
    public void emptyTokens_isLeftUntouched() {
        IToken[] empty = new IToken[0];

        assertEquals(0, new FormatEndProcNameRule(PROC_NAME, true).apply(empty).length);
        assertEquals(0, new FormatEndProcNameRule(PROC_NAME, false).apply(empty).length);
    }

    // --- helpers ---

    private IToken[] endProcTokens(IToken second) {
        return new IToken[] { new DeclToken("end-proc", "end-proc ", 0), second };
    }
}
