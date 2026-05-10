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
import de.tools400.lpex.irpgformatter.utils.ErrorGroup;
import de.tools400.lpex.irpgformatter.utils.UIUtils;

public abstract class AbstractFormatHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (selection instanceof IStructuredSelection) {
            int count = resolveItems((IStructuredSelection)selection);
            if (count > 0) {
                scheduleFormatterJob();
            } else {
                displayNoValidEntriesFoundError();
            }

        }

        return null;
    }

    protected abstract int resolveItems(IStructuredSelection selection) throws ExecutionException;

    protected abstract void scheduleFormatterJob();

    protected void displaySuccessDialog(String message) {
        UIJob job = new UIJob(UIUtils.getDisplay(), "") {
            @Override
            public IStatus runInUIThread(IProgressMonitor arg0) {
                MessageDialog.openInformation(UIUtils.getShell(), Messages.I_N_F_O_R_M_A_T_I_O_N, message);
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    protected void displayNoValidEntriesFoundError() {
        UIUtils.displaySimpleErrorDialog(Messages.Error_No_valid_entries_found);
    }

    /**
     * Shows the unified master/detail error dialog. Both write-level errors
     * (one resource per group, one detail line) and statement-level errors (one
     * resource per group, multiple detail lines) are presented in the same
     * dialog so the user only deals with one window.
     */
    protected void displayErrorDialog(String message, ErrorGroup[] groups) {
        UIJob job = new UIJob(UIUtils.getDisplay(), "") {
            @Override
            public IStatus runInUIThread(IProgressMonitor arg0) {
                UIUtils.displayErrorDetailsDialog(message, groups);
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    /**
     * Converts write-level error objects (one error message per resource) into
     * the {@link ErrorGroup} representation used by the master/detail dialog,
     * so they can be displayed alongside statement-level errors.
     */
    protected static ErrorGroup[] toErrorGroups(IErrorObject[] errors) {
        ErrorGroup[] groups = new ErrorGroup[errors.length];
        for (int i = 0; i < errors.length; i++) {
            groups[i] = new ErrorGroup(errors[i].getFullPath(), new String[] { errors[i].getErrorMessage() });
        }
        return groups;
    }

    /**
     * Concatenates two {@link ErrorGroup} arrays in order. Used when a batch
     * has both write failures and statement-level errors.
     */
    protected static ErrorGroup[] concat(ErrorGroup[] a, ErrorGroup[] b) {
        ErrorGroup[] result = new ErrorGroup[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
