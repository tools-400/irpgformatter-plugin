/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import de.tools400.lpex.irpgformatter.Messages;

public class ErrorDetailsTitleAreaDialog extends TitleAreaDialog {

    private String title;
    private String errorMessage;
    private int messageType;
    private String[] errorDetails;
    private int[] lineNumbers;
    private int selectedLineNumber;

    /**
     * Creates a dialog with a given title and message type, e.g. for warnings.
     *
     * @param shell - the parent shell
     * @param title - the title of the dialog
     * @param errorMessage - the message above the details
     * @param messageType - the message type, see {@link IMessageProvider}
     * @param errorDetails - the details
     * @param lineNumbers - the line numbers of the details, or
     *        <code>null</code>. With line numbers, a double-click on a detail
     *        closes the dialog and {@link #getSelectedLineNumber()} returns its
     *        line number.
     */
    public ErrorDetailsTitleAreaDialog(Shell shell, String title, String errorMessage, int messageType, String[] errorDetails,
        int[] lineNumbers) {
        super(shell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.title = title;
        this.errorMessage = errorMessage;
        this.messageType = messageType;
        this.errorDetails = errorDetails;
        this.lineNumbers = lineNumbers;
        this.selectedLineNumber = -1;
    }

    /**
     * Returns the line number of the detail, that was double-clicked, or -1, if
     * the dialog was closed otherwise.
     */
    public int getSelectedLineNumber() {
        return selectedLineNumber;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }

    @Override
    public void create() {
        super.create();
        setTitle(title);
        setMessage(errorMessage, messageType);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite)super.createDialogArea(parent);

        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setLayout(new GridLayout(1, false));

        List list = new List(container, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 200;
        gd.widthHint = 400;
        list.setLayoutData(gd);

        for (String detail : errorDetails) {
            list.add(detail);
        }

        UIUtils.addCopySupport(list);

        if (lineNumbers != null) {
            // Double-click or Enter
            list.addListener(SWT.DefaultSelection, event -> goToSelectedLine(list));

            Label hint = new Label(container, SWT.NONE);
            hint.setText(Messages.Label_Double_click_a_message_to_go_to_its_line);
            hint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        }

        return area;
    }

    private void goToSelectedLine(List list) {

        int index = list.getSelectionIndex();
        if (index < 0 || index >= lineNumbers.length || lineNumbers[index] <= 0) {
            return;
        }

        selectedLineNumber = lineNumbers[index];
        okPressed();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }
}
