package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.rules.casing.FormatCompilerDirectiveRule;

public class FormatCompilerDirectiveRuleTest {

    @Test
    public void uppercase_copy() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.UPPERCASE);
        assertEquals("/COPY library/file.member", rule.format("/copy library/file.member"));
    }

    @Test
    public void uppercase_include() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.UPPERCASE);
        assertEquals("/INCLUDE library/file.member", rule.format("/include library/file.member"));
    }

    @Test
    public void lowercase_copy() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.LOWERCASE);
        assertEquals("/copy library/file.member", rule.format("/COPY library/file.member"));
    }

    @Test
    public void lowercase_include() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.LOWERCASE);
        assertEquals("/include library/file.member", rule.format("/INCLUDE library/file.member"));
    }

    @Test
    public void upperCamel_copy() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.UPPER_CAMEL);
        assertEquals("/Copy library/file.member", rule.format("/COPY library/file.member"));
    }

    @Test
    public void firstUpper_include() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.FIRST_UPPER);
        assertEquals("/Include library/file.member", rule.format("/INCLUDE library/file.member"));
    }

    @Test
    public void lowerCamel_copy() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.LOWER_CAMEL);
        assertEquals("/copy library/file.member", rule.format("/COPY library/file.member"));
    }

    @Test
    public void directiveWithoutParameters() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.UPPERCASE);
        assertEquals("/EOF", rule.format("/eof"));
    }

    @Test
    public void directiveWithoutParameters_lowercase() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.LOWERCASE);
        assertEquals("/eof", rule.format("/EOF"));
    }

    @Test
    public void nonDirective_passThrough() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.UPPERCASE);
        assertEquals("dcl-s myVar char(10);", rule.format("dcl-s myVar char(10);"));
    }

    @Test
    public void nullInput() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.UPPERCASE);
        assertNull(rule.format(null));
    }

    @Test
    public void emptyInput() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.UPPERCASE);
        assertEquals("", rule.format(""));
    }

    @Test
    public void parametersPreservedAsIs() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.LOWERCASE);
        assertEquals("/copy QSYSINC/QRPGLESRC.QUSEC", rule.format("/COPY QSYSINC/QRPGLESRC.QUSEC"));
    }

    @Test
    public void uppercase_if() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.UPPERCASE);
        assertEquals("/IF DEFINED(MYDEF)", rule.format("/if DEFINED(MYDEF)"));
    }

    @Test
    public void uppercase_define() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.UPPERCASE);
        assertEquals("/DEFINE MYDEF", rule.format("/define MYDEF"));
    }

    @Test
    public void uppercase_endif() {
        FormatCompilerDirectiveRule rule = new FormatCompilerDirectiveRule(KeywordCasingStyle.UPPERCASE);
        assertEquals("/ENDIF", rule.format("/endif"));
    }
}
