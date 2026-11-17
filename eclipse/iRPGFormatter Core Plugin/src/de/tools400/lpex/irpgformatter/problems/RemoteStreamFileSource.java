/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.problems;

import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * A stream file in the IFS of an IBM i, opened like with the Remote Systems
 * view.
 */
public class RemoteStreamFileSource implements IProblemSource {

    private final IRemoteFile remoteFile;

    public RemoteStreamFileSource(IRemoteFile remoteFile) {
        this.remoteFile = remoteFile;
    }

    @Override
    public String getKey() {
        return "ifs:" + remoteFile.getHost().getAliasName() + ":" + remoteFile.getAbsolutePath(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public String getLabel() {
        return remoteFile.getAbsolutePath();
    }

    @Override
    public IEditorPart open(IWorkbenchPage page) throws Exception {

        SystemEditableRemoteFile editableFile = new SystemEditableRemoteFile(remoteFile);
        editableFile.open(page.getWorkbenchWindow().getShell());

        IEditorPart editor = editableFile.getEditorPart();
        if (editor != null) {
            return editor;
        }

        return EditorSources.findEditor(page, editableFile.getLocalResource());
    }
}
