/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.parser;

import java.util.regex.Pattern;

/**
 * Enumeration of the compiler directives that control which source lines the
 * compiler reads.
 * <p>
 * {@link #of(String)} tries the patterns in the order of declaration, therefore
 * <code>OTHER</code> must be declared after the specific directives.
 */
public enum DirectiveType {

    /** Start of a conditional group (/if) */
    IF ("^\\s*/if\\b"),

    /** Further condition of a conditional group (/elseif) */
    ELSEIF ("^\\s*/elseif\\b"),

    /** Last branch of a conditional group (/else) */
    ELSE ("^\\s*/else\\b"),

    /** End of a conditional group (/endif) */
    ENDIF ("^\\s*/endif\\b"),

    /** End of the source member (/eof) */
    EOF ("^\\s*/eof\\b"),

    /** Other compiler directive (/copy, /define, etc.) */
    OTHER ("^\\s*/[a-zA-Z]"),

    /** No compiler directive */
    NONE (null);

    private Pattern pattern;

    private DirectiveType(String pattern) {
        this.pattern = pattern != null ? Pattern.compile(pattern, Pattern.CASE_INSENSITIVE) : null;
    }

    /**
     * Returns the type of the compiler directive of a given statement.
     *
     * @param statement - statement or source line
     * @return directive type or {@link #NONE}, if the statement is not a
     *         compiler directive
     */
    public static DirectiveType of(String statement) {

        if (statement != null) {
            for (DirectiveType type : values()) {
                if (type.pattern != null && type.pattern.matcher(statement).find()) {
                    return type;
                }
            }
        }

        return NONE;
    }
}
