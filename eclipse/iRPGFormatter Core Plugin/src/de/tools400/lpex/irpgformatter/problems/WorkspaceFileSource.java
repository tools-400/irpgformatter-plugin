/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.problems;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;

/**
 * A stream file in the workspace.
 */
public class WorkspaceFileSource implements IProblemSource {

    private final IFile file;

    public WorkspaceFileSource(IFile file) {
        this.file = file;
    }

    @Override
    public String getKey() {
        return "file:" + file.getFullPath().toString(); //$NON-NLS-1$
    }

    @Override
    public String getLabel() {
        return file.getFullPath().toOSString();
    }

    @Override
    public IEditorPart open(IWorkbenchPage page) throws Exception {
        return IDE.openEditor(page, file, true);
    }
}
