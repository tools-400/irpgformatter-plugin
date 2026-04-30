/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.tokenizer;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.parser.StatementType;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

/**
 * Parses a given string into tokens like:
 * <ul>
 * <li>DCL - DCL-*</li>
 * <li>NAME - myProcName</li>
 * <li>LITERAL - 'my literal'</li>
 * <li>FUNCTION - function(...)</li>
 * <li>SPECIAL_WORD - function(parameter1: parameter2: literal: ...)</li>
 * </ul>
 * Function parameters are stored as children of a function. Example parameters:
 * <ul>
 * <li>Keywords: DATE(*ISO), ALWNULL(*USRCTL), OPTIONS(*STRING)</li>
 * <li>Literals: COPYRIGHT('Copyright 2026, My Company')</li>
 * <li>Names: EXTPROC(pProcPointer)</li>
 * </ul>
 */
public class Tokenizer implements RpgleSourceConstants {

    private final FormatterConfig config;

    public Tokenizer() {
        this(FormatterConfig.fromPreferences());
    }

    public Tokenizer(FormatterConfig config) {
        this.config = config;
    }

    public IToken[] tokenize(String value) throws RpgleFormatterException {
        return tokenize(value, null);
    }

    /**
     * Tokenizes <code>value</code> with awareness of the surrounding statement
     * type. Some token positions in RPGLE are unambiguously names (e.g. the
     * first token of a sub-field — or the second token, if the optional
     * <code>DCL-SUBF</code> prefix is present — or the second token of a
     * <code>dcl-*</code> statement). Without context, an identifier that
     * happens to match a reserved keyword (such as <code>CCSID</code>) would
     * be misclassified as <code>KEYWORD</code>. Passing the statement type
     * lets the tokenizer force a <code>NAME</code> classification at those
     * positions.
     *
     * @param value the source text of the statement
     * @param context the statement type the line was identified as; pass
     *        <code>null</code> (or {@link StatementType#OTHER}) to disable
     *        forced-name handling
     */
    public IToken[] tokenize(String value, StatementType context) throws RpgleFormatterException {

        List<IToken> tokens = new LinkedList<>();

        String remaining = value.trim();

        TokenType tokenType = classifyToken(remaining, context, tokens);
        IToken token = null;
        int offset = 0;

        int lengthBefore = remaining.length();
        while (remaining.length() > 0) {
            if (tokenType == TokenType.CTL) {
                token = parseControl(remaining, offset);
            } else if (tokenType == TokenType.DCL) {
                token = parseDeclaration(remaining, offset);
            } else if (tokenType == TokenType.NAME) {
                token = parseName(remaining, offset);
            } else if (tokenType == TokenType.SPECIAL_WORD) {
                token = parseSpecialWord(remaining, offset);
            } else if (tokenType == TokenType.LITERAL) {
                token = parseLiteral(remaining, offset);
            } else if (tokenType == TokenType.DATA_TYPE) {
                token = parseVariable(remaining, offset);
            } else if (tokenType == TokenType.FUNCTION) {
                token = parseFunction(remaining, offset);
            } else if (tokenType == TokenType.KEYWORD) {
                token = parseKeyword(remaining, offset);
            } else if (tokenType == TokenType.EOL) {
                token = parseEndOfLine(remaining, offset);
            } else if (tokenType == TokenType.COMMENT) {
                token = parseComment(remaining, offset);
            } else if (tokenType == TokenType.OTHER) {
                token = parseOther(remaining, offset);
            } else {
                throw new RpgleFormatterException("Unknown token type: " + tokenType.name());
            }
            if (token != null) {

                offset = offset + token.getRawLength();
                remaining = remaining.substring(token.getRawLength()).trim();

                tokens.add(token);

                if (lengthBefore == remaining.length()) {
                    throw new RpgleFormatterException("Endless loop in tokenizer detected.");
                }

                token = null;
                tokenType = classifyToken(remaining, context, tokens);
            } else {
                throw new RpgleFormatterException("No token found!");
            }
        }

        return tokens.toArray(new IToken[0]);
    }

    private IToken parseControl(String line, int offset) {

        int i = findEndOfToken(line);

        String value = line.substring(0, i);

        int rawLength = value.length();
        rawLength = skipSpaceAndColon(line.substring(i)) + i;
        IToken token = new CtlToken(value, line.substring(0, rawLength), offset);

        return token;
    }

    private IToken parseDeclaration(String line, int offset) {

        int i = findEndOfToken(line);

        String value = line.substring(0, i);

        int rawLength = value.length();
        rawLength = skipSpaceAndColon(line.substring(i)) + i;
        IToken token = new DeclToken(value, line.substring(0, rawLength), offset);

        return token;
    }

    private IToken parseName(String line, int offset) {

        int i = findEndOfToken(line);

        String value = line.substring(0, i);

        int rawLength = value.length();
        rawLength = skipSpaceAndColon(line.substring(i)) + i;
        IToken token = new NameToken(value, line.substring(0, rawLength), offset);

        return token;
    }

    private IToken parseOther(String line, int offset) {

        int i = findEndOfToken(line);

        String value = line.substring(0, i);

        int rawLength = value.length();
        rawLength = skipSpaceAndColon(line.substring(i)) + i;
        IToken token = new OtherToken(value, line.substring(0, rawLength), offset);

        return token;
    }

    private IToken parseSpecialWord(String line, int offset) {

        int i = findEndOfToken(line);

        String value = line.substring(0, i);

        int rawLength = value.length();
        rawLength = skipSpaceAndColon(line.substring(i)) + i;
        IToken token = new SpecialWordToken(value, line.substring(0, rawLength), offset);

        return token;
    }

    private int findEndOfToken(String line) {

        String currentChar;
        int i;
        for (i = 0; i < line.length(); i++) {
            currentChar = line.substring(i, i + 1);
            if (SPACE.equals(currentChar)) {
                i++;
                break;
            } else if (COLON.equals(currentChar) || OPEN_BRACKET.equals(currentChar) || SEMICOLON.equals(currentChar)) {
                break;
            }
        }
        return i;
    }

    private IToken parseLiteral(String line, int offset) {

        String currentChar = null;
        String nextChar = null;

        boolean insideLiteral = false;

        int i;
        for (i = 0; i < line.length(); i++) {
            currentChar = line.substring(i, i + 1);
            if (i < line.length() - 1) {
                nextChar = line.substring(i + 1, i + 2);
            } else {
                nextChar = null;
            }
            if (SINGLE_QUOTE.equals(currentChar)) {
                if (SINGLE_QUOTE.equals(nextChar)) {
                    // skip second single quote in a row
                    i++;
                } else {
                    insideLiteral = !insideLiteral;
                }
                if (!insideLiteral) {
                    i++;
                    break;
                }
            }
        }

        String value = line.substring(0, i);

        int rawLength = value.length();
        rawLength = skipSpaceAndColon(line.substring(i)) + i;
        IToken token = new LiteralToken(value, line.substring(0, rawLength), offset);

        return token;
    }

    private IToken parseVariable(String line, int offset) throws RpgleFormatterException {
        return parseTokenWithArgs(line, offset, TokenType.DATA_TYPE);
    }

    private IToken parseFunction(String line, int offset) throws RpgleFormatterException {
        return parseTokenWithArgs(line, offset, TokenType.FUNCTION);
    }

    private IToken parseKeyword(String line, int offset) throws RpgleFormatterException {
        return parseTokenWithArgs(line, offset, TokenType.KEYWORD);
    }

    private IToken parseTokenWithArgs(String line, int offset, TokenType type) throws RpgleFormatterException {

        String name = null;
        StringBuilder parameters = new StringBuilder();
        boolean insideParameters = false;
        // boolean supportsNoArgs = (type != TokenType.FUNCTION);

        String currentChar;

        int i;
        for (i = 0; i < line.length(); i++) {

            currentChar = line.substring(i, i + 1);

            if (OPEN_BRACKET.equals(currentChar)) {
                // Get keyword name and start collecting parameters
                name = line.substring(0, i);
                insideParameters = true;
                continue;
            }

            if (insideParameters) {
                if (CLOSE_BRACKET.equals(currentChar)) {
                    i++;
                    break;
                } else {
                    // Collect keyword parameters
                    parameters.append(currentChar);
                }
            }

            // Keyword without parameters
            if (!insideParameters && (SPACE.equals(currentChar) || SEMICOLON.equals(currentChar))) {
                name = line.substring(0, i);
                break;
            }
        }

        int rawLength = skipSpaceAndColon(line.substring(i)) + i;
        String rawValue = line.substring(0, rawLength);

        IToken token = createToken(type, name, rawValue, offset);

        if (parameters.length() > 0) {
            IToken[] argTokens = tokenize(parameters.toString());
            for (IToken argToken : argTokens) {
                token.addChild(argToken);
            }
        }

        return token;
    }

    private IToken createToken(TokenType type, String value, String rawValue, int offset) {
        if (type == TokenType.KEYWORD) {
            return new KeywordToken(value, rawValue, offset);
        } else if (type == TokenType.DATA_TYPE) {
            return new DataTypeToken(value, rawValue, offset);
        } else {
            return new FunctionToken(value, rawValue, offset);
        }
    }

    private IToken parseComment(String line, int offset) {

        IToken token = new CommentToken(line.trim(), line, offset);

        return token;
    }

    private IToken parseEndOfLine(String line, int offset) {

        String currentChar = null;

        int i;
        for (i = 0; i < line.length(); i++) {
            currentChar = line.substring(i, i + 1);
            if (SEMICOLON.equals(currentChar)) {
                i++;
                break;
            }
        }

        String value = line.substring(0, i);
        int rawLength = skipSpaceAndColon(line.substring(i)) + i;
        IToken token = new EolToken(value, line.substring(0, rawLength), offset);

        return token;
    }

    private int skipSpaceAndColon(String line) {

        String currentChar;
        for (int i = 0; i < line.length(); i++) {
            currentChar = line.substring(i, i + 1);
            if (!SPACE.equals(currentChar) && !COLON.equals(currentChar)) {
                return i;
            }
        }

        return line.length();
    }

    private TokenType classifyToken(String line, StatementType context, List<IToken> previousTokens) {

        if (StringUtils.isNullOrEmpty(line)) {
            return null;
        }

        if (line.toUpperCase().startsWith("CTL-OPT")) {
            return TokenType.CTL;
        } else if (line.toUpperCase().startsWith("DCL-") || line.toUpperCase().startsWith("END-")) {
            return TokenType.DCL;
        } else if (line.startsWith(COMMENT)) {
            return TokenType.COMMENT;
        } else if (checkEndOfLine(line)) {
            return TokenType.EOL;
        } else if (checkLiteral(line)) {
            return TokenType.LITERAL;
        } else if (checkDateTime(line)) {
            return TokenType.DATE_TIME_LITERAL;
        } else if (checkSpecialWord(line)) {
            return TokenType.SPECIAL_WORD;
        }

        // At positions where RPGLE syntax requires a name (e.g. the first
        // token of a sub-field, or the second token of a dcl-* statement),
        // force NAME classification when the text matches a name pattern.
        // This prevents identifiers that happen to match a reserved keyword
        // (such as CCSID) from being misclassified as KEYWORD.
        if (mustBeNameToken(context, previousTokens) && checkNameToken(line)) {
            return TokenType.NAME;
        }

        if (checkKeyword(line)) {
            return TokenType.KEYWORD;
        } else if (checkDataType(line)) {
            return TokenType.DATA_TYPE;
        } else if (checkFunction(line)) {
            return TokenType.FUNCTION;
        } else if (checkNameToken(line)) {
            return TokenType.NAME;
        }

        // Tokens of type:
        // -> numeric values of data types

        return TokenType.OTHER;
    }

    /**
     * Returns <code>true</code> if the next token to be classified within a
     * statement of type <code>context</code> must be a NAME token according to
     * RPGLE syntax — regardless of whether the text happens to match a
     * reserved keyword. The decision is based on the tokens that have already
     * been collected for the current statement.
     */
    private boolean mustBeNameToken(StatementType context, List<IToken> previousTokens) {

        if (context == null) {
            return false;
        }

        int tokenIndex = previousTokens.size();

        switch (context) {
        case DCL_SUBF:
            // Sub-fields start with the field name. An optional leading
            // DCL-* token (e.g. `DCL-SUBF`) shifts the name to the next
            // position.
            if (tokenIndex == 0) {
                return true;
            }
            if (tokenIndex == 1 && previousTokens.get(0).getType() == TokenType.DCL) {
                return true;
            }
            return false;
        case DCL_S:
        case DCL_F:
        case DCL_C:
        case DCL_DS:
        case DCL_PR:
        case DCL_PI:
        case DCL_PROC:
        case DCL_ENUM:
            // dcl-<kind> <name> ... — the name is the second token.
            return tokenIndex == 1;
        default:
            return false;
        }
    }

    private boolean checkDateTime(String line) {

        if (line.length() == 0) {
            return false;
        }

        if (line.matches("^(d|t|z')'")) {
            return true;
        }

        return false;
    }

    private boolean checkEndOfLine(String line) {

        if (line.length() == 0) {
            return false;
        }

        if (line.startsWith(SEMICOLON)) {
            return true;
        }

        return false;
    }

    private boolean checkLiteral(String line) {

        if (line.length() == 0) {
            return false;
        }

        if (line.startsWith(SINGLE_QUOTE)) {
            return true;
        }

        return false;
    }

    private boolean checkNameToken(String line) {

        // The first character of the name must be alphabetic. This includes the
        // characters $, #, and @.
        // @ is § on German boxes.
        final String firstChars = "$#@§a-z";

        // The remaining characters must be alphabetic or numeric. This includes
        // the underscore (_).
        final String otherChars = firstChars + "_0-9";

        if (line.length() == 0) {
            return false;
        }

        int i = findEndOfToken(line);

        // Check first character
        String pattern = String.format("(?i)^(?:(?:[%s]{1})(?:[%s]*))$", firstChars, otherChars);
        String name = StringUtils.trimR(line.substring(0, i));
        if (name.matches(pattern)) {
            return true;
        }

        return false;
    }

    private boolean checkSpecialWord(String line) {

        if (!line.startsWith(ASTERISK)) {
            return false;
        }

        String specialWord = line;

        String currentChar;
        for (int i = 0; i < line.length(); i++) {
            currentChar = line.substring(i, i + 1);
            if (SPACE.equals(currentChar) || COLON.equals(currentChar) || CLOSE_BRACKET.equals(currentChar) || SEMICOLON.equals(currentChar)) {
                specialWord = line.substring(0, i);
                break;
            }
        }

        Set<String> specialWordKeys = config.getSpecialWords().keySet();
        if (specialWordKeys.contains(specialWord.toUpperCase())) {
            return true;
        }

        return false;
    }

    private boolean checkFunction(String line) {

        String function = line;

        String currentChar;
        for (int i = 0; i < line.length(); i++) {
            currentChar = line.substring(i, i + 1);
            if (SPACE.equals(currentChar) || SEMICOLON.equals(currentChar)) {
                // function must have parameters
                return false;
            } else if (OPEN_BRACKET.equals(currentChar)) {
                function = line.substring(0, i);
                Set<String> keywordKeys = config.getKeywords().keySet();
                if (!keywordKeys.contains(function.toUpperCase())) {
                    return true;
                }
                break;
            }
        }

        return false;
    }

    private boolean checkKeyword(String line) {

        String keyword = line;

        String currentChar;
        for (int i = 0; i < line.length(); i++) {
            currentChar = line.substring(i, i + 1);
            if (SPACE.equals(currentChar) || SEMICOLON.equals(currentChar)) {
                // keywords without parameters, e.g. 'nomain' or 'qualified'
                keyword = line.substring(0, i);
                break;
            } else if (OPEN_BRACKET.equals(currentChar)) {
                keyword = line.substring(0, i);
                break;
            }
        }

        Set<String> keywordKeys = config.getKeywords().keySet();
        if (keywordKeys.contains(keyword.toUpperCase())) {
            return true;
        }

        return false;
    }

    private boolean checkDataType(String line) {

        String dataType = line;

        String currentChar;
        for (int i = 0; i < line.length(); i++) {
            currentChar = line.substring(i, i + 1);
            if (SPACE.equals(currentChar) || SEMICOLON.equals(currentChar)) {
                // data types without parameters, e.g. 'ind' or 'float'
                dataType = line.substring(0, i);
                break;
            } else if (OPEN_BRACKET.equals(currentChar)) {
                dataType = line.substring(0, i);
                break;
            }
        }

        Set<String> dataTypeKeys = config.getDataTypes().keySet();
        if (dataTypeKeys.contains(dataType.toUpperCase())) {
            return true;
        }

        return false;
    }
}
