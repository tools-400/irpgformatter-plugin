/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.resolvers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;

public class LocalStreamFileResolver {

    private List<IFile> files;
    private Set<Object> unsupportedResources;

    public LocalStreamFileResolver() {
        this.files = new LinkedList<>();
        this.unsupportedResources = new HashSet<Object>();
    }

    public IFile[] resolveLocalStreamFiles(IStructuredSelection selection) {

        Iterator<?> iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            resolveElement(element);
        }

        return files.toArray(new IFile[files.size()]);
    }

    private void resolveElement(Object element) {

        if (element instanceof IFile) {
            // IFile (Project Explorer)
            addFileIfSupported((IFile)element);
        } else if (element instanceof IFolder) {
            // IFolder (Project Explorer)
            addFilesFromContainer((IFolder)element);
        } else if (element instanceof IAdaptable) {
            IResource resource = ((IAdaptable)element).getAdapter(IResource.class);
            if (resource instanceof IFile) {
                // IAdaptable -> IFile
                addFileIfSupported((IFile)resource);
            } else if (resource instanceof IFolder) {
                // IAdaptable -> IFolder
                addFilesFromContainer((IFolder)resource);
            }
        } else {
            if (!unsupportedResources.contains(element)) {
                IRpgleFormatterPlugin.logWarning("Unsupported local file object type: " + element.getClass().getName());
                unsupportedResources.add(element);
            }
        }
    }

    private void addFilesFromContainer(IContainer container) {

        try {
            IResource[] members = container.members();
            for (IResource member : members) {
                if (member instanceof IFile) {
                    addFileIfSupported((IFile)member);
                } else if (member instanceof IContainer) {
                    addFilesFromContainer((IContainer)member);
                }
            }
        } catch (CoreException e) {
            // Ignore inaccessible resources
        }
    }

    private void addFileIfSupported(IFile file) {

        String extension = file.getFileExtension();
        if (extension != null && RpgleFormatter.isSupportedSourceType(extension.toUpperCase())) {
            files.add(file);
        }
    }
}
