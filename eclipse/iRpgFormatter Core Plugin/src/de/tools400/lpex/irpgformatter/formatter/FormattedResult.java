/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

/**
 * Wraps the array of {@link FormattedStatement}s produced by the formatter
 * and provides convenience methods for flat-line access.
 */
public class FormattedResult {

    private final FormattedStatement[] statements;

    public FormattedResult(FormattedStatement[] statements) {
        this.statements = statements;
    }

    public FormattedStatement[] getStatements() {
        return statements;
    }

    /**
     * Flattens all formatted lines into a single array, preserving order.
     */
    public String[] toLines() {
        int total = getLineCount();
        String[] lines = new String[total];
        int index = 0;
        for (FormattedStatement stmt : statements) {
            for (String line : stmt.getFormattedLines()) {
                lines[index++] = line;
            }
        }
        return lines;
    }

    /**
     * Returns the total number of formatted lines across all statements.
     */
    public int getLineCount() {
        int count = 0;
        for (FormattedStatement stmt : statements) {
            count += stmt.getFormattedLines().length;
        }
        return count;
    }
}
