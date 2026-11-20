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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;
import de.tools400.lpex.irpgformatter.utils.ExceptionUtils;

/**
 * Checks the reading of the stream file inputs with the input of the local file
 * system; the inputs of the workspace and of the IFS need the platform or a
 * system.
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

    /**
     * The message tells the reason, e.g. a missing authority, because a batch
     * job shows only the message.
     */
    @Test
    public void getSourceLines_ofMissingFileTellsTheReason() {

        IRpgleInput input = RpgleInputFactory.createFromJavaIoFile(new File(folder.getRoot(), "missing.rpgle"));

        try {
            input.getSourceLines();
            fail("RpgleFormatterException expected");
        } catch (RpgleFormatterException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(ExceptionUtils.getLocalizedMessage(e.getCause())));
        }
    }

    // --- character set and byte order mark of files outside the workspace ---

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    /**
     * A UTF-8 file is read as UTF-8, also if the fallback, e.g. the encoding of
     * the workspace, is windows-1252, which lacks some of its characters.
     */
    @Test
    public void getSourceLines_ofUtf8File_readsUtf8() throws Exception {

        File file = createFile("**free\n// Á Í Ő 丁\n".getBytes(StandardCharsets.UTF_8));

        IRpgleInput input = RpgleInputFactory.createFromJavaIoFile(file, WINDOWS_1252);

        assertEquals("// Á Í Ő 丁", input.getSourceLines()[1]);
    }

    @Test
    public void getSourceLines_ofNonUtf8File_readsFallback() throws Exception {

        File file = createFile("**free\n// äöü\n".getBytes(WINDOWS_1252));

        IRpgleInput input = RpgleInputFactory.createFromJavaIoFile(file, WINDOWS_1252);

        assertEquals("// äöü", input.getSourceLines()[1]);
    }

    @Test
    public void getFirstSourceLine_ofFileWithByteOrderMark_isFreeFormat() throws Exception {

        File file = createFile("﻿**FREE\ndcl-s x char(10);\n".getBytes(StandardCharsets.UTF_8));

        IRpgleInput input = RpgleInputFactory.createFromJavaIoFile(file, WINDOWS_1252);

        assertEquals("**FREE", input.getFirstSourceLine());
        assertTrue(input.isFreeFormat());
        assertEquals("**FREE", input.getSourceLines()[0]);
    }

    private File createFile(byte[] content) throws Exception {

        File file = folder.newFile("source.rpgle");
        Files.write(file.toPath(), content);

        return file;
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
