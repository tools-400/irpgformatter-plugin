/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import java.util.EnumSet;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.filters.SystemFilterReference;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.utils.UIUtils;

public class FormatSourceHandler extends AbstractHandler {

    private FormatRemoteMemberHandler remoteMemberHandler = new FormatRemoteMemberHandler();
    private FormatLocalStreamFileHandler localStreamFileHandler = new FormatLocalStreamFileHandler();
    private FormatJavaIoStreamFileHandler javaIoStreamFileHandler = new FormatJavaIoStreamFileHandler();
    private FormatRemoteStreamFileHandler remoteStreamFileHandler = new FormatRemoteStreamFileHandler();

    private enum Category {
        LOCAL,
        LOCAL_FS,
        IFS_REMOTE,
        QSYS_REMOTE
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }

        IStructuredSelection structuredSelection = (IStructuredSelection)selection;

        EnumSet<Category> categories = EnumSet.noneOf(Category.class);
        Iterator<?> iterator = structuredSelection.iterator();
        while (iterator.hasNext()) {
            categories.add(classify(iterator.next()));
        }

        if (categories.size() > 1) {
            UIUtils.displaySimpleErrorDialog(Messages.Error_Mixed_selection_not_supported);
            return null;
        }

        if (categories.contains(Category.LOCAL)) {
            // local elements of Eclipse projects
            localStreamFileHandler.execute(event);
        } else if (categories.contains(Category.LOCAL_FS)) {
            // local elements of non-Eclipse projects
            javaIoStreamFileHandler.execute(event);
        } else if (categories.contains(Category.IFS_REMOTE)) {
            // remote stream files
            remoteStreamFileHandler.execute(event);
        } else {
            // remote members
            remoteMemberHandler.execute(event);
        }

        return null;
    }

    private Category classify(Object element) {

        if (element instanceof AbstractRemoteFile) {
            AbstractRemoteFile remoteFile = (AbstractRemoteFile)element;
            String configId = remoteFile.getParentRemoteFileSubSystem().getConfigurationId();
            if ("local.files".equals(configId)) { //$NON-NLS-1$
                return Category.LOCAL_FS;
            }
            return Category.IFS_REMOTE;
        }

        if (element instanceof IFile || element instanceof IContainer) {
            return Category.LOCAL;
        }

        if (element instanceof IRemoteFile) {
            return Category.IFS_REMOTE;
        }

        if (element instanceof SystemFilterReference) {
            SystemFilterReference ref = (SystemFilterReference)element;
            Object provider = ref.getFilterPoolReferenceManager().getProvider();
            if (provider instanceof IRemoteFileSubSystem) {
                String configId = ((IRemoteFileSubSystem)provider).getConfigurationId();
                if ("local.files".equals(configId)) { //$NON-NLS-1$
                    return Category.LOCAL_FS;
                }
                return Category.IFS_REMOTE;
            }
            return Category.QSYS_REMOTE;
        }

        return Category.QSYS_REMOTE;
    }
}
