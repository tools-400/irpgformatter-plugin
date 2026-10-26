/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

import java.util.LinkedList;
import java.util.List;

import de.tools400.lpex.irpgformatter.parser.StatementType;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.FormattingRules;
import de.tools400.lpex.irpgformatter.rules.IFormattingRule;
import de.tools400.lpex.irpgformatter.rules.NullFormattingRule;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;
import de.tools400.lpex.irpgformatter.rules.casing.FormatDataTypeRule;
import de.tools400.lpex.irpgformatter.rules.casing.FormatDeclarationTypeRule;
import de.tools400.lpex.irpgformatter.rules.casing.FormatKeywordRule;
import de.tools400.lpex.irpgformatter.rules.casing.FormatSpecialWordsRule;
import de.tools400.lpex.irpgformatter.rules.statements.FormatParameterRule;
import de.tools400.lpex.irpgformatter.rules.statements.MaximumNameLengthRule;
import de.tools400.lpex.irpgformatter.rules.statements.MinimumNameLengthRule;
import de.tools400.lpex.irpgformatter.statement.CollectedStatement;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;
import de.tools400.lpex.irpgformatter.tokenizer.Tokenizer;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

public class FormatterUtils implements RpgleSourceConstants {

    private final FormatterConfig config;
    private final FormattingRules formattingRules;
    private final MaximumNameLengthRule maxNameLengthRule;
    private final MinimumNameLengthRule minNameLengthRule;
    private final FormatParameterRule formatParameterRule;

    private ILineNumberProvider lineNumberProvider;

    /**
     * How a name or a literal is split: the continuation character at the end
     * of a part and the search for the end of a part.
     */
    private abstract static class Splitter {

        private final String continuation;

        Splitter(String continuation) {
            this.continuation = continuation;
        }

        String getContinuation() {
            return continuation;
        }

        /**
         * Returns the length of the next part of the text, that is not longer
         * than the given length.
         */
        abstract int findBreakpoint(String text, int maxLength);
    }

    private final Splitter NAME_SPLITTER = new Splitter(ELLIPSIS) {
        @Override
        int findBreakpoint(String text, int maxLength) {
            return findBreakpointInName(text, maxLength);
        }
    };

    private final Splitter LITERAL_SPLITTER = new Splitter(PLUS) {
        @Override
        int findBreakpoint(String text, int maxLength) {
            return findBreakpointInLiteral(text, maxLength);
        }
    };

    public FormatterUtils(FormatterConfig config) {
        this.config = config;
        this.formattingRules = new FormattingRules(config);
        this.maxNameLengthRule = new MaximumNameLengthRule(config);
        this.minNameLengthRule = new MinimumNameLengthRule(config);
        this.formatParameterRule = new FormatParameterRule(config);
        this.lineNumberProvider = null;
    }

    public FormatterUtils(FormatterConfig config, ILineNumberProvider lineNumberProvider) {
        this(config);
        this.lineNumberProvider = lineNumberProvider;
    }

    public FormatterConfig getConfig() {
        return config;
    }

    public FormattingRules getFormattingRules() {
        return formattingRules;
    }

    public String[] formatTokens(String line, IToken[] tokens, int defaultIndent, int maxLineLength) throws RpgleFormatterException {
        return formatTokens(line, tokens, defaultIndent, maxLineLength, null);
    }

    public String[] formatTokens(String line, IToken[] tokens, int indent, int maxLineLength, Integer subFieldAlignCol)
        throws RpgleFormatterException {

        boolean isSubField = subFieldAlignCol != null;
        boolean isAlignSubFieldsEnabled = config.isAlignSubFields();

        int maxLength = maxLineLength - indent;

        List<String> results = new LinkedList<>();

        String formattedLine = line;

        for (int i = 0; i < tokens.length; i++) {
            IToken token = tokens[i];
            String[] tokenResults;
            if (token.getType() == TokenType.CTL) {
                tokenResults = formatControl(formattedLine, token, maxLength);
            } else if (token.getType() == TokenType.DCL) {
                tokenResults = formatDeclaration(formattedLine, token, maxLength);
            } else if (token.getType() == TokenType.NAME) {
                tokenResults = breakName(formattedLine, token, maxLength, "", "");
            } else if (token.getType() == TokenType.SPECIAL_WORD) {
                tokenResults = formatSpecialWord(formattedLine, token, maxLength);
            } else if (token.getType() == TokenType.LITERAL) {
                tokenResults = breakLiteral(formattedLine, token, maxLength, "", "");
            } else if (token.getType() == TokenType.DATE_TIME_LITERAL) {
                tokenResults = formatNone(formattedLine, token, maxLength);
            } else if (token.getType() == TokenType.FUNCTION) {
                tokenResults = formatFunctionWithParameters(formattedLine, token, maxLength);
            } else if (token.getType() == TokenType.KEYWORD) {
                tokenResults = formatKeywordWithParameters(formattedLine, token, maxLength);
            } else if (token.getType() == TokenType.DATA_TYPE) {
                tokenResults = formatDataTypeWithParameters(formattedLine, token, maxLength);
            } else if (token.getType() == TokenType.EOL) {
                tokenResults = formatEndOfLine(formattedLine, token, maxLength);
            } else if (token.getType() == TokenType.COMMENT) {
                tokenResults = formatComment(formattedLine, token, maxLength);
            } else if (token.getType() == TokenType.OTHER) {
                tokenResults = formatNone(formattedLine, token, maxLength);
            } else {
                throw new UnexpectedTokenException(token, getLineNumber());
            }

            if (tokenResults.length == 1) {
                // Only 1 line returned. Use this line for next iteration.
                formattedLine = tokenResults[tokenResults.length - 1];
            } else {
                // Multiple lines returned, so the current line is full.

                // Add lines up to length -1 to result.
                for (int j = 0; j < tokenResults.length - 1; j++) {
                    results.add(tokenResults[j]);
                }

                // Use last line returned for next iteration.
                formattedLine = tokenResults[tokenResults.length - 1];
            }

            if (token.getType() == TokenType.NAME || isSpecialWordStarN(token)) {
                if (isSubField) {
                    if (isAlignSubFieldsEnabled) {
                        if (formattedLine.trim().length() > subFieldAlignCol) {
                            throw new UnexpectedErrorException("Cannot align sub-field. Current line length " + formattedLine.length()
                                + " exceeds alignment column " + subFieldAlignCol + ".", getLineNumber());
                        }
                        formattedLine = StringUtils.padR(formattedLine.trim(), subFieldAlignCol);
                    } else if (subFieldAlignCol == 0) {
                        formattedLine = formattedLine.trim();
                    }
                }
            }

            // Add space if not end-of-line marker.
            if (i < tokens.length - 1) {
                if (tokens[i + 1].getType() != TokenType.EOL) {
                    formattedLine = formattedLine + SPACE;
                }
            }
        }

        // Add current line to result.
        if (formattedLine.length() > 0) {
            results.add(formattedLine);
        }

        // Indent lines, if requested.
        if (indent > 0) {
            results = indentLines(results, indent);
        }

        return results.toArray(new String[0]);
    }

    private static List<String> indentLines(List<String> lines, int indent) {

        String indentSpaces = StringUtils.spaces(indent);
        for (int i = 0; i < lines.size(); i++) {
            lines.set(i, indentSpaces + lines.get(i));
        }

        return lines;
    }

    private String[] formatNone(String line, IToken token, int maxLineLength) throws RpgleFormatterException {
        return formatOther(line, token, maxLineLength, new NullFormattingRule());
    }

    private String[] formatControl(String line, IToken token, int maxLineLength) throws RpgleFormatterException {
        return formatOther(line, token, maxLineLength, new FormatDeclarationTypeRule(config.getDeclarationTypes(), config.getKeywordCasingStyle()));
    }

    private String[] formatDeclaration(String currentLine, IToken token, int maxLineLength) throws RpgleFormatterException {
        return formatOther(currentLine, token, maxLineLength,
            new FormatDeclarationTypeRule(config.getDeclarationTypes(), config.getKeywordCasingStyle()));
    }

    private String[] formatComment(String line, IToken token, int maxLineLength) throws RpgleFormatterException {
        return formatOther(line, token, maxLineLength, new NullFormattingRule());
    }

    private String[] formatEndOfLine(String line, IToken token, int maxLineLength) throws RpgleFormatterException {
        return formatOther(line, token, maxLineLength, new NullFormattingRule());
    }

    private String[] formatSpecialWord(String line, IToken token, int maxLineLength) throws RpgleFormatterException {
        return formatOther(line, token, maxLineLength, new FormatSpecialWordsRule(config.getSpecialWords(), config.getKeywordCasingStyle()));
    }

    private String[] formatOther(String line, IToken token, int maxLineLength, IFormattingRule formattingRule) throws RpgleFormatterException {
        List<String> parts = new LinkedList<>();

        String subIndent = StringUtils.getIndent(line);
        if (subIndent.length() == 0) {
            subIndent = formattingRules.createIndent(1);
        }

        String value = FormattingRules.applyFormattingRule(token.getValue(), formattingRule);

        int maxLength = maxLineLength - line.length();
        if (value.length() <= maxLength) {
            parts.add(line + value);
        } else {
            // Line overflow. Trim line.
            parts.add(StringUtils.trimR(line));
            maxLength = maxLineLength - subIndent.length();
            if (subIndent.length() + value.length() <= maxLineLength) {
                parts.add(subIndent + value);
            } else {
                throw new LineOverflowException(value, getLineNumber());
            }
        }

        return parts.toArray(new String[0]);
    }

    public String[] formatFunctionWithParameters(String line, IToken token, int maxLineLength) throws RpgleFormatterException {

        if (token.getType() != TokenType.FUNCTION) {
            throw new UnexpectedTokenException(token, getLineNumber());
        }

        if (token.haveChildren()) {
            return formatWithParameters(line, token, maxLineLength, new NullFormattingRule());
        } else {
            return formatOther(line, token, maxLineLength, new NullFormattingRule());
        }
    }

    public String[] formatKeywordWithParameters(String line, IToken token, int maxLineLength) throws RpgleFormatterException {

        if (token.getType() != TokenType.KEYWORD) {
            throw new UnexpectedTokenException(token, getLineNumber());
        }

        if (token.haveChildren()) {
            return formatWithParameters(line, token, maxLineLength, new FormatKeywordRule(config.getKeywords(), config.getKeywordCasingStyle()));
        } else {
            return formatOther(line, token, maxLineLength, new FormatKeywordRule(config.getKeywords(), config.getKeywordCasingStyle()));
        }
    }

    public String[] formatDataTypeWithParameters(String line, IToken token, int maxLineLength) throws RpgleFormatterException {

        if (token.getType() != TokenType.DATA_TYPE) {
            throw new UnexpectedTokenException(token, getLineNumber());
        }

        if (token.haveChildren()) {
            return formatWithParameters(line, token, maxLineLength, new FormatDataTypeRule(config.getDataTypes(), config.getKeywordCasingStyle()));
        } else {
            return formatOther(line, token, maxLineLength, new FormatDataTypeRule(config.getDataTypes(), config.getKeywordCasingStyle()));
        }
    }

    public String[] formatWithParameters(String line, IToken token, int maxLineLength, IFormattingRule... formattingRule)
        throws RpgleFormatterException {

        boolean isBreakBeforeKeyword = config.isBreakBeforeKeyword();

        List<String> result = new LinkedList<>();

        String subIndent = formattingRules.createIndent(1);

        String keyword = FormattingRules.applyFormattingRule(token.getValue(), formattingRule);
        String startKeyword = keyword + OPEN_BRACKET;
        IToken[] children = token.getChildren();
        String parameters = buildParameters(children);

        String endKeyword = CLOSE_BRACKET;

        // Try the complete line.
        String completeFunction = startKeyword + parameters + endKeyword;
        if (line.length() + completeFunction.length() <= maxLineLength) {
            result.add(line + completeFunction);
        } else {

            String[] parameterLines;

            String currentLine;
            if (isBreakBeforeKeyword) {
                if (!StringUtils.isNullOrEmpty(line)) {
                    // Line overflow. Trim line.
                    result.add(StringUtils.trimR(line));
                }
                if (subIndent.length() + startKeyword.length() < maxLineLength) {
                    currentLine = subIndent + startKeyword;
                } else {
                    throw new LineOverflowException(startKeyword, getLineNumber());
                }
            } else {
                if (line.length() + startKeyword.length() < maxLineLength) {
                    currentLine = line + startKeyword;
                } else {
                    // Line overflow. Trim line.
                    result.add(StringUtils.trimR(line));
                    currentLine = subIndent + startKeyword;
                }

                // Break before parameter if closing bracket would go to next
                // line
                if (line.length() + completeFunction.length() - endKeyword.length() == maxLineLength) {
                    // Line overflow. Trim line.
                    result.add(StringUtils.trimR(currentLine));
                    currentLine = subIndent;
                }
            }

            parameterLines = formatParameters(currentLine, children, maxLineLength);

            for (String parameterLine : parameterLines) {
                result.add(StringUtils.trimR(parameterLine));
            }

            // Close function
            int lastIndex = result.size() - 1;
            String lastLine = result.get(lastIndex);
            if (lastLine.length() + endKeyword.length() <= maxLineLength) {
                lastLine = lastLine + endKeyword;
                updateLastListItem(result, lastLine);
            } else {
                result.add(subIndent + endKeyword);
            }
        }

        return result.toArray(new String[0]);
    }

    public String buildParameters(IToken[] tokens) {

        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < tokens.length; i++) {
            String parameter = formatParameter(tokens[i]);
            parameter = formatParameterRule.format(parameter, i, tokens.length);
            buffer.append(parameter);
        }

        return buffer.toString();
    }

    private String[] formatParameters(String line, IToken[] tokens, int maxLineLength) throws RpgleFormatterException {

        String currentLine = line;

        List<String> result = new LinkedList<>();

        String[] parameterParts;
        for (int i = 0; i < tokens.length; i++) {

            FormatParameterRule.Delimiter delimiter = formatParameterRule.delimiterFor(i, tokens.length);
            String parameterPrefix = delimiter.getPrefix();
            String parameterSuffix = delimiter.getSuffix();

            if (i == 0) {
                parameterParts = breakParameter(currentLine, tokens[i], maxLineLength, parameterPrefix, parameterSuffix);
            } else {

                int lastIndex = result.size() - 1;
                currentLine = result.get(lastIndex);
                result.remove(lastIndex);

                parameterParts = breakParameter(currentLine, tokens[i], maxLineLength, parameterPrefix, parameterSuffix);
            }

            for (int p = 0; p < parameterParts.length; p++) {
                result.add(parameterParts[p]);
            }
        }

        return result.toArray(new String[0]);
    }

    private String[] breakParameter(String currentLine, IToken token, int maxLineLength, String prefix, String suffix)
        throws RpgleFormatterException {

        String[] parameterParts;
        if (token.getType() == TokenType.LITERAL) {
            parameterParts = breakLiteral(currentLine, token, maxLineLength, prefix, suffix);
        } else if (token.getType() == TokenType.NAME) {
            parameterParts = breakName(currentLine, token, maxLineLength, prefix, suffix);
        } else if (token.getType() == TokenType.SPECIAL_WORD) {
            parameterParts = notBreakableWithRules(currentLine, token, maxLineLength, prefix, suffix,
                new FormatSpecialWordsRule(config.getSpecialWords(), config.getKeywordCasingStyle()));
        } else if (token.getType() == TokenType.OTHER) {
            parameterParts = notBreakableWithRules(currentLine, token, maxLineLength, prefix, suffix, new NullFormattingRule());
        } else if (token.getType() == TokenType.FUNCTION) {
            parameterParts = formatFunctionWithParameters(currentLine, token, maxLineLength);
        } else if (token.getType() == TokenType.KEYWORD) {
            parameterParts = formatKeywordWithParameters(currentLine, token, maxLineLength);
        } else {
            throw new UnexpectedTokenException(token, getLineNumber());
        }

        return parameterParts;
    }

    private String[] notBreakableWithRules(String line, IToken token, int maxLineLength, String prefix, String suffix, IFormattingRule... rules)
        throws RpgleFormatterException {

        String subIndent = getSubIndent(line);

        List<String> parts = new LinkedList<>();

        String value = FormattingRules.applyFormattingRule(token.getValue(), rules);

        int maxLength = maxLineLength - line.length() - prefix.length() - suffix.length();
        if (value.length() <= maxLength) {
            parts.add(line + prefix + value + suffix);
        } else {
            // Line overflow. Trim line.
            parts.add(StringUtils.trimR(line));
            // Drop leading whitespace from prefix on the new line — the indent
            // already provides the separation, so a leading space (e.g. from
            // BEFORE/BOTH spacing in delimiter-after-parameter mode) would just
            // shift the parameter one column to the right.
            String wrappedPrefix = StringUtils.trimL(prefix);
            maxLength = maxLineLength - subIndent.length() - wrappedPrefix.length() - suffix.length();
            if (value.length() <= maxLength) {
                parts.add(subIndent + wrappedPrefix + value + suffix);
            } else {
                throw new LineOverflowException(value, getLineNumber());
            }
        }

        return parts.toArray(new String[0]);
    }

    public String[] breakLiteral(String line, IToken token, int maxLineLength, String prefix, String suffix) throws RpgleFormatterException {
        return breakNameOrLiteral(line, token, maxLineLength, prefix, suffix, LITERAL_SPLITTER);
    }

    public String[] breakName(String line, IToken token, int maxLineLength, String prefix, String suffix) throws RpgleFormatterException {
        return breakNameOrLiteral(line, token, maxLineLength, prefix, suffix, NAME_SPLITTER);
    }

    private String[] breakNameOrLiteral(String line, IToken token, int maxLineLength, String prefix, String suffix, Splitter splitter)
        throws RpgleFormatterException {

        if (token.getType() != TokenType.NAME && token.getType() != TokenType.LITERAL) {
            throw new UnexpectedTokenException(token, getLineNumber());
        }

        NameOrLiteralBreaker breaker = new NameOrLiteralBreaker(getSubIndent(line), maxLineLength, suffix, splitter);
        breaker.breakValue(line, prefix, token.getValue());

        return breaker.getParts();
    }

    /**
     * Returns the indent of a continuation line: the indent of the line, but
     * at least one indent level.
     */
    private String getSubIndent(String line) {

        String subIndent = StringUtils.getIndent(line);
        if (subIndent.length() == 0) {
            subIndent = formattingRules.createIndent(1);
        }

        return subIndent;
    }

    /**
     * Breaks a name or a literal, that does not fit into the line, into parts.
     * The first part follows the line and the prefix, the middle parts end with
     * the continuation character and the last part ends with the suffix. Each
     * part after the first one starts on a continuation line.
     */
    private final class NameOrLiteralBreaker {

        private final String subIndent;
        private final int maxLineLength;
        private final String suffix;
        private final Splitter splitter;
        private final List<String> parts = new LinkedList<>();

        NameOrLiteralBreaker(String subIndent, int maxLineLength, String suffix, Splitter splitter) {
            this.subIndent = subIndent;
            this.maxLineLength = maxLineLength;
            this.suffix = suffix;
            this.splitter = splitter;
        }

        String[] getParts() {
            return parts.toArray(new String[0]);
        }

        void breakValue(String line, String prefix, String value) throws RpgleFormatterException {

            String continuation = splitter.getContinuation();

            // Space left for prefix and continuation character?
            if (maxLineLength - prefix.length() - line.length() - continuation.length() <= 0) {
                parts.add(StringUtils.trimR(line));
                line = subIndent;
                // Drop leading whitespace from prefix on the new line — the
                // indent already provides the separation, so a leading space
                // (e.g. from BEFORE/BOTH spacing in delimiter-after-parameter
                // mode) would just shift the value one column to the right.
                prefix = StringUtils.trimL(prefix);
            }

            int maxLength = maxLineLength - prefix.length() - line.length() - StringUtils.trimR(suffix).length();
            if (value.length() <= maxLength && value.length() <= maxNameLengthRule.apply(value.length())) {
                parts.add(line + prefix + value + StringUtils.trimR(suffix));
                return;
            }

            maxLength = maxLineLength - line.length() - prefix.length() - continuation.length();
            int breakPoint = splitter.findBreakpoint(value, maxNameLengthRule.apply(maxLength));

            // The first part would be shorter than the minimum length: start on
            // a continuation line. That ends the recursion, because the line is
            // the sub indent then.
            if (!line.equals(subIndent) && !minNameLengthRule.isSatisfiedBy(breakPoint)) {
                parts.add(StringUtils.trimR(line));
                breakValue(subIndent, prefix, value);
                return;
            }

            addParts(line + prefix, value, breakPoint);
        }

        /**
         * Adds the first part, which ends at the break point, and the parts of
         * the rest of the value.
         */
        private void addParts(String firstLine, String value, int breakPoint) throws RpgleFormatterException {

            String continuation = splitter.getContinuation();

            String remaining = addPart(firstLine, value, breakPoint);
            while (remaining.length() > 0) {
                if (subIndent.length() + remaining.length() + suffix.length() <= maxNameLengthRule.apply(maxLineLength)) {
                    parts.add(subIndent + remaining + suffix);
                    remaining = "";
                } else {
                    int maxLength = maxLineLength - subIndent.length() - continuation.length();
                    remaining = addPart(subIndent, remaining, splitter.findBreakpoint(remaining, maxNameLengthRule.apply(maxLength)));
                }
            }
        }

        /**
         * Adds the line with the value up to the break point and the
         * continuation character. A part, that takes the whole rest of the
         * value, is the last part instead: it ends with the suffix, if that
         * fits into the line; otherwise its last character moves to the next
         * line. A continuation character, that nothing continues, breaks the
         * source.
         *
         * @return the rest of the value
         * @throws LineOverflowException - not even one character fits into the
         *         line, e.g. the last character with the suffix into a
         *         continuation line; the next line would be no longer
         */
        private String addPart(String line, String value, int breakPoint) throws RpgleFormatterException {

            if (breakPoint >= value.length()) {
                if (line.length() + value.length() + suffix.length() <= maxLineLength) {
                    parts.add(line + value + suffix);
                    return "";
                }
                breakPoint = value.length() - 1;
            }

            if (breakPoint <= 0) {
                throw new LineOverflowException(value, getLineNumber());
            }

            parts.add(line + value.substring(0, breakPoint) + splitter.getContinuation());

            return value.substring(breakPoint);
        }
    }

    private static int findBreakpointInLiteral(String literal, int maxLength) {

        if (literal.length() < maxLength) {
            return literal.length();
        }

        int offset = maxLength - 1;

        String currentChar;
        for (int i = offset; i >= 0; i--) {
            currentChar = literal.substring(i, i + 1);
            if (SPACE.equals(currentChar)) {
                return i + 1;
            }
        }

        return maxLength;
    }

    private int findBreakpointInName(String name, int maxLength) {

        boolean isBreakBetweenCaseChange = config.isBreakBetweenCaseChange();

        if (name.length() < maxLength) {
            return name.length();
        }

        if (!isBreakBetweenCaseChange) {
            return maxLength;
        }

        int offset = maxLength - 2;

        Character currentChar;
        Character nextChar;
        for (int i = offset; i >= 0; i--) {
            currentChar = name.charAt(i);
            nextChar = name.charAt(i + 1);
            if (Character.isLowerCase(currentChar) && Character.isUpperCase(nextChar)) {
                return i + 1;
            }
        }

        return maxLength;
    }

    private String formatParameter(IToken token) {

        String parameter = token.getValue();
        if (token.getType() == TokenType.FUNCTION) {
            return parameter + OPEN_BRACKET + buildParameters(token.getChildren()) + CLOSE_BRACKET;
        } else if (token.getType() == TokenType.KEYWORD) {
            parameter = formattingRules.formatKeyword(parameter);
        } else if (token.getType() == TokenType.DATA_TYPE) {
            parameter = formattingRules.formatDataType(parameter);
        } else if (token.getType() == TokenType.SPECIAL_WORD) {
            parameter = formattingRules.formatSpecialWord(parameter);
        }

        return parameter;
    }

    private static String updateLastListItem(List<String> listItems, String value) {

        int lastIndex = listItems.size() - 1;

        return updateListItem(listItems, lastIndex, value);
    }

    private static String updateListItem(List<String> listItems, int index, String value) {

        listItems.set(index, value);

        return value;
    }

    /**
     * Gets the keyword alignment column for sub-fields in a block. Computes and
     * caches the value on the parent statement.
     *
     * @param statement - a block statement (dcl-ds, dcl-pi, dcl-pr)
     * @param maxLineLength - maximum length of a source line
     * @return the alignment column (0-based offset within the indented line)
     * @throws RpgleFormatterException
     */
    public int getSubFieldAlignColumn(CollectedStatement statement, int maxLineLength) throws RpgleFormatterException {

        CollectedStatement parent = statement.getParent();
        if (parent == null) {
            return -1;
        }

        int alignColumn = parent.getKeywordAlignColumn();
        if (alignColumn < 0) {
            alignColumn = computeKeywordAlignColumn(parent, maxLineLength);
            parent.setKeywordAlignColumn(alignColumn);
        }

        return alignColumn;
    }

    /**
     * Computes the keyword alignment column for all sub-fields in a block. The
     * alignment column is determined by the longest field name (optionally
     * preceded by a <code>DCL-SUBF</code> prefix) that does not exceed
     * MAX_NAME_LENGTH, plus 1 space. Fields with names longer than
     * MAX_NAME_LENGTH are broken with ellipsis and contribute with their last
     * part to the alignment calculation.
     *
     * @param statement - a block statement (dcl-ds, dcl-pi, dcl-pr)
     * @param maxLineLength - maximum length of a source line
     * @return the alignment column (0-based offset within the indented line)
     * @throws RpgleFormatterException
     */
    private int computeKeywordAlignColumn(CollectedStatement statement, int maxLineLength) throws RpgleFormatterException {

        int maxNameLength = maxNameLengthRule.apply(maxLineLength);
        int longestName = 0;

        Tokenizer tokenizer = new Tokenizer(config);

        for (CollectedStatement child : statement.getChildren()) {

            IToken[] tokens = tokenizer.tokenize(child.getStatement(), StatementType.DCL_SUBF);
            IToken nameToken = getNameToken(tokens);

            String lastNamePart;
            if (nameToken.getType() == TokenType.NAME) {
                String[] nameParts = breakName("", nameToken, maxLineLength, "", "");
                lastNamePart = nameParts[nameParts.length - 1];
            } else if (nameToken.getType() == TokenType.SPECIAL_WORD) {
                lastNamePart = nameToken.getValue();
            } else {
                throw new UnexpectedTokenException(nameToken, getLineNumber());
            }

            int prefixLength = 0;
            if (tokens.length > 0 && tokens[0].getType() == TokenType.DCL) {
                // Optional `DCL-SUBF` prefix shifts the name (and thus the
                // keyword alignment column) to the right by the prefix length
                // plus one separating space.
                prefixLength = tokens[0].getValue().length() + 1;
            }

            int effectiveLength = prefixLength + lastNamePart.length();

            if (effectiveLength <= maxNameLength && effectiveLength > longestName) {
                longestName = effectiveLength;
            }
        }

        return longestName;
    }

    /**
     * Returns the name token from the tokens of a tokenized statement. The name
     * token is either a <code>NAME</code> token or the special word
     * <code>*N</code>.
     *
     * @param tokens - tokens of a tokenized statement
     * @return the name token
     * @throws RpgleFormatterException
     */
    private IToken getNameToken(IToken[] tokens) throws RpgleFormatterException {

        for (IToken token : tokens) {
            if (TokenType.NAME == token.getType() || isSpecialWordStarN(token)) {
                return token;
            }
        }

        throw new TokenNotFoundException(TokenType.NAME.name(), getLineNumber());
    }

    private static boolean isSpecialWordStarN(IToken token) {
        return TokenType.SPECIAL_WORD == token.getType() && "*N".equalsIgnoreCase(token.getValue());
    }

    private int getLineNumber() {

        if (lineNumberProvider != null) {
            return lineNumberProvider.getLineNumber();
        }

        return -1;
    }
}
