package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.utils.StringUtils;

public class StringUtilsTest {

    // --- getIndent ---

    @Test
    public void getIndent_noIndent() {
        assertEquals("", StringUtils.getIndent("dcl-s myVar char(10);"));
    }

    @Test
    public void getIndent_twoSpaces() {
        assertEquals("  ", StringUtils.getIndent("  custId char(10);"));
    }

    @Test
    public void getIndent_fourSpaces() {
        assertEquals("    ", StringUtils.getIndent("    custId char(10);"));
    }

    @Test
    public void getIndent_emptyLine() {
        assertEquals("", StringUtils.getIndent(""));
    }

    @Test
    public void getIndent_onlySpaces() {
        assertEquals("     ", StringUtils.getIndent("     "));
    }

    // --- isLiteral ---

    @Test
    public void isLiteral_simpleString() {
        assertTrue(StringUtils.isLiteral("'hello'"));
    }

    @Test
    public void isLiteral_withSpaces() {
        assertTrue(StringUtils.isLiteral("  'hello'  "));
    }

    @Test
    public void isLiteral_notALiteral() {
        assertFalse(StringUtils.isLiteral("hello"));
    }

    @Test
    public void isLiteral_onlyOpenQuote() {
        assertFalse(StringUtils.isLiteral("'hello"));
    }

    @Test
    public void isLiteral_emptyLiteral() {
        assertTrue(StringUtils.isLiteral("''"));
    }

    // --- isNullOrEmpty ---

    @Test
    public void isNullOrEmpty_null() {
        assertTrue(StringUtils.isNullOrEmpty(null));
    }

    @Test
    public void isNullOrEmpty_empty() {
        assertTrue(StringUtils.isNullOrEmpty(""));
    }

    @Test
    public void isNullOrEmpty_notEmpty() {
        assertFalse(StringUtils.isNullOrEmpty("text"));
    }

    @Test
    public void isNullOrEmpty_whitespace() {
        assertFalse(StringUtils.isNullOrEmpty(" "));
    }

    // --- spaces ---

    @Test
    public void spaces_zero() {
        assertEquals("", StringUtils.spaces(0));
    }

    @Test
    public void spaces_five() {
        assertEquals("     ", StringUtils.spaces(5));
    }

    @Test
    public void spaces_correctLength() {
        assertEquals(100, StringUtils.spaces(100).length());
    }

    // --- trimL ---

    @Test
    public void trimL_leadingSpaces() {
        assertEquals("hello  ", StringUtils.trimL("   hello  "));
    }

    @Test
    public void trimL_noLeadingSpaces() {
        assertEquals("hello", StringUtils.trimL("hello"));
    }

    @Test
    public void trimL_onlySpaces() {
        assertEquals("", StringUtils.trimL("   "));
    }

    // --- trimR ---

    @Test
    public void trimR_trailingSpaces() {
        assertEquals("   hello", StringUtils.trimR("   hello  "));
    }

    @Test
    public void trimR_noTrailingSpaces() {
        assertEquals("hello", StringUtils.trimR("hello"));
    }

    // --- padR ---

    @Test
    public void padR_shorterThanLength() {
        assertEquals("abc   ", StringUtils.padR("abc", 6));
    }

    @Test
    public void padR_equalToLength() {
        assertEquals("abc", StringUtils.padR("abc", 3));
    }

    @Test
    public void padR_longerThanLength() {
        assertEquals("abcdef", StringUtils.padR("abcdef", 3));
    }
}
