/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.api;

/**
 * A problem, that iRPGFormatter reported for a file, as it is shown in the view
 * <i>iRPGFormatter Problems</i>.
 */
public final class FileFormatProblem {

    public enum Severity {
        /** A statement or the file could not be formatted. */
        ERROR,
        /** The file was formatted, but the indentation may be wrong. */
        WARNING,
        /** The file was skipped, e.g. because it is not in free format. */
        INFORMATION;
    }

    private final Severity severity;
    private final int lineNumber;
    private final String message;

    /**
     * Creates a problem.
     *
     * @param severity - the severity
     * @param lineNumber - the line of the problem in the file, as it is now, or
     *        0, if the problem has no line
     * @param message - the message, in the language of RDi
     */
    public FileFormatProblem(Severity severity, int lineNumber, String message) {
        this.severity = severity;
        this.lineNumber = lineNumber;
        this.message = message;
    }

    public Severity getSeverity() {
        return severity;
    }

    /**
     * Returns the line of the problem in the file, as it is after formatting.
     *
     * @return the line number or 0, if the problem has no line, e.g. a file,
     *         that cannot be read
     */
    public int getLineNumber() {
        return lineNumber;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return severity + ":" + lineNumber + ": " + message; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
