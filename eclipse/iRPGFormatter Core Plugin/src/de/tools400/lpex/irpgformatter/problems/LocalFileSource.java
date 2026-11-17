/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.problems;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;

/**
 * A stream file in the local file system, outside of the workspace.
 */
public class LocalFileSource implements IProblemSource {

    private final File file;

    public LocalFileSource(File file) {
        this.file = file;
    }

    @Override
    public String getKey() {
        return "local:" + file.getAbsolutePath(); //$NON-NLS-1$
    }

    @Override
    public String getLabel() {
        return file.getAbsolutePath();
    }

    @Override
    public IEditorPart open(IWorkbenchPage page) throws Exception {

        IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());

        return IDE.openEditorOnFileStore(page, fileStore);
    }
}
