/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.jobs;

import de.tools400.lpex.irpgformatter.handlers.IErrorObject;
import de.tools400.lpex.irpgformatter.utils.ErrorGroup;

public interface IFormatPostRun {

    /**
     * Called at the end of a format job.
     *
     * @param formattedCount number of sources whose formatted source was
     *        written back
     * @param errors sources that could not be written at all (lock,
     *        validation, I/O, ...)
     * @param statementErrors written sources that nevertheless contain
     *        individual statement-level errors (original lines kept for
     *        those statements); one group per affected source
     */
    public void postRun(int formattedCount, IErrorObject[] errors, ErrorGroup[] statementErrors);
}
