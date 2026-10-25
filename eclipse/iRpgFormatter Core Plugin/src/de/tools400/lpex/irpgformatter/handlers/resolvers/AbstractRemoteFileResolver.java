/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.resolvers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.filters.SystemFilterReference;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

/**
 * Base class of the resolvers of the RSE file subsystems: <i>Local Files</i>
 * and the IFS of a remote system. Resolves a filter into its files and
 * directories.
 */
public abstract class AbstractRemoteFileResolver extends AbstractResolver {

    protected AbstractRemoteFileResolver(IProgressMonitor monitor) {
        super(monitor);
    }

    /**
     * Resolves a selected element, or a file or directory of a filter.
     */
    protected abstract void resolveElement(Object element);

    /**
     * Passes the files and directories of the filter to
     * {@link #resolveElement(Object)}. An error is remembered and ends this
     * filter; the resolver goes on with the next selected element.
     */
    protected void resolveFilterReference(SystemFilterReference ref) {

        try {
            Object provider = ref.getFilterPoolReferenceManager().getProvider();
            if (!(provider instanceof FileServiceSubSystem)) {
                return;
            }
            FileServiceSubSystem subSystem = (FileServiceSubSystem)provider;
            String[] filterStrings = ref.getReferencedFilter().getFilterStrings();
            for (String filterString : filterStrings) {
                if (isCanceled()) {
                    return;
                }
                Object[] children = subSystem.resolveFilterString(filterString, getMonitor());
                if (children == null) {
                    continue;
                }
                for (Object child : children) {
                    if (child instanceof IRemoteFile) {
                        resolveElement(child);
                    }
                }
            }
        } catch (Exception e) {
            setError(e);
        }
    }
}
