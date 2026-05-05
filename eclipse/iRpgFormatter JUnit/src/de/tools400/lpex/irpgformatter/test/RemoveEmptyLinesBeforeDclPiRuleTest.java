/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;

/**
 * Unit tests for the "remove empty lines before dcl-pi" feature.
 * <p>
 * Tests are run end-to-end through {@link RpgleFormatter} with the option
 * enabled so that the full pipeline is exercised in its natural context.
 * All inputs are complete, valid RPGLE snippets so the parent-stack validator
 * in {@code ContinuationHandler} does not throw.
 */
public class RemoveEmptyLinesBeforeDclPiRuleTest extends AbstractTestCase {

    private RpgleFormatter formatter;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        formatter = new RpgleFormatter();
        formatter.getConfig().setRemoveEmptyLinesBeforeDclPi(true);
    }

    // --- basic suppression ---

    @Test
    public void blankLineBefore_isSuppressed() throws RpgleFormatterException {
        // @formatter:off
        String[] result = format(
            "dcl-proc myProc;",
            "",
            "  dcl-pi *n;",
            "  end-pi;",
            "end-proc;"
        );
        // @formatter:on
        // blank between dcl-proc and dcl-pi is removed → 4 lines instead of 5
        assertEquals(4, result.length);
        assertEquals("dcl-proc myProc;", result[0]);
        assertEquals("  dcl-pi *n;", result[1]);
        assertEquals("  end-pi;", result[2]);
        assertEquals("end-proc;", result[3]);
    }

    @Test
    public void emptyCommentBefore_isSuppressed() throws RpgleFormatterException {
        // @formatter:off
        String[] result = format(
            "dcl-proc myProc;",
            "//",
            "  dcl-pi *n;",
            "  end-pi;",
            "end-proc;"
        );
        // @formatter:on
        // "//" is completely removed (not just blanked) → 4 lines instead of 5
        assertEquals(4, result.length);
        assertEquals("dcl-proc myProc;", result[0]);
        assertEquals("  dcl-pi *n;", result[1]);
    }

    @Test
    public void multipleBlanksBefore_areAllSuppressed() throws RpgleFormatterException {
        // @formatter:off
        String[] result = format(
            "dcl-proc myProc;",
            "",
            "",
            "  dcl-pi *n;",
            "  end-pi;",
            "end-proc;"
        );
        // @formatter:on
        // both blanks removed → 4 lines instead of 6
        assertEquals(4, result.length);
        assertEquals("dcl-proc myProc;", result[0]);
        assertEquals("  dcl-pi *n;", result[1]);
    }

    // --- combination ---

    @Test
    public void blankAndEmptyCommentBefore_bothSuppressed() throws RpgleFormatterException {
        // @formatter:off
        String[] result = format(
            "dcl-proc myProc;",
            "",
            "//",
            "  dcl-pi *n;",
            "  end-pi;",
            "end-proc;"
        );
        // @formatter:on
        // blank + "//" both removed → 4 lines instead of 6
        assertEquals(4, result.length);
        assertEquals("dcl-proc myProc;", result[0]);
        assertEquals("  dcl-pi *n;", result[1]);
    }

    @Test
    public void contentCommentBefore_isPreserved() throws RpgleFormatterException {
        // @formatter:off
        String[] result = format(
            "dcl-proc myProc;",
            "  // Documents the procedure interface.",
            "  dcl-pi *n;",
            "  end-pi;",
            "end-proc;"
        );
        // @formatter:on
        // content comment is not a candidate → all 5 lines kept
        assertEquals(5, result.length);
        assertEquals("dcl-proc myProc;", result[0]);
        assertEquals("  // Documents the procedure interface.", result[1]);
        assertEquals("  dcl-pi *n;", result[2]);
    }

    // --- not before dcl-pi ---

    @Test
    public void blankLineNotBeforeDclPi_isPreserved() throws RpgleFormatterException {
        // @formatter:off
        String[] result = format(
            "dcl-proc myProc;",
            "  dcl-pi *n;",
            "  end-pi;",
            "",
            "end-proc;"
        );
        // @formatter:on
        // blank is after end-pi (not before dcl-pi) → 5 lines kept
        assertEquals(5, result.length);
        assertEquals("", result[3]);
    }

    // --- feature off ---

    @Test
    public void featureDisabled_blankLinesKept() throws RpgleFormatterException {
        formatter.getConfig().setRemoveEmptyLinesBeforeDclPi(false);
        // @formatter:off
        String[] result = format(
            "dcl-proc myProc;",
            "",
            "  dcl-pi *n;",
            "  end-pi;",
            "end-proc;"
        );
        // @formatter:on
        // feature off → blank is kept, 5 lines total
        assertEquals(5, result.length);
        assertEquals("", result[1]);
    }

    // --- interaction with removeEmptyCommentLines ---

    @Test
    public void bothFeatures_emptyCommentCompletelyRemoved() throws RpgleFormatterException {
        // With removeEmptyCommentLines only: "//" becomes "" (blank line, 5 lines total).
        // With removeEmptyLinesBeforeDclPi on as well: "//" is completely deleted (4 lines).
        formatter.getConfig().setRemoveEmptyCommentLines(true);
        // @formatter:off
        String[] result = format(
            "dcl-proc myProc;",
            "//",
            "  dcl-pi *n;",
            "  end-pi;",
            "end-proc;"
        );
        // @formatter:on
        // suppressBeforeDclPi takes priority: line is deleted, not blanked → 4 lines
        assertEquals(4, result.length);
        assertEquals("dcl-proc myProc;", result[0]);
        assertEquals("  dcl-pi *n;", result[1]);
    }

    // --- helpers ---

    private String[] format(String... lines) throws RpgleFormatterException {
        return formatter.format(new TextLinesInput(lines), 0).toLines();
    }
}
