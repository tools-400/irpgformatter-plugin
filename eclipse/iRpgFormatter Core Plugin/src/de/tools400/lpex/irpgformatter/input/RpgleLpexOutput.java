/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.utils.LpexViewUtils;

/**
 * Writes formatted source lines back to a specific range in an LPEX view.
 */
public class RpgleLpexOutput implements IRpgleOutput {

    private final LpexView view;
    private final int startLine;
    private final int endLine;

    /**
     * Creates a new range output for the specified line range.
     *
     * @param view the LPEX view
     * @param startLine the first line of the range (1-based)
     * @param endLine the last line of the range (1-based, inclusive)
     */
    RpgleLpexOutput(LpexView view, int startLine, int endLine) {
        this.view = view;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    @Override
    public boolean writeSourceLines(String[] lines) throws RpgleFormatterException {
        int originalLineCount = countSourceLinesInRange();
        int newLineCount = lines.length;

        // Update existing lines in the range
        int sourceLineIndex = 0;
        int currentLine = startLine;

        while (currentLine <= endLine && sourceLineIndex < lines.length) {
            if (LpexViewUtils.isSourceLine(view, currentLine)) {
                String currentText = LpexViewUtils.getElementText(view, currentLine);
                if (!lines[sourceLineIndex].equals(currentText)) {
                    LpexViewUtils.setElementText(view, currentLine, lines[sourceLineIndex]);
                }
                sourceLineIndex++;
            }
            currentLine++;
        }

        // Handle line count difference
        if (newLineCount > originalLineCount) {
            // Need to add lines at end of range
            addLinesAfterRange(lines, originalLineCount);
        } else if (newLineCount < originalLineCount) {
            // Need to remove extra lines from range
            removeLinesFromRange(originalLineCount - newLineCount);
        }

        return true;
    }

    /**
     * Counts the number of source lines in the range.
     */
    private int countSourceLinesInRange() {
        int count = 0;
        for (int i = startLine; i <= endLine; i++) {
            if (LpexViewUtils.isSourceLine(view, i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Adds new lines after the original range.
     */
    private void addLinesAfterRange(String[] lines, int existingCount) {
        int insertPosition = endLine;

        for (int i = existingCount; i < lines.length; i++) {
            LpexViewUtils.locateElement(view, insertPosition);
            LpexViewUtils.addLine(view);
            insertPosition++;
            LpexViewUtils.setElementText(view, insertPosition, lines[i]);
        }
    }

    /**
     * Removes extra lines from the end of the range.
     */
    private void removeLinesFromRange(int countToRemove) {
        // Find the last source lines in the range and remove them
        int removed = 0;
        int currentLine = endLine;

        while (removed < countToRemove && currentLine >= startLine) {
            if (LpexViewUtils.isSourceLine(view, currentLine)) {
                LpexViewUtils.locateElement(view, currentLine);
                LpexViewUtils.deleteLine(view);
                removed++;
            }
            currentLine--;
        }
    }
}
