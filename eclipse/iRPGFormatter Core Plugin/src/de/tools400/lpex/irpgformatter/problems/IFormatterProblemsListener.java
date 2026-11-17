/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.problems;

/**
 * Listener of {@link FormatterProblems}.
 */
public interface IFormatterProblemsListener {

    /**
     * Called, when problems were added, replaced or removed. It may be called
     * from any thread, e.g. from the job, that formats several sources.
     */
    public void problemsChanged();
}
