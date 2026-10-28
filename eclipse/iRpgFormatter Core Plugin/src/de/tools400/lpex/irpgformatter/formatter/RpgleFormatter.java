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
import de.tools400.lpex.irpgformatter.parser.DirectiveType;
import de.tools400.lpex.irpgformatter.parser.StatementType;
import de.tools400.lpex.irpgformatter.preferences.CalculationFormatter;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.FormattingRules;
import de.tools400.lpex.irpgformatter.rules.casing.FormatSpecialWordsRule;
import de.tools400.lpex.irpgformatter.rules.statements.FormatConstKeywordRule;
import de.tools400.lpex.irpgformatter.rules.statements.FormatEndProcNameRule;
import de.tools400.lpex.irpgformatter.rules.statements.FormatPiNameRule;
import de.tools400.lpex.irpgformatter.rules.statements.RemoveEmptyCommentLinesRule;
import de.tools400.lpex.irpgformatter.rules.statements.RemoveEmptyLinesBeforeDclPiRule;
import de.tools400.lpex.irpgformatter.rules.statements.SortConstValueToEndRule;
import de.tools400.lpex.irpgformatter.statement.CollectedStatement;
import de.tools400.lpex.irpgformatter.statement.ContinuationHandler;
import de.tools400.lpex.irpgformatter.statement.EmbeddedDirective;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;
import de.tools400.lpex.irpgformatter.tokenizer.Tokenizer;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

/**
 * RPGLE source code formatter.
 * <p>
 * Formats the control options, the declarations and the compiler directives,
 * see preference "Format declarations", as well as the calculations, the
 * comment lines in front of them and the embedded SQL statements, see
 * preference "Format calculations". The formatting rules are described in the
 * section "Formatting Rules" of the README.
 * </p>
 */
public class RpgleFormatter implements ILineNumberProvider {

    public static final int DEFAULT_END_COLUMN = 100;

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
    private final FormatConstKeywordRule formatConstKeywordRule;
    private final SortConstValueToEndRule sortConstValueToEndRule;
    private final RemoveEmptyCommentLinesRule removeEmptyCommentLinesRule;
    private final RemoveEmptyLinesBeforeDclPiRule removeEmptyLinesBeforeDclPiRule;
    private final CalculationStatementFormatter calculationStatementFormatter;

    private final List<FormatError> errors = new ArrayList<>();
    private final List<FormatError> warnings = new ArrayList<>();
    private int endColumn;
    private CollectedStatement currentStatement;

    public RpgleFormatter(FormatterConfig config) {
        this.config = config;
        this.formatterUtils = new FormatterUtils(config, this);
        this.tokenizer = new Tokenizer(config);
        this.formatConstKeywordRule = new FormatConstKeywordRule(config);
        this.sortConstValueToEndRule = new SortConstValueToEndRule(config);
        this.removeEmptyCommentLinesRule = new RemoveEmptyCommentLinesRule();
        this.removeEmptyLinesBeforeDclPiRule = new RemoveEmptyLinesBeforeDclPiRule();
        this.calculationStatementFormatter = new CalculationStatementFormatter(config);
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

    /**
     * Sets the effective end column from the physical source length.
     * <p>
     * The resulting end column is
     * {@code min(configuredMaxLineWidth, sourceLength)}, so the formatter never
     * exceeds either the physical record boundary or the user-configured
     * maximum line width.
     * <p>
     * Callers are responsible for passing the right value:
     * <ul>
     * <li><b>Source members</b> — pass the physical SRCDTA field length (e.g.
     * {@code SourceMember.getSourceLength()} or
     * {@code LpexViewUtils.computeSourceLength()}).</li>
     * <li><b>Stream files</b> — pass {@link #DEFAULT_END_COLUMN}, because
     * stream files have no physical record limit and the configured max line
     * width is applied via the {@code min()} above.</li>
     * </ul>
     *
     * @param sourceLength physical source length, or
     *        {@link #DEFAULT_END_COLUMN} for stream files
     */
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
        return format(input, indent, new String[0]);
    }

    /**
     * Formats the RPGLE source from the given input, that follows the given
     * lines.
     * <p>
     * The preceding lines are not formatted. They only tell the blocks, that
     * enclose the input, so that selected lines are indented like in the whole
     * source. A statement, that starts in front of the input, is not formatted
     * either.
     * </p>
     *
     * @param input - the input source
     * @param indent - the indent of the statements outside of all blocks
     * @param precedingLines - the source lines in front of the input
     * @return formatted result containing statement-level mapping
     * @throws RpgleFormatterException if formatting fails
     */
    public FormattedResult format(IRpgleInput input, int indent, String[] precedingLines) throws RpgleFormatterException {

        if (!input.isFreeFormat()) {
            IRpgleFormatterPlugin.logError(Messages.Error_Not_free_format, null);
            String[] sourceLines = input.getSourceLines();
            return new FormattedResult(
                new FormattedStatement[] { new FormattedStatement(input.getStartLineNumber(), sourceLines.length, sourceLines) });
        }

        errors.clear();
        warnings.clear();

        String[] inputLines = input.getSourceLines();
        String[] sourceLines = new String[precedingLines.length + inputLines.length];
        System.arraycopy(precedingLines, 0, sourceLines, 0, precedingLines.length);
        System.arraycopy(inputLines, 0, sourceLines, precedingLines.length, inputLines.length);
        List<FormattedStatement> results = new ArrayList<>();

        // Step 1: Collect multi-line statements
        int startLineNumber = input.getStartLineNumber();
        CollectedStatement[] statements = ContinuationHandler.collectStatements(sourceLines, startLineNumber - precedingLines.length);

        // Step 2: Pre-compute which comment lines should be suppressed (output
        // as blank)
        boolean[] suppressComment = config.isRemoveEmptyCommentLines() ? removeEmptyCommentLinesRule.apply(statements)
            : new boolean[statements.length];

        boolean[] suppressBeforeDclPi = config.isRemoveEmptyLinesBeforeDclPi() ? removeEmptyLinesBeforeDclPiRule.apply(statements)
            : new boolean[statements.length];

        // Step 3: Process each statement
        boolean formatterCompileTimeArrayOnly = false;
        boolean formatterTemporarilyDisabled = false;

        for (int stmtIdx = 0; stmtIdx < statements.length; stmtIdx++) {
            currentStatement = statements[stmtIdx];

            // Format only compile time array statements after the first compile
            // time array has been processed
            StatementType type = currentStatement.getType();
            if (type == StatementType.COMPILE_TIME_ARRAY) {
                formatterCompileTimeArrayOnly = true;
            }

            // Statements in front of the input only tell the nesting and
            // whether the formatter is disabled
            boolean isPreceding = currentStatement.getStartLineNumber() < startLineNumber;

            // Warn, if the branches of a conditional group end with different
            // blocks, because then the indentation may be wrong
            CollectedStatement unbalancedIfDirective = currentStatement.getUnbalancedIfDirective();
            if (unbalancedIfDirective != null && !isPreceding) {
                warnings.add(new FormatError(currentStatement.getStartLineNumber(), currentStatement.getEndLineNumber(), type,
                    Messages.bind(Messages.Warning_Warning_on_line_A_Branches_of_IF_group_of_line_B_end_in_different_blocks,
                        currentStatement.getStartLineNumber(), unbalancedIfDirective.getStartLineNumber()),
                    null));
            }

            // Check for formatter directive
            int directive = getFormatterDirective(currentStatement, type);
            if (directive != 0) {
                if (!isPreceding) {
                    results.add(asUnformatted(currentStatement));
                }
                formatterTemporarilyDisabled = (directive == -1);
                continue;
            }

            if (isPreceding) {
                // The lines of a statement, that starts in front of the input,
                // stay unchanged.
                if (currentStatement.getEndLineNumber() >= startLineNumber) {
                    results.add(asUnformatted(currentStatement, startLineNumber));
                }
                continue;
            }

            boolean passthrough = formatterTemporarilyDisabled || (formatterCompileTimeArrayOnly && type != StatementType.COMPILE_TIME_ARRAY)
                || hasEmbeddedDirectivesAndComments(currentStatement) || currentStatement.isAfterEof();
            if (passthrough) {
                results.add(asUnformatted(currentStatement));
                continue;
            }

            // Calculations, that the IBM formatter formats or nobody, and
            // declarations, that are not formatted
            if (!isFormatted(currentStatement, type)) {
                results.add(asUnformatted(currentStatement));
                continue;
            }

            List<String> stmtOutput = formatStatement(currentStatement, type, suppressBeforeDclPi[stmtIdx], suppressComment[stmtIdx], indent);
            results
                .add(new FormattedStatement(currentStatement.getStartLineNumber(), currentStatement.numLines(), stmtOutput.toArray(new String[0])));
        }

        return new FormattedResult(results.toArray(new FormattedStatement[0]));
    }

    /**
     * Returns the output lines of a statement: unchanged for an OTHER
     * statement, none for a comment or blank line, that is removed before a
     * dcl-pi, a blank line for a comment, that is removed, and the formatted
     * lines otherwise.
     */
    private List<String> formatStatement(CollectedStatement statement, StatementType type, boolean isRemovedBeforeDclPi, boolean isRemovedComment,
        int indent) {

        if (isRemovedBeforeDclPi && (type == StatementType.COMMENT || type == StatementType.BLANK)) {
            // The line is completely removed.
            return new ArrayList<>();
        }

        if (type == StatementType.COMMENT && isRemovedComment) {
            return new ArrayList<>(Collections.singletonList(""));
        }

        return formatOrKeepOriginal(statement, type, indent);
    }

    /**
     * Returns the embedded comments and the formatted lines of the statement.
     * If the statement cannot be formatted, its original lines are returned
     * instead and the error is recorded.
     */
    private List<String> formatOrKeepOriginal(CollectedStatement statement, StatementType type, int indent) {

        try {
            List<String> stmtOutput = new ArrayList<>();
            // Calculations keep their lines, including the comments between
            // them, whereas declarations are rebuilt from their tokens.
            if (!isCalculation(type)) {
                int commentIndent = getStatementIndent(statement, indent);
                for (String comment : statement.getEmbeddedComments()) {
                    stmtOutput.add(indentComment(comment, commentIndent, statement.getStartLineNumber()));
                }
            }
            stmtOutput.addAll(formatLine(statement, type, indent));
            return stmtOutput;
        } catch (Exception e) {
            Throwable unexpectedCause = null;
            if (e instanceof LineOverflowException) {
                ((LineOverflowException)e).setLineNumbers(statement.getStartLineNumber(), statement.getEndLineNumber());
            } else {
                unexpectedCause = e;
            }
            errors.add(
                new FormatError(statement.getStartLineNumber(), statement.getEndLineNumber(), type, getMessageWithLineNumber(e), unexpectedCause));
            // Original-source fallback: keep the statement verbatim, so that
            // later lines remain numbered correctly; the caller is informed
            // via getErrors(). The original lines contain the embedded
            // comments, too, so they must not be added again.
            return new ArrayList<>(statement.getOriginalStatements());
        }
    }

    /**
     * Returns the message of the exception, if it contains the line number. The
     * subclasses of {@link RpgleFormatterException} put it into their message;
     * the base class (e.g. thrown by the tokenizer) and runtime exceptions do
     * not.
     *
     * @return the message or <code>null</code>, if {@link FormatError} has to
     *         build it from the cause
     */
    private static String getMessageWithLineNumber(Exception e) {

        if (e instanceof RpgleFormatterException && e.getClass() != RpgleFormatterException.class) {
            return e.getMessage();
        }

        return null;
    }

    private FormattedStatement asUnformatted(CollectedStatement stmt) {
        return new FormattedStatement(stmt.getStartLineNumber(), stmt.numLines(), stmt.getOriginalStatements().toArray(new String[0]));
    }

    /**
     * Returns the lines of a statement from the given line on unchanged.
     */
    private FormattedStatement asUnformatted(CollectedStatement stmt, int firstLineNumber) {

        List<String> lines = stmt.getOriginalStatements();
        List<String> tail = lines.subList(firstLineNumber - stmt.getStartLineNumber(), lines.size());

        return new FormattedStatement(firstLineNumber, tail.size(), tail.toArray(new String[0]));
    }

    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Returns the list of statements that could not be formatted during the
     * last {@link #format(IRpgleInput, int)} call. The corresponding original
     * source lines are still present in the formatted result; this list is the
     * only place where the underlying error information is exposed.
     *
     * @return an unmodifiable list of formatting errors (empty if all
     *         statements were formatted successfully)
     */
    public List<FormatError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Returns the messages of the errors of the last
     * {@link #format(IRpgleInput, int)} call for the error dialog. Each message
     * contains its line number.
     *
     * @return the messages (empty if all statements were formatted
     *         successfully)
     */
    public String[] getErrorMessages() {
        return getMessages(errors);
    }

    public int getWarningCount() {
        return warnings.size();
    }

    /**
     * Returns the warnings of the last {@link #format(IRpgleInput, int)} call,
     * such as a conditional group, whose branches end with different blocks.
     * Unlike errors, warnings do not leave statements unchanged.
     *
     * @return an unmodifiable list of formatting warnings
     */
    public List<FormatError> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Returns the messages of the warnings of the last
     * {@link #format(IRpgleInput, int)} call for the warning dialog. Each
     * message contains its line number.
     *
     * @return the messages (empty if there are no warnings)
     */
    public String[] getWarningMessages() {
        return getMessages(warnings);
    }

    /**
     * Returns the messages of the errors followed by the messages of the
     * warnings of the last {@link #format(IRpgleInput, int)} call.
     *
     * @return the messages (empty if there are neither errors nor warnings)
     */
    public String[] getErrorAndWarningMessages() {

        List<FormatError> errorsAndWarnings = new ArrayList<>(errors);
        errorsAndWarnings.addAll(warnings);

        return getMessages(errorsAndWarnings);
    }

    private static String[] getMessages(List<FormatError> formatErrors) {

        String[] messages = new String[formatErrors.size()];
        for (int i = 0; i < formatErrors.size(); i++) {
            messages[i] = formatErrors.get(i).getMessage();
        }

        return messages;
    }

    /**
     * Writes the unexpected errors of the last
     * {@link #format(IRpgleInput, int)} call with their stack trace to the
     * Eclipse error log, because the error dialog shows only the message.
     * Expected errors like {@link LineOverflowException} have no cause and are
     * not logged.
     *
     * @param sourceName name of the formatted source for the log message
     */
    public void logUnexpectedErrors(String sourceName) {

        for (FormatError error : errors) {
            if (error.getCause() != null) {
                IRpgleFormatterPlugin.logError(String.format("Unexpected error on line %d of %s.", error.getStartLineNumber(), sourceName), //$NON-NLS-1$
                    error.getCause());
            }
        }
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

        int indent = getStatementIndent(statement, defaultIndent);

        if (statement.hasEmbeddedDirectives()) {
            return formatWithEmbeddedDirectives(statement, type, indent);
        }

        if (isCalculation(type)) {
            return calculationStatementFormatter.format(statement, type, indent, ensureEndColumn());
        }

        switch (type) {
        case BLANK:
            // Blank line without indent
            return new ArrayList<>(Collections.singletonList(""));
        case FREE_DIRECTIVE:
            return formatFreeDirective(statement);
        case COMMENT:
            return formatComment(statement, indent);
        case COMPILER_DIRECTIVE:
            return formatCompilerDirective(statement, indent);
        case COMPILE_TIME_ARRAY:
            // apply casing only
            return formatCompileTimeArray(statement, indent);
        case CTL_OPT:
            return formatCtlOpt(statement, indent);
        case DCL_C:
            return formatDclC(statement, indent);
        case DCL_DS:
        case DCL_PR:
        case DCL_PI:
        case DCL_ENUM:
        case DCL_PROC:
            return formatDclBlock(statement, type, indent);
        case DCL_F:
        case DCL_S:
            return formatDclStatement(statement, type, indent);
        case DCL_SUBF:
            return formatSubField(statement, indent);
        case END_DS:
        case END_PR:
        case END_PI:
        case END_ENUM:
        case END_PROC:
            return formatEndStatement(statement, type, indent);
        default:
            return formatKeepOriginal(statement);
        }
    }

    /**
     * Returns <code>true</code>, if iRPGFormatter formats the statement: a
     * calculation, if it is the formatter of the calculations, and any other
     * statement, if it formats the declarations. A comment line is formatted
     * like the statement, that follows it, or like a calculation, if no
     * statement follows.
     */
    private boolean isFormatted(CollectedStatement statement, StatementType type) throws RpgleFormatterException {

        if (type == StatementType.COMMENT) {
            CollectedStatement followingStatement = statement.getFollowingStatement();
            if (followingStatement == null) {
                return isFormatCalculations();
            }
            type = followingStatement.getType();
        }

        if (isCalculation(type)) {
            return isFormatCalculations();
        }

        return config.isFormatDeclarations();
    }

    private boolean isFormatCalculations() {
        return config.getCalculationFormatter() == CalculationFormatter.IRPG_FORMATTER;
    }

    /**
     * Returns the indent of a statement from the nesting of its blocks.
     */
    private int getStatementIndent(CollectedStatement statement, int defaultIndent) {
        return statement.getIndentLevel() * config.getIndent() + statement.getSelectDepth() * config.getSelectIndent() + defaultIndent;
    }

    /**
     * Returns <code>true</code> for a calculation statement, such as
     * <code>IF</code> or an assignment. Statements that the formatter does not
     * know are calculations, too.
     */
    private static boolean isCalculation(StatementType type) {
        return type == StatementType.OTHER || type.isCalculation();
    }

    /**
     * Formats the **free directive: without indent, but in the casing of the
     * special words.
     */
    private List<String> formatFreeDirective(CollectedStatement statement) {

        String freeSpecialWord = FormattingRules.applyFormattingRule(statement.getStatement(),
            new FormatSpecialWordsRule(config.getSpecialWords(), config.getKeywordCasingStyle()));

        return new ArrayList<>(Collections.singletonList(freeSpecialWord));
    }

    /**
     * Formats a comment line with the indent of the next statement.
     *
     * @throws LineOverflowException if the line exceeds the end column
     */
    private List<String> formatComment(CollectedStatement statement, int indent) throws RpgleFormatterException {
        return new ArrayList<>(Collections.singletonList(indentComment(statement.getLine(0), indent, statement.getStartLineNumber())));
    }

    /**
     * Returns the comment with the indent.
     *
     * @param comment - the comment line or a comment, that is moved in front of
     *        its statement
     * @param indent - the indent
     * @param lineNumber - the line number for the error message
     * @throws LineOverflowException if the indented comment exceeds the end
     *         column and is longer than before
     */
    private String indentComment(String comment, int indent, int lineNumber) throws RpgleFormatterException {

        String formatted = StringUtils.spaces(indent) + comment.trim();
        if (formatted.length() > ensureEndColumn() && formatted.length() > comment.length()) {
            throw new LineOverflowException(comment.trim(), lineNumber);
        }

        return formatted;
    }

    /**
     * Returns the line unchanged.
     */
    private List<String> formatKeepOriginal(CollectedStatement statement) {

        List<String> lines = statement.getOriginalStatements();

        return lines;
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
     * Formats a compile time array.
     */
    private List<String> formatCompileTimeArray(CollectedStatement statement, int indent) {

        List<String> result = new ArrayList<>();

        String formatted = formatterUtils.getFormattingRules().formatCompileTimeArray(statement.getStatement());
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
        String[] results = formatterUtils.formatTokens("", tokens, indent, ensureEndColumn());

        return Arrays.asList(results);
    }

    /**
     * Formats a dcl-c (constant) statement.
     *
     * @throws RpgleFormatterException
     */
    private List<String> formatDclC(CollectedStatement statement, int indent) throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(statement.getStatement(), StatementType.DCL_C);
        tokens = applyTokenRules(statement, StatementType.DCL_C, tokens, true);
        String[] results = formatterUtils.formatTokens("", tokens, indent, ensureEndColumn());

        return Arrays.asList(results);
    }

    /**
     * Formats dcl-ds, dcl-pr, dcl-pi statements.
     */
    private List<String> formatDclBlock(CollectedStatement statement, StatementType type, int indent) throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(statement.getStatement(), type);
        tokens = applyTokenRules(statement, type, tokens, true);
        String[] results = formatterUtils.formatTokens("", tokens, indent, ensureEndColumn());

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
        String[] results = formatterUtils.formatTokens("", tokens, indent, ensureEndColumn());

        return Arrays.asList(results);
    }

    /**
     * Formats a sub-statement (parameter, subfield).
     */
    private List<String> formatSubField(CollectedStatement statement, int indent) throws RpgleFormatterException {

        int subFieldAlignCol = formatterUtils.getSubFieldAlignColumn(statement, ensureEndColumn());

        IToken[] tokens = tokenizer.tokenize(statement.getStatement(), StatementType.DCL_SUBF);
        tokens = applyTokenRules(statement, StatementType.DCL_SUBF, tokens, true);
        String[] results = formatterUtils.formatTokens("", tokens, indent, ensureEndColumn(), subFieldAlignCol);

        return Arrays.asList(results);
    }

    /**
     * Formats an end statement (end-ds, end-pr, end-pi, end-proc).
     *
     * @throws RpgleFormatterException
     */
    private List<String> formatEndStatement(CollectedStatement statement, StatementType type, int indent) throws RpgleFormatterException {

        IToken[] tokens = tokenizer.tokenize(statement.getStatement(), type);
        tokens = applyTokenRules(statement, type, tokens, true);
        String[] results = formatterUtils.formatTokens("", tokens, indent, ensureEndColumn());

        return Arrays.asList(results);
    }

    /**
     * Applies the rules of a statement type to the tokens of a statement or of
     * a section of a statement.
     * <p>
     * Rules that address a token by its position in the statement, such as the
     * name of a procedure interface, are applied to the first section only.
     * </p>
     *
     * @param statement - the statement
     * @param type - the type of the statement
     * @param tokens - the tokens of the statement or of a section of it
     * @param isStartOfStatement - <code>true</code>, if the tokens start with
     *        the first token of the statement
     * @return the tokens after applying the rules
     */
    private IToken[] applyTokenRules(CollectedStatement statement, StatementType type, IToken[] tokens, boolean isStartOfStatement)
        throws RpgleFormatterException {

        if (type == StatementType.DCL_SUBF) {
            tokens = sortConstValueToEndRule.apply(tokens);
        }

        if (!isStartOfStatement) {
            return tokens;
        }

        switch (type) {
        case DCL_C:
            return formatConstKeywordRule.apply(tokens);
        case DCL_PI:
            CollectedStatement parent = statement.getParent();
            String procName = null;
            if (parent != null && parent.getType() == StatementType.DCL_PROC) {
                procName = getProcName(parent);
            }
            return new FormatPiNameRule(procName, config.isReplacePiName()).apply(tokens);
        case END_PROC:
            CollectedStatement procStmt = statement.getMatchingDclProc();
            String endProcName = (procStmt != null) ? getProcName(procStmt) : null;
            return new FormatEndProcNameRule(endProcName, config.isRemoveEndProcName()).apply(tokens);
        default:
            return tokens;
        }
    }

    /**
     * Formats a declaration with compiler directives between its lines, such as
     * <code>/IF</code> ... <code>/ENDIF</code> within a <code>DCL-S</code>
     * statement.
     * <p>
     * The directives split the statement into sections. Each section is
     * formatted on lines of its own, so that no token moves across a
     * directive. The directives are indented like continuation lines, the
     * sections within a conditional group one level deeper.
     * </p>
     * <p>
     * The statement is left unchanged, if a directive is enclosed by a token,
     * such as the parameters of a keyword, or if the statement is not a
     * declaration.
     * </p>
     */
    private List<String> formatWithEmbeddedDirectives(CollectedStatement statement, StatementType type, int indent)
        throws RpgleFormatterException {

        switch (type) {
        case CTL_OPT:
        case DCL_C:
        case DCL_DS:
        case DCL_PR:
        case DCL_PI:
        case DCL_ENUM:
        case DCL_PROC:
        case DCL_F:
        case DCL_S:
        case DCL_SUBF:
        case END_DS:
        case END_PR:
        case END_PI:
        case END_ENUM:
        case END_PROC:
            break;
        default:
            return formatKeepOriginal(statement);
        }

        List<IToken[]> sections = splitAtEmbeddedDirectives(statement, type);
        if (sections == null) {
            return formatKeepOriginal(statement);
        }

        Integer subFieldAlignCol = null;
        if (type == StatementType.DCL_SUBF) {
            subFieldAlignCol = formatterUtils.getSubFieldAlignColumn(statement, ensureEndColumn());
        }

        List<String> results = new ArrayList<>();
        List<EmbeddedDirective> directives = statement.getEmbeddedDirectives();
        int indentStep = config.getIndent();
        int groupDepth = 0;

        for (int i = 0; i < sections.size(); i++) {

            int sectionIndent = indent;
            if (i > 0) {
                String directive = directives.get(i - 1).getLine();
                DirectiveType directiveType = DirectiveType.of(directive);
                if (directiveType == DirectiveType.ELSEIF || directiveType == DirectiveType.ELSE || directiveType == DirectiveType.ENDIF) {
                    groupDepth = Math.max(0, groupDepth - 1);
                }
                String formatted = formatterUtils.getFormattingRules().formatCompilerDirective(directive.trim());
                results.add(StringUtils.spaces(indent + (groupDepth + 1) * indentStep) + formatted);
                if (directiveType == DirectiveType.IF || directiveType == DirectiveType.ELSEIF || directiveType == DirectiveType.ELSE) {
                    groupDepth++;
                }
                sectionIndent = indent + (groupDepth + 1) * indentStep;
            }

            IToken[] tokens = sections.get(i);
            if (tokens.length == 0) {
                continue;
            }

            tokens = applyTokenRules(statement, type, tokens, i == 0);
            String[] lines = formatterUtils.formatTokens("", tokens, sectionIndent, ensureEndColumn(), i == 0 ? subFieldAlignCol : null);
            for (String line : lines) {
                results.add(StringUtils.trimR(line));
            }
        }

        return results;
    }

    /**
     * Splits the tokens of a statement at its embedded directives.
     *
     * @return the sections of the statement, one more than directives, or
     *         <code>null</code>, if a token encloses a directive
     */
    private List<IToken[]> splitAtEmbeddedDirectives(CollectedStatement statement, StatementType type) throws RpgleFormatterException {

        String text = statement.getStatement();
        IToken[] tokens = tokenizer.tokenize(text, type);

        List<IToken[]> sections = new ArrayList<>();
        int start = 0;
        for (EmbeddedDirective directive : statement.getEmbeddedDirectives()) {
            int end = countLeadingTokens(tokens, text.substring(0, directive.getOffset()), type);
            if (end < start) {
                return null;
            }
            sections.add(Arrays.copyOfRange(tokens, start, end));
            start = end;
        }
        sections.add(Arrays.copyOfRange(tokens, start, tokens.length));

        return sections;
    }

    /**
     * Returns the number of tokens of the text in front of a directive, if
     * these tokens are the leading tokens of the statement. Otherwise a token
     * of the statement encloses the directive.
     *
     * @return number of tokens or -1, if a token encloses the directive
     */
    private int countLeadingTokens(IToken[] tokens, String textInFrontOfDirective, StatementType type) {

        IToken[] leadingTokens;
        try {
            leadingTokens = tokenizer.tokenize(textInFrontOfDirective, type);
        } catch (RpgleFormatterException e) {
            return -1;
        }

        if (leadingTokens.length > tokens.length) {
            return -1;
        }

        // The raw value includes the blanks behind the token, which are missing
        // at the end of the text in front of the directive.
        for (int i = 0; i < leadingTokens.length; i++) {
            if (!leadingTokens[i].getRawValue().trim().equals(tokens[i].getRawValue().trim())) {
                return -1;
            }
        }

        return leadingTokens.length;
    }

    /**
     * Returns <code>true</code>, if a statement contains both compiler
     * directives and comments between its lines. The formatter would move the
     * comments in front of the statement and thus out of the branches of a
     * conditional group, therefore such a statement is left unchanged.
     */
    private static boolean hasEmbeddedDirectivesAndComments(CollectedStatement statement) {
        return statement.hasEmbeddedDirectives() && statement.getEmbeddedComments().length > 0;
    }

    private int ensureEndColumn() throws RpgleFormatterException {

        if (endColumn <= 1) {
            throw new RpgleFormatterException("End column not set.");
        }

        return endColumn;
    }

    @Override
    public int getLineNumber() {
        return currentStatement.getStartLineNumber();
    }
}
