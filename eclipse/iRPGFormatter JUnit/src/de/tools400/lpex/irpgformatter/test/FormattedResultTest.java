/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.FormattedStatement;

public class FormattedResultTest extends AbstractTestCase {

    @Test
    public void mapLineTo_unchangedStatements() {

        FormattedResult source = result(stmt(1, 2, "A1", "A2"), stmt(3, 1, "B1"), stmt(4, 3, "C1", "C2", "C3"));
        FormattedResult target = result(stmt(1, 2, "A1", "A2"), stmt(3, 1, "B1"), stmt(4, 3, "C1", "C2", "C3"));

        for (int i = 0; i < source.getLineCount(); i++) {
            assertEquals("Line " + i + " should map to itself", i, source.mapLineTo(i, target));
        }
    }

    @Test
    public void mapLineTo_statementExpands() {

        // Statement B expands from 1 line to 3 lines
        FormattedResult source = result(stmt(1, 2, "A1", "A2"), stmt(3, 1, "B1"), stmt(4, 2, "C1", "C2"));
        FormattedResult target = result(stmt(1, 2, "A1", "A2"), stmt(3, 1, "B1", "B2", "B3"), stmt(4, 2, "C1", "C2"));

        // A1 -> A1
        assertEquals(0, source.mapLineTo(0, target));
        // A2 -> A2
        assertEquals(1, source.mapLineTo(1, target));
        // B1 (offset 0) -> B1 (offset 0)
        assertEquals(2, source.mapLineTo(2, target));
        // C1 (offset 0 in stmt 2) -> C1 now at line 5
        assertEquals(5, source.mapLineTo(3, target));
        // C2 (offset 1 in stmt 2) -> C2 now at line 6
        assertEquals(6, source.mapLineTo(4, target));
    }

    @Test
    public void mapLineTo_statementContracts() {

        // Statement B shrinks from 3 lines to 1 line
        FormattedResult source = result(stmt(1, 2, "A1", "A2"), stmt(3, 3, "B1", "B2", "B3"), stmt(6, 1, "C1"));
        FormattedResult target = result(stmt(1, 2, "A1", "A2"), stmt(3, 3, "B1"), stmt(6, 1, "C1"));

        // A1 -> A1
        assertEquals(0, source.mapLineTo(0, target));
        // A2 -> A2
        assertEquals(1, source.mapLineTo(1, target));
        // B1 (offset 0) -> B1 (offset 0)
        assertEquals(2, source.mapLineTo(2, target));
        // B2 (offset 1) -> clamped to offset 0 (only 1 line in target)
        assertEquals(2, source.mapLineTo(3, target));
        // B3 (offset 2) -> clamped to offset 0
        assertEquals(2, source.mapLineTo(4, target));
        // C1 -> C1 now at line 3
        assertEquals(3, source.mapLineTo(5, target));
    }

    @Test
    public void mapLineTo_mixedChanges() {

        // A: 2->1 (shrink), B: 1->3 (expand), C: 2->2 (same)
        FormattedResult source = result(stmt(1, 2, "A1", "A2"), stmt(3, 1, "B1"), stmt(4, 2, "C1", "C2"));
        FormattedResult target = result(stmt(1, 2, "A1x"), stmt(3, 1, "B1x", "B2x", "B3x"), stmt(4, 2, "C1x", "C2x"));

        // A1 (offset 0) -> A1x (offset 0, line 0)
        assertEquals(0, source.mapLineTo(0, target));
        // A2 (offset 1) -> clamped to offset 0 (only 1 line in target stmt A)
        assertEquals(0, source.mapLineTo(1, target));
        // B1 (offset 0) -> B1x (offset 0, line 1)
        assertEquals(1, source.mapLineTo(2, target));
        // C1 (offset 0) -> C1x (offset 0, line 4)
        assertEquals(4, source.mapLineTo(3, target));
        // C2 (offset 1) -> C2x (offset 1, line 5)
        assertEquals(5, source.mapLineTo(4, target));
    }

    @Test
    public void mapLineTo_lineAfterEnd() {

        FormattedResult source = result(stmt(1, 1, "A1"), stmt(2, 1, "B1"));
        FormattedResult target = result(stmt(1, 1, "A1"), stmt(2, 1, "B1"), stmt(3, 1, "C1"));

        // Line 99 is beyond source -> returns last line of target
        assertEquals(2, source.mapLineTo(99, target));
    }

    @Test
    public void isChanged_sameLines() {

        FormattedResult result = new FormattedResult(new FormattedStatement[] { stmt(1, 2, "A1", "A2"), stmt(3, 1, "B1") },
            new String[] { "A1", "A2", "B1" });

        assertFalse(result.isChanged());
    }

    @Test
    public void isChanged_changedLine() {

        FormattedResult result = new FormattedResult(new FormattedStatement[] { stmt(1, 2, "A1", "A2"), stmt(3, 1, "B1") },
            new String[] { "A1", "a2", "B1" });

        assertTrue(result.isChanged());
    }

    @Test
    public void isChanged_addedLine() {

        FormattedResult result = new FormattedResult(new FormattedStatement[] { stmt(1, 1, "A1", "A2"), stmt(2, 1, "B1") },
            new String[] { "A1", "B1" });

        assertTrue(result.isChanged());
    }

    @Test
    public void isChanged_removedLine() {

        FormattedResult result = new FormattedResult(new FormattedStatement[] { stmt(1, 2, "A1"), stmt(3, 1, "B1") },
            new String[] { "A1", "A2", "B1" });

        assertTrue(result.isChanged());
    }

    // -- getFormattedLineNumber: the view iRPGFormatter Problems goes to the
    // statement of an error after the formatted lines were written --

    @Test
    public void getFormattedLineNumber_unchangedStatements() {
        FormattedResult result = result(stmt(1, 2, "A1", "A2"), stmt(3, 1, "B1"), stmt(4, 2, "C1", "C2"));
        for (int line = 1; line <= 5; line++) {
            assertEquals(line, result.getFormattedLineNumber(line));
        }
    }

    @Test
    public void getFormattedLineNumber_statementInFrontExpands() {
        // B expands from 1 line to 3 lines: C moves from line 4 to line 6
        FormattedResult result = result(stmt(1, 2, "A1", "A2"), stmt(3, 1, "B1", "B2", "B3"), stmt(4, 2, "C1", "C2"));
        assertEquals(1, result.getFormattedLineNumber(1));
        assertEquals(3, result.getFormattedLineNumber(3));
        assertEquals(6, result.getFormattedLineNumber(4));
    }

    @Test
    public void getFormattedLineNumber_statementInFrontContracts() {
        // B shrinks from 3 lines to 1 line: C moves from line 6 to line 4
        FormattedResult result = result(stmt(1, 2, "A1", "A2"), stmt(3, 3, "B1"), stmt(6, 1, "C1"));
        assertEquals(3, result.getFormattedLineNumber(3));
        assertEquals(4, result.getFormattedLineNumber(6));
    }

    @Test
    public void getFormattedLineNumber_selectedLines() {
        // selected lines 10-14: B expands by one line, the lines in front of
        // the selection are not part of the result
        FormattedResult result = result(stmt(10, 1, "A1"), stmt(11, 2, "B1", "B2", "B3"), stmt(13, 2, "C1", "C2"));
        assertEquals(10, result.getFormattedLineNumber(10));
        assertEquals(11, result.getFormattedLineNumber(11));
        assertEquals(14, result.getFormattedLineNumber(13));
    }

    @Test
    public void isChanged_withoutSourceLines() {
        assertTrue(result(stmt(1, 1, "A1")).isChanged());
    }

    // -- helpers --

    private FormattedResult result(FormattedStatement... stmts) {
        return new FormattedResult(stmts);
    }

    private FormattedStatement stmt(int startLine, int origCount, String... lines) {
        return new FormattedStatement(startLine, origCount, lines);
    }
}
