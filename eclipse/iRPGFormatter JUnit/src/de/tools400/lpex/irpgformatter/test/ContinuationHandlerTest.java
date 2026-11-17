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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.BlockStructureException;
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

    // --- single line statement tests ---

    @Test
    public void singleLine_dcl_ds_with_likeds() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-ds msg likeds(msg_t);",
            "dcl-s i int(10);"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        assertEquals("dcl-ds msg likeds(msg_t);", statements[0].getStatement());
    }

    @Test
    public void singleLine_dcl_ds_with_likerec() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-ds ds_SUBFILE_OUT likerec(PBSFL: *OUTPUT) inz;",
            "dcl-s i int(10);"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        assertEquals("dcl-ds ds_SUBFILE_OUT likerec(PBSFL: *OUTPUT) inz;", statements[0].getStatement());
    }

    @Test
    public void singleLine_dcl_ds_with_end_ds() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-ds myFile extname('MYFILE') qualified inz end-ds;",
            "dcl-s i int(10);"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        assertEquals(2, statements.length);
        assertEquals("dcl-ds myFile extname('MYFILE') qualified inz end-ds;", statements[0].getStatement());
        assertNull(statements[1].getParent());
        assertEquals(0, statements[1].getIndentLevel());
    }

    @Test
    public void singleLine_dcl_ds_with_end_ds_inside_procedure() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-proc myProc;",
            "  dcl-ds myFile extname('MYFILE') qualified inz end-ds;",
            "  dcl-s i int(10);",
            "end-proc;"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        assertEquals(4, statements.length);
        assertEquals(1, statements[1].getIndentLevel());
        assertSame(statements[0], statements[2].getParent());
        assertEquals(1, statements[2].getIndentLevel());
        assertEquals(0, statements[3].getIndentLevel());
    }

    @Test
    public void singleLine_dcl_pr_with_end_pr() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-pr myProc ind extproc('MYPROC') end-pr;",
            "dcl-pr myPgm extpgm('MYPGM') end-pr myPgm;",
            "dcl-s i int(10);"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        assertEquals(3, statements.length);
        assertNull(statements[1].getParent());
        assertEquals(0, statements[1].getIndentLevel());
        assertNull(statements[2].getParent());
        assertEquals(0, statements[2].getIndentLevel());
    }

    @Test
    public void singleLine_dcl_pi_with_end_pi_inside_procedure() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-proc myProc;",
            "  dcl-pi *n ind end-pi;",
            "  dcl-s i int(10);",
            "end-proc;"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        assertEquals(4, statements.length);
        assertEquals(1, statements[1].getIndentLevel());
        assertSame(statements[0], statements[2].getParent());
        assertEquals(1, statements[2].getIndentLevel());
        assertEquals(0, statements[3].getIndentLevel());
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

    /**
     * The compiler ignores blanks behind the continuation character (checked on
     * IBM i 7.5).
     */
    @Test
    public void joinContinuedLines_plusContinuationWithTrailingBlanks() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-s msg char(100) inz('Hello +   ",
            "  World');"
        };
        // @formatter:on
        String result = joinStatements(lines).getStatement();
        assertEquals("dcl-s msg char(100) inz('Hello World');", result);
    }

    @Test
    public void joinContinuedLines_hyphenContinuationWithTrailingBlanks() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-s msg char(100) inz('Hello -   ",
            "  World');"
        };
        // @formatter:on
        String result = joinStatements(lines).getStatement();
        assertEquals("dcl-s msg char(100) inz('Hello   World');", result);
    }

    /**
     * A comment at the end of a line, that does not end the statement, is
     * collected like a whole comment line; the code goes on in the next line.
     */
    @Test
    public void joinContinuedLines_commentAtEndOfContinuedLine() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-s x   // the name",
            "  char(10);"
        };
        // @formatter:on
        CollectedStatement statement = joinStatements(lines);
        assertEquals("dcl-s x char(10);", statement.getStatement());
        assertArrayEquals(new String[] { "// the name" }, statement.getEmbeddedComments());
    }

    /**
     * A single quote in a comment line must not start a literal.
     */
    @Test
    public void joinContinuedLines_quoteInEmbeddedComment() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-s x",
            "  // it's the name",
            "  char(10);",
            "dcl-s y int(10);"
        };
        // @formatter:on
        ContinuationHandler.setTestMode(true);
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines);
        assertEquals(2, statements.length);
        assertEquals("dcl-s x char(10);", statements[0].getStatement());
        assertEquals("dcl-s y int(10);", statements[1].getStatement());
    }

    /**
     * A semicolon in a comment does not end the statement.
     */
    @Test
    public void joinContinuedLines_semicolonInCommentAtEndOfContinuedLine() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-s z   // no end;",
            "  int(10);"
        };
        // @formatter:on
        String result = joinStatements(lines).getStatement();
        assertEquals("dcl-s z int(10);", result);
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

    // --- block structure errors ---

    /**
     * END-PROC leaves a DCL-DS open: the error is reported on the line of the
     * DCL-DS and names the line of END-PROC, too.
     */
    @Test
    public void collectStatements_endProcLeavesDataStructureOpen() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-proc myProc;",
            "  dcl-ds myDs qualified;",
            "    subf char(10);",
            "end-proc;"
        };
        // @formatter:on
        BlockStructureException e = collectStatementsWithError(lines);
        assertArrayEquals(new String[] { "Error on line #2: DCL-DS myDs is not closed before END-PROC of line #4." }, e.getErrorMessages());
        assertEquals(2, e.getFirstLineNumber());
        assertEquals(e.getErrorMessages()[0], e.getMessage());
    }

    /**
     * A procedure, that is still open at the end of the source.
     */
    @Test
    public void collectStatements_procedureNotClosedAtEndOfSource() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-proc myProc;",
            "  dcl-s x int(10);"
        };
        // @formatter:on
        BlockStructureException e = collectStatementsWithError(lines);
        assertArrayEquals(new String[] { "Error on line #1: DCL-PROC myProc is not closed by END-PROC." }, e.getErrorMessages());
    }

    /**
     * Two independent errors are both reported. The first error does not cause
     * further errors, because END-PROC closes the procedure together with the
     * open DCL-DS.
     */
    @Test
    public void collectStatements_twoIndependentErrorsWithoutFollowUpErrors() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-proc proc1;",
            "  dcl-ds ds1;",
            "    a char(1);",
            "end-proc;",
            "dcl-proc proc2;",
            "end-proc;",
            "dcl-proc proc3;",
            "  dcl-pi *n;",
            "    b char(1);",
            "end-proc;"
        };
        // @formatter:on
        BlockStructureException e = collectStatementsWithError(lines);
        // @formatter:off
        assertArrayEquals(new String[] {
            "Error on line #2: DCL-DS ds1 is not closed before END-PROC of line #4.",
            "Error on line #8: DCL-PI *n is not closed before END-PROC of line #10." },
            e.getErrorMessages());
        // @formatter:on
        assertEquals(2, e.getErrorCount());
    }

    /**
     * The errors are ordered by the line numbers of the blocks, also if the
     * block, that is still open at the end of the source, is found last.
     */
    @Test
    public void collectStatements_errorsOrderedByLineNumber() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-ds ds1;",
            "  a char(1);",
            "dcl-proc myProc;",
            "  dcl-ds ds2;",
            "    b char(1);",
            "end-proc;"
        };
        // @formatter:on
        BlockStructureException e = collectStatementsWithError(lines);
        // @formatter:off
        assertArrayEquals(new String[] {
            "Error on line #1: DCL-DS ds1 is not closed by END-DS.",
            "Error on line #4: DCL-DS ds2 is not closed before END-PROC of line #6." },
            e.getErrorMessages());
        // @formatter:on
        assertEquals(1, e.getFirstLineNumber());
    }

    /**
     * A prototype without END-PR in front of the next prototype is still open
     * at the end of the source, because END-PR closes the next prototype.
     */
    @Test
    public void collectStatements_prototypeWithoutEndPr() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-pr p1 extproc('P1');",
            "  parm char(10);",
            "dcl-pr p2 extproc('P2');",
            "end-pr;"
        };
        // @formatter:on
        BlockStructureException e = collectStatementsWithError(lines);
        assertArrayEquals(new String[] { "Error on line #1: DCL-PR p1 is not closed by END-PR." }, e.getErrorMessages());
    }

    /**
     * A prototype within a procedure, that is not closed before END-PROC,
     * like a DCL-DS or a DCL-PI.
     */
    @Test
    public void collectStatements_endProcLeavesPrototypeOpen() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-proc myProc;",
            "  dcl-pr inner extproc('INNER');",
            "    parm char(10);",
            "end-proc;"
        };
        // @formatter:on
        BlockStructureException e = collectStatementsWithError(lines);
        assertArrayEquals(new String[] { "Error on line #2: DCL-PR inner is not closed before END-PROC of line #4." }, e.getErrorMessages());
    }

    /**
     * An end statement without a block, that it ends, is no error, because
     * selected lines may start within the block.
     */
    @Test
    public void collectStatements_endStatementWithoutBlockIsNoError() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "    subf char(10);",
            "  end-ds;",
            "end-proc;",
            "dcl-s x int(10);"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines, 1);
        assertEquals(4, statements.length);
    }

    /**
     * Calculation blocks, that are not closed, only affect the indentation,
     * also in front of END-PROC.
     */
    @Test
    public void collectStatements_openCalculationBlockBeforeEndProcIsNoError() throws RpgleFormatterException {
        // @formatter:off
        String[] lines = {
            "dcl-proc myProc;",
            "  if x;",
            "    y = 1;",
            "end-proc;",
            "if z;",
            "  y = 2;"
        };
        // @formatter:on
        CollectedStatement[] statements = ContinuationHandler.collectStatements(lines, 1);
        assertEquals(6, statements.length);
    }

    private BlockStructureException collectStatementsWithError(String[] lines) throws RpgleFormatterException {

        try {
            ContinuationHandler.collectStatements(lines, 1);
        } catch (BlockStructureException e) {
            return e;
        }

        fail("BlockStructureException expected");
        return null;
    }

    private CollectedStatement joinStatements(String[] lines) throws RpgleFormatterException {

        ContinuationHandler.setTestMode(true);
        CollectedStatement collectedStatement = ContinuationHandler.collectStatements(lines)[0];

        return collectedStatement;
    }
}
