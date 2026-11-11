/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

/**
 * Holds a single formatted statement together with its original position and
 * size in the source, so that output handlers can perform statement-aware
 * updates (skip unchanged, insert/delete lines).
 */
public class FormattedStatement {

    private final int originalStartLine;
    private final int originalLineCount;
    private final String[] formattedLines;

    /**
     * @param originalStartLine 1-based start line of the statement in the
     *        original source
     * @param originalLineCount number of lines the statement occupied in the
     *        original source (including embedded comments)
     * @param formattedLines the formatted output lines for this statement
     */
    public FormattedStatement(int originalStartLine, int originalLineCount, String[] formattedLines) {
        this.originalStartLine = originalStartLine;
        this.originalLineCount = originalLineCount;
        this.formattedLines = formattedLines;
    }

    public int getOriginalStartLine() {
        return originalStartLine;
    }

    public int getOriginalLineCount() {
        return originalLineCount;
    }

    public String[] getFormattedLines() {
        return formattedLines;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (String line : formattedLines) {
            buffer.append(line);
        }
        return buffer.toString();
    }
}
