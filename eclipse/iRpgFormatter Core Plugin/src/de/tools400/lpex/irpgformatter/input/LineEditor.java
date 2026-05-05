/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

/**
 * Abstraction for line-based editing operations on a source document. Allows
 * production code to work with LPEX views while tests use simple in-memory
 * implementations.
 */
public interface LineEditor {

    public String getElementText(int lineNumber);

    public void setElementText(int lineNumber, String text);

    /**
     * Inserts a new empty line after {@code element} (1-based) without moving
     * the visible cursor.
     */
    public void addLineAt(int element);

    /**
     * Deletes the line at {@code element} (1-based) without moving the visible
     * cursor.
     */
    public void deleteLineAt(int element);
}
