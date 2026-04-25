/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.ParameterSpacingStyle;
import de.tools400.lpex.irpgformatter.rules.FormatParameterRule;

public class FormatParameterRuleTest extends AbstractTestCase {

    private FormatParameterRule createRule(ParameterSpacingStyle style, boolean delimiterBefore) {
        FormatterConfig config = getFormatterConfig();
        config.setParameterSpacingStyle(style);
        config.setDelimiterBeforeParameter(delimiterBefore);
        return new FormatParameterRule(config);
    }

    // --- Delimiter after parameter (default) ---

    @Test
    public void spacingNone_delimAfter_firstParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.NONE, false);
        assertEquals("*omit:", rule.format("*omit", 0, 3));
    }

    @Test
    public void spacingNone_delimAfter_middleParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.NONE, false);
        assertEquals("*omit:", rule.format("*omit", 1, 3));
    }

    @Test
    public void spacingNone_delimAfter_lastParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.NONE, false);
        assertEquals("*omit", rule.format("*omit", 2, 3));
    }

    @Test
    public void spacingBefore_delimAfter_firstParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.BEFORE, false);
        assertEquals("*omit:", rule.format("*omit", 0, 3));
    }

    @Test
    public void spacingBefore_delimAfter_middleParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.BEFORE, false);
        assertEquals(" *omit:", rule.format("*omit", 1, 3));
    }

    @Test
    public void spacingBefore_delimAfter_lastParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.BEFORE, false);
        assertEquals(" *omit", rule.format("*omit", 2, 3));
    }

    @Test
    public void spacingAfter_delimAfter_middleParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.AFTER, false);
        assertEquals("*omit :", rule.format("*omit", 1, 3));
    }

    @Test
    public void spacingBoth_delimAfter_middleParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.BOTH, false);
        assertEquals(" *omit :", rule.format("*omit", 1, 3));
    }

    @Test
    public void spacingBoth_delimAfter_firstParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.BOTH, false);
        assertEquals(" *omit :", rule.format("*omit", 0, 3));
    }

    @Test
    public void spacingBoth_delimAfter_lastParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.BOTH, false);
        assertEquals(" *omit ", rule.format("*omit", 2, 3));
    }

    // --- Delimiter before parameter ---

    @Test
    public void spacingNone_delimBefore_firstParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.NONE, true);
        assertEquals("*omit", rule.format("*omit", 0, 3));
    }

    @Test
    public void spacingNone_delimBefore_middleParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.NONE, true);
        assertEquals(":*omit", rule.format("*omit", 1, 3));
    }

    @Test
    public void spacingNone_delimBefore_lastParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.NONE, true);
        assertEquals(":*omit", rule.format("*omit", 2, 3));
    }

    @Test
    public void spacingBefore_delimBefore_middleParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.BEFORE, true);
        assertEquals(": *omit", rule.format("*omit", 1, 3));
    }

    @Test
    public void spacingAfter_delimBefore_middleParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.AFTER, true);
        assertEquals(":*omit ", rule.format("*omit", 1, 3));
    }

    @Test
    public void spacingBoth_delimBefore_middleParam() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.BOTH, true);
        assertEquals(": *omit ", rule.format("*omit", 1, 3));
    }

    // --- Single parameter ---

    @Test
    public void singleParameter_delimAfter() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.BEFORE, false);
        assertEquals("*omit", rule.format("*omit", 0, 1));
    }

    @Test
    public void singleParameter_delimBefore() {
        FormatParameterRule rule = createRule(ParameterSpacingStyle.BEFORE, true);
        assertEquals("*omit", rule.format("*omit", 0, 1));
    }
}
