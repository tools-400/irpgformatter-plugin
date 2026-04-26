/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.util.LinkedList;
import java.util.List;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.MemberDescription;
import com.ibm.as400.access.Record;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.SequentialFile;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.utils.IBMiUtils;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

/**
 * {@link IRpgleInput} implementation for IBM i source members via JT400.
 */
public class RpgleRemoteMemberInput extends AbstractRpgleInput implements IRpgleInput {

    private final AS400 system;
    private final String library;
    private final String sourceFile;
    private final String sourceMember;
    private final String path;

    RpgleRemoteMemberInput(AS400 system, String library, String sourceFile, String sourceMember) {
        this.system = system;
        this.library = library;
        this.sourceFile = sourceFile;
        this.sourceMember = sourceMember;
        this.path = IBMiUtils.getMemberPath(library, sourceFile, sourceMember);
    }

    @Override
    public String getFirstSourceLine() throws RpgleFormatterException {

        try {

            RecordFormat recordFormat = IBMiUtils.getRecordFormat(system, library, sourceFile);

            SequentialFile file = new SequentialFile(system, path);
            file.setRecordFormat(recordFormat);
            file.open(AS400File.READ_ONLY, 0, AS400File.COMMIT_LOCK_LEVEL_NONE);

            try {

                String line;

                Record record = file.readFirst();
                if (record != null) {
                    line = StringUtils.trimR(((String)record.getField("SRCDTA")));
                } else {
                    line = "";
                }

                return line;

            } finally {
                file.close();
            }

        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_reading_file_A, getName()), e);
        }
    }

    @Override
    public String[] getSourceLines() throws RpgleFormatterException {

        try {

            RecordFormat recordFormat = IBMiUtils.getRecordFormat(system, library, sourceFile);

            SequentialFile file = new SequentialFile(system, path);

            try {

                file.setRecordFormat(recordFormat);
                file.open(AS400File.READ_ONLY, 0, AS400File.COMMIT_LOCK_LEVEL_NONE);

                List<String> lines = new LinkedList<>();

                String line = null;

                Record record = file.readFirst();
                while (record != null) {
                    line = StringUtils.trimR(((String)record.getField("SRCDTA")));
                    lines.add(line);
                    record = file.readNext();
                }

                return lines.toArray(new String[lines.size()]);

            } finally {
                file.close();
            }

        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_reading_file_A, getName()), e);
        }
    }

    @Override
    public String getName() {
        return library + "/" + sourceFile + "(" + sourceMember + ")";
    }

    public String getSourceType() throws Exception {

        MemberDescription member = new MemberDescription(system, path);
        String sourceType = (String)member.getValue(MemberDescription.SOURCE_TYPE);

        return sourceType;
    }

    @Override
    public IRpgleOutput getOutput() {
        return new RpgleMemberOutput(system, library, sourceFile, sourceMember);
    }
}
