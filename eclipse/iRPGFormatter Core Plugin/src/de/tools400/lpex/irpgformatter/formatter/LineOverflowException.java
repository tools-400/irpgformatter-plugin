/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

import de.tools400.lpex.irpgformatter.Messages;

public class LineOverflowException extends RpgleFormatterException {

    private static final long serialVersionUID = 1L;

    private int startLineNumber = -1;
    private int endLineNumber = -1;

    public LineOverflowException(String value, int lineNumber) {
        super(Messages.bind(Messages.Error_Error_on_line_A_Line_too_small_for_value_A, lineNumber, value));
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
}
