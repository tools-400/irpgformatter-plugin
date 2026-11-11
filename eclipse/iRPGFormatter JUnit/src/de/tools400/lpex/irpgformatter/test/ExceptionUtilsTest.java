/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.utils.ExceptionUtils;

/**
 * Checks the message of an exception, that is shown to the user.
 */
public class ExceptionUtilsTest {

    @Test
    public void getLocalizedMessage_returnsMessage() {
        assertEquals("File not found.", ExceptionUtils.getLocalizedMessage(new IllegalStateException("File not found.")));
    }

    @Test
    public void getLocalizedMessage_withoutMessageReturnsClassName() {
        assertEquals("java.lang.NullPointerException", ExceptionUtils.getLocalizedMessage(new NullPointerException()));
    }

    @Test
    public void getLocalizedMessage_withEmptyMessageReturnsClassName() {
        assertEquals("java.lang.IllegalArgumentException", ExceptionUtils.getLocalizedMessage(new IllegalArgumentException("")));
    }

    @Test
    public void getLocalizedMessage_ofUnknownHostReturnsClassNameAndHost() {
        assertEquals("java.net.UnknownHostException: gfd400", ExceptionUtils.getLocalizedMessage(new UnknownHostException("gfd400")));
    }

    @Test
    public void getLocalizedMessage_replacesControlCharacters() {
        assertEquals("a�b�c", ExceptionUtils.getLocalizedMessage(new IllegalStateException("a\tb\u0000c")));
    }
}
