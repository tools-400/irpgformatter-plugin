/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

/**
 * Exception thrown, when the blocks of a source are not closed properly, such
 * as a <code>DCL-DS</code> without <code>END-DS</code> in front of
 * <code>END-PROC</code>. The source is not formatted then, because the
 * indentation of the following statements would be wrong.
 * <p>
 * The exception holds the messages of all errors, ordered by their line
 * number. {@link #getMessage()} returns the first one.
 * </p>
 */
public class BlockStructureException extends RpgleFormatterException {

    private static final long serialVersionUID = 1L;

    private String[] errorMessages;
    private int firstLineNumber;

    /**
     * Creates the exception.
     *
     * @param errorMessages - the messages of the errors, ordered by their line
     *        number, at least one
     * @param firstLineNumber - the line number of the first error
     */
    public BlockStructureException(String[] errorMessages, int firstLineNumber) {
        super(errorMessages[0]);

        this.errorMessages = errorMessages.clone();
        this.firstLineNumber = firstLineNumber;
    }

    public String[] getErrorMessages() {
        return errorMessages.clone();
    }

    public int getErrorCount() {
        return errorMessages.length;
    }

    public int getFirstLineNumber() {
        return firstLineNumber;
    }
}
