/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.FormattedStatement;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;

/**
 * Writes formatted source lines back to a specific range in an LPEX view.
 * Uses statement-aware update logic: processes statements from bottom to top
 * so that insertions/deletions do not shift positions of earlier statements.
 */
public class RpgleLpexOutput implements IRpgleOutput {

    private final LineEditor editor;

    /**
     * Creates a new output backed by the given {@link LineEditor}.
     *
     * @param editor the line editor
     */
    public RpgleLpexOutput(LineEditor editor) {
        this.editor = editor;
    }

    @Override
    public boolean writeSourceLines(FormattedResult result) throws RpgleFormatterException {
        FormattedStatement[] statements = result.getStatements();

        // Process from bottom to top so that line insertions/deletions
        // do not affect the positions of earlier (higher-up) statements.
        for (int i = statements.length - 1; i >= 0; i--) {
            FormattedStatement stmt = statements[i];
            int origStart = stmt.getOriginalStartLine();
            int origCount = stmt.getOriginalLineCount();
            String[] newLines = stmt.getFormattedLines();

            // 1. Check if statement is unchanged → skip entirely
            if (linesAreEqual(origStart, origCount, newLines)) {
                continue;
            }

            // 2. Overwrite common lines
            int updateCount = Math.min(origCount, newLines.length);
            for (int j = 0; j < updateCount; j++) {
                editor.setElementText(origStart + j, newLines[j]);
            }

            // 3a. More lines than original → insert after last overwritten line
            if (newLines.length > origCount) {
                for (int j = origCount; j < newLines.length; j++) {
                    editor.locateElement(origStart + j - 1);
                    editor.addLine();
                    editor.setElementText(origStart + j, newLines[j]);
                }
            }
            // 3b. Fewer lines than original → delete surplus lines (from bottom)
            else if (newLines.length < origCount) {
                for (int j = 0; j < origCount - newLines.length; j++) {
                    editor.locateElement(origStart + newLines.length);
                    editor.deleteLine();
                }
            }
        }

        return true;
    }

    private boolean linesAreEqual(int startLine, int count, String[] newLines) {
        if (count != newLines.length) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            if (!editor.getElementText(startLine + i).equals(newLines[i])) {
                return false;
            }
        }
        return true;
    }
}
