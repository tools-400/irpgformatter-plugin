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
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;

/**
 * Unit tests for sources with conditional compilation (<code>/IF</code>,
 * <code>/ELSE</code>, <code>/ENDIF</code>, <code>/DEFINE</code>) and
 * <code>/COPY</code> directives.
 * <p>
 * The formatter neither evaluates conditions nor resolves copy members. The
 * formatted source must compile to the same program as the original source,
 * whatever branch the compiler selects. Therefore every compiler directive must
 * keep a line of its own, and the lines of all branches must be formatted
 * alike.
 * </p>
 */
public class ConditionalCompilationTest extends AbstractTestCase {

    private static int MAX_LINE_LENGTH = 90;

    /** A compiler directive that follows other text on the same line. */
    private static final Pattern MERGED_DIRECTIVE = Pattern
        .compile("(?i).*\\S\\s+/(if|elseif|else|endif|define|undefine|copy|include|eof)\\b.*");

    private RpgleFormatter formatter;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        formatter = new RpgleFormatter();
        formatter.setSourceLength(MAX_LINE_LENGTH);
    }

    // ----------------------------------------------------------------------
    // Alternative statements in the branches of /IF ... /ELSE ... /ENDIF
    // ----------------------------------------------------------------------

    @Test
    public void format_alternativeExtprocOfPrototype_alreadyFormatted_unchanged() throws RpgleFormatterException {

        // Taken from LOG4RPG
        // @formatter:off
        String[] source = new String[] {
            "//  Custom appender ...",
            "//  ... prototye: new, returns TRUE on success else FALSE",
            "/if defined(DEFINE_PROCPTR)",
            "dcl-pr SrvPgmAppender_new ind extproc(g_pAppender_new);",
            "/else",
            "dcl-pr SrvPgmAppender_new ind extproc('SrvPgmAppender_new');",
            "/endif",
            "  i_pSuper   like(LOG4RPG_pAppender_t) const;",
            "  io_pHandle pointer;",
            "  i_propList like(LOG4RPG_pPropertyList_t) const;",
            "  i_prefix   like(LOG4RPG_prefix_t) const;",
            "end-pr;"
        };
        // @formatter:on

        String[] result = format(source);

        assertArrayEquals(source, result);
    }

    @Test
    public void format_alternativePrototypeHeaders_formatsBothBranches() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "//  Custom appender ...",
            "/if defined(DEFINE_PROCPTR)",
            "DCL-PR SrvPgmAppender_new IND ExtProc( g_pAppender_new );",
            "/else",
            "DCL-PR SrvPgmAppender_new IND ExtProc( 'SrvPgmAppender_new' );",
            "/endif",
            "  i_pSuper   LIKE( LOG4RPG_pAppender_t ) CONST;",
            "  io_pHandle POINTER;",
            "END-PR;",
            "DCL-S after INT( 10 );"
        );
        // @formatter:on

        assertEquals(10, result.length);
        assertEquals("//  Custom appender ...", result[0]);
        assertEquals("/if defined(DEFINE_PROCPTR)", result[1]);
        assertEquals("dcl-pr SrvPgmAppender_new ind extproc(g_pAppender_new);", result[2]);
        assertEquals("/else", result[3]);
        assertEquals("dcl-pr SrvPgmAppender_new ind extproc('SrvPgmAppender_new');", result[4]);
        assertEquals("/endif", result[5]);
        assertEquals("  i_pSuper   like(LOG4RPG_pAppender_t) const;", result[6]);
        assertEquals("  io_pHandle pointer;", result[7]);
        assertEquals("end-pr;", result[8]);
        assertEquals("dcl-s after int(10);", result[9]);
        assertEquals(0, formatter.getWarningCount());
    }

    @Test
    public void format_alternativeProcedureHeaders_formatsBothBranches() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "/if defined(X)",
            "DCL-PROC p EXPORT;",
            "/else",
            "DCL-PROC p;",
            "/endif",
            "  DCL-PI *n IND;",
            "  END-PI;",
            "  DCL-S v INT( 10 );",
            "  RETURN *ON;",
            "END-PROC;",
            "DCL-S after INT( 10 );"
        );
        // @formatter:on

        assertEquals(11, result.length);
        assertEquals("/if defined(X)", result[0]);
        assertEquals("dcl-proc p export;", result[1]);
        assertEquals("/else", result[2]);
        assertEquals("dcl-proc p;", result[3]);
        assertEquals("/endif", result[4]);
        assertEquals("  dcl-pi *n ind;", result[5]);
        assertEquals("  end-pi;", result[6]);
        assertEquals("  dcl-s v int(10);", result[7]);
        assertEquals("  RETURN *ON;", result[8]); // ignored by the formatter
        assertEquals("end-proc;", result[9]);
        assertEquals("dcl-s after int(10);", result[10]);
    }

    @Test
    public void format_alternativeIfHeaders_keepsDirectives() throws RpgleFormatterException {

        // @formatter:off
        String[] source = new String[] {
            "/if defined(X)",
            "if a = 1;",
            "/else",
            "if a = 2;",
            "/endif",
            "  b = 1;",
            "endif;"
        };
        // @formatter:on

        String[] result = format(source);

        assertDirectivesPreserved(source, result);
    }

    // ----------------------------------------------------------------------
    // Block structure of the branches
    //
    // Each branch starts with the blocks that are open at /IF. After /ENDIF
    // the formatter continues with the blocks that are open at the end of the
    // first branch.
    // ----------------------------------------------------------------------

    @Test
    public void format_alternativeInterfaceHeadersInsideProcedure_indentsLikeOneHeader() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "/if defined(X)",
            "  DCL-PI *n IND;",
            "/else",
            "  DCL-PI *n IND OPDESC;",
            "/endif",
            "    a INT( 10 );",
            "  END-PI;",
            "END-PROC;"
        );
        // @formatter:on

        assertEquals(9, result.length);
        assertEquals("dcl-proc p;", result[0]);
        assertEquals("/if defined(X)", result[1]);
        assertEquals("  dcl-pi *n ind;", result[2]);
        assertEquals("/else", result[3]);
        assertEquals("  dcl-pi *n ind opdesc;", result[4]);
        assertEquals("/endif", result[5]);
        assertEquals("    a int(10);", result[6]);
        assertEquals("  end-pi;", result[7]);
        assertEquals("end-proc;", result[8]);
    }

    @Test
    public void format_nestedConditionalGroups_indentsLikeOneHeader() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "/if defined(A)",
            "/if defined(B)",
            "DCL-PR p IND EXTPROC( 'AB' );",
            "/else",
            "DCL-PR p IND EXTPROC( 'A' );",
            "/endif",
            "/else",
            "DCL-PR p IND;",
            "/endif",
            "  x INT( 10 );",
            "END-PR;",
            "DCL-S after INT( 10 );"
        );
        // @formatter:on

        assertEquals(12, result.length);
        assertEquals("dcl-pr p ind extproc('AB');", result[2]);
        assertEquals("dcl-pr p ind extproc('A');", result[4]);
        assertEquals("dcl-pr p ind;", result[7]);
        assertEquals("  x int(10);", result[9]);
        assertEquals("end-pr;", result[10]);
        assertEquals("dcl-s after int(10);", result[11]);
        assertEquals(0, formatter.getWarningCount());
    }

    @Test
    public void format_branchesEndWithDifferentBlocks_warns() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "/if defined(X)",
            "DCL-DS a;",
            "/else",
            "DCL-DS a LIKEDS( t );",
            "/endif",
            "  f INT( 10 );",
            "END-DS;"
        );
        // @formatter:on

        assertEquals(0, formatter.getErrorCount());
        assertEquals(1, formatter.getWarningCount());
        assertEquals("Warning on line #5: The branches of the /IF group of line #1 end in different blocks. "
            + "The indentation of the following lines may be wrong.", formatter.getWarningMessages()[0]);

        // continues with the blocks at the end of the first branch
        assertEquals(7, result.length);
        assertEquals("  f int(10);", result[5]);
        assertEquals("end-ds;", result[6]);
    }

    @Test
    public void format_groupWithoutElseOpensBlock_warns() throws RpgleFormatterException {

        // Without /ELSE the compiler may skip the block header.
        // @formatter:off
        format(
            "/if defined(X)",
            "DCL-PR p IND;",
            "/endif",
            "  a INT( 10 );",
            "END-PR;"
        );
        // @formatter:on

        assertEquals(0, formatter.getErrorCount());
        assertEquals(1, formatter.getWarningCount());
    }

    @Test
    public void format_directivesOfGroupInsideBlock_getIndentOfIf() throws RpgleFormatterException {

        formatter.getConfig().setUnindentCompilerDirectives(false);

        // @formatter:off
        String[] result = format(
            "DCL-PROC p;",
            "/if defined(X)",
            "DCL-PR q IND EXTPROC( 'X' );",
            "/else",
            "DCL-PR q IND;",
            "/endif",
            "a INT( 10 );",
            "END-PR;",
            "END-PROC;"
        );
        // @formatter:on

        assertEquals(9, result.length);
        assertEquals("dcl-proc p;", result[0]);
        assertEquals("  /if defined(X)", result[1]);
        assertEquals("  dcl-pr q ind extproc('X');", result[2]);
        assertEquals("  /else", result[3]);
        assertEquals("  dcl-pr q ind;", result[4]);
        assertEquals("  /endif", result[5]);
        assertEquals("    a int(10);", result[6]);
        assertEquals("  end-pr;", result[7]);
        assertEquals("end-proc;", result[8]);
    }

    // ----------------------------------------------------------------------
    // Conditional lines and /COPY inside a block
    // ----------------------------------------------------------------------

    @Test
    public void format_conditionalParameter_formatsAllParameters() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "DCL-PR p2 IND;",
            "  a INT( 10 );",
            "/if defined(X)",
            "  b INT( 10 );",
            "/endif",
            "  c INT( 10 );",
            "END-PR;"
        );
        // @formatter:on

        assertEquals(7, result.length);
        assertEquals("dcl-pr p2 ind;", result[0]);
        assertEquals("  a int(10);", result[1]);
        assertEquals("/if defined(X)", result[2]);
        assertEquals("  b int(10);", result[3]);
        assertEquals("/endif", result[4]);
        assertEquals("  c int(10);", result[5]);
        assertEquals("end-pr;", result[6]);
    }

    @Test
    public void format_copyAndConditionalSubfieldInDs_formatsAllSubfields() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "DCL-DS myDs QUALIFIED;",
            "  f1 CHAR( 10 );",
            "  /copy qcpysrc,dsfields",
            "/if defined(X)",
            "  f2 INT( 10 );",
            "/endif",
            "END-DS;"
        );
        // @formatter:on

        assertEquals(7, result.length);
        assertEquals("dcl-ds myDs qualified;", result[0]);
        assertEquals("  f1 char(10);", result[1]);
        assertEquals("/copy qcpysrc,dsfields", result[2]); // unindent compiler directives
        assertEquals("/if defined(X)", result[3]);
        assertEquals("  f2 int(10);", result[4]);
        assertEquals("/endif", result[5]);
        assertEquals("end-ds;", result[6]);
    }

    // ----------------------------------------------------------------------
    // Compiler directives inside a statement
    //
    // The ILE RPG Reference ("Conditional Directives Within a Free-Form
    // Statement") allows /IF, /ELSEIF, /ELSE and /ENDIF within any free-form
    // statement other than a calculation statement (RNF0714), provided that
    // the whole conditional group lies within the statement.
    //
    // The directives split the statement into sections, which are formatted
    // on lines of their own. The directives get the indent of continuation
    // lines, the sections within a conditional group one level more.
    // ----------------------------------------------------------------------

    @Test
    public void format_directivesInsidePrototypeHeader_formatsSections() throws RpgleFormatterException {

        // @formatter:off
        String[] source = new String[] {
            "DCL-PR p3 IND",
            "/if defined(X)",
            "  ExtProc( 'P3X' )",
            "/else",
            "  ExtProc( 'P3' )",
            "/endif",
            "  ;",
            "  p INT( 10 );",
            "END-PR;"
        };
        // @formatter:on

        String[] result = format(source);

        assertDirectivesPreserved(source, result);
        assertEquals(9, result.length);
        assertEquals("dcl-pr p3 ind", result[0]);
        assertEquals("  /if defined(X)", result[1]);
        assertEquals("    extproc('P3X')", result[2]);
        assertEquals("  /else", result[3]);
        assertEquals("    extproc('P3')", result[4]);
        assertEquals("  /endif", result[5]);
        assertEquals("  ;", result[6]);
        assertEquals("  p int(10);", result[7]);
        assertEquals("end-pr;", result[8]);
    }

    @Test
    public void format_directivesInsideStandaloneField_formatsSections() throws RpgleFormatterException {

        // Valid example of the ILE RPG Reference
        // @formatter:off
        String[] source = new String[] {
            "DCL-S name",
            "  /IF DEFINED(TRUE)",
            "    CHAR(10)",
            "  /ELSE",
            "    VARCHAR(10)",
            "  /ENDIF",
            " ;"
        };
        // @formatter:on

        String[] result = format(source);

        assertDirectivesPreserved(source, result);
        assertEquals(7, result.length);
        assertEquals("dcl-s name", result[0]);
        assertEquals("  /if defined(TRUE)", result[1]);
        assertEquals("    char(10)", result[2]);
        assertEquals("  /else", result[3]);
        assertEquals("    varchar(10)", result[4]);
        assertEquals("  /endif", result[5]);
        assertEquals("  ;", result[6]);
    }

    @Test
    public void format_directivesInsideParameter_formatsSections() throws RpgleFormatterException {

        // @formatter:off
        String[] source = new String[] {
            "DCL-PI *n;",
            "  name",
            "    /IF DEFINED(TRUE)",
            "      CHAR(10)",
            "    /ELSE",
            "      VARCHAR(10)",
            "    /ENDIF",
            "    CONST;",
            "  other INT( 10 ) CONST;",
            "END-PI;"
        };
        // @formatter:on

        String[] result = format(source);

        assertDirectivesPreserved(source, result);
        assertEquals(10, result.length);
        assertEquals("dcl-pi *n;", result[0]);
        assertEquals("  name", result[1]);
        assertEquals("    /if defined(TRUE)", result[2]);
        assertEquals("      char(10)", result[3]);
        assertEquals("    /else", result[4]);
        assertEquals("      varchar(10)", result[5]);
        assertEquals("    /endif", result[6]);
        assertEquals("    const;", result[7]);
        assertEquals("  other int(10) const;", result[8]);
        assertEquals("end-pi;", result[9]);
    }

    @Test
    public void format_directivesInsideStatementOfProcedure_formatsSections() throws RpgleFormatterException {

        // Directives between statements are unindented, directives within a
        // statement get the indent of continuation lines.
        // @formatter:off
        String[] result = format(
            "DCL-PROC getName EXPORT;",
            "/IF DEFINED(TRACE)",
            "  DCL-S trace IND INZ( *ON );",
            "/ENDIF",
            "  DCL-S name",
            "  /IF DEFINED(UNICODE)",
            "    UCS2( 50 )",
            "  /ELSE",
            "    VARCHAR( 50 )",
            "  /ENDIF",
            "  ;",
            "END-PROC;"
        );
        // @formatter:on

        assertEquals(12, result.length);
        assertEquals("dcl-proc getName export;", result[0]);
        assertEquals("/if defined(TRACE)", result[1]);
        assertEquals("  dcl-s trace ind inz(*on);", result[2]);
        assertEquals("/endif", result[3]);
        assertEquals("  dcl-s name", result[4]);
        assertEquals("    /if defined(UNICODE)", result[5]);
        assertEquals("      ucs2(50)", result[6]);
        assertEquals("    /else", result[7]);
        assertEquals("      varchar(50)", result[8]);
        assertEquals("    /endif", result[9]);
        assertEquals("    ;", result[10]);
        assertEquals("end-proc;", result[11]);
    }

    @Test
    public void format_constInsideBranch_staysInBranch() throws RpgleFormatterException {

        // "Sort const/value to end" must not move CONST out of the branch.
        // @formatter:off
        String[] result = format(
            "DCL-PR p IND;",
            "  a INT( 10 )",
            "  /IF DEFINED(X)",
            "    CONST",
            "  /ENDIF",
            "  ;",
            "END-PR;"
        );
        // @formatter:on

        assertEquals(7, result.length);
        assertEquals("  a int(10)", result[1]);
        assertEquals("    /if defined(X)", result[2]);
        assertEquals("      const", result[3]);
        assertEquals("    /endif", result[4]);
        assertEquals("    ;", result[5]);
    }

    @Test
    public void format_directivesInsideParametersOfKeyword_unchanged() throws RpgleFormatterException {

        // The directives cannot be put on lines of their own, because the
        // keyword encloses them.
        // @formatter:off
        String[] source = new String[] {
            "DCL-PR p IND EXTPROC(",
            "/IF DEFINED(X)",
            "  'P3X'",
            "/ELSE",
            "  'P3'",
            "/ENDIF",
            ");",
            "END-PR;"
        };
        // @formatter:on

        String[] result = format(source);

        assertEquals(8, result.length);
        for (int i = 0; i < 7; i++) {
            assertEquals(source[i], result[i]);
        }
        assertEquals("end-pr;", result[7]);
    }

    @Test
    public void format_commentInsideBranch_unchanged() throws RpgleFormatterException {

        // The formatter would move the comment in front of the statement.
        // @formatter:off
        String[] source = new String[] {
            "DCL-S name",
            "/IF DEFINED(X)",
            "  // Unicode",
            "  UCS2( 50 )",
            "/ELSE",
            "  VARCHAR( 50 )",
            "/ENDIF",
            ";"
        };
        // @formatter:on

        String[] result = format(source);

        assertArrayEquals(source, result);
    }

    // ----------------------------------------------------------------------
    // Statements split between a copy member and the source
    // ----------------------------------------------------------------------

    @Test
    public void format_prototypeHeaderInCopyMember_keepsParametersUnchanged() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "/copy qcpysrc,prhead",
            "  a INT( 10 );",
            "  b INT( 10 );",
            "END-PR;",
            "DCL-S x INT( 10 );",
            "DCL-PR p4 IND;",
            "  y INT( 10 );",
            "END-PR;"
        );
        // @formatter:on

        assertEquals(8, result.length);
        assertEquals("/copy qcpysrc,prhead", result[0]);
        assertEquals("  a INT( 10 );", result[1]); // header unknown
        assertEquals("  b INT( 10 );", result[2]); // header unknown
        assertEquals("end-pr;", result[3]);
        assertEquals("dcl-s x int(10);", result[4]);
        assertEquals("dcl-pr p4 ind;", result[5]);
        assertEquals("  y int(10);", result[6]);
        assertEquals("end-pr;", result[7]);
    }

    @Test
    public void format_copyMemberWithSubfieldsOnly_unchanged() throws RpgleFormatterException {

        // @formatter:off
        String[] source = new String[] {
            "  f1 CHAR( 10 );",
            "  f2 INT( 10 );"
        };
        // @formatter:on

        String[] result = format(source);

        assertArrayEquals(source, result);
    }

    @Test
    public void format_copyMemberWithIncludeGuard_formatsPrototype() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "/if not defined(LOG4RPG_PROTOS)",
            "/define LOG4RPG_PROTOS",
            "DCL-PR p1 IND;",
            "  a INT( 10 );",
            "END-PR;",
            "/endif"
        );
        // @formatter:on

        assertEquals(6, result.length);
        assertEquals("/if not defined(LOG4RPG_PROTOS)", result[0]);
        assertEquals("/define LOG4RPG_PROTOS", result[1]);
        assertEquals("dcl-pr p1 ind;", result[2]);
        assertEquals("  a int(10);", result[3]);
        assertEquals("end-pr;", result[4]);
        assertEquals("/endif", result[5]);
        assertEquals(0, formatter.getWarningCount());
    }

    // ----------------------------------------------------------------------
    // /EOF
    //
    // The compiler ignores the lines after /EOF, unless /EOF is part of a
    // conditional group, such as an include guard of a copy member.
    // ----------------------------------------------------------------------

    @Test
    public void format_linesAfterEof_unchanged() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "DCL-S x INT( 10 );",
            "/eof",
            "This member contains the prototypes of the",
            "logging service program.",
            "DCL-S y INT( 10 );",
            "DCL-DS notClosed;"
        );
        // @formatter:on

        assertEquals(6, result.length);
        assertEquals("dcl-s x int(10);", result[0]);
        assertEquals("/eof", result[1]);
        assertEquals("This member contains the prototypes of the", result[2]);
        assertEquals("logging service program.", result[3]);
        assertEquals("DCL-S y INT( 10 );", result[4]);
        assertEquals("DCL-DS notClosed;", result[5]);
    }

    @Test
    public void format_eofInsideConditionalGroup_formatsRestOfMember() throws RpgleFormatterException {

        // Include guard of a copy member, see /EOF in the ILE RPG Reference
        // @formatter:off
        String[] result = format(
            "/IF DEFINED(XYZ_COPIED)",
            "/EOF",
            "/ELSE",
            "/DEFINE XYZ_COPIED",
            "DCL-PR p1 IND;",
            "  a INT( 10 );",
            "END-PR;",
            "/ENDIF"
        );
        // @formatter:on

        assertEquals(8, result.length);
        assertEquals("dcl-pr p1 ind;", result[4]);
        assertEquals("  a int(10);", result[5]);
        assertEquals("end-pr;", result[6]);
        assertEquals(0, formatter.getWarningCount());
    }

    @Test
    public void format_eofAfterConditionalGroup_ignoresRestOfMember() throws RpgleFormatterException {

        // @formatter:off
        String[] result = format(
            "/if defined(X)",
            "DCL-S a INT( 10 );",
            "/endif",
            "/eof",
            "DCL-S b INT( 10 );"
        );
        // @formatter:on

        assertEquals(5, result.length);
        assertEquals("dcl-s a int(10);", result[1]);
        assertEquals("/eof", result[3]);
        assertEquals("DCL-S b INT( 10 );", result[4]);
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------

    private String[] format(String... source) throws RpgleFormatterException {
        return formatter.format(new TextLinesInput(source), 0).toLines();
    }

    /**
     * Asserts that the formatted source contains the compiler directives of the
     * original source in the same order, each on a line of its own.
     */
    private void assertDirectivesPreserved(String[] source, String[] result) {

        assertEquals("Compiler directives", getDirectives(source), getDirectives(result));

        for (String line : result) {
            assertFalse("Compiler directive merged into a statement: " + line, MERGED_DIRECTIVE.matcher(line).matches());
        }
    }

    private List<String> getDirectives(String[] lines) {

        List<String> directives = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches("/[a-zA-Z].*")) {
                directives.add(trimmed.toLowerCase());
            }
        }

        return directives;
    }
}
