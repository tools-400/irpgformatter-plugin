package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.preferencepages.keywordeditor.KeywordEntry;
import de.tools400.lpex.irpgformatter.utils.KeywordUtils;

public class KeywordUtilsTest {

    // --- getDefaultDataTypes ---

    @Test
    public void getDefaultDataTypes_notNull() {
        assertNotNull(KeywordUtils.getDefaultDataTypes());
    }

    @Test
    public void getDefaultDataTypes_notEmpty() {
        assertFalse(KeywordUtils.getDefaultDataTypes().isEmpty());
    }

    @Test
    public void getDefaultDataTypes_containsChar() {
        Map<String, String> dataTypes = KeywordUtils.getDefaultDataTypes();
        assertTrue(dataTypes.containsKey("CHAR"));
    }

    // --- getDefaultDeclarationTypes ---

    @Test
    public void getDefaultDeclarationTypes_notNull() {
        assertNotNull(KeywordUtils.getDefaultDeclarationTypes());
    }

    @Test
    public void getDefaultDeclarationTypes_notEmpty() {
        assertFalse(KeywordUtils.getDefaultDeclarationTypes().isEmpty());
    }

    @Test
    public void getDefaultDeclarationTypes_containsDclDs() {
        Map<String, String> declTypes = KeywordUtils.getDefaultDeclarationTypes();
        assertTrue(declTypes.containsKey("DCL-DS"));
    }

    // --- getDefaultKeywords ---

    @Test
    public void getDefaultKeywords_notNull() {
        assertNotNull(KeywordUtils.getDefaultKeywords());
    }

    @Test
    public void getDefaultKeywords_notEmpty() {
        assertFalse(KeywordUtils.getDefaultKeywords().isEmpty());
    }

    // --- getDefaultSpecialWords ---

    @Test
    public void getDefaultSpecialWords_notNull() {
        assertNotNull(KeywordUtils.getDefaultSpecialWords());
    }

    @Test
    public void getDefaultSpecialWords_notEmpty() {
        assertFalse(KeywordUtils.getDefaultSpecialWords().isEmpty());
    }

    @Test
    public void getDefaultSpecialWords_containsNopass() {
        Map<String, String> specialWords = KeywordUtils.getDefaultSpecialWords();
        assertTrue(specialWords.containsKey("*NOPASS"));
    }

    // --- keywordsToString / stringToKeywords (round-trip) ---

    @Test
    public void keywordsToString_notNull() {
        Map<String, String> keywords = new LinkedHashMap<>();
        keywords.put("EXTPROC", "ExtProc");
        keywords.put("OPDESC", "OpDesc");
        String result = KeywordUtils.keywordsToString(keywords);
        assertNotNull(result);
        assertTrue(result.contains("EXTPROC=ExtProc"));
        assertTrue(result.contains("OPDESC=OpDesc"));
    }

    @Test
    public void stringToKeywords_parsesCorrectly() {
        String text = "EXTPROC=ExtProc\nOPDESC=OpDesc\n";
        Map<String, String> map = KeywordUtils.stringToKeywords(text);
        assertEquals("ExtProc", map.get("EXTPROC"));
        assertEquals("OpDesc", map.get("OPDESC"));
    }

    @Test
    public void roundTrip_keywordsToStringAndBack() {
        Map<String, String> original = new LinkedHashMap<>();
        original.put("EXTPROC", "ExtProc");
        original.put("OPDESC", "OpDesc");
        original.put("QUALIFIED", "Qualified");

        String str = KeywordUtils.keywordsToString(original);
        Map<String, String> roundTripped = KeywordUtils.stringToKeywords(str);

        assertEquals("ExtProc", roundTripped.get("EXTPROC"));
        assertEquals("OpDesc", roundTripped.get("OPDESC"));
        assertEquals("Qualified", roundTripped.get("QUALIFIED"));
    }

    @Test
    public void stringToKeywords_emptyString() {
        Map<String, String> map = KeywordUtils.stringToKeywords("");
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    // --- dataTypesToString ---

    @Test
    public void dataTypesToString_formatsCorrectly() {
        Map<String, String> dataTypes = new LinkedHashMap<>();
        dataTypes.put("CHAR", "Char");
        dataTypes.put("VARCHAR", "VarChar");
        String result = KeywordUtils.dataTypesToString(dataTypes);
        assertNotNull(result);
        assertTrue(result.contains("CHAR=Char"));
    }

    // --- declarationTypesToString ---

    @Test
    public void declarationTypesToString_formatsCorrectly() {
        Map<String, String> declTypes = new LinkedHashMap<>();
        declTypes.put("DCL-DS", "Dcl-Ds");
        String result = KeywordUtils.declarationTypesToString(declTypes);
        assertNotNull(result);
        assertTrue(result.contains("DCL-DS=Dcl-Ds"));
    }

    // --- mapToEntries ---

    @Test
    public void mapToEntries_convertsCorrectly() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("EXTPROC", "ExtProc");
        map.put("OPDESC", "OpDesc");

        List<KeywordEntry> entries = KeywordUtils.mapToEntries(map);
        assertEquals(2, entries.size());
    }

    @Test
    public void mapToEntries_sortedByKey() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("ZETA", "Zeta");
        map.put("ALPHA", "Alpha");
        map.put("MIDDLE", "Middle");

        List<KeywordEntry> entries = KeywordUtils.mapToEntries(map);
        assertEquals("ALPHA", entries.get(0).getKey());
        assertEquals("MIDDLE", entries.get(1).getKey());
        assertEquals("ZETA", entries.get(2).getKey());
    }

    @Test
    public void mapToEntries_emptyMap() {
        Map<String, String> map = new LinkedHashMap<>();
        List<KeywordEntry> entries = KeywordUtils.mapToEntries(map);
        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

    // --- entriesToMap ---

    @Test
    public void entriesToMap_convertsCorrectly() {
        List<KeywordEntry> entries = new java.util.ArrayList<>();
        entries.add(new KeywordEntry("EXTPROC", "ExtProc"));
        entries.add(new KeywordEntry("OPDESC", "OpDesc"));

        Map<String, String> map = KeywordUtils.entriesToMap(entries);
        assertEquals("ExtProc", map.get("EXTPROC"));
        assertEquals("OpDesc", map.get("OPDESC"));
    }

    @Test
    public void entriesToMap_emptyList() {
        List<KeywordEntry> entries = new java.util.ArrayList<>();
        Map<String, String> map = KeywordUtils.entriesToMap(entries);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    // --- mapToEntries / entriesToMap round-trip ---

    @Test
    public void roundTrip_mapToEntriesAndBack() {
        Map<String, String> original = new LinkedHashMap<>();
        original.put("EXTPROC", "ExtProc");
        original.put("OPDESC", "OpDesc");

        List<KeywordEntry> entries = KeywordUtils.mapToEntries(original);
        Map<String, String> roundTripped = KeywordUtils.entriesToMap(entries);

        assertEquals("ExtProc", roundTripped.get("EXTPROC"));
        assertEquals("OpDesc", roundTripped.get("OPDESC"));
    }
}
