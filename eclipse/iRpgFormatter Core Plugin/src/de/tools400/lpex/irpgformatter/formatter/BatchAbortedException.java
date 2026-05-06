/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

/**
 * Thrown when the user declines to connect an offline system, signalling that
 * the entire batch job should be aborted rather than just skipping the current
 * member.
 */
public class BatchAbortedException extends Exception {

    private static final long serialVersionUID = 1L;

    public BatchAbortedException() {
        super();
    }
}
