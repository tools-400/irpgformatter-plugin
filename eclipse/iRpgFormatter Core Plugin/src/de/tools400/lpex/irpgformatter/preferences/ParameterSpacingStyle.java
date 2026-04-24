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
public enum ParameterSpacingStyle {

    /** No space around colon delimiter */
    NONE (Messages.Label_No_space_parameter, ":*omit:"),

    /** Space before parameter (after colon) */
    BEFORE (Messages.Label_Before_parameter, ": *omit:"),

    /** Space after parameter (before colon) */
    AFTER (Messages.Label_After_parameter, ":*omit :"),

    /** Space before and after parameter */
    BOTH (Messages.Label_Before_after_parameter, ": *omit :");

    private final String displayName;
    private final String example;

    ParameterSpacingStyle(String displayName, String example) {
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
     * @param label the enum UI label
     * @return the casing style, or LOWERCASE if not found
     */
    public static ParameterSpacingStyle fromLabel(String label) {
        if (label != null) {
            for (ParameterSpacingStyle style : values()) {
                if (style.getDisplayName().equals(label)) {
                    return style;
                }
            }
        }
        return Preferences.getInstance().getDefaultParameterSpacingStyle();
    }
}
