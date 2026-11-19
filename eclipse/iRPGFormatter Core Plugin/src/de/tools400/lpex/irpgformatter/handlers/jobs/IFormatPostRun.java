/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.jobs;

public interface IFormatPostRun {

    /**
     * Called at the end of a format job. The problems of the sources are in the
     * view {@link de.tools400.lpex.irpgformatter.views.FormatterProblemsView}.
     *
     * @param formattedCount number of sources whose formatted source was
     *        written back or whose lines stayed the same, so that it was not
     *        written
     * @param notFormattedCount number of sources that could not be formatted at
     *        all (lock, validation, block structure, I/O, ...)
     * @param statementErrorsCount number of formatted sources that nevertheless
     *        contain errors or warnings of statements (original lines kept for
     *        the statements with errors)
     * @param canceled <code>true</code>, if the user canceled the job or
     *        declined to connect, so that not all sources were processed
     */
    public void postRun(int formattedCount, int notFormattedCount, int statementErrorsCount, boolean canceled);
}
