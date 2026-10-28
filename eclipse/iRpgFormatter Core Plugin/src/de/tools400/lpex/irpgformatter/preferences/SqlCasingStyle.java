/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferences;

/**
 * Casing of the keywords and of the identifiers of an embedded SQL statement,
 * see IBM preference "Automatic uppercasing" of the ILE RPG SQL formatting. The
 * order matches the values of the IBM preferences "Keywords and Built-in
 * Functions" and "Identifiers".
 */
public enum SqlCasingStyle {

    /** "Uppercase" */
    UPPER,

    /** "Lowercase" */
    LOWER,

    /** "Initial character": first character upper case, the others lower case */
    INITIAL,

    /** "Ignore": casing unchanged */
    IGNORE;

    /**
     * Returns the style of a value of the IBM preferences "Keywords and
     * Built-in Functions" and "Identifiers".
     *
     * @param ibmValue - value of the IBM preference
     * @return the style, {@link #IGNORE} for an unknown value
     */
    public static SqlCasingStyle fromIbmValue(int ibmValue) {

        if (ibmValue >= 0 && ibmValue < values().length) {
            return values()[ibmValue];
        }

        return IGNORE;
    }

    /**
     * Returns the word in this casing style.
     *
     * @param word - the word
     * @return the word in this casing style
     */
    public String apply(String word) {

        switch (this) {
        case UPPER:
            return word.toUpperCase();
        case LOWER:
            return word.toLowerCase();
        case INITIAL:
            if (word.isEmpty()) {
                return word;
            }
            return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
        default:
            return word;
        }
    }
}
