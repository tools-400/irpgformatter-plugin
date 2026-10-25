/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;

/**
 * Checks the reading of the stream file inputs with the input of the local
 * file system; the inputs of the workspace and of the IFS need the platform
 * or a system.
 */
public class StreamFileInputTest extends AbstractTestCase {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void getFirstSourceLine_returnsFirstLine() throws Exception {

        IRpgleInput input = RpgleInputFactory.createFromJavaIoFile(createFile("**free", "dcl-s f int(10);", "return;"));

        assertEquals("**free", input.getFirstSourceLine());
        assertTrue(input.isFreeFormat());
    }

    @Test
    public void getFirstSourceLine_ofEmptyFileIsNull() throws Exception {

        IRpgleInput input = RpgleInputFactory.createFromJavaIoFile(createFile());

        assertNull(input.getFirstSourceLine());
    }

    @Test
    public void getSourceLines_returnsAllLines() throws Exception {

        IRpgleInput input = RpgleInputFactory.createFromJavaIoFile(createFile("**free", "dcl-s f int(10);", "return;"));

        assertArrayEquals(new String[] { "**free", "dcl-s f int(10);", "return;" }, input.getSourceLines());
    }

    @Test
    public void getSourceLines_ofMissingFileNamesTheFile() {

        IRpgleInput input = RpgleInputFactory.createFromJavaIoFile(new File(folder.getRoot(), "missing.rpgle"));

        try {
            input.getSourceLines();
            fail("RpgleFormatterException expected");
        } catch (RpgleFormatterException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("missing.rpgle"));
        }
    }

    private File createFile(String... lines) throws Exception {

        File file = folder.newFile("source.rpgle");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charset.defaultCharset())) {
            for (String line : lines) {
                writer.write(line);
                writer.write("\n");
            }
        }

        return file;
    }
}
