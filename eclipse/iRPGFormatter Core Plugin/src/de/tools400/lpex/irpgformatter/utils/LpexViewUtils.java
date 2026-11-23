/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import com.ibm.etools.iseries.parsers.ISeriesEditorRPGILEParser;
import com.ibm.lpex.core.LpexParser;
import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.preferences.Preferences;

public final class LpexViewUtils {

    public static ISeriesEditorRPGILEParser getParser(LpexView view) {

        LpexParser lpexParser = view.parser();
        if (!(view.parser() instanceof ISeriesEditorRPGILEParser)) {
            return null;
        }

        ISeriesEditorRPGILEParser rpgleParser = (ISeriesEditorRPGILEParser)lpexParser;

        return rpgleParser;
    }

    public static void addLine(LpexView view) {
        view.doCommand("add");
    }

    public static void deleteLine(LpexView view) {
        view.doCommand("delete");
    }

    public static int queryLine(LpexView view) {
        return view.queryInt("line");
    }

    public static int queryPosition(LpexView view) {
        return view.queryInt("position");
    }

    public static void setLine(LpexView view, int line) {
        view.doCommand("locate line " + line);
    }

    public static void setPosition(LpexView view, int position) {
        view.doCommand("set position " + position);
    }

    public static void locateElement(LpexView view, int element) {
        view.doCommand("locate element " + element);
    }

    public static void displayMessage(LpexView view, String message) {
        view.doCommand("set messageText " + message);
    }

    public static int getNumLines(LpexView view) {
        return view.queryInt("lines");
    }

    /**
     * Returns the text of a line of the document. The lines do not count the
     * show elements, e.g. the messages of the syntax check of RDi, which are
     * elements, but no lines.
     *
     * @param view - the LPEX view
     * @param line - the line (1-based)
     * @return the text of the line
     */
    public static String getLineText(LpexView view, int line) {
        return view.elementText(view.elementOfLine(line));
    }

    /**
     * Sets the text of a line of the document, see
     * {@link #getLineText(LpexView, int)}.
     *
     * @param view - the LPEX view
     * @param line - the line (1-based)
     * @param text - the new text
     */
    public static void setLineText(LpexView view, int line, String text) {
        view.setElementText(view.elementOfLine(line), text);
    }

    public static String getName(LpexView view) {
        return view.query("name");
    }

    public static boolean isReadOnly(LpexView view) {
        return view.queryOn("readonly");
    }

    /**
     * Returns the indent of the first source line at or after the given line,
     * that is neither blank nor the <code>**FREE</code> directive. The manual
     * format of a selection starts with this indent, if the lines in front of
     * the selection cannot be collected into statements.
     *
     * @return indent or 0, if there is no such line up to the end of the source
     */
    public static int getIndentOfFirstStatement(LpexView view, int startLine) {

        int numLines = getNumLines(view);
        for (int line = startLine; line <= numLines; line++) {
            String text = getLineText(view, line);
            if (text == null || text.trim().length() == 0 || text.toUpperCase().startsWith("**FREE")) {
                continue;
            }
            return StringUtils.getIndent(text).length();
        }

        return 0;
    }

    /**
     * Returns the physical record length for source members, or the end column
     * of the given preferences for stream files (where LPEX reports "default"
     * instead of a number), which is <code>Integer.MAX_VALUE</code> for
     * <code>*MAX</code>.
     *
     * @param view - the view of the source
     * @param preferences - the preferences of the source, e.g. of its project
     */
    public static int getMaxLineLength(LpexView view, Preferences preferences) {
        String raw = view.query("save.textLimit");
        if (raw == null || "default".equals(raw)) {
            return preferences.getEndColumn();
        }
        return Integer.parseInt(raw);
    }

    /**
     * Returns true if there is a block selection in the view.
     */
    public static boolean hasBlockSelection(LpexView view) {
        return view.queryOn("block.anythingSelected");
    }

    /**
     * Returns the block selection range as an int array [startLine, endLine]:
     * the lines, that contain selected text, see
     * {@link BlockSelectionUtils#getLastSelectedElement(String, int, int, int)}.
     * The selection is made of elements; a show element belongs to the line in
     * front of it, see {@link #getLineText(LpexView, int)}.
     *
     * @return array with start and end line, or null if no selection
     */
    public static int[] getBlockSelectionRange(LpexView view) {
        if (!hasBlockSelection(view)) {
            return null;
        }
        int top = view.queryInt("block.TopElement");
        int bottom = BlockSelectionUtils.getLastSelectedElement(view.query("block.type"), top, view.queryInt("block.BottomElement"),
            view.queryInt("block.bottomPosition"));
        return new int[] { Math.max(1, view.lineOfElement(top)), view.lineOfElement(bottom) };
    }

    /**
     * Selects a block range from startLine to endLine (inclusive). Uses stream
     * mode which clears automatically when the cursor moves.
     *
     * @param view the LPEX view
     * @param startLine the first line to select (1-based)
     * @param endLine the last line to select (1-based)
     */
    public static void selectBlockRange(LpexView view, int startLine, int endLine) {
        // Clear any existing selection
        view.doCommand("block clear");

        // Position at start of first line and begin selection
        locateElement(view, view.elementOfLine(startLine));
        view.doCommand("block set element");

        // Position at end of last line and extend selection
        locateElement(view, view.elementOfLine(endLine));
        view.doCommand("block set element");
    }
}
