/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import de.tools400.lpex.irpgformatter.preferences.ParameterSpacingStyle;
import de.tools400.lpex.irpgformatter.preferences.PreferencesProfileManager;
import de.tools400.lpex.irpgformatter.preferences.PreferencesProfileManager.ProfileData;

/**
 * Checks the export and import of the preference profiles as XML files.
 */
public class PreferencesProfileManagerTest extends AbstractTestCase {

    /**
     * IDs of the settings, that a complete profile contains. The custom preview
     * source is only exported, if there is one.
     */
    private static final List<String> REQUIRED_SETTING_IDS = Arrays.asList("executeIbmFormatter", "useConstKeyword", "delimiterBeforeParameter",
        "parameterSpacingStyle", "alignSubFields", "breakBetweenCaseChange", "breakBeforeKeyword", "sortConstValueToEnd", "replacePiName",
        "removeEndProcName", "unindentCompilerDirectives", "maxNameLength", "minNameLength", "executeIrpgFormatter", "formatOnSave",
        "removeEmptyCommentLines", "removeEmptyLinesBeforeDclPi", "dataTypes", "declarationTypes", "keywords", "specialWords");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void exportProfile_writesProfileHeader() throws Exception {

        Element profiles = parse(export(createData())).getDocumentElement();
        Element profile = (Element)profiles.getElementsByTagName("profile").item(0);

        assertEquals("profiles", profiles.getTagName());
        assertEquals("1", profiles.getAttribute("version"));
        assertEquals("iRPGFormatterProfile", profile.getAttribute("kind"));
        assertEquals("iRPGFormatter", profile.getAttribute("name"));
        assertEquals("1", profile.getAttribute("version"));
    }

    @Test
    public void exportProfile_writesSettingsInOrder() throws Exception {

        List<String> expected = new ArrayList<>(REQUIRED_SETTING_IDS);
        expected.add("customPreviewSource");

        assertEquals(expected, getSettingIds(export(createData())));
    }

    @Test
    public void exportProfile_writesValuesAsText() throws Exception {

        Map<String, Element> settings = getSettings(export(createData()));

        assertEquals("true", settings.get("useConstKeyword").getAttribute("value"));
        assertEquals("false", settings.get("alignSubFields").getAttribute("value"));
        assertEquals("AFTER", settings.get("parameterSpacingStyle").getAttribute("value"));
        assertEquals("33", settings.get("maxNameLength").getAttribute("value"));

        Element entry = (Element)settings.get("dataTypes").getElementsByTagName("entry").item(0);
        assertEquals("CHAR", entry.getAttribute("key"));
        assertEquals("Char", entry.getAttribute("value"));
    }

    @Test
    public void exportProfile_omitsEmptyCustomPreviewSource() throws Exception {

        ProfileData data = createData();
        data.setCustomPreviewSource(null);
        assertFalse(getSettingIds(export(data)).contains("customPreviewSource"));

        data.setCustomPreviewSource("");
        assertFalse(getSettingIds(export(data)).contains("customPreviewSource"));
    }

    @Test
    public void exportAndImport_restoreAllSettings() throws Exception {

        ProfileData data = createData();

        assertEquals(dump(data), dump(importProfile(export(data))));
    }

    @Test
    public void exportAndImport_normalizeCustomPreviewSource() throws Exception {

        ProfileData data = createData();
        data.setCustomPreviewSource("  \r\n**free\r\ndcl-s x char(10);\rreturn;\n  ");

        assertEquals("**free\ndcl-s x char(10);\nreturn;", importProfile(export(data)).getCustomPreviewSource());
    }

    @Test
    public void exportAndImport_turnMissingMapIntoEmptyMap() throws Exception {

        ProfileData data = createData();
        data.setDataTypes(null);
        data.setSpecialWords(null);

        ProfileData imported = importProfile(export(data));

        assertTrue(imported.getDataTypes().isEmpty());
        assertTrue(imported.getSpecialWords().isEmpty());
    }

    @Test
    public void importProfile_missingSettingsKeepCurrentValues() throws Exception {

        ProfileData data = createData();
        importProfile(writeProfile("<setting id=\"useConstKeyword\" value=\"false\"/>"), data);

        ProfileData expected = createData();
        expected.setUseConstKeyword(false);
        assertEquals(dump(expected), dump(data));
    }

    @Test
    public void importProfile_completeProfileReturnsTrue() throws Exception {

        assertTrue(importProfile(export(createData()), new ProfileData()));
        assertTrue(importProfile(writeProfileWithout(null), new ProfileData()));
    }

    @Test
    public void importProfile_profileWithoutCustomPreviewSourceReturnsTrue() throws Exception {

        ProfileData data = createData();
        data.setCustomPreviewSource(null);

        assertTrue(importProfile(export(data), new ProfileData()));
    }

    @Test
    public void importProfile_missingSettingReturnsFalse() throws Exception {

        assertFalse(importProfile(writeProfileWithout("minNameLength"), new ProfileData()));
        assertFalse(importProfile(writeProfileWithout("keywords"), new ProfileData()));
    }

    @Test
    public void importProfile_unknownOrNestedSettingDoesNotReplaceMissingOne() throws Exception {

        assertFalse(importProfile(writeProfileWithout("minNameLength", "<setting id=\"unknownSetting\" value=\"1\"/>"), new ProfileData()));
        assertFalse(
            importProfile(writeProfileWithout("minNameLength", "<setting id=\"dataTypes\"><setting id=\"minNameLength\" value=\"1\"/></setting>"),
                new ProfileData()));
    }

    @Test
    public void importProfile_ignoresUnknownAndNestedSettings() throws Exception {

        ProfileData imported = importProfile(writeProfile( //
            "<setting id=\"unknownSetting\" value=\"true\"/>", //
            "<setting id=\"dataTypes\"><entry key=\"CHAR\" value=\"Char\"/><setting id=\"useConstKeyword\" value=\"true\"/></setting>"));

        assertFalse(imported.isUseConstKeyword());
        assertEquals("{CHAR=Char}", imported.getDataTypes().toString());
    }

    @Test
    public void importProfile_lastOfDuplicateSettingsWins() throws Exception {

        ProfileData imported = importProfile(writeProfile( //
            "<setting id=\"maxNameLength\" value=\"30\"/>", //
            "<setting id=\"maxNameLength\" value=\"40\"/>"));

        assertEquals(40, imported.getMaxNameLength());
    }

    @Test
    public void importProfile_readsBooleanOtherThanTrueAsFalse() throws Exception {

        ProfileData data = new ProfileData();
        data.setAlignSubFields(true);

        importProfile(writeProfile( //
            "<setting id=\"alignSubFields\" value=\"yes\"/>", //
            "<setting id=\"formatOnSave\" value=\"TRUE\"/>"), data);

        assertFalse(data.isAlignSubFields());
        assertTrue(data.isFormatOnSave());
    }

    @Test
    public void importProfile_keepsOrderOfMapEntries() throws Exception {

        ProfileData imported = importProfile(writeProfile( //
            "<setting id=\"keywords\"><entry key=\"Z\" value=\"z\"/><entry key=\"A\" value=\"a\"/><entry key=\"M\" value=\"m\"/></setting>"));

        assertEquals(Arrays.asList("Z", "A", "M"), new ArrayList<>(imported.getKeywords().keySet()));
    }

    @Test
    public void importProfile_withoutProfileOfKindFails() throws Exception {

        File file = writeFile("<profiles version=\"1\"><profile kind=\"other\" name=\"x\" version=\"1\"/></profiles>");

        try {
            importProfile(file);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals("No iRPGFormatterProfile found in file.", e.getMessage());
        }
    }

    @Test(expected = NumberFormatException.class)
    public void importProfile_invalidNumberFails() throws Exception {

        importProfile(writeProfile("<setting id=\"maxNameLength\" value=\"abc\"/>"));
    }

    @Test
    public void importProfile_doesNotResolveExternalEntities() throws Exception {

        File secret = folder.newFile("secret.txt");
        Files.write(secret.toPath(), "SECRET".getBytes(StandardCharsets.UTF_8));
        File file = writeFile("<!DOCTYPE profiles [ <!ENTITY ext SYSTEM \"" + secret.toURI() + "\"> ]>"
            + "<profiles version=\"1\"><profile kind=\"iRPGFormatterProfile\" name=\"iRPGFormatter\" version=\"1\">"
            + "<setting id=\"customPreviewSource\">&ext;</setting></profile></profiles>");

        ProfileData data = new ProfileData();
        try {
            importProfile(file, data);
            fail("SAXParseException expected");
        } catch (SAXParseException e) {
            // A profile must not have a DOCTYPE
        }
        assertNull(data.getCustomPreviewSource());
    }

    @Test
    public void importProfile_reportsInvalidFileOnlyByException() throws Exception {

        File file = writeFile("<profiles version=\"1\"><profile kind=\"iRPGFormatterProfile\">");

        PrintStream originalErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err, true));
        try {
            importProfile(file, new ProfileData());
            fail("SAXParseException expected");
        } catch (SAXParseException e) {
            // The file is not complete
        } finally {
            System.setErr(originalErr);
        }
        assertEquals("", err.toString());
    }

    /**
     * Returns profile data with values that differ from the defaults.
     */
    private ProfileData createData() {

        ProfileData data = new ProfileData();
        data.setParameterSpacingStyle(ParameterSpacingStyle.AFTER.name());
        data.setUseConstKeyword(true);
        data.setDelimiterBeforeParameter(true);
        data.setAlignSubFields(false);
        data.setBreakBetweenCaseChange(true);
        data.setBreakBeforeKeyword(true);
        data.setSortConstValueToEnd(true);
        data.setReplacePiName(false);
        data.setRemoveEndProcName(false);
        data.setUnindentCompilerDirectives(false);
        data.setMaxNameLength(33);
        data.setMinNameLength(7);
        data.setExecuteIbmFormatter(false);
        data.setExecuteIrpgFormatter(false);
        data.setFormatOnSave(true);
        data.setRemoveEmptyCommentLines(true);
        data.setRemoveEmptyLinesBeforeDclPi(true);
        data.setDataTypes(createMap("CHAR", "Char", "VARCHAR", "VarChar"));
        data.setDeclarationTypes(createMap("DCL-S", "Dcl-S"));
        data.setKeywords(createMap("LIKEDS", "LikeDS", "A&B<C\"", "Ä'ß"));
        data.setSpecialWords(createMap("*OMIT", "*Omit"));
        data.setCustomPreviewSource("**free\ndcl-s x char(10);");
        return data;
    }

    private Map<String, String> createMap(String... keysAndValues) {

        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    private String dump(ProfileData data) {

        return data.getParameterSpacingStyle() + "|" + data.isUseConstKeyword() + "|" + data.isDelimiterBeforeParameter() + "|"
            + data.isAlignSubFields() + "|" + data.isBreakBetweenCaseChange() + "|" + data.isBreakBeforeKeyword() + "|" + data.isSortConstValueToEnd()
            + "|" + data.isReplacePiName() + "|" + data.isRemoveEndProcName() + "|" + data.isUnindentCompilerDirectives() + "|"
            + data.getMaxNameLength() + "|" + data.getMinNameLength() + "|" + data.isExecuteIbmFormatter() + "|" + data.isExecuteIrpgFormatter() + "|"
            + data.isFormatOnSave() + "|" + data.isRemoveEmptyCommentLines() + "|" + data.isRemoveEmptyLinesBeforeDclPi() + "|" + data.getDataTypes()
            + "|" + data.getDeclarationTypes() + "|" + data.getKeywords() + "|" + data.getSpecialWords() + "|" + data.getCustomPreviewSource();
    }

    private File export(ProfileData data) throws Exception {

        File file = new File(folder.getRoot(), "export.xml");
        PreferencesProfileManager.exportProfile(file.getPath(), data);
        return file;
    }

    private ProfileData importProfile(File file) throws Exception {

        ProfileData data = new ProfileData();
        importProfile(file, data);
        return data;
    }

    private boolean importProfile(File file, ProfileData data) throws Exception {
        return PreferencesProfileManager.importProfile(file.getPath(), data);
    }

    /**
     * Writes a profile with all required settings except the given one and with
     * the given additional settings.
     */
    private File writeProfileWithout(String missingId, String... additionalSettings) throws Exception {

        List<String> settings = new ArrayList<>();
        for (String id : REQUIRED_SETTING_IDS) {
            if (!id.equals(missingId)) {
                settings.add("<setting id=\"" + id + "\" value=\"1\"/>");
            }
        }
        settings.addAll(Arrays.asList(additionalSettings));

        return writeProfile(settings.toArray(new String[settings.size()]));
    }

    private File writeProfile(String... settings) throws Exception {

        StringBuilder xml = new StringBuilder();
        xml.append("<profiles version=\"1\"><profile kind=\"iRPGFormatterProfile\" name=\"iRPGFormatter\" version=\"1\">");
        for (String setting : settings) {
            xml.append(setting);
        }
        xml.append("</profile></profiles>");

        return writeFile(xml.toString());
    }

    private File writeFile(String xml) throws Exception {

        File file = new File(folder.getRoot(), "import.xml");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write(xml);
        }
        return file;
    }

    private Document parse(File file) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
    }

    private List<String> getSettingIds(File file) throws Exception {
        return new ArrayList<>(getSettings(file).keySet());
    }

    /**
     * Returns the settings of the profile, which are the direct children of the
     * profile element, by their IDs in the order of the file.
     */
    private Map<String, Element> getSettings(File file) throws Exception {

        Element profile = (Element)parse(file).getElementsByTagName("profile").item(0);

        Map<String, Element> settings = new LinkedHashMap<>();
        NodeList children = profile.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element && "setting".equals(child.getNodeName())) {
                settings.put(((Element)child).getAttribute("id"), (Element)child);
            }
        }
        return settings;
    }
}
