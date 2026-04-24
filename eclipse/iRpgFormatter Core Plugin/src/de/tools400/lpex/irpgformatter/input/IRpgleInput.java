/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;

/**
 * Interface for abstracting RPGLE source input. Allows the formatter to work
 * with different input types.
 */
public interface IRpgleInput {

    /**
     * Checks whether the source is in <b>**free</b> format.
     * 
     * @return <code>true</code> if source is in <b>**free</b>, otherwise
     *         <code>false</code>
     * @throws RpgleFormatterException
     */
    public boolean isFreeFormat() throws RpgleFormatterException;

    /**
     * Returns the first line of the source code.
     */
    public String getFirstSourceLine() throws RpgleFormatterException;

    /**
     * Returns the source lines from the input.
     *
     * @return array of source lines
     * @throws RpgleFormatterException if the source cannot be read
     */
    public String[] getSourceLines() throws RpgleFormatterException;

    /**
     * Returns the name of the input source (for error reporting).
     *
     * @return the name of the input source
     */
    public String getName();

    /**
     * Returns the type of the source member.
     *
     * @return the type of the input source member
     */
    public String getSourceType() throws Exception;

    /**
     * Returns the 1-based start line number of this input within the overall
     * source. For full-file inputs this is 0; for range-based inputs (e.g. LPEX
     * selection) it reflects the actual position in the source member.
     *
     * @return 1-based start line number
     */
    public int getStartLineNumber();

    /**
     * Returns an output handler for writing formatted source back.
     *
     * @return the output handler for this input
     */
    public IRpgleOutput getOutput();
}
