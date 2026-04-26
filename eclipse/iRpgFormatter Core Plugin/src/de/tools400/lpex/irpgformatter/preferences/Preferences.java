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

    private static final String KEYWORD_CASING_STYLE = "keywordCasingStyle";
    private static final String PARAMETER_SPACING_STYLE = "parameterSpacingStyle";
    private static final String USE_CONST_KEYWORD = "useConstKeyword";
    private static final String DELIMITER_BEFORE_PARAMETER = "delimiterBeforeParameter";
    private static final String ALIGN_SUB_FIELDS = "alignSubFields";
    private static final String BREAK_BETWEEN_CASE_CHANGE = "breakBetweenCaseChange";
    private static final String BREAK_BEFORE_KEYWORD = "breakBeforeKeyword";
    private static final String SORT_CONST_VALUE_TO_END = "sortConstValueToEnd";
    private static final String MAX_NAME_LENGTH = "maxNameLength";
    private static final String MIN_NAME_LENGTH = "minNameLength";
    private static final String FORMATTER_PREVIEW_VERTICAL_RULER_COLUMN = "formatterPreviewVertialRulerColumn";

    private static final String EXECUTE_IBM_FORMATTER = "executeIbmFormatter";
    private static final String EXECUTE_IRPG_FORMATTER = "executeIrpgFormatter";
    private static final String FORMAT_ON_SAVE = "formatOnSave";

    private static final String DATA_TYPES = "dataTypes";
    private static final String DECLARATION_TYPES = "declarationTypes";
    private static final String KEYWORDS = "keywords";
    private static final String SPECIAL_WORDS = "keywordParameters";
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
    private static final int DEFAULT_MAX_NAME_LENGTH = 60;
    private static final int DEFAULT_MIN_NAME_LENGTH = 10;
    private static final int DEFAULT_FORMATTER_PREVIEW_VERTICAL_RULER_COLUMN = 35;
    private static final String DEFAULT_PLUGIN_FORMATTER_SOURCE = "/default_preview_source.rpgle";
    private static final String DEFAULT_CUSTOM_FORMATTER_SOURCE = "";
    private static final boolean DEFAULT_CUSTOM_PREVIEW_CONTENT = false;

    private static final boolean DEFAULT_EXECUTE_IBM_FORMATTER = true;
    private static final boolean DEFAULT_EXECUTE_IRPG_FORMATTER = true;
    private static final boolean DEFAULT_FORMAT_ON_SAVE = false;

    private static Map<String, String> cachedDataTypes;
    private static Map<String, String> cachedDeclarationTypes;
    private static Map<String, String> cachedKeywords;
    private static Map<String, String> cachedKeywordParameters;

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

    public int getEndColumn(int currentSourceLength) {

        int length;

        String strLength = ibmPreferenceStore.getString("RPGLE.FORMATTING.end");
        if ("*MAX".equals(strLength)) {
            return currentSourceLength;
        }

        if (StringUtils.isNullOrEmpty(strLength)) {
            length = ibmPreferenceStore.getInt("RPGLE.FORMATTING.end");
        } else {
            length = Integer.parseInt(strLength);
        }

        length = Math.min(length, currentSourceLength);

        return length;
    }

    public int getMaxLineWidth() {

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

    public boolean isExecuteIbmFormatter() {
        return preferenceStore.getBoolean(EXECUTE_IBM_FORMATTER);
    }

    public void setExecuteIbmFormatter(boolean enabled) {
        preferenceStore.setValue(EXECUTE_IBM_FORMATTER, enabled);
    }

    public boolean isExecuteIrpgFormatter() {
        return preferenceStore.getBoolean(EXECUTE_IRPG_FORMATTER);
    }

    public void setExecuteIrpgFormatter(boolean enabled) {
        preferenceStore.setValue(EXECUTE_IRPG_FORMATTER, enabled);
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

    public void setUserKeyActions(String userKeyActions) {
        preferenceStore.setValue(USER_KEY_ACTIONS, userKeyActions);
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
        preferenceStore.setDefault(KEYWORD_CASING_STYLE, getDefaultKeywordCasingStyle().name());
        preferenceStore.setDefault(PARAMETER_SPACING_STYLE, getDefaultParameterSpacingStyle().name());
        preferenceStore.setDefault(USE_CONST_KEYWORD, getDefaultUseConstKeyword());
        preferenceStore.setDefault(DELIMITER_BEFORE_PARAMETER, getDefaultDelimiterBeforeParameter());
        preferenceStore.setDefault(ALIGN_SUB_FIELDS, getDefaultAlignSubFields());
        preferenceStore.setDefault(BREAK_BETWEEN_CASE_CHANGE, getDefaultBreakBetweenCaseChange());
        preferenceStore.setDefault(BREAK_BEFORE_KEYWORD, getDefaultBreakBeforeKeyword());
        preferenceStore.setDefault(SORT_CONST_VALUE_TO_END, getDefaultSortConstValueToEnd());
        preferenceStore.setDefault(EXECUTE_IBM_FORMATTER, getDefaultExecuteIbmFormatter());
        preferenceStore.setDefault(EXECUTE_IRPG_FORMATTER, getDefaultExecuteIrpgFormatter());
        preferenceStore.setDefault(FORMAT_ON_SAVE, getDefaultFormatOnSave());
        preferenceStore.setDefault(USER_KEY_ACTIONS, getDefaultUserKeyActions());
        preferenceStore.setDefault(DATA_TYPES, getDefaultDataTypesAsString());
        preferenceStore.setDefault(DECLARATION_TYPES, getDefaultDeclarationTypesAsString());
        preferenceStore.setDefault(KEYWORDS, getDefaultKeywordsAsString());
        preferenceStore.setDefault(SPECIAL_WORDS, getDefaultSpecialWordsAsString());
        preferenceStore.setDefault(MAX_NAME_LENGTH, getDefaultMaxNameLength());
        preferenceStore.setDefault(MIN_NAME_LENGTH, getDefaultMinLiteralLength());
        preferenceStore.setDefault(DEFAULT_FORMATTER_SOURCE, getDefaultPluginFormatterSource());
        preferenceStore.setDefault(CUSTOM_FORMATTER_SOURCE, getDefaultCustomFormatterSource());
        preferenceStore.setDefault(CUSTOM_PREVIEW_CONTENT, getDefaultCustomPreviewContent());
        preferenceStore.setDefault(FORMATTER_PREVIEW_VERTICAL_RULER_COLUMN, getDefaultFormatterPreviewVerticalRulerColumn());

        ibmPreferenceStore.setDefault("RPGLE.FORMATTING.length", 2);
        ibmPreferenceStore.setDefault("RPGLE.FORMATTING.start", 1);
        ibmPreferenceStore.setDefault("RPGLE.FORMATTING.end", "*MAX");

        ibmPreferenceStore.setDefault("com.ibm.etools.iseries.edit.preferences.parser.ilerpg.enter.autoclosecontrol.autoclosecontrolvalue", "endxx");
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

    public boolean getDefaultExecuteIbmFormatter() {
        return DEFAULT_EXECUTE_IBM_FORMATTER;
    }

    public boolean getDefaultExecuteIrpgFormatter() {
        return DEFAULT_EXECUTE_IRPG_FORMATTER;
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
