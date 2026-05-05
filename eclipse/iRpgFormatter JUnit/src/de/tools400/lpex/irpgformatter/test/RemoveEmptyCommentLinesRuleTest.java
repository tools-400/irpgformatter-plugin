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
 * Unit tests for the "remove empty comment lines" feature.
 * <p>
 * Tests are run end-to-end through {@link RpgleFormatter} with the option
 * enabled so that the full three-pass algorithm is exercised in its natural
 * context.
 */
public class RemoveEmptyCommentLinesRuleTest extends AbstractTestCase {

    private RpgleFormatter formatter;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        formatter = new RpgleFormatter();
        formatter.getConfig().setRemoveEmptyCommentLines(true);
    }

    // --- basic suppression ---

    @Test
    public void standaloneEmptyComment_isSuppressed() throws RpgleFormatterException {
        String[] result = format("//");
        assertEquals(1, result.length);
        assertEquals("", result[0]);
    }

    @Test
    public void nonEmptyComment_isPreserved() throws RpgleFormatterException {
        String[] result = format("// This is a comment");
        assertEquals(1, result.length);
        assertEquals("// This is a comment", result[0]);
    }

    @Test
    public void multipleEmptyComments_areAllSuppressed() throws RpgleFormatterException {
        // @formatter:off
        String[] result = format(
            "//",
            "//",
            "//"
        );
        // @formatter:on
        assertEquals(3, result.length);
        assertEquals("", result[0]);
        assertEquals("", result[1]);
        assertEquals("", result[2]);
    }

    @Test
    public void emptyCommentBetweenCodeLines_isSuppressed() throws RpgleFormatterException {
        // @formatter:off
        String[] result = format(
            "dcl-s a char(1);",
            "//",
            "dcl-s b char(1);"
        );
        // @formatter:on
        assertEquals(3, result.length);
        assertEquals("dcl-s a char(1);", result[0]);
        assertEquals("", result[1]);
        assertEquals("dcl-s b char(1);", result[2]);
    }

    // --- separator lines ---

    @Test
    public void separatorLine_isPreserved() throws RpgleFormatterException {
        String[] result = format("//-------");
        assertEquals(1, result.length);
        assertEquals("//-------", result[0]);
    }

    @Test
    public void emptyCommentDirectlyBeforeSeparator_isSuppressed() throws RpgleFormatterException {
        // The single empty comment directly before the separator has no structural
        // purpose (nothing but blank above it), so step 3 re-suppresses it.
        // @formatter:off
        String[] result = format(
            "//",
            "//-------"
        );
        // @formatter:on
        assertEquals(2, result.length);
        assertEquals("", result[0]);
        assertEquals("//-------", result[1]);
    }

    @Test
    public void contentCommentBeforeSeparator_isPreserved() throws RpgleFormatterException {
        // @formatter:off
        String[] result = format(
            "// Header comment",
            "//-------"
        );
        // @formatter:on
        assertEquals(2, result.length);
        assertEquals("// Header comment", result[0]);
        assertEquals("//-------", result[1]);
    }

    @Test
    public void emptyThenContentThenSeparator_leadingEmptySuppressed() throws RpgleFormatterException {
        // The leading empty comment has no structural role; the content comment does.
        // @formatter:off
        String[] result = format(
            "//",
            "// Header comment",
            "//-------"
        );
        // @formatter:on
        assertEquals(3, result.length);
        assertEquals("", result[0]);
        assertEquals("// Header comment", result[1]);
        assertEquals("//-------", result[2]);
    }

    @Test
    public void emptyCommentAfterCodeBeforeSeparator_isSuppressed() throws RpgleFormatterException {
        // @formatter:off
        String[] result = format(
            "dcl-s a char(1);",
            "//",
            "// Header comment",
            "//-------"
        );
        // @formatter:on
        assertEquals(4, result.length);
        assertEquals("dcl-s a char(1);", result[0]);
        assertEquals("", result[1]);
        assertEquals("// Header comment", result[2]);
        assertEquals("//-------", result[3]);
    }

    @Test
    public void separatorVariants_areAllRecognized() throws RpgleFormatterException {
        // Each of these must be treated as a structural marker (not suppressed).
        String[] separators = { "//---", "//===", "//***", "//+++", "//~~~" };
        for (String sep : separators) {
            String[] result = format(sep);
            assertEquals("Separator should be preserved: " + sep, 1, result.length);
            assertEquals("Separator should be preserved: " + sep, sep, result[0]);
        }
    }

    // --- ILEDoc blocks ---

    @Test
    public void iledocDelimiter_isPreserved() throws RpgleFormatterException {
        String[] result = format("///");
        assertEquals(1, result.length);
        assertEquals("///", result[0]);
    }

    @Test
    public void emptyCommentInsideIledocBlock_isPreserved() throws RpgleFormatterException {
        // @formatter:off
        String[] result = format(
            "///",
            "//",
            "///"
        );
        // @formatter:on
        assertEquals(3, result.length);
        assertEquals("///", result[0]);
        assertEquals("//", result[1]);   // inside ILEDoc block — must not be suppressed
        assertEquals("///", result[2]);
    }

    @Test
    public void emptyCommentBeforeIledocBlock_isSuppressed() throws RpgleFormatterException {
        // The opening "///" is a structural marker; the backward scan restores then
        // step 3 re-suppresses the single empty comment directly above it.
        // @formatter:off
        String[] result = format(
            "//",
            "///",
            "// ILEDoc content",
            "///"
        );
        // @formatter:on
        assertEquals(4, result.length);
        assertEquals("", result[0]);     // re-suppressed by step 3
        assertEquals("///", result[1]);
        assertEquals("// ILEDoc content", result[2]);
        assertEquals("///", result[3]);
    }

    @Test
    public void contentCommentBeforeIledocBlock_isPreserved() throws RpgleFormatterException {
        // @formatter:off
        String[] result = format(
            "// Intro comment",
            "///",
            "// ILEDoc content",
            "///"
        );
        // @formatter:on
        assertEquals(4, result.length);
        assertEquals("// Intro comment", result[0]);
        assertEquals("///", result[1]);
        assertEquals("// ILEDoc content", result[2]);
        assertEquals("///", result[3]);
    }

    // --- @formatter:off regions ---

    @Test
    public void emptyCommentInsideFormatterOffRegion_isNotSuppressed() throws RpgleFormatterException {
        // @formatter:off
        String[] result = format(
            "// @formatter:off",
            "//",
            "// @formatter:on"
        );
        // @formatter:on
        assertEquals(3, result.length);
        assertEquals("// @formatter:off", result[0]);
        assertEquals("//", result[1]);   // inside disabled region — must not be touched
        assertEquals("// @formatter:on", result[2]);
    }

    @Test
    public void backwardScanStopsAtFormatterOff_forwardScanSkipped() throws RpgleFormatterException {
        // When the backward scan reaches a @formatter:off boundary, step 3
        // (forward re-suppression) is skipped entirely.  Lines between the boundary
        // and the separator are therefore left unchanged.
        // @formatter:off
        String[] result = format(
            "// @formatter:off",
            "//",
            "// @formatter:on",
            "//",
            "//-------"
        );
        // @formatter:on
        assertEquals(5, result.length);
        assertEquals("// @formatter:off", result[0]);
        assertEquals("//", result[1]);   // inside disabled region — untouched
        assertEquals("// @formatter:on", result[2]);
        assertEquals("//", result[3]);   // restored by backward scan; step 3 skipped
        assertEquals("//-------", result[4]);
    }

    // --- disabled (feature off) ---

    @Test
    public void featureDisabled_emptyCommentsAreKept() throws RpgleFormatterException {
        formatter.getConfig().setRemoveEmptyCommentLines(false);
        // @formatter:off
        String[] result = format(
            "//",
            "// @formatter:off",
            "//",
            "// @formatter:on"
        );
        // @formatter:on
        assertEquals(4, result.length);
        assertEquals("//", result[0]);
        assertEquals("// @formatter:off", result[1]);
        assertEquals("//", result[2]);
        assertEquals("// @formatter:on", result[3]);
    }

    // --- helpers ---

    private String[] format(String... lines) throws RpgleFormatterException {
        return formatter.format(new TextLinesInput(lines), 0).toLines();
    }
}
