/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

public class LineOverflowException extends RpgleFormatterException {

    private static final long serialVersionUID = 1L;

    private int startLineNumber = -1;
    private int endLineNumber = -1;

    public LineOverflowException(String value) {
        super(String.format("Line length too small for value: [%s]", value));
    }

    public int getStartLineNumber() {
        return startLineNumber;
    }

    public int getEndLineNumber() {
        return endLineNumber;
    }

    public void setLineNumbers(int startLineNumber, int endLineNumber) {
        this.startLineNumber = startLineNumber;
        this.endLineNumber = endLineNumber;
    }

    @Override
    public String getMessage() {
        if (startLineNumber >= 0) {
            return super.getMessage() + String.format(" (lines %d-%d)", startLineNumber + 1, endLineNumber + 1);
        }
        return super.getMessage();
    }
}
