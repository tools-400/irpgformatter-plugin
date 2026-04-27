/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import de.tools400.lpex.irpgformatter.Messages;

public class ErrorDetailsMessageDialog extends Dialog {

    private static final int DETAILS_BUTTON_ID = IDialogConstants.CLIENT_ID + 1;

    private String errorMessage;
    private String[] errorDetails;
    private Button detailsButton;
    private List detailsList;
    private boolean listCreated;

    public ErrorDetailsMessageDialog(Shell shell, String errorMessage, String[] errorDetails) {
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
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite)super.createDialogArea(parent);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        composite.setLayout(new GridLayout(2, false));

        Label iconLabel = new Label(composite, SWT.NONE);
        Image errorImage = parent.getDisplay().getSystemImage(SWT.ICON_ERROR);
        iconLabel.setImage(errorImage);
        iconLabel.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

        Label messageLabel = new Label(composite, SWT.WRAP);
        messageLabel.setText(errorMessage);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.widthHint = 400;
        messageLabel.setLayoutData(gd);

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        detailsButton = createButton(parent, DETAILS_BUTTON_ID, IDialogConstants.SHOW_DETAILS_LABEL, false);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == DETAILS_BUTTON_ID) {
            toggleDetailsArea();
        } else {
            super.buttonPressed(buttonId);
        }
    }

    private void toggleDetailsArea() {
        Point windowSize = getShell().getSize();
        Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);

        if (listCreated) {
            detailsList.dispose();
            listCreated = false;
            detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
        } else {
            detailsList = new List((Composite)getContents(), SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
            GridData gd = new GridData(GridData.FILL_BOTH);
            detailsList.setLayoutData(gd);
            for (String detail : errorDetails) {
                detailsList.add(detail);
            }
            addCopySupport(detailsList);
            listCreated = true;
            detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
        }

        Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
    }

    private void addCopySupport(List list) {

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
        selectAllItem.setText("Select All\tCtrl+A");
        selectAllItem.addListener(SWT.Selection, event -> list.selectAll());
        MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
        copyItem.setText("Copy\tCtrl+C");
        copyItem.addListener(SWT.Selection, event -> copySelection(list));
        list.setMenu(menu);
    }

    private void copySelection(List list) {
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
        clipboard.setContents(new Object[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
        clipboard.dispose();
    }
}
