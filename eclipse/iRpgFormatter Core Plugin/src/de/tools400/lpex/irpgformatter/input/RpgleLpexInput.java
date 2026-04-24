/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.util.ArrayList;
import java.util.List;

import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.utils.FileUtils;
import de.tools400.lpex.irpgformatter.utils.LpexViewUtils;

/**
 * {@link IRpgleInput} implementation that wraps an LPEX view.
 */
public class RpgleLpexInput extends AbstractRpgleInput implements IRpgleInput {

    private final LpexView view;
    private final int startLine;
    private final int endLine;
    private final boolean isRange;

    /**
     * Creates an input for the entire LPEX view.
     *
     * @param view the LPEX view
     */
    RpgleLpexInput(LpexView view) {
        this(view, 1, LpexViewUtils.getNumLines(view), false);
    }

    /**
     * Creates an input for a specific line range.
     *
     * @param view the LPEX view
     * @param startLine the first line to read (1-based)
     * @param endLine the last line to read (1-based, inclusive)
     */
    RpgleLpexInput(LpexView view, int startLine, int endLine) {
        this(view, startLine, endLine, true);
    }

    private RpgleLpexInput(LpexView view, int startLine, int endLine, boolean isRange) {
        this.view = view;
        this.startLine = startLine;
        this.endLine = endLine;
        this.isRange = isRange;
    }

    @Override
    public String getFirstSourceLine() throws RpgleFormatterException {

        int numLines = view.elements();

        String line = "";

        for (int index = 1; index <= numLines; index++) {
            if (LpexViewUtils.isSourceLine(view, index)) {
                line = LpexViewUtils.getElementText(view, index);
                break;
            }
        }

        return line;
    }

    @Override
    public String[] getSourceLines() throws RpgleFormatterException {

        List<String> lines = new ArrayList<>();
        for (int i = startLine; i <= endLine; i++) {
            if (LpexViewUtils.isSourceLine(view, i)) {
                lines.add(LpexViewUtils.getElementText(view, i));
            }
        }
        return lines.toArray(new String[0]);
    }

    @Override
    public String getName() {
        String name = LpexViewUtils.getName(view);
        if (name == null) {
            name = "*N";
        }
        if (isRange) {
            return name + " [lines: " + startLine + "-" + endLine + "]";
        }
        return name;
    }

    public String getSourceType() throws Exception {
        return FileUtils.getExtension(getName());
    }

    @Override
    public int getStartLineNumber() {
        return startLine;
    }

    /**
     * Returns the start line of the range.
     */
    public int getStartLine() {
        return startLine;
    }

    /**
     * Returns the end line of the range.
     */
    public int getEndLine() {
        return endLine;
    }

    @Override
    public IRpgleOutput getOutput() {
        return new RpgleLpexOutput(view, startLine, endLine);
    }
}
