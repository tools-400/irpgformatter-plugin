/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.resolvers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Base class of the resolvers. Holds the progress monitor, with which the user
 * cancels the resolving, and the first error, so that the handler can show it,
 * if no item was found.
 */
public abstract class AbstractResolver {

    private final IProgressMonitor monitor;
    private Exception error;

    protected AbstractResolver(IProgressMonitor monitor) {
        this.monitor = monitor != null ? monitor : new NullProgressMonitor();
    }

    protected IProgressMonitor getMonitor() {
        return monitor;
    }

    protected boolean isCanceled() {
        return monitor.isCanceled();
    }

    /**
     * Remembers the first error. The resolver goes on with the next item.
     */
    protected void setError(Exception e) {
        if (error == null) {
            error = e;
        }
    }

    /**
     * Returns the first error that occurred while resolving the items.
     *
     * @return first error or <code>null</code>
     */
    public Exception getError() {
        return error;
    }
}
