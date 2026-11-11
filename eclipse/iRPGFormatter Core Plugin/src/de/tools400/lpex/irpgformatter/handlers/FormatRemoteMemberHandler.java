/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;

import de.tools400.lpex.irpgformatter.handlers.jobs.FormatRemoteSourceMemberJob;
import de.tools400.lpex.irpgformatter.handlers.resolvers.RemoteMembersResolver;

public class FormatRemoteMemberHandler extends AbstractFormatHandler {

    private SourceMember[] members;

    public FormatRemoteMemberHandler() {
        super(SourceKind.SOURCE_MEMBERS);
    }

    @Override
    public int resolveItems(IStructuredSelection selection, IProgressMonitor monitor) throws ExecutionException {

        RemoteMembersResolver resolver = new RemoteMembersResolver(monitor);
        members = resolver.resolveRemoteMembers(selection);
        checkResolved(members.length, resolver);

        return members.length;
    }

    @Override
    protected void scheduleFormatterJob() {

        FormatRemoteSourceMemberJob job = new FormatRemoteSourceMemberJob(members, this);
        job.schedule();
    }
}
