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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;
import de.tools400.lpex.irpgformatter.preferences.CalculationFormatter;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.preferences.PreferenceStoreProvider;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.preferences.SqlBreakStyle;
import de.tools400.lpex.irpgformatter.preferences.SqlCasingStyle;

/**
 * Unit tests for the project specific settings: the preferences of a project
 * are independent of the preferences of the workspace, and the formatter uses
 * the preferences, it is created with.
 */
public class ProjectPreferencesTest extends AbstractTestCase {

    /*
     * The IBM settings by their keys in the preferences of IBM.
     */
    private static final String START_COLUMN = "RPGLE.FORMATTING.start";
    private static final String END_COLUMN = "RPGLE.FORMATTING.end";
    private static final String INDENT = "RPGLE.FORMATTING.length";
    private static final String SELECT_INDENT = "RPGLE.FORMATTING.select";
    private static final String PRESERVE_RELATIVE_INDENT = "RPGLE.FORMATTING.multiple";
    private static final String SQL_FORMATTING = "SQLRPGLE.FORMATTING.base";
    private static final String SQL_INDENT = "SQLRPGLE.INDENT.S1_Blanks";
    private static final String SQL_BREAK_ON_COMMA = "SQLRPGLE.FORMATTING.onComma";
    private static final String SQL_BREAK_ON_AND_OR = "SQLRPGLE.FORMATTING.onAndOr";
    private static final String SQL_UPPERCASING = "SQLRPGLE.UPPERCASING.base";
    private static final String SQL_CASING_STYLE = "SQLRPGLE.UPPERCASING.base.value";
    private static final String SQL_KEYWORD_CASING = "SQLRPGLE.UPPERCASING.keywords";
    private static final String SQL_IDENTIFIER_CASING = "SQLRPGLE.UPPERCASING.identifiers";
    private static final String KEYWORD_CASING_STYLE = "com.ibm.etools.iseries.edit.preferences.parser.ilerpg.enter.autoclosecontrol.autoclosecontrolvalue";

    /**
     * The setters of the settings, that only the workspace has: a project uses
     * the preview of the workspace. Every other setter must write a setting of
     * <code>PROJECT_SETTINGS</code> of {@link Preferences}, see
     * {@link #removeProjectSpecificSettings_removesEverySetting()}. The setters
     * of the word lists refuse a project themselves.
     */
    private static final Set<String> SETTERS_OF_WORKSPACE = new HashSet<>(
        Arrays.asList("setDefaultFormatterSource", "setCustomFormatterSource", "setCustomPreviewContent", "setFormatterPreviewVerticalRulerColumn"));

    private Preferences workspace;
    private Preferences project;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        workspace = Preferences.getInstance();
        project = PreferenceStoreProvider.createTestProjectPreferences();
    }

    // --- Switch ---

    @Test
    public void project_withoutSettings_isNotProjectSpecific() {
        assertFalse(project.isProjectSpecificSettings());
    }

    @Test
    public void workspace_isNotProjectSpecific() {
        assertFalse(workspace.isProjectSpecificSettings());
    }

    @Test
    public void setProjectSpecificSettings_makesProjectSpecific() {

        project.setProjectSpecificSettings();

        assertTrue(project.isProjectSpecificSettings());
        assertFalse(workspace.isProjectSpecificSettings());
    }

    @Test
    public void removeProjectSpecificSettings_restoresDefaults() {

        project.setProjectSpecificSettings();
        project.setUseConstKeyword(true);
        project.setMaxNameLength(80);
        project.setCalculationFormatter(CalculationFormatter.OFF);

        project.removeProjectSpecificSettings();

        assertFalse(project.isProjectSpecificSettings());
        assertEquals(project.getDefaultUseConstKeyword(), project.isUseConstKeyword());
        assertEquals(project.getDefaultMaxNameLength(), project.getMaxNameLength());
        assertEquals(project.getDefaultCalculationFormatter(), project.getCalculationFormatter());
    }

    /**
     * Calls every setter of the preferences of a project: after
     * <code>removeProjectSpecificSettings()</code> every getter returns the
     * value of a project without settings again. A setting, that is missing in
     * <code>PROJECT_SETTINGS</code> of {@link Preferences}, would stay in the
     * file of the project.
     */
    @Test
    public void removeProjectSpecificSettings_removesEverySetting() throws Exception {

        Map<String, Object> withoutSettings = getValues(PreferenceStoreProvider.createTestProjectPreferences());

        int count = 0;
        for (Method setter : Preferences.class.getMethods()) {
            if (!isSetter(setter) || SETTERS_OF_WORKSPACE.contains(setter.getName())) {
                continue;
            }

            Preferences preferences = PreferenceStoreProvider.createTestProjectPreferences();
            preferences.setProjectSpecificSettings();
            if (!setOtherValue(preferences, setter)) {
                // A word list: a project has none of its own.
                continue;
            }

            preferences.removeProjectSpecificSettings();

            assertEquals(setter.getName(), Collections.emptyMap(), getDifferences(withoutSettings, getValues(preferences)));
            count++;
        }

        assertTrue("Only " + count + " setters tested", count >= 20);
    }

    // --- Independent values ---

    @Test
    public void project_hasDefaultValues() {

        assertEquals(workspace.getDefaultMaxNameLength(), project.getMaxNameLength());
        assertEquals(workspace.getDefaultCalculationFormatter(), project.getCalculationFormatter());
        assertEquals(workspace.getDefaultKeywords(), project.getKeywords());
    }

    @Test
    public void projectValue_doesNotChangeWorkspace() {

        project.setUseConstKeyword(true);
        project.setMaxNameLength(80);

        assertTrue(project.isUseConstKeyword());
        assertEquals(80, project.getMaxNameLength());
        assertFalse(workspace.isUseConstKeyword());
        assertEquals(workspace.getDefaultMaxNameLength(), workspace.getMaxNameLength());
    }

    @Test
    public void workspaceValue_doesNotChangeProject() {

        workspace.setRemoveEndProcName(false);

        assertFalse(workspace.isRemoveEndProcName());
        assertTrue(project.isRemoveEndProcName());
    }

    // --- Word lists ---

    @Test
    public void isProject() {
        assertTrue(project.isProject());
        assertFalse(workspace.isProject());
    }

    /**
     * A project has no word lists of its own: it returns the lists of the
     * workspace, also after they were changed and cached.
     */
    @Test
    public void project_returnsWordListsOfWorkspace() {

        project.getKeywords();
        project.getDataTypes();

        workspace.setDataTypes(singleKeyword("CHAR", "CHAR"));
        workspace.setDeclarationTypes(singleKeyword("DCL-S", "Dcl-S"));
        workspace.setKeywords(singleKeyword("INZ", "InZ"));
        workspace.setSpecialWords(singleKeyword("*NOPASS", "*NoPass"));
        workspace.setOperationCodes(singleKeyword("SETLL", "SetLL"));
        workspace.setBuiltInFunctions(singleKeyword("%SUBDT", "%SubDt"));

        assertEquals(singleKeyword("CHAR", "CHAR"), project.getDataTypes());
        assertEquals(singleKeyword("DCL-S", "Dcl-S"), project.getDeclarationTypes());
        assertEquals(singleKeyword("INZ", "InZ"), project.getKeywords());
        assertEquals(singleKeyword("*NOPASS", "*NoPass"), project.getSpecialWords());
        assertEquals(singleKeyword("SETLL", "SetLL"), project.getOperationCodes());
        assertEquals(singleKeyword("%SUBDT", "%SubDt"), project.getBuiltInFunctions());
    }

    @Test(expected = IllegalStateException.class)
    public void setKeywords_ofProject_isRejected() {
        project.setKeywords(singleKeyword("INZ", "InZ"));
    }

    @Test(expected = IllegalStateException.class)
    public void setBuiltInFunctions_ofProject_isRejected() {
        project.setBuiltInFunctions(singleKeyword("%SUBDT", "%SubDt"));
    }

    // --- Formatter ---

    @Test
    public void formatterConfig_ofProject_hasValuesOfProject() {

        project.setUseConstKeyword(true);
        project.setRemoveEndProcName(false);

        FormatterConfig projectConfig = FormatterConfig.fromPreferences(project);
        FormatterConfig workspaceConfig = FormatterConfig.fromPreferences(workspace);

        assertTrue(projectConfig.isUseConstKeyword());
        assertFalse(projectConfig.isRemoveEndProcName());
        assertFalse(workspaceConfig.isUseConstKeyword());
        assertTrue(workspaceConfig.isRemoveEndProcName());
    }

    @Test
    public void formatterConfig_ofProject_hasWordListsOfWorkspace() {

        workspace.setKeywords(singleKeyword("INZ", "InZ"));

        FormatterConfig projectConfig = FormatterConfig.fromPreferences(project);

        assertEquals(singleKeyword("INZ", "InZ"), projectConfig.getKeywords());
    }

    /**
     * A project without IBM settings of its own uses the IBM settings of RDi.
     */
    @Test
    public void formatterConfig_ofProjectWithoutIbmSettings_hasIbmSettingsOfRdi() {

        FormatterConfig projectConfig = FormatterConfig.fromPreferences(project);
        FormatterConfig workspaceConfig = FormatterConfig.fromPreferences(workspace);

        assertEquals(workspaceConfig.getIndent(), projectConfig.getIndent());
        assertEquals(workspaceConfig.getMaxLineWidth(), projectConfig.getMaxLineWidth());
        assertEquals(workspaceConfig.getKeywordCasingStyle(), projectConfig.getKeywordCasingStyle());
    }

    @Test
    public void formatterConfig_ofProjectWithIbmSettings_hasIbmSettingsOfProject() {

        project.setIbmSettingValues(createIbmSettingsOfProject());

        FormatterConfig config = FormatterConfig.fromPreferences(project);

        assertEquals(8, config.getStartColumn());
        assertEquals(100, config.getMaxLineWidth());
        assertEquals(4, config.getIndent());
        assertEquals(3, config.getSelectIndent());
        assertFalse(config.isPreserveRelativeIndentOfContinuedLines());
        assertTrue(config.isFormatEmbeddedSql());
        assertEquals(5, config.getSqlIndent());
        assertEquals(SqlBreakStyle.BEFORE, config.getSqlBreakOnComma());
        assertEquals(SqlBreakStyle.NONE, config.getSqlBreakOnAndOr());
        assertEquals(SqlCasingStyle.INITIAL, config.getSqlKeywordCasingStyle());
        assertEquals(SqlCasingStyle.LOWER, config.getSqlIdentifierCasingStyle());
        assertEquals(KeywordCasingStyle.UPPER_CAMEL, config.getKeywordCasingStyle());
    }

    @Test
    public void formatterConfig_hasStartColumn() {

        PreferenceStoreProvider.setIbmTestValue(START_COLUMN, "8");

        assertEquals(8, FormatterConfig.fromPreferences(workspace).getStartColumn());
    }

    /**
     * The preview of the preference page shows the IBM settings of the page,
     * e.g. those of a project, that are not saved yet.
     */
    @Test
    public void formatterConfig_setIbmSettings_replacesOnlyIbmSettings() {

        workspace.setUseConstKeyword(true);
        FormatterConfig config = FormatterConfig.fromPreferences(workspace);

        config.setIbmSettings(Preferences.forIbmSettingValues(createIbmSettingsOfProject()));

        assertEquals(8, config.getStartColumn());
        assertEquals(100, config.getMaxLineWidth());
        assertEquals(4, config.getIndent());
        assertEquals(SqlCasingStyle.INITIAL, config.getSqlKeywordCasingStyle());
        assertEquals(KeywordCasingStyle.UPPER_CAMEL, config.getKeywordCasingStyle());
        assertTrue(config.isUseConstKeyword());
    }

    @Test
    public void format_withProjectPreferences_usesProjectSettings() throws RpgleFormatterException {

        project.setRemoveEndProcName(false);

        String[] source = { "dcl-proc Foo;", "end-proc Foo;" };

        String[] projectResult = format(project, source);
        String[] workspaceResult = format(workspace, source);

        assertEquals("end-proc Foo;", projectResult[1].trim());
        assertEquals("end-proc;", workspaceResult[1].trim());
    }

    /**
     * The indentation, the start column and the keyword casing style of a
     * project apply, when its sources are formatted.
     */
    @Test
    public void format_withIbmSettingsOfProject_usesIbmSettingsOfProject() throws RpgleFormatterException {

        project.setIbmSettingValues(settings(INDENT, "4", START_COLUMN, "3", KEYWORD_CASING_STYLE, "ENDXX"));

        String[] source = { "dcl-proc Foo;", "dcl-s x int(10);", "end-proc;" };

        String[] projectResult = formatAtStartColumn(project, source);
        String[] workspaceResult = formatAtStartColumn(workspace, source);

        assertEquals("  DCL-PROC Foo;", projectResult[0]);
        assertEquals("      DCL-S x INT(10);", projectResult[1]);
        assertEquals("dcl-proc Foo;", workspaceResult[0]);
        assertEquals("  dcl-s x int(10);", workspaceResult[1]);
    }

    // --- IBM settings ---

    @Test
    public void getIbmSettingNames_returnsTheIbmSettingsInTheOrderOfThePage() {

        assertEquals(
            Arrays.asList(START_COLUMN, END_COLUMN, INDENT, SELECT_INDENT, PRESERVE_RELATIVE_INDENT, SQL_FORMATTING, SQL_INDENT, SQL_BREAK_ON_COMMA,
                SQL_BREAK_ON_AND_OR, SQL_UPPERCASING, SQL_CASING_STYLE, SQL_KEYWORD_CASING, SQL_IDENTIFIER_CASING, KEYWORD_CASING_STYLE),
            Preferences.getIbmSettingNames());
    }

    @Test
    public void getIbmSettingValues_returnsAllIbmSettingsAsText() {

        Map<String, String> values = workspace.getIbmSettingValues();

        assertEquals(Preferences.getIbmSettingNames(), new ArrayList<>(values.keySet()));
        assertEquals("1", values.get(START_COLUMN));
        assertEquals("*MAX", values.get(END_COLUMN));
        assertEquals("2", values.get(INDENT));
        assertEquals("true", values.get(PRESERVE_RELATIVE_INDENT));
        assertEquals("endxx", values.get(KEYWORD_CASING_STYLE));
    }

    @Test
    public void workspace_hasNoIbmSettingsOfProject() {
        assertFalse(workspace.hasIbmSettings());
    }

    /**
     * A project without IBM settings of its own has the IBM settings of RDi,
     * also after they were changed.
     */
    @Test
    public void project_withoutIbmSettings_hasIbmSettingsOfRdi() {

        PreferenceStoreProvider.setIbmTestValue(INDENT, "4");
        PreferenceStoreProvider.setIbmTestValue(END_COLUMN, "100");

        assertFalse(project.hasIbmSettings());
        assertEquals(4, project.getIndent());
        assertEquals(100, project.getEndColumn());
        assertEquals(workspace.getIbmSettingValues(), project.getIbmSettingValues());
    }

    @Test
    public void project_withIbmSettings_hasIbmSettingsOfProject() {

        project.setIbmSettingValues(createIbmSettingsOfProject());

        assertTrue(project.hasIbmSettings());
        assertEquals(createIbmSettingsOfProject(), project.getIbmSettingValues());
        assertEquals(8, project.getStartColumn());
        assertEquals("8", project.getStartColumnAsText());
        assertEquals(100, project.getEndColumn());
        assertEquals("100", project.getEndColumnAsText());
        assertEquals(4, project.getIndent());
        assertEquals(3, project.getSelectIndent());
        assertFalse(project.isPreserveRelativeIndentOfContinuedLines());
        assertTrue(project.isFormatEmbeddedSql());
        assertEquals(5, project.getSqlIndent());
        assertEquals(SqlBreakStyle.BEFORE, project.getSqlBreakOnComma());
        assertEquals(SqlBreakStyle.NONE, project.getSqlBreakOnAndOr());
        assertTrue(project.isSqlUppercasing());
        assertEquals(SqlCasingStyle.INITIAL, project.getSqlKeywordCasingStyle());
        assertEquals(SqlCasingStyle.LOWER, project.getSqlIdentifierCasingStyle());
        assertEquals(KeywordCasingStyle.UPPER_CAMEL, project.getKeywordCasingStyle());
    }

    @Test
    public void project_withIbmSettings_doesNotChangeRdi() {

        project.setIbmSettingValues(createIbmSettingsOfProject());

        assertEquals(1, workspace.getStartColumn());
        assertEquals(Integer.MAX_VALUE, workspace.getEndColumn());
        assertEquals(2, workspace.getIndent());
        assertEquals(KeywordCasingStyle.LOWERCASE, workspace.getKeywordCasingStyle());
    }

    @Test
    public void project_withIbmSettings_ignoresChangesOfRdi() {

        project.setIbmSettingValues(createIbmSettingsOfProject());

        PreferenceStoreProvider.setIbmTestValue(INDENT, "6");

        assertEquals(4, project.getIndent());
    }

    /**
     * A value, that equals the default value of its type, stays in the file of
     * the project. The preferences of Eclipse would remove it, if it were
     * stored as a number or as a boolean, because the project has no default
     * values of the IBM settings, and then the value of RDi would apply.
     */
    @Test
    public void project_keepsIbmSettingWithDefaultValueOfItsType() {

        PreferenceStoreProvider.setIbmTestValue(SELECT_INDENT, "2");
        PreferenceStoreProvider.setIbmTestValue(SQL_FORMATTING, "true");

        project.setIbmSettingValues(settings(SELECT_INDENT, "0", SQL_FORMATTING, "false"));

        assertEquals(0, project.getSelectIndent());
        assertFalse(project.isFormatEmbeddedSql());
        assertEquals("0", project.getProjectSettingValues().get(SELECT_INDENT));
        assertEquals("false", project.getProjectSettingValues().get(SQL_FORMATTING));
    }

    /**
     * The file of a project of a former version has no IBM settings: the IBM
     * settings of RDi apply, like before.
     */
    @Test
    public void projectFileWithoutIbmSettings_hasIbmSettingsOfRdi() {

        Preferences former = PreferenceStoreProvider
            .createTestProjectPreferences(settings("projectSpecificSettings", "true", "removeEndProcName", "false"));
        PreferenceStoreProvider.setIbmTestValue(INDENT, "4");

        assertTrue(former.isProjectSpecificSettings());
        assertFalse(former.isRemoveEndProcName());
        assertFalse(former.hasIbmSettings());
        assertEquals(4, former.getIndent());
    }

    /**
     * An IBM setting, that is missing in the file of the project, has the value
     * of RDi.
     */
    @Test
    public void projectFileWithSomeIbmSettings_hasMissingIbmSettingsOfRdi() {

        Preferences partial = PreferenceStoreProvider.createTestProjectPreferences(settings(INDENT, "4"));
        PreferenceStoreProvider.setIbmTestValue(END_COLUMN, "100");

        assertTrue(partial.hasIbmSettings());
        assertEquals(4, partial.getIndent());
        assertEquals(100, partial.getEndColumn());
    }

    @Test
    public void setIbmSettingValues_keepsMissingIbmSettings() {

        project.setIbmSettingValues(createIbmSettingsOfProject());
        project.setIbmSettingValues(settings(INDENT, "6"));

        assertEquals(6, project.getIndent());
        assertEquals(100, project.getEndColumn());
    }

    /**
     * The IBM settings of the workspace are the settings of RDi.
     */
    @Test
    public void setIbmSettingValues_ofWorkspace_changesSettingsOfRdi() {

        workspace.setIbmSettingValues(settings(INDENT, "5", KEYWORD_CASING_STYLE, "ENDXX"));

        assertFalse(workspace.hasIbmSettings());
        assertEquals(5, workspace.getIndent());
        assertEquals(KeywordCasingStyle.UPPERCASE, workspace.getKeywordCasingStyle());
        assertEquals(5, project.getIndent());
    }

    @Test
    public void removeProjectSpecificSettings_removesIbmSettings() {

        project.setProjectSpecificSettings();
        project.setIbmSettingValues(createIbmSettingsOfProject());

        project.removeProjectSpecificSettings();

        assertFalse(project.hasIbmSettings());
        assertEquals(workspace.getIbmSettingValues(), project.getIbmSettingValues());
    }

    /**
     * The settings of the project are restored, if its file could not be saved.
     */
    @Test
    public void setProjectSettingValues_restoresIbmSettings() {

        project.setProjectSpecificSettings();
        project.setIbmSettingValues(createIbmSettingsOfProject());
        Map<String, String> values = project.getProjectSettingValues();

        project.removeProjectSpecificSettings();
        project.setProjectSettingValues(values);

        assertTrue(project.hasIbmSettings());
        assertEquals(createIbmSettingsOfProject(), project.getIbmSettingValues());
    }

    @Test
    public void forIbmSettingValues_hasTheGivenIbmSettings() {

        Preferences values = Preferences.forIbmSettingValues(createIbmSettingsOfProject());

        assertEquals(createIbmSettingsOfProject(), values.getIbmSettingValues());
        assertEquals(4, values.getIndent());
        assertEquals(KeywordCasingStyle.UPPER_CAMEL, values.getKeywordCasingStyle());
        assertEquals(2, workspace.getIndent());
    }

    @Test
    public void checkIbmSettingValue_acceptsValidValues() {

        Preferences.checkIbmSettingValue(END_COLUMN, "*MAX");
        Preferences.checkIbmSettingValue(END_COLUMN, "100");
        Preferences.checkIbmSettingValue(INDENT, "2");
        Preferences.checkIbmSettingValue(SQL_BREAK_ON_COMMA, "1");
        Preferences.checkIbmSettingValue(PRESERVE_RELATIVE_INDENT, "true");
        Preferences.checkIbmSettingValue(SQL_FORMATTING, "false");
        for (String style : Arrays.asList("ENDXX", "EndXx", "Endxx", "endXx", "endxx")) {
            Preferences.checkIbmSettingValue(KEYWORD_CASING_STYLE, style);
        }
    }

    /**
     * An invalid value is reported with the name of the setting, e.g. when a
     * profile is imported, and is never written into the settings of RDi.
     */
    @Test
    public void checkIbmSettingValue_rejectsTextOfNumber() {
        assertRejected(SQL_INDENT, "two");
    }

    @Test
    public void checkIbmSettingValue_rejectsTextOfEndColumn() {
        assertRejected(END_COLUMN, "MAX");
    }

    @Test
    public void checkIbmSettingValue_rejectsBooleanOtherThanTrueOrFalse() {
        assertRejected(PRESERVE_RELATIVE_INDENT, "yes");
        assertRejected(SQL_UPPERCASING, "TRUE");
    }

    @Test
    public void checkIbmSettingValue_rejectsUnknownKeywordCasingStyle() {
        assertRejected(KEYWORD_CASING_STYLE, "EnDxX");
    }

    private static void assertRejected(String name, String value) {

        try {
            Preferences.checkIbmSettingValue(name, value);
            fail("IllegalArgumentException expected for " + name + "=" + value);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(name));
            assertTrue(e.getMessage(), e.getMessage().contains(value));
        }
    }

    // --- Differences to RDi ---

    @Test
    public void getIbmSettingDifferences_returnsNamesOfDifferentSettings() {

        project.setIbmSettingValues(settings(INDENT, "4", SQL_INDENT, "5", KEYWORD_CASING_STYLE, "ENDXX"));

        assertEquals(Arrays.asList(INDENT, SQL_INDENT, KEYWORD_CASING_STYLE), project.getIbmSettingDifferences(workspace));
    }

    @Test
    public void getIbmSettingDifferences_withSameValues_isEmpty() {

        project.setIbmSettingValues(workspace.getIbmSettingValues());

        assertEquals(Collections.emptyList(), project.getIbmSettingDifferences(workspace));
    }

    /**
     * A value, that does not change the formatted source, is no difference: the
     * casing of SQL is ignored without automatic uppercasing.
     */
    @Test
    public void getIbmSettingDifferences_ignoresCasingOfSqlWithoutUppercasing() {

        project.setIbmSettingValues(settings(SQL_CASING_STYLE, "2", SQL_KEYWORD_CASING, "2"));

        assertEquals(Collections.emptyList(), project.getIbmSettingDifferences(workspace));
    }

    /**
     * The casing of SQL is compared, as it is applied: "All lowercase" changes
     * the casing of the keywords and of the identifiers.
     */
    @Test
    public void getIbmSettingDifferences_comparesAppliedCasingOfSql() {

        project.setIbmSettingValues(settings(SQL_UPPERCASING, "true", SQL_CASING_STYLE, "1"));

        assertEquals(Arrays.asList(SQL_UPPERCASING, SQL_KEYWORD_CASING, SQL_IDENTIFIER_CASING), project.getIbmSettingDifferences(workspace));
    }

    @Test
    public void getIbmSettingDifferences_comparesEndColumnAsNumber() {

        PreferenceStoreProvider.setIbmTestValue(END_COLUMN, "100");
        project.setIbmSettingValues(settings(END_COLUMN, "0100"));

        assertEquals(Collections.emptyList(), project.getIbmSettingDifferences(workspace));
    }

    /**
     * Returns IBM settings, that differ from the IBM settings of the tests.
     */
    private static Map<String, String> createIbmSettingsOfProject() {

        return settings(START_COLUMN, "8", END_COLUMN, "100", INDENT, "4", SELECT_INDENT, "3", PRESERVE_RELATIVE_INDENT, "false", SQL_FORMATTING,
            "true", SQL_INDENT, "5", SQL_BREAK_ON_COMMA, "1", SQL_BREAK_ON_AND_OR, "2", SQL_UPPERCASING, "true", SQL_CASING_STYLE, "2",
            SQL_KEYWORD_CASING, "2", SQL_IDENTIFIER_CASING, "1", KEYWORD_CASING_STYLE, "EndXx");
    }

    private static Map<String, String> settings(String... namesAndValues) {

        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < namesAndValues.length; i += 2) {
            values.put(namesAndValues[i], namesAndValues[i + 1]);
        }

        return values;
    }

    private String[] format(Preferences preferences, String... source) throws RpgleFormatterException {

        RpgleFormatter formatter = new RpgleFormatter(FormatterConfig.fromPreferences(preferences));
        formatter.setSourceLength(Integer.MAX_VALUE);

        return formatter.format(new TextLinesInput(source), 0).toLines();
    }

    /**
     * Formats the source like the editor: the first level starts at the start
     * column.
     */
    private String[] formatAtStartColumn(Preferences preferences, String... source) throws RpgleFormatterException {

        FormatterConfig config = FormatterConfig.fromPreferences(preferences);
        RpgleFormatter formatter = new RpgleFormatter(config);
        formatter.setSourceLength(Integer.MAX_VALUE);

        return formatter.format(new TextLinesInput(source), config.getStartColumn() - 1).toLines();
    }

    private static Map<String, String> singleKeyword(String key, String value) {

        Map<String, String> keywords = new LinkedHashMap<>();
        keywords.put(key, value);

        return keywords;
    }

    /**
     * Returns <code>true</code> for a setter of a single setting, e.g.
     * <code>setMaxNameLength(int)</code>. The setter of the IBM settings has
     * tests of its own, see
     * {@link #removeProjectSpecificSettings_removesIbmSettings()}.
     */
    private static boolean isSetter(Method method) {
        return method.getName().startsWith("set") && method.getParameterTypes().length == 1 && !Modifier.isStatic(method.getModifiers())
            && method.getReturnType() == void.class && !method.getName().equals("setProjectSettingValues")
            && !method.getName().equals("setIbmSettingValues");
    }

    /**
     * Calls the setter with a value, that changes a getter of the preferences.
     *
     * @return <code>false</code>, if the setter refuses the preferences of a
     *         project
     */
    private static boolean setOtherValue(Preferences preferences, Method setter) throws Exception {

        Map<String, Object> before = getValues(preferences);
        for (Object value : getCandidates(setter.getParameterTypes()[0])) {
            try {
                setter.invoke(preferences, value);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof IllegalStateException) {
                    return false;
                }
                throw e;
            }
            if (!before.equals(getValues(preferences))) {
                return true;
            }
        }

        throw new AssertionError(setter.getName() + " changes no value of the project");
    }

    private static Object[] getCandidates(Class<?> type) {

        if (type == boolean.class) {
            return new Object[] { true, false };
        } else if (type == int.class) {
            return new Object[] { 77, 78 };
        } else if (type == String.class) {
            return new Object[] { "test", "other" };
        } else if (type.isEnum()) {
            return type.getEnumConstants();
        } else if (type == Map.class) {
            return new Object[] { Collections.emptyMap() };
        }

        throw new AssertionError("Unexpected parameter type: " + type.getName());
    }

    /**
     * Returns the values of all getters of the preferences by the names of the
     * getters. An array is returned as its text, because the getters return a
     * new array every time.
     */
    private static Map<String, Object> getValues(Preferences preferences) throws Exception {

        Map<String, Object> values = new TreeMap<>();
        for (Method getter : Preferences.class.getMethods()) {
            String name = getter.getName();
            if ((name.startsWith("get") || name.startsWith("is")) && !name.startsWith("getDefault") && !name.equals("getClass")
                && getter.getParameterTypes().length == 0 && !Modifier.isStatic(getter.getModifiers())) {
                Object value = getter.invoke(preferences);
                values.put(name, value instanceof Object[] ? Arrays.deepToString((Object[])value) : value);
            }
        }

        return values;
    }

    /**
     * Returns the getters, whose values differ, with the expected and the
     * actual value.
     */
    private static Map<String, String> getDifferences(Map<String, Object> expected, Map<String, Object> actual) {

        Map<String, String> differences = new TreeMap<>();
        for (String name : expected.keySet()) {
            if (!Objects.equals(expected.get(name), actual.get(name))) {
                differences.put(name, expected.get(name) + " -> " + actual.get(name));
            }
        }

        return differences;
    }
}
