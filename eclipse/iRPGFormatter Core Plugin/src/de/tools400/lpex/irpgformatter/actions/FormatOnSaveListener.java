/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.actions;

import java.util.List;

import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.BlockStructureException;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.input.IRpgleOutput;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;
import de.tools400.lpex.irpgformatter.preferences.CalculationFormatter;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.preferences.SourcePreferences;
import de.tools400.lpex.irpgformatter.problems.EditorSources;
import de.tools400.lpex.irpgformatter.problems.FormatterProblem;
import de.tools400.lpex.irpgformatter.problems.FormatterProblems;
import de.tools400.lpex.irpgformatter.problems.IProblemSource;
import de.tools400.lpex.irpgformatter.problems.ProblemFactory;
import de.tools400.lpex.irpgformatter.utils.IBMRpgleFormatterUtils;
import de.tools400.lpex.irpgformatter.utils.LpexViewUtils;
import de.tools400.lpex.irpgformatter.views.FormatterProblemsView;

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

            // The project of the source may switch format on save on or off.
            Preferences preferences = SourcePreferences.get(view);
            if (!preferences.isFormatOnSave()) {
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
                executeFormatter(view, input, preferences);
            }

        } catch (BlockStructureException e) {
            // No dialog on save, but the user must learn, why the source was
            // not formatted: the view shows the errors, the editor keeps the
            // focus.
            IProblemSource source = EditorSources.get(view);
            FormatterProblems.getInstance().replace(source, ProblemFactory.fromBlockStructureException(source, e));
            LpexViewUtils.displayMessage(view,
                Messages.bind(Messages.Error_Source_not_formatted_N_errors_first_on_line_A, e.getErrorCount(), e.getFirstLineNumber()));
            FormatterProblemsView.show(source);
        } catch (Throwable e) {
            IRpgleFormatterPlugin.logError("Format on save failed", e);
        }
    }

    private static void executeFormatter(LpexView view, IRpgleInput input, Preferences preferences) throws RpgleFormatterException {

        RpgleFormatter formatter = new RpgleFormatter(FormatterConfig.fromPreferences(preferences));
        formatter.setSourceLength(LpexViewUtils.getMaxLineLength(view));

        int defaultIndent = preferences.getStartColumn() - 1;

        CalculationFormatter calculationFormatter = preferences.getCalculationFormatter();

        if (calculationFormatter == CalculationFormatter.IBM_FORMATTER) {
            IBMRpgleFormatterUtils.executeIBMFormatter(view);
        }

        if (preferences.isFormatDeclarations() || calculationFormatter == CalculationFormatter.IRPG_FORMATTER) {
            FormattedResult result = formatter.format(input, defaultIndent);
            IRpgleOutput output = input.getOutput();
            output.writeSourceLines(result);
            // No dialog on save, but a bug must not get lost.
            formatter.logUnexpectedErrors(input.getName());

            IProblemSource source = EditorSources.get(view);
            List<FormatterProblem> problems = ProblemFactory.fromFormatter(source, formatter, result);
            FormatterProblems.getInstance().replace(source, problems);
            if (!problems.isEmpty()) {
                FormatterProblemsView.show(source);
            }
        }
    }
}
