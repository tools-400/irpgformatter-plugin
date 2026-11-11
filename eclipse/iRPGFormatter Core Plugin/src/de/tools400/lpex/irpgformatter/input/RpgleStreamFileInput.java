/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;

import de.tools400.lpex.irpgformatter.utils.FileUtils;

/**
 * {@link IRpgleInput} implementation that wraps an Eclipse IFile.
 */
public class RpgleStreamFileInput extends AbstractStreamFileInput {

    private final IFile file;

    RpgleStreamFileInput(IFile file) {
        this.file = file;
    }

    @Override
    protected Reader openReader() throws Exception {

        // The charset first, so that no stream is left open, if it fails.
        String charset = file.getCharset();

        return new InputStreamReader(file.getContents(), charset);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    public String getSourceType() throws Exception {
        return FileUtils.getExtension(getName());
    }

    @Override
    public IRpgleOutput getOutput() {
        return new RpgleStreamFileOutput(file);
    }
}
