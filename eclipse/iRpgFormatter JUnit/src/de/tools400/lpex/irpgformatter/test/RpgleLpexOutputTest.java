/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.FormattedStatement;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.RpgleLpexOutput;

/**
 * Tests for {@link RpgleLpexOutput#writeSourceLines(FormattedResult)}.
 * <p>
 * Verifies that the reverse-iteration logic correctly handles insertions,
 * deletions, and unchanged statements without shifting positions.
 */
public class RpgleLpexOutputTest extends AbstractTestCase {

    @Test
    public void unchanged_statementsAreSkipped() throws RpgleFormatterException {
        // 3 statements, all unchanged → buffer stays identical
        ArrayLineEditor editor = new ArrayLineEditor("A1", "B1", "C1");

        FormattedStatement[] stmts = {
            new FormattedStatement(1, 1, new String[] { "A1" }),
            new FormattedStatement(2, 1, new String[] { "B1" }),
            new FormattedStatement(3, 1, new String[] { "C1" })
        };

        new RpgleLpexOutput(editor).writeSourceLines(new FormattedResult(stmts));

        assertArrayEquals(new String[] { "A1", "B1", "C1" }, editor.toArray());
    }

    @Test
    public void singleExpansion_linesInserted() throws RpgleFormatterException {
        // 1 statement grows from 1 → 3 lines
        ArrayLineEditor editor = new ArrayLineEditor("A1");

        FormattedStatement[] stmts = {
            new FormattedStatement(1, 1, new String[] { "A1-new", "A2-new", "A3-new" })
        };

        new RpgleLpexOutput(editor).writeSourceLines(new FormattedResult(stmts));

        assertArrayEquals(new String[] { "A1-new", "A2-new", "A3-new" }, editor.toArray());
    }

    @Test
    public void singleContraction_linesDeleted() throws RpgleFormatterException {
        // 1 statement shrinks from 3 → 1 line
        ArrayLineEditor editor = new ArrayLineEditor("A1", "A2", "A3");

        FormattedStatement[] stmts = {
            new FormattedStatement(1, 3, new String[] { "A1-new" })
        };

        new RpgleLpexOutput(editor).writeSourceLines(new FormattedResult(stmts));

        assertArrayEquals(new String[] { "A1-new" }, editor.toArray());
    }

    @Test
    public void mixedChanges_reverseIterationPreservesPositions() throws RpgleFormatterException {
        // 4 statements:
        //   A (lines 1-2): grows 2 → 4
        //   B (lines 3-4): shrinks 2 → 1
        //   C (lines 5-8): shrinks 4 → 3
        //   D (lines 9-10): unchanged 2 → 2
        ArrayLineEditor editor = new ArrayLineEditor(
            "A1", "A2",         // stmt A: lines 1-2
            "B1", "B2",         // stmt B: lines 3-4
            "C1", "C2", "C3", "C4", // stmt C: lines 5-8
            "D1", "D2"          // stmt D: lines 9-10
        );

        FormattedStatement[] stmts = {
            new FormattedStatement(1, 2, new String[] { "A1x", "A2x", "A3x", "A4x" }),
            new FormattedStatement(3, 2, new String[] { "B1x" }),
            new FormattedStatement(5, 4, new String[] { "C1x", "C2x", "C3x" }),
            new FormattedStatement(9, 2, new String[] { "D1", "D2" })
        };

        new RpgleLpexOutput(editor).writeSourceLines(new FormattedResult(stmts));

        assertArrayEquals(
            new String[] { "A1x", "A2x", "A3x", "A4x", "B1x", "C1x", "C2x", "C3x", "D1", "D2" },
            editor.toArray()
        );
    }

    @Test
    public void adjacentExpansions_allCorrect() throws RpgleFormatterException {
        // 3 statements, all grow from 1 → 3
        ArrayLineEditor editor = new ArrayLineEditor("A1", "B1", "C1");

        FormattedStatement[] stmts = {
            new FormattedStatement(1, 1, new String[] { "A1x", "A2x", "A3x" }),
            new FormattedStatement(2, 1, new String[] { "B1x", "B2x", "B3x" }),
            new FormattedStatement(3, 1, new String[] { "C1x", "C2x", "C3x" })
        };

        new RpgleLpexOutput(editor).writeSourceLines(new FormattedResult(stmts));

        assertArrayEquals(
            new String[] { "A1x", "A2x", "A3x", "B1x", "B2x", "B3x", "C1x", "C2x", "C3x" },
            editor.toArray()
        );
    }

    @Test
    public void adjacentContractions_allCorrect() throws RpgleFormatterException {
        // 3 statements, all shrink from 3 → 1
        ArrayLineEditor editor = new ArrayLineEditor(
            "A1", "A2", "A3",
            "B1", "B2", "B3",
            "C1", "C2", "C3"
        );

        FormattedStatement[] stmts = {
            new FormattedStatement(1, 3, new String[] { "A1x" }),
            new FormattedStatement(4, 3, new String[] { "B1x" }),
            new FormattedStatement(7, 3, new String[] { "C1x" })
        };

        new RpgleLpexOutput(editor).writeSourceLines(new FormattedResult(stmts));

        assertArrayEquals(new String[] { "A1x", "B1x", "C1x" }, editor.toArray());
    }

    @Test
    public void onlyMiddleChanged() throws RpgleFormatterException {
        // A and C unchanged, B changed → A and C untouched
        ArrayLineEditor editor = new ArrayLineEditor("A1", "B1", "C1");

        FormattedStatement[] stmts = {
            new FormattedStatement(1, 1, new String[] { "A1" }),
            new FormattedStatement(2, 1, new String[] { "B1-changed" }),
            new FormattedStatement(3, 1, new String[] { "C1" })
        };

        new RpgleLpexOutput(editor).writeSourceLines(new FormattedResult(stmts));

        assertArrayEquals(new String[] { "A1", "B1-changed", "C1" }, editor.toArray());
    }
}
