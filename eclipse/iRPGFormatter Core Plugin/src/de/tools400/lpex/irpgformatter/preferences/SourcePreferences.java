/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferences;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.problems.EditorSources;

/**
 * Finds the preferences, that apply to a source: the preferences of its
 * project, if the project has settings of its own, otherwise the preferences of
 * the workspace.
 * <p>
 * Only a file of a project of the workspace has a project. A member or a stream
 * file of the IFS, that Remote Systems downloaded into a local copy, a file of
 * the local file system and a source, whose editor cannot be found, are
 * formatted with the preferences of the workspace.
 * </p>
 */
public final class SourcePreferences {

    private SourcePreferences() {
        // Utility class
    }

    /**
     * Returns the preferences of the source, that the given editor view shows.
     *
     * @param view - the view of the LPEX editor
     * @return the preferences of the project of the source or of the workspace
     */
    public static Preferences get(LpexView view) {

        IEditorPart editor = EditorSources.findEditor(view);
        if (editor == null) {
            return Preferences.getInstance();
        }

        return get(editor.getEditorInput().getAdapter(IFile.class));
    }

    /**
     * Returns the preferences of the given file.
     *
     * @param file - the file or <code>null</code>
     * @return the preferences of the project of the file or of the workspace
     */
    public static Preferences get(IFile file) {

        IProject project = getProject(file);
        if (project == null) {
            return Preferences.getInstance();
        }

        Preferences projectPreferences = getProjectPreferences(project);
        if (!projectPreferences.isProjectSpecificSettings()) {
            return Preferences.getInstance();
        }

        return projectPreferences;
    }

    /**
     * Returns the preferences of a project, whether the project has settings of
     * its own or not. They are stored in the file
     * <code>.settings/de.tools400.lpex.irpgformatter.core.plugin.prefs</code>
     * of the project.
     *
     * @param project - an open project
     * @return the preferences of the project
     */
    public static Preferences getProjectPreferences(IProject project) {
        return Preferences.forProject(new ScopedPreferenceStore(new ProjectScope(project), IRpgleFormatterPlugin.PLUGIN_ID));
    }

    /**
     * Returns the project of a file, unless the file is the local copy of a
     * member or of a stream file of the IFS, that Remote Systems downloaded
     * into the project <i>RemoteSystemsTempFiles</i>.
     *
     * @param file - the file or <code>null</code>
     * @return the open project or <code>null</code>
     */
    private static IProject getProject(IFile file) {

        if (file == null || isLocalCopyOfRemoteFile(file)) {
            return null;
        }

        IProject project = file.getProject();
        if (project == null || !project.isOpen()) {
            return null;
        }

        return project;
    }

    /**
     * Returns <code>true</code>, if the file is the local copy of a remote
     * file, the same test as {@link EditorSources} uses.
     */
    private static boolean isLocalCopyOfRemoteFile(IFile file) {

        SystemIFileProperties properties = new SystemIFileProperties(file);

        return properties.getRemoteFileObject() != null || properties.getRemoteFilePath() != null;
    }
}
