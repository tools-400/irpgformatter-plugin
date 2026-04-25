/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.rules.casing.FormatSpecialWordsRule;

public class FormatSpecialWordsRuleTest {

    private Map<String, String> specialWords;

    @Before
    public void setUp() {
        specialWords = new HashMap<>();
        specialWords.put("*NOPASS", "*NoPass");
        specialWords.put("*OMIT", "*Omit");
        specialWords.put("*VARSIZE", "*VarSize");
        specialWords.put("*STRING", "*String");
        specialWords.put("*ON", "*On");
        specialWords.put("*OFF", "*Off");
        specialWords.put("*BLANKS", "*Blanks");
        specialWords.put("*ZEROS", "*Zeros");
        specialWords.put("*NULL", "*Null");
    }

    @Test
    public void uppercase_nopass() {
        FormatSpecialWordsRule rule = new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.UPPERCASE);
        assertEquals("*NOPASS", rule.format("*nopass"));
    }

    @Test
    public void lowercase_nopass() {
        FormatSpecialWordsRule rule = new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.LOWERCASE);
        assertEquals("*nopass", rule.format("*NOPASS"));
    }

    @Test
    public void upperCamel_nopass() {
        FormatSpecialWordsRule rule = new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.UPPER_CAMEL);
        assertEquals("*NoPass", rule.format("*nopass"));
    }

    @Test
    public void lowerCamel_nopass() {
        FormatSpecialWordsRule rule = new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.LOWER_CAMEL);
        assertEquals("*noPass", rule.format("*NOPASS"));
    }

    @Test
    public void firstUpper_nopass() {
        FormatSpecialWordsRule rule = new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.FIRST_UPPER);
        assertEquals("*Nopass", rule.format("*NOPASS"));
    }

    @Test
    public void uppercase_omit() {
        FormatSpecialWordsRule rule = new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.UPPERCASE);
        assertEquals("*OMIT", rule.format("*omit"));
    }

    @Test
    public void uppercase_varsize() {
        FormatSpecialWordsRule rule = new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.UPPERCASE);
        assertEquals("*VARSIZE", rule.format("*varsize"));
    }

    @Test
    public void upperCamel_varsize() {
        FormatSpecialWordsRule rule = new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.UPPER_CAMEL);
        assertEquals("*VarSize", rule.format("*varsize"));
    }

    @Test
    public void unknownSpecialWord_passThrough() {
        FormatSpecialWordsRule rule = new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.UPPERCASE);
        assertEquals("*CUSTOM", rule.format("*CUSTOM"));
    }

    @Test
    public void allStyles_blanks() {
        assertEquals("*BLANKS", new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.UPPERCASE).format("*blanks"));
        assertEquals("*Blanks", new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.UPPER_CAMEL).format("*blanks"));
        assertEquals("*Blanks", new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.FIRST_UPPER).format("*blanks"));
        assertEquals("*blanks", new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.LOWER_CAMEL).format("*blanks"));
        assertEquals("*blanks", new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.LOWERCASE).format("*blanks"));
    }

    @Test
    public void allStyles_on() {
        assertEquals("*ON", new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.UPPERCASE).format("*on"));
        assertEquals("*On", new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.UPPER_CAMEL).format("*on"));
        assertEquals("*on", new FormatSpecialWordsRule(specialWords, KeywordCasingStyle.LOWERCASE).format("*ON"));
    }
}
