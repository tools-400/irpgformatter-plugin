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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import de.tools400.lpex.irpgformatter.Messages;

public class ErrorDetailsTitleAreaDialog extends TitleAreaDialog {

    private String errorMessage;
    private String[] errorDetails;

    public ErrorDetailsTitleAreaDialog(Shell shell, String errorMessage, String[] errorDetails) {
        super(shell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.errorMessage = errorMessage;
        this.errorDetails = errorDetails;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.E_R_R_O_R);
    }

    @Override
    public void create() {
        super.create();
        setTitle(Messages.E_R_R_O_R);
        setMessage(errorMessage, IMessageProvider.ERROR);
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

        return area;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }
}
