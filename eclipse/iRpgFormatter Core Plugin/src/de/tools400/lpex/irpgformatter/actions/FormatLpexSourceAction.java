/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.actions;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

import com.ibm.lpex.core.LpexAction;
import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FormatError;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.input.IRpgleOutput;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;
import de.tools400.lpex.irpgformatter.input.RpgleLpexInput;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.utils.IBMRpgleFormatterUtils;
import de.tools400.lpex.irpgformatter.utils.LpexViewUtils;
import de.tools400.lpex.irpgformatter.utils.StringUtils;
import de.tools400.lpex.irpgformatter.utils.UIUtils;

public class FormatLpexSourceAction implements LpexAction {

    public static final String ID = "iRPGFormatter.Format";

    private int line;
    private int position;

    public FormatLpexSourceAction() {
    }

    @Override
    public boolean available(LpexView view) {
        return !LpexViewUtils.isReadOnly(view);
    }

    @Override
    public void doAction(LpexView view) {

        try {

            boolean haveSelection = LpexViewUtils.hasBlockSelection(view);

            // Use selection-aware input factory - formats only selected lines
            // if there is a selection, otherwise formats the entire source
            IRpgleInput input;
            int startLine;
            int endLine;
            int defaultIndent;
            if (haveSelection) {
                input = RpgleInputFactory.createFromSelection(view);
                startLine = ((RpgleLpexInput)input).getStartLine();
                endLine = ((RpgleLpexInput)input).getEndLine();
                // Extend selected lines to logical block
                LpexViewUtils.selectBlockRange(view, startLine, endLine);
                // Use current indent of first line
                defaultIndent = getIndent(view, startLine);
            } else {
                input = RpgleInputFactory.createFromAll(view);
                startLine = 1;
                endLine = -1;
                // Use indent of preferences
                defaultIndent = Preferences.getInstance().getStartColumn() - 1;
            }

            String validationError = RpgleFormatter.validateInput(input);
            if (validationError != null) {
                MessageDialog.openError(UIUtils.getShell(), Messages.E_R_R_O_R, validationError);
                return;
            } else {
                executeFormatter(view, input, haveSelection, startLine, defaultIndent);
            }

        } catch (RpgleFormatterException e) {
            LpexViewUtils.displayMessage(view, Messages.bind(Messages.Error_Format_failed_A, e.getMessage()));
            e.printStackTrace();
        } catch (Throwable e) {
            LpexViewUtils.displayMessage(view, Messages.bind(Messages.Error_Unexpected_error_A, e.getMessage()));
            e.printStackTrace();
        }
    }

    private void executeFormatter(LpexView view, IRpgleInput input, boolean haveSelection, int startLine, int defaultIndent)
        throws RpgleFormatterException {

        boolean executeIbmFormatter = Preferences.getInstance().isExecuteIbmFormatter();
        boolean executeIrpgFormatter = Preferences.getInstance().isExecuteIrpgFormatter();

        RpgleFormatter formatter = new RpgleFormatter();
        int sourceLength = LpexViewUtils.getMaxLineLength(view);
        formatter.setSourceLength(sourceLength);

        int endLine = -1;
        saveCursorPosition(view);

        if (executeIbmFormatter) {
            IBMRpgleFormatterUtils.executeIBMFormatter(view);
            // Update indent from first line.
            // The IBM formatter may have changed it.
            defaultIndent = getIndent(view, startLine);
        }

        if (executeIrpgFormatter) {
            FormattedResult result = formatter.format(input, defaultIndent);

            // Write formatted source back to view. Statements that failed to
            // format are written as their original source lines, so the
            // overall write still happens — even when getErrorCount() > 0.
            IRpgleOutput output = input.getOutput();
            output.writeSourceLines(result);

            // Re-select the (possibly resized) block regardless of errors,
            // since the lines have been written back to the view.
            if (haveSelection && result.getLineCount() > 0) {
                endLine = startLine + result.getLineCount() - 1;
                LpexViewUtils.selectBlockRange(view, startLine, endLine);
            }

            if (formatter.getErrorCount() > 0) {
                reportFormattingErrors(formatter.getErrors());
            } else if (haveSelection && result.getLineCount() > 0) {
                LpexViewUtils.displayMessage(view, Messages.bind(Messages.Message_Source_Lines_A_B_formatted_successfully, startLine, endLine));
            } else {
                LpexViewUtils.displayMessage(view, Messages.Message_Source_formatted_successfully);
            }
        } else if (executeIbmFormatter) {
            LpexViewUtils.displayMessage(view, Messages.Message_Source_formatted_successfully);
        } else {
            LpexViewUtils.displayMessage(view, Messages.Message_No_formatting_applied);
        }

        restoreCursorPosition(view);
    }

    /**
     * Shows the standard error-details dialog summarizing the failed
     * statements and logs any unexpected underlying exceptions to the
     * Eclipse error log so that they are not silently swallowed.
     */
    private void reportFormattingErrors(List<FormatError> errors) {

        String[] details = new String[errors.size()];
        for (int i = 0; i < errors.size(); i++) {
            FormatError error = errors.get(i);
            details[i] = Messages.bind(Messages.Error_Line_A_message_B, error.getStartLineNumber() + 1, error.getMessage());
        }

        UIUtils.displayErrorDetailsDialog(
            Messages.bind(Messages.Error_Source_formatted_with_N_errors, errors.size()), details);

        // Unexpected underlying exceptions still go to the Eclipse error log
        // for post-mortem analysis. LineOverflowException-style errors carry
        // a null cause and are intentionally only shown to the user.
        for (FormatError error : errors) {
            if (error.getCause() != null) {
                IRpgleFormatterPlugin.logError(error.getMessage(), error.getCause());
            }
        }
    }

    private int getIndent(LpexView view, int startLine) {

        String line = LpexViewUtils.getElementText(view, startLine).toUpperCase();
        while (line.trim().length() == 0 || "**FREE".equals(line)) {
            startLine++;
            line = LpexViewUtils.getElementText(view, startLine).toUpperCase();
        }

        return StringUtils.getIndent(line).length();
    }

    private void saveCursorPosition(LpexView view) {
        line = LpexViewUtils.queryLine(view);
        position = LpexViewUtils.queryPosition(view);
    }

    private void restoreCursorPosition(LpexView view) {
        LpexViewUtils.setLine(view, line);
        LpexViewUtils.setPosition(view, position);
    }
}
