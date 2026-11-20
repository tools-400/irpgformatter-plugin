/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages.keywordeditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

/**
 * Editing support for the Value column in the keyword TableViewer. Only the
 * Value column is editable; the Key column is read-only.
 */
public class KeywordEditingSupport extends EditingSupport {

    private final TableViewer viewer;
    private final TextCellEditor editor;
    private final Runnable changeListener;

    /**
     * Creates editing support for the value column.
     *
     * @param viewer the table viewer
     * @param changeListener called, when a value was changed
     */
    public KeywordEditingSupport(TableViewer viewer, Runnable changeListener) {
        super(viewer);
        this.viewer = viewer;
        this.editor = new TextCellEditor(viewer.getTable());
        this.changeListener = changeListener;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        return editor;
    }

    @Override
    protected boolean canEdit(Object element) {
        return element instanceof KeywordEntry;
    }

    @Override
    protected Object getValue(Object element) {
        if (element instanceof KeywordEntry) {
            return ((KeywordEntry)element).getValue();
        }
        return "";
    }

    /**
     * Sets the value, if it is the key in another casing. Otherwise the entry
     * keeps its value, see {@link KeywordEntry#isValidValue(String, String)}.
     */
    @Override
    protected void setValue(Object element, Object value) {
        if (element instanceof KeywordEntry && value instanceof String) {
            String newValue = ((String)value).trim();
            KeywordEntry entry = (KeywordEntry)element;
            if (KeywordEntry.isValidValue(entry.getKey(), newValue) && !newValue.equals(entry.getValue())) {
                entry.setValue(newValue);
                viewer.update(element, null);
                changeListener.run();
            }
        }
    }
}
