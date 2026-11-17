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
 * The exception holds the messages of all errors and their line numbers,
 * ordered by the line numbers. {@link #getMessage()} returns the first
 * message.
 * </p>
 */
public class BlockStructureException extends RpgleFormatterException {

    private static final long serialVersionUID = 1L;

    private String[] errorMessages;
    private int[] lineNumbers;

    /**
     * Creates the exception.
     *
     * @param errorMessages - the messages of the errors, ordered by their line
     *        number, at least one
     * @param lineNumbers - the line numbers of the errors, one per message
     */
    public BlockStructureException(String[] errorMessages, int[] lineNumbers) {
        super(errorMessages[0]);

        if (errorMessages.length != lineNumbers.length) {
            throw new IllegalArgumentException("One line number per message expected."); //$NON-NLS-1$
        }

        this.errorMessages = errorMessages.clone();
        this.lineNumbers = lineNumbers.clone();
    }

    public String[] getErrorMessages() {
        return errorMessages.clone();
    }

    /**
     * Returns the line numbers of the errors in the order of
     * {@link #getErrorMessages()}.
     */
    public int[] getLineNumbers() {
        return lineNumbers.clone();
    }

    public int getErrorCount() {
        return errorMessages.length;
    }

    public int getFirstLineNumber() {
        return lineNumbers[0];
    }
}
