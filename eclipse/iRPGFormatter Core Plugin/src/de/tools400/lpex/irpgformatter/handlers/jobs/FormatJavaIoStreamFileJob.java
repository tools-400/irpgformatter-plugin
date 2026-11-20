/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.jobs;

import java.io.File;
import java.nio.charset.Charset;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FileLockedException;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;
import de.tools400.lpex.irpgformatter.problems.IProblemSource;
import de.tools400.lpex.irpgformatter.problems.LocalFileSource;

/**
 * Formats files of the local file system, that are not part of the workspace.
 */
public class FormatJavaIoStreamFileJob extends AbstractFormatJob<File> {

    public FormatJavaIoStreamFileJob(File[] files, IFormatPostRun postRun) {
        super(Messages.Job_Formatting_stream_files, files, postRun);
    }

    @Override
    protected String getPath(File file) {
        return file.getAbsolutePath();
    }

    @Override
    protected IProblemSource createProblemSource(File file) {
        return new LocalFileSource(file);
    }

    @Override
    protected void checkAvailability(File file) throws FileLockedException {

        if (file.exists() && !file.canWrite()) {
            throw new FileLockedException(file.getAbsolutePath());
        }
    }

    @Override
    protected IRpgleInput createInput(File file, IProgressMonitor monitor) {

        // A file outside of the workspace has no character set of its own:
        // a file, that is not UTF-8, is read with the encoding of the
        // workspace, if it can read the file.
        return RpgleInputFactory.createFromJavaIoFile(file, Charset.forName(ResourcesPlugin.getEncoding()));
    }
}
