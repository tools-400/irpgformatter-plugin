/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;

public class UIUtils {

    private UIUtils() {
        // Utility class
    }

    public static Shell getShell() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }

    public static Display getDisplay() {
        return PlatformUI.getWorkbench().getDisplay();
    }

    public static void displayErrorDialog(String message, String[] errorDetails) {

        MultiStatus multiStatus = new MultiStatus(IRpgleFormatterPlugin.PLUGIN_ID, IStatus.ERROR, message, null);

        for (String detail : errorDetails) {
            multiStatus.add(new Status(IStatus.ERROR, IRpgleFormatterPlugin.PLUGIN_ID, detail));
        }

        ErrorDialog.openError(getShell(), Messages.E_R_R_O_R, message, multiStatus);
    }

    public static void displayErrorDetailsTitleAreaDialog(String message, String[] errorDetails) {
        ErrorDetailsTitleAreaDialog dialog = new ErrorDetailsTitleAreaDialog(getShell(), message, errorDetails);
        dialog.open();
    }

    public static void displayErrorDetailsMessageDialog(String message, String[] errorDetails) {
        ErrorDetailsMessageDialog dialog = new ErrorDetailsMessageDialog(getShell(), message, errorDetails);
        dialog.open();
    }
}
