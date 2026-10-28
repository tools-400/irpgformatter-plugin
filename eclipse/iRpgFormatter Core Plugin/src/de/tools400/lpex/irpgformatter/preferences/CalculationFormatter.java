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
 * The formatter of the calculations, of the comment lines in front of them and
 * of the embedded SQL statements.
 */
public enum CalculationFormatter {

    /** The calculations are left unchanged. */
    OFF,

    /**
     * The IBM formatter formats the calculations before iRPGFormatter runs,
     * which leaves them unchanged.
     */
    IBM_FORMATTER,

    /** iRPGFormatter formats the calculations. */
    IRPG_FORMATTER;

    /**
     * Gets the display name for this formatter. The name is resolved
     * dynamically from NLS messages so it always reflects the active locale.
     *
     * @return the display name
     */
    public String getDisplayName() {
        switch (this) {
        case OFF:
            return Messages.Label_Calculation_formatter_Off;
        case IBM_FORMATTER:
            return Messages.Label_Calculation_formatter_IBM_formatter;
        case IRPG_FORMATTER:
            return Messages.Label_Calculation_formatter_iRPGFormatter;
        default:
            return name();
        }
    }

    /**
     * Gets a formatter by its display name.
     *
     * @param label the UI label
     * @return the formatter, or the default if not found
     */
    public static CalculationFormatter fromLabel(String label) {
        if (label != null) {
            for (CalculationFormatter formatter : values()) {
                if (formatter.getDisplayName().equals(label)) {
                    return formatter;
                }
            }
        }
        return Preferences.getInstance().getDefaultFormatCalculations();
    }
}
