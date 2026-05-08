/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;

/**
 * Writes formatted source lines back to a {@link java.io.File}.
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

            String[] lines = result.toLines();
            String content = String.join(System.lineSeparator(), lines);

            Writer writer = new OutputStreamWriter(new FileOutputStream(file, false), charset);
            try {
                writer.write(content);
            } finally {
                writer.close();
            }

            return true;

        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_writing_file_A, file.getName()), e);
        }
    }
}
