/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.rules.statements;

import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.ParameterSpacingStyle;
import de.tools400.lpex.irpgformatter.rules.RpgleSourceConstants;

public class FormatParameterRule implements RpgleSourceConstants {

    private final FormatterConfig config;

    public FormatParameterRule(FormatterConfig config) {
        this.config = config;
    }

    /**
     * Computes the delimiter parts (prefix and suffix) that surround the
     * parameter at position {@code i} of {@code length} parameters. Use this
     * when handling line wrapping — the suffix stays on the previous line
     * (after a trim) and the prefix moves to the next line.
     */
    public Delimiter delimiterFor(int i, int length) {

        boolean addColonBeforeParameter = config.isDelimiterBeforeParameter();
        ParameterSpacingStyle parameterSpacingStyle = config.getParameterSpacingStyle();
        boolean isBoth = parameterSpacingStyle == ParameterSpacingStyle.BOTH;
        boolean spaceBeforeParameter = parameterSpacingStyle == ParameterSpacingStyle.BEFORE || isBoth;
        boolean spaceAfterParameter = parameterSpacingStyle == ParameterSpacingStyle.AFTER || isBoth;

        boolean isFirstParameter = (i == 0);
        boolean isLastParameter = (i == length - 1);

        StringBuilder prefix = new StringBuilder();
        StringBuilder suffix = new StringBuilder();

        if (addColonBeforeParameter) {
            if (!isFirstParameter) {
                prefix.append(COLON);
                if (spaceBeforeParameter) {
                    prefix.append(SPACE);
                }
            } else if (isBoth) {
                // BOTH pads the start of the first parameter.
                prefix.append(SPACE);
            }
            if (!isLastParameter) {
                if (spaceAfterParameter) {
                    suffix.append(SPACE);
                }
            } else if (isBoth) {
                // BOTH pads the end of the last parameter.
                suffix.append(SPACE);
            }
        } else {
            if (!isFirstParameter) {
                if (spaceBeforeParameter) {
                    prefix.append(SPACE);
                }
            } else if (isBoth) {
                // BOTH pads the start of the first parameter.
                prefix.append(SPACE);
            }
            if (!isLastParameter) {
                if (spaceAfterParameter) {
                    suffix.append(SPACE);
                }
                suffix.append(COLON);
            } else if (isBoth) {
                // BOTH pads the end of the last parameter.
                suffix.append(SPACE);
            }
        }

        return new Delimiter(prefix.toString(), suffix.toString());
    }

    /**
     * Returns the parameter wrapped with its delimiters as a single string.
     * Use this on the single-line code path; use
     * {@link #delimiterFor(int, int)} when you need to handle line wrapping.
     */
    public String format(String parameter, int i, int length) {
        Delimiter delimiter = delimiterFor(i, length);
        return delimiter.getPrefix() + parameter + delimiter.getSuffix();
    }

    /**
     * The delimiter parts (prefix and suffix) that surround a parameter on a
     * single line. On a line wrap the suffix stays on the previous line and
     * the prefix moves to the next line.
     */
    public static final class Delimiter {

        private final String prefix;
        private final String suffix;

        public Delimiter(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }
    }
}
