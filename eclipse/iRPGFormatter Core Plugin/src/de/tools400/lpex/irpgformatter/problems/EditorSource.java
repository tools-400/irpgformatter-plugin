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

import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.utils.StringUtils;

/**
 * A source, that was formatted in an LPEX editor. Its problems are removed,
 * when the editor is closed, see {@link EditorSources}.
 */
public class EditorSource extends AbstractProblemSource {

    private final LpexView view;
    private final String label;

    EditorSource(LpexView view) {
        this.view = view;
        this.label = getLabel(view);
    }

    public LpexView getView() {
        return view;
    }

    @Override
    public String getKey() {
        return "editor:" + System.identityHashCode(view); //$NON-NLS-1$
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public IEditorPart open(IWorkbenchPage page) throws Exception {

        IEditorPart editor = EditorSources.findEditor(view);
        if (editor != null) {
            editor.getSite().getPage().activate(editor);
        }

        return editor;
    }

    private static String getLabel(LpexView view) {

        IEditorPart editor = EditorSources.findEditor(view);
        if (editor != null) {
            if (!StringUtils.isNullOrEmpty(editor.getTitleToolTip())) {
                return editor.getTitleToolTip();
            }
            return editor.getTitle();
        }

        return view.query("name"); //$NON-NLS-1$
    }
}
