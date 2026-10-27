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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;
import de.tools400.lpex.irpgformatter.preferencepages.PreviewComments;
import de.tools400.lpex.irpgformatter.preferences.Preferences;

/**
 * Checks that the preview can scroll to every comment of
 * {@link PreviewComments}.
 */
public class PreviewCommentsTest extends AbstractTestCase {

    @Test
    public void findLine_ignoresNumberOfBlanks() {

        String[] lines = { "**free", "//  Remove empty comment lines.", "  // Break before keyword." };

        assertEquals(1, PreviewComments.findLine(lines, PreviewComments.REMOVE_EMPTY_COMMENT_LINES));
        assertEquals(2, PreviewComments.findLine(lines, PreviewComments.BREAK_BEFORE_KEYWORD));
        assertEquals(-1, PreviewComments.findLine(lines, PreviewComments.REMOVE_END_PROC_NAME));
    }

    @Test
    public void allCommentsExistInDefaultPreviewSource() throws Exception {

        String[] lines = getDefaultPreviewSource();

        for (String comment : getAllComments()) {
            assertTrue("Missing in default_preview_source.rpgle: " + comment, PreviewComments.findLine(lines, comment) >= 0);
        }
    }

    /**
     * The preview scrolls in the formatted source, so the comments must survive
     * the formatter.
     */
    @Test
    public void allCommentsExistInFormattedDefaultPreviewSource() throws Exception {

        String[] lines = new RpgleFormatter().format(new TextLinesInput(getDefaultPreviewSource()), 0).toLines();

        for (String comment : getAllComments()) {
            assertTrue("Missing in the formatted preview: " + comment, PreviewComments.findLine(lines, comment) >= 0);
        }
    }

    private String[] getDefaultPreviewSource() {
        return Preferences.getInstance().getDefaultPluginFormatterSource().split("\n");
    }

    private List<String> getAllComments() throws IllegalAccessException {

        List<String> comments = new ArrayList<>();
        for (Field field : PreviewComments.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                comments.add((String)field.get(null));
            }
        }

        assertFalse("No comments found in PreviewComments", comments.isEmpty());

        return comments;
    }
}
