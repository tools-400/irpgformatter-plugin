/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.actions;

import org.eclipse.jface.dialogs.MessageDialog;

import com.ibm.lpex.core.LpexAction;
import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.IRpgleOutput;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;
import de.tools400.lpex.irpgformatter.input.RpgleLpexInput;
import de.tools400.lpex.irpgformatter.preferences.CalculationFormatter;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.utils.IBMRpgleFormatterUtils;
import de.tools400.lpex.irpgformatter.utils.LpexViewUtils;
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
            RpgleLpexInput input;
            int startLine;
            int endLine;
            if (haveSelection) {
                input = (RpgleLpexInput)RpgleInputFactory.createFromSelection(view);
                startLine = input.getStartLine();
                endLine = input.getEndLine();
                // Extend selected lines to logical block
                LpexViewUtils.selectBlockRange(view, startLine, endLine);
            } else {
                input = (RpgleLpexInput)RpgleInputFactory.createFromAll(view);
                startLine = 1;
                endLine = -1;
            }

            String validationError = RpgleFormatter.validateInput(input);
            if (validationError != null) {
                MessageDialog.openError(UIUtils.getShell(), Messages.E_R_R_O_R, validationError);
                return;
            } else {
                executeFormatter(view, input, haveSelection, startLine);
            }

        } catch (RpgleFormatterException e) {
            LpexViewUtils.displayMessage(view, Messages.bind(Messages.Error_Format_failed_A, e.getMessage()));
            IRpgleFormatterPlugin.logError("Format failed.", e);
        } catch (Throwable e) {
            LpexViewUtils.displayMessage(view, Messages.bind(Messages.Error_Unexpected_error_A, e.getMessage()));
            IRpgleFormatterPlugin.logError("Unexpected error while formatting.", e);
        }
    }

    private void executeFormatter(LpexView view, RpgleLpexInput input, boolean haveSelection, int startLine) throws RpgleFormatterException {

        CalculationFormatter calculationFormatter = Preferences.getInstance().getFormatCalculations();
        boolean executeIbmFormatter = calculationFormatter == CalculationFormatter.IBM_FORMATTER;
        boolean executeIrpgFormatter = Preferences.getInstance().isFormatDeclarations() || calculationFormatter == CalculationFormatter.IRPG_FORMATTER;

        RpgleFormatter formatter = new RpgleFormatter();
        formatter.setSourceLength(LpexViewUtils.getMaxLineLength(view));

        int endLine = -1;
        saveCursorPosition(view);

        if (executeIbmFormatter) {
            IBMRpgleFormatterUtils.executeIBMFormatter(view);
        }

        if (executeIrpgFormatter) {
            FormattedResult result = format(formatter, view, input, startLine);

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
                formatter.logUnexpectedErrors(input.getName());
                UIUtils.displayErrorDetailsDialog(Messages.bind(Messages.Error_Source_formatted_with_N_errors, formatter.getErrorCount()),
                    formatter.getErrorAndWarningMessages());
            } else if (formatter.getWarningCount() > 0) {
                UIUtils.displayWarningDetailsDialog(Messages.bind(Messages.Warning_Source_formatted_with_N_warnings, formatter.getWarningCount()),
                    formatter.getWarningMessages());
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
     * Formats the input. The lines in front of a selection tell the formatter
     * the blocks, that enclose the selection, so that the selection is
     * indented like the whole source. If these lines cannot be collected into
     * statements, the selection is formatted alone, starting with the current
     * indent of its first line.
     */
    private FormattedResult format(RpgleFormatter formatter, LpexView view, RpgleLpexInput input, int startLine) throws RpgleFormatterException {

        int defaultIndent = Preferences.getInstance().getStartColumn() - 1;

        try {
            return formatter.format(input, defaultIndent, input.getPrecedingSourceLines());
        } catch (RpgleFormatterException e) {
            if (startLine == 1) {
                throw e;
            }
            return formatter.format(input, LpexViewUtils.getIndentOfFirstStatement(view, startLine));
        }
    }

    private void saveCursorPosition(LpexView view) {
        line = LpexViewUtils.queryLine(view);
        position = LpexViewUtils.queryPosition(view);
    }

    private void restoreCursorPosition(LpexView view) {

        if (line <= LpexViewUtils.getNumLines(view)) {
            LpexViewUtils.setLine(view, line);
            LpexViewUtils.setPosition(view, position);
        } else {
            LpexViewUtils.setLine(view, LpexViewUtils.getNumLines(view));
            LpexViewUtils.setPosition(view, 1);
        }
    }
}
