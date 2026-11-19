/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.problems;

import org.eclipse.core.resources.IFile;

/**
 * Base class of the problem sources. It holds the file of the workspace, to
 * which {@link ProblemMarkers} attach the markers: the file itself or the local
 * copy of a member or a stream file of the IFS, that Remote Systems downloaded.
 */
public abstract class AbstractProblemSource implements IProblemSource {

    private IFile markerFile;

    @Override
    public IFile getMarkerFile() {
        return markerFile;
    }

    /**
     * Sets the file of the workspace for the markers, e.g. the local copy of a
     * member, that is known only after it was downloaded.
     *
     * @param markerFile - the file or <code>null</code>
     */
    public void setMarkerFile(IFile markerFile) {
        this.markerFile = markerFile;
    }
}
