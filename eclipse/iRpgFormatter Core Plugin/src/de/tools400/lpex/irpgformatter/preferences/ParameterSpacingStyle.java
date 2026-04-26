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
    NONE (":*omit:"),

    /** Space before parameter (after colon) */
    BEFORE (": *omit:"),

    /** Space after parameter (before colon) */
    AFTER (":*omit :"),

    /** Space before and after parameter */
    BOTH (": *omit :");

    private final String example;

    ParameterSpacingStyle(String example) {
        this.example = example;
    }

    /**
     * Gets the display name for this spacing style. The name is resolved
     * dynamically from NLS messages so it always reflects the active locale.
     *
     * @return the display name
     */
    public String getDisplayName() {
        switch (this) {
        case NONE:
            return Messages.Label_No_space_parameter;
        case BEFORE:
            return Messages.Label_Before_parameter;
        case AFTER:
            return Messages.Label_After_parameter;
        case BOTH:
            return Messages.Label_Before_after_parameter;
        default:
            return name();
        }
    }

    @Override
    public String toString() {
        return getDisplayName() + " (" + example + ")";
    }

    /**
     * Gets a spacing style by its display name.
     *
     * @param label the UI label
     * @return the spacing style, or the default if not found
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
