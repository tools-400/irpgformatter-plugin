package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.rules.NullFormattingRule;

public class NullFormattingRuleTest {

    @Test
    public void returnsValueUnchanged() {
        NullFormattingRule rule = new NullFormattingRule();
        assertEquals("dcl-s myVar char(10);", rule.format("dcl-s myVar char(10);"));
    }

    @Test
    public void returnsEmptyString() {
        NullFormattingRule rule = new NullFormattingRule();
        assertEquals("", rule.format(""));
    }

    @Test
    public void returnsNull() {
        NullFormattingRule rule = new NullFormattingRule();
        assertNull(rule.format(null));
    }

    @Test
    public void preservesWhitespace() {
        NullFormattingRule rule = new NullFormattingRule();
        assertEquals("  spaces  ", rule.format("  spaces  "));
    }
}
