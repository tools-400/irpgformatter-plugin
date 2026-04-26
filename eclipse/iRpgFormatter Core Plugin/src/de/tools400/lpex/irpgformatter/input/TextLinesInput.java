/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;

public class TextLinesInput extends AbstractRpgleInput implements IRpgleInput {

    private final String[] lines;
    private final String name;

    public TextLinesInput(String... lines) {
        this.lines = lines;
        this.name = "TextLinesInput";
    }

    @Override
    public boolean isFreeFormat() throws RpgleFormatterException {
        return true;
    }

    @Override
    public String getFirstSourceLine() throws RpgleFormatterException {
        return lines[0];
    }

    @Override
    public String[] getSourceLines() {
        return lines;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getSourceType() throws Exception {
        throw new IllegalAccessError("Not implemented.");
    }

    @Override
    public IRpgleOutput getOutput() {
        // Not needed for formatter tests - return no-op implementation
        return result -> {
            return false;
        };
    }
}
