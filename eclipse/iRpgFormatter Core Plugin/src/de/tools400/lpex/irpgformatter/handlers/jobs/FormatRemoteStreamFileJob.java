/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

import com.ibm.as400.access.IFSFile;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FileLockedException;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;

/**
 * Formats stream files of the IFS.
 */
public class FormatRemoteStreamFileJob extends AbstractFormatJob<IRemoteFile> {

    public FormatRemoteStreamFileJob(IRemoteFile[] files, IFormatPostRun postRun) {
        super(Messages.Job_Formatting_remote_stream_files, files, postRun);
    }

    @Override
    protected String getPath(IRemoteFile file) {
        return file.getAbsolutePath();
    }

    @Override
    protected void checkAvailability(IRemoteFile file) throws Exception {

        IBMiConnection connection = IBMiConnection.getConnection(file.getHost());
        ensureConnected(connection);

        String path = file.getAbsolutePath();
        IFSFile ifsFile = new IFSFile(connection.getAS400ToolboxObject(), path);
        if (!ifsFile.canWrite()) {
            throw new FileLockedException(path);
        }
    }

    @Override
    protected IRpgleInput createInput(IRemoteFile file, IProgressMonitor monitor) throws Exception {

        IBMiConnection connection = IBMiConnection.getConnection(file.getHost());
        return RpgleInputFactory.createFromRemoteStreamFile(connection.getAS400ToolboxObject(), file.getAbsolutePath());
    }
}
