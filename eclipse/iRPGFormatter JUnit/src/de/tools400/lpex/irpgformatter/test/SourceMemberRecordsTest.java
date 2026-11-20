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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.FormattedStatement;
import de.tools400.lpex.irpgformatter.input.SourceMemberRecords;

/**
 * Unit tests for {@link SourceMemberRecords}: the sequence numbers (SRCSEQ) and
 * dates (SRCDAT) of a source member, that the batch formatted. Unchanged lines
 * keep their date. Without <i>Resequence lines at save</i> of RDi, the lines
 * keep their sequence number, too, and an inserted line gets the next one.
 */
public class SourceMemberRecordsTest extends AbstractTestCase {

    private static final int TODAY = 261120;

    // --- Resequence lines at save: off ---

    @Test
    public void unchangedLines_keepSequenceNumberAndDate() {

        String[] original = { record(100, 260101, "**free"), record(200, 250315, "dcl-s x char(10);") };

        List<String> records = builder(false, 100, 100).build(original, result(statement(1, 1, "**free"), statement(2, 1, "dcl-s x char(10);")));

        assertEquals(Arrays.asList(original), records);
    }

    @Test
    public void changedLine_keepsSequenceNumber_getsToday() {

        String[] original = { record(100, 260101, "**free"), record(200, 250315, "dcl-s   x   char(10);") };

        List<String> records = builder(false, 100, 100).build(original, result(statement(1, 1, "**free"), statement(2, 1, "dcl-s x char(10);")));

        assertEquals(Arrays.asList(record(100, 260101, "**free"), record(200, TODAY, "dcl-s x char(10);")), records);
    }

    @Test
    public void joinedLines_secondLineIsRemoved() {

        String[] original = { record(100, 260101, "**free"), record(300, 250315, "dcl-s   y"), record(400, 250315, "       char(10);"),
            record(500, 240101, "dcl-s z char(10);") };

        List<String> records = builder(false, 100, 100).build(original,
            result(statement(1, 1, "**free"), statement(2, 2, "dcl-s y char(10);"), statement(4, 1, "dcl-s z char(10);")));

        assertEquals(Arrays.asList(record(100, 260101, "**free"), record(300, TODAY, "dcl-s y char(10);"), record(500, 240101, "dcl-s z char(10);")),
            records);
    }

    @Test
    public void splitLine_insertedLineGetsNextSequenceNumber() {

        String[] original = { record(600, 240101, "dcl-c LONG 'aaa bbb';"), record(700, 240101, "dcl-s z char(10);") };

        List<String> records = builder(false, 100, 100).build(original,
            result(statement(1, 1, "dcl-c LONG 'aaa +", "  bbb';"), statement(2, 1, "dcl-s z char(10);")));

        assertEquals(Arrays.asList(record(600, TODAY, "dcl-c LONG 'aaa +"), record(601, TODAY, "  bbb';"), record(700, 240101, "dcl-s z char(10);")),
            records);
    }

    /**
     * No sequence number is free between 1.00 and 1.01: the member is
     * resequenced with the start and the increment of RDi.
     */
    @Test
    public void insertedLineWithoutRoom_resequencesTheMember() {

        String[] original = { record(100, 240101, "dcl-c LONG 'aaa bbb';"), record(101, 240101, "dcl-s z char(10);") };

        List<String> records = builder(false, 100, 100).build(original,
            result(statement(1, 1, "dcl-c LONG 'aaa +", "  bbb';"), statement(2, 1, "dcl-s z char(10);")));

        assertEquals(Arrays.asList(record(100, TODAY, "dcl-c LONG 'aaa +"), record(200, TODAY, "  bbb';"), record(300, 240101, "dcl-s z char(10);")),
            records);
    }

    // --- Resequence lines at save: on ---

    @Test
    public void resequenceAtSave_numbersAllLines_keepsDatesOfUnchangedLines() {

        String[] original = { record(500, 260101, "**free"), record(900, 250315, "dcl-s   x   char(10);"),
            record(1300, 240101, "dcl-s z char(10);") };

        List<String> records = builder(true, 1, 10).build(original,
            result(statement(1, 1, "**free"), statement(2, 1, "dcl-s x char(10);"), statement(3, 1, "dcl-s z char(10);")));

        assertEquals(Arrays.asList(record(1, 260101, "**free"), record(11, TODAY, "dcl-s x char(10);"), record(21, 240101, "dcl-s z char(10);")),
            records);
    }

    /**
     * The numbers would exceed 9999.99: like RDi, a member with fewer than 9999
     * lines is resequenced with start 1.00 and increment 1.00.
     */
    @Test
    public void resequence_numbersTooLarge_fewerThan9999Lines() {

        String[] original = { record(100, 240101, "a"), record(200, 240101, "b") };

        List<String> records = builder(true, 9000, 500000).build(original, result(statement(1, 1, "a"), statement(2, 1, "b")));

        assertEquals(Arrays.asList(record(100, 240101, "a"), record(200, 240101, "b")), records);
    }

    /**
     * 10001 lines with increment 1.00 would exceed 9999.99: like RDi, the
     * member is resequenced with start 0.10 and increment 0.10, so that each
     * sequence number has 6 digits.
     */
    @Test
    public void resequence_moreThan9999Lines_sequenceNumbersHaveSixDigits() {

        int count = 10001;
        String[] original = new String[count];
        FormattedStatement[] statements = new FormattedStatement[count];
        for (int i = 0; i < count; i++) {
            original[i] = record(i + 1, 240101, "line");
            statements[i] = statement(i + 1, 1, "line");
        }

        List<String> records = builder(true, 100, 100).build(original, new FormattedResult(statements));

        assertEquals(count, records.size());
        for (String record : records) {
            assertTrue(record, record.matches("[0-9]{6}240101line"));
        }
        assertEquals(record(10, 240101, "line"), records.get(0));
        assertEquals(record(100010, 240101, "line"), records.get(count - 1));
    }

    private static SourceMemberRecords builder(boolean isResequenceAtSave, int start, int increment) {
        return new SourceMemberRecords(TODAY, isResequenceAtSave, start, increment);
    }

    private static String record(int sequenceNumber, int date, String text) {
        return String.format("%06d%06d%s", sequenceNumber, date, text);
    }

    private static FormattedStatement statement(int originalStartLine, int originalLineCount, String... formattedLines) {
        return new FormattedStatement(originalStartLine, originalLineCount, formattedLines);
    }

    private static FormattedResult result(FormattedStatement... statements) {
        return new FormattedResult(statements);
    }
}
