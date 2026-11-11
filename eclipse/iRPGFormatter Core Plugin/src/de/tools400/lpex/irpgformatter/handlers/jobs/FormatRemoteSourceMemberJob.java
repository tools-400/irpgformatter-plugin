/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.jobs;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.as400.access.IFSFile;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FileLockedException;
import de.tools400.lpex.irpgformatter.handlers.SourceMember;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;

/**
 * Formats source members.
 */
public class FormatRemoteSourceMemberJob extends AbstractFormatJob<SourceMember> {

    public FormatRemoteSourceMemberJob(SourceMember[] sourceMembers, IFormatPostRun postRun) {
        super(Messages.Job_Formatting_remote_source_members, sourceMembers, postRun);
    }

    @Override
    protected String getPath(SourceMember sourceMember) {
        return sourceMember.toString();
    }

    @Override
    protected void checkAvailability(SourceMember sourceMember) throws Exception {

        IBMiConnection connection = getConnection(sourceMember);
        ensureConnected(connection);

        String library = sourceMember.getLibraryName();
        String file = sourceMember.getFileName();
        String member = sourceMember.getMemberName();

        String memberPath = "/QSYS.LIB/" + library + ".LIB/" + file + ".FILE/" + member + ".MBR";
        IFSFile ifsFile = new IFSFile(connection.getAS400ToolboxObject(), memberPath);
        if (!ifsFile.canWrite()) {
            throw new FileLockedException(sourceMember.toString());
        }
    }

    @Override
    protected IRpgleInput createInput(SourceMember sourceMember, IProgressMonitor monitor) throws Exception {
        return RpgleInputFactory.createFromEditableRemoteMember(getConnection(sourceMember), sourceMember.getLibraryName(),
            sourceMember.getFileName(), sourceMember.getMemberName(), monitor);
    }

    @Override
    protected int getSourceLength(SourceMember sourceMember) throws Exception {
        return sourceMember.getSourceLength();
    }

    private IBMiConnection getConnection(SourceMember sourceMember) {
        return IBMiConnection.getConnection(sourceMember.getProfileName(), sourceMember.getConnectionName());
    }
}
