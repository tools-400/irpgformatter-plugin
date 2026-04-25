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
import static org.junit.Assert.assertTrue;

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

    @Test
    public void toString_upperCamel_containsExample() {
        String str = KeywordCasingStyle.UPPER_CAMEL.toString();
        assertTrue(str.contains("Upper Camel Case"));
        assertTrue(str.contains("Dcl-Ds"));
    }

    @Test
    public void toString_lowercase_containsExample() {
        String str = KeywordCasingStyle.LOWERCASE.toString();
        assertTrue(str.contains("All Lowercase"));
        assertTrue(str.contains("dcl-ds"));
    }

    // --- values ---

    @Test
    public void values_hasFiveStyles() {
        assertEquals(5, KeywordCasingStyle.values().length);
    }

    // --- getDisplayName ---

    @Test
    public void getDisplayName_uppercase() {
        assertEquals("All Uppercase", KeywordCasingStyle.UPPERCASE.getDisplayName());
    }

    @Test
    public void getDisplayName_upperCamel() {
        assertEquals("Upper Camel Case", KeywordCasingStyle.UPPER_CAMEL.getDisplayName());
    }

    @Test
    public void getDisplayName_firstUpper() {
        assertEquals("First Char Uppercase", KeywordCasingStyle.FIRST_UPPER.getDisplayName());
    }

    @Test
    public void getDisplayName_lowerCamel() {
        assertEquals("Lower Camel Case", KeywordCasingStyle.LOWER_CAMEL.getDisplayName());
    }

    @Test
    public void getDisplayName_lowercase() {
        assertEquals("All Lowercase", KeywordCasingStyle.LOWERCASE.getDisplayName());
    }

    // --- fromLabel ---

    @Test
    public void fromLabel_uppercase() {
        assertEquals(KeywordCasingStyle.UPPERCASE, KeywordCasingStyle.fromLabel("All Uppercase"));
    }

    @Test
    public void fromLabel_upperCamel() {
        assertEquals(KeywordCasingStyle.UPPER_CAMEL, KeywordCasingStyle.fromLabel("Upper Camel Case"));
    }

    @Test
    public void fromLabel_firstUpper() {
        assertEquals(KeywordCasingStyle.FIRST_UPPER, KeywordCasingStyle.fromLabel("First Char Uppercase"));
    }

    @Test
    public void fromLabel_lowerCamel() {
        assertEquals(KeywordCasingStyle.LOWER_CAMEL, KeywordCasingStyle.fromLabel("Lower Camel Case"));
    }

    @Test
    public void fromLabel_lowercase() {
        assertEquals(KeywordCasingStyle.LOWERCASE, KeywordCasingStyle.fromLabel("All Lowercase"));
    }

    @Test
    public void fromLabel_null_returnsDefault() {
        KeywordCasingStyle style = KeywordCasingStyle.fromLabel(null);
        assertNotNull(style);
        assertEquals(KeywordCasingStyle.LOWERCASE, style);
    }

    @Test
    public void fromLabel_unknown_returnsDefault() {
        KeywordCasingStyle style = KeywordCasingStyle.fromLabel("NonExistentStyle");
        assertNotNull(style);
        assertEquals(KeywordCasingStyle.LOWERCASE, style);
    }

    // --- valueOf ---

    @Test
    public void valueOf_uppercase() {
        assertEquals(KeywordCasingStyle.UPPERCASE, KeywordCasingStyle.valueOf("UPPERCASE"));
    }

    @Test
    public void valueOf_lowercase() {
        assertEquals(KeywordCasingStyle.LOWERCASE, KeywordCasingStyle.valueOf("LOWERCASE"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOf_invalid_throws() {
        KeywordCasingStyle.valueOf("INVALID");
    }
}
