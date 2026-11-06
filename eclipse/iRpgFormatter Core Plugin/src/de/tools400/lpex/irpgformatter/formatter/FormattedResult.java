/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

import java.util.Arrays;

/**
 * Wraps the array of {@link FormattedStatement}s produced by the formatter and
 * provides convenience methods for flat-line access.
 */
public class FormattedResult {

    private final FormattedStatement[] statements;
    private final String[] sourceLines;

    /**
     * Creates a result without the source lines. It counts as changed.
     */
    public FormattedResult(FormattedStatement[] statements) {
        this(statements, null);
    }

    /**
     * Creates a result of the given source lines.
     *
     * @param statements - the formatted statements
     * @param sourceLines - the source lines, that were formatted
     */
    public FormattedResult(FormattedStatement[] statements, String[] sourceLines) {
        this.statements = statements;
        this.sourceLines = sourceLines;
    }

    public FormattedStatement[] getStatements() {
        return statements;
    }

    /**
     * Tells, whether the formatted lines differ from the source lines. A result
     * without source lines counts as changed.
     *
     * @return <code>true</code>, if a line was changed, added or removed,
     *         otherwise <code>false</code>
     */
    public boolean isChanged() {

        if (sourceLines == null) {
            return true;
        }

        return !Arrays.equals(sourceLines, toLines());
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
     * Maps a 0-based line index in this result's formatted output to the
     * corresponding 0-based line index in the target result.
     * <p>
     * Both results must originate from the same raw source so that statements
     * correspond 1:1 by index.
     * </p>
     */
    public int mapLineTo(int formattedLine, FormattedResult target) {

        // 1. Find statement index + offset in this result
        int linesSoFar = 0;
        int stmtIndex = -1;
        int offsetInStmt = 0;
        for (int i = 0; i < statements.length; i++) {
            int stmtLines = statements[i].getFormattedLines().length;
            if (formattedLine < linesSoFar + stmtLines) {
                stmtIndex = i;
                offsetInStmt = formattedLine - linesSoFar;
                break;
            }
            linesSoFar += stmtLines;
        }
        if (stmtIndex < 0) {
            return Math.max(0, target.getLineCount() - 1);
        }

        // 2. Map to same statement in target, clamp offset
        FormattedStatement[] targetStmts = target.getStatements();
        if (stmtIndex >= targetStmts.length) {
            return Math.max(0, target.getLineCount() - 1);
        }
        int targetLinesSoFar = 0;
        for (int i = 0; i < stmtIndex; i++) {
            targetLinesSoFar += targetStmts[i].getFormattedLines().length;
        }
        int targetStmtLines = targetStmts[stmtIndex].getFormattedLines().length;
        int clampedOffset = Math.min(offsetInStmt, targetStmtLines - 1);
        return targetLinesSoFar + clampedOffset;
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
