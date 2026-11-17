/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.problems;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.lpex.alef.LpexAbstractTextEditor;

/**
 * Creates the markers of iRPGFormatter again, when an LPEX editor opens a file,
 * that has some, see {@link ProblemMarkers#refresh(IFile)}. Otherwise the
 * editor would not show them in its ruler.
 */
public final class EditorMarkerRefresher implements IWindowListener, IPartListener2 {

    private EditorMarkerRefresher() {
    }

    /**
     * Listens to the editors of all workbench windows and refreshes the markers
     * of the LPEX editors, that are already open. Must be called in the UI
     * thread.
     */
    public static void install() {

        EditorMarkerRefresher refresher = new EditorMarkerRefresher();

        IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.addWindowListener(refresher);

        for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
            refresher.windowOpened(window);
            for (IWorkbenchPage page : window.getPages()) {
                for (IEditorReference reference : page.getEditorReferences()) {
                    refresh(reference.getEditor(false));
                }
            }
        }
    }

    private static void refresh(IEditorPart editor) {

        if (!(editor instanceof LpexAbstractTextEditor)) {
            return;
        }

        IFile file = (IFile)editor.getEditorInput().getAdapter(IFile.class);
        if (file != null) {
            ProblemMarkers.refresh(file);
        }
    }

    @Override
    public void partOpened(IWorkbenchPartReference partReference) {
        if (partReference instanceof IEditorReference) {
            refresh(((IEditorReference)partReference).getEditor(false));
        }
    }

    @Override
    public void windowOpened(IWorkbenchWindow window) {
        window.getPartService().addPartListener(this);
    }

    @Override
    public void windowClosed(IWorkbenchWindow window) {
        window.getPartService().removePartListener(this);
    }

    @Override
    public void windowActivated(IWorkbenchWindow window) {
    }

    @Override
    public void windowDeactivated(IWorkbenchWindow window) {
    }

    @Override
    public void partActivated(IWorkbenchPartReference partReference) {
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partReference) {
    }

    @Override
    public void partClosed(IWorkbenchPartReference partReference) {
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partReference) {
    }

    @Override
    public void partHidden(IWorkbenchPartReference partReference) {
    }

    @Override
    public void partVisible(IWorkbenchPartReference partReference) {
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partReference) {
    }
}
