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
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.tools400.lpex.irpgformatter.Messages;

/**
 * Master/detail error dialog: a sortable table of resources (e.g. members)
 * on top, the detail messages of the selected row at the bottom. Suited for
 * batch operations where errors naturally group by resource.
 */
public class MemberErrorsDialog extends TitleAreaDialog {

    private final String headerMessage;
    private final ErrorGroup[] groups;

    private Table memberTable;
    private List detailList;
    private Label detailHeader;

    public MemberErrorsDialog(Shell shell, String headerMessage, ErrorGroup[] groups) {
        super(shell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.headerMessage = headerMessage;
        this.groups = groups;
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
        setMessage(headerMessage, IMessageProvider.ERROR);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite)super.createDialogArea(parent);

        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setLayout(new GridLayout(1, false));

        SashForm sash = new SashForm(container, SWT.VERTICAL);
        GridData sashData = new GridData(SWT.FILL, SWT.FILL, true, true);
        sashData.heightHint = 320;
        sashData.widthHint = 560;
        sash.setLayoutData(sashData);

        createMasterTable(sash);
        createDetailPane(sash);

        sash.setWeights(new int[] { 50, 50 });

        // Pre-select the first row so the detail pane is populated on open.
        if (memberTable.getItemCount() > 0) {
            memberTable.select(0);
            updateDetail(0);
        }

        return area;
    }

    private void createMasterTable(Composite parent) {

        memberTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        memberTable.setHeaderVisible(true);
        memberTable.setLinesVisible(true);

        TableColumn memberColumn = new TableColumn(memberTable, SWT.LEFT);
        memberColumn.setText(Messages.ColumnLabel_Member);
        memberColumn.setWidth(380);

        TableColumn errorsColumn = new TableColumn(memberTable, SWT.RIGHT);
        errorsColumn.setText(Messages.ColumnLabel_Errors);
        errorsColumn.setWidth(80);

        for (ErrorGroup group : groups) {
            TableItem item = new TableItem(memberTable, SWT.NONE);
            item.setText(0, group.getLabel());
            item.setText(1, Integer.toString(group.getDetailCount()));
        }

        memberTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int idx = memberTable.getSelectionIndex();
                if (idx >= 0) {
                    updateDetail(idx);
                }
            }
        });
    }

    private void createDetailPane(Composite parent) {

        Composite detailContainer = new Composite(parent, SWT.NONE);
        detailContainer.setLayout(new GridLayout(1, false));

        detailHeader = new Label(detailContainer, SWT.NONE);
        detailHeader.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        detailList = new List(detailContainer, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        detailList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        addCopySupport(detailList);
    }

    private void updateDetail(int groupIndex) {
        ErrorGroup group = groups[groupIndex];
        detailHeader.setText(Messages.bind(Messages.Label_Errors_in_A, group.getLabel()));
        detailList.removeAll();
        for (String detail : group.getDetails()) {
            detailList.add(detail);
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
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
