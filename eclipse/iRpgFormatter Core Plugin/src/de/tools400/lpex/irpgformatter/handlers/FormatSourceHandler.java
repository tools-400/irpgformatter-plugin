/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.ui.handlers.HandlerUtil;

public class FormatSourceHandler extends AbstractHandler {

    private FormatRemoteSourceHandler remoteSourceHandler = new FormatRemoteSourceHandler();
    private FormatStreamFileHandler streamFileHandler = new FormatStreamFileHandler();
    private FormatRemoteStreamFileHandler remoteStreamFileHandler = new FormatRemoteStreamFileHandler();

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (selection instanceof IStructuredSelection) {

            IStructuredSelection structuredSelection = (IStructuredSelection)selection;

            if (isStreamFileSelection(structuredSelection)) {
                streamFileHandler.execute(event);
            } else if (isRemoteStreamFileSelection(structuredSelection)) {
                remoteStreamFileHandler.execute(event);
            } else {
                remoteSourceHandler.execute(event);
            }
        }

        return null;
    }

    private boolean isStreamFileSelection(IStructuredSelection selection) {

        Object first = selection.getFirstElement();

        if (first == null) {
            return false;
        }

        if (first instanceof IResource) {
            return true;
        }

        if (first instanceof IAdaptable) {
            return ((IAdaptable)first).getAdapter(IResource.class) != null;
        }

        return false;
    }

    private boolean isRemoteStreamFileSelection(IStructuredSelection selection) {

        Object first = selection.getFirstElement();
        return first instanceof IRemoteFile;
    }
}
