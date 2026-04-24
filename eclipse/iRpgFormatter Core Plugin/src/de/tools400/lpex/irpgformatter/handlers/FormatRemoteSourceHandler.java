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
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.handlers.jobs.FormatRemoteSourceMembersJob;
import de.tools400.lpex.irpgformatter.handlers.jobs.FormatRemoteSourceMembersJob.MemberError;
import de.tools400.lpex.irpgformatter.handlers.jobs.IFormatRemoteSourceMembersPostRun;
import de.tools400.lpex.irpgformatter.utils.UIUtils;

public class FormatRemoteSourceHandler extends AbstractHandler implements IFormatRemoteSourceMembersPostRun {

    public static final String ID = "de.tools400.lpex.irpgformatter.commands.format";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (selection instanceof IStructuredSelection) {

            SourceMember[] sourceMembers = resolveSourceMembers(selection);
            scheduleFormatterJob(sourceMembers);
        }

        return null;
    }

    private SourceMember[] resolveSourceMembers(ISelection selection) {

        SourceMembersResolver resolver = new SourceMembersResolver();
        SourceMember[] sourceMembers = resolver.resolveSourceMembers(selection);

        return sourceMembers;
    }

    private void scheduleFormatterJob(SourceMember[] sourceMembers) {

        FormatRemoteSourceMembersJob job = new FormatRemoteSourceMembersJob(sourceMembers, this);
        job.schedule();
    }

    /**
     * Callback of the formatter job. Called at the end of the formatter.
     */
    @Override
    public void run(SourceMember[] formatted, MemberError[] errors) {

        if (errors.length > 0 && formatted.length > 0) {
            displayErrorDialog(formatted, errors);
        } else if (errors.length > 0) {
            displayErrorDialog(formatted, errors);
        } else {
            displaySuccessDialog(formatted);
        }
    }

    private void displaySuccessDialog(SourceMember[] formatted) {
        UIJob job = new UIJob(UIUtils.getDisplay(), "") {
            @Override
            public IStatus runInUIThread(IProgressMonitor arg0) {
                String message = Messages.bind("Finished formatting source members. {0} members formatted.", formatted.length);
                MessageDialog.openInformation(UIUtils.getShell(), "Information", message);
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    private void displayErrorDialog(SourceMember[] formatted, MemberError[] errors) {
        UIJob job;
        if (errors.length > 0 && formatted.length > 0) {
            job = new UIJob(UIUtils.getDisplay(), "") {
                @Override
                public IStatus runInUIThread(IProgressMonitor arg0) {
                    MessageDialog.openError(UIUtils.getShell(), Messages.E_R_R_O_R,
                        Messages.bind("{0} members formatted. {1} members not formatted.", formatted.length, errors.length));
                    return Status.OK_STATUS;
                }
            };
        } else {
            job = new UIJob(UIUtils.getDisplay(), "") {
                @Override
                public IStatus runInUIThread(IProgressMonitor arg0) {
                    MessageDialog.openError(UIUtils.getShell(), Messages.E_R_R_O_R, "Not all members formatted: " + errors.length);
                    return Status.OK_STATUS;
                }
            };
        }
        job.schedule();
    }
}
