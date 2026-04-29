/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;
import de.tools400.lpex.irpgformatter.statement.CollectedStatement;

/**
 * Unit tests for {@link RpgleFormatter}.
 */
public class RpgleFormatterTest extends AbstractTestCase {

    private static int MAX_LINE_LENGTH = 90;

    private RpgleFormatter formatter;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        formatter = new RpgleFormatter();
        formatter.setSourceLength(MAX_LINE_LENGTH);
    }

    // --- format - test source ---

    @Test
    public void format_source() throws RpgleFormatterException {

        // @formatter:off
        String[] line = formatter.format(new TextLinesInput(
            "Ctl-Opt Option(*SrcStmt : *NoDebugIO);",
            "Dcl-Proc myProc Pointer( *Proc );",
            " Dcl-Pi *n;",
            "  marker VarChar( 10 ) Options( *Varsize );",
            " End-Pi;",
            " Dcl-S myPointer Pointer;",
            " Dcl-Pr myPrototype ExtProc( 'MODULE_myPrototype' );",
            "  mySubField1 VarChar(10);",
            "  mySubField2 VarChar(10);",
            " End-Pr;",
            " Dcl-C MY_CONSTANT 1;",
            " Dcl-Ds myStructure LikeDs( refStruct_t );",
            " Return *Null;",
            "End-Proc;"
        ),0).toLines();
        // @formatter:on

        assertEquals("ctl-opt option(*srcstmt: *nodebugio);", line[0]);
        assertEquals("dcl-proc myProc pointer(*proc);", line[1]);
        assertEquals("  dcl-pi *n;", line[2]);
        assertEquals("    marker varchar(10) options(*varsize);", line[3]);
        assertEquals("  end-pi;", line[4]);
        assertEquals("  dcl-s myPointer pointer;", line[5]);
        assertEquals("  dcl-pr myPrototype extproc('MODULE_myPrototype');", line[6]);
        assertEquals("    mySubField1 varchar(10);", line[7]);
        assertEquals("    mySubField2 varchar(10);", line[8]);
        assertEquals("  end-pr;", line[9]);
        assertEquals("  dcl-c MY_CONSTANT 1;", line[10]);
        assertEquals("  dcl-ds myStructure likeds(refStruct_t);", line[11]);
        assertEquals(" Return *Null;", line[12]); // ignored by the formatter
        assertEquals("end-proc;", line[13]);
    }

    // --- format - basic functionality ---

    @Test
    public void format_emptyInput() throws RpgleFormatterException {
        String[] result = formatter.format(new TextLinesInput(), 0).toLines();
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void format_blankLines() throws RpgleFormatterException {
        String[] result = formatter.format(new TextLinesInput("", "", ""), 0).toLines();
        assertEquals(3, result.length);
        assertEquals("", result[0]);
        assertEquals("", result[1]);
        assertEquals("", result[2]);
    }

    @Test
    public void format_freeDirective() throws RpgleFormatterException {
        String[] result = formatter.format(new TextLinesInput("**FREE"), 0).toLines();
        assertEquals(1, result.length);
        assertEquals("**free", result[0]);
    }

    @Test
    public void format_line_comment() throws RpgleFormatterException {
        String[] result = formatter.format(new TextLinesInput("dcl-s aField char(10); // This is a char field"), 0).toLines();
        assertEquals(1, result.length);
        assertEquals("dcl-s aField char(10); // This is a char field", result[0]);
    }

    @Test
    public void format_mixed_comments() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-pr lf_get... // first",
            "",
            "  // middle", 
            "  AppMsg...      // last",
            "  FileName",
            "    Char(10); // comment",
            "end-pr;"
        ),0).toLines();
      // @formatter:on
        assertEquals(5, result.length);
        assertEquals("// first", result[0]);
        assertEquals("// middle", result[1]);
        assertEquals("// last", result[2]);
        assertEquals("dcl-pr lf_getAppMsgFileName char(10); // comment", result[3]);
        assertEquals("end-pr;", result[4]);
    }

    @Test
    public void format_line_comment_of_continued_line() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-s aField",
            "  char(10); // This is a char field"
        ),0).toLines();
        // @formatter:on

        assertEquals(1, result.length);
        assertEquals("dcl-s aField char(10); // This is a char field", result[0]);
    }

    // --- format - declaration statements ---

    @Test
    public void format_dclS() throws RpgleFormatterException {
        String[] result = formatter.format(new TextLinesInput("dcl-s   myVar   char( 10 );"), 0).toLines();
        assertEquals(1, result.length);
        // Should normalize whitespace and trim bracket spaces
        assertTrue(result[0].contains("myVar"));
        assertTrue(result[0].contains("char(10)"));
    }

    @Test
    public void format_dclC() throws RpgleFormatterException {
        String[] result = formatter.format(new TextLinesInput("dcl-c MY_CONST const(100);"), 0).toLines();
        assertEquals(1, result.length);
        // Should remove const() wrapper (default behavior)
        assertTrue(result[0].contains("MY_CONST"));
        assertTrue(result[0].contains("100"));
        // const() should be removed
        assertTrue(!result[0].toLowerCase().contains("const("));
        assertEquals("dcl-c MY_CONST 100;", result[0]);
    }

    @Test
    public void format_dclC_withConstKeywordEnabled() throws RpgleFormatterException {

        // Enable use const keyword preference
        formatter.getConfig().setUseConstKeyword(true);

        // Test with const() already present - should keep it
        String[] result = formatter.format(new TextLinesInput("dcl-c MY_CONST const(100);"), 0).toLines();
        assertEquals(1, result.length);
        assertTrue(result[0].contains("MY_CONST"));
        assertTrue(result[0].toLowerCase().contains("const(100)"));

        // Test without const() - should add it
        result = formatter.format(new TextLinesInput("dcl-c MY_CONST 100;"), 0).toLines();
        assertEquals(1, result.length);
        assertTrue(result[0].contains("MY_CONST"));
        assertTrue(result[0].toLowerCase().contains("const(100)"));
    }

    @Test
    public void format_dclC_withConstKeywordDisabled() throws RpgleFormatterException {
        // Ensure use const keyword is disabled (default)
        formatter.getConfig().setUseConstKeyword(false);

        // Test with const() present - should remove it
        String[] result = formatter.format(new TextLinesInput("dcl-c MY_CONST const(100);"), 0).toLines();
        assertEquals(1, result.length);
        assertTrue(result[0].contains("MY_CONST"));
        assertTrue(result[0].contains("100"));
        assertTrue(!result[0].toLowerCase().contains("const("));

        // Test without const() - should stay without
        result = formatter.format(new TextLinesInput("dcl-c MY_CONST 'Hello';"), 0).toLines();
        assertEquals(1, result.length);
        assertTrue(result[0].contains("MY_CONST"));
        assertTrue(result[0].contains("'Hello'"));
        assertTrue(!result[0].toLowerCase().contains("const("));
    }

    @Test
    public void format_multiLineLiteral_with_spaces() throws RpgleFormatterException {

        // Literal with spaces. Do not exceed maximum name/literal length, break
        // after space

        formatter.getConfig().setMaxNameLength(90);

        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-c MESSAGE 'This is a very long literal, exceeding the maximum line +",
            "  length of 90 characters. Lines inside the literal must end with the +",
            "  plus character.';"
        ),0).toLines();
        // @formatter:on

        assertEquals(2, result.length);
        assertEquals("dcl-c MESSAGE 'This is a very long literal, exceeding the maximum line length of 90 +", result[0]);
        assertEquals("  characters. Lines inside the literal must end with the plus character.';", result[1]);
    }

    @Test
    public void format_multiLineLiteral_without_space() throws RpgleFormatterException {

        // Literal without spaces. Break at maximum name/literal length

        formatter.getConfig().setMaxNameLength(50);

        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-c MESSAGE 'This_is_a_very_long_literal,_exceeding_the_maximu+",
            "  m_line_length_of_90_characters._Lines_inside_the_literal_must_+",
            "  end_with_the_plus_character.';"
        ),0).toLines();
        // @formatter:on

        assertEquals(3, result.length);
        assertEquals("dcl-c MESSAGE 'This_is_a_very_long_literal,_exceeding_the_maximu+", result[0]);
        assertEquals("  m_line_length_of_90_characters._Lines_inside_the_l+", result[1]);
        assertEquals("  iteral_must_end_with_the_plus_character.';", result[2]);
    }

    @Test
    public void format_multiLineLiteral_starting_on_next_line() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-c LONG_CONSTANT_NAME_ENFORCING_THE_VALUE_TO_START_ON_THE_NEXT_LINE 'Literal+",
            "  StartingOnTheNextLine.';"
        ),0).toLines();
        // @formatter:on

        // Name (64 chars) > MAX_NAME_LENGTH (60), so name is split with
        // ellipsis
        assertEquals(2, result.length);
        assertEquals("dcl-c LONG_CONSTANT_NAME_ENFORCING_THE_VALUE_TO_START_ON_THE_NEXT_...", result[0]);
        assertEquals("  LINE 'LiteralStartingOnTheNextLine.';", result[1]);
    }

    // --- format - dcl-c line breaking ---

    @Test
    public void format_dclC_longName_ellipsis() throws RpgleFormatterException {

        formatter.setSourceLength(60);

        String longName = "ABCDEFGHIJ_ABCDEFGHIJ_ABCDEFGHIJ_ABCDEFGHIJ_ABCDEFGHIJ_ABCDEFGHIJ_ABCDEFGHIJ_ABCDEFGHIJ_ABCDEFGHIJ_ABCDEFGHIJ_ABCDEFGHIJ_ABCDEFGHIJ";
        String line = "dcl-c " + longName + " 100;";
        String[] result = formatter.format(new TextLinesInput(line), 0).toLines();
        assertEquals(3, result.length);

        assertTrue("First line should start with dcl-c", result[0].startsWith("dcl-c"));
        assertTrue("First line should end with ellipsis", result[0].endsWith("..."));

        assertTrue("Second line should be indented", result[1].startsWith("  "));
        assertTrue("Second line should end with ellipsis", result[1].endsWith("..."));

        assertTrue("Third line should be indented", result[2].startsWith("  "));
        assertTrue("Third line should contain the value", result[2].contains("100"));
        assertTrue("Third line should end with semicolon", result[2].endsWith(";"));

        assertEquals(line, buildStatement(result));
    }

    @Test
    public void format_dclC_longLiteral_plus() throws RpgleFormatterException {
        String literal = "'This is a very long literal value that definitely exceeds the maximum allowed line width of ninety chars'";
        String line = "dcl-c MESSAGE " + literal + ";";
        String[] result = formatter.format(new TextLinesInput(line), 0).toLines();

        assertTrue("Should produce multiple lines", result.length >= 2);
        assertTrue("First line should contain plus continuation", result[0].endsWith("+"));
        assertTrue("Last line should end with semicolon", result[result.length - 1].trim().endsWith("';"));

        assertEquals(line, buildStatement(result));
    }

    @Test
    public void format_dclC_longName_and_literal() throws RpgleFormatterException {

        formatter.setSourceLength(70);

        String longName = "A_VERY_LONG_CONSTANT_NAME_THAT_EXCEEDS_THE_MAXIMUM_NAME_LENGTH_LIMIT_IN_FACT_IST_MUST_BE_SPLITTED_MORE_THAN_ONCE_TO_GET_A_VALID_RESULT";
        String literal = "'This is a literal value that combined with the long name exceeds the line width. Even the literal must be splitted three time for producing a valid result.'";
        String line = "dcl-c " + longName + " " + literal + ";";
        String[] result = formatter.format(new TextLinesInput(line), 0).toLines();

        assertTrue("Should produce 5 lines", result.length == 5);

        assertTrue("First line should end with ellipsis", result[0].endsWith("..."));
        assertTrue("Second line should end with ellipsis", result[1].endsWith("..."));
        assertTrue("Second line should be indented", result[1].startsWith("  "));
        assertTrue("Third line should end with +", result[2].endsWith("+"));
        assertTrue("Third line should be indented", result[2].startsWith("  "));
        assertTrue("Fourth line should end with +", result[3].endsWith("+"));
        assertTrue("Fourth line should be indented", result[3].startsWith("  "));
        assertTrue("Fifth line should end with ;", result[4].endsWith(";"));
        assertTrue("Fifth line should be indented", result[4].startsWith("  "));

        assertEquals(line, buildStatement(result));
    }

    @Test
    public void format_dclC_numeric_no_break() throws RpgleFormatterException {

        formatter.setSourceLength(70);

        String longName = "A_VERY_LONG_NUMERIC_CONSTANT_NAME_THAT_EXCEEDS_MAX_LINE_WIDTH_WITH_VALUE_APPENDED";
        String value = "12345";
        String line = "dcl-c " + longName + " " + value + ";";
        String[] result = formatter.format(new TextLinesInput(line), 0).toLines();
        assertTrue("Should produce multiple lines", result.length == 2);

        assertTrue("First line should end with ellipsis", result[0].endsWith("..."));
        assertTrue("Second line should contain the value", result[1].contains(value + ";"));

        assertEquals(line, buildStatement(result));
    }

    @Test
    public void format_dclC_numeric_value_too_long() throws RpgleFormatterException {

        formatter.setSourceLength(60);

        String longName = "LONG_CONSTANT_NAME_FORCING_THE_VALUE_TO_BE_WRITTEN_TO_THE_NEXT_LINE";
        String value = "12345678901234567890";
        String line = "dcl-c " + longName + " " + value + ";";
        String[] result = formatter.format(new TextLinesInput(line), 0).toLines();
        assertTrue("Should produce multiple lines", result.length == 2);

        assertTrue("First line should end with ellipsis", result[0].endsWith("..."));
        assertTrue("Second line should contain the value", result[1].contains(value + ";"));

        assertEquals(line, buildStatement(result));
    }

    @Test
    public void format_dclC_minLiteralLength() throws RpgleFormatterException {

        // Set MIN_LITERAL_LENGTH high to force value to next line
        // With minLiteralLength=80, name "MY_CONST" (8 chars):
        // prefix = "dcl-c MY_CONST" (14), available = 90 - 15 - 1 = 74 < 80
        // So value is moved to the next line

        formatter.getConfig().setMinLiteralLength(80);

        // Literal of 76 chars to exceed line width (6+8+1+76+1 = 92 > 90)
        String literal = "'This literal value is long enough to force a line break when on the same line'";
        String[] result = formatter.format(new TextLinesInput("dcl-c MY_CONST " + literal + ";"), 0).toLines();

        assertEquals(3, result.length);
        assertTrue("First line starts with dcl-c", result[0].startsWith("dcl-c MY_CONST"));
        assertTrue("Constant starts on second line", result[1].startsWith("  '"));
        assertTrue("Constant continues on third line", result[1].endsWith("+"));
        assertTrue("Third line is indented", result[1].startsWith("  "));
        assertTrue("Constant ends on third line", result[2].endsWith("';"));

        // Verify round-trip
        String expected = "dcl-c MY_CONST 'This literal value is long enough to force a line break when on the same line';";
        assertEquals(expected, buildStatement(new String[] { result[0], result[1], result[2] }));
    }

    // --- format - DCL-PI/PR line breaking ---

    @Test
    public void format_dclPr_breakBeforeExtproc() throws RpgleFormatterException {
        // Total 93 chars > 90, should break before extproc (preferred)
        // @formatter:off
        
        formatter.getConfig().setBreakBeforeKeyword(true);

        String[] result = formatter.format(new TextLinesInput(
            "dcl-pr myProcedureWithReturnValue char(10) ExtProc('MODULE_NAME_myProcedureWithReturnValue');",
            "end-pr;"
        ),0).toLines();
        // @formatter:on
        assertEquals(3, result.length);
        assertEquals("dcl-pr myProcedureWithReturnValue char(10)", result[0]);
        assertEquals("  extproc('MODULE_NAME_myProcedureWithReturnValue');", result[1]);
        assertEquals("end-pr;", result[2]);

        // Verify round-trip
        String expected = "dcl-pr myProcedureWithReturnValue char(10) extproc('MODULE_NAME_myProcedureWithReturnValue');";
        assertEquals(expected, buildStatement(new String[] { result[0], result[1] }));
    }

    @Test
    public void format_dclPi_breakBeforeExtpgm() throws RpgleFormatterException {

        formatter.getConfig().setBreakBeforeKeyword(true);

        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-pi myProcedureForExternalProgram char(10) ExtPgm('MODULE_myProcedureForExternalProgram');",
            "end-pi;"
        ),0).toLines();
        // @formatter:on
        assertEquals(3, result.length);
        assertEquals("dcl-pi myProcedureForExternalProgram char(10)", result[0]);
        assertEquals("  extpgm('MODULE_myProcedureForExternalProgram');", result[1]);
        assertEquals("end-pi;", result[2]);
    }

    @Test
    public void format_dclPr_nameEllipsis() throws RpgleFormatterException {

        formatter.setSourceLength(60);

        // Name 70 chars exceeds maxNameLength(60) - keyword(6) - space(1) = 53
        String longName = "aVeryLongProcedureNameThatDefinitelyExceedsTheMaximumNameLengthLimitXX";
        String inputLine = "dcl-pr " + longName + " varchar(1000);";
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            inputLine,
            "end-pr;"
        ),0).toLines();
        // @formatter:on
        assertEquals(3, result.length);
        assertTrue("First line should starts with 'dcl-pr'", result[0].startsWith("dcl-pr"));
        assertTrue("First line should end with ellipsis", result[0].endsWith("..."));
        assertTrue("Second line should be indented", result[1].startsWith("  mumName"));
        assertTrue("Second line should end with semicolon", result[1].endsWith(";"));
        assertEquals("end-pr;", result[2]);

        // Verify round-trip
        assertEquals(inputLine, buildStatement(new String[] { result[0], result[1] }));
    }

    @Test
    public void format_dclPr_nameEllipsis_and_extproc() throws RpgleFormatterException {
        // Name 77 chars: beforeExt = 7+77+9 = 93 > 90, forces name ellipsis +
        // extproc on next line
        String longName = "aVeryLongProcedureNameThatDefinitelyExceedsTheMaximumNameLengthLimitForTestXX";
        String inputLine = "dcl-pr " + longName + " char(10) extproc('MODULE_NAME');";
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            inputLine,
            "end-pr;"
        ),0).toLines();
        // @formatter:on
        assertEquals(3, result.length);
        assertTrue("First line should end with ellipsis", result[0].endsWith("..."));
        assertTrue("Second line should be indented", result[1].startsWith("  "));
        assertTrue("Second line should contain return type", result[1].contains("char(10)"));
        assertTrue("Second line should contain extproc", result[1].trim().contains("extproc("));
        assertTrue("Second line should end with semicolon", result[2].trim().endsWith(";"));
        assertEquals("end-pr;", result[2]);

        // Verify round-trip
        assertEquals(inputLine, buildStatement(new String[] { result[0], result[1] }));
    }

    @Test
    public void format_dclPr_extprocLiteralPlus() throws RpgleFormatterException {

        formatter.getConfig().setBreakBeforeKeyword(true);

        // extproc literal too long - needs plus continuation
        String inputLine = "dcl-pr myProc extproc('THIS_IS_A_VERY_LONG_MODULE_NAME_THAT_EXCEEDS_THE_MAXIMUM_ALLOWED_LINE_WIDTH_FOR_FORMATTER');";
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            inputLine,
            "end-pr;"
        ),0).toLines();
        // @formatter:on
        assertEquals(4, result.length);
        assertEquals("dcl-pr myProc", result[0]);
        assertTrue("Second line should start with extproc", result[1].trim().startsWith("extproc("));
        assertTrue("Second line should end with +", result[1].endsWith("+"));
        assertTrue("Third line should end with closing", result[2].trim().endsWith("');"));
        assertEquals("end-pr;", result[3]);

        // Verify round-trip
        assertEquals(inputLine, buildStatement(new String[] { result[0], result[1], result[2] }));
    }

    @Test
    public void format_dclPr_extprocVariable() throws RpgleFormatterException {

        formatter.getConfig().setBreakBeforeKeyword(true);
        formatter.setSourceLength(60);

        // extproc with variable name (not literal) - break before extproc
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-pr sortProcedureWithAVeryLongName char(10) ExtProc(pSortProcedureWithAVeryLongNamePtr);",
            "end-pr;"
        ),0).toLines();
        // @formatter:on
        assertEquals(3, result.length);
        assertEquals("dcl-pr sortProcedureWithAVeryLongName char(10)", result[0]);
        assertEquals("  extproc(pSortProcedureWithAVeryLongNamePtr);", result[1]);
        assertEquals("end-pr;", result[2]);
    }

    // --- format - Long keywords breaking ---

    @Test
    public void format_ctlOpt_breakAtOpenBracket() throws RpgleFormatterException {

        formatter.getConfig().setBreakBeforeKeyword(true);

        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "CTL-OPT DATFMT(*ISO) TIMFMT(*ISO) DECEDIT('0,') OPTION(*SRCSTMT : *NODEBUGIO) COPYRIGHT('Donald Duck');"
        ),0).toLines();
        // @formatter:on
        assertTrue("First line must end before the literal",
            "ctl-opt datfmt(*iso) timfmt(*iso) decedit('0,') option(*srcstmt: *nodebugio)".equals(result[0]));
        assertTrue("Second line ends with the indented literal", "  copyright('Donald Duck');".equals(result[1]));
    }

    @Test
    public void format_ctlOpt_do_not_break_keywords() throws RpgleFormatterException {

        formatter.getConfig().setBreakBeforeKeyword(true);

        String[] result = new String[0];

        formatter.setSourceLength(50);

        // @formatter:off
        result = formatter.format(new TextLinesInput(
            "CTL-OPT DATFMT(*ISO) TIMFMT(*ISO) DECEDIT('0,') OPTION(*SRCSTMT : *NODEBUGIO) COPYRIGHT('Donald Duck');"
        ),0).toLines();
        // @formatter:on

        assertTrue("First line must end before the literal", "ctl-opt datfmt(*iso) timfmt(*iso) decedit('0,')".equals(result[0]));
        assertTrue("Second line ends before the literal", "  option(*srcstmt: *nodebugio)".equals(result[1]));
        assertTrue("Third line ends with the indented literal", "  copyright('Donald Duck');".equals(result[2]));
    }

    @Test
    public void format_ctlOpt_do_not_break_keywords_short_literals() throws RpgleFormatterException {

        String[] result = new String[0];

        // try {

        formatter.getConfig().setMinLiteralLength(6);
        formatter.setSourceLength(50);

        // @formatter:off
            String inputLine = "CTL-OPT DATFMT(*ISO) TIMFMT(*ISO) DECEDIT('0,') OPTION(*SRCSTMT:*NODEBUGIO) COPYRIGHT('*** Donald Duck greets the world! ***');";
            result = formatter.format(new TextLinesInput(
                inputLine
            ),0).toLines();
            // @formatter:on
        // } finally {
        // formatter.setSourceLength(MAX_LINE_LENGTH);
        // getPreferences().setMinLiteralLength(Preferences.DEFAULT_MIN_LITERAL_LENGTH);
        // }

        for (String line : result) {
            System.out.println(line);
        }

        assertEquals(3, result.length);
        assertTrue("First line must end before the 'option' keyword", "ctl-opt datfmt(*iso) timfmt(*iso) decedit('0,')".equals(result[0]));
        assertTrue("Second line starts with the indented 'option' keyword", result[1].startsWith("  option("));
        assertTrue("Second line starts with the copyright keyword", result[1].endsWith("copyright("));
        assertTrue("Third line holds the literal", result[2].equals("  '*** Donald Duck greets the world! ***');"));
        // Verify round-trip of header
        assertEquals(inputLine.toLowerCase().replaceAll(":", ": "), buildStatement(new String[] { result[0], result[1], result[2] }).toLowerCase());
    }

    // --- format - DCL-DS line breaking ---

    @Test
    public void format_dclDs_nameEllipsis() throws RpgleFormatterException {
        // Name 75 chars: total = 7+75+10 = 92 > 90, name(75) > maxName(53)

        formatter.getConfig().setMaxNameLength(53);
        formatter.setSourceLength(70);

        String longName = "aVeryLongDataStructureNameThatDefinitelyExceedsTheMaximumNameLengthLimitAndThatTakesInTheFinalFormattedSourceCode123456789";
        String inputLine = "dcl-ds " + longName + " template;";
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            inputLine,
            "  field1 char(10);",
            "end-ds;"
        ),0).toLines();
        // @formatter:on
        assertEquals(5, result.length);
        assertTrue("First line should end with ellipsis", result[0].endsWith("..."));
        assertTrue("Second line should be indented", result[1].startsWith("  "));
        assertTrue("Third line should contain template", result[2].contains("template"));
        assertEquals("  field1 char(10);", result[3]);
        assertEquals("end-ds;", result[4]);

        // Verify round-trip of header
        assertEquals(inputLine, buildStatement(new String[] { result[0], result[1], result[2] }));
        assertEquals("field1 char(10);", buildStatement(new String[] { result[3] }));
        assertEquals("end-ds;", buildStatement(new String[] { result[4] }));
    }

    @Test
    public void format_dclDs_nameEllipsis_multipleKeywords() throws RpgleFormatterException {

        formatter.setSourceLength(60);

        String longName = "aVeryLongDataStructureNameThatDefinitelyExceedsTheMaximumNameLengthLimitAndThatTakesInTheFinalFormattedSourceCode";
        String inputLine = "dcl-ds " + longName + " qualified based(pDummy);";
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            inputLine,
            "  field1 char(10);",
            "end-ds;"
        ),0).toLines();
        // @formatter:on
        assertEquals(5, result.length);
        assertTrue("First line should end with ellipsis", result[0].endsWith("..."));
        assertTrue("Second line should be indented", result[1].startsWith("  "));
        assertTrue("Second line should end with ellipsis", result[1].endsWith("..."));
        assertTrue("Third line should be indented", result[2].startsWith("  "));
        assertTrue("Third line should contain qualified", result[2].contains("qualified"));
        assertTrue("Third line should contain based", result[2].contains("based(pDummy)"));
        assertTrue("Third line should end with semicolon", result[2].endsWith(";"));

        // Verify round-trip of header
        assertEquals(inputLine, buildStatement(new String[] { result[0], result[1], result[2] }));
        assertEquals("field1 char(10);", buildStatement(new String[] { result[3] }));
        assertEquals("end-ds;", buildStatement(new String[] { result[4] }));
    }

    @Test
    public void format_dclPr_extprocLiteralPlus_minLiteralLength() throws RpgleFormatterException {

        // Set minLiteralLength very high (80) so that the extproc literal
        // cannot
        // start on the same line as extproc(. With default prefix " extproc("
        // (10
        // chars), available = 90 - 10 - 1 = 79 < 80, forcing extproc( onto its
        // own line with the literal starting on the next line.

        formatter.getConfig().setMinLiteralLength(80);

        String inputLine = "dcl-pr myProc extproc('THIS_IS_A_VERY_LONG_MODULE_NAME_THAT_EXCEEDS_THE_MAXIMUM_ALLOWED_LINE_WIDTH_FOR_FORMATTER');";
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            inputLine,
            "end-pr;"
        ),0).toLines();
            
        for (String line : result) {
            System.out.println(line);
        }
        // @formatter:on

        assertEquals(4, result.length);
        assertTrue("First line starts with dcl-pr", result[0].startsWith("dcl-pr myProc extproc("));
        assertTrue("Constant starts on second line", result[1].startsWith("  '"));
        assertTrue("Constant continues on third line", result[1].endsWith("+"));
        assertTrue("Third line is indented", result[1].startsWith("  "));
        assertTrue("Constant ends on third line", result[2].endsWith("');"));

        // Verify round-trip
        String expected = "dcl-pr myProc extproc('THIS_IS_A_VERY_LONG_MODULE_NAME_THAT_EXCEEDS_THE_MAXIMUM_ALLOWED_LINE_WIDTH_FOR_FORMATTER');";
        assertEquals(expected, buildStatement(new String[] { result[0], result[1], result[2] }));
    }

    @Test
    public void format_breakLongLine_minLiteralLength() throws RpgleFormatterException {

        // Test that breakLongLine respects the configurable minLiteralLength.
        // With minLiteralLength=80, a literal that starts near the end of the
        // first line should be moved entirely to the next line.

        formatter.getConfig().setMinLiteralLength(80);

        // dcl-s with a literal value long enough to exceed line width
        // prefix "dcl-s myVar char(10) inz(" = 25 chars, literal starts at
        // offset 25
        // available = 90 - 25 = 65 < 80 (minLiteralLength), so literal
        // should
        // move to next line
        String literal = "'This is a long literal value that is long enough to exceed the maximum line width of ninety'";
        String inputLine = "dcl-s myVar char(100) inz(" + literal + ");";
        String[] result = formatter.format(new TextLinesInput(inputLine), 0).toLines();

        assertTrue("Should produce multiple lines", result.length >= 2);
        // The literal should NOT be split on the first line, but moved to
        // the next line because minLiteralLength is not met
        assertTrue("First line starts with dcl-pr", result[0].startsWith("dcl-s myVar char(100) inz("));
        assertTrue("Constant starts on second line", result[1].startsWith("  '"));
        assertTrue("Constant continues on third line", result[1].endsWith("+"));
        assertTrue("Third line is indented", result[1].startsWith("  "));
        assertTrue("Constant ends on third line", result[2].endsWith("');"));
    }

    // --- format - block statements ---

    @Test
    public void format_dclDs() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-ds myDs;",
            "  field1 char(10);",
            "end-ds;"
        ),0).toLines();
        // @formatter:on
        assertEquals(3, result.length);
        assertTrue(result[0].contains("myDs"));
        assertTrue(result[1].contains("field1"));
    }

    @Test
    public void format_dclPr() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-pr myProc;",
            "  parm1 char(10);",
            "end-pr;"
        ),0).toLines();
        // @formatter:on
        assertEquals(3, result.length);
        assertTrue(result[0].contains("myProc"));
        assertTrue(result[1].contains("parm1"));
    }

    @Test
    public void format_dclPi() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-pi myProc;",
            "  parm1 int;",
            "end-pi;"
        ),0).toLines();
        // @formatter:on
        assertEquals(3, result.length);
        assertTrue(result[0].contains("myProc"));
        assertTrue(result[1].contains("parm1"));
    }

    @Test
    public void format_dclPi_replacesNameWithStarN_whenInsideDclProc() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-proc myProc;",
            "  dcl-pi myProc;",
            "    parm1 char(10);",
            "  end-pi;",
            "end-proc;"
        ),0).toLines();
        // @formatter:on
        assertEquals("dcl-proc myProc;", result[0]);
        assertEquals("  dcl-pi *n;", result[1]);
    }

    @Test
    public void format_dclPi_preservesName_whenNoDclProcParent() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-pi myProc;",
            "  parm1 int;",
            "end-pi;"
        ),0).toLines();
        // @formatter:on
        assertTrue(result[0].contains("myProc"));
    }

    @Test
    public void format_dclPi_preservesStarN_whenAlreadyUsed() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-proc myProc;",
            "  dcl-pi *n;",
            "  end-pi;",
            "end-proc;"
        ),0).toLines();
        // @formatter:on
        assertEquals("  dcl-pi *n;", result[1]);
    }

    @Test
    public void format_dclPi_preservesName_whenPreferenceDisabled() throws RpgleFormatterException {
        formatter.getConfig().setReplacePiName(false);
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-proc myProc;",
            "  dcl-pi myProc;",
            "  end-pi;",
            "end-proc;"
        ),0).toLines();
        // @formatter:on
        assertTrue(result[1].contains("myProc"));
    }

    @Test
    public void format_subfieldIndentation() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-ds myDs;",
            "field1 char(10);",
            "end-ds;"
        ),0).toLines();
        // @formatter:on
        assertEquals(3, result.length);
        // Subfield should be indented
        assertTrue("Subfield should be indented", result[1].startsWith("  "));
    }

    // --- format - compiler directives ---

    @Test
    public void format_includeDirective() throws RpgleFormatterException {
        String[] result = formatter.format(new TextLinesInput("/include myfile"), 0).toLines();
        assertEquals(1, result.length);
        assertTrue(result[0].toLowerCase().startsWith("/include"));
    }

    @Test
    public void format_ifDirective() throws RpgleFormatterException {
        String[] result = formatter.format(new TextLinesInput("/if defined(DEBUG)"), 0).toLines();
        assertEquals(1, result.length);
        assertTrue(result[0].toLowerCase().startsWith("/if"));
    }

    @Test
    public void format_dclDs_withEmbeddedCompilerDirectives() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-ds msg_t qualified           based(pDummy);",
            "/if defined(*V5R3M0)",
            "  ID likeds(msgID_t);",
            "/else",
            "  ID char(7);",
            "/endif",
            "  data char(512);",
            "  type char(10);",
            "  file char(10);",
            "  lib char(10);",
            "end-ds;"
        ),0).toLines();
        // @formatter:on
        assertEquals(11, result.length);

        // dcl-ds header: whitespace normalized
        assertEquals("dcl-ds msg_t qualified based(pDummy);", result[0]);

        // Compiler directives should NOT be indented (unindentCompilerDirectives = true by default)
        assertEquals("/if defined(*V5R3M0)", result[1]);
        assertEquals("/else", result[3]);
        assertEquals("/endif", result[5]);

        // Subfields should be indented and aligned
        assertEquals("  ID   likeds(msgID_t);", result[2]);
        assertEquals("  ID   char(7);", result[4]);
        assertEquals("  data char(512);", result[6]);

        // end-ds should not be indented
        assertEquals("end-ds;", result[10]);
    }

    // --- format - multi-line statements ---

    @Test
    public void format_ellipsisContinuation() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-pr myFirst...",
            "Proc",
            "  char(10);",
            "end-pr;"
        ),0).toLines();
        // @formatter:on
        // Should join the continued lines
        assertEquals(2, result.length);
        assertEquals("dcl-pr myFirstProc char(10);", result[0]);
    }

    // --- ignore evaluation statements ---

    @Test
    public void format_ignore_evaluation_lines() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "  if   (ID = cMSG_ID_NONE) and (data = '');",
            "    p_assert(cFalse:",
            "             'Message data must not be *BLANKS in case that -",
            "               message ID is specified as cMsg_ID_NONE.'           );",
            "  endif;"
        ),0).toLines();
        // @formatter:on
        // Should not join the lines
        assertEquals(result[0], "  if   (ID = cMSG_ID_NONE) and (data = '');");
        assertEquals(result[1], "    p_assert(cFalse:");
        assertEquals(result[2], "             'Message data must not be *BLANKS in case that -");
        assertEquals(result[3], "               message ID is specified as cMsg_ID_NONE.'           );");
        assertEquals(result[4], "  endif;");
    }

    // --- format - whitespace normalization ---

    @Test
    public void format_normalizeSpaces() throws RpgleFormatterException {
        String[] result = formatter.format(new TextLinesInput("dcl-s    isWhat    Ind;"), 0).toLines();
        assertEquals(1, result.length);
        // Should not have multiple consecutive spaces
        assertEquals("dcl-s isWhat ind;", result[0]);
    }

    @Test
    public void format_trimBracketSpaces() throws RpgleFormatterException {
        String[] result = formatter.format(new TextLinesInput("dcl-s isWhat char( 10 );"), 0).toLines();
        assertEquals(1, result.length);
        assertTrue("Should trim spaces inside brackets", result[0].contains("char(10)"));
    }

    // --- format - complete source ---

    // @Test
    public void format_completeProgram() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "**free",
            "",
            "// Control options",
            "ctl-opt dftactgrp(*no);",
            "",
            "// Constants",
            "dcl-c MAX_SIZE const(100);",
            "",
            "// Variables",
            "dcl-s counter int;",
            "",
            "// Data structure",
            "dcl-ds myData;",
            "  field1 char(10);",
            "  field2 int;",
            "end-ds;"
        ),0).toLines();
        // @formatter:on

        assertNotNull(result);
        assertTrue("Should have multiple output lines", result.length >= 10);
        assertEquals("**FREE", result[0]);
    }

    // --- format - sub-field keyword alignment ---

    @Test
    public void format_subFields_not_aligned() throws RpgleFormatterException {

        formatter.getConfig().setAlignSubFields(false);

        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-ds myDs;",
            "  x char(1);",
            "  longFieldName char(50);",
            "  y int(10);",
            "end-ds;"
        ),0).toLines();

        // @formatter:on
        assertEquals(5, result.length);
        assertEquals("  x char(1);", result[1]);
        assertEquals("  longFieldName char(50);", result[2]);
        assertEquals("  y int(10);", result[3]);
    }

    @Test
    public void format_subFieldAlignment_basic() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-ds myDs;",
            "  x char(1);",
            "  longFieldName char(50);",
            "  y int(10);",
            "end-ds;"
        ),0).toLines();
        // @formatter:on
        assertEquals(5, result.length);
        // Longest name: "longFieldName" = 13 chars, alignColumn = 14
        assertEquals("  x             char(1);", result[1]);
        assertEquals("  longFieldName char(50);", result[2]);
        assertEquals("  y             int(10);", result[3]);
    }

    @Test
    public void format_subFieldAlignment_withEllipsis() throws RpgleFormatterException {

        // Name > MAX_NAME_LENGTH (60) triggers ellipsis; does not affect
        // alignment
        formatter.setSourceLength(60);

        String longName = "aLongSubFieldNameThatDefinitelyExceedsTheMaximumNameLengthLimit";
        // String longName = "aLongSubFieldWithLineBreak";
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-ds myDs;",
            "  " + longName + " char(10);",
            "  shortName char(5) inz('1   5');",
            "end-ds;"
        ),0).toLines();
        // @formatter:on

        for (String line : result) {
            System.out.println(line);
        }

        assertTrue("First line of first sub-field line should end with ellipsis", result[1].endsWith("..."));
        assertEquals("  gthLimit  char(10);", result[2]);
        // The ellipsis-split field's keywords also align
        assertEquals("  shortName char(5) inz('1   5');", result[result.length - 2]);
    }

    @Test
    public void format_subFieldAlignment_dclPi() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-pi myProc;",
            "  pName char(50);",
            "  pID int(10);",
            "end-pi;"
        ),0).toLines();
        // @formatter:on
        assertEquals(4, result.length);
        // Longest name: "pName" = 5 chars, alignColumn = 6
        assertEquals("  pName char(50);", result[1]);
        assertEquals("  pID   int(10);", result[2]);
    }

    @Test
    public void format_subFieldAlignment_dclPr() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-pr myProc;",
            "  pName char(50);",
            "  pID int(10);",
            "end-pr;"
        ),0).toLines();
        // @formatter:on
        assertEquals(4, result.length);
        // Longest name: "pName" = 5 chars, alignColumn = 6
        assertEquals("  pName char(50);", result[1]);
        assertEquals("  pID   int(10);", result[2]);
    }

    @Test
    public void format_subFieldAlignment_compilerDirectivesIgnored() throws RpgleFormatterException {
        // Compiler directives within a block must NOT affect alignment
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-ds myDs;",
            "/if defined(USE_LONG)",
            "  longFieldName varchar(200);",
            "/else",
            "  longFieldName varchar(100);",
            "/endif",
            "  x int(10);",
            "end-ds;"
        ),0).toLines();
        // @formatter:on
        // Longest name: "longFieldName" = 13 chars, alignColumn = 14
        assertEquals("  longFieldName varchar(200);", result[2]);
        assertEquals("  longFieldName varchar(100);", result[4]);
        assertEquals("  x             int(10);", result[6]);
    }

    @Test
    public void format_subFieldAlignment_singleField() throws RpgleFormatterException {
        // Block with a single sub-field: minimal padding (name + 1 space)
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-ds myDs;",
            "  field1 char(10);",
            "end-ds;"
        ),0).toLines();
        // @formatter:on
        assertEquals(3, result.length);
        // "field1" = 6 chars, alignColumn = 7, so 1 space after name
        assertEquals("  field1 char(10);", result[1]);
    }

    // --- format - formatter directives (@formatter:off / @formatter:on) ---

    @Test
    public void format_formatterDirective_basicOffOn() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-s   myVar1   char( 10 );",
            "// @formatter:off",
            "dcl-s   myVar2   char( 10 );",
            "// @formatter:on",
            "dcl-s   myVar3   char( 10 );"
        ),0).toLines();
        // @formatter:on
        assertEquals(5, result.length);
        assertEquals("dcl-s myVar1 char(10);", result[0]);
        assertEquals("// @formatter:off", result[1]);
        assertEquals("dcl-s   myVar2   char( 10 );", result[2]);
        assertEquals("// @formatter:on", result[3]);
        assertEquals("dcl-s myVar3 char(10);", result[4]);
    }

    @Test
    public void format_formatterDirective_offUntilEnd() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-s   myVar1   char( 10 );",
            "// @formatter:off",
            "dcl-s   myVar2   char( 10 );",
            "dcl-s   myVar3   char( 10 );"
        ),0).toLines();
        // @formatter:on
        assertEquals(4, result.length);
        assertEquals("dcl-s myVar1 char(10);", result[0]);
        assertEquals("// @formatter:off", result[1]);
        assertEquals("dcl-s   myVar2   char( 10 );", result[2]);
        assertEquals("dcl-s   myVar3   char( 10 );", result[3]);
    }

    @Test
    public void format_formatterDirective_multipleBlocks() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "dcl-s   myVar1   char( 10 );",
            "// @formatter:off",
            "dcl-s   myVar2   char( 10 );",
            "// @formatter:on",
            "dcl-s   myVar3   char( 10 );",
            "// @formatter:off",
            "dcl-s   myVar4   char( 10 );",
            "// @formatter:on",
            "dcl-s   myVar5   char( 10 );"
        ),0).toLines();
        // @formatter:on
        assertEquals(9, result.length);
        assertEquals("dcl-s myVar1 char(10);", result[0]);
        assertEquals("dcl-s   myVar2   char( 10 );", result[2]);
        assertEquals("dcl-s myVar3 char(10);", result[4]);
        assertEquals("dcl-s   myVar4   char( 10 );", result[6]);
        assertEquals("dcl-s myVar5 char(10);", result[8]);
    }

    @Test
    public void format_formatterDirective_caseInsensitive() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "// @FORMATTER:OFF",
            "dcl-s   myVar1   char( 10 );",
            "// @Formatter:On",
            "dcl-s   myVar2   char( 10 );"
        ),0).toLines();
        // @formatter:on
        assertEquals(4, result.length);
        assertEquals("// @FORMATTER:OFF", result[0]);
        assertEquals("dcl-s   myVar1   char( 10 );", result[1]);
        assertEquals("// @Formatter:On", result[2]);
        assertEquals("dcl-s myVar2 char(10);", result[3]);
    }

    @Test
    public void format_formatterDirective_flexibleWhitespace() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "//   @formatter:off",
            "dcl-s   myVar1   char( 10 );",
            "//     @formatter:on",
            "dcl-s   myVar2   char( 10 );"
        ),0).toLines();
        // @formatter:on
        assertEquals(4, result.length);
        assertEquals("//   @formatter:off", result[0]);
        assertEquals("dcl-s   myVar1   char( 10 );", result[1]);
        assertEquals("//     @formatter:on", result[2]);
        assertEquals("dcl-s myVar2 char(10);", result[3]);
    }

    @Test
    public void format_formatterDirective_blankLinesAndComments() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "// @formatter:off",
            "",
            "// a regular comment",
            "dcl-s   myVar1   char( 10 );",
            "// @formatter:on"
        ),0).toLines();
        // @formatter:on
        assertEquals(5, result.length);
        assertEquals("// @formatter:off", result[0]);
        assertEquals("", result[1]);
        assertEquals("// a regular comment", result[2]);
        assertEquals("dcl-s   myVar1   char( 10 );", result[3]);
        assertEquals("// @formatter:on", result[4]);
    }

    @Test
    public void format_formatterDirective_blockStatements() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "// @formatter:off",
            "dcl-ds   myDs ;",
            "  field1   char( 10 ) ;",
            "end-ds ;",
            "// @formatter:on"
        ),0).toLines();
        // @formatter:on
        assertEquals(5, result.length);
        assertEquals("// @formatter:off", result[0]);
        assertEquals("dcl-ds   myDs ;", result[1]);
        assertEquals("  field1   char( 10 ) ;", result[2]);
        assertEquals("end-ds ;", result[3]);
        assertEquals("// @formatter:on", result[4]);
    }

    @Test
    public void format_formatterDirective_redundantDirectives() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "// @formatter:off",
            "// @formatter:off",
            "dcl-s   myVar1   char( 10 );",
            "// @formatter:on",
            "// @formatter:on",
            "dcl-s   myVar2   char( 10 );"
        ),0).toLines();
        // @formatter:on
        assertEquals(6, result.length);
        assertEquals("dcl-s   myVar1   char( 10 );", result[2]);
        assertEquals("dcl-s myVar2 char(10);", result[5]);
    }

    @Test
    public void format_formatterDirective_noFalseTrigger() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "// @formatter:off but with extra text",
            "dcl-s   myVar1   char( 10 );",
            "// @formatter:on  trailing",
            "dcl-s   myVar2   char( 10 );"
        ),0).toLines();
        // @formatter:on
        assertEquals(4, result.length);
        // Both lines should be formatted because the directives have extra text
        assertEquals("dcl-s myVar1 char(10);", result[1]);
        assertEquals("dcl-s myVar2 char(10);", result[3]);
    }

    @Test
    public void format_formatterDirective_continuationLines() throws RpgleFormatterException {
        // @formatter:off
        String[] result = formatter.format(new TextLinesInput(
            "// @formatter:off",
            "dcl-pr myFirst...",
            "Proc",
            "  char(10);",
            "end-pr;",
            "// @formatter:on"
        ),0).toLines();
        // @formatter:on
        assertEquals(6, result.length);
        assertEquals("// @formatter:off", result[0]);
        assertEquals("dcl-pr myFirst...", result[1]);
        assertEquals("Proc", result[2]);
        assertEquals("  char(10);", result[3]);
        assertEquals("end-pr;", result[4]);
        assertEquals("// @formatter:on", result[5]);
    }

    // --- format - end-proc name removal ---

    @Test
    public void format_endProc_removesName_whenPreferenceEnabled() throws RpgleFormatterException {
        // Default: removeEndProcName = true
        String[] result = formatter.format(new TextLinesInput(
            "dcl-proc myProc;",
            "  dcl-pi *n;",
            "  end-pi;",
            "end-proc myProc;"
        ),0).toLines();
        assertEquals("end-proc;", result[3]);
    }

    @Test
    public void format_endProc_preservesName_whenPreferenceDisabled() throws RpgleFormatterException {
        formatter.getConfig().setRemoveEndProcName(false);
        String[] result = formatter.format(new TextLinesInput(
            "dcl-proc myProc;",
            "  dcl-pi *n;",
            "  end-pi;",
            "end-proc myProc;"
        ),0).toLines();
        assertTrue(result[3].contains("myProc"));
    }

    @Test
    public void format_compilerDirective_unindented_whenPreferenceEnabled() throws RpgleFormatterException {
        // Default: unindentCompilerDirectives = true
        String[] result = formatter.format(new TextLinesInput(
            "dcl-proc myProc;",
            "  dcl-pi *n;",
            "  end-pi;",
            "  /copy qcpysrc,types",
            "end-proc;"
        ),0).toLines();
        assertEquals("/copy qcpysrc,types", result[3].trim());
        assertEquals(0, result[3].indexOf("/"));
    }

    @Test
    public void format_compilerDirective_indented_whenPreferenceDisabled() throws RpgleFormatterException {
        formatter.getConfig().setUnindentCompilerDirectives(false);
        String[] result = formatter.format(new TextLinesInput(
            "dcl-proc myProc;",
            "  dcl-pi *n;",
            "  end-pi;",
            "  /copy qcpysrc,types",
            "end-proc;"
        ),0).toLines();
        assertTrue(result[3].startsWith("  "));
        assertTrue(result[3].trim().startsWith("/copy"));
    }

    private String buildStatement(String[] result) throws RpgleFormatterException {

        CollectedStatement statement = new CollectedStatement();
        for (String line : result) {
            statement.add(line);
        }

        return statement.getStatement();
    }
}
