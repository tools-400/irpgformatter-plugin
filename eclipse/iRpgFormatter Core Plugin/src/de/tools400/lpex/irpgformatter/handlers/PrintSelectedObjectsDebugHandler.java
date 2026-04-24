/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.filters.SystemFilterReference;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.etools.iseries.services.qsys.api.IQSYSResource;

public class PrintSelectedObjectsDebugHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;

            System.out.println("=== Selected Objects ===");

            Iterator<?> iterator = structuredSelection.iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Object element = iterator.next();
                count++;
                printObjectInfo(element, count);
            }

            System.out.println("========================");
            System.out.println("Total selected: " + count);
        } else {
            System.out.println("No structured selection available");
        }

        return null;
    }

    private void printObjectInfo(Object element, int index) {

        System.out.println("[" + index + "] Type: " + element.getClass().getName());
        System.out.println("    toString: " + element.toString());

        // Try to get IResource information if available
        IResource resource = null;
        if (element instanceof IResource) {
            resource = (IResource)element;
        } else if (element instanceof IAdaptable) {
            resource = ((IAdaptable)element).getAdapter(IResource.class);
        }

        if (resource != null) {
            System.out.println("    Resource Name: " + resource.getName());
            System.out.println("    Resource Path: " + resource.getFullPath());
            System.out.println("    Resource Type: " + getResourceTypeName(resource.getType()));
            System.out.println("    Location: " + resource.getLocation());
        } else {

            try {

                if ((element instanceof IQSYSResource)) {

                    IQSYSResource qsysResource = (IQSYSResource)element;
                    System.out.println("    Resource Name: " + qsysResource.getFullName());
                    System.out.println("    Resource Desc: " + qsysResource.getDescription());

                } else if ((element instanceof SystemFilterReference)) {

                    SystemFilterReference filterReference = (SystemFilterReference)element;
                    System.out.println("    Resource Name: " + filterReference.getName());
                    System.out.println("    Resource Desc: " + filterReference.getDescription());
                }

            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }

        }
    }

    private String getResourceTypeName(int type) {

        switch (type) {
        case IResource.FILE:
            return "FILE";
        case IResource.FOLDER:
            return "FOLDER";
        case IResource.PROJECT:
            return "PROJECT";
        case IResource.ROOT:
            return "ROOT";
        default:
            return "UNKNOWN (" + type + ")";
        }
    }
}
