/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.util.ArrayList;
import java.util.List;

import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.FormattedStatement;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

/**
 * Builds the records of the local copy of a source member from the formatted
 * source: the sequence number (SRCSEQ, 6 digits), the date (SRCDAT, YYMMDD) and
 * the source data (SRCDTA) of each line.
 * <p>
 * An unchanged line keeps its date, a changed or inserted line gets the date of
 * today. With <i>Resequence lines at save</i> of RDi, all lines are numbered
 * with the start and the increment of RDi. Otherwise the lines keep their
 * sequence numbers and an inserted line gets the next number behind the line in
 * front of it; only if there is no free number, the member is resequenced.
 */
public class SourceMemberRecords {

    /**
     * The largest sequence number: 9999.99.
     */
    private static final int MAX_SEQUENCE_NUMBER = 999999;

    /**
     * Sequence number of a line, that is not in the member yet.
     */
    private static final int INSERTED = -1;

    /**
     * Length of SRCSEQ and SRCDAT in front of the source data.
     */
    private static final int PREFIX_LENGTH = 12;

    private final int today;
    private final boolean isResequenceAtSave;
    private final int resequenceStart;
    private final int resequenceIncrement;

    /**
     * Creates the builder of the records.
     *
     * @param today - the date of a changed line, YYMMDD
     * @param isResequenceAtSave - <i>Resequence lines at save</i> of RDi
     * @param resequenceStart - the first sequence number of a resequenced
     *        member, in units of SRCSEQ (100 = 1.00)
     * @param resequenceIncrement - the increment of a resequenced member, in
     *        units of SRCSEQ
     */
    public SourceMemberRecords(int today, boolean isResequenceAtSave, int resequenceStart, int resequenceIncrement) {
        this.today = today;
        this.isResequenceAtSave = isResequenceAtSave;
        this.resequenceStart = resequenceStart;
        this.resequenceIncrement = resequenceIncrement;
    }

    /**
     * Returns the records of the formatted source.
     *
     * @param originalRecords - the records of the local copy before formatting
     *        or <code>null</code>, if they are not known
     * @param result - the formatted source
     * @return the records
     */
    public List<String> build(String[] originalRecords, FormattedResult result) {

        List<Line> lines = new ArrayList<>();
        for (FormattedStatement statement : result.getStatements()) {
            String[] formattedLines = statement.getFormattedLines();
            for (int i = 0; i < formattedLines.length; i++) {
                String originalRecord = null;
                if (i < statement.getOriginalLineCount()) {
                    originalRecord = getRecord(originalRecords, statement.getOriginalStartLine() + i);
                }
                lines.add(createLine(originalRecord, formattedLines[i]));
            }
        }

        if (isResequenceAtSave || !numberInsertedLines(lines)) {
            resequence(lines);
        }

        List<String> records = new ArrayList<>();
        for (Line line : lines) {
            records.add(String.format("%06d%06d%s", line.sequenceNumber, line.date, line.text));
        }

        return records;
    }

    /**
     * Returns the record of a line (1-based) or <code>null</code>, if it is not
     * known or has no sequence number and date.
     */
    private static String getRecord(String[] records, int line) {

        if (records == null || line < 1 || line > records.length) {
            return null;
        }

        String record = records[line - 1];
        if (record.length() < PREFIX_LENGTH || !record.substring(0, PREFIX_LENGTH).matches("[0-9]+")) {
            return null;
        }

        return record;
    }

    /**
     * Creates a line, that keeps the sequence number of its original record and
     * its date, if the text is the same.
     */
    private Line createLine(String originalRecord, String text) {

        if (originalRecord == null) {
            return new Line(INSERTED, today, text);
        }

        int sequenceNumber = Integer.parseInt(originalRecord.substring(0, 6));
        int date = Integer.parseInt(originalRecord.substring(6, PREFIX_LENGTH));
        if (!text.equals(StringUtils.trimR(originalRecord.substring(PREFIX_LENGTH)))) {
            date = today;
        }

        return new Line(sequenceNumber, date, text);
    }

    /**
     * Gives each inserted line the next number behind the line in front of it.
     *
     * @return <code>false</code>, if there are not enough free numbers in front
     *         of the next line
     */
    private static boolean numberInsertedLines(List<Line> lines) {

        int i = 0;
        while (i < lines.size()) {
            if (lines.get(i).sequenceNumber != INSERTED) {
                i++;
                continue;
            }

            int end = i;
            while (end < lines.size() && lines.get(end).sequenceNumber == INSERTED) {
                end++;
            }

            int previous = i > 0 ? lines.get(i - 1).sequenceNumber : 0;
            int next = end < lines.size() ? lines.get(end).sequenceNumber : MAX_SEQUENCE_NUMBER + 1;
            if (previous + end - i >= next) {
                return false;
            }

            for (int k = i; k < end; k++) {
                lines.get(k).sequenceNumber = previous + k - i + 1;
            }
            i = end;
        }

        return true;
    }

    /**
     * Numbers all lines with the start and the increment of RDi. Like RDi, a
     * member, whose numbers would exceed 9999.99, is numbered with start and
     * increment 1.00 (fewer than 9999 lines), 0.10 (fewer than 99999 lines) or
     * 0.01. The same applies, if start or increment are not set.
     */
    private void resequence(List<Line> lines) {

        int start = resequenceStart;
        int increment = resequenceIncrement;
        int count = lines.size();

        if (start < 1 || increment < 1 || start + count * increment > MAX_SEQUENCE_NUMBER) {
            if (count < 9999) {
                start = 100;
            } else if (count < 99999) {
                start = 10;
            } else {
                start = 1;
            }
            increment = start;
        }

        for (int i = 0; i < count; i++) {
            lines.get(i).sequenceNumber = start + i * increment;
        }
    }

    /**
     * A line of the member.
     */
    private static final class Line {

        private int sequenceNumber;
        private final int date;
        private final String text;

        Line(int sequenceNumber, int date, String text) {
            this.sequenceNumber = sequenceNumber;
            this.date = date;
            this.text = text;
        }
    }
}
