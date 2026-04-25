/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.ibm.lpex.alef.LpexAbstractTextEditor;
import com.ibm.lpex.alef.LpexPreload;
import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.actions.FormatOnSaveListener;
import de.tools400.lpex.irpgformatter.menu.MenuExtension;

public class Preload implements LpexPreload {

    private static final String COMMAND_SAVE = "org.eclipse.ui.file.save"; //$NON-NLS-1$

    public Preload() {
        return;
    }

    public void preload() {

        // MenuExtension menuExtension = MenuExtension.newMainMenuExtension();
        MenuExtension menuExtension = MenuExtension.newStartOfSourceMenuExtension();
        // MenuExtension menuExtension =
        // MenuExtension.newEndOfSourceMenuExtension();
        menuExtension.initializeLpexEditor(IRpgleFormatterPlugin.getDefault());

        registerFormatOnSave();

        return;
    }

    private void registerFormatOnSave() {

        // Delay registration until the workbench is fully initialized.
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                try {
                    installFormatOnSaveListener();
                } catch (Throwable e) {
                    IRpgleFormatterPlugin.logError("Failed to register format-on-save listener", e);
                }
            }
        });
    }

    private void installFormatOnSaveListener() {

        ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
        if (commandService == null) {
            IRpgleFormatterPlugin.logError("ICommandService not available; format-on-save disabled", null);
            return;
        }

        commandService.addExecutionListener(new IExecutionListener() {

            public void preExecute(String commandId, ExecutionEvent event) {
                if (COMMAND_SAVE.equals(commandId)) {
                    formatActiveEditor();
                }
            }

            public void postExecuteSuccess(String commandId, Object returnValue) {
            }

            public void postExecuteFailure(String commandId, ExecutionException exception) {
            }

            public void notHandled(String commandId, NotHandledException exception) {
            }
        });
    }

    private static void formatActiveEditor() {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return;
        }

        IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            return;
        }

        IEditorPart editor = page.getActiveEditor();
        if (!(editor instanceof LpexAbstractTextEditor)) {
            return;
        }

        LpexView view = ((LpexAbstractTextEditor)editor).getLpexView();
        if (view != null) {
            FormatOnSaveListener.formatQuietly(view);
        }
    }
}
