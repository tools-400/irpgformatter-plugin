/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.input;

import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.utils.LpexViewUtils;

/**
 * {@link LineEditor} implementation that delegates to an LPEX view.
 */
class LpexLineEditor implements LineEditor {

    private final LpexView view;

    LpexLineEditor(LpexView view) {
        this.view = view;
    }

    @Override
    public String getElementText(int lineNumber) {
        return LpexViewUtils.getElementText(view, lineNumber);
    }

    @Override
    public void setElementText(int lineNumber, String text) {
        LpexViewUtils.setElementText(view, lineNumber, text);
    }

    @Override
    public void locateElement(int lineNumber) {
        LpexViewUtils.locateElement(view, lineNumber);
    }

    @Override
    public void addLine() {
        LpexViewUtils.addLine(view);
    }

    @Override
    public void deleteLine() {
        LpexViewUtils.deleteLine(view);
    }
}
