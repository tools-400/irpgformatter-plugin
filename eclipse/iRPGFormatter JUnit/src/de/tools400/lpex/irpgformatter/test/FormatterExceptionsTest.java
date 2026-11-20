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

import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;

/**
 * Unit tests for statements, that the formatter left unchanged with an
 * unexpected error: names with national characters.
 */
public class FormatterExceptionsTest extends AbstractTestCase {

    // --- Names with national characters ---

    /**
     * The characters $, # and @ appear as different symbols on some code pages.
     * In CCSID 277 # is Æ, so that the compiler accepts the name Ære in a
     * member with CCSID 277, but not in a member with CCSID 273.
     */
    @Test
    public void format_subFieldWithNationalCharacter_isAligned() throws RpgleFormatterException {

        RpgleFormatter formatter = createFormatter(FormatterConfig.fromPreferences(), 100);

        String[] result = format(formatter, "**FREE", "dcl-ds kunde qualified;", "  Ære   packed(5:0);", "  navn char(30);", "end-ds;");

        assertArrayEquals(new String[] { "**free", "dcl-ds kunde qualified;", "  Ære  packed(5: 0);", "  navn char(30);", "end-ds;" }, result);
        assertEquals(0, formatter.getErrorCount());
    }

    // --- Helpers ---

    private RpgleFormatter createFormatter(FormatterConfig config, int sourceLength) {

        RpgleFormatter formatter = new RpgleFormatter(config);
        formatter.setSourceLength(sourceLength);

        return formatter;
    }

    private String[] format(RpgleFormatter formatter, String... source) throws RpgleFormatterException {
        return formatter.format(new TextLinesInput(source), 0).toLines();
    }
}
