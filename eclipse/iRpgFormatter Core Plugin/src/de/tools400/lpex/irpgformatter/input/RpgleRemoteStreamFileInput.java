/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSFileInputStream;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.utils.FileUtils;

/**
 * {@link IRpgleInput} implementation that wraps a remote IFS stream file
 * accessed via JT400.
 */
public class RpgleRemoteStreamFileInput extends AbstractRpgleInput implements IRpgleInput {

    private final AS400 system;
    private final String path;

    RpgleRemoteStreamFileInput(AS400 system, String path) {
        this.system = system;
        this.path = path;
    }

    @Override
    public String getFirstSourceLine() throws RpgleFormatterException {

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(new IFSFileInputStream(system, path), "UTF-8"));

            try {

                String line = reader.readLine();
                return line;

            } finally {
                reader.close();
            }

        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_reading_file_A, path), e);
        }
    }

    @Override
    public String[] getSourceLines() throws RpgleFormatterException {

        List<String> lines = new ArrayList<>();

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(new IFSFileInputStream(system, path), "UTF-8"));

            try {

                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } finally {
                reader.close();
            }

        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_reading_file_A, path), e);
        }

        return lines.toArray(new String[0]);
    }

    @Override
    public String getName() {
        return path;
    }

    public String getSourceType() throws Exception {
        return FileUtils.getExtension(path);
    }

    @Override
    public IRpgleOutput getOutput() {
        return new RpgleRemoteStreamFileOutput(system, path);
    }

    @Override
    public String toString() {

        String buffer = String.format("Remote Stream File: %s", getName());

        return buffer;
    }
}
