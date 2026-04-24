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
import de.tools400.lpex.irpgformatter.statement.CollectedStatement;
import de.tools400.lpex.irpgformatter.statement.ContinuationHandler;

/**
 * Unit tests for {@link ContinuationHandler}.
 */
public class ContinuationHandlerTest extends AbstractTestCase {

    // --- collectStatements tests ---

    @Test
    public void collectStatements_multiLineStatement() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-s myVar",
            "  char(10);"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        assertEquals(1, statements.length);
        assertTrue(statements[0].getLine(0).contains("dcl-s myVar"));
        assertTrue(statements[0].getLine(1).contains("char(10);"));
    }

    @Test
    public void collectStatements_freeDirective() throws RpgleFormatterException {
        String[] lines = { "**FREE" };
        CollectedStatement statements = ContinuationHandler.collectStatements(lines)[0];
        assertEquals(1, statements.numLines());
        assertEquals("**FREE", statements.getLine(0));
    }

    @Test
    public void collectStatements_comments() throws RpgleFormatterException {
        String[] lines = { "// This is a comment" };
        CollectedStatement statements = ContinuationHandler.collectStatements(lines)[0];
        assertEquals(1, statements.numLines());
        assertEquals("// This is a comment", statements.getLine(0));
    }

    @Test
    public void collectStatements_compilerDirectives() throws RpgleFormatterException {
        String[] lines = { "/copy myfile" };
        CollectedStatement statements = ContinuationHandler.collectStatements(lines)[0];
        assertEquals(1, statements.numLines());
        assertEquals("/copy myfile", statements.getLine(0));
    }

    @Test
    public void collectStatements_blankLines() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "",
            "dcl-s x int;",
            ""
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        assertEquals(3, statements.length);

        assertEquals(1, statements[0].numLines());
        assertEquals("", statements[0].getLine(0));

        assertEquals(1, statements[1].numLines());
        assertEquals("dcl-s x int;", statements[1].getLine(0));

        assertEquals(1, statements[2].numLines());
        assertEquals("", statements[2].getLine(0));
    }

    @Test
    public void collectStatements_multipleStatements() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-s x int;",
            "dcl-s y char(10);"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        assertEquals(2, statements.length);
    }

    @Test
    public void collectStatements_ellipsisContinuation() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-pr myProc...",
            "  extproc('MYPROC');",
            "end-pr;"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        assertEquals(2, statements.length);
        assertEquals(2, statements[0].numLines());
    }

    // --- joinContinuedLines tests ---

    @Test
    public void joinContinuedLines_singleLine() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-s myVar char(10);"
        };
        // @formatter:on
        String result = joinStatements(lines).getStatement();
        assertEquals("dcl-s myVar char(10);", result);
    }

    @Test
    public void joinContinuedLines_ellipsisContinuation() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-pr myProc...",
            "  extproc('MYPROC');"
        };
        // @formatter:on
        String result = joinStatements(lines).getStatement();
        assertEquals("dcl-pr myProcextproc('MYPROC');", result);
    }

    @Test
    public void joinContinuedLines_plusContinuation() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-s msg char(100) inz('Hello +",
            "  World');"
        };
        // @formatter:on
        String result = joinStatements(lines).getStatement();
        assertEquals("dcl-s msg char(100) inz('Hello World');", result);
    }

    @Test
    public void joinContinuedLines_hyphenContinuation() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-s msg char(100) inz('Hello -",
            "  World');"
        };
        // @formatter:on
        String result = joinStatements(lines).getStatement();
        assertEquals("dcl-s msg char(100) inz('Hello   World');", result);
    }

    @Test
    public void joinContinuedLines_noMarkers() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-s myVar",
            "  char(10);"
        };
        // @formatter:on
        String result = joinStatements(lines).getStatement();
        assertEquals("dcl-s myVar char(10);", result);
    }

    @Test
    public void joinContinuedLines_multipleLines() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-pr my...",
            "  First...",
            "  Procedure;",
        };
        // @formatter:on
        String result = joinStatements(lines).getStatement();
        assertEquals("dcl-pr myFirstProcedure;", result);
    }

    @Test
    public void joinContinuedLines_emptyString() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            ""
        };
        // @formatter:on
        String result = joinStatements(lines).getStatement();
        assertEquals("", result);
    }

    @Test
    public void joinContinuedLines_escapedQuotes() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-s msg char(50) inz('It''s a test');"
        };
        // @formatter:on
        String result = joinStatements(lines).getStatement();
        assertEquals("dcl-s msg char(50) inz('It''s a test');", result);
    }

    // --- Literal continuation tests ---

    @Test
    public void joinContinuedLines_plusContinuationInsideLiteral() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-c LITERAL 'This literal contains a ++",
            "character.';"
        };
        // @formatter:on
        CollectedStatement collectedStatement = joinStatements(lines);
        String result = collectedStatement.getStatement();
        assertEquals("dcl-c LITERAL 'This literal contains a +character.';", result);
    }

    @Test
    public void joinContinuedLines_hyphenContinuationInsideLiteral() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-c LITERAL 'This literal contains a --",
            "   character.';"
        };
        // @formatter:on
        CollectedStatement collectedStatement = joinStatements(lines);
        String result = collectedStatement.getStatement();
        assertEquals("dcl-c LITERAL 'This literal contains a -   character.';", result);
    }

    // --- Complex scenario tests ---

    @Test
    public void complex_dclPrWithExtproc() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-pr sendMessage",
            "  extproc('SNDMSG');",
            "  msgText char(100) const;",
            "end-pr;"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        // Should collect as multiple statements or one depending on
        // semicolons
        assertTrue(statements.length >= 1);
    }

    @Test
    public void complex_ctlOptMultiLine() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "ctl-opt option(*srcstmt)",
            "  dftactgrp(*no)",
            "  actgrp(*caller);"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        assertEquals(1, statements.length);
        assertEquals("ctl-opt option(*srcstmt) dftactgrp(*no) actgrp(*caller);", statements[0].getStatement());
    }

    @Test
    public void complex_dclDsWithSubfields() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-ds myDs;",
            "  field1 char(10);",
            "  field2 int;",
            "end-ds;"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        assertEquals(4, statements.length);
    }

    // --- Trailing comment tests ---

    @Test
    public void collectStatements_trailingComment() throws RpgleFormatterException {
        String[] lines = { "dcl-s myVar char(10);  // my variable" };
        CollectedStatement statements = ContinuationHandler.collectStatements(lines)[0];
        assertEquals(1, statements.numLines());
        assertEquals("dcl-s myVar char(10);  // my variable", statements.getLine(0));
    }

    @Test
    public void collectStatements_trailingCommentMultipleStatements() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-s var1 int;  // first var",
            "dcl-s var2 int;  // second var"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        assertEquals(2, statements.length);
        assertEquals("dcl-s var1 int;  // first var", statements[0].getStatement());
        assertEquals("dcl-s var2 int;  // second var", statements[1].getStatement());
    }

    @Test
    public void collectStatements_dclPrWithTrailingComments() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-pr QMHSNDPM extpgm('QMHSNDPM');",
            "  i_msgID char(7) const;",
            "  i_length int(10) const;  // message length",
            "  i_wait int(10) const  options(*nopass);  // optional",
            "end-pr;"
        };
        // @formatter:on
        CollectedStatement statements[] = ContinuationHandler.collectStatements(lines);
        assertEquals(5, statements.length);
    }

    @Test
    public void collectStatements_trailingCommentWithSlashInLiteral() throws RpgleFormatterException {
        // The // inside the literal should NOT be treated as a comment
        //@formatter:off
        String[] lines = {
            "dcl-s path char(100) inz('http://example.com');",
        };
        //@formatter:on
        CollectedStatement statements[] = ContinuationHandler.collectStatements(lines);
        assertEquals(1, statements.length);
        assertEquals("dcl-s path char(100) inz('http://example.com');", statements[0].getStatement());
    }

    private CollectedStatement joinStatements(String[] lines) throws RpgleFormatterException {

        ContinuationHandler.setTestMode(true);
        CollectedStatement collectedStatement = ContinuationHandler.collectStatements(lines)[0];

        return collectedStatement;
    }
}
