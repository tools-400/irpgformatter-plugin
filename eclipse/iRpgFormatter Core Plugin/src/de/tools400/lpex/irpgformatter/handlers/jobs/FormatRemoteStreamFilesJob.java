/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.jobs;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSFile;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FileLockedException;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.input.IRpgleOutput;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.utils.ExceptionUtils;

public class FormatRemoteStreamFilesJob extends Job {

    private IRemoteFile[] files;
    private RpgleFormatter formatter;
    private IFormatRemoteStreamFilesPostRun postRun;

    private List<FileError> errors;
    private List<IRemoteFile> formatted;

    public FormatRemoteStreamFilesJob(IRemoteFile[] files, IFormatRemoteStreamFilesPostRun postRun) {
        super(Messages.Job_Formatting_remote_stream_files);

        this.files = files;
        this.formatter = new RpgleFormatter();
        this.postRun = postRun;

        this.errors = new LinkedList<>();
        this.formatted = new LinkedList<>();
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        try {

            int totalNumberOfFiles = files.length;

            monitor.beginTask(Messages.Job_Formatting, totalNumberOfFiles);

            int count = 0;

            while (count < totalNumberOfFiles) {

                IRemoteFile file = files[count];

                monitor.setTaskName(file.getAbsolutePath());
                executeFormatter(file);

                monitor.worked(1);

                count++;
            }

        } catch (Exception e) {
            ExceptionUtils.handleBatchException(e);
        }

        monitor.done();

        if (postRun != null) {
            postRun.run(formatted.toArray(new IRemoteFile[formatted.size()]), errors.toArray(new FileError[errors.size()]));
        }

        return Status.OK_STATUS;
    }

    private void executeFormatter(IRemoteFile file) throws RpgleFormatterException, Exception {

        try {

            IBMiConnection connection = IBMiConnection.getConnection(file.getHost());
            AS400 system = connection.getAS400ToolboxObject();
            String path = file.getAbsolutePath();

            ensureWritable(system, path);

            IRpgleInput input = RpgleInputFactory.createFromRemoteStreamFile(system, path);

            String validationError = RpgleFormatter.validateInput(input);
            if (validationError != null) {
                errors.add(new FileError(file, validationError));
            } else {
                executeFormatter(file, input);
            }

        } catch (Exception e) {
            FileError memberError = new FileError(file, e.getLocalizedMessage());
            errors.add(memberError);
        }
    }

    private void ensureWritable(AS400 system, String path) throws IOException, FileLockedException {

        IFSFile ifsFile = new IFSFile(system, path);
        if (!ifsFile.canWrite()) {
            throw new FileLockedException(path);
        }
    }

    private void executeFormatter(IRemoteFile file, IRpgleInput input) throws Exception, RpgleFormatterException {

        formatter.setSourceLength(Preferences.getInstance().getEndColumn(100));
        int defaultIndent = Preferences.getInstance().getStartColumn() - 1;
        FormattedResult result = formatter.format(input, defaultIndent);

        IRpgleOutput output = input.getOutput();
        if (output.writeSourceLines(result)) {
            formatted.add(file);
        } else {
            errors.add(new FileError(file, Messages.Error_Could_not_format_member));
        }
    }

    public class FileError implements IErrorObject {

        private IRemoteFile file;
        private String errorMessage;

        public FileError(IRemoteFile file, String errorMessage) {
            this.file = file;
            this.errorMessage = errorMessage;
        }

        public IRemoteFile getFile() {
            return file;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String getFullPath() {
            return file.getAbsolutePath();
        }
    }
}
