/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.handlers.jobs.FormatRemoteStreamFileJob;
import de.tools400.lpex.irpgformatter.handlers.jobs.FormatRemoteStreamFileJob.FileError;
import de.tools400.lpex.irpgformatter.handlers.jobs.IFormatRemoteStreamFilesPostRun;
import de.tools400.lpex.irpgformatter.utils.ErrorGroup;

public class FormatRemoteStreamFileHandler extends AbstractFormatHandler implements IFormatRemoteStreamFilesPostRun {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (selection instanceof IStructuredSelection) {

            IRemoteFile[] files = resolveRemoteStreamFiles((IStructuredSelection)selection);
            scheduleFormatterJob(files);
        }

        return null;
    }

    private IRemoteFile[] resolveRemoteStreamFiles(IStructuredSelection selection) {

        RemoteStreamFileResolver resolver = new RemoteStreamFileResolver();
        IRemoteFile[] files = resolver.resolveRemoteStreamFiles(selection);

        return files;
    }

    private void scheduleFormatterJob(IRemoteFile[] files) {

        FormatRemoteStreamFileJob job = new FormatRemoteStreamFileJob(files, this);
        job.schedule();
    }

    /**
     * Callback of the formatter job. Called at the end of the formatter.
     */
    @Override
    public void run(IRemoteFile[] formatted, FileError[] errors, ErrorGroup[] statementErrors) {

        if (errors.length > 0) {
            String message;
            if (formatted.length > 0) {
                message = Messages.bind(Messages.Error_A_formatted_B_errors, formatted.length, errors.length);
            } else {
                message = Messages.bind(Messages.Error_Not_all_files_formatted_A, errors.length);
            }
            displayErrorDialog(message, errors);
        } else if (statementErrors.length > 0) {
            String message = Messages.bind(Messages.Error_A_files_formatted_with_statement_errors_B,
                formatted.length, statementErrors.length);
            displayStatementErrorsDialog(message, statementErrors);
        } else {
            displaySuccessDialog(Messages.bind(Messages.Info_Finished_formatting_stream_files_A, formatted.length));
        }
    }
}
