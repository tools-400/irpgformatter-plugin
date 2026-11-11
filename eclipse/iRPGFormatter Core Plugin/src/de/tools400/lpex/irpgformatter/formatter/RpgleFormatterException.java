/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

/**
 * Exception thrown when an error occurs during RPGLE formatting.
 */
public class RpgleFormatterException extends Exception {

    private static final long serialVersionUID = 1L;

    public RpgleFormatterException(String message) {
        super(message);
    }

    public RpgleFormatterException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpgleFormatterException(Throwable cause) {
        super(cause);
    }
}
