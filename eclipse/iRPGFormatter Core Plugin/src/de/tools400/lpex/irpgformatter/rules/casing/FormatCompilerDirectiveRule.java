/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.casing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;
import de.tools400.lpex.irpgformatter.rules.IFormattingRule;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;
import de.tools400.lpex.irpgformatter.tokenizer.IToken;
import de.tools400.lpex.irpgformatter.tokenizer.TokenType;
import de.tools400.lpex.irpgformatter.tokenizer.Tokenizer;

/**
 * Formats compiler directives, such as
 * <ul>
 * <code>/copy library/file.member</code>
 * <code>/include library/file.member</code>
 * </ul>
 * The name of the directive gets the keyword casing. The operand of
 * <code>/copy</code>, <code>/include</code>, <code>/define</code> and
 * <code>/undefine</code> and the text behind it are kept as they are, because
 * the compiler takes that text as a comment. The same applies to the text of
 * <code>/title</code>, <code>/eject</code> and <code>/space</code>. Only the
 * blanks between the parts are reduced to one.
 */
public class FormatCompilerDirectiveRule extends AbstractCasingRule implements IFormattingRule, RpgleSourceConstants {

    /**
     * Directives, whose operand is a name (source member, stream file or
     * condition name), followed by an optional comment.
     */
    private static final Set<String> NAME_OPERAND_DIRECTIVES = new HashSet<String>(Arrays.asList("COPY", "INCLUDE", "DEFINE", "UNDEFINE"));

    /**
     * Directives, whose text is no RPG code: the heading of /title and the
     * comment behind /eject and /space.
     */
    private static final Set<String> TEXT_DIRECTIVES = new HashSet<String>(Arrays.asList("TITLE", "EJECT", "SPACE"));

    public FormatCompilerDirectiveRule() {
        super();
    }

    public FormatCompilerDirectiveRule(KeywordCasingStyle keywordCasingStyle) {
        super(keywordCasingStyle);
    }

    @Override
    public String format(String directive) {

        if (directive == null || directive.isEmpty()) {
            return directive;
        }

        String trimmed = directive.trim();
        if (!trimmed.startsWith(COMPILER_DIRECTIVE)) {
            return directive;
        }

        trimmed = trimmed.substring(1);

        KeywordCasingStyle style = getKeywordCasingStyle();

        String name = getDirectiveName(trimmed);
        String rest = trimmed.substring(name.length()).trim();
        if (NAME_OPERAND_DIRECTIVES.contains(name.toUpperCase())) {
            String operand = getOperand(rest);
            String text = rest.substring(operand.length()).trim();
            return join(applyDirectiveCasing(name, style), operand, text);
        } else if (TEXT_DIRECTIVES.contains(name.toUpperCase())) {
            return join(applyDirectiveCasing(name, style), rest);
        }

        Tokenizer tokenizer = new Tokenizer();
        try {
            IToken[] tokens = tokenizer.tokenize(trimmed);
            StringBuilder buffer = new StringBuilder();
            for (IToken token : tokens) {
                TokenType type = token.getType();
                if (buffer.length() == 0 || type == TokenType.FUNCTION || type == TokenType.KEYWORD) {
                    buffer.append(applyDirectiveCasing(token.getValue(), style));
                    if (token.haveChildren()) {
                        IToken[] children = token.getChildren();
                        buffer.append(OPEN_BRACKET);
                        for (int i = 0; i < children.length; i++) {
                            buffer.append(children[i].getValue());
                            if (i < children.length - 1) {
                                buffer.append(SPACE);
                            }
                        }
                        buffer.append(CLOSE_BRACKET);
                    }
                } else {
                    buffer.append(token.getValue());
                }
                buffer.append(SPACE);
            }
            return COMPILER_DIRECTIVE + buffer.toString().trim();
        } catch (RpgleFormatterException e) {
            IRpgleFormatterPlugin.logError("Could not tokenize compiler directive: " + directive, e);
            return directive;
        }
    }

    /**
     * Returns the name of a directive without the leading slash, e.g.
     * <code>copy</code> of <code>copy qcpysrc,types</code>.
     */
    private static String getDirectiveName(String directive) {

        int i = 0;
        while (i < directive.length() && !Character.isWhitespace(directive.charAt(i))) {
            i++;
        }

        return directive.substring(0, i);
    }

    /**
     * Returns the operand at the start of a given text: a name up to the next
     * blank or a stream file name enclosed in quotes, that may contain blanks.
     * Returns an empty string, if the text starts with a comment.
     */
    private static String getOperand(String text) {

        if (text.isEmpty() || text.startsWith("//")) {
            return "";
        }

        char first = text.charAt(0);
        if (first == '\'' || first == '"') {
            int end = text.indexOf(first, 1);
            if (end > 0) {
                return text.substring(0, end + 1);
            }
            return text;
        }

        int i = 0;
        while (i < text.length() && !Character.isWhitespace(text.charAt(i))) {
            i++;
        }

        return text.substring(0, i);
    }

    /**
     * Joins the name of a directive and its non-empty parts with one blank.
     */
    private static String join(String name, String... parts) {

        StringBuilder buffer = new StringBuilder(COMPILER_DIRECTIVE).append(name);
        for (String part : parts) {
            if (!part.isEmpty()) {
                buffer.append(SPACE).append(part);
            }
        }

        return buffer.toString();
    }

    /**
     * Applies casing style to a compiler directive (e.g., /include, /copy).
     */
    private static String applyDirectiveCasing(String directive, KeywordCasingStyle style) {

        switch (style) {
        case UPPERCASE:
            return directive.toUpperCase();
        case UPPER_CAMEL:
        case FIRST_UPPER:
            // Capitalize first letter after /
            if (directive.length() > 1) {
                return Character.toUpperCase(directive.charAt(0)) + directive.substring(1).toLowerCase();
            }
            return directive;
        case LOWER_CAMEL:
        case LOWERCASE:
        default:
            return directive.toLowerCase();
        }
    }
}
