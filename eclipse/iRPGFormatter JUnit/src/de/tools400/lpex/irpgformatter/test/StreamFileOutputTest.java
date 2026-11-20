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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.FormattedStatement;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;
import de.tools400.lpex.irpgformatter.utils.ExceptionUtils;

/**
 * Checks that the output of the local file system keeps the line separator of
 * the file; the output of the workspace needs the platform.
 */
public class StreamFileOutputTest extends AbstractTestCase {

    private static final String[] LINES = { "**free", "dcl-s f int(10);", "return;" };

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void writeSourceLines_keepsCrlf() throws Exception {
        assertEquals("**free\r\ndcl-s f int(10);\r\nreturn;\r\n", writeLines("**free\r\ndcl-s f int(10);\r\nreturn;\r\n"));
    }

    @Test
    public void writeSourceLines_keepsLf() throws Exception {
        assertEquals("**free\ndcl-s f int(10);\nreturn;\n", writeLines("**free\ndcl-s f int(10);\nreturn;\n"));
    }

    @Test
    public void writeSourceLines_endsLastLine() throws Exception {
        assertEquals("**free\ndcl-s f int(10);\nreturn;\n", writeLines("**free\ndcl-s f int(10);\nreturn;"));
    }

    /**
     * The message tells the reason, e.g. a locked file, because a batch job
     * shows only the message.
     */
    @Test
    public void writeSourceLines_intoFolderTellsTheReason() throws Exception {

        File notAFile = folder.newFolder("source.rpgle");
        FormattedResult result = new FormattedResult(new FormattedStatement[] { new FormattedStatement(1, LINES.length, LINES) });

        try {
            RpgleInputFactory.createFromJavaIoFile(notAFile).getOutput().writeSourceLines(result);
            fail("RpgleFormatterException expected");
        } catch (RpgleFormatterException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("source.rpgle"));
            assertTrue(e.getMessage(), e.getMessage().contains(ExceptionUtils.getLocalizedMessage(e.getCause())));
        }
    }

    // --- character set and byte order mark of files outside the workspace ---

    /**
     * A UTF-8 file is written back as UTF-8, also if the fallback, e.g. the
     * encoding of the workspace, is windows-1252, which lacks some of its
     * characters.
     */
    @Test
    public void writeSourceLines_keepsUtf8() throws Exception {
        assertUnchanged("**free\n// Á Í Ő 丁\n".getBytes(StandardCharsets.UTF_8), WINDOWS_1252);
    }

    @Test
    public void writeSourceLines_keepsByteOrderMark() throws Exception {
        assertUnchanged("﻿**free\ndcl-s f int(10);\n".getBytes(StandardCharsets.UTF_8), WINDOWS_1252);
    }

    /**
     * A file of windows-1252 keeps its umlauts also in a workspace with the
     * encoding UTF-8, which cannot read them.
     */
    @Test
    public void writeSourceLines_ofNonUtf8FileAndUtf8Fallback_keepsTheBytes() throws Exception {
        assertUnchanged("**free\n// äöü ß €\n".getBytes(WINDOWS_1252), StandardCharsets.UTF_8);
    }

    /**
     * Reads the lines of a file with the given content and writes them back
     * unchanged: the file must keep its bytes.
     */
    private void assertUnchanged(byte[] content, Charset fallbackCharset) throws Exception {

        File file = folder.newFile("source.rpgle");
        Files.write(file.toPath(), content);

        IRpgleInput input = RpgleInputFactory.createFromJavaIoFile(file, fallbackCharset);
        String[] lines = input.getSourceLines();
        input.getOutput().writeSourceLines(new FormattedResult(new FormattedStatement[] { new FormattedStatement(1, lines.length, lines) }));

        assertArrayEquals(content, Files.readAllBytes(file.toPath()));
    }

    /**
     * Writes {@link #LINES} with the output into a file with the given content
     * and returns the new content.
     */
    private String writeLines(String content) throws Exception {

        File file = folder.newFile("source.rpgle");
        Files.write(file.toPath(), content.getBytes(Charset.defaultCharset()));

        FormattedResult result = new FormattedResult(new FormattedStatement[] { new FormattedStatement(1, LINES.length, LINES) });
        RpgleInputFactory.createFromJavaIoFile(file).getOutput().writeSourceLines(result);

        return new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
    }
}
