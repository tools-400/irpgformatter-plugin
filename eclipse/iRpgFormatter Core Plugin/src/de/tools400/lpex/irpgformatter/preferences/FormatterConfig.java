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

    private boolean formatDeclarations;
    private CalculationFormatter calculationFormatter;
    private int indent;
    private int selectIndent;
    private boolean preserveRelativeIndentOfContinuedLines;
    private boolean formatEmbeddedSql;
    private int sqlIndent;
    private SqlBreakStyle sqlBreakOnComma;
    private SqlBreakStyle sqlBreakOnAndOr;
    private SqlCasingStyle sqlKeywordCasingStyle;
    private SqlCasingStyle sqlIdentifierCasingStyle;
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
    private boolean removeEmptyCommentLines;
    private boolean removeEmptyLinesBeforeDclPi;
    private ConditionParentheses conditionParentheses;
    private boolean formatOperationCodesAndBuiltInFunctions;
    private KeywordCasingStyle keywordCasingStyle;
    private ParameterSpacingStyle parameterSpacingStyle;
    private Map<String, String> keywords;
    private Map<String, String> dataTypes;
    private Map<String, String> declarationTypes;
    private Map<String, String> specialWords;
    private Map<String, String> operationCodes;
    private Map<String, String> builtInFunctions;

    private FormatterConfig() {
    }

    public boolean isFormatDeclarations() {
        return formatDeclarations;
    }

    public CalculationFormatter getCalculationFormatter() {
        return calculationFormatter;
    }

    public int getIndent() {
        return indent;
    }

    public int getSelectIndent() {
        return selectIndent;
    }

    public boolean isPreserveRelativeIndentOfContinuedLines() {
        return preserveRelativeIndentOfContinuedLines;
    }

    public boolean isFormatEmbeddedSql() {
        return formatEmbeddedSql;
    }

    public int getSqlIndent() {
        return sqlIndent;
    }

    public SqlBreakStyle getSqlBreakOnComma() {
        return sqlBreakOnComma;
    }

    public SqlBreakStyle getSqlBreakOnAndOr() {
        return sqlBreakOnAndOr;
    }

    public SqlCasingStyle getSqlKeywordCasingStyle() {
        return sqlKeywordCasingStyle;
    }

    public SqlCasingStyle getSqlIdentifierCasingStyle() {
        return sqlIdentifierCasingStyle;
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

    public boolean isRemoveEmptyCommentLines() {
        return removeEmptyCommentLines;
    }

    public boolean isRemoveEmptyLinesBeforeDclPi() {
        return removeEmptyLinesBeforeDclPi;
    }

    public ConditionParentheses getConditionParentheses() {
        return conditionParentheses;
    }

    public boolean isFormatOperationCodesAndBuiltInFunctions() {
        return formatOperationCodesAndBuiltInFunctions;
    }

    public Map<String, String> getOperationCodes() {
        return operationCodes;
    }

    public Map<String, String> getBuiltInFunctions() {
        return builtInFunctions;
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

    public void setFormatDeclarations(boolean formatDeclarations) {
        this.formatDeclarations = formatDeclarations;
    }

    public void setCalculationFormatter(CalculationFormatter calculationFormatter) {
        this.calculationFormatter = calculationFormatter;
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public void setSelectIndent(int selectIndent) {
        this.selectIndent = selectIndent;
    }

    public void setPreserveRelativeIndentOfContinuedLines(boolean preserveRelativeIndentOfContinuedLines) {
        this.preserveRelativeIndentOfContinuedLines = preserveRelativeIndentOfContinuedLines;
    }

    public void setFormatEmbeddedSql(boolean formatEmbeddedSql) {
        this.formatEmbeddedSql = formatEmbeddedSql;
    }

    public void setSqlIndent(int sqlIndent) {
        this.sqlIndent = sqlIndent;
    }

    public void setSqlBreakOnComma(SqlBreakStyle sqlBreakOnComma) {
        this.sqlBreakOnComma = sqlBreakOnComma;
    }

    public void setSqlBreakOnAndOr(SqlBreakStyle sqlBreakOnAndOr) {
        this.sqlBreakOnAndOr = sqlBreakOnAndOr;
    }

    public void setSqlKeywordCasingStyle(SqlCasingStyle sqlKeywordCasingStyle) {
        this.sqlKeywordCasingStyle = sqlKeywordCasingStyle;
    }

    public void setSqlIdentifierCasingStyle(SqlCasingStyle sqlIdentifierCasingStyle) {
        this.sqlIdentifierCasingStyle = sqlIdentifierCasingStyle;
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

    public void setRemoveEmptyCommentLines(boolean removeEmptyCommentLines) {
        this.removeEmptyCommentLines = removeEmptyCommentLines;
    }

    public void setRemoveEmptyLinesBeforeDclPi(boolean removeEmptyLinesBeforeDclPi) {
        this.removeEmptyLinesBeforeDclPi = removeEmptyLinesBeforeDclPi;
    }

    public void setConditionParentheses(ConditionParentheses conditionParentheses) {
        this.conditionParentheses = conditionParentheses;
    }

    public void setFormatOperationCodesAndBuiltInFunctions(boolean formatOperationCodesAndBuiltInFunctions) {
        this.formatOperationCodesAndBuiltInFunctions = formatOperationCodesAndBuiltInFunctions;
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
        config.formatDeclarations = prefs.isFormatDeclarations();
        config.calculationFormatter = prefs.getFormatCalculations();
        config.indent = prefs.getIndent();
        config.selectIndent = prefs.getSelectIndent();
        config.preserveRelativeIndentOfContinuedLines = prefs.isPreserveRelativeIndentOfContinuedLines();
        config.formatEmbeddedSql = prefs.isFormatEmbeddedSql();
        config.sqlIndent = prefs.getSqlIndent();
        config.sqlBreakOnComma = prefs.getSqlBreakOnComma();
        config.sqlBreakOnAndOr = prefs.getSqlBreakOnAndOr();
        config.sqlKeywordCasingStyle = prefs.getSqlKeywordCasingStyle();
        config.sqlIdentifierCasingStyle = prefs.getSqlIdentifierCasingStyle();
        config.maxLineWidth = prefs.getEndColumn();
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
        config.removeEmptyCommentLines = prefs.isRemoveEmptyCommentLines();
        config.removeEmptyLinesBeforeDclPi = prefs.isRemoveEmptyLinesBeforeDclPi();
        config.conditionParentheses = prefs.getConditionParentheses();
        config.formatOperationCodesAndBuiltInFunctions = prefs.isFormatOperationCodesAndBuiltInFunctions();
        config.keywordCasingStyle = prefs.getKeywordCasingStyle();
        config.parameterSpacingStyle = prefs.getParameterSpacingStyle();
        config.keywords = Collections.unmodifiableMap(prefs.getKeywords());
        config.dataTypes = Collections.unmodifiableMap(prefs.getDataTypes());
        config.declarationTypes = Collections.unmodifiableMap(prefs.getDeclarationTypes());
        config.specialWords = Collections.unmodifiableMap(prefs.getSpecialWords());
        config.operationCodes = Collections.unmodifiableMap(prefs.getOperationCodes());
        config.builtInFunctions = Collections.unmodifiableMap(prefs.getBuiltInFunctions());

        return config;
    }
}
