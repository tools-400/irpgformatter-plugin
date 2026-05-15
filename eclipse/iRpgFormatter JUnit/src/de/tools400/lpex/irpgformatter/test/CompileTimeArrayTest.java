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
import de.tools400.lpex.irpgformatter.statement.CollectedStatement;

/**
 * Unit tests for compile-time array (**CTDATA) handling.
 * <p>
 * Compile-time data sections are introduced by lines starting with two
 * asterisks (e.g. <code>**CTDATA</code>, <code>**ALTSEQ</code>,
 * <code>**FTRANS</code>) and must be treated as single-line statements
 * by the formatter so that the data lines following them are never merged
 * into a multi-line statement.
 * </p>
 * <p>
 * The free-format marker <code>**FREE</code> also starts with two asterisks,
 * but must be identified as {@link StatementType#FREE_DIRECTIVE} and not as
 * {@link StatementType#COMPILE_TIME_ARRAY}.
 * </p>
 */
public class CompileTimeArrayTest extends AbstractTestCase {

    // ----------------------------------------------------------------------
    // StatementIdentifier.identifyStatementType
    // ----------------------------------------------------------------------

    @Test
    public void identify_marker_only() {
        assertEquals(StatementType.COMPILE_TIME_ARRAY, StatementIdentifier.identifyStatementType("**"));
    }

    @Test
    public void identify_ctdata_lowercase() {
        assertEquals(StatementType.COMPILE_TIME_ARRAY, StatementIdentifier.identifyStatementType("**ctdata arrname"));
    }

    @Test
    public void identify_ctdata_uppercase() {
        assertEquals(StatementType.COMPILE_TIME_ARRAY, StatementIdentifier.identifyStatementType("**CTDATA arrname"));
    }

    @Test
    public void identify_ctdata_mixed_case() {
        assertEquals(StatementType.COMPILE_TIME_ARRAY, StatementIdentifier.identifyStatementType("**CtData arrname"));
    }

    @Test
    public void identify_ctdata_with_space_after_marker() {
        assertEquals(StatementType.COMPILE_TIME_ARRAY, StatementIdentifier.identifyStatementType("** ctdata(ctrlEntry)"));
    }

    @Test
    public void identify_ctdata_with_parentheses() {
        assertEquals(StatementType.COMPILE_TIME_ARRAY, StatementIdentifier.identifyStatementType("**ctdata(arrname)"));
    }

    @Test
    public void identify_ctdata_with_trailing_carriage_return() {
        // Windows line endings must not break detection
        assertEquals(StatementType.COMPILE_TIME_ARRAY, StatementIdentifier.identifyStatementType("**ctdata arrname\r"));
    }

    @Test
    public void identify_altseq() {
        // Alternate collating sequence is a **-prefixed compile-time data block
        assertEquals(StatementType.COMPILE_TIME_ARRAY, StatementIdentifier.identifyStatementType("**ALTSEQ"));
    }

    @Test
    public void identify_ftrans() {
        // File translation table is a **-prefixed compile-time data block
        assertEquals(StatementType.COMPILE_TIME_ARRAY, StatementIdentifier.identifyStatementType("**FTRANS"));
    }

    @Test
    public void identify_free_directive_takes_precedence() {
        // **FREE must remain identified as FREE_DIRECTIVE, never as COMPILE_TIME_ARRAY
        assertEquals(StatementType.FREE_DIRECTIVE, StatementIdentifier.identifyStatementType("**FREE"));
    }

    @Test
    public void identify_free_directive_lowercase_takes_precedence() {
        assertEquals(StatementType.FREE_DIRECTIVE, StatementIdentifier.identifyStatementType("**free"));
    }

    @Test
    public void identify_leading_whitespace_is_not_compile_time_array() {
        // The ** marker must be in column 1 (no leading whitespace)
        assertEquals(StatementType.OTHER, StatementIdentifier.identifyStatementType("  **ctdata arrname"));
    }

    @Test
    public void identify_single_asterisk_is_not_compile_time_array() {
        // A single * is a fixed-format comment, not a CTDATA marker
        assertEquals(StatementType.OTHER, StatementIdentifier.identifyStatementType("*ctdata arrname"));
    }

    // ----------------------------------------------------------------------
    // CollectedStatement integration
    //
    // The change being verified here: compile-time array lines must be
    // recognized as single-line statements. The collected statement must be
    // marked complete after a single add() call so that subsequent data
    // lines do not get merged into the same statement.
    // ----------------------------------------------------------------------

    @Test
    public void collectedStatement_completed_after_single_ctdata_line() throws RpgleFormatterException {
        CollectedStatement stmt = new CollectedStatement();
        stmt.add("**ctdata arrname");
        assertTrue("Compile-time array line must terminate the statement", stmt.isComplete());
    }

    @Test
    public void collectedStatement_type_is_compile_time_array() throws RpgleFormatterException {
        CollectedStatement stmt = new CollectedStatement();
        stmt.add("**CTDATA arrname");
        assertEquals(StatementType.COMPILE_TIME_ARRAY, stmt.getType());
    }

    @Test
    public void collectedStatement_marker_only_is_compile_time_array() throws RpgleFormatterException {
        CollectedStatement stmt = new CollectedStatement();
        stmt.add("**");
        assertTrue("Bare ** must terminate the statement", stmt.isComplete());
        assertEquals(StatementType.COMPILE_TIME_ARRAY, stmt.getType());
    }

    @Test
    public void collectedStatement_altseq_is_compile_time_array() throws RpgleFormatterException {
        CollectedStatement stmt = new CollectedStatement();
        stmt.add("**ALTSEQ");
        assertTrue(stmt.isComplete());
        assertEquals(StatementType.COMPILE_TIME_ARRAY, stmt.getType());
    }

    @Test
    public void collectedStatement_ftrans_is_compile_time_array() throws RpgleFormatterException {
        CollectedStatement stmt = new CollectedStatement();
        stmt.add("**FTRANS");
        assertTrue(stmt.isComplete());
        assertEquals(StatementType.COMPILE_TIME_ARRAY, stmt.getType());
    }

    @Test
    public void collectedStatement_with_trailing_carriage_return_is_complete() throws RpgleFormatterException {
        // Lines coming from the LPEX editor may carry a trailing CR;
        // detection must not depend on stripping it.
        CollectedStatement stmt = new CollectedStatement();
        stmt.add("**ctdata arrname\r");
        assertTrue(stmt.isComplete());
        assertEquals(StatementType.COMPILE_TIME_ARRAY, stmt.getType());
    }

    @Test
    public void collectedStatement_free_directive_stays_free_directive() throws RpgleFormatterException {
        // Sanity check: **FREE is still classified as FREE_DIRECTIVE even though
        // the new isCompileTimeArray() check would also match the ** prefix.
        CollectedStatement stmt = new CollectedStatement();
        stmt.add("**FREE");
        assertTrue(stmt.isComplete());
        assertEquals(StatementType.FREE_DIRECTIVE, stmt.getType());
    }

    @Test(expected = RpgleFormatterException.class)
    public void collectedStatement_rejects_second_line_after_ctdata() throws RpgleFormatterException {
        // Once a CTDATA line has terminated the statement, adding another line
        // must throw - data lines belong to a separate statement.
        CollectedStatement stmt = new CollectedStatement();
        stmt.add("**ctdata arrname");
        stmt.add("DATA01");
    }
}
