/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.casing;

import java.util.Map;

import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.preferences.Preferences;

public class FormatSpecialWordsRule extends AbstractLookupCasingRule {

    public FormatSpecialWordsRule() {
        super(Preferences.getInstance().getSpecialWords());
    }

    public FormatSpecialWordsRule(Map<String, String> specialWords, KeywordCasingStyle keywordCasingStyle) {
        super(specialWords, keywordCasingStyle);
    }
}
