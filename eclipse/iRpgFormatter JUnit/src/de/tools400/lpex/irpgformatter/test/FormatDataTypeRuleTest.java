package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.rules.casing.FormatDataTypeRule;

public class FormatDataTypeRuleTest {

    private Map<String, String> dataTypes;

    @Before
    public void setUp() {
        dataTypes = new HashMap<>();
        dataTypes.put("CHAR", "Char");
        dataTypes.put("VARCHAR", "VarChar");
        dataTypes.put("INT", "Int");
        dataTypes.put("UNS", "Uns");
        dataTypes.put("PACKED", "Packed");
        dataTypes.put("ZONED", "Zoned");
        dataTypes.put("BINDEC", "BinDec");
        dataTypes.put("FLOAT", "Float");
        dataTypes.put("DATE", "Date");
        dataTypes.put("TIME", "Time");
        dataTypes.put("TIMESTAMP", "Timestamp");
        dataTypes.put("IND", "Ind");
        dataTypes.put("POINTER", "Pointer");
        dataTypes.put("LIKEDS", "LikeDs");
        dataTypes.put("LIKE", "Like");
    }

    @Test
    public void uppercase_char() {
        FormatDataTypeRule rule = new FormatDataTypeRule(dataTypes, KeywordCasingStyle.UPPERCASE);
        assertEquals("CHAR", rule.format("char"));
    }

    @Test
    public void lowercase_char() {
        FormatDataTypeRule rule = new FormatDataTypeRule(dataTypes, KeywordCasingStyle.LOWERCASE);
        assertEquals("char", rule.format("CHAR"));
    }

    @Test
    public void upperCamel_varchar() {
        FormatDataTypeRule rule = new FormatDataTypeRule(dataTypes, KeywordCasingStyle.UPPER_CAMEL);
        assertEquals("VarChar", rule.format("varchar"));
    }

    @Test
    public void lowerCamel_varchar() {
        FormatDataTypeRule rule = new FormatDataTypeRule(dataTypes, KeywordCasingStyle.LOWER_CAMEL);
        assertEquals("varchar", rule.format("VARCHAR"));
    }

    @Test
    public void firstUpper_packed() {
        FormatDataTypeRule rule = new FormatDataTypeRule(dataTypes, KeywordCasingStyle.FIRST_UPPER);
        assertEquals("Packed", rule.format("PACKED"));
    }

    @Test
    public void uppercase_likeds() {
        FormatDataTypeRule rule = new FormatDataTypeRule(dataTypes, KeywordCasingStyle.UPPERCASE);
        assertEquals("LIKEDS", rule.format("likeds"));
    }

    @Test
    public void unknownDataType_passThrough() {
        FormatDataTypeRule rule = new FormatDataTypeRule(dataTypes, KeywordCasingStyle.UPPERCASE);
        assertEquals("myCustomType", rule.format("myCustomType"));
    }

    @Test
    public void allStyles_timestamp() {
        assertEquals("TIMESTAMP", new FormatDataTypeRule(dataTypes, KeywordCasingStyle.UPPERCASE).format("timestamp"));
        assertEquals("Timestamp", new FormatDataTypeRule(dataTypes, KeywordCasingStyle.UPPER_CAMEL).format("timestamp"));
        assertEquals("Timestamp", new FormatDataTypeRule(dataTypes, KeywordCasingStyle.FIRST_UPPER).format("timestamp"));
        assertEquals("timestamp", new FormatDataTypeRule(dataTypes, KeywordCasingStyle.LOWER_CAMEL).format("timestamp"));
        assertEquals("timestamp", new FormatDataTypeRule(dataTypes, KeywordCasingStyle.LOWERCASE).format("timestamp"));
    }

    @Test
    public void upperCamel_binDec() {
        FormatDataTypeRule rule = new FormatDataTypeRule(dataTypes, KeywordCasingStyle.UPPER_CAMEL);
        assertEquals("BinDec", rule.format("bindec"));
    }
}
