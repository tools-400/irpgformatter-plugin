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
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.utils.FileUtils;

/**
 * {@link IRpgleInput} implementation that wraps a {@link java.io.File} from
 * the local filesystem (e.g. an RSE Local Files selection that is not part
 * of an Eclipse workspace project).
 */
public class RpgleJavaIoFileInput extends AbstractRpgleInput implements IRpgleInput {

    private final File file;
    private final Charset charset;

    RpgleJavaIoFileInput(File file) {
        this.file = file;
        this.charset = Charset.defaultCharset();
    }

    @Override
    public String getFirstSourceLine() throws RpgleFormatterException {

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));

            try {

                String line = reader.readLine();
                return line;

            } finally {
                reader.close();
            }

        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_reading_file_A, file.getName()), e);
        }
    }

    @Override
    public String[] getSourceLines() throws RpgleFormatterException {

        List<String> lines = new ArrayList<>();

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));

            try {

                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } finally {
                reader.close();
            }

        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_reading_file_A, file.getName()), e);
        }

        return lines.toArray(new String[0]);
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
