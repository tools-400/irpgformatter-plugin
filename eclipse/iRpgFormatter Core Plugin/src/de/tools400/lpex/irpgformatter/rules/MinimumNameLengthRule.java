/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules;

import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;

public class MinimumNameLengthRule {

    private final FormatterConfig config;

    public MinimumNameLengthRule(FormatterConfig config) {
        this.config = config;
    }

    public boolean isSatisfiedBy(int length) {

        if (length >= config.getMinLiteralLength()) {
            return true;
        }

        return false;
    }
}
