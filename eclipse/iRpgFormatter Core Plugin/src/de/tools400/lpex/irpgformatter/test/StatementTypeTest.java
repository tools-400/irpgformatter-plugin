/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.parser.StatementIdentifier;
import de.tools400.lpex.irpgformatter.parser.StatementType;
import de.tools400.lpex.irpgformatter.preferences.KeywordCasingStyle;

/**
 * Unit tests for {@link KeywordCasingStyle}.
 */
public class StatementTypeTest extends AbstractTestCase {

    @Test
    public void ctl_opt() throws RpgleFormatterException {
        String line = "ctl-opt debug option(*srcstmt: *nodebugio);";
        assertEquals(StatementType.CTL_OPT, StatementIdentifier.identifyStatementType(line));
    }

    @Test
    public void dcl_f() throws RpgleFormatterException {
        String line = "dcl-f QSYSPRT printer(80) oflind(*in70) usropn;";
        assertEquals(StatementType.DCL_F, StatementIdentifier.identifyStatementType(line));
    }

    @Test
    public void dcl_c() throws RpgleFormatterException {
        String line = "dcl-c MY_CONSTANT 1;";
        assertEquals(StatementType.DCL_C, StatementIdentifier.identifyStatementType(line));
    }

    @Test
    public void dcl_ds() throws RpgleFormatterException {
        String line = "dcl-ds myStructure_t qualified template;";
        assertEquals(StatementType.DCL_DS, StatementIdentifier.identifyStatementType(line));
    }

    @Test
    public void dcl_pr() throws RpgleFormatterException {
        String line = "dcl-pr myPrototype ind extproc('MODULE_myPrototype');";
        assertEquals(StatementType.DCL_PR, StatementIdentifier.identifyStatementType(line));
    }

    @Test
    public void dcl_pi() throws RpgleFormatterException {
        String line = "dcl-pi myProcedure ind extproc(*dclcase);";
        assertEquals(StatementType.DCL_PI, StatementIdentifier.identifyStatementType(line));
    }

    @Test
    public void dcl_s() throws RpgleFormatterException {
        String line = "dcl-s myPointer pointer;";
        assertEquals(StatementType.DCL_S, StatementIdentifier.identifyStatementType(line));
    }

    /*
     * Sub-fields are identified as StatementType.OTHER, because statement type
     * is identified without context
     */
    @Test
    public void dcl_subf_all() {
        //@formatter:off
        String[] lines = new String[] {
            "dcl-s myPointer pointer;",
            "dcl-pr myPrototype extproc('MODULE_myPrototype');",
            "  mySubField1 varchar(10);",
            "  mySubField2 varchar(10);",
            "end-pr;",
            "dcl-c MY_CONSTANT 1;"
        };
        StatementType[] expected = new StatementType[] {
            StatementType.DCL_S,
            StatementType.DCL_PR,
            StatementType.OTHER,
            StatementType.OTHER,
            StatementType.END_PR,
            StatementType.DCL_C
        };
        //@formatter:off
        for (int i = 0; i < lines.length; i++) {
            assertEquals(expected[i], StatementIdentifier.identifyStatementType(lines[i]));
        }
    }

    /*
     * Sub-fields are identified as StatementType.OTHER, because statement type is identified without context
     */
    @Test
    public void dcl_subf_range() {
        //@formatter:off
        String[] lines = new String[] {
            "dcl-s myPointer pointer;",
            "dcl-pr myPrototype extproc('MODULE_myPrototype');",
            "  mySubField1 varchar(10);",
            "  mySubField2 varchar(10);",
            "end-pr;",
            "dcl-c MY_CONSTANT 1;"
        };
        StatementType[] expected = new StatementType[] {
            null,
            null,
            StatementType.OTHER,
            StatementType.OTHER,
            null,
            null
        };
        //@formatter:off
        for (int i = 2; i <= 3; i++) {
            assertTrue(expected[i] != null);
            assertEquals(expected[i], StatementIdentifier.identifyStatementType(lines[i]));
        }
    }

    @Test
    public void end_ds() throws RpgleFormatterException {
        String line = "end-ds;";
        assertEquals(StatementType.END_DS, StatementIdentifier.identifyStatementType(line));
    }

    @Test
    public void end_pr() throws RpgleFormatterException {
        String line = "end-pr;";
        assertEquals(StatementType.END_PR, StatementIdentifier.identifyStatementType(line));
    }

    @Test
    public void end_pi() throws RpgleFormatterException {
        String line = "end-pi;";
        assertEquals(StatementType.END_PI, StatementIdentifier.identifyStatementType(line));
    }

    @Test
    public void compiler_directive() throws RpgleFormatterException {
        //@formatter:off
        String[] lines = new String[] {
            "/copy library/file member",
            "/include library/file member",
            "/eject",
            "/define MY_LABEL",
            "/if defined(MY_LABEL)",
            "/if not defined(MY_LABEL)",
            "/endif"
        };
        //@formatter:off
        for (String line : lines) {
            assertEquals(StatementType.COMPILER_DIRECTIVE, StatementIdentifier.identifyStatementType(line));
        }
    }

    @Test
    public void comment() throws RpgleFormatterException {
        String line = "// my line comment";
        assertEquals(StatementType.COMMENT, StatementIdentifier.identifyStatementType(line));
    }

    @Test
    public void free_directive() throws RpgleFormatterException {
        String line = "**Free";
        assertEquals(StatementType.FREE_DIRECTIVE, StatementIdentifier.identifyStatementType(line));
    }

    @Test
    public void blank() throws RpgleFormatterException {
        //@formatter:off
        String[] lines = new String[] {
            "",
            "   "
        };
        //@formatter:off
        for (String line : lines) {
            assertEquals(StatementType.BLANK, StatementIdentifier.identifyStatementType(line));
        }
    }

    @Test
    public void other() throws RpgleFormatterException {
        //@formatter:off
        String[] lines = new String[] {
            "if (bar = 'foo');",
            "  a = b + c",
            "endif;"
        };
        //@formatter:off
        for (String line : lines) {
            assertEquals(StatementType.OTHER, StatementIdentifier.identifyStatementType(line));
        }
    }
}
