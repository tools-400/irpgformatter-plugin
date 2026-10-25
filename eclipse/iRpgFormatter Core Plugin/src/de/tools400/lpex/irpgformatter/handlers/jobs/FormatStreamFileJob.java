/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.jobs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FileLockedException;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;

/**
 * Formats stream files of the workspace.
 */
public class FormatStreamFileJob extends AbstractFormatJob<IFile> {

    public FormatStreamFileJob(IFile[] files, IFormatPostRun postRun) {
        super(Messages.Job_Formatting_stream_files, files, postRun);
    }

    @Override
    protected String getPath(IFile file) {
        return file.getFullPath().toOSString();
    }

    @Override
    protected void checkAvailability(IFile file) throws FileLockedException {

        if (file.isReadOnly()) {
            throw new FileLockedException(file.getFullPath().toOSString());
        }
    }

    @Override
    protected IRpgleInput createInput(IFile file, IProgressMonitor monitor) {
        return RpgleInputFactory.createFromStreamFile(file);
    }
}
