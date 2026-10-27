/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.utils.FileUtils;

/**
 * Writes formatted source lines back to an Eclipse IFile. The line separator of
 * the file is kept.
 */
public class RpgleStreamFileOutput implements IRpgleOutput {

    private final IFile file;

    RpgleStreamFileOutput(IFile file) {
        this.file = file;
    }

    @Override
    public boolean writeSourceLines(FormattedResult result) throws RpgleFormatterException {

        try {

            Charset charset = Charset.forName(file.getCharset());
            String content = FileUtils.joinLines(result.toLines(), getLineSeparator(charset));
            InputStream stream = new ByteArrayInputStream(content.getBytes(charset));
            file.setContents(stream, true, true, null);

            return true;

        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_writing_file_A, file.getName()), e);
        }
    }

    /**
     * Returns the line separator of the existing file, so that formatting does
     * not change it.
     */
    private String getLineSeparator(Charset charset) throws CoreException, IOException {

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getContents(), charset))) {
            return FileUtils.detectLineSeparator(reader, System.lineSeparator());
        }
    }
}
