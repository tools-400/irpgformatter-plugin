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
import de.tools400.lpex.irpgformatter.handlers.jobs.IFormatPostRun;
import de.tools400.lpex.irpgformatter.handlers.resolvers.AbstractResolver;
import de.tools400.lpex.irpgformatter.problems.FormatterProblems;
import de.tools400.lpex.irpgformatter.utils.ExceptionUtils;
import de.tools400.lpex.irpgformatter.utils.UIUtils;
import de.tools400.lpex.irpgformatter.views.FormatterProblemsView;

public abstract class AbstractFormatHandler extends AbstractHandler implements IFormatPostRun {

    private final BatchSourceKind sourceKind;

    protected AbstractFormatHandler(BatchSourceKind sourceKind) {
        this.sourceKind = sourceKind;
    }

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

    /**
     * Callback of the formatter job. Called at the end of the formatter. The
     * job has put the problems of the sources into the view
     * {@link FormatterProblemsView}; the view is activated and shows the result
     * above them. After a cancel, only the problems are shown.
     */
    @Override
    public void postRun(int formattedCount, int notFormattedCount, int statementErrorsCount, boolean canceled) {

        String summary = sourceKind.getSummary(formattedCount, notFormattedCount, statementErrorsCount);
        if (summary == null) {
            if (!canceled) {
                displaySuccessDialog(sourceKind.getFinishedMessage(formattedCount));
            }
            return;
        }

        displayProblemsView(summary);
    }

    private void displaySuccessDialog(String message) {
        UIJob job = new UIJob(UIUtils.getDisplay(), "") {
            @Override
            public IStatus runInUIThread(IProgressMonitor arg0) {
                MessageDialog.openInformation(UIUtils.getShell(), Messages.I_N_F_O_R_M_A_T_I_O_N, message);
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    private void displayNoValidEntriesFoundError() {
        UIUtils.displaySimpleErrorDialog(Messages.Error_No_valid_entries_found);
    }

    /**
     * Activates the view of the problems, which lists the problems of all
     * sources, and shows the result of the job above them.
     */
    private void displayProblemsView(String summary) {
        UIJob job = new UIJob(UIUtils.getDisplay(), "") {
            @Override
            public IStatus runInUIThread(IProgressMonitor arg0) {
                FormatterProblems.getInstance().setSummary(summary);
                FormatterProblemsView.show(true);
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
}
