/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.rules.casing.KeywordCasingUtils;

/**
 * Unit tests for {@link KeywordCasingUtils}.
 */
public class KeywordCasingTest extends AbstractTestCase {

    // --- Casing transformation tests ---

    // Simple keywords
    @Test
    public void casingTransformation_Char_UPPERCASE() {
        assertEquals("CHAR", KeywordCasingUtils.apply("Char", KeywordCasingStyle.UPPERCASE));
    }

    @Test
    public void casingTransformation_Char_UPPER_CAMEL() {
        assertEquals("Char", KeywordCasingUtils.apply("Char", KeywordCasingStyle.UPPER_CAMEL));
    }

    @Test
    public void casingTransformation_Char_FIRST_UPPER() {
        assertEquals("Char", KeywordCasingUtils.apply("Char", KeywordCasingStyle.FIRST_UPPER));
    }

    @Test
    public void casingTransformation_Char_LOWER_CAMEL() {
        assertEquals("char", KeywordCasingUtils.apply("Char", KeywordCasingStyle.LOWER_CAMEL));
    }

    @Test
    public void casingTransformation_Char_LOWERCASE() {
        assertEquals("char", KeywordCasingUtils.apply("Char", KeywordCasingStyle.LOWERCASE));
    }

    // Hyphenated keywords
    @Test
    public void casingTransformation_DclDs_UPPERCASE() {
        assertEquals("DCL-DS", KeywordCasingUtils.apply("Dcl-Ds", KeywordCasingStyle.UPPERCASE));
    }

    @Test
    public void casingTransformation_DclDs_UPPER_CAMEL() {
        assertEquals("Dcl-Ds", KeywordCasingUtils.apply("Dcl-Ds", KeywordCasingStyle.UPPER_CAMEL));
    }

    @Test
    public void casingTransformation_DclDs_FIRST_UPPER() {
        assertEquals("Dcl-ds", KeywordCasingUtils.apply("Dcl-Ds", KeywordCasingStyle.FIRST_UPPER));
    }

    @Test
    public void casingTransformation_DclDs_LOWER_CAMEL() {
        assertEquals("dcl-Ds", KeywordCasingUtils.apply("Dcl-Ds", KeywordCasingStyle.LOWER_CAMEL));
    }

    @Test
    public void casingTransformation_DclDs_LOWERCASE() {
        assertEquals("dcl-ds", KeywordCasingUtils.apply("Dcl-Ds", KeywordCasingStyle.LOWERCASE));
    }

    // Star parameters
    @Test
    public void casingTransformation_NoPass_UPPERCASE() {
        assertEquals("*NOPASS", KeywordCasingUtils.apply("*NoPass", KeywordCasingStyle.UPPERCASE));
    }

    @Test
    public void casingTransformation_NoPass_UPPER_CAMEL() {
        assertEquals("*NoPass", KeywordCasingUtils.apply("*NoPass", KeywordCasingStyle.UPPER_CAMEL));
    }

    @Test
    public void casingTransformation_NoPass_FIRST_UPPER() {
        assertEquals("*Nopass", KeywordCasingUtils.apply("*NoPass", KeywordCasingStyle.FIRST_UPPER));
    }

    @Test
    public void casingTransformation_NoPass_LOWER_CAMEL() {
        assertEquals("*noPass", KeywordCasingUtils.apply("*NoPass", KeywordCasingStyle.LOWER_CAMEL));
    }

    @Test
    public void casingTransformation_NoPass_LOWERCASE() {
        assertEquals("*nopass", KeywordCasingUtils.apply("*NoPass", KeywordCasingStyle.LOWERCASE));
    }

    // --- Edge case tests ---

    @Test
    public void edgeCase_nullInput() {
        assertNull(KeywordCasingUtils.apply(null, KeywordCasingStyle.LOWERCASE));
    }

    @Test
    public void edgeCase_emptyInput() {
        assertEquals("", KeywordCasingUtils.apply("", KeywordCasingStyle.LOWERCASE));
    }

    @Test
    public void edgeCase_singleAsterisk() {
        assertEquals("*", KeywordCasingUtils.apply("*", KeywordCasingStyle.UPPERCASE));
    }

    // Single character keywords
    @Test
    public void singleChar_A_UPPERCASE() {
        assertEquals("A", KeywordCasingUtils.apply("A", KeywordCasingStyle.UPPERCASE));
    }

    @Test
    public void singleChar_A_LOWERCASE() {
        assertEquals("a", KeywordCasingUtils.apply("A", KeywordCasingStyle.LOWERCASE));
    }

    @Test
    public void singleChar_a_UPPERCASE() {
        assertEquals("A", KeywordCasingUtils.apply("a", KeywordCasingStyle.UPPERCASE));
    }

    @Test
    public void singleChar_Z_FIRST_UPPER() {
        assertEquals("Z", KeywordCasingUtils.apply("Z", KeywordCasingStyle.FIRST_UPPER));
    }

    @Test
    public void singleChar_z_UPPER_CAMEL() {
        assertEquals("z", KeywordCasingUtils.apply("z", KeywordCasingStyle.UPPER_CAMEL));
    }

    @Test
    public void singleChar_Z_UPPER_CAMEL() {
        assertEquals("Z", KeywordCasingUtils.apply("Z", KeywordCasingStyle.UPPER_CAMEL));
    }

    @Test
    public void edgeCase_trailingHyphen() {
        String result = KeywordCasingUtils.apply("Dcl-", KeywordCasingStyle.UPPERCASE);
        assertEquals("DCL-", result);
    }

    @Test
    public void edgeCase_leadingHyphen() {
        String result = KeywordCasingUtils.apply("-Ds", KeywordCasingStyle.UPPERCASE);
        assertEquals("-DS", result);
    }
}
