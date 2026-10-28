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
 * <p>
 * {@link StatementIdentifier#identifyStatementType(String)} tries the patterns
 * in the order of declaration. Only a <code>**FREE</code> line matches two
 * patterns, therefore <code>FREE_DIRECTIVE</code> must be declared before
 * <code>COMPILE_TIME_ARRAY</code>.
 */
public enum StatementType {

    /** Blank line */
    BLANK (null),

    /** Free-format directive (**FREE) */
    FREE_DIRECTIVE ("^\\*\\*FREE"),

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

    /* End block statements */

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

    /** End of an if group (endif) */
    ENDIF ("^\\s*endif\\b", null, false),

    /** End of a do group (enddo) */
    ENDDO ("^\\s*enddo\\b", null, false),

    /** End of a for group (endfor) */
    ENDFOR ("^\\s*endfor\\b", null, false),

    /** End of a select group (endsl) */
    ENDSL ("^\\s*endsl\\b", null, false),

    /** End of a monitor group (endmon) */
    ENDMON ("^\\s*endmon\\b", null, false),

    /** End of a subroutine (endsr) */
    ENDSR ("^\\s*endsr\\b", null, false),

    /* Start block statements */

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

    /** Start of an if group (if) */
    IF ("^\\s*if\\b", ENDIF, false),

    /** Start of a do-while group (dow) */
    DOW ("^\\s*dow\\b", ENDDO, false),

    /** Start of a do-until group (dou) */
    DOU ("^\\s*dou\\b", ENDDO, false),

    /** Start of a for-each group (for-each), must be declared before FOR */
    FOR_EACH ("^\\s*for-each\\b", ENDFOR, false),

    /** Start of a for group (for) */
    FOR ("^\\s*for\\b", ENDFOR, false),

    /** Start of a select group (select) */
    SELECT ("^\\s*select\\b", ENDSL, false),

    /** Start of a monitor group (monitor) */
    MONITOR ("^\\s*monitor\\b", ENDMON, false),

    /** Start of a subroutine (begsr) */
    BEGSR ("^\\s*begsr\\b", ENDSR, false),

    /* Branch statements, placed on the level of the start of their block */

    /** Further condition of an if group (elseif) */
    ELSEIF ("^\\s*elseif\\b", null, true),

    /** Last branch of an if group (else) */
    ELSE ("^\\s*else\\b", null, true),

    /** Branch of a select group (when-is), must be declared before WHEN */
    WHEN_IS ("^\\s*when-is\\b", null, true),

    /** Branch of a select group (when-in), must be declared before WHEN */
    WHEN_IN ("^\\s*when-in\\b", null, true),

    /** Branch of a select group (when) */
    WHEN ("^\\s*when\\b", null, true),

    /** Last branch of a select group (other) */
    SELECT_OTHER ("^\\s*other\\b", null, true),

    /** Error handler of a monitor group (on-error) */
    ON_ERROR ("^\\s*on-error\\b", null, true),

    /** Exception handler of a monitor group (on-excp) */
    ON_EXCP ("^\\s*on-excp\\b", null, true),

    /** Exit section of a procedure (on-exit) */
    ON_EXIT ("^\\s*on-exit\\b", null, true),

    /** Subfield declaration (dcl-subf) */
    DCL_SUBF (null),

    /** Compile-time data (**CTDATA, **ALTSEQ, **FTRANS) */
    COMPILE_TIME_ARRAY ("^[*]{2}(\\s*ctdata.*)?"),

    /** Other statement types (not formatted) */
    OTHER (null);

    private Pattern pattern;
    private boolean hasSubFields;
    private StatementType endOfBlockType;
    private boolean isBranchOfBlock;
    private boolean isCalculation;

    private StatementType(String pattern) {
        this(pattern, false, null, false, false);
    }

    private StatementType(String pattern, boolean hasSubFields, StatementType endOfBlockType) {
        this(pattern, hasSubFields, endOfBlockType, false, false);
    }

    /**
     * Creates a calculation statement type.
     *
     * @param pattern - pattern of the operation code
     * @param endOfBlockType - type of the statement that ends the block, or
     *        <code>null</code>, if the statement does not start a block
     * @param isBranchOfBlock - <code>true</code> for a branch of a block, such
     *        as <code>ELSE</code>
     */
    private StatementType(String pattern, StatementType endOfBlockType, boolean isBranchOfBlock) {
        this(pattern, false, endOfBlockType, isBranchOfBlock, true);
    }

    private StatementType(String pattern, boolean hasSubFields, StatementType endOfBlockType, boolean isBranchOfBlock,
        boolean isCalculation) {

        this.pattern = pattern != null ? Pattern.compile(pattern, Pattern.CASE_INSENSITIVE) : null;
        this.hasSubFields = hasSubFields;
        this.endOfBlockType = endOfBlockType;
        this.isBranchOfBlock = isBranchOfBlock;
        this.isCalculation = isCalculation;
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

    /**
     * Returns <code>true</code> for a branch of a block, such as
     * <code>ELSE</code>, <code>WHEN</code> or <code>ON-ERROR</code>. A branch
     * is placed on the level of the statement that starts the block.
     */
    public boolean isBranchOfBlock() {
        return isBranchOfBlock;
    }

    /**
     * Returns <code>true</code> for an operation code of the calculations, such
     * as <code>IF</code> or <code>ENDIF</code>.
     */
    public boolean isCalculation() {
        return isCalculation;
    }
}
