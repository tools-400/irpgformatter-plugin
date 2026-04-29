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
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.handlers.jobs.FormatStreamFileJob;
import de.tools400.lpex.irpgformatter.handlers.jobs.FormatStreamFileJob.FileError;
import de.tools400.lpex.irpgformatter.handlers.jobs.IFormatStreamFilesPostRun;

public class FormatStreamFileHandler extends AbstractFormatHandler implements IFormatStreamFilesPostRun {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (selection instanceof IStructuredSelection) {

            IFile[] files = resolveStreamFiles((IStructuredSelection)selection);
            scheduleFormatterJob(files);
        }

        return null;
    }

    private IFile[] resolveStreamFiles(IStructuredSelection selection) {

        StreamFileResolver resolver = new StreamFileResolver();
        IFile[] files = resolver.resolveStreamFiles(selection);

        return files;
    }

    private void scheduleFormatterJob(IFile[] files) {

        FormatStreamFileJob job = new FormatStreamFileJob(files, this);
        job.schedule();
    }

    /**
     * Callback of the formatter job. Called at the end of the formatter.
     */
    @Override
    public void run(IFile[] formatted, FileError[] errors) {

        if (errors.length > 0) {
            String message;
            if (formatted.length > 0) {
                message = Messages.bind(Messages.Error_A_formatted_B_errors, formatted.length, errors.length);
            } else {
                message = Messages.bind(Messages.Error_Not_all_files_formatted_A, errors.length);
            }
            displayErrorDialog(message, errors);
        } else {
            displaySuccessDialog(Messages.bind(Messages.Info_Finished_formatting_stream_files_A, formatted.length));
        }
    }
}
