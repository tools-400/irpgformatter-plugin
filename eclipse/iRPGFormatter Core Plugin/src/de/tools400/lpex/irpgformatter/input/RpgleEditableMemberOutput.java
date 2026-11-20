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

import com.ibm.etools.iseries.rse.ui.resources.QSYSEditableRemoteSourceFileMember;
import com.ibm.etools.systems.editor.SystemEditorPlugin;
import com.ibm.etools.systems.editor.preferences.ISystemTextEditorPreferenceConstants;

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
    private final String[] originalRecords;

    /**
     * Creates the output of a member.
     *
     * @param editableMember - the member
     * @param monitor - the monitor of the upload
     * @param originalRecords - the records of the local copy before formatting
     *        or <code>null</code>, if they are not known
     */
    RpgleEditableMemberOutput(QSYSEditableRemoteSourceFileMember editableMember, IProgressMonitor monitor, String[] originalRecords) {
        this.editableMember = editableMember;
        this.monitor = monitor;
        this.originalRecords = originalRecords;
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

            // Write local file with 12-character prefix (SRCSEQ + SRCDAT +
            // SRCDTA)
            StringBuilder content = new StringBuilder();
            for (String record : createRecords().build(originalRecords, result)) {
                content.append(record).append("\n");
            }

            IFile localFile = editableMember.getLocalResource();
            ByteArrayInputStream stream = new ByteArrayInputStream(content.toString().getBytes("UTF-8")); //$NON-NLS-1$
            localFile.setContents(stream, true, false, monitor);

        } catch (Exception e) {
            throw new RpgleFormatterException(Messages.bind(Messages.Error_Failed_writing_file_A, editableMember.getMember().getAbsoluteName()), e);
        }
    }

    /**
     * Creates the builder of the records with the settings <i>Resequence lines
     * at save</i>, <i>Resequence start at</i> and <i>Resequence increment
     * by</i> of the Remote Systems LPEX Editor, that RDi applies, when it saves
     * a member in the editor.
     */
    private static SourceMemberRecords createRecords() {

        IPreferenceStore store = SystemEditorPlugin.getDefault().getPreferenceStore();
        boolean isResequenceAtSave = store.getBoolean(ISystemTextEditorPreferenceConstants.PREF_RESEQ_SAVE);
        int start = store.getInt(ISystemTextEditorPreferenceConstants.PREF_RESEQ_START);
        int increment = store.getInt(ISystemTextEditorPreferenceConstants.PREF_RESEQ_INCR);

        return new SourceMemberRecords(DateUtils.getSourceLineDate(), isResequenceAtSave, start, increment);
    }
}
