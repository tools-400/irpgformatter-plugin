/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferences;

import de.tools400.lpex.irpgformatter.Messages;

/**
 * The outer parentheses of the condition of an <code>IF</code>,
 * <code>ELSEIF</code>, <code>WHEN</code>, <code>DOW</code> or <code>DOU</code>
 * statement.
 */
public enum ConditionParentheses {

    /** The parentheses are left unchanged. */
    OFF,

    /** The condition is enclosed in parentheses: {@code if (a = 1);} */
    WITH_PARENTHESES,

    /** The condition is not enclosed in parentheses: {@code if a = 1;} */
    WITHOUT_PARENTHESES;

    /**
     * Gets the display name for this style. The name is resolved dynamically
     * from NLS messages so it always reflects the active locale.
     *
     * @return the display name
     */
    public String getDisplayName() {
        switch (this) {
        case OFF:
            return Messages.Label_Condition_parentheses_Off;
        case WITH_PARENTHESES:
            return Messages.Label_Condition_parentheses_With_parentheses;
        case WITHOUT_PARENTHESES:
            return Messages.Label_Condition_parentheses_Without_parentheses;
        default:
            return name();
        }
    }

    /**
     * Gets a style by its display name.
     *
     * @param label the UI label
     * @return the style, or the default if not found
     */
    public static ConditionParentheses fromLabel(String label) {
        if (label != null) {
            for (ConditionParentheses style : values()) {
                if (style.getDisplayName().equals(label)) {
                    return style;
                }
            }
        }
        return Preferences.getInstance().getDefaultConditionParentheses();
    }
}
