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
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.filters.SystemFilterReference;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.ui.handlers.HandlerUtil;

public class FormatSourceHandler extends AbstractHandler {

    private FormatRemoteMemberHandler remoteSourceHandler = new FormatRemoteMemberHandler();
    private FormatLocalStreamFileHandler localStreamFileHandler = new FormatLocalStreamFileHandler();
    private FormatRemoteStreamFileHandler remoteStreamFileHandler = new FormatRemoteStreamFileHandler();

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (selection instanceof IStructuredSelection) {

            IStructuredSelection structuredSelection = (IStructuredSelection)selection;

            if (isStreamFileSelection(structuredSelection) || isLocalFileSelection(structuredSelection)) {
                localStreamFileHandler.execute(event);
            } else if (isRemoteStreamFileSelection(structuredSelection) || isRemoteStreamFileFilterSelection(structuredSelection)) {
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

        if (first instanceof AbstractRemoteFile) {
            AbstractRemoteFile remoteFile = (AbstractRemoteFile)first;
            String subsystemId = remoteFile.getParentRemoteFileSubSystem().getConfigurationId();
            if ("local.files".equals(subsystemId)) {
                return true;
            }
        }

        return false;
    }

    private boolean isLocalFileSelection(IStructuredSelection selection) {

        Object first = selection.getFirstElement();
        return first instanceof IFile || first instanceof IContainer;
    }

    private boolean isRemoteStreamFileSelection(IStructuredSelection selection) {

        Object first = selection.getFirstElement();
        return first instanceof IRemoteFile;
    }

    private boolean isRemoteStreamFileFilterSelection(IStructuredSelection selection) {

        Object first = selection.getFirstElement();
        if (!(first instanceof SystemFilterReference)) {
            return false;
        }
        SystemFilterReference ref = (SystemFilterReference)first;
        Object provider = ref.getFilterPoolReferenceManager().getProvider();
        if (!(provider instanceof SubSystem)) {
            return false;
        }
        String configId = ((SubSystem)provider).getConfigurationId();
        return "com.ibm.etools.iseries.subsystems.ifs.files.IFSFileServiceSubSystem".equals(configId);
    }
}
