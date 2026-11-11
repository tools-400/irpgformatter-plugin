/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.FormattedStatement;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;

/**
 * Checks that the output of the local file system keeps the line separator of
 * the file; the output of the workspace needs the platform.
 */
public class StreamFileOutputTest extends AbstractTestCase {

    private static final String[] LINES = { "**free", "dcl-s f int(10);", "return;" };

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
