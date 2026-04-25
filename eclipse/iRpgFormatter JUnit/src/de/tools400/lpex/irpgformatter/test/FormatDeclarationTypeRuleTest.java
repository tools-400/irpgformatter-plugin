package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.rules.casing.FormatDeclarationTypeRule;

public class FormatDeclarationTypeRuleTest {

    private Map<String, String> declarationTypes;

    @Before
    public void setUp() {
        declarationTypes = new HashMap<>();
        declarationTypes.put("DCL-S", "Dcl-S");
        declarationTypes.put("DCL-C", "Dcl-C");
        declarationTypes.put("DCL-DS", "Dcl-Ds");
        declarationTypes.put("DCL-PR", "Dcl-Pr");
        declarationTypes.put("DCL-PI", "Dcl-Pi");
        declarationTypes.put("DCL-F", "Dcl-F");
        declarationTypes.put("DCL-PROC", "Dcl-Proc");
        declarationTypes.put("DCL-ENUM", "Dcl-Enum");
        declarationTypes.put("END-DS", "End-Ds");
        declarationTypes.put("END-PR", "End-Pr");
        declarationTypes.put("END-PI", "End-Pi");
        declarationTypes.put("END-PROC", "End-Proc");
        declarationTypes.put("END-ENUM", "End-Enum");
        declarationTypes.put("CTL-OPT", "Ctl-Opt");
    }

    @Test
    public void uppercase_dclDs() {
        FormatDeclarationTypeRule rule = new FormatDeclarationTypeRule(declarationTypes, KeywordCasingStyle.UPPERCASE);
        assertEquals("DCL-DS", rule.format("dcl-ds"));
    }

    @Test
    public void lowercase_dclDs() {
        FormatDeclarationTypeRule rule = new FormatDeclarationTypeRule(declarationTypes, KeywordCasingStyle.LOWERCASE);
        assertEquals("dcl-ds", rule.format("DCL-DS"));
    }

    @Test
    public void upperCamel_dclDs() {
        FormatDeclarationTypeRule rule = new FormatDeclarationTypeRule(declarationTypes, KeywordCasingStyle.UPPER_CAMEL);
        assertEquals("Dcl-Ds", rule.format("dcl-ds"));
    }

    @Test
    public void lowerCamel_dclDs() {
        FormatDeclarationTypeRule rule = new FormatDeclarationTypeRule(declarationTypes, KeywordCasingStyle.LOWER_CAMEL);
        assertEquals("dcl-Ds", rule.format("DCL-DS"));
    }

    @Test
    public void firstUpper_dclDs() {
        FormatDeclarationTypeRule rule = new FormatDeclarationTypeRule(declarationTypes, KeywordCasingStyle.FIRST_UPPER);
        assertEquals("Dcl-ds", rule.format("DCL-DS"));
    }

    @Test
    public void uppercase_endDs() {
        FormatDeclarationTypeRule rule = new FormatDeclarationTypeRule(declarationTypes, KeywordCasingStyle.UPPERCASE);
        assertEquals("END-DS", rule.format("end-ds"));
    }

    @Test
    public void lowercase_ctlOpt() {
        FormatDeclarationTypeRule rule = new FormatDeclarationTypeRule(declarationTypes, KeywordCasingStyle.LOWERCASE);
        assertEquals("ctl-opt", rule.format("CTL-OPT"));
    }

    @Test
    public void uppercase_dclProc() {
        FormatDeclarationTypeRule rule = new FormatDeclarationTypeRule(declarationTypes, KeywordCasingStyle.UPPERCASE);
        assertEquals("DCL-PROC", rule.format("dcl-proc"));
    }

    @Test
    public void unknownDeclarationType_passThrough() {
        FormatDeclarationTypeRule rule = new FormatDeclarationTypeRule(declarationTypes, KeywordCasingStyle.UPPERCASE);
        assertEquals("myCustomDecl", rule.format("myCustomDecl"));
    }

    @Test
    public void uppercase_dclEnum() {
        FormatDeclarationTypeRule rule = new FormatDeclarationTypeRule(declarationTypes, KeywordCasingStyle.UPPERCASE);
        assertEquals("DCL-ENUM", rule.format("dcl-enum"));
    }

    @Test
    public void uppercase_endEnum() {
        FormatDeclarationTypeRule rule = new FormatDeclarationTypeRule(declarationTypes, KeywordCasingStyle.UPPERCASE);
        assertEquals("END-ENUM", rule.format("end-enum"));
    }
}
