/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.jobs;

import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

import de.tools400.lpex.irpgformatter.handlers.jobs.FormatRemoteStreamFilesJob.FileError;

public interface IFormatRemoteStreamFilesPostRun {

    public void run(IRemoteFile[] formatted, FileError[] errors);
}
