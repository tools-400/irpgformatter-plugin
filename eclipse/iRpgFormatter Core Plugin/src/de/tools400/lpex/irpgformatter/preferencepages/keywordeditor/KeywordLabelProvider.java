/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages.keywordeditor;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for the keyword TableViewer. Provides labels for the Key
 * (column 0) and Value (column 1) columns.
 */
public class KeywordLabelProvider extends LabelProvider implements ITableLabelProvider {

    /** Column index for the key */
    public static final int COL_KEY = 0;

    /** Column index for the value */
    public static final int COL_VALUE = 1;

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof KeywordEntry) {
            KeywordEntry entry = (KeywordEntry)element;
            switch (columnIndex) {
            case COL_KEY:
                return entry.getKey();
            case COL_VALUE:
                return entry.getValue();
            default:
                return "";
            }
        }
        return "";
    }
}
