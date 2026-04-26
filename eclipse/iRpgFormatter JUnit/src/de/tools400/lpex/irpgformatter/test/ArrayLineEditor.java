/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tools400.lpex.irpgformatter.input.LineEditor;

/**
 * In-memory {@link LineEditor} backed by an {@link ArrayList}.
 * Uses 1-based line numbers to match LPEX conventions.
 */
class ArrayLineEditor implements LineEditor {

    private final List<String> lines;
    private int currentLine;

    ArrayLineEditor(String... initialLines) {
        lines = new ArrayList<>(Arrays.asList(initialLines));
        currentLine = 1;
    }

    @Override
    public String getElementText(int lineNumber) {
        return lines.get(lineNumber - 1);
    }

    @Override
    public void setElementText(int lineNumber, String text) {
        lines.set(lineNumber - 1, text);
    }

    @Override
    public void locateElement(int lineNumber) {
        currentLine = lineNumber;
    }

    @Override
    public void addLine() {
        lines.add(currentLine, "");
        currentLine++;
    }

    @Override
    public void deleteLine() {
        lines.remove(currentLine - 1);
    }

    String[] toArray() {
        return lines.toArray(new String[0]);
    }

    int size() {
        return lines.size();
    }
}
