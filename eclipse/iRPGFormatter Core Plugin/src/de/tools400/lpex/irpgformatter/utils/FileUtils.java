/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import java.io.IOException;
import java.io.Reader;

public class FileUtils {

    private static final String CRLF = "\r\n";
    private static final String LF = "\n";

    private FileUtils() {
        // Utility class
    }

    public static String getExtension(String path) {

        if (path == null || !path.contains(".")) {
            return "";
        }

        return path.substring(path.lastIndexOf(".") + 1);
    }

    /**
     * Returns the line separator of the first line, CRLF or LF, or the default,
     * if the text has no line feed. Reads up to the first line feed; the caller
     * closes the reader.
     *
     * @param reader reader of the text, preferably buffered
     * @param defaultSeparator separator of a text without line feed
     * @return the line separator
     */
    public static String detectLineSeparator(Reader reader, String defaultSeparator) throws IOException {

        int previous = -1;
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '\n') {
                if (previous == '\r') {
                    return CRLF;
                }
                return LF;
            }
            previous = c;
        }

        return defaultSeparator;
    }

    /**
     * Joins the lines to the content of a file. Each line ends with the line
     * separator, the last one, too.
     */
    public static String joinLines(String[] lines, String lineSeparator) {

        StringBuilder buffer = new StringBuilder();
        for (String line : lines) {
            buffer.append(line);
            buffer.append(lineSeparator);
        }

        return buffer.toString();
    }
}
