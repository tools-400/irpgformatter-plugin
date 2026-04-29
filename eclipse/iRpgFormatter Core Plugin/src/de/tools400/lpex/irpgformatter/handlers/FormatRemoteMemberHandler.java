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
import org.eclipse.ui.handlers.HandlerUtil;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.handlers.jobs.FormatRemoteSourceMemberJob;
import de.tools400.lpex.irpgformatter.handlers.jobs.FormatRemoteSourceMemberJob.MemberError;
import de.tools400.lpex.irpgformatter.handlers.jobs.IFormatRemoteSourceMembersPostRun;

public class FormatRemoteMemberHandler extends AbstractFormatHandler implements IFormatRemoteSourceMembersPostRun {

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

        FormatRemoteSourceMemberJob job = new FormatRemoteSourceMemberJob(sourceMembers, this);
        job.schedule();
    }

    /**
     * Callback of the formatter job. Called at the end of the formatter.
     */
    @Override
    public void run(SourceMember[] formatted, MemberError[] errors) {

        if (errors.length > 0) {
            String message;
            if (formatted.length > 0) {
                message = Messages.bind(Messages.Error_A_members_formatted_B_errors, formatted.length, errors.length);
            } else {
                message = Messages.bind(Messages.Error_Not_all_members_formatted_A, errors.length);
            }
            displayErrorDialog(message, errors);
        } else {
            displaySuccessDialog(Messages.bind(Messages.Info_Finished_formatting_source_members_A, formatted.length));
        }
    }
}
