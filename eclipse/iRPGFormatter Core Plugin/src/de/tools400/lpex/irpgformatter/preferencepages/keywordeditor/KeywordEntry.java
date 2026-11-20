/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages.keywordeditor;

/**
 * Represents a keyword entry with a key (uppercase, for matching) and a value
 * (UpperCamelCase, canonical form for casing transformation).
 */
public class KeywordEntry {

    private final String key;
    private String value;

    /**
     * Creates a new keyword entry.
     *
     * @param key the uppercase key (read-only, used for matching)
     * @param value the UpperCamelCase value (editable, canonical form)
     */
    public KeywordEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Gets the key (uppercase, read-only).
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the value (UpperCamelCase, editable).
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value (UpperCamelCase, editable).
     *
     * @param value the new value
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    /**
     * Returns <code>true</code>, if the value is the key in another casing, as
     * the formatter requires it: the value only defines the casing of the word,
     * see
     * {@link de.tools400.lpex.irpgformatter.rules.casing.AbstractLookupCasingRule}.
     *
     * @param key - the key
     * @param value - the value or <code>null</code>
     * @return <code>true</code> for a valid value
     */
    public static boolean isValidValue(String key, String value) {
        return value != null && value.equalsIgnoreCase(key);
    }
}
