/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferences;

import de.tools400.lpex.irpgformatter.Messages;

/**
 * Enumeration of keyword casing styles for RPG formatting.
 */
public enum KeywordCasingStyle {

    /** All uppercase: DCL-DS, *NOPASS */
    UPPERCASE ("DCL-DS"),

    /** UpperCamelCase: Dcl-Ds, *NoPass */
    UPPER_CAMEL ("Dcl-Ds"),

    /** First char uppercase: Dcl-ds, *Nopass */
    FIRST_UPPER ("Dcl-ds"),

    /** lowerCamelCase: dcl-Ds, *noPass */
    LOWER_CAMEL ("dcl-Ds"),

    /** All lowercase: dcl-ds, *nopass */
    LOWERCASE ("dcl-ds");

    private final String example;

    KeywordCasingStyle(String example) {
        this.example = example;
    }

    /**
     * Gets the display name for this casing style. The name is resolved
     * dynamically from NLS messages so it always reflects the active locale.
     *
     * @return the display name
     */
    public String getDisplayName() {
        switch (this) {
        case UPPERCASE:
            return Messages.Label_All_Uppercase;
        case UPPER_CAMEL:
            return Messages.Label_UpperCamelCase;
        case FIRST_UPPER:
            return Messages.Label_First_Char_Uppercase;
        case LOWER_CAMEL:
            return Messages.Label_lowerCamelCase;
        case LOWERCASE:
            return Messages.Label_All_lowercase;
        default:
            return name();
        }
    }

    @Override
    public String toString() {
        return getDisplayName() + " (" + example + ")";
    }

    /**
     * Gets a casing style by its display name.
     *
     * @param label the UI label
     * @return the casing style, or the default if not found
     */
    public static KeywordCasingStyle fromLabel(String label) {
        if (label != null) {
            for (KeywordCasingStyle style : values()) {
                if (style.getDisplayName().equals(label)) {
                    return style;
                }
            }
        }
        return Preferences.getInstance().getDefaultKeywordCasingStyle();
    }
}
