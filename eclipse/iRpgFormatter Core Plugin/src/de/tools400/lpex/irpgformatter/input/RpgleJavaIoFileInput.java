/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import de.tools400.lpex.irpgformatter.utils.FileUtils;

/**
 * {@link IRpgleInput} implementation that wraps a {@link java.io.File} from the
 * local filesystem (e.g. an RSE Local Files selection that is not part of an
 * Eclipse workspace project).
 */
public class RpgleJavaIoFileInput extends AbstractStreamFileInput {

    private final File file;
    private final Charset charset;

    RpgleJavaIoFileInput(File file) {
        this.file = file;
        this.charset = Charset.defaultCharset();
    }

    @Override
    protected Reader openReader() throws Exception {
        return new InputStreamReader(new FileInputStream(file), charset);
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
        return new RpgleJavaIoFileOutput(file, charset);
    }
}
