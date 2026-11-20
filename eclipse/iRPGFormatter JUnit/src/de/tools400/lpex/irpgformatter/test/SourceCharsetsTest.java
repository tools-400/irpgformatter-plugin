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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.input.SourceCharsets;

/**
 * Unit tests for the detection of the character set of a stream file, that is
 * not part of the workspace.
 */
public class SourceCharsetsTest {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    @Test
    public void detect_byteOrderMark_isUtf8() {
        byte[] bytes = "﻿**FREE\n".getBytes(StandardCharsets.UTF_8);
        assertEquals(StandardCharsets.UTF_8, SourceCharsets.detect(bytes, WINDOWS_1252));
    }

    @Test
    public void detect_validUtf8_isUtf8() {
        byte[] bytes = "// Á Í Ő 丁 ä\n".getBytes(StandardCharsets.UTF_8);
        assertEquals(StandardCharsets.UTF_8, SourceCharsets.detect(bytes, WINDOWS_1252));
    }

    @Test
    public void detect_ascii_isUtf8() {
        byte[] bytes = "**FREE\ndcl-s x char(10);\n".getBytes(StandardCharsets.US_ASCII);
        assertEquals(StandardCharsets.UTF_8, SourceCharsets.detect(bytes, WINDOWS_1252));
    }

    /**
     * An umlaut of windows-1252 is no valid UTF-8, so that the file is read
     * with the fallback, e.g. the encoding of the workspace.
     */
    @Test
    public void detect_invalidUtf8_isFallback() {
        byte[] bytes = "// äöü\n".getBytes(WINDOWS_1252);
        assertEquals(WINDOWS_1252, SourceCharsets.detect(bytes, WINDOWS_1252));
    }

    /**
     * A file, that is neither UTF-8 nor readable with the fallback, e.g. a file
     * of windows-1252 in a workspace with the encoding UTF-8, must keep its
     * bytes, when it is read and written with the detected character set.
     */
    @Test
    public void detect_invalidUtf8AndUtf8Fallback_keepsTheBytes() {
        assertKeepsTheBytes("// äöü\n".getBytes(WINDOWS_1252), StandardCharsets.UTF_8);
    }

    /**
     * 0x81 is no character of windows-1252.
     */
    @Test
    public void detect_byteThatTheFallbackCannotRead_keepsTheBytes() {
        assertKeepsTheBytes(new byte[] { '/', '/', ' ', (byte)0x81, (byte)0xE4, '\n' }, WINDOWS_1252);
    }

    private void assertKeepsTheBytes(byte[] bytes, Charset fallback) {
        Charset charset = SourceCharsets.detect(bytes, fallback);
        assertArrayEquals(charset.name(), bytes, new String(bytes, charset).getBytes(charset));
    }
}
