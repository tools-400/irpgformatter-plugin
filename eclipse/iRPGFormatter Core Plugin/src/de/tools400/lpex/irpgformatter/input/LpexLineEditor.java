/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import com.ibm.lpex.core.LpexDocumentLocation;
import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.utils.LpexViewUtils;

/**
 * {@link LineEditor} implementation that delegates to an LPEX view. The line
 * numbers are those of the document: an LPEX view also has show elements, e.g.
 * the messages of the syntax check of RDi, which are elements, but no lines.
 * Each line is mapped to its element when it is accessed, because inserted and
 * deleted lines move the elements behind them.
 */
class LpexLineEditor implements LineEditor {

    public final LpexView view;

    LpexLineEditor(LpexView view) {
        this.view = view;
    }

    @Override
    public String getElementText(int lineNumber) {
        return LpexViewUtils.getLineText(view, lineNumber);
    }

    @Override
    public void setElementText(int lineNumber, String text) {
        LpexViewUtils.setLineText(view, lineNumber, text);
    }

    @Override
    public void addLineAt(int lineNumber) {
        // doCommand with explicit location avoids moving the visible cursor,
        // eliminating screen flicker from cursor jumps during batch updates.
        view.doCommand(new LpexDocumentLocation(view.elementOfLine(lineNumber), 1), "add");
    }

    @Override
    public void deleteLineAt(int lineNumber) {
        view.doCommand(new LpexDocumentLocation(view.elementOfLine(lineNumber), 1), "delete");
    }
}
