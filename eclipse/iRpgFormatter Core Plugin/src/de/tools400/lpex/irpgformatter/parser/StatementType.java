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
 * Enumeration of RPGLE statement types that the formatter handles.
 */
public enum StatementType {

    /** Blank line */
    BLANK (null),

    /** Free-format directive (**FREE) */
    FREE_DIRECTIVE ("^\\s*\\*\\*FREE\\b"),

    /** Comment line */
    COMMENT ("^\\s*//"),

    /** Compiler directive (/if, /define, /copy, etc.) */
    COMPILER_DIRECTIVE ("^\\s*/[a-zA-Z]"),

    /** Control options (ctl-opt) */
    CTL_OPT ("^\\s*ctl-opt\\b"),

    /** File declaration (dcl-f) */
    DCL_F ("^\\s*dcl-f\\b"),

    /** Constant declaration (dcl-c) */
    DCL_C ("^\\s*dcl-c\\b"),

    /** Standalone variable (dcl-s) */
    DCL_S ("^\\s*dcl-s\\b"),

    /** End of data structure (end-ds) */
    END_DS ("^\\s*end-ds\\b"),

    /** End of procedure prototype (end-pr) */
    END_PR ("^\\s*end-pr\\b"),

    /** End of procedure interface (end-pi) */
    END_PI ("^\\s*end-pi\\b"),

    /** End of procedure (end-proc) */
    END_PROC ("^\\s*end-proc\\b"),

    /** End of enumeration (end-enum) */
    END_ENUM ("^\\s*end-enum\\b"),

    /** Start of data structure (dcl-ds) */
    DCL_DS ("^\\s*dcl-ds\\b", true, END_DS),

    /** Start of procedure prototype (dcl-pr) */
    DCL_PR ("^\\s*dcl-pr\\b", true, END_PR),

    /** Start of procedure interface (dcl-pi) */
    DCL_PI ("^\\s*dcl-pi\\b", true, END_PI),

    /** Start of procedure (dcl-proc) */
    DCL_PROC ("^\\s*dcl-proc\\b", false, END_PROC),

    /** Start of enumeration (dcl-enum) */
    DCL_ENUM ("^\\s*dcl-enum\\b", true, END_ENUM),

    /** Subfield declaration (dcl-subf) */
    DCL_SUBF (null),

    /** Other statement types (not formatted) */
    OTHER (null);

    private Pattern pattern;
    private boolean hasSubFields;
    private StatementType endOfBlockType;

    private StatementType(String pattern) {
        this(pattern, false, null);
    }

    private StatementType(String pattern, boolean hasSubFields, StatementType endOfBlockType) {

        this.pattern = pattern != null ? Pattern.compile(pattern, Pattern.CASE_INSENSITIVE) : null;
        this.hasSubFields = hasSubFields;
        this.endOfBlockType = endOfBlockType;
    }

    public Pattern pattern() {
        return pattern;
    }

    public boolean hasSubFields() {
        return hasSubFields;
    }

    public boolean isStartOfBLock() {
        return endOfBlockType != null;
    }

    public StatementType getEndOfBlockType() {
        return endOfBlockType;
    }
}
