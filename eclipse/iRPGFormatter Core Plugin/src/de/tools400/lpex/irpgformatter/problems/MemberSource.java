/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.problems;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

import com.ibm.etools.iseries.rse.ui.resources.QSYSEditableRemoteSourceFileMember;
import com.ibm.etools.iseries.services.qsys.api.IQSYSMember;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.handlers.SourceMember;

/**
 * A source member, opened like with the Remote Systems view. If the connection
 * is offline, it is connected.
 */
public class MemberSource implements IProblemSource {

    private final SourceMember sourceMember;

    public MemberSource(SourceMember sourceMember) {
        this.sourceMember = sourceMember;
    }

    /**
     * Returns the key of the member. The names of library, file and member are
     * compared in upper case, because the editor and Remote Systems may spell
     * them differently.
     */
    @Override
    public String getKey() {
        String memberName = sourceMember.getLibraryName() + "/" + sourceMember.getFileName() + "(" + sourceMember.getMemberName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return "member:" + sourceMember.getProfileName() + "." + sourceMember.getConnectionName() + ":" + memberName.toUpperCase(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public String getLabel() {
        return sourceMember.toString();
    }

    @Override
    public IEditorPart open(IWorkbenchPage page) throws Exception {

        IBMiConnection connection = IBMiConnection.getConnection(sourceMember.getProfileName(), sourceMember.getConnectionName());
        if (connection == null) {
            throw new Exception(Messages.bind(Messages.Error_Connection_A_not_found, sourceMember.getConnectionName()));
        }

        if (!connection.isConnected()) {
            connection.connect();
        }

        IQSYSMember member = connection.getMember(sourceMember.getLibraryName(), sourceMember.getFileName(), sourceMember.getMemberName(),
            new NullProgressMonitor());

        QSYSEditableRemoteSourceFileMember editableMember = new QSYSEditableRemoteSourceFileMember(member);
        editableMember.open(page.getWorkbenchWindow().getShell());

        IEditorPart editor = editableMember.getEditorPart();
        if (editor != null) {
            return editor;
        }

        return EditorSources.findEditor(page, editableMember.getLocalResource());
    }
}
