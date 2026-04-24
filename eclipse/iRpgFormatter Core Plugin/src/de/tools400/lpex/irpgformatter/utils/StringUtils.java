/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;

public class StringUtils implements RpgleSourceConstants {

    private static final String SPACE = " ";
    private static final StringBuilder SPACES = new StringBuilder();

    private StringUtils() {
        // Utility class
    }

    public static String getIndent(String line) {

        int i = 0;
        while (i < line.length() && SPACE.equals(line.substring(i, i + 1))) {
            i++;
        }

        return line.substring(0, i);
    }

    public static boolean isLiteral(String value) {

        String trimmed = value.trim();
        if (trimmed.startsWith(SINGLE_QUOTE) && trimmed.endsWith(SINGLE_QUOTE)) {
            return true;
        }

        return false;
    }

    public static boolean isNullOrEmpty(String aValue) {
        if (aValue == null || aValue.length() == 0) {
            return true;
        }
        return false;
    }

    public static String spaces(int count) {

        // Produces a static empty string up to 200 characters
        while (SPACES.length() < Math.max(count, 200)) {
            SPACES.append(SPACE);
        }

        // Extend the return value, if it has to be longer than 200 characters
        if (SPACES.length() < count) {
            StringBuilder spaces = new StringBuilder(SPACES);
            while (spaces.length() < count) {
                if (spaces.length() + SPACES.length() < count) {
                    spaces.append(SPACES);
                } else {
                    spaces.append(SPACES.substring(0, count - spaces.length()));
                }
                return spaces.toString();
            }
        }

        return SPACES.substring(0, count);
    }

    public static String trimL(String aString) {
        return aString.replaceAll("^\\s+", "");
    }

    public static String trimR(String aString) {
        return aString.replaceAll("\\s+$", "");
    }

    public static String padR(String value, int length) {

        if (value.length() > length) {
            return value;
        }

        StringBuilder padded = new StringBuilder();

        padded.append(value);

        while (padded.length() < length) {
            padded.append(" ");
        }

        return padded.toString();
    }
}
