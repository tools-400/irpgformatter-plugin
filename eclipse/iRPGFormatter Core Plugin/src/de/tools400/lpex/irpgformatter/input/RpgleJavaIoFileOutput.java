/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.utils.FileUtils;

/**
 * Writes formatted source lines back to a {@link java.io.File}. The line
 * separator of the file is kept.
 */
public class RpgleJavaIoFileOutput implements IRpgleOutput {

    private final File file;
    private final Charset charset;

    RpgleJavaIoFileOutput(File file, Charset charset) {
        this.file = file;
        this.charset = charset;
    }

    @Override
    public boolean writeSourceLines(FormattedResult result) throws RpgleFormatterException {

        try {

            String content = FileUtils.joinLines(result.toLines(), getLineSeparator());

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file, false), charset)) {
                writer.write(content);
            }

            return true;

        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_writing_file_A, file.getName()), e);
        }
    }

    /**
     * Returns the line separator of the existing file, so that formatting does
     * not change it.
     */
    private String getLineSeparator() throws IOException {

        if (!file.exists()) {
            return System.lineSeparator();
        }

        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))) {
            return FileUtils.detectLineSeparator(reader, System.lineSeparator());
        }
    }
}
