/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.menu.MenuExtension;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;
import de.tools400.lpex.irpgformatter.utils.KeywordUtils;
import de.tools400.lpex.irpgformatter.utils.ResourceUtils;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

/**
 * The preferences of iRPGFormatter: the preferences of the workspace
 * ({@link #getInstance()}) or of a project
 * ({@link #forProject(IPreferenceStore)}).
 */
public final class Preferences implements RpgleSourceConstants {

    /**
     * The preferences of the workspace.
     */
    private static Preferences instance;

    /**
     * Preferences of the plugin: of the workspace or of a project.
     */
    private final IPreferenceStore preferenceStore;

    /**
     * Preferences of the IBM formatter, always those of the workspace.
     */
    private final IPreferenceStore ibmPreferenceStore;

    private static final String PARAMETER_SPACING_STYLE = "parameterSpacingStyle";
    private static final String USE_CONST_KEYWORD = "useConstKeyword";
    private static final String DELIMITER_BEFORE_PARAMETER = "delimiterBeforeParameter";
    private static final String ALIGN_SUB_FIELDS = "alignSubFields";
    private static final String BREAK_BETWEEN_CASE_CHANGE = "breakBetweenCaseChange";
    private static final String BREAK_BEFORE_KEYWORD = "breakBeforeKeyword";
    private static final String SORT_CONST_VALUE_TO_END = "sortConstValueToEnd";
    private static final String REPLACE_PI_NAME = "replacePiName";
    private static final String REMOVE_END_PROC_NAME = "removeEndProcName";
    private static final String UNINDENT_COMPILER_DIRECTIVES = "unindentCompilerDirectives";
    private static final String REMOVE_EMPTY_COMMENT_LINES = "removeEmptyCommentLines";
    private static final String REMOVE_EMPTY_LINES_BEFORE_DCL_PI = "removeEmptyLinesBeforeDclPi";
    private static final String CONDITION_PARENTHESES = "conditionParentheses";
    private static final String FORMAT_OPERATION_CODES_AND_BUILT_IN_FUNCTIONS = "formatOperationCodesAndBuiltInFunctions";
    private static final String FORMAT_SPECIAL_WORDS_OF_CALCULATIONS = "formatSpecialWordsOfCalculations";
    private static final String MAX_NAME_LENGTH = "maxNameLength";
    private static final String MIN_NAME_LENGTH = "minNameLength";
    private static final String FORMATTER_PREVIEW_VERTICAL_RULER_COLUMN = "formatterPreviewVertialRulerColumn";

    /**
     * Only in the preferences of a project: the project has settings of its own
     */
    private static final String PROJECT_SPECIFIC_SETTINGS = "projectSpecificSettings";

    /** "Execute IBM formatter", replaced by {@link #CALCULATION_FORMATTER} */
    private static final String EXECUTE_IBM_FORMATTER = "executeIbmFormatter";
    /** "Format declarations", formerly "Execute iRPG formatter" */
    private static final String FORMAT_DECLARATIONS = "executeIrpgFormatter";
    /** "Format calculations" */
    private static final String CALCULATION_FORMATTER = "formatCalculations";
    private static final String FORMAT_ON_SAVE = "formatOnSave";

    private static final String DATA_TYPES = "dataTypes";
    private static final String DECLARATION_TYPES = "declarationTypes";
    private static final String KEYWORDS = "keywords";
    private static final String SPECIAL_WORDS = "keywordParameters";
    private static final String OPERATION_CODES = "operationCodes";
    private static final String BUILT_IN_FUNCTIONS = "builtInFunctions";
    private static final String USER_KEY_ACTIONS = "userKeyActions";

    private static final String DEFAULT_FORMATTER_SOURCE = "pluginFormatterSource";
    private static final String CUSTOM_FORMATTER_SOURCE = "customFormatterSource";
    private static final String CUSTOM_PREVIEW_CONTENT = "customPreviewContent";

    private static final KeywordCasingStyle DEFAULT_KEYWORD_CASING_STYLE = KeywordCasingStyle.LOWERCASE;
    private static final ParameterSpacingStyle DEFAULT_PARAMETER_SPACING_STYLE = ParameterSpacingStyle.BEFORE;
    private static final boolean DEFAULT_USE_CONST_KEYWORD = false;
    private static final boolean DEFAULT_DELIMITER_BEFORE_PARAMETER = false;
    private static final boolean DEFAULT_ALIGN_SUB_FIELDS = true;
    private static final boolean DEFAULT_BREAK_BETWEEN_CASE_CHANGE = false;
    private static final boolean DEFAULT_BREAK_BEFORE_KEYWORD = false;
    private static final boolean DEFAULT_SORT_CONST_VALUE_TO_END = false;
    private static final boolean DEFAULT_REPLACE_PI_NAME = true;
    private static final boolean DEFAULT_REMOVE_END_PROC_NAME = true;
    private static final boolean DEFAULT_UNINDENT_COMPILER_DIRECTIVES = true;
    private static final boolean DEFAULT_REMOVE_EMPTY_COMMENT_LINES = false;
    private static final boolean DEFAULT_REMOVE_EMPTY_LINES_BEFORE_DCL_PI = false;
    private static final ConditionParentheses DEFAULT_CONDITION_PARENTHESES = ConditionParentheses.OFF;
    private static final boolean DEFAULT_FORMAT_OPERATION_CODES_AND_BUILT_IN_FUNCTIONS = false;
    private static final boolean DEFAULT_FORMAT_SPECIAL_WORDS_OF_CALCULATIONS = false;
    private static final int DEFAULT_MAX_NAME_LENGTH = 60;
    private static final int DEFAULT_MIN_NAME_LENGTH = 10;
    private static final int DEFAULT_FORMATTER_PREVIEW_VERTICAL_RULER_COLUMN = 35;
    private static final String DEFAULT_PLUGIN_FORMATTER_SOURCE = "/default_preview_source.rpgle";
    private static final String DEFAULT_CUSTOM_FORMATTER_SOURCE = "";
    private static final boolean DEFAULT_CUSTOM_PREVIEW_CONTENT = false;

    private static final boolean DEFAULT_FORMAT_DECLARATIONS = true;
    private static final CalculationFormatter DEFAULT_CALCULATION_FORMATTER = CalculationFormatter.IRPG_FORMATTER;
    private static final boolean DEFAULT_FORMAT_ON_SAVE = false;

    /*
     * The settings of IBM, that iRPGFormatter uses. Their names are the keys of
     * the preferences of IBM, also in the file of a project and in a profile.
     * They are persisted: never rename them.
     */
    public static final String IBM_START_COLUMN = "RPGLE.FORMATTING.start";
    public static final String IBM_END_COLUMN = "RPGLE.FORMATTING.end";
    public static final String IBM_INDENT = "RPGLE.FORMATTING.length";
    public static final String IBM_SELECT_INDENT = "RPGLE.FORMATTING.select";
    public static final String IBM_PRESERVE_RELATIVE_INDENT = "RPGLE.FORMATTING.multiple";
    public static final String IBM_SQL_FORMATTING = "SQLRPGLE.FORMATTING.base";
    public static final String IBM_SQL_INDENT = "SQLRPGLE.INDENT.S1_Blanks";
    public static final String IBM_SQL_BREAK_ON_COMMA = "SQLRPGLE.FORMATTING.onComma";
    public static final String IBM_SQL_BREAK_ON_AND_OR = "SQLRPGLE.FORMATTING.onAndOr";
    public static final String IBM_SQL_UPPERCASING = "SQLRPGLE.UPPERCASING.base";
    public static final String IBM_SQL_CASING_STYLE = "SQLRPGLE.UPPERCASING.base.value";
    public static final String IBM_SQL_KEYWORD_CASING = "SQLRPGLE.UPPERCASING.keywords";
    public static final String IBM_SQL_IDENTIFIER_CASING = "SQLRPGLE.UPPERCASING.identifiers";
    public static final String IBM_KEYWORD_CASING_STYLE = "com.ibm.etools.iseries.edit.preferences.parser.ilerpg.enter.autoclosecontrol.autoclosecontrolvalue";

    /** Value of {@link #IBM_END_COLUMN} without an end column */
    private static final String IBM_END_COLUMN_MAX = "*MAX";

    /** The values of {@link #IBM_KEYWORD_CASING_STYLE}, that RDi writes */
    private static final List<String> IBM_KEYWORD_CASING_STYLES = Collections
        .unmodifiableList(Arrays.asList("ENDXX", "EndXx", "Endxx", "endXx", "endxx"));

    /**
     * The settings of IBM, that iRPGFormatter uses, in the order of the group
     * <i>IBM Settings</i> of the preference page.
     */
    private static final List<String> IBM_SETTINGS = Collections.unmodifiableList(Arrays.asList(IBM_START_COLUMN, IBM_END_COLUMN, IBM_INDENT,
        IBM_SELECT_INDENT, IBM_PRESERVE_RELATIVE_INDENT, IBM_SQL_FORMATTING, IBM_SQL_INDENT, IBM_SQL_BREAK_ON_COMMA, IBM_SQL_BREAK_ON_AND_OR,
        IBM_SQL_UPPERCASING, IBM_SQL_CASING_STYLE, IBM_SQL_KEYWORD_CASING, IBM_SQL_IDENTIFIER_CASING, IBM_KEYWORD_CASING_STYLE));

    /**
     * The settings, that a project can have of its own: the settings of the
     * formatter and the IBM settings. The word lists, the settings of the
     * preview and the shortcut stay those of the workspace.
     */
    private static final List<String> PROJECT_SETTINGS;

    static {
        List<String> projectSettings = new ArrayList<>(Arrays.asList(PROJECT_SPECIFIC_SETTINGS, PARAMETER_SPACING_STYLE, USE_CONST_KEYWORD,
            DELIMITER_BEFORE_PARAMETER, ALIGN_SUB_FIELDS, BREAK_BETWEEN_CASE_CHANGE, BREAK_BEFORE_KEYWORD, SORT_CONST_VALUE_TO_END, REPLACE_PI_NAME,
            REMOVE_END_PROC_NAME, UNINDENT_COMPILER_DIRECTIVES, REMOVE_EMPTY_COMMENT_LINES, REMOVE_EMPTY_LINES_BEFORE_DCL_PI, CONDITION_PARENTHESES,
            FORMAT_OPERATION_CODES_AND_BUILT_IN_FUNCTIONS, FORMAT_SPECIAL_WORDS_OF_CALCULATIONS, MAX_NAME_LENGTH, MIN_NAME_LENGTH,
            EXECUTE_IBM_FORMATTER, FORMAT_DECLARATIONS, CALCULATION_FORMATTER, FORMAT_ON_SAVE));
        projectSettings.addAll(IBM_SETTINGS);
        PROJECT_SETTINGS = Collections.unmodifiableList(projectSettings);
    }

    /**
     * The IBM settings, whose values are numbers. The other IBM settings are
     * the end column, the booleans and the style of the keyword casing.
     */
    private static final List<String> IBM_NUMBER_SETTINGS = Collections
        .unmodifiableList(Arrays.asList(IBM_START_COLUMN, IBM_INDENT, IBM_SELECT_INDENT, IBM_SQL_INDENT, IBM_SQL_BREAK_ON_COMMA,
            IBM_SQL_BREAK_ON_AND_OR, IBM_SQL_CASING_STYLE, IBM_SQL_KEYWORD_CASING, IBM_SQL_IDENTIFIER_CASING));

    /**
     * The IBM settings, whose values are <code>true</code> or
     * <code>false</code>
     */
    private static final List<String> IBM_BOOLEAN_SETTINGS = Collections
        .unmodifiableList(Arrays.asList(IBM_PRESERVE_RELATIVE_INDENT, IBM_SQL_FORMATTING, IBM_SQL_UPPERCASING));

    /**
     * <code>true</code> for the preferences of a project. They return the word
     * lists of the workspace.
     */
    private final boolean project;

    private final WordListCache cachedDataTypes = new WordListCache(DATA_TYPES);
    private final WordListCache cachedDeclarationTypes = new WordListCache(DECLARATION_TYPES);
    private final WordListCache cachedKeywords = new WordListCache(KEYWORDS);
    private final WordListCache cachedKeywordParameters = new WordListCache(SPECIAL_WORDS);
    private final WordListCache cachedOperationCodes = new WordListCache(OPERATION_CODES);
    private final WordListCache cachedBuiltInFunctions = new WordListCache(BUILT_IN_FUNCTIONS);

    private Preferences(IPreferenceStore preferenceStore, IPreferenceStore ibmPreferenceStore, boolean project) {
        this.preferenceStore = preferenceStore;
        this.ibmPreferenceStore = ibmPreferenceStore;
        this.project = project;
    }

    /**
     * Thread-safe method that returns the preferences of the workspace.
     */
    public synchronized static Preferences getInstance() {
        if (instance == null) {
            instance = new Preferences(PreferenceStoreProvider.getPreferenceStore(), PreferenceStoreProvider.getIbmPreferenceStore(), false);
        }
        return instance;
    }

    /**
     * Returns the preferences of a project, that are kept in the given
     * preference store of the project. Their default values are those of the
     * workspace. The word lists have no project specific values: they are
     * always those of the workspace.
     * <p>
     * The preferences of a project apply only, if
     * {@link #isProjectSpecificSettings()} returns <code>true</code>. Then all
     * settings of the formatter come from the project, a setting, that the
     * project does not contain, has the default value. An IBM setting, that the
     * project does not contain, e.g. in the file of a former version, has the
     * value of RDi.
     * </p>
     *
     * @param projectPreferenceStore - the preference store of the project
     * @return the preferences of the project
     */
    public static Preferences forProject(IPreferenceStore projectPreferenceStore) {
        return new Preferences(projectPreferenceStore, PreferenceStoreProvider.getIbmPreferenceStore(), true);
    }

    /**
     * Returns preferences, whose IBM settings have the given values, e.g. the
     * values of the preference page, that are not saved yet. Only the getters
     * of the IBM settings return meaningful values, the other settings have no
     * values. An IBM setting, that is missing in the map, has the value of RDi.
     *
     * @param ibmSettingValues - the values by the names of the IBM settings,
     *        see {@link #getIbmSettingValues()}
     * @return the preferences with the IBM settings
     */
    public static Preferences forIbmSettingValues(Map<String, String> ibmSettingValues) {

        Preferences preferences = new Preferences(new MapPreferenceStore(), PreferenceStoreProvider.getIbmPreferenceStore(), true);
        preferences.setIbmSettingValues(ibmSettingValues);

        return preferences;
    }

    /**
     * Returns <code>true</code> for the preferences of a project.
     */
    public boolean isProject() {
        return project;
    }

    /**
     * A project has no word lists of its own: its getters return the lists of
     * the workspace, its setters must not be called.
     */
    private void checkWordListsOfWorkspace() {
        if (project) {
            throw new IllegalStateException("A project has no word lists of its own.");
        }
    }

    /**
     * Resets the singleton instance. Call this when switching between test mode
     * and normal mode.
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    /**
     * Returns <code>true</code>, if the project of the preferences has settings
     * of its own. The preferences of the workspace never have.
     */
    public boolean isProjectSpecificSettings() {
        return preferenceStore.getBoolean(PROJECT_SPECIFIC_SETTINGS);
    }

    /**
     * Switches the settings of the project on, that are stored with the
     * setters. Use {@link #removeProjectSpecificSettings()} to switch them off.
     */
    public void setProjectSpecificSettings() {
        preferenceStore.setValue(PROJECT_SPECIFIC_SETTINGS, true);
    }

    /**
     * Removes the settings of the project, so that the settings of the
     * workspace apply again.
     */
    public void removeProjectSpecificSettings() {

        for (String name : PROJECT_SETTINGS) {
            preferenceStore.setToDefault(name);
        }
    }

    /**
     * Returns the values of the settings of the project, so that
     * {@link #setProjectSettingValues(Map)} can restore them, e.g. when the
     * file of the project could not be saved. A setting, that has its default
     * value, has the value <code>null</code>.
     *
     * @return the values by the names of the settings
     */
    public Map<String, String> getProjectSettingValues() {

        Map<String, String> values = new HashMap<>();
        for (String name : PROJECT_SETTINGS) {
            values.put(name, preferenceStore.isDefault(name) ? null : preferenceStore.getString(name));
        }

        return values;
    }

    /**
     * Restores the settings of the project, that
     * {@link #getProjectSettingValues()} returned.
     *
     * @param values - the values by the names of the settings
     */
    public void setProjectSettingValues(Map<String, String> values) {

        for (String name : PROJECT_SETTINGS) {
            String value = values.get(name);
            if (value == null) {
                preferenceStore.setToDefault(name);
            } else {
                preferenceStore.setValue(name, value);
            }
        }
    }

    /**
     * Writes the preferences into their file, e.g. the file of the project in
     * its folder <code>.settings</code>. The workspace also writes the IBM
     * settings of RDi, that {@link #setIbmSettingValues(Map)} changed.
     *
     * @throws IOException - the file could not be written
     */
    public void save() throws IOException {

        save(preferenceStore);

        if (!project) {
            save(ibmPreferenceStore);
        }
    }

    private static void save(IPreferenceStore store) throws IOException {

        if (store instanceof IPersistentPreferenceStore) {
            ((IPersistentPreferenceStore)store).save();
        }
    }

    /**
     * Returns the names of the IBM settings, that iRPGFormatter uses, in the
     * order of the group <i>IBM Settings</i> of the preference page. The names
     * are the keys of the preferences of IBM.
     */
    public static List<String> getIbmSettingNames() {
        return IBM_SETTINGS;
    }

    /**
     * Checks the value of an IBM setting, e.g. of a profile, that is imported.
     * The value is written into the settings of RDi as it is, so that it must
     * be a value, that RDi writes itself.
     *
     * @param name - the name of the IBM setting
     * @param value - the value as text
     * @throws IllegalArgumentException - the value is invalid; the message
     *         names the value and the setting
     */
    public static void checkIbmSettingValue(String name, String value) {

        if (!isValidIbmSettingValue(name, value)) {
            throw new IllegalArgumentException(Messages.bind(Messages.Error_Invalid_value_A_of_IBM_setting_B, value, name));
        }
    }

    private static boolean isValidIbmSettingValue(String name, String value) {

        if (IBM_NUMBER_SETTINGS.contains(name)) {
            return isNumber(value);
        } else if (IBM_END_COLUMN.equals(name)) {
            return IBM_END_COLUMN_MAX.equals(value) || isNumber(value);
        } else if (IBM_BOOLEAN_SETTINGS.contains(name)) {
            return Boolean.TRUE.toString().equals(value) || Boolean.FALSE.toString().equals(value);
        } else if (IBM_KEYWORD_CASING_STYLE.equals(name)) {
            return IBM_KEYWORD_CASING_STYLES.contains(value);
        }

        return true;
    }

    private static boolean isNumber(String value) {

        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns <code>true</code>, if the project has IBM settings of its own.
     * The file of a project of a former version has none, and the workspace
     * never has: then the IBM settings of RDi apply.
     */
    public boolean hasIbmSettings() {

        for (String name : IBM_SETTINGS) {
            if (isIbmSettingOfProject(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the values of the IBM settings: those of the project or of RDi.
     *
     * @return the values as text by the names of the IBM settings, in the order
     *         of {@link #getIbmSettingNames()}
     */
    public Map<String, String> getIbmSettingValues() {

        Map<String, String> values = new LinkedHashMap<>();
        for (String name : IBM_SETTINGS) {
            values.put(name, getIbmStore(name).getString(name));
        }

        return values;
    }

    /**
     * Returns the names of the IBM settings, whose values differ from those of
     * the given preferences, e.g. of RDi. The values are compared, as the
     * formatter applies them: the end column as a number, the casing of SQL as
     * the casing of the keywords and of the identifiers, which is ignored
     * without automatic uppercasing. The setting
     * <code>SQLRPGLE.UPPERCASING.base.value</code> is part of the casing and
     * never returned.
     *
     * @param other - the preferences to compare with
     * @return the names in the order of {@link #getIbmSettingNames()}
     */
    public List<String> getIbmSettingDifferences(Preferences other) {

        Map<String, Object> values = getAppliedIbmSettings();
        Map<String, Object> otherValues = other.getAppliedIbmSettings();

        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Object> value : values.entrySet()) {
            if (!Objects.equals(value.getValue(), otherValues.get(value.getKey()))) {
                names.add(value.getKey());
            }
        }

        return names;
    }

    /**
     * Returns the IBM settings, as the formatter applies them, by their names.
     */
    private Map<String, Object> getAppliedIbmSettings() {

        Map<String, Object> values = new LinkedHashMap<>();
        values.put(IBM_START_COLUMN, getStartColumn());
        values.put(IBM_END_COLUMN, getEndColumn());
        values.put(IBM_INDENT, getIndent());
        values.put(IBM_SELECT_INDENT, getSelectIndent());
        values.put(IBM_PRESERVE_RELATIVE_INDENT, isPreserveRelativeIndentOfContinuedLines());
        values.put(IBM_SQL_FORMATTING, isFormatEmbeddedSql());
        values.put(IBM_SQL_INDENT, getSqlIndent());
        values.put(IBM_SQL_BREAK_ON_COMMA, getSqlBreakOnComma());
        values.put(IBM_SQL_BREAK_ON_AND_OR, getSqlBreakOnAndOr());
        values.put(IBM_SQL_UPPERCASING, isSqlUppercasing());
        values.put(IBM_SQL_KEYWORD_CASING, getSqlKeywordCasingStyle());
        values.put(IBM_SQL_IDENTIFIER_CASING, getSqlIdentifierCasingStyle());
        values.put(IBM_KEYWORD_CASING_STYLE, getKeywordCasingStyle());

        return values;
    }

    /**
     * Sets the IBM settings. A project stores them in its file, the workspace
     * writes them into the settings of RDi. An IBM setting, that is missing in
     * the map, keeps its value.
     * <p>
     * The values are stored as text. Eclipse removes a number or a boolean,
     * that equals its default value, and a project has no default values of the
     * IBM settings: e.g. an indent of 0 would be lost and the value of RDi
     * would apply.
     * </p>
     *
     * @param values - the values as text by the names of the IBM settings, see
     *        {@link #getIbmSettingValues()}
     */
    public void setIbmSettingValues(Map<String, String> values) {

        IPreferenceStore store = project ? preferenceStore : ibmPreferenceStore;
        for (String name : IBM_SETTINGS) {
            String value = values.get(name);
            if (value != null) {
                store.setValue(name, value);
            }
        }
    }

    /**
     * Returns the store of an IBM setting: the store of the project, if the
     * project has a value of its own, otherwise the store of RDi.
     */
    private IPreferenceStore getIbmStore(String name) {

        if (isIbmSettingOfProject(name)) {
            return preferenceStore;
        }

        return ibmPreferenceStore;
    }

    private boolean isIbmSettingOfProject(String name) {
        return project && !preferenceStore.isDefault(name);
    }

    public String getStartColumnAsText() {

        String startColumnStr = Integer.toString(getStartColumn());

        return startColumnStr;
    }

    public int getStartColumn() {

        int startColumn = getIbmStore(IBM_START_COLUMN).getInt(IBM_START_COLUMN);

        return startColumn;
    }

    public String getEndColumnAsText() {

        IPreferenceStore store = getIbmStore(IBM_END_COLUMN);
        String endColumnStr = store.getString(IBM_END_COLUMN);
        if (!IBM_END_COLUMN_MAX.equals(endColumnStr)) {
            endColumnStr = Integer.toString(store.getInt(IBM_END_COLUMN));
        }

        return endColumnStr;
    }

    public int getEndColumn() {

        IPreferenceStore store = getIbmStore(IBM_END_COLUMN);
        String strLength = store.getString(IBM_END_COLUMN);
        if (IBM_END_COLUMN_MAX.equals(strLength)) {
            return Integer.MAX_VALUE;
        }

        if (StringUtils.isNullOrEmpty(strLength)) {
            return store.getInt(IBM_END_COLUMN);
        }

        return Integer.parseInt(strLength);
    }

    public int getIndent() {
        return getIbmStore(IBM_INDENT).getInt(IBM_INDENT);
    }

    /**
     * Returns the extra blanks of WHEN and OTHER within a SELECT statement, see
     * IBM preference "Indent WHEN/OTHER within SELECT statement".
     */
    public int getSelectIndent() {
        return getIbmStore(IBM_SELECT_INDENT).getInt(IBM_SELECT_INDENT);
    }

    /**
     * Returns <code>true</code>, if continued lines keep their indent relative
     * to the first line of the statement, see IBM preference "Preserve relative
     * indentation of continued lines". Otherwise continued lines are lined up.
     */
    public boolean isPreserveRelativeIndentOfContinuedLines() {
        return getIbmStore(IBM_PRESERVE_RELATIVE_INDENT).getBoolean(IBM_PRESERVE_RELATIVE_INDENT);
    }

    /**
     * Returns <code>true</code>, if embedded SQL statements are formatted, see
     * IBM preference "Formatting" of the ILE RPG SQL formatting. Otherwise only
     * their casing is changed.
     */
    public boolean isFormatEmbeddedSql() {
        return getIbmStore(IBM_SQL_FORMATTING).getBoolean(IBM_SQL_FORMATTING);
    }

    /**
     * Returns <code>true</code>, if the casing of embedded SQL statements is
     * changed, see IBM preference "Automatic uppercasing" of the ILE RPG SQL
     * formatting.
     */
    public boolean isSqlUppercasing() {
        return getIbmStore(IBM_SQL_UPPERCASING).getBoolean(IBM_SQL_UPPERCASING);
    }

    /**
     * Returns the number of blanks of each indent level of an embedded SQL
     * statement, see IBM preference "Indent" of the ILE RPG SQL formatting.
     */
    public int getSqlIndent() {
        return getIbmStore(IBM_SQL_INDENT).getInt(IBM_SQL_INDENT);
    }

    /**
     * Returns the line break at the commas of an embedded SQL statement, see
     * IBM preference "New line: On comma" of the ILE RPG SQL formatting.
     */
    public SqlBreakStyle getSqlBreakOnComma() {
        return SqlBreakStyle.fromIbmValue(getIbmStore(IBM_SQL_BREAK_ON_COMMA).getInt(IBM_SQL_BREAK_ON_COMMA));
    }

    /**
     * Returns the line break at AND/OR of an embedded SQL statement, see IBM
     * preference "On AND/OR keywords" of the ILE RPG SQL formatting.
     */
    public SqlBreakStyle getSqlBreakOnAndOr() {
        return SqlBreakStyle.fromIbmValue(getIbmStore(IBM_SQL_BREAK_ON_AND_OR).getInt(IBM_SQL_BREAK_ON_AND_OR));
    }

    /**
     * Returns the casing of the keywords of an embedded SQL statement, see IBM
     * preference "Automatic uppercasing" of the ILE RPG SQL formatting.
     */
    public SqlCasingStyle getSqlKeywordCasingStyle() {
        return getSqlCasingStyle(IBM_SQL_KEYWORD_CASING);
    }

    /**
     * Returns the casing of the identifiers of an embedded SQL statement, see
     * IBM preference "Automatic uppercasing" of the ILE RPG SQL formatting.
     */
    public SqlCasingStyle getSqlIdentifierCasingStyle() {
        return getSqlCasingStyle(IBM_SQL_IDENTIFIER_CASING);
    }

    /**
     * Returns the casing of an embedded SQL statement like RDi: the style "All
     * uppercase" or "All lowercase" applies to the keywords and to the
     * identifiers, the style "Custom" uses the value of the given preference.
     */
    private SqlCasingStyle getSqlCasingStyle(String customKey) {

        if (!isSqlUppercasing()) {
            return SqlCasingStyle.IGNORE;
        }

        switch (getIbmStore(IBM_SQL_CASING_STYLE).getInt(IBM_SQL_CASING_STYLE)) {
        case 0:
            return SqlCasingStyle.UPPER;
        case 1:
            return SqlCasingStyle.LOWER;
        case 2:
            return SqlCasingStyle.fromIbmValue(getIbmStore(customKey).getInt(customKey));
        default:
            return SqlCasingStyle.IGNORE;
        }
    }

    public KeywordCasingStyle getKeywordCasingStyle() {

        String ibmCasingStyle = getIbmStore(IBM_KEYWORD_CASING_STYLE).getString(IBM_KEYWORD_CASING_STYLE);
        if ("ENDXX".equals(ibmCasingStyle)) {
            return KeywordCasingStyle.UPPERCASE;
        } else if ("EndXx".equals(ibmCasingStyle)) {
            return KeywordCasingStyle.UPPER_CAMEL;
        } else if ("Endxx".equals(ibmCasingStyle)) {
            return KeywordCasingStyle.FIRST_UPPER;
        } else if ("endXx".equals(ibmCasingStyle)) {
            return KeywordCasingStyle.LOWER_CAMEL;
        } else {
            return KeywordCasingStyle.LOWERCASE;
        }
    }

    public ParameterSpacingStyle getParameterSpacingStyle() {
        try {
            String parameterSpacingStyle = preferenceStore.getString(PARAMETER_SPACING_STYLE);
            return ParameterSpacingStyle.valueOf(parameterSpacingStyle);
        } catch (Exception e) {
            return getDefaultParameterSpacingStyle();
        }
    }

    public String[] getParameterSpacingStyles() {
        List<String> parameterSpacingStyles = new LinkedList<>();
        ParameterSpacingStyle[] spacingStyles = ParameterSpacingStyle.values();
        for (int i = 0; i < spacingStyles.length; i++) {
            parameterSpacingStyles.add(spacingStyles[i].getDisplayName());
        }
        return parameterSpacingStyles.toArray(new String[0]);
    }

    public void setParameterSpacingStyle(ParameterSpacingStyle style) {
        preferenceStore.setValue(PARAMETER_SPACING_STYLE, style.name());
    }

    public boolean isUseConstKeyword() {
        return preferenceStore.getBoolean(USE_CONST_KEYWORD);
    }

    public void setUseConstKeyword(boolean useConstKeyword) {
        preferenceStore.setValue(USE_CONST_KEYWORD, useConstKeyword);
    }

    public boolean isDelimiterBeforeParameter() {
        return preferenceStore.getBoolean(DELIMITER_BEFORE_PARAMETER);
    }

    public void setDelimiterBeforeParameter(boolean delimiterBeforeParameter) {
        preferenceStore.setValue(DELIMITER_BEFORE_PARAMETER, delimiterBeforeParameter);
    }

    public boolean isAlignSubFields() {
        return preferenceStore.getBoolean(ALIGN_SUB_FIELDS);
    }

    public void setAlignSubFields(boolean enabled) {
        preferenceStore.setValue(ALIGN_SUB_FIELDS, enabled);
    }

    public boolean isBreakBetweenCaseChange() {
        return preferenceStore.getBoolean(BREAK_BETWEEN_CASE_CHANGE);
    }

    public void setBreakBetweenCaseChange(boolean enabled) {
        preferenceStore.setValue(BREAK_BETWEEN_CASE_CHANGE, enabled);
    }

    public boolean isBreakBeforeKeyword() {
        return preferenceStore.getBoolean(BREAK_BEFORE_KEYWORD);
    }

    public void setBreakBeforeKeyword(boolean enabled) {
        preferenceStore.setValue(BREAK_BEFORE_KEYWORD, enabled);
    }

    public boolean isSortConstValueToEnd() {
        return preferenceStore.getBoolean(SORT_CONST_VALUE_TO_END);
    }

    public void setSortConstValueToEnd(boolean enabled) {
        preferenceStore.setValue(SORT_CONST_VALUE_TO_END, enabled);
    }

    public boolean isReplacePiName() {
        return preferenceStore.getBoolean(REPLACE_PI_NAME);
    }

    public void setReplacePiName(boolean enabled) {
        preferenceStore.setValue(REPLACE_PI_NAME, enabled);
    }

    public boolean isRemoveEndProcName() {
        return preferenceStore.getBoolean(REMOVE_END_PROC_NAME);
    }

    public void setRemoveEndProcName(boolean enabled) {
        preferenceStore.setValue(REMOVE_END_PROC_NAME, enabled);
    }

    public boolean isUnindentCompilerDirectives() {
        return preferenceStore.getBoolean(UNINDENT_COMPILER_DIRECTIVES);
    }

    public void setUnindentCompilerDirectives(boolean enabled) {
        preferenceStore.setValue(UNINDENT_COMPILER_DIRECTIVES, enabled);
    }

    public boolean isRemoveEmptyCommentLines() {
        return preferenceStore.getBoolean(REMOVE_EMPTY_COMMENT_LINES);
    }

    public void setRemoveEmptyCommentLines(boolean enabled) {
        preferenceStore.setValue(REMOVE_EMPTY_COMMENT_LINES, enabled);
    }

    public boolean isRemoveEmptyLinesBeforeDclPi() {
        return preferenceStore.getBoolean(REMOVE_EMPTY_LINES_BEFORE_DCL_PI);
    }

    public void setRemoveEmptyLinesBeforeDclPi(boolean enabled) {
        preferenceStore.setValue(REMOVE_EMPTY_LINES_BEFORE_DCL_PI, enabled);
    }

    public boolean getDefaultRemoveEmptyLinesBeforeDclPi() {
        return DEFAULT_REMOVE_EMPTY_LINES_BEFORE_DCL_PI;
    }

    /**
     * Returns whether the condition of an IF, ELSEIF, WHEN, DOW or DOU
     * statement is enclosed in parentheses.
     */
    public ConditionParentheses getConditionParentheses() {
        try {
            return ConditionParentheses.valueOf(preferenceStore.getString(CONDITION_PARENTHESES));
        } catch (Exception e) {
            return getDefaultConditionParentheses();
        }
    }

    public void setConditionParentheses(ConditionParentheses style) {
        preferenceStore.setValue(CONDITION_PARENTHESES, style.name());
    }

    public String[] getConditionParenthesesStyles() {
        List<String> styles = new LinkedList<>();
        for (ConditionParentheses style : ConditionParentheses.values()) {
            styles.add(style.getDisplayName());
        }
        return styles.toArray(new String[0]);
    }

    public ConditionParentheses getDefaultConditionParentheses() {
        return DEFAULT_CONDITION_PARENTHESES;
    }

    /**
     * Returns <code>true</code>, if the operation codes and the built-in
     * functions of the calculations are formatted in the keyword casing style.
     */
    public boolean isFormatOperationCodesAndBuiltInFunctions() {
        return preferenceStore.getBoolean(FORMAT_OPERATION_CODES_AND_BUILT_IN_FUNCTIONS);
    }

    public void setFormatOperationCodesAndBuiltInFunctions(boolean enabled) {
        preferenceStore.setValue(FORMAT_OPERATION_CODES_AND_BUILT_IN_FUNCTIONS, enabled);
    }

    public boolean getDefaultFormatOperationCodesAndBuiltInFunctions() {
        return DEFAULT_FORMAT_OPERATION_CODES_AND_BUILT_IN_FUNCTIONS;
    }

    public boolean isFormatSpecialWordsOfCalculations() {
        return preferenceStore.getBoolean(FORMAT_SPECIAL_WORDS_OF_CALCULATIONS);
    }

    public void setFormatSpecialWordsOfCalculations(boolean enabled) {
        preferenceStore.setValue(FORMAT_SPECIAL_WORDS_OF_CALCULATIONS, enabled);
    }

    public boolean getDefaultFormatSpecialWordsOfCalculations() {
        return DEFAULT_FORMAT_SPECIAL_WORDS_OF_CALCULATIONS;
    }

    /**
     * Returns the canonical spellings of the operation codes by the operation
     * code in upper case.
     */
    public Map<String, String> getOperationCodes() {
        if (project) {
            return getInstance().getOperationCodes();
        }
        return cachedOperationCodes.get(preferenceStore);
    }

    public void setOperationCodes(Map<String, String> operationCodes) {
        checkWordListsOfWorkspace();
        String value = KeywordUtils.keywordsToString(operationCodes);
        preferenceStore.setValue(OPERATION_CODES, value);
    }

    public Map<String, String> getDefaultOperationCodes() {
        return KeywordUtils.getDefaultOperationCodes();
    }

    /**
     * Returns the canonical spellings of the built-in functions by the built-in
     * function in upper case.
     */
    public Map<String, String> getBuiltInFunctions() {
        if (project) {
            return getInstance().getBuiltInFunctions();
        }
        return cachedBuiltInFunctions.get(preferenceStore);
    }

    public void setBuiltInFunctions(Map<String, String> builtInFunctions) {
        checkWordListsOfWorkspace();
        String value = KeywordUtils.keywordsToString(builtInFunctions);
        preferenceStore.setValue(BUILT_IN_FUNCTIONS, value);
    }

    public Map<String, String> getDefaultBuiltInFunctions() {
        return KeywordUtils.getDefaultBuiltInFunctions();
    }

    /**
     * Returns <code>true</code>, if iRPGFormatter formats everything except the
     * calculations: the control options, the declarations and the compiler
     * directives.
     */
    public boolean isFormatDeclarations() {
        return preferenceStore.getBoolean(FORMAT_DECLARATIONS);
    }

    public void setFormatDeclarations(boolean enabled) {
        preferenceStore.setValue(FORMAT_DECLARATIONS, enabled);
    }

    /**
     * Returns the formatter of the calculations.
     * <p>
     * The preference replaces "Execute IBM formatter". If that one was switched
     * off, the calculations stay unchanged.
     * </p>
     */
    public CalculationFormatter getCalculationFormatter() {

        if (preferenceStore.isDefault(CALCULATION_FORMATTER) && !preferenceStore.isDefault(EXECUTE_IBM_FORMATTER)
            && !preferenceStore.getBoolean(EXECUTE_IBM_FORMATTER)) {
            return CalculationFormatter.OFF;
        }

        try {
            return CalculationFormatter.valueOf(preferenceStore.getString(CALCULATION_FORMATTER));
        } catch (Exception e) {
            return getDefaultCalculationFormatter();
        }
    }

    public void setCalculationFormatter(CalculationFormatter formatter) {
        preferenceStore.setValue(CALCULATION_FORMATTER, formatter.name());
        // The replaced preference must not override the new one, when it
        // becomes the default value.
        preferenceStore.setToDefault(EXECUTE_IBM_FORMATTER);
    }

    public String[] getCalculationFormatters() {
        List<String> calculationFormatters = new LinkedList<>();
        for (CalculationFormatter formatter : CalculationFormatter.values()) {
            calculationFormatters.add(formatter.getDisplayName());
        }
        return calculationFormatters.toArray(new String[0]);
    }

    public boolean isFormatOnSave() {
        return preferenceStore.getBoolean(FORMAT_ON_SAVE);
    }

    public void setFormatOnSave(boolean enabled) {
        preferenceStore.setValue(FORMAT_ON_SAVE, enabled);
    }

    public int getMaxNameLength() {
        return preferenceStore.getInt(MAX_NAME_LENGTH);
    }

    public void setMaxNameLength(int maxNameLength) {
        preferenceStore.setValue(MAX_NAME_LENGTH, maxNameLength);
    }

    public int getMinNameLength() {
        return preferenceStore.getInt(MIN_NAME_LENGTH);
    }

    public void setMinLiteralLength(int minLiteralLength) {
        preferenceStore.setValue(MIN_NAME_LENGTH, minLiteralLength);
    }

    public String getDefaultFormatterSource() {
        return preferenceStore.getString(DEFAULT_FORMATTER_SOURCE);
    }

    public void setDefaultFormatterSource(String source) {
        preferenceStore.setValue(DEFAULT_FORMATTER_SOURCE, source);
    }

    public String getCustomFormatterSource() {
        return preferenceStore.getString(CUSTOM_FORMATTER_SOURCE);
    }

    public void setCustomFormatterSource(String source) {
        preferenceStore.setValue(CUSTOM_FORMATTER_SOURCE, source);
    }

    public boolean isCustomPreviewContent() {
        return preferenceStore.getBoolean(CUSTOM_PREVIEW_CONTENT);
    }

    public void setCustomPreviewContent(boolean customPreviewContent) {
        preferenceStore.setValue(CUSTOM_PREVIEW_CONTENT, customPreviewContent);
    }

    public int getFormatterPreviewVerticalRulerColumn() {
        return preferenceStore.getInt(FORMATTER_PREVIEW_VERTICAL_RULER_COLUMN);
    }

    public void setFormatterPreviewVerticalRulerColumn(int column) {
        preferenceStore.setValue(FORMATTER_PREVIEW_VERTICAL_RULER_COLUMN, column);
    }

    public String getUserKeyActions() {
        return preferenceStore.getString(USER_KEY_ACTIONS);
    }

    public Map<String, String> getDataTypes() {
        if (project) {
            return getInstance().getDataTypes();
        }
        return cachedDataTypes.get(preferenceStore);
    }

    public Map<String, String> getDeclarationTypes() {
        if (project) {
            return getInstance().getDeclarationTypes();
        }
        return cachedDeclarationTypes.get(preferenceStore);
    }

    public void setDataTypes(Map<String, String> dataTypes) {
        checkWordListsOfWorkspace();
        String value = KeywordUtils.keywordsToString(dataTypes);
        preferenceStore.setValue(DATA_TYPES, value);
    }

    public void setDeclarationTypes(Map<String, String> declarationTypes) {
        checkWordListsOfWorkspace();
        String value = KeywordUtils.keywordsToString(declarationTypes);
        preferenceStore.setValue(DECLARATION_TYPES, value);
    }

    public Map<String, String> getKeywords() {
        if (project) {
            return getInstance().getKeywords();
        }
        return cachedKeywords.get(preferenceStore);
    }

    public void setKeywords(Map<String, String> keywords) {
        checkWordListsOfWorkspace();
        String value = KeywordUtils.keywordsToString(keywords);
        preferenceStore.setValue(KEYWORDS, value);
    }

    public Map<String, String> getSpecialWords() {
        if (project) {
            return getInstance().getSpecialWords();
        }
        return cachedKeywordParameters.get(preferenceStore);
    }

    public void setSpecialWords(Map<String, String> keywordParameters) {
        checkWordListsOfWorkspace();
        String value = KeywordUtils.keywordsToString(keywordParameters);
        preferenceStore.setValue(SPECIAL_WORDS, value);
    }

    public void initializeDefaultPreferences() {

        preferenceStore.setDefault(PARAMETER_SPACING_STYLE, getDefaultParameterSpacingStyle().name());
        preferenceStore.setDefault(USE_CONST_KEYWORD, getDefaultUseConstKeyword());
        preferenceStore.setDefault(DELIMITER_BEFORE_PARAMETER, getDefaultDelimiterBeforeParameter());
        preferenceStore.setDefault(ALIGN_SUB_FIELDS, getDefaultAlignSubFields());
        preferenceStore.setDefault(BREAK_BETWEEN_CASE_CHANGE, getDefaultBreakBetweenCaseChange());
        preferenceStore.setDefault(BREAK_BEFORE_KEYWORD, getDefaultBreakBeforeKeyword());
        preferenceStore.setDefault(SORT_CONST_VALUE_TO_END, getDefaultSortConstValueToEnd());
        preferenceStore.setDefault(REPLACE_PI_NAME, getDefaultReplacePiName());
        preferenceStore.setDefault(REMOVE_END_PROC_NAME, getDefaultRemoveEndProcName());
        preferenceStore.setDefault(UNINDENT_COMPILER_DIRECTIVES, getDefaultUnindentCompilerDirectives());
        preferenceStore.setDefault(REMOVE_EMPTY_COMMENT_LINES, getDefaultRemoveEmptyCommentLines());
        preferenceStore.setDefault(REMOVE_EMPTY_LINES_BEFORE_DCL_PI, getDefaultRemoveEmptyLinesBeforeDclPi());
        preferenceStore.setDefault(CONDITION_PARENTHESES, getDefaultConditionParentheses().name());
        preferenceStore.setDefault(FORMAT_OPERATION_CODES_AND_BUILT_IN_FUNCTIONS, getDefaultFormatOperationCodesAndBuiltInFunctions());
        preferenceStore.setDefault(FORMAT_SPECIAL_WORDS_OF_CALCULATIONS, getDefaultFormatSpecialWordsOfCalculations());
        preferenceStore.setDefault(EXECUTE_IBM_FORMATTER, true);
        preferenceStore.setDefault(FORMAT_DECLARATIONS, getDefaultFormatDeclarations());
        preferenceStore.setDefault(CALCULATION_FORMATTER, getDefaultCalculationFormatter().name());
        preferenceStore.setDefault(FORMAT_ON_SAVE, getDefaultFormatOnSave());
        preferenceStore.setDefault(USER_KEY_ACTIONS, getDefaultUserKeyActions());
        preferenceStore.setDefault(DATA_TYPES, getDefaultDataTypesAsString());
        preferenceStore.setDefault(DECLARATION_TYPES, getDefaultDeclarationTypesAsString());
        preferenceStore.setDefault(KEYWORDS, getDefaultKeywordsAsString());
        preferenceStore.setDefault(SPECIAL_WORDS, getDefaultSpecialWordsAsString());
        preferenceStore.setDefault(OPERATION_CODES, KeywordUtils.keywordsToString(getDefaultOperationCodes()));
        preferenceStore.setDefault(BUILT_IN_FUNCTIONS, KeywordUtils.keywordsToString(getDefaultBuiltInFunctions()));
        preferenceStore.setDefault(MAX_NAME_LENGTH, getDefaultMaxNameLength());
        preferenceStore.setDefault(MIN_NAME_LENGTH, getDefaultMinLiteralLength());
        preferenceStore.setDefault(DEFAULT_FORMATTER_SOURCE, getDefaultPluginFormatterSource());
        preferenceStore.setDefault(CUSTOM_FORMATTER_SOURCE, getDefaultCustomFormatterSource());
        preferenceStore.setDefault(CUSTOM_PREVIEW_CONTENT, getDefaultCustomPreviewContent());
        preferenceStore.setDefault(FORMATTER_PREVIEW_VERTICAL_RULER_COLUMN, getDefaultFormatterPreviewVerticalRulerColumn());

        if (PreferenceStoreProvider.isTestMode()) {
            ibmPreferenceStore.setDefault(IBM_INDENT, 2);
            ibmPreferenceStore.setDefault(IBM_START_COLUMN, 1);
            ibmPreferenceStore.setDefault(IBM_END_COLUMN, IBM_END_COLUMN_MAX);
            ibmPreferenceStore.setDefault(IBM_SELECT_INDENT, 0);
            ibmPreferenceStore.setDefault(IBM_PRESERVE_RELATIVE_INDENT, true);

            ibmPreferenceStore.setDefault(IBM_SQL_FORMATTING, false);
            ibmPreferenceStore.setDefault(IBM_SQL_INDENT, 2);
            ibmPreferenceStore.setDefault(IBM_SQL_BREAK_ON_COMMA, 0);
            ibmPreferenceStore.setDefault(IBM_SQL_BREAK_ON_AND_OR, 0);
            ibmPreferenceStore.setDefault(IBM_SQL_UPPERCASING, false);
            ibmPreferenceStore.setDefault(IBM_SQL_CASING_STYLE, 0);
            ibmPreferenceStore.setDefault(IBM_SQL_KEYWORD_CASING, 0);
            ibmPreferenceStore.setDefault(IBM_SQL_IDENTIFIER_CASING, 0);

            ibmPreferenceStore.setDefault(IBM_KEYWORD_CASING_STYLE, "endxx");
        }
    }

    public KeywordCasingStyle getDefaultKeywordCasingStyle() {
        return DEFAULT_KEYWORD_CASING_STYLE;
    }

    public ParameterSpacingStyle getDefaultParameterSpacingStyle() {
        return DEFAULT_PARAMETER_SPACING_STYLE;
    }

    public boolean getDefaultUseConstKeyword() {
        return DEFAULT_USE_CONST_KEYWORD;
    }

    public boolean getDefaultDelimiterBeforeParameter() {
        return DEFAULT_DELIMITER_BEFORE_PARAMETER;
    }

    public boolean getDefaultAlignSubFields() {
        return DEFAULT_ALIGN_SUB_FIELDS;
    }

    public boolean getDefaultBreakBetweenCaseChange() {
        return DEFAULT_BREAK_BETWEEN_CASE_CHANGE;
    }

    public boolean getDefaultBreakBeforeKeyword() {
        return DEFAULT_BREAK_BEFORE_KEYWORD;
    }

    public boolean getDefaultSortConstValueToEnd() {
        return DEFAULT_SORT_CONST_VALUE_TO_END;
    }

    public boolean getDefaultReplacePiName() {
        return DEFAULT_REPLACE_PI_NAME;
    }

    public boolean getDefaultRemoveEndProcName() {
        return DEFAULT_REMOVE_END_PROC_NAME;
    }

    public boolean getDefaultUnindentCompilerDirectives() {
        return DEFAULT_UNINDENT_COMPILER_DIRECTIVES;
    }

    public boolean getDefaultRemoveEmptyCommentLines() {
        return DEFAULT_REMOVE_EMPTY_COMMENT_LINES;
    }

    public boolean getDefaultFormatDeclarations() {
        return DEFAULT_FORMAT_DECLARATIONS;
    }

    public CalculationFormatter getDefaultCalculationFormatter() {
        return DEFAULT_CALCULATION_FORMATTER;
    }

    public boolean getDefaultFormatOnSave() {
        return DEFAULT_FORMAT_ON_SAVE;
    }

    public int getDefaultMaxNameLength() {
        return DEFAULT_MAX_NAME_LENGTH;
    }

    public int getDefaultMinLiteralLength() {
        return DEFAULT_MIN_NAME_LENGTH;
    }

    public String getDefaultPluginFormatterSource() {
        String defaultPreviewSource = ResourceUtils.loadFromStringResource(DEFAULT_PLUGIN_FORMATTER_SOURCE);
        return defaultPreviewSource;
    }

    public String getDefaultCustomFormatterSource() {
        return DEFAULT_CUSTOM_FORMATTER_SOURCE;
    }

    public boolean getDefaultCustomPreviewContent() {
        return DEFAULT_CUSTOM_PREVIEW_CONTENT;
    }

    public int getDefaultFormatterPreviewVerticalRulerColumn() {
        return DEFAULT_FORMATTER_PREVIEW_VERTICAL_RULER_COLUMN;
    }

    public String getDefaultUserKeyActions() {
        return MenuExtension.getInitialUserKeyActions();
    }

    public Map<String, String> getDefaultDataTypes() {
        return KeywordUtils.getDefaultDataTypes();
    }

    public Map<String, String> getDefaultDeclarationTypes() {
        return KeywordUtils.getDefaultDeclarationTypes();
    }

    public Map<String, String> getDefaultKeywords() {
        return KeywordUtils.getDefaultKeywords();
    }

    public Map<String, String> getDefaultSpecialWords() {
        return KeywordUtils.getDefaultSpecialWords();
    }

    private String getDefaultDataTypesAsString() {
        return KeywordUtils.dataTypesToString(getDefaultDataTypes());
    }

    private String getDefaultDeclarationTypesAsString() {
        return KeywordUtils.declarationTypesToString(getDefaultDeclarationTypes());
    }

    private String getDefaultKeywordsAsString() {
        return KeywordUtils.keywordsToString(getDefaultKeywords());
    }

    private String getDefaultSpecialWordsAsString() {
        return KeywordUtils.keywordsToString(getDefaultSpecialWords());
    }
}
