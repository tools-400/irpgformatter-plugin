package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.preferences.ParameterSpacingStyle;
import de.tools400.lpex.irpgformatter.preferences.PreferenceStoreProvider;
import de.tools400.lpex.irpgformatter.preferences.Preferences;

public class PreferencesTest extends AbstractTestCase {

    // --- Singleton ---

    @Test
    public void getInstance_returnsSameInstance() {
        Preferences a = Preferences.getInstance();
        Preferences b = Preferences.getInstance();
        assertSame(a, b);
    }

    @Test
    public void resetInstance_createsFreshInstance() {
        Preferences before = Preferences.getInstance();
        Preferences.resetInstance();
        Preferences after = Preferences.getInstance();
        assertNotSame(before, after);
    }

    // --- Default values ---

    @Test
    public void getDefaultKeywordCasingStyle_isLowercase() {
        assertEquals(KeywordCasingStyle.LOWERCASE, Preferences.getInstance().getDefaultKeywordCasingStyle());
    }

    @Test
    public void getDefaultParameterSpacingStyle_isBefore() {
        assertEquals(ParameterSpacingStyle.BEFORE, Preferences.getInstance().getDefaultParameterSpacingStyle());
    }

    @Test
    public void getDefaultUseConstKeyword_isFalse() {
        assertFalse(Preferences.getInstance().getDefaultUseConstKeyword());
    }

    @Test
    public void getDefaultDelimiterBeforeParameter_isFalse() {
        assertFalse(Preferences.getInstance().getDefaultDelimiterBeforeParameter());
    }

    @Test
    public void getDefaultAlignSubFields_isTrue() {
        assertTrue(Preferences.getInstance().getDefaultAlignSubFields());
    }

    @Test
    public void getDefaultBreakBetweenCaseChange_isFalse() {
        assertFalse(Preferences.getInstance().getDefaultBreakBetweenCaseChange());
    }

    @Test
    public void getDefaultBreakBeforeKeyword_isFalse() {
        assertFalse(Preferences.getInstance().getDefaultBreakBeforeKeyword());
    }

    @Test
    public void getDefaultSortConstValueToEnd_isFalse() {
        assertFalse(Preferences.getInstance().getDefaultSortConstValueToEnd());
    }

    @Test
    public void getDefaultMaxNameLength_is60() {
        assertEquals(60, Preferences.getInstance().getDefaultMaxNameLength());
    }

    @Test
    public void getDefaultMinLiteralLength_is20() {
        assertEquals(20, Preferences.getInstance().getDefaultMinLiteralLength());
    }

    @Test
    public void getDefaultExecuteIbmFormatter_isTrue() {
        assertTrue(Preferences.getInstance().getDefaultExecuteIbmFormatter());
    }

    @Test
    public void getDefaultExecuteIrpgFormatter_isTrue() {
        assertTrue(Preferences.getInstance().getDefaultExecuteIrpgFormatter());
    }

    @Test
    public void getDefaultFormatOnSave_isFalse() {
        assertFalse(Preferences.getInstance().getDefaultFormatOnSave());
    }

    @Test
    public void getDefaultFormatterPreviewVerticalRulerColumn_is45() {
        assertEquals(45, Preferences.getInstance().getDefaultFormatterPreviewVerticalRulerColumn());
    }

    // --- Getters / Setters ---

    @Test
    public void setAndGetParameterSpacingStyle() {
        Preferences prefs = Preferences.getInstance();
        prefs.setParameterSpacingStyle(ParameterSpacingStyle.BOTH);
        assertEquals(ParameterSpacingStyle.BOTH, prefs.getParameterSpacingStyle());
    }

    @Test
    public void setAndGetUseConstKeyword() {
        Preferences prefs = Preferences.getInstance();
        prefs.setUseConstKeyword(true);
        assertTrue(prefs.isUseConstKeyword());
    }

    @Test
    public void setAndGetDelimiterBeforeParameter() {
        Preferences prefs = Preferences.getInstance();
        prefs.setDelimiterBeforeParameter(true);
        assertTrue(prefs.isDelimiterBeforeParameter());
    }

    @Test
    public void setAndGetAlignSubFields() {
        Preferences prefs = Preferences.getInstance();
        prefs.setAlignSubFields(false);
        assertFalse(prefs.isAlignSubFields());
    }

    @Test
    public void setAndGetBreakBetweenCaseChange() {
        Preferences prefs = Preferences.getInstance();
        prefs.setBreakBetweenCaseChange(true);
        assertTrue(prefs.isBreakBetweenCaseChange());
    }

    @Test
    public void setAndGetBreakBeforeKeyword() {
        Preferences prefs = Preferences.getInstance();
        prefs.setBreakBeforeKeyword(true);
        assertTrue(prefs.isBreakBeforeKeyword());
    }

    @Test
    public void setAndGetSortConstValueToEnd() {
        Preferences prefs = Preferences.getInstance();
        prefs.setSortConstValueToEnd(true);
        assertTrue(prefs.isSortConstValueToEnd());
    }

    @Test
    public void setAndGetMaxNameLength() {
        Preferences prefs = Preferences.getInstance();
        prefs.setMaxNameLength(80);
        assertEquals(80, prefs.getMaxNameLength());
    }

    @Test
    public void setAndGetMinLiteralLength() {
        Preferences prefs = Preferences.getInstance();
        prefs.setMinLiteralLength(30);
        assertEquals(30, prefs.getMinNameLength());
    }

    @Test
    public void setAndGetExecuteIbmFormatter() {
        Preferences prefs = Preferences.getInstance();
        prefs.setExecuteIbmFormatter(false);
        assertFalse(prefs.isExecuteIbmFormatter());
    }

    @Test
    public void setAndGetExecuteIrpgFormatter() {
        Preferences prefs = Preferences.getInstance();
        prefs.setExecuteIrpgFormatter(false);
        assertFalse(prefs.isExecuteIrpgFormatter());
    }

    @Test
    public void setAndGetFormatOnSave() {
        Preferences prefs = Preferences.getInstance();
        prefs.setFormatOnSave(true);
        assertTrue(prefs.isFormatOnSave());
    }

    @Test
    public void setAndGetFormatterPreviewVerticalRulerColumn() {
        Preferences prefs = Preferences.getInstance();
        prefs.setFormatterPreviewVerticalRulerColumn(80);
        assertEquals(80, prefs.getFormatterPreviewVerticalRulerColumn());
    }

    @Test
    public void setAndGetCustomPreviewContent() {
        Preferences prefs = Preferences.getInstance();
        prefs.setCustomPreviewContent(true);
        assertTrue(prefs.isCustomPreviewContent());
    }

    @Test
    public void setAndGetCustomFormatterSource() {
        Preferences prefs = Preferences.getInstance();
        prefs.setCustomFormatterSource("custom source");
        assertEquals("custom source", prefs.getCustomFormatterSource());
    }

    // --- Keyword maps ---

    @Test
    public void getDataTypes_notEmpty() {
        Map<String, String> dataTypes = Preferences.getInstance().getDataTypes();
        assertNotNull(dataTypes);
        assertFalse(dataTypes.isEmpty());
    }

    @Test
    public void getDeclarationTypes_notEmpty() {
        Map<String, String> declTypes = Preferences.getInstance().getDeclarationTypes();
        assertNotNull(declTypes);
        assertFalse(declTypes.isEmpty());
    }

    @Test
    public void getKeywords_notEmpty() {
        Map<String, String> keywords = Preferences.getInstance().getKeywords();
        assertNotNull(keywords);
        assertFalse(keywords.isEmpty());
    }

    @Test
    public void getSpecialWords_notEmpty() {
        Map<String, String> specialWords = Preferences.getInstance().getSpecialWords();
        assertNotNull(specialWords);
        assertFalse(specialWords.isEmpty());
    }

    // --- Parameter spacing styles array ---

    @Test
    public void getParameterSpacingStyles_hasFourEntries() {
        String[] styles = Preferences.getInstance().getParameterSpacingStyles();
        assertEquals(4, styles.length);
    }

    // --- IBM preferences ---

    @Test
    public void getIndent_defaultIs2() {
        assertEquals(2, Preferences.getInstance().getIndent());
    }

    @Test
    public void getStartColumn_defaultIs1() {
        assertEquals(1, Preferences.getInstance().getStartColumn());
    }

    @Test
    public void getStartColumnAsText_defaultIs1() {
        assertEquals("1", Preferences.getInstance().getStartColumnAsText());
    }

    // --- PreferenceStoreProvider ---

    @Test
    public void isTestMode_trueAfterEnable() {
        assertTrue(PreferenceStoreProvider.isTestMode());
    }

    @Test
    public void disableTestMode_setsToFalse() {
        PreferenceStoreProvider.disableTestMode();
        assertFalse(PreferenceStoreProvider.isTestMode());
        // Re-enable for other tests
        PreferenceStoreProvider.enableTestMode();
        Preferences.resetInstance();
        PreferenceStoreProvider.initializeTestDefaults();
        Preferences.getInstance().initializeDefaultPreferences();
    }

    // --- Default keyword maps ---

    @Test
    public void getDefaultDataTypes_notEmpty() {
        Map<String, String> dataTypes = Preferences.getInstance().getDefaultDataTypes();
        assertNotNull(dataTypes);
        assertFalse(dataTypes.isEmpty());
    }

    @Test
    public void getDefaultDeclarationTypes_notEmpty() {
        Map<String, String> declTypes = Preferences.getInstance().getDefaultDeclarationTypes();
        assertNotNull(declTypes);
        assertFalse(declTypes.isEmpty());
    }

    @Test
    public void getDefaultKeywords_notEmpty() {
        Map<String, String> keywords = Preferences.getInstance().getDefaultKeywords();
        assertNotNull(keywords);
        assertFalse(keywords.isEmpty());
    }

    @Test
    public void getDefaultSpecialWords_notEmpty() {
        Map<String, String> specialWords = Preferences.getInstance().getDefaultSpecialWords();
        assertNotNull(specialWords);
        assertFalse(specialWords.isEmpty());
    }
}
