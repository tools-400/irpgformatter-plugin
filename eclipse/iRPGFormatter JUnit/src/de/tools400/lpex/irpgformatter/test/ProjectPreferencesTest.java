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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import de.tools400.lpex.irpgformatter.preferences.PreferenceStoreProvider;
import de.tools400.lpex.irpgformatter.preferences.Preferences;

/**
 * Unit tests for the project specific settings: the preferences of a project
 * are independent of the preferences of the workspace, and the formatter uses
 * the preferences, it is created with.
 */
public class ProjectPreferencesTest extends AbstractTestCase {

    /**
     * The setters of the settings, that only the workspace has: a project uses
     * the preview and the IBM settings of the workspace. Every other setter
     * must write a setting of <code>PROJECT_SETTINGS</code> of
     * {@link Preferences}, see
     * {@link #removeProjectSpecificSettings_removesEverySetting()}. The setters
     * of the word lists refuse a project themselves.
     */
    private static final Set<String> SETTERS_OF_WORKSPACE = new HashSet<>(Arrays.asList("setMaxLineLength", "setDefaultFormatterSource",
        "setCustomFormatterSource", "setCustomPreviewContent", "setFormatterPreviewVerticalRulerColumn"));

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

    /**
     * Returns <code>true</code> for a setter of a single setting, e.g.
     * <code>setMaxNameLength(int)</code>.
     */
    private static boolean isSetter(Method method) {
        return method.getName().startsWith("set") && method.getParameterTypes().length == 1 && !Modifier.isStatic(method.getModifiers())
            && method.getReturnType() == void.class && !method.getName().equals("setProjectSettingValues");
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
