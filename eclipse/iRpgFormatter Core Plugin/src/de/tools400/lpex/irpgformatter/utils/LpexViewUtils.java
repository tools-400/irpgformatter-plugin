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

    public static String getElementText(LpexView view, int index) {
        return view.elementText(index);
    }

    public static void setElementText(LpexView view, int index, String text) {
        view.setElementText(index, text);
    }

    public static String getName(LpexView view) {
        return view.query("name");
    }

    public static boolean isReadOnly(LpexView view) {
        return view.queryOn("readonly");
    }

    public static boolean isSourceLine(LpexView aLpexView, int aLineNumber) {
        boolean isSourceLine = !aLpexView.show(aLineNumber);
        return isSourceLine;
    }

    public static int getMaxLineLength(LpexView view) {
        return view.queryInt("save.textLimit");
    }

    /**
     * Returns true if there is a block selection in the view.
     */
    public static boolean hasBlockSelection(LpexView view) {
        return view.queryOn("block.anythingSelected");
    }

    /**
     * Returns the block selection range as an int array [startLine, endLine].
     *
     * @return array with start and end line, or null if no selection
     */
    public static int[] getBlockSelectionRange(LpexView view) {
        if (!hasBlockSelection(view)) {
            return null;
        }
        int top = view.queryInt("block.TopElement");
        int bottom = view.queryInt("block.BottomElement");
        int column = view.queryInt("position");
        if (column == 1) {
            bottom--;
        }
        return new int[] { top, bottom };
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
        locateElement(view, startLine);
        view.doCommand("block set element");

        // Position at end of last line and extend selection
        locateElement(view, endLine);
        view.doCommand("block set element");
    }
}
