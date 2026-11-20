/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import de.tools400.lpex.irpgformatter.Messages;

/**
 * The kind of the sources of a batch, that formats several sources, with the
 * texts of its result. Has no Eclipse types, so that the unit tests can use it.
 */
public enum BatchSourceKind {
    STREAM_FILES (
        Messages.Info_Finished_formatting_stream_files_A,
        Messages.Error_A_formatted_B_errors,
        Messages.Error_A_files_formatted_with_statement_errors_B,
        Messages.Error_A_files_formatted_B_failed_C_with_statement_errors),
    SOURCE_MEMBERS (
        Messages.Info_Finished_formatting_source_members_A,
        Messages.Error_A_members_formatted_B_errors,
        Messages.Error_A_members_formatted_with_statement_errors_B,
        Messages.Error_A_members_formatted_B_failed_C_with_statement_errors);

    private final String finished;
    private final String formattedNotFormatted;
    private final String formattedWithStatementErrors;
    private final String formattedNotFormattedWithStatementErrors;

    private BatchSourceKind(String finished, String formattedNotFormatted, String formattedWithStatementErrors,
        String formattedNotFormattedWithStatementErrors) {
        this.finished = finished;
        this.formattedNotFormatted = formattedNotFormatted;
        this.formattedWithStatementErrors = formattedWithStatementErrors;
        this.formattedNotFormattedWithStatementErrors = formattedNotFormattedWithStatementErrors;
    }

    /**
     * Returns the message, that all sources were formatted.
     *
     * @param formatted - the number of formatted sources
     * @return the message
     */
    public String getFinishedMessage(int formatted) {
        return Messages.bind(finished, formatted);
    }

    /**
     * Returns the summary of a batch, that the view of the problems shows above
     * them.
     *
     * @param formatted - the number of formatted sources
     * @param notFormatted - the number of sources, that were not formatted,
     *        e.g. because they are not free-form, are locked or could not be
     *        read or written
     * @param withStatementErrors - the number of formatted sources with errors
     *        or warnings of statements
     * @return the summary or <code>null</code>, if all sources were formatted
     *         without problems
     */
    public String getSummary(int formatted, int notFormatted, int withStatementErrors) {

        if (notFormatted == 0 && withStatementErrors == 0) {
            return null;
        }

        if (withStatementErrors == 0) {
            return Messages.bind(formattedNotFormatted, formatted, notFormatted);
        }

        if (notFormatted == 0) {
            return Messages.bind(formattedWithStatementErrors, formatted, withStatementErrors);
        }

        return Messages.bind(formattedNotFormattedWithStatementErrors, new Object[] { formatted, notFormatted, withStatementErrors });
    }
}
