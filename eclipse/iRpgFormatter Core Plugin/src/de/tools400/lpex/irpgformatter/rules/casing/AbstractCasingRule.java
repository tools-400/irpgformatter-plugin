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

public abstract class AbstractCasingRule {

    private Map<String, String> formattingMap;
    private KeywordCasingStyle keywordCasingStyle;

    public AbstractCasingRule(Map<String, String> formattingMap) {
        this.formattingMap = formattingMap;
        this.keywordCasingStyle = null;
    }

    public AbstractCasingRule(Map<String, String> formattingMap, KeywordCasingStyle keywordCasingStyle) {
        this.formattingMap = formattingMap;
        this.keywordCasingStyle = keywordCasingStyle;
    }

    protected Map<String, String> getFormattingMap() {
        return formattingMap;
    }

    /**
     * Gets the keyword casing style. Returns the value passed via the
     * constructor, or falls back to the {@link Preferences} singleton.
     *
     * @return the keyword casing style
     */
    protected KeywordCasingStyle getKeywordCasingStyle() {
        if (keywordCasingStyle != null) {
            return keywordCasingStyle;
        }
        return Preferences.getInstance().getKeywordCasingStyle();
    }
}
