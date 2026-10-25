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
import de.tools400.lpex.irpgformatter.rules.IFormattingRule;

/**
 * Base class of the casing rules, that look up a word in a table of canonical
 * spellings: keywords, data types, declaration types and special words.
 */
public abstract class AbstractLookupCasingRule extends AbstractCasingRule implements IFormattingRule {

    /**
     * Canonical spellings by the word in upper case.
     */
    private final Map<String, String> formattingMap;

    /**
     * Creates a rule, that takes the keyword casing style from the
     * preferences.
     */
    public AbstractLookupCasingRule(Map<String, String> formattingMap) {
        this.formattingMap = formattingMap;
    }

    public AbstractLookupCasingRule(Map<String, String> formattingMap, KeywordCasingStyle keywordCasingStyle) {
        super(keywordCasingStyle);
        this.formattingMap = formattingMap;
    }

    /**
     * Returns the canonical spelling of the word in the keyword casing style
     * or the word unchanged, if it is not in the table.
     */
    @Override
    public final String format(String value) {

        String canonical = formattingMap.get(value.toUpperCase());
        if (canonical == null) {
            return value;
        }

        return KeywordCasingUtils.apply(canonical, getKeywordCasingStyle());
    }
}
