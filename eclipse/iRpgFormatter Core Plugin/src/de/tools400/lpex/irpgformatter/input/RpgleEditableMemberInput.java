/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.etools.iseries.rse.ui.resources.QSYSEditableRemoteSourceFileMember;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.utils.StringUtils;

/**
 * {@link IRpgleInput} implementation for IBM i source members via
 * {@link QSYSEditableRemoteSourceFileMember} (RDi bulk download/upload API).
 */
public class RpgleEditableMemberInput extends AbstractRpgleInput implements IRpgleInput {

    private final QSYSEditableRemoteSourceFileMember editableMember;
    private final IProgressMonitor monitor;
    private String[] cachedLines;

    RpgleEditableMemberInput(QSYSEditableRemoteSourceFileMember editableMember, IProgressMonitor monitor) {
        this.editableMember = editableMember;
        this.monitor = monitor;
    }

    private void ensureDownloaded() throws RpgleFormatterException {
        if (cachedLines != null) {
            return;
        }

        try {
            editableMember.download(monitor);
            IFile localFile = editableMember.getLocalResource();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(localFile.getContents(), "UTF-8")); //$NON-NLS-1$

            try {
                List<String> lines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(StringUtils.trimR(line.substring(12))); // strip seq. number and date
                }
                cachedLines = lines.toArray(new String[lines.size()]);
            } finally {
                reader.close();
            }

        } catch (Exception e) {
            throw new RpgleFormatterException(
                Messages.bind(Messages.Error_Failed_reading_file_A, getName()), e);
        }
    }

    @Override
    public String getFirstSourceLine() throws RpgleFormatterException {
        ensureDownloaded();
        return cachedLines.length > 0 ? cachedLines[0] : "";
    }

    @Override
    public String[] getSourceLines() throws RpgleFormatterException {
        ensureDownloaded();
        return cachedLines;
    }

    @Override
    public String getName() {
        return editableMember.getMember().getAbsoluteName();
    }

    @Override
    public String getSourceType() {
        return editableMember.getMember().getType();
    }

    @Override
    public IRpgleOutput getOutput() {
        return new RpgleEditableMemberOutput(editableMember, monitor);
    }
}
