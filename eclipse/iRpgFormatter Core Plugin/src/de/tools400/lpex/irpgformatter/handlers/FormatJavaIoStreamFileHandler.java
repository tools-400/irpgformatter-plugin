/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import java.io.File;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.handlers.jobs.FormatJavaIoStreamFileJob;
import de.tools400.lpex.irpgformatter.handlers.jobs.FormatJavaIoStreamFileJob.FileError;
import de.tools400.lpex.irpgformatter.handlers.jobs.IFormatJavaIoStreamFilesPostRun;
import de.tools400.lpex.irpgformatter.handlers.resolvers.JavaIoStreamFileResolver;
import de.tools400.lpex.irpgformatter.utils.ErrorGroup;

public class FormatJavaIoStreamFileHandler extends AbstractFormatHandler implements IFormatJavaIoStreamFilesPostRun {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (selection instanceof IStructuredSelection) {

            File[] files = resolveStreamFiles((IStructuredSelection)selection);
            scheduleFormatterJob(files);
        }

        return null;
    }

    private File[] resolveStreamFiles(IStructuredSelection selection) {

        JavaIoStreamFileResolver resolver = new JavaIoStreamFileResolver();
        File[] files = resolver.resolveStreamFiles(selection);

        return files;
    }

    private void scheduleFormatterJob(File[] files) {

        FormatJavaIoStreamFileJob job = new FormatJavaIoStreamFileJob(files, this);
        job.schedule();
    }

    /**
     * Callback of the formatter job. Called at the end of the formatter.
     * Both write-level and statement-level errors are presented together
     * in the master/detail dialog.
     */
    @Override
    public void run(File[] formatted, FileError[] errors, ErrorGroup[] statementErrors) {

        if (errors.length == 0 && statementErrors.length == 0) {
            displaySuccessDialog(Messages.bind(Messages.Info_Finished_formatting_stream_files_A, formatted.length));
            return;
        }

        ErrorGroup[] groups = concat(toErrorGroups(errors), statementErrors);
        String message = buildHeader(formatted.length, errors.length, statementErrors.length);
        displayErrorDialog(message, groups);
    }

    private static String buildHeader(int formatted, int writeErrors, int statementErrorFiles) {
        if (writeErrors > 0 && statementErrorFiles == 0) {
            if (formatted > 0) {
                return Messages.bind(Messages.Error_A_formatted_B_errors, formatted, writeErrors);
            }
            return Messages.bind(Messages.Error_Not_all_files_formatted_A, writeErrors);
        }
        if (writeErrors == 0 && statementErrorFiles > 0) {
            return Messages.bind(Messages.Error_A_files_formatted_with_statement_errors_B, formatted, statementErrorFiles);
        }
        return Messages.bind(Messages.Error_A_files_formatted_B_failed_C_with_statement_errors,
            new Object[] { formatted, writeErrors, statementErrorFiles });
    }
}
