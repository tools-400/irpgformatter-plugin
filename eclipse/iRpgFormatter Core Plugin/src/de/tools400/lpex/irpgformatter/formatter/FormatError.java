/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

import de.tools400.lpex.irpgformatter.parser.StatementType;

/**
 * Captures information about a single statement that could not be formatted.
 * The original (unformatted) source lines of the statement are preserved in
 * the formatted result; this object lets the caller report the failure to
 * the user without losing the underlying cause.
 */
public class FormatError {

    private final int startLineNumber;
    private final int endLineNumber;
    private final StatementType statementType;
    private final String message;
    private final Throwable cause;

    /**
     * @param startLineNumber 0-based start line of the failing statement
     * @param endLineNumber 0-based end line of the failing statement
     * @param statementType the statement type that failed to format
     * @param message human-readable error message (typically
     *        {@code exception.getMessage()})
     * @param cause the underlying throwable for unexpected failures, or
     *        {@code null} for expected formatter errors (e.g.
     *        {@link LineOverflowException}) that do not warrant a stack trace
     *        in the Eclipse error log
     */
    public FormatError(int startLineNumber, int endLineNumber, StatementType statementType, String message, Throwable cause) {
        this.startLineNumber = startLineNumber;
        this.endLineNumber = endLineNumber;
        this.statementType = statementType;
        this.message = message;
        this.cause = cause;
    }

    public int getStartLineNumber() {
        return startLineNumber;
    }

    public int getEndLineNumber() {
        return endLineNumber;
    }

    public StatementType getStatementType() {
        return statementType;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }
}
