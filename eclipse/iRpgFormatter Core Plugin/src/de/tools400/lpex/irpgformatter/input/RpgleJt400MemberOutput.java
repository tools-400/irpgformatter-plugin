/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.math.BigDecimal;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.MemberDescription;
import com.ibm.as400.access.Record;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.SequentialFile;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.utils.ClCommandUtils;
import de.tools400.lpex.irpgformatter.utils.DateUtils;
import de.tools400.lpex.irpgformatter.utils.IBMiUtils;

/**
 * {@link IRpgleOutput} implementation for IBM i source members via JT400.
 */
public class RpgleJt400MemberOutput implements IRpgleOutput {

    private final AS400 system;
    private final String library;
    private final String sourceFile;
    private final String sourceMember;
    private final String ifsPath;

    RpgleJt400MemberOutput(AS400 system, String library, String sourceFile, String sourceMember) {
        this.system = system;
        this.library = library;
        this.sourceFile = sourceFile;
        this.sourceMember = sourceMember;
        this.ifsPath = IBMiUtils.getMemberPath(library, sourceFile, sourceMember);
    }

    @Override
    public boolean writeSourceLines(String[] lines) throws RpgleFormatterException {

        try {

            int recordLength = IBMiUtils.getRecordLength(system, library, sourceFile);

            if (!ClCommandUtils.ensureTempSourceFile(system, recordLength)) {
                return false;
            }

            if (!ClCommandUtils.ensureTempMember(system)) {
                return false;
            }

            if (!ClCommandUtils.removeTempMember(system)) {
                return false;
            }

            // TODO: remove debug code
            for (String line : lines) {
                System.out.println(line);
            }

            if (1 == 2) {
                MemberDescription memberDescription = new MemberDescription(system, ifsPath);
                int existingCount = (Integer)memberDescription.getValue(MemberDescription.CURRENT_NUMBER_OF_RECORDS);

                RecordFormat format = IBMiUtils.getRecordFormat(system, library, sourceFile);

                SequentialFile file = new SequentialFile(system, ifsPath);
                file.setRecordFormat(format);
                file.open(AS400File.READ_WRITE, 0, AS400File.COMMIT_LOCK_LEVEL_NONE);

                try {

                    int seqNbr = 0;
                    int newCount = lines.length;
                    int updateCount = Math.min(existingCount, newCount);
                    int currentDate = DateUtils.getSourceLineDate();

                    // Update records that exist in both old and new content
                    if (updateCount > 0) {
                        file.positionCursorToFirst();
                        for (int i = 0; i < updateCount; i++) {
                            seqNbr = seqNbr + 1;
                            Record record = file.readNext();
                            setRecordFields(record, seqNbr, currentDate, lines[i]);
                            file.update(record);
                        }
                    }

                    // Append records beyond the existing count
                    file.positionCursorAfterLast();
                    for (int i = existingCount; i < newCount; i++) {
                        seqNbr = seqNbr + 1;
                        Record record = format.getNewRecord();
                        setRecordFields(record, seqNbr, currentDate, lines[i]);
                        file.write(record);
                    }

                    // Delete surplus existing records from the end
                    for (int i = existingCount; i > newCount; i--) {
                        file.positionCursorToLast();
                        file.deleteCurrentRecord();
                    }
                } finally {
                    file.close();
                }
            }

            return true;

        } catch (Exception e) {
            throw new RpgleFormatterException(
                Messages.bind(Messages.Error_Failed_writing_file_A, library + "/" + sourceFile + "(" + sourceMember + ")"), e);
        }
    }

    private void setRecordFields(Record record, int seqNbr, int currentDate, String line) {
        record.setField("SRCSEQ", new BigDecimal(seqNbr));
        record.setField("SRCDAT", currentDate);
        record.setField("SRCDTA", line);
    }
}