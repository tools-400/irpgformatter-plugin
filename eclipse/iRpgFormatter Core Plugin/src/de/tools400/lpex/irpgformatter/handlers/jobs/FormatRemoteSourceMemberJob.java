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
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSFile;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FileLockedException;
import de.tools400.lpex.irpgformatter.formatter.FormatError;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.handlers.SourceMember;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.input.IRpgleOutput;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.utils.ErrorGroup;
import de.tools400.lpex.irpgformatter.utils.ExceptionUtils;

public class FormatRemoteSourceMemberJob extends Job {

    private SourceMember[] sourceMembers;
    private RpgleFormatter formatter;
    private IFormatRemoteSourceMembersPostRun postRun;

    private IProgressMonitor monitor;
    private List<MemberError> errors;
    private List<SourceMember> formatted;
    private List<ErrorGroup> statementErrors;

    public FormatRemoteSourceMemberJob(SourceMember[] sourceMembers, IFormatRemoteSourceMembersPostRun postRun) {
        super(Messages.Job_Formatting_remote_source_members);

        this.sourceMembers = sourceMembers;
        this.formatter = new RpgleFormatter();
        this.formatter.setSourceLength(80);
        this.postRun = postRun;

        this.errors = new LinkedList<>();
        this.formatted = new LinkedList<>();
        this.statementErrors = new LinkedList<>();
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

        this.monitor = monitor;

        try {

            int totalNumberOfMembers = sourceMembers.length;

            monitor.beginTask(Messages.Job_Formatting, totalNumberOfMembers);

            int count = 0;

            while (count < totalNumberOfMembers) {

                SourceMember sourceMember = sourceMembers[count];

                monitor.setTaskName(sourceMember.toString());
                executeFormatter(sourceMember);

                monitor.worked(1);

                count++;
            }

        } catch (Exception e) {
            ExceptionUtils.handleBatchException(e);
        }

        monitor.done();

        if (postRun != null) {
            postRun.postRun(formatted.toArray(new SourceMember[formatted.size()]), errors.toArray(new MemberError[errors.size()]),
                statementErrors.toArray(new ErrorGroup[statementErrors.size()]));
        }

        return Status.OK_STATUS;
    }

    private void executeFormatter(SourceMember sourceMember) throws RpgleFormatterException, Exception {

        try {

            String profileName = sourceMember.getProfileName();
            String connectionName = sourceMember.getConnectionName();
            IBMiConnection connection = IBMiConnection.getConnection(profileName, connectionName);

            monitor.subTask(Messages.SubTask_Checking_availability);
            ensureWritable(connection, sourceMember);

            monitor.subTask(Messages.SubTask_Reading);

            String library = sourceMember.getLibraryName();
            String file = sourceMember.getFileName();
            String member = sourceMember.getMemberName();

            IRpgleInput input = RpgleInputFactory.createFromEditableRemoteMember(connection, library, file, member, monitor);

            String validationError = RpgleFormatter.validateInput(input);
            if (validationError != null) {
                errors.add(new MemberError(sourceMember, validationError));
            } else {
                executeFormatter(sourceMember, input);
            }

        } catch (Exception e) {
            e.printStackTrace();
            MemberError memberError = new MemberError(sourceMember, e.getLocalizedMessage());
            errors.add(memberError);
        }
    }

    private void ensureWritable(IBMiConnection connection, SourceMember sourceMember)
        throws SystemMessageException, IOException, FileLockedException {

        String library = sourceMember.getLibraryName();
        String file = sourceMember.getFileName();
        String member = sourceMember.getMemberName();

        AS400 system = connection.getAS400ToolboxObject();
        String memberPath = "/QSYS.LIB/" + library + ".LIB/" + file + ".FILE/" + member + ".MBR";
        IFSFile ifsFile = new IFSFile(system, memberPath);

        if (!ifsFile.canWrite()) {
            throw new FileLockedException(sourceMember.toString());
        }
    }

    private void executeFormatter(SourceMember sourceMember, IRpgleInput input) throws Exception, RpgleFormatterException {

        formatter.setSourceLength(sourceMember.getRecordLength());
        int defaultIndent = Preferences.getInstance().getStartColumn() - 1;

        monitor.subTask(Messages.SubTask_Formatting);
        FormattedResult result = formatter.format(input, defaultIndent);

        monitor.subTask(Messages.SubTask_Writing);
        IRpgleOutput output = input.getOutput();
        if (output.writeSourceLines(result)) {
            formatted.add(sourceMember);
            if (formatter.getErrorCount() > 0) {
                statementErrors.add(new ErrorGroup(sourceMember.toString(), toDetailMessages(formatter.getErrors())));
            }
        } else {
            errors.add(new MemberError(sourceMember, Messages.Error_Could_not_format_member));
        }
    }

    private static String[] toDetailMessages(List<FormatError> formatErrors) {
        String[] details = new String[formatErrors.size()];
        for (int i = 0; i < formatErrors.size(); i++) {
            FormatError fe = formatErrors.get(i);
            details[i] = Messages.bind(Messages.Error_Line_A_message_B, fe.getStartLineNumber() + 1, fe.getMessage());
        }
        return details;
    }

    public class MemberError implements IErrorObject {

        private SourceMember sourceMember;
        private String errorMessage;

        public MemberError(SourceMember sourceMember, String errorMessage) {
            this.sourceMember = sourceMember;
            this.errorMessage = errorMessage;
        }

        public SourceMember getSourceMember() {
            return sourceMember;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String getFullPath() {
            return sourceMember.toString();
        }
    }
}
