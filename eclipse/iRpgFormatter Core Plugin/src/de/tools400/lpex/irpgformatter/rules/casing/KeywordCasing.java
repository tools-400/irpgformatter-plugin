/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.casing;

import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;

/**
 * Utility class for applying casing transformations to RPG keywords.
 * <p>
 * Keywords are stored in UpperCamelCase format (e.g., "Dcl-Ds", "*NoPass") and
 * transformed to the target casing style.
 * </p>
 */
public final class KeywordCasing {

    private KeywordCasing() {
        // Utility class
    }

    /**
     * Applies the specified casing style to a keyword.
     * <p>
     * Handles hyphenated keywords (e.g., "Dcl-Ds") and keyword parameters
     * starting with * (e.g., "*NoPass").
     * </p>
     *
     * @param keyword the keyword in UpperCamelCase format
     * @param style the target casing style
     * @return the keyword formatted in the target style
     */
    public static String apply(String keyword, KeywordCasingStyle style) {
        if (keyword == null || keyword.isEmpty()) {
            return keyword;
        }

        switch (style) {
        case UPPERCASE:
            return keyword.toUpperCase();

        case UPPER_CAMEL:
            // Already in UpperCamelCase format
            return keyword;

        case FIRST_UPPER:
            return toFirstCharUppercase(keyword);

        case LOWER_CAMEL:
            return toLowerCamelCase(keyword);

        case LOWERCASE:
        default:
            return keyword.toLowerCase();
        }
    }

    /**
     * Converts a keyword to First Char Uppercase style.
     * <p>
     * Example: "Dcl-Ds" -> "Dcl-ds", "*NoPass" -> "*Nopass"
     * </p>
     */
    private static String toFirstCharUppercase(String keyword) {
        if (keyword.startsWith("*")) {
            // Handle *params: *NoPass -> *Nopass
            if (keyword.length() > 1) {
                return "*" + capitalizeFirst(keyword.substring(1).toLowerCase());
            }
            return keyword;
        }

        if (keyword.contains("-")) {
            // Handle hyphenated: Dcl-Ds -> Dcl-ds
            String[] parts = keyword.split("-");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    result.append("-");
                }
                if (i == 0) {
                    result.append(capitalizeFirst(parts[i].toLowerCase()));
                } else {
                    result.append(parts[i].toLowerCase());
                }
            }
            return result.toString();
        }

        // Simple keyword: Char -> Char
        return capitalizeFirst(keyword.toLowerCase());
    }

    /**
     * Converts a keyword to lowerCamelCase style.
     * <p>
     * Example: "Dcl-Ds" -> "dcl-Ds", "*NoPass" -> "*noPass"
     * </p>
     */
    private static String toLowerCamelCase(String keyword) {
        if (keyword.startsWith("*")) {
            // Handle *params: *NoPass -> *noPass
            if (keyword.length() > 1) {
                String rest = keyword.substring(1);
                return "*" + lowercaseFirst(rest);
            }
            return keyword;
        }

        if (keyword.contains("-")) {
            // Handle hyphenated: Dcl-Ds -> dcl-Ds
            String[] parts = keyword.split("-");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    result.append("-");
                }
                if (i == 0) {
                    result.append(parts[i].toLowerCase());
                } else {
                    // Preserve case of subsequent parts
                    result.append(capitalizeFirst(parts[i].toLowerCase()));
                }
            }
            return result.toString();
        }

        // Simple keyword: Char -> char
        return keyword.toLowerCase();
    }

    /**
     * Capitalizes the first character of a string.
     */
    private static String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Lowercases the first character of a string while preserving the rest.
     */
    private static String lowercaseFirst(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
