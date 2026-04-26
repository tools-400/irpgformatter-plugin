/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.handlers.jobs.FormatRemoteStreamFilesJob;
import de.tools400.lpex.irpgformatter.handlers.jobs.FormatRemoteStreamFilesJob.FileError;
import de.tools400.lpex.irpgformatter.handlers.jobs.IFormatRemoteStreamFilesPostRun;
import de.tools400.lpex.irpgformatter.utils.UIUtils;

public class FormatRemoteStreamFileHandler extends AbstractHandler implements IFormatRemoteStreamFilesPostRun {

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

        FormatRemoteStreamFilesJob job = new FormatRemoteStreamFilesJob(files, this);
        job.schedule();
    }

    /**
     * Callback of the formatter job. Called at the end of the formatter.
     */
    @Override
    public void run(IRemoteFile[] formatted, FileError[] errors) {

        if (errors.length > 0) {
            displayErrorDialog(formatted, errors);
        } else {
            displaySuccessDialog(formatted);
        }
    }

    private void displaySuccessDialog(IRemoteFile[] formatted) {
        UIJob job = new UIJob(UIUtils.getDisplay(), "") {
            @Override
            public IStatus runInUIThread(IProgressMonitor arg0) {
                String message = Messages.bind(Messages.Info_Finished_formatting_stream_files_A, formatted.length);
                MessageDialog.openInformation(UIUtils.getShell(), "Information", message);
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    private void displayErrorDialog(IRemoteFile[] formatted, FileError[] errors) {
        String[] errorDetails = new String[errors.length];
        for (int i = 0; i < errors.length; i++) {
            errorDetails[i] = errors[i].getFile().getAbsolutePath() + ": " + errors[i].getErrorMessage();
        }
        String message;
        if (formatted.length > 0) {
            message = Messages.bind(Messages.Error_A_formatted_B_errors, formatted.length, errors.length);
        } else {
            message = Messages.bind(Messages.Error_Not_all_files_formatted_A, errors.length);
        }
        UIJob job = new UIJob(UIUtils.getDisplay(), "") {
            @Override
            public IStatus runInUIThread(IProgressMonitor arg0) {
                UIUtils.displayErrorDetailsTitleAreaDialog(message, errorDetails);
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
}
