/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.resolvers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.filters.SystemFilterReference;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.handlers.FormatRemoteStreamFileHandler;
import de.tools400.lpex.irpgformatter.utils.FileUtils;

/**
 * Flattens a selection of IFS stream files of a remote system (single files,
 * directories, or filter references) into an array of {@link IRemoteFile}. Used
 * by {@link FormatRemoteStreamFileHandler} for files displayed in the <i>Remote
 * Systems</i> view.
 */
public class RemoteStreamFileResolver extends AbstractRemoteFileResolver {

    private List<IRemoteFile> files;

    public RemoteStreamFileResolver(IProgressMonitor monitor) {
        super(monitor);
        this.files = new LinkedList<>();
    }

    public IRemoteFile[] resolveRemoteStreamFiles(IStructuredSelection selection) {

        Iterator<?> iterator = selection.iterator();
        while (iterator.hasNext() && !isCanceled()) {
            Object element = iterator.next();
            resolveElement(element);
        }

        return files.toArray(new IRemoteFile[files.size()]);
    }

    @Override
    protected void resolveElement(Object element) {

        if (element instanceof IRemoteFile) {
            IRemoteFile remoteFile = (IRemoteFile)element;
            if (remoteFile.isDirectory()) {
                // IRemoteFile (Remote Systems View) -> directory
                addFilesFromDirectory(remoteFile);
            } else {
                // IRemoteFile (Remote Systems View) -> file
                addFileIfSupported(remoteFile);
            }
        } else if (element instanceof SystemFilterReference) {
            resolveFilterReference((SystemFilterReference)element);
        }
    }

    private void addFilesFromDirectory(IRemoteFile directory) {

        try {
            IRemoteFileSubSystem subSystem = directory.getParentRemoteFileSubSystem();
            IRemoteFile[] children = subSystem.list(directory, getMonitor());
            for (IRemoteFile child : children) {
                if (isCanceled()) {
                    return;
                }
                if (child.isDirectory()) {
                    addFilesFromDirectory(child);
                } else {
                    addFileIfSupported(child);
                }
            }
        } catch (Exception e) {
            // Ignore inaccessible directories
            setError(e);
        }
    }

    private void addFileIfSupported(IRemoteFile file) {

        String extension = FileUtils.getExtension(file.getName());
        if (extension != null && RpgleFormatter.isSupportedSourceType(extension.toUpperCase())) {
            files.add(file);
        }
    }
}
