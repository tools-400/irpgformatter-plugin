/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.parser.StatementType;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.FormattingRules;
import de.tools400.lpex.irpgformatter.rules.casing.FormatSpecialWordsRule;
import de.tools400.lpex.irpgformatter.rules.statements.FormatEndProcNameRule;
import de.tools400.lpex.irpgformatter.rules.statements.FormatConstKeywordRule;
import de.tools400.lpex.irpgformatter.rules.statements.FormatPiNameRule;
import de.tools400.lpex.irpgformatter.statement.CollectedStatement;
import de.tools400.lpex.irpgformatter.statement.ContinuationHandler;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;
import de.tools400.lpex.irpgformatter.tokenizer.Tokenizer;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

/**
 * RPGLE source code formatter.
 * <p>
 * Formats control specifications, definition specifications, and compiler
 * directives according to the formatting rules defined in RPGLE-Formatter.md.
 * </p>
 */
public class RpgleFormatter {

    private static final String SOURCE_TYPE_RPGLE = "RPGLE";
    private static final String SOURCE_TYPE_SQLRPGLE = "SQLRPGLE";

    private static final Pattern FORMATTER_DIRECTIVE_PATTERN = Pattern.compile("^\\s*//\\s*@formatter:(on|off)\\s*$", Pattern.CASE_INSENSITIVE);

    private static final Set<String> SUPPORTED_SOURCE_TYPES;
    static {
        Set<String> types = new HashSet<>();
        types.add(SOURCE_TYPE_RPGLE);
        types.add(SOURCE_TYPE_SQLRPGLE);
        SUPPORTED_SOURCE_TYPES = Collections.unmodifiableSet(types);
    }

    private final FormatterConfig config;
    private final FormatterUtils formatterUtils;
    private final Tokenizer tokenizer;

    private int errorCount;
    private int endColumn;

    public RpgleFormatter(FormatterConfig config) {
        this.config = config;
        this.endColumn = config.getEndColumn(100);
        this.formatterUtils = new FormatterUtils(config);
        this.tokenizer = new Tokenizer(config);
    }

    public RpgleFormatter() {
        this(FormatterConfig.fromPreferences());
    }

    public static boolean isSupportedSourceType(String sourceType) {
        return sourceType != null && SUPPORTED_SOURCE_TYPES.contains(sourceType.toUpperCase());
    }

    public static String validateInput(IRpgleInput input) throws Exception {

        String sourceType = input.getSourceType();
        if (!isSupportedSourceType(sourceType)) {
            return Messages.bind(Messages.Error_Unsupported_source_type_A, sourceType);
        }

        if (!input.isFreeFormat()) {
            return Messages.Error_Not_free_format;
        }
        return null;
    }

    public FormatterConfig getConfig() {
        return config;
    }

    public void setSourceLength(int sourceLength) {
        this.endColumn = config.getEndColumn(sourceLength);
    }

    /**
     * Formats the RPGLE source from the given input.
     *
     * @param input - the input source
     * @return formatted result containing statement-level mapping
     * @throws RpgleFormatterException if formatting fails
     */
    public FormattedResult format(IRpgleInput input, int indent) throws RpgleFormatterException {

        if (!input.isFreeFormat()) {
            IRpgleFormatterPlugin.logError(Messages.Error_Not_free_format, null);
            String[] sourceLines = input.getSourceLines();
            return new FormattedResult(
                new FormattedStatement[] { new FormattedStatement(input.getStartLineNumber(), sourceLines.length, sourceLines) });
        }

        errorCount = 0;

        String[] sourceLines = input.getSourceLines();
        List<FormattedStatement> results = new ArrayList<>();

        // Step 1: Collect multi-line statements
        int startLineNumber = input.getStartLineNumber();
        CollectedStatement[] statements = ContinuationHandler.collectStatements(sourceLines, startLineNumber);

        // Step 2: Process each statement
        boolean formatterDisabled = false;
        for (CollectedStatement statement : statements) {

            // Format the statement
            StatementType type = statement.getType();

            // Check for formatter directive
            int directive = getFormatterDirective(statement, type);
            if (directive != 0) {
                results.add(new FormattedStatement(statement.getStartLineNumber(), statement.numLines(),
                    statement.getOriginalStatements().toArray(new String[0])));
                formatterDisabled = (directive == -1);
                continue;
            }

            // If formatter is disabled, output original lines unchanged
            if (formatterDisabled) {
                results.add(new FormattedStatement(statement.getStartLineNumber(), statement.numLines(),
                    statement.getOriginalStatements().toArray(new String[0])));
                continue;
            }

            List<String> stmtOutput = new ArrayList<>();
            if (type == StatementType.OTHER) {
                stmtOutput.addAll(statement.getOriginalStatements());
            } else {
                for (String embeddedComment : statement.getEmbeddedComments()) {
                    stmtOutput.add(embeddedComment);
                }
                try {
                    stmtOutput.addAll(formatLine(statement, type, indent));
                } catch (Exception e) {
                    if (e instanceof LineOverflowException) {
                        ((LineOverflowException)e).setLineNumbers(statement.getStartLineNumber(), statement.getEndLineNumber());
                    } else {
                        IRpgleFormatterPlugin.logError("Unexpected formatting error.", e);
                    }
                    stmtOutput.addAll(statement.getOriginalStatements());
                    errorCount++;
                }
            }
            results.add(new FormattedStatement(statement.getStartLineNumber(), statement.numLines(), stmtOutput.toArray(new String[0])));
        }

        return new FormattedResult(results.toArray(new FormattedStatement[0]));
    }

    public int getErrorCount() {
        return errorCount;
    }

    /**
     * Checks whether a COMMENT-type statement is a formatter directive.
     *
     * @return {@code -1} for {@code @formatter:off}, {@code +1} for
     *         {@code @formatter:on}, {@code 0} if not a directive
     */
    private int getFormatterDirective(CollectedStatement statement, StatementType type) {
        if (type != StatementType.COMMENT) {
            return 0;
        }
        Matcher matcher = FORMATTER_DIRECTIVE_PATTERN.matcher(statement.getStatement());
        if (matcher.matches()) {
            return "off".equalsIgnoreCase(matcher.group(1)) ? -1 : 1;
        }
        return 0;
    }

    /**
     * Formats a single line based on its statement type.
     */
    private List<String> formatLine(CollectedStatement statement, StatementType type, int defaultIndent) throws RpgleFormatterException {
        List<String> result = new ArrayList<>();

        int indent = statement.getIndentLevel() * config.getIndent() + defaultIndent;

        switch (type) {
        case BLANK:
            // Add blank line without indent
            result.add("");
            break;
        case FREE_DIRECTIVE:
            // Add **free without indent, but case formatted
            String freeSpecialWord = statement.getStatement();
            freeSpecialWord = FormattingRules.applyFormattingRule(freeSpecialWord,
                new FormatSpecialWordsRule(config.getSpecialWords(), config.getKeywordCasingStyle()));
            result.add(freeSpecialWord);
            break;
        case COMMENT:
            // Format with indent
            result.addAll(formatComment(statement, indent));
            break;
        case COMPILER_DIRECTIVE:
            // Format with indent
            result.addAll(formatCompilerDirective(statement, indent));
            break;
        case CTL_OPT:
            // Format with indent
            result.addAll(formatCtlOpt(statement, indent));
            break;
        case DCL_C:
            // Format with indent
            result.addAll(formatDclC(statement, indent));
            break;
        case DCL_DS:
        case DCL_PR:
        case DCL_PI:
        case DCL_ENUM:
        case DCL_PROC:
            // Format with indent
            result.addAll(formatDclBlock(statement, type, indent));
            break;
        case DCL_F:
        case DCL_S:
            // Format with indent
            result.addAll(formatDclStatement(statement, type, indent));
            break;
        case DCL_SUBF:
            // Format with indent
            result.addAll(formatSubField(statement, indent));
            break;
        case END_DS:
        case END_PR:
        case END_PI:
        case END_ENUM:
        case END_PROC:
            // Format with indent
            result.addAll(formatEndStatement(statement, type, indent));
            break;
        case OTHER:
        default:
            // Format with indent
            result.addAll(formatKeepOriginal(statement));
            break;
        }

        return result;
    }

    /**
     * Returns the line unchanged.
     */
    private List<String> formatKeepOriginal(CollectedStatement statement) {

        List<String> lines = statement.getOriginalStatements();

        return lines;
    }

    /**
     * Formats a line comment.
     */
    private List<String> formatComment(CollectedStatement statement, int indent) {

        List<String> result = new ArrayList<>();

        String formatted = statement.getStatement().trim();
        result.add(StringUtils.spaces(indent) + formatted);

        return result;
    }

    /**
     * Formats a compiler directive.
     */
    private List<String> formatCompilerDirective(CollectedStatement statement, int indent) {

        List<String> result = new ArrayList<>();

        String formatted = formatterUtils.getFormattingRules().formatCompilerDirective(statement.getStatement());
        if (config.isUnindentCompilerDirectives()) {
            indent = 0;
        }
        result.add(StringUtils.spaces(indent) + formatted);

        return result;
    }

    /**
     * Formats a ctl-opt statement.
     */
    private List<String> formatCtlOpt(CollectedStatement statement, int indent) throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(statement.getStatement(), StatementType.CTL_OPT);
        String[] results = formatterUtils.formatTokens("", tokens, indent, endColumn);

        return Arrays.asList(results);
    }

    /**
     * Formats a dcl-c (constant) statement.
     *
     * @throws RpgleFormatterException
     */
    private List<String> formatDclC(CollectedStatement statement, int indent) throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(statement.getStatement(), StatementType.DCL_C);
        tokens = new FormatConstKeywordRule(config).apply(tokens);
        String[] results = formatterUtils.formatTokens("", tokens, indent, endColumn);

        return Arrays.asList(results);
    }

    /**
     * Formats dcl-ds, dcl-pr, dcl-pi statements.
     */
    private List<String> formatDclBlock(CollectedStatement statement, StatementType type, int indent) throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(statement.getStatement(), type);
        if (type == StatementType.DCL_PI) {
            CollectedStatement parent = statement.getParent();
            String procName = null;
            if (parent != null && parent.getType() == StatementType.DCL_PROC) {
                procName = getProcName(parent);
            }
            tokens = new FormatPiNameRule(procName, config.isReplacePiName()).apply(tokens);
        }
        String[] results = formatterUtils.formatTokens("", tokens, indent, endColumn);

        return Arrays.asList(results);
    }

    /**
     * Extracts the procedure name (NAME token) from a DCL-PROC statement.
     */
    private String getProcName(CollectedStatement procStatement) throws RpgleFormatterException {
        IToken[] procTokens = tokenizer.tokenize(procStatement.getStatement(), StatementType.DCL_PROC);
        if (procTokens.length >= 2 && procTokens[1].getType() == TokenType.NAME) {
            return procTokens[1].getValue();
        }
        return null;
    }

    /**
     * Formats dcl-f, dcl-s statements.
     */
    private List<String> formatDclStatement(CollectedStatement statement, StatementType type, int indent) throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(statement.getStatement(), type);
        String[] results = formatterUtils.formatTokens("", tokens, indent, endColumn);

        return Arrays.asList(results);
    }

    /**
     * Formats a sub-statement (parameter, subfield).
     */
    private List<String> formatSubField(CollectedStatement statement, int indent) throws RpgleFormatterException {

        int subFieldAlignCol = formatterUtils.getSubFieldAlignColumn(statement, endColumn);

        IToken[] tokens = tokenizer.tokenize(statement.getStatement(), StatementType.DCL_SUBF);
        tokens = formatterUtils.sortConstValueToEnd(tokens);
        String[] results = formatterUtils.formatTokens("", tokens, indent, endColumn, subFieldAlignCol);

        return Arrays.asList(results);
    }

    /**
     * Formats an end statement (end-ds, end-pr, end-pi, end-proc).
     *
     * @throws RpgleFormatterException
     */
    private List<String> formatEndStatement(CollectedStatement statement, StatementType type, int indent) throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(statement.getStatement(), type);
        if (type == StatementType.END_PROC) {
            CollectedStatement procStmt = statement.getMatchingDclProc();
            String procName = (procStmt != null) ? getProcName(procStmt) : null;
            tokens = new FormatEndProcNameRule(procName, config.isRemoveEndProcName()).apply(tokens);
        }
        String[] results = formatterUtils.formatTokens("", tokens, indent, endColumn);

        return Arrays.asList(results);
    }
}
