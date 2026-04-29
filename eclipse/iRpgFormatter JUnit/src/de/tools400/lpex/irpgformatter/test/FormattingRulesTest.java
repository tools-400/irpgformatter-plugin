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
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.rules.FormattingRules;
import de.tools400.lpex.irpgformatter.rules.IFormattingRule;
import de.tools400.lpex.irpgformatter.rules.NullFormattingRule;

/**
 * Unit tests for {@link FormattingRules}.
 */
public class FormattingRulesTest extends AbstractTestCase {

    // --- createIndent tests ---

    @Test
    public void createIndent_level0() {
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        assertEquals("", rules.createIndent(0));
    }

    @Test
    public void createIndent_level1() {
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        assertEquals("  ", rules.createIndent(1));
    }

    @Test
    public void createIndent_level2() {
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        assertEquals("    ", rules.createIndent(2));
    }

    @Test
    public void createIndent_level3() {
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        assertEquals("      ", rules.createIndent(3));
    }

    @Test
    public void createIndent_lengthIsLevelTimesIndentSize() {
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        for (int level = 0; level <= 5; level++) {
            String indent = rules.createIndent(level);
            assertEquals(level * getIndentSize(), indent.length());
        }
    }

    // --- createIndent with custom indent size ---

    @Test
    public void createIndent_level3_indent4() {
        getFormatterConfig().setIndent(4);
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        assertEquals("            ", rules.createIndent(3));
        assertEquals(12, rules.createIndent(3).length());
    }

    // --- formatKeyword ---

    @Test
    public void formatKeyword_lowercase() {
        getFormatterConfig().setKeywordCasingStyle(KeywordCasingStyle.LOWERCASE);
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        String result = rules.formatKeyword("ExtProc");
        assertNotNull(result);
    }

    @Test
    public void formatKeyword_uppercase() {
        getFormatterConfig().setKeywordCasingStyle(KeywordCasingStyle.UPPERCASE);
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        String result = rules.formatKeyword("extproc");
        assertNotNull(result);
    }

    // --- formatSpecialWord ---

    @Test
    public void formatSpecialWord_returnsFormatted() {
        getFormatterConfig().setKeywordCasingStyle(KeywordCasingStyle.UPPERCASE);
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        String result = rules.formatSpecialWord("*nopass");
        assertNotNull(result);
    }

    // --- formatDataType ---

    @Test
    public void formatDataType_returnsFormatted() {
        getFormatterConfig().setKeywordCasingStyle(KeywordCasingStyle.UPPERCASE);
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        String result = rules.formatDataType("char");
        assertNotNull(result);
    }

    // --- formatCompilerDirective ---

    @Test
    public void formatCompilerDirective_returnsFormatted() {
        getFormatterConfig().setKeywordCasingStyle(KeywordCasingStyle.UPPERCASE);
        FormattingRules rules = new FormattingRules(getFormatterConfig());
        String result = rules.formatCompilerDirective("/copy");
        assertNotNull(result);
    }

    // --- applyFormattingRule (static) ---

    @Test
    public void applyFormattingRule_noLiterals() {
        NullFormattingRule rule = new NullFormattingRule();
        assertEquals("dcl-s myVar char(10);", FormattingRules.applyFormattingRule("dcl-s myVar char(10);", rule));
    }

    @Test
    public void applyFormattingRule_withLiteral_preservesLiteral() {
        IFormattingRule toUpper = new IFormattingRule() {
            @Override
            public String format(String value) {
                return value.toUpperCase();
            }
        };
        String result = FormattingRules.applyFormattingRule("inz('hello')", toUpper);
        // The non-literal part should be uppercased, but the literal preserved
        assertEquals("INZ('hello')", result);
    }

    @Test
    public void applyFormattingRule_withEmptyLiteral() {
        IFormattingRule toUpper = new IFormattingRule() {
            @Override
            public String format(String value) {
                return value.toUpperCase();
            }
        };
        String result = FormattingRules.applyFormattingRule("inz('')", toUpper);
        assertEquals("INZ('')", result);
    }

    @Test
    public void applyFormattingRule_noLiterals_appliesRule() {
        IFormattingRule toUpper = new IFormattingRule() {
            @Override
            public String format(String value) {
                return value.toUpperCase();
            }
        };
        assertEquals("DCL-S MYVAR CHAR(10);", FormattingRules.applyFormattingRule("dcl-s myVar char(10);", toUpper));
    }

    @Test
    public void applyFormattingRule_multipleRules() {
        IFormattingRule trimRule = new IFormattingRule() {
            @Override
            public String format(String value) {
                return value.trim();
            }
        };
        IFormattingRule toUpper = new IFormattingRule() {
            @Override
            public String format(String value) {
                return value.toUpperCase();
            }
        };
        assertEquals("HELLO", FormattingRules.applyFormattingRule("  hello  ", trimRule, toUpper));
    }

    @Test
    public void applyFormattingRule_withEscapedQuoteInsideLiteral() {
        IFormattingRule toUpper = new IFormattingRule() {
            @Override
            public String format(String value) {
                return value.toUpperCase();
            }
        };
        // 'it''s' contains an escaped quote inside a literal - both '' must be preserved
        String result = FormattingRules.applyFormattingRule("inz('it''s a test')", toUpper);
        assertEquals("INZ('it''s a test')", result);
    }

    private int getIndentSize() {
        return Preferences.getInstance().getIndent();
    }
}
