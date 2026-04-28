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
import com.ibm.etools.iseries.subsystems.qsys.resources.QSYSRemoteMemberTransfer;

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

        try {

            String[] lines = result.toLines();
            int currentDate = DateUtils.getSourceLineDate();

            // Write local file with 12-character prefix (SRCSEQ + SRCDAT + SRCDTA)
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                String srcSeq = String.format("%06d", (i + 1) * 100);
                String srcDat = String.format("%06d", currentDate);
                content.append(srcSeq).append(srcDat).append(lines[i]).append("\n");
            }

            IFile localFile = editableMember.getLocalResource();
            ByteArrayInputStream stream = new ByteArrayInputStream(
                content.toString().getBytes("UTF-8")); //$NON-NLS-1$
            localFile.setContents(stream, true, false, monitor);

            // Upload local file to remote member
            String localPath = editableMember.getDownloadPath();
            QSYSRemoteMemberTransfer memberTransfer =
                new QSYSRemoteMemberTransfer(editableMember.getMember(), localPath);
            memberTransfer.upload(false, 1, 1);

            return true;

        } catch (Exception e) {
            throw new RpgleFormatterException(
                Messages.bind(Messages.Error_Failed_writing_file_A,
                    editableMember.getMember().getAbsoluteName()), e);
        }
    }
}
