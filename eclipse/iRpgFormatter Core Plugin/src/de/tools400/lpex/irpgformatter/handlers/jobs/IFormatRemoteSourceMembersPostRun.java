/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.jobs;

import de.tools400.lpex.irpgformatter.handlers.SourceMember;
import de.tools400.lpex.irpgformatter.handlers.jobs.FormatRemoteSourceMemberJob.MemberError;
import de.tools400.lpex.irpgformatter.utils.ErrorGroup;

public interface IFormatRemoteSourceMembersPostRun {

    /**
     * @param formatted members whose formatted source was written back
     * @param errors members that could not be written at all (lock,
     *        validation, I/O, ...)
     * @param statementErrors written members that nevertheless contain
     *        individual statement-level errors (original lines kept for
     *        those statements); one group per affected member
     */
    public void postRun(SourceMember[] formatted, MemberError[] errors, ErrorGroup[] statementErrors);
}
