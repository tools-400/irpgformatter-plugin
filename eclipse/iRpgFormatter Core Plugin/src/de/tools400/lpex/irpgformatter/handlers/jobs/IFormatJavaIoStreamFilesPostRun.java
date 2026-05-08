/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.jobs;

import java.io.File;

import de.tools400.lpex.irpgformatter.handlers.jobs.FormatJavaIoStreamFileJob.FileError;
import de.tools400.lpex.irpgformatter.utils.ErrorGroup;

public interface IFormatJavaIoStreamFilesPostRun {

    /**
     * @param formatted files whose formatted source was written back
     * @param errors files that could not be written at all
     * @param statementErrors written files that nevertheless contain
     *        individual statement-level errors; one group per affected file
     */
    public void run(File[] formatted, FileError[] errors, ErrorGroup[] statementErrors);
}
