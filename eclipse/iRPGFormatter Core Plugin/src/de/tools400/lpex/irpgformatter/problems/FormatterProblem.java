/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.problems;

/**
 * A problem of a source, that was formatted: an error or a warning with its
 * message and the line, to which the view goes.
 */
public class FormatterProblem {

    public enum Severity {
        ERROR,
        WARNING;
    }

    private final IProblemSource source;
    private final Severity severity;
    private final int lineNumber;
    private final String message;

    /**
     * Creates a problem.
     *
     * @param source - the source of the problem
     * @param severity - error or warning
     * @param lineNumber - the line of the problem in the source, as it is now,
     *        or 0, if the problem has no line, e.g. a source, that cannot be
     *        read
     * @param message - the message for the user
     */
    public FormatterProblem(IProblemSource source, Severity severity, int lineNumber, String message) {
        this.source = source;
        this.severity = severity;
        this.lineNumber = lineNumber;
        this.message = message;
    }

    public IProblemSource getSource() {
        return source;
    }

    public Severity getSeverity() {
        return severity;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return source.getLabel() + ":" + lineNumber + ": " + message; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
