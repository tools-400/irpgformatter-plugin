/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.handlers.resolvers.AbstractResolver;
import de.tools400.lpex.irpgformatter.utils.ErrorGroup;
import de.tools400.lpex.irpgformatter.utils.ExceptionUtils;
import de.tools400.lpex.irpgformatter.utils.UIUtils;

public abstract class AbstractFormatHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (selection instanceof IStructuredSelection) {

            int count;
            try {
                count = resolveItemsInBackground((IStructuredSelection)selection);
            } catch (InterruptedException e) {
                // canceled by the user
                return null;
            } catch (ExecutionException e) {
                UIUtils.displaySimpleErrorDialog(e.getMessage());
                return null;
            }

            if (count > 0) {
                scheduleFormatterJob();
            } else {
                displayNoValidEntriesFoundError();
            }

        }

        return null;
    }

    /**
     * Resolves the selected items outside of the UI thread, because resolving
     * libraries, source files, filters and directories lists them on the host.
     * If it takes a while, a progress dialog opens, in which the user can
     * cancel.
     */
    private int resolveItemsInBackground(final IStructuredSelection selection) throws ExecutionException, InterruptedException {

        final int[] count = new int[1];
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.SubTask_Resolving_selection, IProgressMonitor.UNKNOWN);
                    try {
                        count[0] = resolveItems(selection, monitor);
                    } catch (ExecutionException e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                    if (monitor.isCanceled()) {
                        throw new InterruptedException();
                    }
                }
            });
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof ExecutionException) {
                throw (ExecutionException)e.getCause();
            }
            IRpgleFormatterPlugin.logError("Failed to resolve the selected items.", e.getCause());
            throw new ExecutionException(ExceptionUtils.getLocalizedMessage(e.getCause()), e.getCause());
        }

        return count[0];
    }

    /**
     * Resolves the selected items. Called outside of the UI thread.
     */
    protected abstract int resolveItems(IStructuredSelection selection, IProgressMonitor monitor) throws ExecutionException;

    /**
     * Throws the first error of the resolver, if no item was found, so that the
     * user sees the reason instead of 'No valid entries found'.
     */
    protected static void checkResolved(int count, AbstractResolver resolver) throws ExecutionException {

        if (count == 0 && resolver.getError() != null) {
            Exception error = resolver.getError();
            throw new ExecutionException(ExceptionUtils.getLocalizedMessage(error), error);
        }
    }

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
