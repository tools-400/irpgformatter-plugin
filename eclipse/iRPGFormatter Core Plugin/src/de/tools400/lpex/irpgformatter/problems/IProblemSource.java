/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.problems;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * A source, whose problems are listed in the view
 * {@link de.tools400.lpex.irpgformatter.views.FormatterProblemsView}: a source
 * in an editor, a source member, a stream file in the IFS, in the workspace or
 * in the local file system.
 */
public interface IProblemSource {

    /**
     * Returns the key of the source. Formatting the source again replaces the
     * problems with the same key.
     */
    public String getKey();

    /**
     * Returns the name of the source for the view, e.g. the path of a stream
     * file.
     */
    public String getLabel();

    /**
     * Opens the source in an editor or activates the editor, that shows it.
     *
     * @param page - the workbench page
     * @return the editor or <code>null</code>, if it cannot be found
     * @throws Exception if the source cannot be opened
     */
    public IEditorPart open(IWorkbenchPage page) throws Exception;
}
