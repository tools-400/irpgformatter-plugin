/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.etools.iseries.rse.ui.resources.QSYSEditableRemoteSourceFileMember;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.utils.DateUtils;

/**
 * {@link IRpgleOutput} implementation for IBM i source members via
 * {@link QSYSEditableRemoteSourceFileMember} (RDi bulk download/upload API).
 */
public class RpgleEditableMemberOutput implements IRpgleOutput {

    private final QSYSEditableRemoteSourceFileMember editableMember;
    private final IProgressMonitor monitor;

    RpgleEditableMemberOutput(QSYSEditableRemoteSourceFileMember editableMember, IProgressMonitor monitor) {
        this.editableMember = editableMember;
        this.monitor = monitor;
    }

    @Override
    public boolean writeSourceLines(FormattedResult result) throws RpgleFormatterException {
        updateLocalFile(result);
        return true;
    }

    /**
     * Writes the formatted source lines to the local Eclipse workspace file.
     * <p>
     * The call to {@code IFile.setContents()} fires an Eclipse , which is
     * picked up by RDi's {@code QSYSTempFileListener}. That listener then
     * uploads the local file to the IBM i member automatically — no explicit
     * upload call is needed here.
     *
     * @param result the formatted source to write
     * @throws RpgleFormatterException if the local file cannot be written
     */
    private void updateLocalFile(FormattedResult result) throws RpgleFormatterException {

        try {

            String[] lines = result.toLines();
            int currentDate = DateUtils.getSourceLineDate();

            // Write local file with 12-character prefix (SRCSEQ + SRCDAT +
            // SRCDTA)
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                String srcSeq = String.format("%06d", (i + 1) * 100);
                String srcDat = String.format("%06d", currentDate);
                content.append(srcSeq).append(srcDat).append(lines[i]).append("\n");
            }

            IFile localFile = editableMember.getLocalResource();
            ByteArrayInputStream stream = new ByteArrayInputStream(content.toString().getBytes("UTF-8")); //$NON-NLS-1$
            localFile.setContents(stream, true, false, monitor);

        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_writing_file_A, editableMember.getMember().getAbsoluteName()), e);
        }
    }

}
