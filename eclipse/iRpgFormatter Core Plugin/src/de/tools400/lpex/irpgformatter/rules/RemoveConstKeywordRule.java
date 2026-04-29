/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules;

import de.tools400.lpex.irpgformatter.preferences.Preferences;

public class RemoveConstKeywordRule implements IFormattingRule, RpgleSourceConstants {

    private final Boolean useConstKeyword;

    public RemoveConstKeywordRule() {
        this.useConstKeyword = null;
    }

    public RemoveConstKeywordRule(boolean useConstKeyword) {
        this.useConstKeyword = Boolean.valueOf(useConstKeyword);
    }

    @Override
    public String format(String value) {

        String result;

        boolean constEnabled;
        if (useConstKeyword != null) {
            constEnabled = useConstKeyword.booleanValue();
        } else {
            constEnabled = Preferences.getInstance().isUseConstKeyword();
        }

        String key = value.toUpperCase();
        if ("CONST".equals(key) && !constEnabled) {
            result = "";
        } else {
            result = value;
        }

        return result;
    }
}
