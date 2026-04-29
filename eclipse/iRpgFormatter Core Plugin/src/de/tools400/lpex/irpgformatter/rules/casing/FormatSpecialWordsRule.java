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
import de.tools400.lpex.irpgformatter.rules.IFormattingRule;

public class FormatSpecialWordsRule extends AbstractCasingRule implements IFormattingRule {

    public FormatSpecialWordsRule() {
        super(Preferences.getInstance().getSpecialWords());
    }

    public FormatSpecialWordsRule(Map<String, String> specialWords, KeywordCasingStyle keywordCasingStyle) {
        super(specialWords, keywordCasingStyle);
    }

    @Override
    public String format(String value) {

        String result;

        Map<String, String> keywordsMap = getFormattingMap();

        String key = value.toUpperCase();
        String canonical = keywordsMap.get(key);
        if (canonical != null) {
            result = KeywordCasingUtils.apply(canonical, getKeywordCasingStyle());
        } else {
            result = value;
        }

        return result;
    }
}
