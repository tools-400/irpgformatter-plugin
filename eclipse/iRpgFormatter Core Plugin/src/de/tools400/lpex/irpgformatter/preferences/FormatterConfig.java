/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferences;

import java.util.Collections;
import java.util.Map;

/**
 * Configuration snapshot of all formatting-relevant settings.
 * <p>
 * Decouples the formatter from the {@link Preferences} singleton so that
 * formatting can be driven by any source of configuration values (e.g. live UI
 * widgets in the preference page).
 * </p>
 */
public class FormatterConfig {

    private int indent;
    private int maxLineWidth;
    private int maxNameLength;
    private int minLiteralLength;
    private boolean alignSubFields;
    private boolean breakBeforeKeyword;
    private boolean delimiterBeforeParameter;
    private boolean breakBetweenCaseChange;
    private boolean useConstKeyword;
    private boolean sortConstValueToEnd;
    private boolean replacePiName;
    private boolean removeEndProcName;
    private boolean unindentCompilerDirectives;
    private KeywordCasingStyle keywordCasingStyle;
    private ParameterSpacingStyle parameterSpacingStyle;
    private Map<String, String> keywords;
    private Map<String, String> dataTypes;
    private Map<String, String> declarationTypes;
    private Map<String, String> specialWords;

    private FormatterConfig() {
    }

    public int getIndent() {
        return indent;
    }

    public int getMaxLineWidth() {
        return maxLineWidth;
    }

    public int getMaxNameLength() {
        return maxNameLength;
    }

    public int getMinLiteralLength() {
        return minLiteralLength;
    }

    public boolean isAlignSubFields() {
        return alignSubFields;
    }

    public boolean isBreakBeforeKeyword() {
        return breakBeforeKeyword;
    }

    public boolean isDelimiterBeforeParameter() {
        return delimiterBeforeParameter;
    }

    public boolean isBreakBetweenCaseChange() {
        return breakBetweenCaseChange;
    }

    public boolean isUseConstKeyword() {
        return useConstKeyword;
    }

    public boolean isSortConstValueToEnd() {
        return sortConstValueToEnd;
    }

    public boolean isReplacePiName() {
        return replacePiName;
    }

    public boolean isRemoveEndProcName() {
        return removeEndProcName;
    }

    public boolean isUnindentCompilerDirectives() {
        return unindentCompilerDirectives;
    }

    public KeywordCasingStyle getKeywordCasingStyle() {
        return keywordCasingStyle;
    }

    public Map<String, String> getKeywords() {
        return keywords;
    }

    public Map<String, String> getDataTypes() {
        return dataTypes;
    }

    public Map<String, String> getDeclarationTypes() {
        return declarationTypes;
    }

    public Map<String, String> getSpecialWords() {
        return specialWords;
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public void setMaxLineWidth(int maxLineWidth) {
        this.maxLineWidth = maxLineWidth;
    }

    public void setMaxNameLength(int maxNameLength) {
        this.maxNameLength = maxNameLength;
    }

    public void setMinLiteralLength(int minLiteralLength) {
        this.minLiteralLength = minLiteralLength;
    }

    public void setAlignSubFields(boolean alignSubFields) {
        this.alignSubFields = alignSubFields;
    }

    public void setBreakBeforeKeyword(boolean breakBeforeKeyword) {
        this.breakBeforeKeyword = breakBeforeKeyword;
    }

    public void setDelimiterBeforeParameter(boolean delimiterBeforeParameter) {
        this.delimiterBeforeParameter = delimiterBeforeParameter;
    }

    public void setBreakBetweenCaseChange(boolean breakBetweenCaseChange) {
        this.breakBetweenCaseChange = breakBetweenCaseChange;
    }

    public void setUseConstKeyword(boolean useConstKeyword) {
        this.useConstKeyword = useConstKeyword;
    }

    public void setSortConstValueToEnd(boolean sortConstValueToEnd) {
        this.sortConstValueToEnd = sortConstValueToEnd;
    }

    public void setReplacePiName(boolean replacePiName) {
        this.replacePiName = replacePiName;
    }

    public void setRemoveEndProcName(boolean removeEndProcName) {
        this.removeEndProcName = removeEndProcName;
    }

    public void setUnindentCompilerDirectives(boolean unindentCompilerDirectives) {
        this.unindentCompilerDirectives = unindentCompilerDirectives;
    }

    public void setKeywordCasingStyle(KeywordCasingStyle keywordCasingStyle) {
        this.keywordCasingStyle = keywordCasingStyle;
    }

    public ParameterSpacingStyle getParameterSpacingStyle() {
        return parameterSpacingStyle;
    }

    public void setParameterSpacingStyle(ParameterSpacingStyle parameterSpacingStyle) {
        this.parameterSpacingStyle = parameterSpacingStyle;
    }

    public void setKeywords(Map<String, String> keywords) {
        this.keywords = Collections.unmodifiableMap(keywords);
    }

    public void setDataTypes(Map<String, String> dataTypes) {
        this.dataTypes = Collections.unmodifiableMap(dataTypes);
    }

    public void setDeclarationTypes(Map<String, String> declarationTypes) {
        this.declarationTypes = Collections.unmodifiableMap(declarationTypes);
    }

    public void setSpecialWords(Map<String, String> specialWords) {
        this.specialWords = Collections.unmodifiableMap(specialWords);
    }

    public int getEndColumn(int sourceLength) {
        return Math.min(maxLineWidth, sourceLength);
    }

    /**
     * Creates a {@link FormatterConfig} from the currently saved
     * {@link Preferences}.
     */
    public static FormatterConfig fromPreferences() {

        Preferences prefs = Preferences.getInstance();

        FormatterConfig config = new FormatterConfig();
        config.indent = prefs.getIndent();
        config.maxLineWidth = prefs.getMaxLineWidth();
        config.maxNameLength = prefs.getMaxNameLength();
        config.minLiteralLength = prefs.getMinNameLength();
        config.alignSubFields = prefs.isAlignSubFields();
        config.breakBeforeKeyword = prefs.isBreakBeforeKeyword();
        config.delimiterBeforeParameter = prefs.isDelimiterBeforeParameter();
        config.breakBetweenCaseChange = prefs.isBreakBetweenCaseChange();
        config.useConstKeyword = prefs.isUseConstKeyword();
        config.sortConstValueToEnd = prefs.isSortConstValueToEnd();
        config.replacePiName = prefs.isReplacePiName();
        config.removeEndProcName = prefs.isRemoveEndProcName();
        config.unindentCompilerDirectives = prefs.isUnindentCompilerDirectives();
        config.keywordCasingStyle = prefs.getKeywordCasingStyle();
        config.parameterSpacingStyle = prefs.getParameterSpacingStyle();
        config.keywords = Collections.unmodifiableMap(prefs.getKeywords());
        config.dataTypes = Collections.unmodifiableMap(prefs.getDataTypes());
        config.declarationTypes = Collections.unmodifiableMap(prefs.getDeclarationTypes());
        config.specialWords = Collections.unmodifiableMap(prefs.getSpecialWords());

        return config;
    }
}
