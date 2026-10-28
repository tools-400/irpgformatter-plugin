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

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;
import de.tools400.lpex.irpgformatter.preferences.CalculationFormatter;
import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;

/**
 * Unit tests for the casing of the operation codes and built-in functions of
 * the calculations, see preference "Apply keyword casing to operation codes and
 * built-in functions".
 */
public class CalculationCasingTest extends AbstractTestCase {

    private static int MAX_LINE_LENGTH = 90;

    private RpgleFormatter formatter;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        formatter = new RpgleFormatter();
        formatter.setSourceLength(MAX_LINE_LENGTH);
        formatter.getConfig().setFormatOperationCodesAndBuiltInFunctions(true);
    }

    @Test
    public void off_leavesCasingUnchanged() throws RpgleFormatterException {

        formatter.getConfig().setFormatOperationCodesAndBuiltInFunctions(false);

        assertArrayEquals(new String[] { "IF %EOF(f);", "EndIf;" }, format("IF %EOF(f);", "EndIf;"));
    }

    @Test
    public void lowercase() throws RpgleFormatterException {

        setStyle(KeywordCasingStyle.LOWERCASE);

        assertArrayEquals(new String[] { "if %eof(F);", "endif;" }, format("IF %EOF(F);", "EndIf;"));
    }

    @Test
    public void uppercase() throws RpgleFormatterException {

        setStyle(KeywordCasingStyle.UPPERCASE);

        assertArrayEquals(new String[] { "IF %EOF(f);", "ENDIF;" }, format("if %eof(f);", "endif;"));
    }

    @Test
    public void allCasingStyles() throws RpgleFormatterException {

        String source = "setll %subdt(d: *years) file;";

        setStyle(KeywordCasingStyle.UPPER_CAMEL);
        assertEquals("SetLL %SubDt(d: *years) file;", format(source)[0]);

        setStyle(KeywordCasingStyle.FIRST_UPPER);
        assertEquals("Setll %Subdt(d: *years) file;", format(source)[0]);

        setStyle(KeywordCasingStyle.LOWER_CAMEL);
        assertEquals("setll %subDt(d: *years) file;", format(source)[0]);
    }

    @Test
    public void hyphenatedOperationCode() throws RpgleFormatterException {

        setStyle(KeywordCasingStyle.UPPER_CAMEL);

        assertEquals("Eval-Corr a = b;", format("EVAL-CORR a = b;")[0]);
    }

    @Test
    public void operationExtenderStaysUnchanged() throws RpgleFormatterException {

        setStyle(KeywordCasingStyle.UPPERCASE);

        assertEquals("CHAIN(e) key file;", format("chain(e) key file;")[0]);
    }

    @Test
    public void onlyTheFirstWordIsAnOperationCode() throws RpgleFormatterException {

        setStyle(KeywordCasingStyle.UPPERCASE);

        assertEquals("readCount = %LEN(read);", format("readCount = %len(read);")[0]);
    }

    @Test
    public void literalsAndCommentsStayUnchanged() throws RpgleFormatterException {

        setStyle(KeywordCasingStyle.UPPERCASE);

        assertEquals("DSPLY '%trim if' + %TRIM(x); // %trim if", format("dsply '%trim if' + %trim(x); // %trim if")[0]);
    }

    @Test
    public void unknownBuiltInFunctionStaysUnchanged() throws RpgleFormatterException {

        setStyle(KeywordCasingStyle.UPPERCASE);

        assertEquals("x = %foo(1) + %ABS(y);", format("x = %foo(1) + %abs(y);")[0]);
    }

    @Test
    public void continuedLines() throws RpgleFormatterException {

        setStyle(KeywordCasingStyle.UPPERCASE);

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "x = %trim(a) +",
            "    %trim(b);",
            "END-PROC;"
        );
        // @formatter:on

        assertEquals("  x = %TRIM(a) +", result[1]);
        assertEquals("      %TRIM(b);", result[2]);
    }

    @Test
    public void continuedLiteral_leavesStatementUnchanged() throws RpgleFormatterException {

        setStyle(KeywordCasingStyle.UPPERCASE);

        // @formatter:off
        String[] result = format(
            "dsply ('abc -",
            "%trim');"
        );
        // @formatter:on

        assertArrayEquals(new String[] { "dsply ('abc -", "%trim');" }, result);
    }

    @Test
    public void embeddedSqlStaysUnchanged() throws RpgleFormatterException {

        setStyle(KeywordCasingStyle.UPPERCASE);

        assertEquals("exec sql set :x = trim(:y);", format("exec sql set :x = trim(:y);")[0]);
    }

    @Test
    public void calculationsByIbmFormatter_leaveCasingUnchanged() throws RpgleFormatterException {

        setStyle(KeywordCasingStyle.UPPERCASE);
        formatter.getConfig().setCalculationFormatter(CalculationFormatter.IBM_FORMATTER);

        assertEquals("if %eof(f);", format("if %eof(f);", "endif;")[0]);
    }

    @Test
    public void changedTables() throws RpgleFormatterException {

        setStyle(KeywordCasingStyle.UPPER_CAMEL);

        Map<String, String> operationCodes = new HashMap<>(formatter.getConfig().getOperationCodes());
        operationCodes.put("SETLL", "SetLl");
        formatter.getConfig().setOperationCodes(operationCodes);

        Map<String, String> builtInFunctions = new HashMap<>(formatter.getConfig().getBuiltInFunctions());
        builtInFunctions.put("%MYBIF", "%MyBif");
        formatter.getConfig().setBuiltInFunctions(builtInFunctions);

        assertEquals("SetLl %MyBif(k) file;", format("setll %mybif(k) file;")[0]);
    }

    private void setStyle(KeywordCasingStyle style) {
        formatter.getConfig().setKeywordCasingStyle(style);
    }

    private String[] format(String... source) throws RpgleFormatterException {
        return formatter.format(new TextLinesInput(source), 0).toLines();
    }
}
