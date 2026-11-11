/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;

import de.tools400.lpex.irpgformatter.handlers.jobs.FormatStreamFileJob;
import de.tools400.lpex.irpgformatter.handlers.resolvers.LocalStreamFileResolver;

public class FormatLocalStreamFileHandler extends AbstractFormatHandler {

    private IFile[] files;

    public FormatLocalStreamFileHandler() {
        super(SourceKind.STREAM_FILES);
    }

    @Override
    public int resolveItems(IStructuredSelection selection, IProgressMonitor monitor) throws ExecutionException {

        LocalStreamFileResolver resolver = new LocalStreamFileResolver(monitor);
        files = resolver.resolveLocalStreamFiles(selection);
        checkResolved(files.length, resolver);

        return files.length;
    }

    @Override
    protected void scheduleFormatterJob() {

        FormatStreamFileJob job = new FormatStreamFileJob(files, this);
        job.schedule();
    }
}
