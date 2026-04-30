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
import de.tools400.lpex.irpgformatter.utils.ErrorGroup;

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
     * Callback of the formatter job. Called at the end of the formatter. Both
     * write-level errors (member could not be written at all) and
     * statement-level errors (member written, but individual statements
     * unchanged) are presented together in the master/detail dialog.
     */
    @Override
    public void postRun(SourceMember[] formatted, MemberError[] errors, ErrorGroup[] statementErrors) {

        if (errors.length == 0 && statementErrors.length == 0) {
            displaySuccessDialog(Messages.bind(Messages.Info_Finished_formatting_source_members_A, formatted.length));
            return;
        }

        ErrorGroup[] groups = concat(toErrorGroups(errors), statementErrors);
        String message = buildHeader(formatted.length, errors.length, statementErrors.length);
        displayErrorDialog(message, groups);
    }

    private static String buildHeader(int formatted, int writeErrors, int statementErrorMembers) {
        if (writeErrors > 0 && statementErrorMembers == 0) {
            if (formatted > 0) {
                return Messages.bind(Messages.Error_A_members_formatted_B_errors, formatted, writeErrors);
            }
            return Messages.bind(Messages.Error_Not_all_members_formatted_A, writeErrors);
        }
        if (writeErrors == 0 && statementErrorMembers > 0) {
            return Messages.bind(Messages.Error_A_members_formatted_with_statement_errors_B, formatted, statementErrorMembers);
        }
        // both write errors and statement errors present
        return Messages.bind(Messages.Error_A_files_formatted_B_failed_C_with_statement_errors,
            new Object[] { formatted, writeErrors, statementErrorMembers });
    }
}
