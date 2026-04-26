/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.utils.FileUtils;

public class RemoteStreamFileResolver {

    private List<IRemoteFile> files;

    public RemoteStreamFileResolver() {
        this.files = new LinkedList<>();
    }

    public IRemoteFile[] resolveRemoteStreamFiles(IStructuredSelection selection) {

        Iterator<?> iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            resolveElement(element);
        }

        return files.toArray(new IRemoteFile[files.size()]);
    }

    private void resolveElement(Object element) {

        if (element instanceof IRemoteFile) {
            IRemoteFile remoteFile = (IRemoteFile)element;
            if (remoteFile.isDirectory()) {
                addFilesFromDirectory(remoteFile);
            } else {
                addFileIfSupported(remoteFile);
            }
        }
    }

    private void addFileIfSupported(IRemoteFile file) {

        String extension = FileUtils.getExtension(file.getName());
        if (extension != null && RpgleFormatter.isSupportedSourceType(extension.toUpperCase())) {
            files.add(file);
        }
    }

    private void addFilesFromDirectory(IRemoteFile directory) {

        try {
            IRemoteFileSubSystem subSystem = directory.getParentRemoteFileSubSystem();
            IRemoteFile[] children = subSystem.list(directory, null);
            for (IRemoteFile child : children) {
                if (child.isDirectory()) {
                    addFilesFromDirectory(child);
                } else {
                    addFileIfSupported(child);
                }
            }
        } catch (Exception e) {
            // Ignore inaccessible directories
        }
    }
}
