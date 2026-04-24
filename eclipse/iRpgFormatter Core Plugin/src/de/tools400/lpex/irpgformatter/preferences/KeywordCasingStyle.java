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
    UPPERCASE (Messages.Label_All_Uppercase, "DCL-DS"),

    /** UpperCamelCase: Dcl-Ds, *NoPass */
    UPPER_CAMEL (Messages.Label_UpperCamelCase, "Dcl-Ds"),

    /** First char uppercase: Dcl-ds, *Nopass */
    FIRST_UPPER (Messages.Label_First_Char_Uppercase, "Dcl-ds"),

    /** lowerCamelCase: dcl-Ds, *noPass */
    LOWER_CAMEL (Messages.Label_lowerCamelCase, "dcl-Ds"),

    /** All lowercase: dcl-ds, *nopass */
    LOWERCASE (Messages.Label_All_lowercase, "dcl-ds");

    private final String displayName;
    private final String example;

    KeywordCasingStyle(String displayName, String example) {
        this.displayName = displayName;
        this.example = example;
    }

    /**
     * Gets the display name for this casing style.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets an example keyword formatted in this style.
     *
     * @return example formatted keyword
     */
    // public String getExample() {
    // return example;
    // }

    @Override
    public String toString() {
        return displayName + " (" + example + ")";
    }

    /**
     * Gets a casing style by its name.
     *
     * @param name the enum UI label
     * @return the casing style, or LOWERCASE if not found
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
