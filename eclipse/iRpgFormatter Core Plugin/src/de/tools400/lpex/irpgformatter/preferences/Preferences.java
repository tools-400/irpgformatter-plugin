/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferences;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;

import de.tools400.lpex.irpgformatter.menu.MenuExtension;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;
import de.tools400.lpex.irpgformatter.utils.KeywordUtils;
import de.tools400.lpex.irpgformatter.utils.ResourceUtils;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

/**
 * Constants for RPGLE Formatter preferences.
 */
public final class Preferences implements RpgleSourceConstants {

    /**
     * The instance of this Singleton class.
     */
    private static Preferences instance;

    /**
     * Global preferences of the plugin.
     */
    private IPreferenceStore preferenceStore;

    /**
     * Preferences of the IBM formatter
     */
    private IPreferenceStore ibmPreferenceStore;

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
    private static final String MAX_NAME_LENGTH = "maxNameLength";
    private static final String MIN_NAME_LENGTH = "minNameLength";
    private static final String FORMATTER_PREVIEW_VERTICAL_RULER_COLUMN = "formatterPreviewVertialRulerColumn";

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
    private static final int DEFAULT_MAX_NAME_LENGTH = 60;
    private static final int DEFAULT_MIN_NAME_LENGTH = 10;
    private static final int DEFAULT_FORMATTER_PREVIEW_VERTICAL_RULER_COLUMN = 35;
    private static final String DEFAULT_PLUGIN_FORMATTER_SOURCE = "/default_preview_source.rpgle";
    private static final String DEFAULT_CUSTOM_FORMATTER_SOURCE = "";
    private static final boolean DEFAULT_CUSTOM_PREVIEW_CONTENT = false;

    private static final boolean DEFAULT_FORMAT_DECLARATIONS = true;
    private static final CalculationFormatter DEFAULT_CALCULATION_FORMATTER = CalculationFormatter.IRPG_FORMATTER;
    private static final boolean DEFAULT_FORMAT_ON_SAVE = false;

    private static Map<String, String> cachedDataTypes;
    private static Map<String, String> cachedDeclarationTypes;
    private static Map<String, String> cachedKeywords;
    private static Map<String, String> cachedKeywordParameters;
    private static Map<String, String> cachedOperationCodes;
    private static Map<String, String> cachedBuiltInFunctions;

    private Preferences() {
        // Singleton class
    }

    /**
     * Thread-safe method that returns the instance of this Singleton class.
     */
    public synchronized static Preferences getInstance() {
        if (instance == null) {
            instance = new Preferences();
            instance.preferenceStore = PreferenceStoreProvider.getPreferenceStore();
            instance.ibmPreferenceStore = PreferenceStoreProvider.getIbmPreferenceStore();
        }
        return instance;
    }

    /**
     * Resets the singleton instance. Call this when switching between test mode
     * and normal mode.
     */
    public static void resetInstance() {
        clearCache();
        instance = null;
    }

    private static void clearCache() {
        cachedDataTypes = null;
        cachedDeclarationTypes = null;
        cachedKeywords = null;
        cachedKeywordParameters = null;
        cachedOperationCodes = null;
        cachedBuiltInFunctions = null;
    }

    public String getStartColumnAsText() {

        String startColumnStr = Integer.toString(getStartColumn());

        return startColumnStr;
    }

    public int getStartColumn() {

        int startColumn = ibmPreferenceStore.getInt("RPGLE.FORMATTING.start");

        return startColumn;
    }

    public String getEndColumnAsText() {

        String endColumnStr = ibmPreferenceStore.getString("RPGLE.FORMATTING.end");
        if (!"*MAX".equals(endColumnStr)) {
            endColumnStr = Integer.toString(ibmPreferenceStore.getInt("RPGLE.FORMATTING.end"));
        }

        return endColumnStr;
    }

    public int getEndColumn() {

        String strLength = ibmPreferenceStore.getString("RPGLE.FORMATTING.end");
        if ("*MAX".equals(strLength)) {
            return Integer.MAX_VALUE;
        }

        if (StringUtils.isNullOrEmpty(strLength)) {
            return ibmPreferenceStore.getInt("RPGLE.FORMATTING.end");
        }

        return Integer.parseInt(strLength);
    }

    public void setMaxLineLength(int width) {
        ibmPreferenceStore.setValue("RPGLE.FORMATTING.end", width);
    }

    public int getIndent() {
        return ibmPreferenceStore.getInt("RPGLE.FORMATTING.length");
    }

    /**
     * Returns the extra blanks of WHEN and OTHER within a SELECT statement, see
     * IBM preference "Indent WHEN/OTHER within SELECT statement".
     */
    public int getSelectIndent() {
        return ibmPreferenceStore.getInt("RPGLE.FORMATTING.select");
    }

    /**
     * Returns <code>true</code>, if continued lines keep their indent relative
     * to the first line of the statement, see IBM preference "Preserve relative
     * indentation of continued lines". Otherwise continued lines are lined up.
     */
    public boolean isPreserveRelativeIndentOfContinuedLines() {
        return ibmPreferenceStore.getBoolean("RPGLE.FORMATTING.multiple");
    }

    /**
     * Returns <code>true</code>, if embedded SQL statements are formatted, see
     * IBM preference "Formatting" of the ILE RPG SQL formatting. Otherwise
     * only their casing is changed.
     */
    public boolean isFormatEmbeddedSql() {
        return ibmPreferenceStore.getBoolean("SQLRPGLE.FORMATTING.base");
    }

    /**
     * Returns <code>true</code>, if the casing of embedded SQL statements is
     * changed, see IBM preference "Automatic uppercasing" of the ILE RPG SQL
     * formatting.
     */
    public boolean isSqlUppercasing() {
        return ibmPreferenceStore.getBoolean("SQLRPGLE.UPPERCASING.base");
    }

    /**
     * Returns the number of blanks of each indent level of an embedded SQL
     * statement, see IBM preference "Indent" of the ILE RPG SQL formatting.
     */
    public int getSqlIndent() {
        return ibmPreferenceStore.getInt("SQLRPGLE.INDENT.S1_Blanks");
    }

    /**
     * Returns the line break at the commas of an embedded SQL statement, see
     * IBM preference "New line: On comma" of the ILE RPG SQL formatting.
     */
    public SqlBreakStyle getSqlBreakOnComma() {
        return SqlBreakStyle.fromIbmValue(ibmPreferenceStore.getInt("SQLRPGLE.FORMATTING.onComma"));
    }

    /**
     * Returns the line break at AND/OR of an embedded SQL statement, see IBM
     * preference "On AND/OR keywords" of the ILE RPG SQL formatting.
     */
    public SqlBreakStyle getSqlBreakOnAndOr() {
        return SqlBreakStyle.fromIbmValue(ibmPreferenceStore.getInt("SQLRPGLE.FORMATTING.onAndOr"));
    }

    /**
     * Returns the casing of the keywords of an embedded SQL statement, see IBM
     * preference "Automatic uppercasing" of the ILE RPG SQL formatting.
     */
    public SqlCasingStyle getSqlKeywordCasingStyle() {
        return getSqlCasingStyle("SQLRPGLE.UPPERCASING.keywords");
    }

    /**
     * Returns the casing of the identifiers of an embedded SQL statement, see
     * IBM preference "Automatic uppercasing" of the ILE RPG SQL formatting.
     */
    public SqlCasingStyle getSqlIdentifierCasingStyle() {
        return getSqlCasingStyle("SQLRPGLE.UPPERCASING.identifiers");
    }

    /**
     * Returns the casing of an embedded SQL statement like RDi: the style "All
     * uppercase" or "All lowercase" applies to the keywords and to the
     * identifiers, the style "Custom" uses the value of the given preference.
     */
    private SqlCasingStyle getSqlCasingStyle(String customKey) {

        if (!ibmPreferenceStore.getBoolean("SQLRPGLE.UPPERCASING.base")) {
            return SqlCasingStyle.IGNORE;
        }

        switch (ibmPreferenceStore.getInt("SQLRPGLE.UPPERCASING.base.value")) {
        case 0:
            return SqlCasingStyle.UPPER;
        case 1:
            return SqlCasingStyle.LOWER;
        case 2:
            return SqlCasingStyle.fromIbmValue(ibmPreferenceStore.getInt(customKey));
        default:
            return SqlCasingStyle.IGNORE;
        }
    }

    public KeywordCasingStyle getKeywordCasingStyle() {

        String ibmCasingStyle = ibmPreferenceStore
            .getString("com.ibm.etools.iseries.edit.preferences.parser.ilerpg.enter.autoclosecontrol.autoclosecontrolvalue");
        if ("ENDXX".equals(ibmCasingStyle)) {
            return KeywordCasingStyle.UPPERCASE;
        } else if ("EndXx".equals(ibmCasingStyle)) {
            return KeywordCasingStyle.UPPER_CAMEL;
        } else if ("Endxx".equals(ibmCasingStyle)) {
            return KeywordCasingStyle.UPPER_CAMEL;
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

    /**
     * Returns the canonical spellings of the operation codes by the operation
     * code in upper case.
     */
    public Map<String, String> getOperationCodes() {
        if (cachedOperationCodes == null) {
            String value = preferenceStore.getString(OPERATION_CODES);
            cachedOperationCodes = KeywordUtils.stringToKeywords(value);
        }
        return cachedOperationCodes;
    }

    public void setOperationCodes(Map<String, String> operationCodes) {
        String value = KeywordUtils.keywordsToString(operationCodes);
        preferenceStore.setValue(OPERATION_CODES, value);
        clearCache();
    }

    public Map<String, String> getDefaultOperationCodes() {
        return KeywordUtils.getDefaultOperationCodes();
    }

    /**
     * Returns the canonical spellings of the built-in functions by the
     * built-in function in upper case.
     */
    public Map<String, String> getBuiltInFunctions() {
        if (cachedBuiltInFunctions == null) {
            String value = preferenceStore.getString(BUILT_IN_FUNCTIONS);
            cachedBuiltInFunctions = KeywordUtils.stringToKeywords(value);
        }
        return cachedBuiltInFunctions;
    }

    public void setBuiltInFunctions(Map<String, String> builtInFunctions) {
        String value = KeywordUtils.keywordsToString(builtInFunctions);
        preferenceStore.setValue(BUILT_IN_FUNCTIONS, value);
        clearCache();
    }

    public Map<String, String> getDefaultBuiltInFunctions() {
        return KeywordUtils.getDefaultBuiltInFunctions();
    }

    /**
     * Returns <code>true</code>, if iRPGFormatter formats everything except
     * the calculations: the control options, the declarations and the
     * compiler directives.
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
     * The preference replaces "Execute IBM formatter". If that one was
     * switched off, the calculations stay unchanged.
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
        if (cachedDataTypes == null) {
            String value = preferenceStore.getString(DATA_TYPES);
            cachedDataTypes = KeywordUtils.stringToKeywords(value);
        }
        return cachedDataTypes;
    }

    public Map<String, String> getDeclarationTypes() {
        if (cachedDeclarationTypes == null) {
            String value = preferenceStore.getString(DECLARATION_TYPES);
            cachedDeclarationTypes = KeywordUtils.stringToKeywords(value);
        }
        return cachedDeclarationTypes;
    }

    public void setDataTypes(Map<String, String> dataTypes) {
        String value = KeywordUtils.keywordsToString(dataTypes);
        preferenceStore.setValue(DATA_TYPES, value);
        clearCache();
    }

    public void setDeclarationTypes(Map<String, String> declarationTypes) {
        String value = KeywordUtils.keywordsToString(declarationTypes);
        preferenceStore.setValue(DECLARATION_TYPES, value);
        clearCache();
    }

    public Map<String, String> getKeywords() {
        if (cachedKeywords == null) {
            String value = preferenceStore.getString(KEYWORDS);
            cachedKeywords = KeywordUtils.stringToKeywords(value);
        }
        return cachedKeywords;
    }

    public void setKeywords(Map<String, String> keywords) {
        String value = KeywordUtils.keywordsToString(keywords);
        preferenceStore.setValue(KEYWORDS, value);
        clearCache();
    }

    public Map<String, String> getSpecialWords() {
        if (cachedKeywordParameters == null) {
            String value = preferenceStore.getString(SPECIAL_WORDS);
            cachedKeywordParameters = KeywordUtils.stringToKeywords(value);
        }
        return cachedKeywordParameters;
    }

    public void setSpecialWords(Map<String, String> keywordParameters) {
        String value = KeywordUtils.keywordsToString(keywordParameters);
        preferenceStore.setValue(SPECIAL_WORDS, value);
        clearCache();
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
            ibmPreferenceStore.setDefault("RPGLE.FORMATTING.length", 2);
            ibmPreferenceStore.setDefault("RPGLE.FORMATTING.start", 1);
            ibmPreferenceStore.setDefault("RPGLE.FORMATTING.end", "*MAX");
            ibmPreferenceStore.setDefault("RPGLE.FORMATTING.select", 0);
            ibmPreferenceStore.setDefault("RPGLE.FORMATTING.multiple", true);

            ibmPreferenceStore.setDefault("SQLRPGLE.FORMATTING.base", false);
            ibmPreferenceStore.setDefault("SQLRPGLE.INDENT.S1_Blanks", 2);
            ibmPreferenceStore.setDefault("SQLRPGLE.FORMATTING.onComma", 0);
            ibmPreferenceStore.setDefault("SQLRPGLE.FORMATTING.onAndOr", 0);
            ibmPreferenceStore.setDefault("SQLRPGLE.UPPERCASING.base", false);
            ibmPreferenceStore.setDefault("SQLRPGLE.UPPERCASING.base.value", 0);
            ibmPreferenceStore.setDefault("SQLRPGLE.UPPERCASING.keywords", 0);
            ibmPreferenceStore.setDefault("SQLRPGLE.UPPERCASING.identifiers", 0);

            ibmPreferenceStore.setDefault("com.ibm.etools.iseries.edit.preferences.parser.ilerpg.enter.autoclosecontrol.autoclosecontrolvalue",
                "endxx");
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
