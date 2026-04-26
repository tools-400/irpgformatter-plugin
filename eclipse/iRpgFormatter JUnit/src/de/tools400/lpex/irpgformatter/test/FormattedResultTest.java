/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.FormattedStatement;

public class FormattedResultTest extends AbstractTestCase {

    @Test
    public void mapLineTo_unchangedStatements() {

        FormattedResult source = result(
            stmt(1, 2, "A1", "A2"),
            stmt(3, 1, "B1"),
            stmt(4, 3, "C1", "C2", "C3")
        );
        FormattedResult target = result(
            stmt(1, 2, "A1", "A2"),
            stmt(3, 1, "B1"),
            stmt(4, 3, "C1", "C2", "C3")
        );

        for (int i = 0; i < source.getLineCount(); i++) {
            assertEquals("Line " + i + " should map to itself", i, source.mapLineTo(i, target));
        }
    }

    @Test
    public void mapLineTo_statementExpands() {

        // Statement B expands from 1 line to 3 lines
        FormattedResult source = result(
            stmt(1, 2, "A1", "A2"),
            stmt(3, 1, "B1"),
            stmt(4, 2, "C1", "C2")
        );
        FormattedResult target = result(
            stmt(1, 2, "A1", "A2"),
            stmt(3, 1, "B1", "B2", "B3"),
            stmt(4, 2, "C1", "C2")
        );

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
        FormattedResult source = result(
            stmt(1, 2, "A1", "A2"),
            stmt(3, 3, "B1", "B2", "B3"),
            stmt(6, 1, "C1")
        );
        FormattedResult target = result(
            stmt(1, 2, "A1", "A2"),
            stmt(3, 3, "B1"),
            stmt(6, 1, "C1")
        );

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
        FormattedResult source = result(
            stmt(1, 2, "A1", "A2"),
            stmt(3, 1, "B1"),
            stmt(4, 2, "C1", "C2")
        );
        FormattedResult target = result(
            stmt(1, 2, "A1x"),
            stmt(3, 1, "B1x", "B2x", "B3x"),
            stmt(4, 2, "C1x", "C2x")
        );

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

        FormattedResult source = result(
            stmt(1, 1, "A1"),
            stmt(2, 1, "B1")
        );
        FormattedResult target = result(
            stmt(1, 1, "A1"),
            stmt(2, 1, "B1"),
            stmt(3, 1, "C1")
        );

        // Line 99 is beyond source -> returns last line of target
        assertEquals(2, source.mapLineTo(99, target));
    }

    // -- helpers --

    private FormattedResult result(FormattedStatement... stmts) {
        return new FormattedResult(stmts);
    }

    private FormattedStatement stmt(int startLine, int origCount, String... lines) {
        return new FormattedStatement(startLine, origCount, lines);
    }
}
