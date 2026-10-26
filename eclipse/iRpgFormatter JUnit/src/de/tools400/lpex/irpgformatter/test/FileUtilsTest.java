/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.utils.FileUtils;

public class FileUtilsTest {

    private static final String DEFAULT = "<default>";

    // --- detectLineSeparator ---

    @Test
    public void detectLineSeparator_crlf() throws IOException {
        assertEquals("\r\n", detect("**free\r\ndcl-s a int(10);\n"));
    }

    @Test
    public void detectLineSeparator_lf() throws IOException {
        assertEquals("\n", detect("**free\ndcl-s a int(10);\r\n"));
    }

    @Test
    public void detectLineSeparator_noLineFeed() throws IOException {
        assertEquals(DEFAULT, detect("**free"));
    }

    @Test
    public void detectLineSeparator_carriageReturnOnly() throws IOException {
        assertEquals(DEFAULT, detect("**free\rdcl-s a int(10);"));
    }

    @Test
    public void detectLineSeparator_emptyText() throws IOException {
        assertEquals(DEFAULT, detect(""));
    }

    // --- joinLines ---

    @Test
    public void joinLines_endsEachLine() {
        assertEquals("**free\r\nreturn;\r\n", FileUtils.joinLines(new String[] { "**free", "return;" }, "\r\n"));
    }

    @Test
    public void joinLines_noLines() {
        assertEquals("", FileUtils.joinLines(new String[0], "\n"));
    }

    private String detect(String text) throws IOException {
        return FileUtils.detectLineSeparator(new StringReader(text), DEFAULT);
    }
}
