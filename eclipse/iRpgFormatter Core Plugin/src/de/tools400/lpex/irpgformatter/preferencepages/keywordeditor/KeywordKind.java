/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages.keywordeditor;

import de.tools400.lpex.irpgformatter.Messages;

/**
 * The kind of the entries of a keyword table. It defines the examples of the
 * dialog and the character, that a key must start with.
 */
public enum KeywordKind {

    /** Data type, declaration type or keyword, such as DCL-DS */
    KEYWORD ("DCL-DS", "Dcl-Ds", null),

    /** Special word, such as *NOPASS */
    SPECIAL_WORD ("*NOPASS", "*NoPass", "*"),

    /** Operation code, such as EVAL-CORR */
    OPERATION_CODE ("EVAL-CORR", "Eval-Corr", null),

    /** Built-in function, such as %SUBDT */
    BUILT_IN_FUNCTION ("%SUBDT", "%SubDt", "%");

    private final String exampleKey;
    private final String exampleValue;
    private final String requiredPrefix;

    private KeywordKind(String exampleKey, String exampleValue, String requiredPrefix) {
        this.exampleKey = exampleKey;
        this.exampleValue = exampleValue;
        this.requiredPrefix = requiredPrefix;
    }

    public String getExampleKey() {
        return exampleKey;
    }

    public String getExampleValue() {
        return exampleValue;
    }

    /**
     * Returns the character, that a key must start with, or <code>null</code>.
     */
    public String getRequiredPrefix() {
        return requiredPrefix;
    }

    /**
     * Returns the error message for a key without the required prefix, or
     * <code>null</code>, if there is no required prefix.
     */
    public String getPrefixErrorMessage() {
        switch (this) {
        case SPECIAL_WORD:
            return Messages.Error_Keyword_parameters_must_start_with_A;
        case BUILT_IN_FUNCTION:
            return Messages.Error_Built_in_functions_must_start_with_A;
        default:
            return null;
        }
    }
}
