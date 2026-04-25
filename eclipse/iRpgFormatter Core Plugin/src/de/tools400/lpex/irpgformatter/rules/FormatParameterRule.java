/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules;

import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.ParameterSpacingStyle;

public class FormatParameterRule implements RpgleSourceConstants {

    private final FormatterConfig config;

    public FormatParameterRule(FormatterConfig config) {
        this.config = config;
    }

    public String format(String parameter, int i, int length) {

        boolean addColonBeforeParameter = config.isDelimiterBeforeParameter();
        ParameterSpacingStyle parameterSpacingStyle = config.getParameterSpacingStyle();

        boolean isFirstParameter = (i == 0);
        boolean isLastParameter = (i == length - 1);

        StringBuilder result = new StringBuilder();

        if (addColonBeforeParameter) {
            if (!isFirstParameter) {
                result.append(COLON);
                if (parameterSpacingStyle == ParameterSpacingStyle.BEFORE || parameterSpacingStyle == ParameterSpacingStyle.BOTH) {
                    result.append(SPACE);
                }
            } else if (parameterSpacingStyle == ParameterSpacingStyle.BOTH) {
                result.append(SPACE);
            }
            result.append(parameter);
            if (!isLastParameter) {
                if (parameterSpacingStyle == ParameterSpacingStyle.AFTER || parameterSpacingStyle == ParameterSpacingStyle.BOTH) {
                    result.append(SPACE);
                }
            } else if (parameterSpacingStyle == ParameterSpacingStyle.BOTH) {
                result.append(SPACE);
            }
        } else {
            if (!isFirstParameter) {
                if (parameterSpacingStyle == ParameterSpacingStyle.BEFORE || parameterSpacingStyle == ParameterSpacingStyle.BOTH) {
                    result.append(SPACE);
                }
            } else if (parameterSpacingStyle == ParameterSpacingStyle.BOTH) {
                result.append(SPACE);
            }
            result.append(parameter);
            if (!isLastParameter) {
                if (parameterSpacingStyle == ParameterSpacingStyle.AFTER || parameterSpacingStyle == ParameterSpacingStyle.BOTH) {
                    result.append(SPACE);
                }
                result.append(COLON);
            } else if (parameterSpacingStyle == ParameterSpacingStyle.BOTH) {
                result.append(SPACE);
            }
        }

        return result.toString();
    }
}
