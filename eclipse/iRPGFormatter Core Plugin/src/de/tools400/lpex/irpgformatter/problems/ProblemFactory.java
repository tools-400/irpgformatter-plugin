/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.problems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.tools400.lpex.irpgformatter.formatter.BlockStructureException;
import de.tools400.lpex.irpgformatter.formatter.FormatError;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.problems.FormatterProblem.Severity;

/**
 * Creates the problems of a source for {@link FormatterProblems}.
 */
public final class ProblemFactory {

    private ProblemFactory() {
        // Utility class
    }

    /**
     * Returns the declaration blocks, that are not closed. The source was not
     * changed, so the line numbers are still valid.
     */
    public static List<FormatterProblem> fromBlockStructureException(IProblemSource source, BlockStructureException e) {

        String[] messages = e.getErrorMessages();
        int[] lineNumbers = e.getLineNumbers();

        List<FormatterProblem> problems = new ArrayList<>();
        for (int i = 0; i < messages.length; i++) {
            problems.add(new FormatterProblem(source, Severity.ERROR, lineNumbers[i], messages[i]));
        }

        return problems;
    }

    /**
     * Returns the errors and warnings of the last formatting, ordered by their
     * line numbers. The formatted lines were written, so the line numbers are
     * mapped to the formatted source, because the statements in front of a
     * problem may have more or less lines than before.
     *
     * @param source - the source
     * @param formatter - the formatter, that formatted the source
     * @param result - the formatted lines, that were written
     * @return the problems, empty if there are neither errors nor warnings
     */
    public static List<FormatterProblem> fromFormatter(IProblemSource source, RpgleFormatter formatter, FormattedResult result) {

        List<FormatterProblem> problems = new ArrayList<>();
        addProblems(problems, source, Severity.ERROR, formatter.getErrors(), result);
        addProblems(problems, source, Severity.WARNING, formatter.getWarnings(), result);

        problems.sort(Comparator.comparingInt(FormatterProblem::getLineNumber));

        return problems;
    }

    /**
     * Returns an error without line number, e.g. of a source, that cannot be
     * read.
     */
    public static List<FormatterProblem> fromMessage(IProblemSource source, String message) {
        return Collections.singletonList(new FormatterProblem(source, Severity.ERROR, 0, message));
    }

    private static void addProblems(List<FormatterProblem> problems, IProblemSource source, Severity severity,
        List<FormatError> formatErrors, FormattedResult result) {

        for (FormatError formatError : formatErrors) {
            int lineNumber = result.getFormattedLineNumber(formatError.getStartLineNumber());
            problems.add(new FormatterProblem(source, severity, lineNumber, formatError.getMessage()));
        }
    }
}
