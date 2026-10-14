/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.test.fakes;

import java.util.ArrayList;
import java.util.List;

import de.tools400.lpex.menuextension.logic.ILogger;

/**
 * Collects the messages.
 */
public class FakeLogger implements ILogger {

    private final List<String> errors = new ArrayList<String>();
    private final List<String> warnings = new ArrayList<String>();
    private final List<String> traces = new ArrayList<String>();

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public List<String> getTraces() {
        return traces;
    }

    public void error(String message, Throwable exception) {
        errors.add(message);
    }

    public void warning(String message) {
        warnings.add(message);
    }

    public void trace(String message) {
        traces.add(message);
    }
}
