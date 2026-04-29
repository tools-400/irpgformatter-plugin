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
import de.tools400.lpex.irpgformatter.rules.statements.RemoveConstKeywordRule;
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

    private interface BreakpointStrategy {
        int findBreakpoint(String text, int maxLength);
    }

    private final BreakpointStrategy NAME_BREAKPOINT = new BreakpointStrategy() {
        public int findBreakpoint(String text, int maxLength) {
            return findBreakpointInName(text, maxLength);
        }
    };

    private final BreakpointStrategy LITERAL_BREAKPOINT = new BreakpointStrategy() {
        public int findBreakpoint(String text, int maxLength) {
            return findBreakpointInLiteral(text, maxLength);
        }
    };

    public FormatterUtils(FormatterConfig config) {
        this.config = config;
        this.formattingRules = new FormattingRules(config);
        this.maxNameLengthRule = new MaximumNameLengthRule(config);
        this.minNameLengthRule = new MinimumNameLengthRule(config);
        this.formatParameterRule = new FormatParameterRule(config);
    }

    public FormatterConfig getConfig() {
        return config;
    }

    public IToken[] sortConstValueToEnd(IToken[] tokens) {
        if (!config.isSortConstValueToEnd()) {
            return tokens;
        }

        List<IToken> constValueTokens = new LinkedList<>();
        List<IToken> otherTokens = new LinkedList<>();
        List<IToken> trailingTokens = new LinkedList<>();

        for (int i = 0; i < tokens.length; i++) {
            IToken token = tokens[i];
            if (token.getType() == TokenType.EOL || token.getType() == TokenType.COMMENT) {
                trailingTokens.add(token);
            } else if (token.getType() == TokenType.KEYWORD && isConstOrValue(token)) {
                constValueTokens.add(token);
            } else {
                otherTokens.add(token);
            }
        }

        if (constValueTokens.isEmpty()) {
            return tokens;
        }

        List<IToken> result = new LinkedList<>();
        result.addAll(otherTokens);
        result.addAll(constValueTokens);
        result.addAll(trailingTokens);
        return result.toArray(new IToken[0]);
    }

    private static boolean isConstOrValue(IToken token) {
        String upper = token.getValue().toUpperCase();
        return "CONST".equals(upper) || "VALUE".equals(upper);
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
                throw new UnexpectedTokenException(token);
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

            if (token.getType() == TokenType.NAME) {
                if (isSubField) {
                    if (isAlignSubFieldsEnabled) {
                        if (formattedLine.trim().length() > subFieldAlignCol) {
                            throw new UnexpectedErrorException("Cannot align sub-field. Current line length " + formattedLine.length()
                                + " exceeds alignment column " + subFieldAlignCol + ".");
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
        return formatOther(line, token, maxLineLength,
            new FormatSpecialWordsRule(config.getSpecialWords(), config.getKeywordCasingStyle()));
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
                throw new LineOverflowException(value);
            }
        }

        return parts.toArray(new String[0]);
    }

    public String[] formatFunctionWithParameters(String line, IToken token, int maxLineLength) throws RpgleFormatterException {

        if (token.getType() != TokenType.FUNCTION) {
            throw new RpgleFormatterException("Invalid token type: " + token.getType());
        }

        if (token.haveChildren()) {
            return formatWithParameters(line, token, maxLineLength, new NullFormattingRule());
        } else {
            return formatOther(line, token, maxLineLength, new NullFormattingRule());
        }
    }

    public String[] formatKeywordWithParameters(String line, IToken token, int maxLineLength) throws RpgleFormatterException {

        if (token.getType() != TokenType.KEYWORD) {
            throw new RpgleFormatterException("Invalid token type: " + token.getType());
        }

        if (token.haveChildren()) {
            return formatWithParameters(line, token, maxLineLength, new FormatKeywordRule(config.getKeywords(), config.getKeywordCasingStyle()),
                new RemoveConstKeywordRule(config.isUseConstKeyword()));
        } else {
            return formatOther(line, token, maxLineLength, new FormatKeywordRule(config.getKeywords(), config.getKeywordCasingStyle()));
        }
    }

    public String[] formatDataTypeWithParameters(String line, IToken token, int maxLineLength) throws RpgleFormatterException {

        if (token.getType() != TokenType.DATA_TYPE) {
            throw new RpgleFormatterException("Invalid token type: " + token.getType());
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
        if (keyword.length() == 0) {
            // e.g. if the const() keyword has been removed
            startKeyword = "";
            endKeyword = "";
        }

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
                    throw new LineOverflowException(startKeyword);
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

        boolean addColonBeforeParameter = config.isDelimiterBeforeParameter();

        String currentLine = line;

        List<String> result = new LinkedList<>();

        String[] parameterParts;
        for (int i = 0; i < tokens.length; i++) {

            String parameterPrefix = "";
            String parameterSuffix = "";

            if (addColonBeforeParameter) {
                if (i > 0) {
                    parameterPrefix = COLON + SPACE;
                }
            } else {
                if (i < tokens.length - 1) {
                    parameterSuffix = COLON + SPACE;
                }
            }

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
        } else {
            throw new UnexpectedTokenException(token);
        }

        return parameterParts;
    }

    private String[] notBreakableWithRules(String line, IToken token, int maxLineLength, String prefix, String suffix, IFormattingRule... rules)
        throws RpgleFormatterException {

        String subIndent = StringUtils.getIndent(line);
        if (subIndent.length() == 0) {
            subIndent = formattingRules.createIndent(1);
        }

        List<String> parts = new LinkedList<>();

        String value = FormattingRules.applyFormattingRule(token.getValue(), rules);

        int maxLength = maxLineLength - line.length() - prefix.length() - suffix.length();
        if (value.length() <= maxLength) {
            parts.add(line + prefix + value + suffix);
        } else {
            // Line overflow. Trim line.
            parts.add(StringUtils.trimR(line));
            maxLength = maxLineLength - subIndent.length() - prefix.length() - suffix.length();
            if (value.length() <= maxLength) {
                parts.add(subIndent + prefix + value + suffix);
            } else {
                throw new LineOverflowException(value);
            }
        }

        return parts.toArray(new String[0]);
    }

    public String[] breakLiteral(String line, IToken token, int maxLineLength, String prefix, String suffix) throws RpgleFormatterException {
        return breakNameOrLiteral(line, token, PLUS, maxLineLength, prefix, suffix, LITERAL_BREAKPOINT);
    }

    public String[] breakName(String line, IToken token, int maxLineLength, String prefix, String suffix) throws RpgleFormatterException {
        return breakNameOrLiteral(line, token, ELLIPSIS, maxLineLength, prefix, suffix, NAME_BREAKPOINT);
    }

    private String[] breakNameOrLiteral(String line, IToken token, String contChar, int maxLineLength, String prefix, String suffix,
        BreakpointStrategy strategy) throws RpgleFormatterException {

        if (token.getType() != TokenType.NAME && token.getType() != TokenType.LITERAL) {
            throw new UnexpectedTokenException(token);
        }

        List<String> parts = new LinkedList<>();

        String subIndent = StringUtils.getIndent(line);
        if (subIndent.length() == 0) {
            subIndent = formattingRules.createIndent(1);
        }

        String value = token.getValue();

        // Space left for prefix and continuation character?
        if (maxLineLength - prefix.length() - line.length() - contChar.length() <= 0) {
            parts.add(line);
            line = subIndent;
        }

        int maxLength = maxLineLength - prefix.length() - line.length() - StringUtils.trimR(suffix).length();
        if (value.length() <= maxLength && value.length() <= maxNameLengthRule.apply(value.length())) {
            parts.add(line + prefix + value + StringUtils.trimR(suffix));
        } else {

            String currentLine = line;
            String remaining = value;

            boolean isFirstPart = true;
            boolean isLastPart = false;

            while (remaining.length() > 0) {

                int breakPoint;
                if (isFirstPart) {
                    // First part
                    maxLength = maxLineLength - currentLine.length() - prefix.length() - contChar.length();
                    breakPoint = strategy.findBreakpoint(remaining, maxNameLengthRule.apply(maxLength));
                    // Check minimum name length — move to a fresh line when the
                    // breakpoint is too small. Recursion is safe because the
                    // recursive call uses subIndent, making condition 1 false.
                    if (!currentLine.equals(subIndent) && !minNameLengthRule.isSatisfiedBy(breakPoint)) {
                        parts.add(currentLine);
                        currentLine = subIndent;
                        String[] nameParts = breakNameOrLiteral(currentLine, token, contChar, maxLineLength, prefix, suffix, strategy);
                        for (String namePart : nameParts) {
                            parts.add(namePart);
                        }
                        return parts.toArray(new String[0]);
                    }
                } else {
                    if (currentLine.length() + remaining.length() + suffix.length() <= maxNameLengthRule.apply(maxLineLength)) {
                        // Last parts
                        breakPoint = remaining.length();
                        isLastPart = true;
                    } else {
                        // Intermediate parts
                        maxLength = maxLineLength - currentLine.length() + -contChar.length();
                        breakPoint = strategy.findBreakpoint(remaining, maxNameLengthRule.apply(maxLength));
                    }
                }

                if (breakPoint <= 0) {
                    throw new LineOverflowException(remaining);
                }

                String part = remaining.substring(0, breakPoint);
                remaining = remaining.substring(breakPoint);

                // First part
                if (isFirstPart) {
                    part = currentLine + prefix + part + contChar;
                    parts.add(part);
                    isFirstPart = false;
                } else {
                    if (isLastPart) {
                        // Last part
                        part = currentLine + part + suffix;
                    } else {
                        // Middle parts
                        part = currentLine + part + contChar;
                    }
                    parts.add(part);
                }

                currentLine = subIndent;
            }
        }

        return parts.toArray(new String[0]);
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
        if (token.getType() == TokenType.KEYWORD) {
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
     * alignment column is determined by the longest field name that does not
     * exceed MAX_NAME_LENGTH, plus 1 space. Fields with names longer than
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

            IToken[] tokens = tokenizer.tokenize(child.getStatement());
            IToken nameToken = getNameToken(tokens);

            String[] nameParts = breakName("", nameToken, maxLineLength, "", "");
            String lastNamePart = nameParts[nameParts.length - 1];

            if (lastNamePart.length() <= maxNameLength && lastNamePart.length() > longestName) {
                longestName = lastNamePart.length();
            }
        }

        return longestName;
    }

    /**
     * Returns the <code>NAME</code> token from a the tokens of a tokenized
     * statement.
     *
     * @param tokens - tokens of a tokenized statement
     * @return the NAME token
     * @throws RpgleFormatterException
     */
    private static IToken getNameToken(IToken[] tokens) throws RpgleFormatterException {

        for (IToken token : tokens) {
            if (TokenType.NAME == token.getType()) {
                return token;
            }
        }

        throw new TokenNotFoundException(TokenType.NAME.name());
    }
}
