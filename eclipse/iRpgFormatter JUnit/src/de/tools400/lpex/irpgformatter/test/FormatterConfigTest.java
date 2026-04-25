/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.preferences.ParameterSpacingStyle;

public class FormatterConfigTest extends AbstractTestCase {

    @Test
    public void fromPreferences_createsValidConfig() {
        FormatterConfig config = getFormatterConfig();
        assertNotNull(config);
        assertNotNull(config.getKeywordCasingStyle());
        assertNotNull(config.getParameterSpacingStyle());
        assertNotNull(config.getKeywords());
        assertNotNull(config.getDataTypes());
        assertNotNull(config.getDeclarationTypes());
        assertNotNull(config.getSpecialWords());
    }

    @Test
    public void fromPreferences_indentGreaterThanZero() {
        FormatterConfig config = getFormatterConfig();
        assertTrue(config.getIndent() > 0);
    }

    @Test
    public void settersAndGetters() {
        FormatterConfig config = getFormatterConfig();

        config.setIndent(4);
        assertEquals(4, config.getIndent());

        config.setMaxLineWidth(100);
        assertEquals(100, config.getMaxLineWidth());

        config.setMaxNameLength(50);
        assertEquals(50, config.getMaxNameLength());

        config.setMinLiteralLength(15);
        assertEquals(15, config.getMinLiteralLength());

        config.setAlignSubFields(true);
        assertTrue(config.isAlignSubFields());

        config.setAlignSubFields(false);
        assertFalse(config.isAlignSubFields());

        config.setBreakBeforeKeyword(true);
        assertTrue(config.isBreakBeforeKeyword());

        config.setDelimiterBeforeParameter(true);
        assertTrue(config.isDelimiterBeforeParameter());

        config.setBreakBetweenCaseChange(true);
        assertTrue(config.isBreakBetweenCaseChange());

        config.setUseConstKeyword(true);
        assertTrue(config.isUseConstKeyword());

        config.setSortConstValueToEnd(true);
        assertTrue(config.isSortConstValueToEnd());

        config.setKeywordCasingStyle(KeywordCasingStyle.UPPERCASE);
        assertEquals(KeywordCasingStyle.UPPERCASE, config.getKeywordCasingStyle());

        config.setParameterSpacingStyle(ParameterSpacingStyle.BOTH);
        assertEquals(ParameterSpacingStyle.BOTH, config.getParameterSpacingStyle());
    }

    @Test
    public void getEndColumn_belowMaxLineWidth() {
        FormatterConfig config = getFormatterConfig();
        config.setMaxLineWidth(100);
        assertEquals(80, config.getEndColumn(80));
    }

    @Test
    public void getEndColumn_aboveMaxLineWidth() {
        FormatterConfig config = getFormatterConfig();
        config.setMaxLineWidth(100);
        assertEquals(100, config.getEndColumn(120));
    }

    @Test
    public void getEndColumn_equalToMaxLineWidth() {
        FormatterConfig config = getFormatterConfig();
        config.setMaxLineWidth(100);
        assertEquals(100, config.getEndColumn(100));
    }

    @Test
    public void keywordMaps_areNotEmpty() {
        FormatterConfig config = getFormatterConfig();
        assertFalse(config.getKeywords().isEmpty());
        assertFalse(config.getDataTypes().isEmpty());
        assertFalse(config.getDeclarationTypes().isEmpty());
        assertFalse(config.getSpecialWords().isEmpty());
    }
}
