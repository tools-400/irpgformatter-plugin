/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.actions.FormatLpexSourceAction;
import de.tools400.lpex.irpgformatter.handlers.AbstractFormatHandler;

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

    /**
     * Application-wide entry point for showing a simple error dialog.
     */
    public static void displaySimpleErrorDialog(String message) {
        MessageDialog.openError(getShell(), Messages.E_R_R_O_R, message);
    }

    /**
     * Application-wide entry point for showing an error message together with a
     * scrollable list of detail entries. All callers that want the standard
     * iRPGFormatter look-and-feel for such dialogs should use this method; the
     * underlying dialog implementation can then be swapped centrally without
     * touching call sites. Call {@link #displayErrorDetailsMessageDialog} instead
     * to use the {@link ErrorDetailsMessageDialog}.
     * <p>
     * Used by the {@link FormatLpexSourceAction}.
     */
    public static void displayErrorDetailsDialog(String message, String[] errorDetails) {
        displayErrorDetailsTitleAreaDialog(message, errorDetails);
    }

    public static void displayErrorDetailsTitleAreaDialog(String message, String[] errorDetails) {
        ErrorDetailsTitleAreaDialog dialog = new ErrorDetailsTitleAreaDialog(getShell(), message, errorDetails);
        dialog.open();
    }

    @SuppressWarnings("unused")
    private static void displayErrorDetailsMessageDialog(String message, String[] errorDetails) {
        ErrorDetailsMessageDialog dialog = new ErrorDetailsMessageDialog(getShell(), message, errorDetails);
        dialog.open();
    }

    /**
     * Application-wide entry point for showing a master/detail error dialog
     * (table of resources on top, error details of the selected row at the
     * bottom). Use when errors naturally group by resource — e.g. one group per
     * source member containing the statement-level errors.
     * <p>
     * Used by all handlers inheriting from {@link AbstractFormatHandler}.
     */
    public static void displayErrorDetailsDialog(String message, ErrorGroup[] errorGroups) {
        MemberErrorsDialog dialog = new MemberErrorsDialog(getShell(), message, errorGroups);
        dialog.open();
    }
}
