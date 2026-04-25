package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.rules.casing.FormatKeywordRule;

public class FormatKeywordRuleTest {

    private Map<String, String> keywords;

    @Before
    public void setUp() {
        keywords = new HashMap<>();
        keywords.put("EXTPROC", "ExtProc");
        keywords.put("EXTPGM", "ExtPgm");
        keywords.put("OPDESC", "OpDesc");
        keywords.put("OPTIONS", "Options");
        keywords.put("CONST", "Const");
        keywords.put("VALUE", "Value");
        keywords.put("QUALIFIED", "Qualified");
        keywords.put("TEMPLATE", "Template");
        keywords.put("INZ", "Inz");
        keywords.put("DIM", "Dim");
        keywords.put("EXPORT", "Export");
    }

    @Test
    public void uppercase_extproc() {
        FormatKeywordRule rule = new FormatKeywordRule(keywords, KeywordCasingStyle.UPPERCASE);
        assertEquals("EXTPROC", rule.format("extproc"));
    }

    @Test
    public void lowercase_extproc() {
        FormatKeywordRule rule = new FormatKeywordRule(keywords, KeywordCasingStyle.LOWERCASE);
        assertEquals("extproc", rule.format("EXTPROC"));
    }

    @Test
    public void upperCamel_extproc() {
        FormatKeywordRule rule = new FormatKeywordRule(keywords, KeywordCasingStyle.UPPER_CAMEL);
        assertEquals("ExtProc", rule.format("extproc"));
    }

    @Test
    public void lowerCamel_extproc() {
        FormatKeywordRule rule = new FormatKeywordRule(keywords, KeywordCasingStyle.LOWER_CAMEL);
        assertEquals("extproc", rule.format("EXTPROC"));
    }

    @Test
    public void firstUpper_extproc() {
        FormatKeywordRule rule = new FormatKeywordRule(keywords, KeywordCasingStyle.FIRST_UPPER);
        assertEquals("Extproc", rule.format("EXTPROC"));
    }

    @Test
    public void uppercase_qualified() {
        FormatKeywordRule rule = new FormatKeywordRule(keywords, KeywordCasingStyle.UPPERCASE);
        assertEquals("QUALIFIED", rule.format("qualified"));
    }

    @Test
    public void uppercase_template() {
        FormatKeywordRule rule = new FormatKeywordRule(keywords, KeywordCasingStyle.UPPERCASE);
        assertEquals("TEMPLATE", rule.format("template"));
    }

    @Test
    public void lowercase_inz() {
        FormatKeywordRule rule = new FormatKeywordRule(keywords, KeywordCasingStyle.LOWERCASE);
        assertEquals("inz", rule.format("INZ"));
    }

    @Test
    public void unknownKeyword_passThrough() {
        FormatKeywordRule rule = new FormatKeywordRule(keywords, KeywordCasingStyle.UPPERCASE);
        assertEquals("myIdentifier", rule.format("myIdentifier"));
    }

    @Test
    public void allStyles_opdesc() {
        assertEquals("OPDESC", new FormatKeywordRule(keywords, KeywordCasingStyle.UPPERCASE).format("opdesc"));
        assertEquals("OpDesc", new FormatKeywordRule(keywords, KeywordCasingStyle.UPPER_CAMEL).format("opdesc"));
        assertEquals("Opdesc", new FormatKeywordRule(keywords, KeywordCasingStyle.FIRST_UPPER).format("opdesc"));
        assertEquals("opdesc", new FormatKeywordRule(keywords, KeywordCasingStyle.LOWER_CAMEL).format("opdesc"));
        assertEquals("opdesc", new FormatKeywordRule(keywords, KeywordCasingStyle.LOWERCASE).format("opdesc"));
    }
}
