/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.actions;

import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.input.IRpgleOutput;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.utils.IBMRpgleFormatterUtils;
import de.tools400.lpex.irpgformatter.utils.LpexViewUtils;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

/**
 * Formats an RPGLE source in the given {@link LpexView} silently. Used by the
 * format-on-save hook registered in {@code Preload}.
 * <p>
 * All exceptions are caught so that a formatting failure never prevents saving.
 */
public final class FormatOnSaveListener {

    private FormatOnSaveListener() {
    }

    /**
     * Formats the source in the given view if it is an RPGLE free-format source
     * and the "Format on save" preference is enabled.
     */
    public static void formatQuietly(LpexView view) {

        try {

            if (!Preferences.getInstance().isFormatOnSave()) {
                return;
            }

            if (LpexViewUtils.isReadOnly(view)) {
                return;
            }

            IRpgleInput input = RpgleInputFactory.createFromAll(view);

            String validationError = RpgleFormatter.validateInput(input);
            if (validationError != null) {
                return;
            } else {
                executeFormatter(view, input);
            }

        } catch (Throwable e) {
            IRpgleFormatterPlugin.logError("Format on save failed", e);
        }
    }

    private static void executeFormatter(LpexView view, IRpgleInput input) throws RpgleFormatterException {

        RpgleFormatter formatter = new RpgleFormatter();
        int sourceLength = LpexViewUtils.getMaxLineLength(view);
        formatter.setSourceLength(sourceLength);

        int defaultIndent = Preferences.getInstance().getStartColumn() - 1;

        if (Preferences.getInstance().isExecuteIbmFormatter()) {
            IBMRpgleFormatterUtils.executeIBMFormatter(view);
            String firstLine = LpexViewUtils.getElementText(view, 1);
            defaultIndent = StringUtils.getIndent(firstLine).length();
        }

        if (Preferences.getInstance().isExecuteIrpgFormatter()) {
            FormattedResult result = formatter.format(input, defaultIndent);
            IRpgleOutput output = input.getOutput();
            output.writeSourceLines(result);
        }
    }
}
