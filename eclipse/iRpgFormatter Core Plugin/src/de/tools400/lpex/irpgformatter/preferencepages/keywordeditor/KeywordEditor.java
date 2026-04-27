/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages.keywordeditor;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import de.tools400.lpex.irpgformatter.Messages;

/**
 * Self-contained keyword editor widget that combines a TableViewer with
 * Add/Change/Remove buttons. Used on the preference page tabs for Data Types,
 * Declaration Types, Keywords and Special Words.
 */
public class KeywordEditor {

    private Composite control;
    private TableViewer tableViewer;
    private List<KeywordEntry> entries;
    private Button addButton;
    private Button changeButton;
    private Button removeButton;

    private final String addDialogTitle;
    private final String changeDialogTitle;
    private final String duplicateDialogTitle;
    private final String duplicateErrorMessage;
    private final boolean isSpecialWord;

    private KeywordEditor(String addDialogTitle, String changeDialogTitle, String duplicateDialogTitle, String duplicateErrorMessage,
        boolean isSpecialWord) {
        this.addDialogTitle = addDialogTitle;
        this.changeDialogTitle = changeDialogTitle;
        this.duplicateDialogTitle = duplicateDialogTitle;
        this.duplicateErrorMessage = duplicateErrorMessage;
        this.isSpecialWord = isSpecialWord;
    }

    /**
     * Creates a keyword editor for data types.
     *
     * @param parent the parent composite (typically a TabFolder)
     */
    public static KeywordEditor forDataTypes(Composite parent) {
        KeywordEditor editor = new KeywordEditor(Messages.Title_Add_Data_Type, Messages.Title_Change_Data_Type, Messages.Title_Duplicate_Data_Type,
            Messages.Error_A_data_type_with_key_A_already_exists, false);
        editor.createControl(parent);
        return editor;
    }

    /**
     * Creates a keyword editor for declaration types.
     *
     * @param parent the parent composite (typically a TabFolder)
     */
    public static KeywordEditor forDeclarationTypes(Composite parent) {
        KeywordEditor editor = new KeywordEditor(Messages.Title_Add_Declaration_Type, Messages.Title_Change_Declaration_Type,
            Messages.Title_Duplicate_Declaration_Type, Messages.Error_A_declaration_type_with_key_A_already_exists, false);
        editor.createControl(parent);
        return editor;
    }

    /**
     * Creates a keyword editor for keywords.
     *
     * @param parent the parent composite (typically a TabFolder)
     */
    public static KeywordEditor forKeywords(Composite parent) {
        KeywordEditor editor = new KeywordEditor(Messages.Title_Add_Keyword, Messages.Title_Change_Keyword, Messages.Title_Duplicate_Keyword,
            Messages.Error_A_keyword_with_key_A_already_exists, false);
        editor.createControl(parent);
        return editor;
    }

    /**
     * Creates a keyword editor for special words (keyword parameters starting
     * with *).
     *
     * @param parent the parent composite (typically a TabFolder)
     */
    public static KeywordEditor forSpecialWords(Composite parent) {
        KeywordEditor editor = new KeywordEditor(Messages.Title_Add_Special_Word, Messages.Title_Change_Special_Word,
            Messages.Title_Duplicate_Special_Word, Messages.Error_A_special_word_with_key_A_already_exists, true);
        editor.createControl(parent);
        return editor;
    }

    /**
     * Returns the top-level control of this editor.
     *
     * @return the control
     */
    public Composite getControl() {
        return control;
    }

    private void createControl(Composite parent) {

        control = new Composite(parent, SWT.NONE);
        control.setLayout(new GridLayout(2, false));

        // Create table viewer
        tableViewer = createKeywordTableViewer(control);
        GridData tableData = new GridData(GridData.FILL_BOTH);
        tableData.heightHint = 200;
        tableViewer.getTable().setLayoutData(tableData);

        // Button panel
        Composite buttonPanel = new Composite(control, SWT.NONE);
        buttonPanel.setLayout(new GridLayout(1, false));
        buttonPanel.setLayoutData(new GridData());

        // Add button
        addButton = new Button(buttonPanel, SWT.PUSH);
        addButton.setText(Messages.Label_Add);
        addButton.setLayoutData(new GridData());
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addEntry();
            }
        });

        // Change button
        changeButton = new Button(buttonPanel, SWT.PUSH);
        changeButton.setText(Messages.Label_Change);
        changeButton.setLayoutData(new GridData());
        changeButton.setEnabled(false);
        changeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                changeSelectedEntry();
            }
        });

        // Remove button
        removeButton = new Button(buttonPanel, SWT.PUSH);
        removeButton.setText(Messages.Label_Remove);
        removeButton.setLayoutData(new GridData());
        removeButton.setEnabled(false);
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeSelectedEntry();
            }
        });

        // Enable/disable change and remove buttons based on selection
        tableViewer.addSelectionChangedListener(event -> {
            boolean hasSelection = !event.getSelection().isEmpty();
            changeButton.setEnabled(hasSelection);
            removeButton.setEnabled(hasSelection);
        });
    }

    /**
     * Loads entries into this editor, replacing any previous content.
     *
     * @param entries the keyword entries to display
     */
    public void load(List<KeywordEntry> entries) {
        this.entries = entries;
        tableViewer.setInput(entries);
    }

    /**
     * Returns the current list of keyword entries.
     *
     * @return the entries (may be {@code null} before {@link #load} is called)
     */
    public List<KeywordEntry> getEntries() {
        return entries;
    }

    private TableViewer createKeywordTableViewer(Composite parent) {
        TableViewer viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);

        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // Key column (read-only)
        TableViewerColumn keyColumn = new TableViewerColumn(viewer, SWT.NONE);
        keyColumn.getColumn().setText(Messages.ColumnLabel_Key_read_only);
        keyColumn.getColumn().setWidth(150);
        keyColumn.getColumn().setResizable(true);
        // No editing support - column is read-only

        // Value column (editable)
        TableViewerColumn valueColumn = new TableViewerColumn(viewer, SWT.NONE);
        valueColumn.getColumn().setText(Messages.ColumnLabel_Value_editable);
        valueColumn.getColumn().setWidth(150);
        valueColumn.getColumn().setResizable(true);
        valueColumn.setEditingSupport(new KeywordEditingSupport(viewer));

        // Set providers
        viewer.setContentProvider(new KeywordContentProvider());
        viewer.setLabelProvider(new KeywordLabelProvider());

        // Enable tooltips
        ColumnViewerToolTipSupport.enableFor(viewer);

        // Add sorting by key column
        TableColumn keyCol = keyColumn.getColumn();
        keyCol.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Toggle sort direction
                int direction = table.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
                table.setSortDirection(direction);
                table.setSortColumn(keyCol);
                viewer.refresh();
            }
        });

        return viewer;
    }

    private void addEntry() {
        AddKeywordDialog dialog = new AddKeywordDialog(addButton.getShell(), addDialogTitle, isSpecialWord);
        if (dialog.open() == Window.OK) {
            String key = dialog.getKey();
            String value = dialog.getValue();

            // Check for duplicate
            for (KeywordEntry entry : entries) {
                if (entry.getKey().equalsIgnoreCase(key)) {
                    MessageDialog.openWarning(addButton.getShell(), duplicateDialogTitle, Messages.bind(duplicateErrorMessage, key));
                    return;
                }
            }

            // Add new entry
            KeywordEntry newEntry = new KeywordEntry(key, value);
            entries.add(newEntry);
            entries.sort((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()));
            tableViewer.refresh();
        }
    }

    private void changeSelectedEntry() {
        IStructuredSelection selection = tableViewer.getStructuredSelection();
        if (!selection.isEmpty()) {
            KeywordEntry entry = (KeywordEntry)selection.getFirstElement();
            AddKeywordDialog dialog = new AddKeywordDialog(changeButton.getShell(), changeDialogTitle, isSpecialWord, entry.getKey(),
                entry.getValue());
            if (dialog.open() == Window.OK) {
                String newKey = dialog.getKey();
                String newValue = dialog.getValue();

                // Check for duplicate (only if key changed)
                if (!newKey.equalsIgnoreCase(entry.getKey())) {
                    for (KeywordEntry existing : entries) {
                        if (existing.getKey().equalsIgnoreCase(newKey)) {
                            MessageDialog.openWarning(changeButton.getShell(), duplicateDialogTitle, Messages.bind(duplicateErrorMessage, newKey));
                            return;
                        }
                    }
                }

                // Replace entry
                int index = entries.indexOf(entry);
                entries.set(index, new KeywordEntry(newKey, newValue));
                entries.sort((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()));
                tableViewer.refresh();
            }
        }
    }

    private void removeSelectedEntry() {
        IStructuredSelection selection = tableViewer.getStructuredSelection();
        if (!selection.isEmpty()) {
            KeywordEntry entry = (KeywordEntry)selection.getFirstElement();
            entries.remove(entry);
            tableViewer.refresh();
        }
    }
}
