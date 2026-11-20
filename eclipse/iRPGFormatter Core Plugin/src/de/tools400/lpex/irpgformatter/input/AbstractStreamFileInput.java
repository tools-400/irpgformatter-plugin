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
import de.tools400.lpex.irpgformatter.utils.ExceptionUtils;

/**
 * Base class of the inputs of stream files: files of the workspace, of the
 * local file system and of the IFS. Reads the lines of the file. A byte order
 * mark at the start of the file is not part of the first line; the output
 * writes it again, see {@link #hasByteOrderMark()}.
 */
public abstract class AbstractStreamFileInput extends AbstractRpgleInput implements IRpgleInput {

    /**
     * The byte order mark, as a reader returns it for every Unicode character
     * set, e.g. for the bytes <code>EF BB BF</code> of UTF-8.
     */
    static final char BYTE_ORDER_MARK = '﻿';

    private boolean byteOrderMark;

    /**
     * Opens a reader of the file with its character set. The caller closes it.
     */
    protected abstract Reader openReader() throws Exception;

    /**
     * Returns <code>true</code>, if the file, that was read, starts with a byte
     * order mark, so that the output writes it again.
     */
    protected boolean hasByteOrderMark() {
        return byteOrderMark;
    }

    @Override
    public String getFirstSourceLine() throws RpgleFormatterException {

        try (BufferedReader reader = new BufferedReader(openReader())) {
            return removeByteOrderMark(reader.readLine());
        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_reading_file_A_B, getName(), ExceptionUtils.getLocalizedMessage(e)),
                e);
        }
    }

    @Override
    public String[] getSourceLines() throws RpgleFormatterException {

        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(openReader())) {
            String line = removeByteOrderMark(reader.readLine());
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_reading_file_A_B, getName(), ExceptionUtils.getLocalizedMessage(e)),
                e);
        }

        return lines.toArray(new String[0]);
    }

    /**
     * Removes the byte order mark from the first line, so that e.g.
     * <code>**FREE</code> is recognized, and remembers it.
     */
    private String removeByteOrderMark(String firstLine) {

        if (firstLine != null && !firstLine.isEmpty() && firstLine.charAt(0) == BYTE_ORDER_MARK) {
            byteOrderMark = true;
            return firstLine.substring(1);
        }

        return firstLine;
    }
}
