/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;

/**
 * Interface for writing formatted RPGLE source back to the target.
 */
public interface IRpgleOutput {

    /**
     * Writes the formatted source lines to the output.
     *
     * @param result the formatted result containing statement-level mapping
     * @throws RpgleFormatterException if writing fails
     */
    public boolean writeSourceLines(FormattedResult result) throws RpgleFormatterException;
}
