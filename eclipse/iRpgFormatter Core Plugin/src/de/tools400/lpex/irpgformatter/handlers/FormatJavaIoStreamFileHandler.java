/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import java.io.File;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;

import de.tools400.lpex.irpgformatter.handlers.jobs.FormatJavaIoStreamFileJob;
import de.tools400.lpex.irpgformatter.handlers.resolvers.JavaIoStreamFileResolver;

public class FormatJavaIoStreamFileHandler extends AbstractFormatHandler {

    private File[] files;

    public FormatJavaIoStreamFileHandler() {
        super(SourceKind.STREAM_FILES);
    }

    @Override
    public int resolveItems(IStructuredSelection selection, IProgressMonitor monitor) throws ExecutionException {

        JavaIoStreamFileResolver resolver = new JavaIoStreamFileResolver(monitor);
        files = resolver.resolveStreamFiles(selection);
        checkResolved(files.length, resolver);

        return files.length;
    }

    @Override
    protected void scheduleFormatterJob() {

        FormatJavaIoStreamFileJob job = new FormatJavaIoStreamFileJob(files, this);
        job.schedule();
    }
}
