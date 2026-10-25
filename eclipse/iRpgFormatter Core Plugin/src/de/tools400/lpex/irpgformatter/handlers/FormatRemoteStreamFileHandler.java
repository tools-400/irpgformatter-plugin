/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

import de.tools400.lpex.irpgformatter.handlers.jobs.FormatRemoteStreamFileJob;
import de.tools400.lpex.irpgformatter.handlers.resolvers.RemoteStreamFileResolver;

public class FormatRemoteStreamFileHandler extends AbstractFormatHandler {

    private IRemoteFile[] files;

    public FormatRemoteStreamFileHandler() {
        super(SourceKind.STREAM_FILES);
    }

    @Override
    public int resolveItems(IStructuredSelection selection, IProgressMonitor monitor) throws ExecutionException {

        RemoteStreamFileResolver resolver = new RemoteStreamFileResolver(monitor);
        files = resolver.resolveRemoteStreamFiles(selection);
        checkResolved(files.length, resolver);

        return files.length;
    }

    @Override
    protected void scheduleFormatterJob() {

        FormatRemoteStreamFileJob job = new FormatRemoteStreamFileJob(files, this);
        job.schedule();
    }
}
