/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;

/**
 * Unit tests for {@link KeywordCasingStyle}.
 */
public class KeywordCasingStyleTest extends AbstractTestCase {

    // --- fromName tests ---

    // @Test
    // public void fromName_uppercase() {
    // assertEquals(KeywordCasingStyle.UPPERCASE,
    // KeywordCasingStyle.fromName("UPPERCASE"));
    // }
    //
    // @Test
    // public void fromName_upperCamel() {
    // assertEquals(KeywordCasingStyle.UPPER_CAMEL,
    // KeywordCasingStyle.fromName("UPPER_CAMEL"));
    // }
    //
    // @Test
    // public void fromName_firstUpper() {
    // assertEquals(KeywordCasingStyle.FIRST_UPPER,
    // KeywordCasingStyle.fromName("FIRST_UPPER"));
    // }
    //
    // @Test
    // public void fromName_lowerCamel() {
    // assertEquals(KeywordCasingStyle.LOWER_CAMEL,
    // KeywordCasingStyle.fromName("LOWER_CAMEL"));
    // }
    //
    // @Test
    // public void fromName_lowercase() {
    // assertEquals(KeywordCasingStyle.LOWERCASE,
    // KeywordCasingStyle.fromName("LOWERCASE"));
    // }
    //
    // @Test
    // public void fromName_nullInput() {
    // assertEquals(KeywordCasingStyle.LOWERCASE,
    // KeywordCasingStyle.fromName(null));
    // }
    //
    // @Test
    // public void fromName_emptyString() {
    // assertEquals(KeywordCasingStyle.LOWERCASE,
    // KeywordCasingStyle.fromName(""));
    // }
    //
    // @Test
    // public void fromName_invalidName_invalid() {
    // assertEquals(KeywordCasingStyle.LOWERCASE,
    // KeywordCasingStyle.fromName("invalid"));
    // }
    //
    // @Test
    // public void fromName_invalidName_UNKNOWN() {
    // assertEquals(KeywordCasingStyle.LOWERCASE,
    // KeywordCasingStyle.fromName("UNKNOWN"));
    // }
    //
    // @Test
    // public void fromName_invalidName_lowercaseString() {
    // assertEquals(KeywordCasingStyle.LOWERCASE,
    // KeywordCasingStyle.fromName("lowercase"));
    // }
    //
    // @Test
    // public void fromName_invalidName_uppercaseString() {
    // assertEquals(KeywordCasingStyle.LOWERCASE,
    // KeywordCasingStyle.fromName("Uppercase"));
    // }

    // --- enum property tests ---

    @Test
    public void allStylesHaveNonNullDisplayName() {
        for (KeywordCasingStyle style : KeywordCasingStyle.values()) {
            assertNotNull(style.getDisplayName());
        }
    }

    @Test
    public void allStylesHaveNonNullToString() {
        for (KeywordCasingStyle style : KeywordCasingStyle.values()) {
            assertNotNull(style.toString());
        }
    }

    // --- toString format tests ---

    @Test
    public void toStringFormat_includesDisplayNameAndExample() {
        String result = KeywordCasingStyle.UPPERCASE.toString();
        assertEquals("All Uppercase (DCL-DS)", result);
    }
}
