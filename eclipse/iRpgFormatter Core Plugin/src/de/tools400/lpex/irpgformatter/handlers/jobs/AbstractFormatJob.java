/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.jobs;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.swt.widgets.Display;

import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.BatchAbortedException;
import de.tools400.lpex.irpgformatter.formatter.FormatError;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.handlers.IErrorObject;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.input.IRpgleOutput;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.utils.ErrorGroup;
import de.tools400.lpex.irpgformatter.utils.ExceptionUtils;

/**
 * Base class of the jobs that format a batch of sources: source members,
 * stream files of the IFS, files of the workspace and files of the local file
 * system. A subclass names, checks and reads a single source. The loop, the
 * progress, the error handling and the result are the same for all sources.
 *
 * @param <T> type of the sources
 */
public abstract class AbstractFormatJob<T> extends Job {

    private T[] sources;
    private RpgleFormatter formatter;
    private IFormatPostRun postRun;

    private IProgressMonitor monitor;
    private List<IErrorObject> errors;
    private int formattedCount;
    private List<ErrorGroup> statementErrors;

    protected AbstractFormatJob(String name, T[] sources, IFormatPostRun postRun) {
        super(name);

        this.sources = sources;
        this.formatter = new RpgleFormatter();
        this.postRun = postRun;

        this.errors = new LinkedList<>();
        this.formattedCount = 0;
        this.statementErrors = new LinkedList<>();
    }

    /**
     * Returns the path of a source, as it is shown in the progress and in the
     * errors.
     */
    protected abstract String getPath(T source);

    /**
     * Checks that the source can be formatted, e.g. that it is not locked.
     */
    protected abstract void checkAvailability(T source) throws Exception;

    /**
     * Reads the source.
     */
    protected abstract IRpgleInput createInput(T source, IProgressMonitor monitor) throws Exception;

    /**
     * Returns the length of the source lines. Stream files have no fixed
     * length.
     */
    protected int getSourceLength(T source) throws Exception {
        return RpgleFormatter.DEFAULT_END_COLUMN;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        this.monitor = monitor;

        boolean canceled = false;
        try {

            monitor.beginTask(Messages.Job_Formatting, sources.length);

            for (T source : sources) {

                if (monitor.isCanceled()) {
                    canceled = true;
                    break;
                }

                monitor.setTaskName(getPath(source));
                executeFormatter(source);

                monitor.worked(1);
            }

        } catch (BatchAbortedException e) {
            // user declined connection dialog — stop processing remaining
            // sources
            canceled = true;
        } catch (Exception e) {
            ExceptionUtils.handleBatchException(e);
        }

        monitor.done();

        if (postRun != null) {
            postRun.postRun(formattedCount, errors.toArray(new IErrorObject[errors.size()]),
                statementErrors.toArray(new ErrorGroup[statementErrors.size()]), canceled);
        }

        if (canceled) {
            return Status.CANCEL_STATUS;
        }

        return Status.OK_STATUS;
    }

    private void executeFormatter(T source) throws BatchAbortedException {

        try {

            monitor.subTask(Messages.SubTask_Checking_availability);
            checkAvailability(source);

            monitor.subTask(Messages.SubTask_Reading);
            IRpgleInput input = createInput(source, monitor);

            String validationError = RpgleFormatter.validateInput(input);
            if (validationError != null) {
                errors.add(new SourceError(getPath(source), validationError));
            } else {
                executeFormatter(source, input);
            }

        } catch (BatchAbortedException e) {
            throw e;
        } catch (Exception e) {
            errors.add(new SourceError(getPath(source), ExceptionUtils.getLocalizedMessage(e)));
        }
    }

    private void executeFormatter(T source, IRpgleInput input) throws Exception {

        formatter.setSourceLength(getSourceLength(source));
        int defaultIndent = Preferences.getInstance().getStartColumn() - 1;

        monitor.subTask(Messages.SubTask_Formatting);
        FormattedResult result = formatter.format(input, defaultIndent);

        monitor.subTask(Messages.SubTask_Writing);
        IRpgleOutput output = input.getOutput();
        if (output.writeSourceLines(result)) {
            formattedCount++;
            if (formatter.getErrorCount() > 0) {
                statementErrors.add(new ErrorGroup(getPath(source), toDetailMessages(formatter.getErrors())));
            }
        } else {
            errors.add(new SourceError(getPath(source), Messages.Error_Could_not_format_member));
        }
    }

    /**
     * Connects the system, if the user agrees.
     *
     * @throws BatchAbortedException - the user does not want to connect
     * @throws SystemMessageException - the connection failed
     */
    protected static void ensureConnected(IBMiConnection connection) throws BatchAbortedException, SystemMessageException {

        if (connection.isConnected()) {
            return;
        }

        final boolean[] userWantsConnect = { false };

        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                userWantsConnect[0] = MessageDialog.openQuestion(Display.getDefault().getActiveShell(), Messages.Title_Connection_offline,
                    Messages.bind(Messages.Question_Connection_A_is_offline_connect, connection.getConnectionName()));
            }
        });

        if (!userWantsConnect[0]) {
            throw new BatchAbortedException();
        }

        // throws SystemMessageException on failure
        connection.connect();
    }

    private static String[] toDetailMessages(List<FormatError> formatErrors) {
        String[] details = new String[formatErrors.size()];
        for (int i = 0; i < formatErrors.size(); i++) {
            FormatError fe = formatErrors.get(i);
            details[i] = Messages.bind(Messages.Error_Line_A_message_B, fe.getStartLineNumber() + 1, fe.getMessage());
        }
        return details;
    }

    /**
     * A source that could not be formatted at all.
     */
    private static final class SourceError implements IErrorObject {

        private String fullPath;
        private String errorMessage;

        public SourceError(String fullPath, String errorMessage) {
            this.fullPath = fullPath;
            this.errorMessage = errorMessage;
        }

        @Override
        public String getFullPath() {
            return fullPath;
        }

        @Override
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
