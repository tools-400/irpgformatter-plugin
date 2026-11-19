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

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;
import de.tools400.lpex.irpgformatter.preferences.CalculationFormatter;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.PreferenceStoreProvider;
import de.tools400.lpex.irpgformatter.preferences.Preferences;

/**
 * Unit tests for the project specific settings: the preferences of a project
 * are independent of the preferences of the workspace, and the formatter uses
 * the preferences, it is created with.
 */
public class ProjectPreferencesTest extends AbstractTestCase {

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
     * The IBM settings have no project specific values.
     */
    @Test
    public void formatterConfig_ofProject_hasIbmValuesOfWorkspace() {

        FormatterConfig projectConfig = FormatterConfig.fromPreferences(project);
        FormatterConfig workspaceConfig = FormatterConfig.fromPreferences(workspace);

        assertEquals(workspaceConfig.getIndent(), projectConfig.getIndent());
        assertEquals(workspaceConfig.getMaxLineWidth(), projectConfig.getMaxLineWidth());
        assertEquals(workspaceConfig.getKeywordCasingStyle(), projectConfig.getKeywordCasingStyle());
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

    private String[] format(Preferences preferences, String... source) throws RpgleFormatterException {

        RpgleFormatter formatter = new RpgleFormatter(FormatterConfig.fromPreferences(preferences));
        formatter.setSourceLength(Integer.MAX_VALUE);

        return formatter.format(new TextLinesInput(source), 0).toLines();
    }

    private static Map<String, String> singleKeyword(String key, String value) {

        Map<String, String> keywords = new LinkedHashMap<>();
        keywords.put(key, value);

        return keywords;
    }
}
