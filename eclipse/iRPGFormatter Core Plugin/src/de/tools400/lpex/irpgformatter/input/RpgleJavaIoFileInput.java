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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;

import de.tools400.lpex.irpgformatter.utils.FileUtils;

/**
 * {@link IRpgleInput} implementation that wraps a {@link java.io.File} from the
 * local filesystem (e.g. an RSE Local Files selection that is not part of an
 * Eclipse workspace project). The file has no character set of its own: it is
 * read and written as UTF-8, if it is UTF-8, otherwise with the fallback, if it
 * can read the file, otherwise as ISO-8859-1, see
 * {@link SourceCharsets#detect(byte[], Charset)}.
 */
public class RpgleJavaIoFileInput extends AbstractStreamFileInput {

    private final File file;
    private final Charset fallbackCharset;
    private Charset charset;

    RpgleJavaIoFileInput(File file, Charset fallbackCharset) {
        this.file = file;
        this.fallbackCharset = fallbackCharset;
    }

    @Override
    protected Reader openReader() throws Exception {
        return new InputStreamReader(new FileInputStream(file), getCharset());
    }

    /**
     * Returns the character set of the file. It is detected once, so that the
     * output writes the file with the character set, with which it was read.
     */
    private Charset getCharset() throws IOException {

        if (charset == null) {
            charset = SourceCharsets.detect(Files.readAllBytes(file.toPath()), fallbackCharset);
        }

        return charset;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    public String getSourceType() throws Exception {
        return FileUtils.getExtension(getName());
    }

    /**
     * Returns the output, that writes the file with the character set and the
     * byte order mark, with which it was read. A file, that was not read, e.g.
     * a new file, is written with the fallback.
     */
    @Override
    public IRpgleOutput getOutput() {
        return new RpgleJavaIoFileOutput(file, charset != null ? charset : fallbackCharset, hasByteOrderMark());
    }
}
