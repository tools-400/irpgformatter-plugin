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
 * Line breaks at the commas and at AND/OR of an embedded SQL statement, see IBM
 * preferences "New line: On comma" and "On AND/OR keywords" of the ILE RPG SQL
 * formatting. The order matches the values of the IBM preferences.
 */
public enum SqlBreakStyle {

    /** "After": line break after the comma or after AND/OR */
    AFTER,

    /** "Before": line break before the comma or before AND/OR */
    BEFORE,

    /** "End of line": no line break */
    NONE;

    /**
     * Returns the style of a value of the IBM preferences.
     *
     * @param ibmValue - value of the IBM preference
     * @return the style, {@link #NONE} for an unknown value
     */
    public static SqlBreakStyle fromIbmValue(int ibmValue) {

        if (ibmValue >= 0 && ibmValue < values().length) {
            return values()[ibmValue];
        }

        return NONE;
    }

    /**
     * Returns the display name of the style, like the IBM preference page.
     *
     * @return the display name
     */
    public String getDisplayName() {

        switch (this) {
        case AFTER:
            return Messages.Label_SQL_break_style_After;
        case BEFORE:
            return Messages.Label_SQL_break_style_Before;
        default:
            return Messages.Label_SQL_break_style_End_of_line;
        }
    }
}
