/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.casing;

import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.preferences.Preferences;

/**
 * Base class of the casing rules. Holds the keyword casing style.
 */
public abstract class AbstractCasingRule {

    private final KeywordCasingStyle keywordCasingStyle;

    /**
     * Creates a rule, that takes the keyword casing style from the
     * {@link Preferences}.
     */
    public AbstractCasingRule() {
        this(null);
    }

    public AbstractCasingRule(KeywordCasingStyle keywordCasingStyle) {
        this.keywordCasingStyle = keywordCasingStyle;
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
