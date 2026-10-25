/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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
     * touching call sites. Call {@link #displayErrorDetailsMessageDialog}
     * instead to use the {@link ErrorDetailsMessageDialog}.
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

    /**
     * Adds Ctrl+A, Ctrl+C and a context menu with "Select All" and "Copy" to a
     * list, e.g. the error messages of a dialog.
     */
    public static void addCopySupport(List list) {

        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.stateMask == SWT.CTRL && e.keyCode == 'a') {
                    list.selectAll();
                } else if (e.stateMask == SWT.CTRL && e.keyCode == 'c') {
                    copySelection(list);
                }
            }
        });

        Menu menu = new Menu(list);
        MenuItem selectAllItem = new MenuItem(menu, SWT.PUSH);
        selectAllItem.setText(Messages.MenuItem_Select_All);
        selectAllItem.addListener(SWT.Selection, event -> list.selectAll());
        MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
        copyItem.setText(Messages.MenuItem_Copy);
        copyItem.addListener(SWT.Selection, event -> copySelection(list));
        list.setMenu(menu);
    }

    private static void copySelection(List list) {
        String[] selection = list.getSelection();
        if (selection.length == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selection.length; i++) {
            if (i > 0) {
                sb.append(System.lineSeparator());
            }
            sb.append(selection[i]);
        }
        Clipboard clipboard = new Clipboard(list.getDisplay());
        try {
            clipboard.setContents(new Object[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
        } finally {
            // setContents() throws an SWTError, if another program holds the
            // clipboard.
            clipboard.dispose();
        }
    }
}
