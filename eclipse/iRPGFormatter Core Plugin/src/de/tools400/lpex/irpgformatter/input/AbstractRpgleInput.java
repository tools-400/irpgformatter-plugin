/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;

public abstract class AbstractRpgleInput {

    private Boolean isFreeFormat;

    public abstract String getFirstSourceLine() throws RpgleFormatterException;

    public int getStartLineNumber() {
        return 1;
    }

    public boolean isFreeFormat() throws RpgleFormatterException {

        if (isFreeFormat == null) {
            String line = getFirstSourceLine();
            if (line != null && line.toUpperCase().startsWith("**FREE")) {
                isFreeFormat = true;
            } else {
                isFreeFormat = false;
            }
        }

        return isFreeFormat;
    }
}
