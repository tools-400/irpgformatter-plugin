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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;

/**
 * Unit tests for statements, that the formatter left unchanged with an
 * unexpected error: names with national characters, sub-fields, that cannot be
 * aligned, a data type as the parameter of a keyword and the data of a compile
 * time array.
 */
public class FormatterExceptionsTest extends AbstractTestCase {

    /** A name, that is too long for the alignment, if DCL-SUBF precedes it. */
    private static final String LONG_NAME = "anzahl_der_bestellungen_im_laufenden_geschaeftsjahr_xy";

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

    // --- Sub-fields, that cannot be aligned ---

    @Test
    public void format_dclSubfWithNameTooLongForAlignment_isNotAligned() throws RpgleFormatterException {

        RpgleFormatter formatter = createFormatter(FormatterConfig.fromPreferences(), 100);

        // @formatter:off
        String[] result = format(formatter,
            "**FREE",
            "dcl-ds bestellung;",
            "  kundennr   packed(7:0);",
            "  datum date;",
            "  dcl-subf " + LONG_NAME + "   int( 10 );",
            "end-ds;"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "**free",
            "dcl-ds bestellung;",
            "  kundennr packed(7: 0);",
            "  datum    date;",
            "  dcl-subf " + LONG_NAME + " int(10);",
            "end-ds;"
        }, result);
        // @formatter:on
        assertEquals(0, formatter.getErrorCount());
    }

    /**
     * A keyword, that is missing in the word list, is a name: only the name of
     * the sub-field is aligned.
     */
    @Test
    public void format_nameBehindDataTypeOfSubField_isNotAligned() throws RpgleFormatterException {

        FormatterConfig config = FormatterConfig.fromPreferences();
        Map<String, String> keywords = new HashMap<>(config.getKeywords());
        keywords.remove("NULLIND");
        config.setKeywords(keywords);
        RpgleFormatter formatter = createFormatter(config, 100);

        String[] result = format(formatter, "**FREE", "dcl-ds ds2;", "  a char(1);", "  fld   char(10) nullind;", "end-ds;");

        assertArrayEquals(new String[] { "**free", "dcl-ds ds2;", "  a   char(1);", "  fld char(10) nullind;", "end-ds;" }, result);
        assertEquals(0, formatter.getErrorCount());
    }

    // --- A data type as the parameter of a keyword ---

    /**
     * The name of the sub-field <code>date</code> is a data type for the
     * tokenizer.
     */
    @Test
    public void format_dataTypeAsParameterBehindEndColumn_isMovedToNextLine() throws RpgleFormatterException {

        RpgleFormatter formatter = createFormatter(FormatterConfig.fromPreferences(), 40);

        String[] result = format(formatter, "**FREE", "dcl-ds ds3;", "  date date;", "  yearOfTheDateFieldXY char(4) overlay(date);", "end-ds;");

        // @formatter:off
        assertArrayEquals(new String[] {
            "**free",
            "dcl-ds ds3;",
            "  date                 date;",
            "  yearOfTheDateFieldXY char(4) overlay(",
            "    date);",
            "end-ds;"
        }, result);
        // @formatter:on
        assertEquals(0, formatter.getErrorCount());
    }

    // --- The data of a compile time array ---

    /**
     * Compiler directives must precede the compile time data (ILE RPG Reference
     * 7.3, page 80): the records behind the first compile time array are data,
     * also if they look like a declaration.
     */
    @Test
    public void format_declarationInDataOfCompileTimeArray_isData() throws RpgleFormatterException {

        RpgleFormatter formatter = createFormatter(FormatterConfig.fromPreferences(), 100);

        // @formatter:off
        String[] result = format(formatter,
            "**FREE",
            "dcl-s arr char(20) dim(2) ctdata;",
            "dcl-s   x   int(10);",
            "**CTDATA arr",
            "DCL-PROC xyz",
            "something"
        );
        // @formatter:on

        // @formatter:off
        assertArrayEquals(new String[] {
            "**free",
            "dcl-s arr char(20) dim(2) ctdata;",
            "dcl-s x int(10);",
            "**ctdata arr",
            "DCL-PROC xyz",
            "something"
        }, result);
        // @formatter:on
        assertEquals(0, formatter.getErrorCount());
    }

    @Test
    public void format_eofInDataOfCompileTimeArray_isData() throws RpgleFormatterException {

        RpgleFormatter formatter = createFormatter(FormatterConfig.fromPreferences(), 100);

        // @formatter:off
        String[] result = format(formatter,
            "**FREE",
            "dcl-s a1 char(5) dim(1) ctdata;",
            "dcl-s a2 char(5) dim(1) ctdata;",
            "**CTDATA a1",
            "/EOF",
            "**CTDATA a2",
            "abcde"
        );
        // @formatter:on

        assertEquals("**ctdata a2", result[5]);
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
