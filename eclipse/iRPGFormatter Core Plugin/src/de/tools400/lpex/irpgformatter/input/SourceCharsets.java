/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * Detects the character set of a stream file, that has no character set of its
 * own, e.g. a file of the local file system outside of the workspace. Has no
 * Eclipse types, so that the unit tests can use it.
 */
public final class SourceCharsets {

    private SourceCharsets() {
        // Utility class
    }

    /**
     * Returns UTF-8 for valid UTF-8, with or without byte order mark, otherwise
     * the fallback, if it can read the content, otherwise ISO-8859-1, so that
     * no byte is lost, when the file is written back. Text in another character
     * set is hardly ever valid UTF-8, e.g. an umlaut of windows-1252 is not.
     * ISO-8859-1 has a character for every byte and is taken e.g. for a file of
     * windows-1252 in a workspace with the encoding UTF-8.
     *
     * @param content - the bytes of the file
     * @param fallback - the character set of other content, e.g. the encoding
     *        of the workspace
     * @return the character set of the content
     */
    public static Charset detect(byte[] content, Charset fallback) {

        if (canRead(content, StandardCharsets.UTF_8)) {
            return StandardCharsets.UTF_8;
        }

        if (canRead(content, fallback)) {
            return fallback;
        }

        return StandardCharsets.ISO_8859_1;
    }

    private static boolean canRead(byte[] content, Charset charset) {

        try {
            charset.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(content));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }
}
