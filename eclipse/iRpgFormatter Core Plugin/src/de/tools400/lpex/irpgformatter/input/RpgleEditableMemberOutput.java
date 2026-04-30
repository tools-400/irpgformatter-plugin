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
import org.eclipse.jface.preference.IPreferenceStore;

import com.ibm.etools.iseries.comm.interfaces.ISeriesHostObjectLock;
import com.ibm.etools.iseries.rse.ui.IBMiRSEPlugin;
import com.ibm.etools.iseries.rse.ui.resources.QSYSEditableRemoteSourceFileMember;
import com.ibm.etools.iseries.services.qsys.api.IQSYSMember;
import com.ibm.etools.iseries.subsystems.qsys.resources.QSYSRemoteMemberTransfer;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FileLockedException;
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

            // Upload local file to remote member
            String localPath = editableMember.getDownloadPath();

            QSYSRemoteMemberTransfer.acquireLock(localPath);

            try {

                String lockMessage = getMemberLockMessage();
                if (lockMessage != null) {
                    // Member is held open by another user/job — abort
                    // before the upload, surfaced via FileLockedException
                    // with the detailed lock message (job/user/number) so
                    // the user can see who's holding the lock.
                    throw new FileLockedException(editableMember.getMember().getAbsoluteName(), lockMessage);
                }

                QSYSRemoteMemberTransfer memberTransfer = new QSYSRemoteMemberTransfer(editableMember.getMember(), localPath);

                IPreferenceStore prefStore = IBMiRSEPlugin.getDefault().getPreferenceStore();
                int startSeqNum = prefStore.getInt("com.ibm.etools.systems.editor.reseq.start");
                int incrSeqNum = prefStore.getInt("com.ibm.etools.systems.editor.reseq.incr");
                boolean insertSequenceNumbersIfRequired = true;

                memberTransfer.upload(insertSequenceNumbersIfRequired, startSeqNum, incrSeqNum);

            } finally {
                QSYSRemoteMemberTransfer.releaseLock(localPath);
            }

            return true;

        } catch (FileLockedException e) {
            // Pass the detailed lock message through unchanged — wrapping
            // it would replace it with the generic "Failed writing file"
            // text and the user would never see who holds the lock.
            throw new RpgleFormatterException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_writing_file_A, editableMember.getMember().getAbsoluteName()), e);
        }
    }

    /**
     * Returns a human-readable description of the lock currently held on the
     * member, or {@code null} if the member is not locked. Errors while
     * querying the lock are logged to the Eclipse error log; the method then
     * returns {@code null} so that the upload may still proceed (failing
     * later with the actual transfer error if appropriate).
     */
    private String getMemberLockMessage() {

        try {

            ISeriesHostObjectLock lock = editableMember.queryLocks();
            if (lock == null) {
                return null;
            }
            IQSYSMember member = editableMember.getMember();
            return Messages.bind(Messages.Error_Member_locked_by_job_A, new Object[] { member.getLibrary(),
                member.getFile(), member.getName(), lock.getJobName(), lock.getJobUser(), lock.getJobNumber() });

        } catch (Exception e) {
            IRpgleFormatterPlugin.logError("Failed to query member lock info.", e);
            return null;
        }
    }
}
