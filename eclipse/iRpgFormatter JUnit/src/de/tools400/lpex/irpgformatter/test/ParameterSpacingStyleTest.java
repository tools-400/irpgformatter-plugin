package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.preferences.ParameterSpacingStyle;

public class ParameterSpacingStyleTest extends AbstractTestCase {

    // --- values ---

    @Test
    public void values_hasFourStyles() {
        assertEquals(4, ParameterSpacingStyle.values().length);
    }

    // --- getDisplayName ---

    @Test
    public void getDisplayName_none() {
        assertEquals("No space", ParameterSpacingStyle.NONE.getDisplayName());
    }

    @Test
    public void getDisplayName_before() {
        assertEquals("Before parameter", ParameterSpacingStyle.BEFORE.getDisplayName());
    }

    @Test
    public void getDisplayName_after() {
        assertEquals("After parameter", ParameterSpacingStyle.AFTER.getDisplayName());
    }

    @Test
    public void getDisplayName_both() {
        assertEquals("Before/after parameter", ParameterSpacingStyle.BOTH.getDisplayName());
    }

    // --- toString ---

    @Test
    public void toString_none_containsExample() {
        String str = ParameterSpacingStyle.NONE.toString();
        assertTrue(str.contains("No space"));
        assertTrue(str.contains(":*omit:"));
    }

    @Test
    public void toString_before_containsExample() {
        String str = ParameterSpacingStyle.BEFORE.toString();
        assertTrue(str.contains("Before parameter"));
        assertTrue(str.contains(": *omit:"));
    }

    @Test
    public void toString_after_containsExample() {
        String str = ParameterSpacingStyle.AFTER.toString();
        assertTrue(str.contains("After parameter"));
        assertTrue(str.contains(":*omit :"));
    }

    @Test
    public void toString_both_containsExample() {
        String str = ParameterSpacingStyle.BOTH.toString();
        assertTrue(str.contains("Before/after parameter"));
        assertTrue(str.contains(": *omit :"));
    }

    @Test
    public void toString_formatIsDisplayNameParenExample() {
        String str = ParameterSpacingStyle.NONE.toString();
        assertEquals("No space (:*omit:)", str);
    }

    // --- fromLabel ---

    @Test
    public void fromLabel_none() {
        assertEquals(ParameterSpacingStyle.NONE, ParameterSpacingStyle.fromLabel("No space"));
    }

    @Test
    public void fromLabel_before() {
        assertEquals(ParameterSpacingStyle.BEFORE, ParameterSpacingStyle.fromLabel("Before parameter"));
    }

    @Test
    public void fromLabel_after() {
        assertEquals(ParameterSpacingStyle.AFTER, ParameterSpacingStyle.fromLabel("After parameter"));
    }

    @Test
    public void fromLabel_both() {
        assertEquals(ParameterSpacingStyle.BOTH, ParameterSpacingStyle.fromLabel("Before/after parameter"));
    }

    @Test
    public void fromLabel_null_returnsDefault() {
        ParameterSpacingStyle style = ParameterSpacingStyle.fromLabel(null);
        assertNotNull(style);
        assertEquals(ParameterSpacingStyle.BEFORE, style);
    }

    @Test
    public void fromLabel_unknown_returnsDefault() {
        ParameterSpacingStyle style = ParameterSpacingStyle.fromLabel("NonExistentStyle");
        assertNotNull(style);
        assertEquals(ParameterSpacingStyle.BEFORE, style);
    }

    // --- valueOf ---

    @Test
    public void valueOf_none() {
        assertEquals(ParameterSpacingStyle.NONE, ParameterSpacingStyle.valueOf("NONE"));
    }

    @Test
    public void valueOf_both() {
        assertEquals(ParameterSpacingStyle.BOTH, ParameterSpacingStyle.valueOf("BOTH"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOf_invalid_throws() {
        ParameterSpacingStyle.valueOf("INVALID");
    }
}
