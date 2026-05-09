/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.resolvers;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.filters.SystemFilterReference;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.handlers.FormatJavaIoStreamFileHandler;
import de.tools400.lpex.irpgformatter.utils.FileUtils;

/**
 * Flattens an RSE Local Files selection (single files, directories, or filter
 * references) into an array of {@link File} pointing at the underlying disk
 * paths. Used by {@link FormatJavaIoStreamFileHandler} for files that are not
 * part of any Eclipse workspace project.
 */
public class JavaIoStreamFileResolver {

    private List<File> files;

    public JavaIoStreamFileResolver() {
        this.files = new LinkedList<>();
    }

    public File[] resolveStreamFiles(IStructuredSelection selection) {

        Iterator<?> iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            resolveElement(element);
        }

        return files.toArray(new File[files.size()]);
    }

    private void resolveElement(Object element) {

        if (element instanceof AbstractRemoteFile) {
            AbstractRemoteFile remoteFile = (AbstractRemoteFile)element;
            File javaFile = new File(remoteFile.getHostFile().getAbsolutePath());
            if (remoteFile.isDirectory()) {
                // AbstractRemoteFile (Remote Systems View) -> directory
                addFilesFromDirectory(javaFile);
            } else {
                // AbstractRemoteFile (Remote Systems View) -> file
                addFileIfSupported(javaFile);
            }
        } else if (element instanceof SystemFilterReference) {
            resolveFilterReference((SystemFilterReference)element);
        }
    }

    private void resolveFilterReference(SystemFilterReference ref) {

        try {
            Object provider = ref.getFilterPoolReferenceManager().getProvider();
            if (!(provider instanceof FileServiceSubSystem)) {
                return;
            }
            FileServiceSubSystem subSystem = (FileServiceSubSystem)provider;
            String[] filterStrings = ref.getReferencedFilter().getFilterStrings();
            for (String filterString : filterStrings) {
                Object[] children = subSystem.resolveFilterString(filterString, null);
                if (children == null) {
                    continue;
                }
                for (Object child : children) {
                    if (child instanceof IRemoteFile) {
                        resolveElement(child);
                    }
                }
            }
        } catch (Exception e) {
            // Ignore inaccessible filters
        }
    }

    private void addFilesFromDirectory(File directory) {

        File[] children = directory.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                addFilesFromDirectory(child);
            } else {
                addFileIfSupported(child);
            }
        }
    }

    private void addFileIfSupported(File file) {

        String extension = FileUtils.getExtension(file.getName());
        if (extension != null && RpgleFormatter.isSupportedSourceType(extension.toUpperCase())) {
            files.add(file);
        }
    }
}
