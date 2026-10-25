/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;

/**
 * Base class of the inputs of stream files: files of the workspace, of the
 * local file system and of the IFS. Reads the lines of the file.
 */
public abstract class AbstractStreamFileInput extends AbstractRpgleInput implements IRpgleInput {

    /**
     * Opens a reader of the file with its character set. The caller closes it.
     */
    protected abstract Reader openReader() throws Exception;

    @Override
    public String getFirstSourceLine() throws RpgleFormatterException {

        try (BufferedReader reader = new BufferedReader(openReader())) {
            return reader.readLine();
        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_reading_file_A, getName()), e);
        }
    }

    @Override
    public String[] getSourceLines() throws RpgleFormatterException {

        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(openReader())) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_reading_file_A, getName()), e);
        }

        return lines.toArray(new String[0]);
    }
}
