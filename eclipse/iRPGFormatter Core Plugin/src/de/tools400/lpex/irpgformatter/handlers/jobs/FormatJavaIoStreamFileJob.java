/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers.jobs;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.FileLockedException;
import de.tools400.lpex.irpgformatter.input.IRpgleInput;
import de.tools400.lpex.irpgformatter.input.RpgleInputFactory;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.preferences.SourcePreferences;
import de.tools400.lpex.irpgformatter.problems.IProblemSource;
import de.tools400.lpex.irpgformatter.problems.LocalFileSource;
import de.tools400.lpex.irpgformatter.problems.WorkspaceFileSource;

/**
 * Formats files of the local file system, e.g. files, that the user selected in
 * the <i>Remote Systems</i> view under <i>Local Files</i>. A file, that is a
 * file of the workspace, too, is formatted like a file of the workspace: with
 * its character set and the preferences of its project, and so that the
 * workspace sees the change.
 */
public class FormatJavaIoStreamFileJob extends AbstractFormatJob<File> {

    /**
     * The files of the workspace by the files of the local file system; the
     * value is <code>null</code> for a file outside of the workspace.
     */
    private final Map<File, IFile> workspaceFiles = new HashMap<>();

    public FormatJavaIoStreamFileJob(File[] files, IFormatPostRun postRun) {
        super(Messages.Job_Formatting_stream_files, files, postRun);
    }

    @Override
    protected String getPath(File file) {
        return file.getAbsolutePath();
    }

    @Override
    protected IProblemSource createProblemSource(File file) {

        IFile workspaceFile = getWorkspaceFile(file);
        if (workspaceFile != null) {
            return new WorkspaceFileSource(workspaceFile);
        }

        return new LocalFileSource(file);
    }

    @Override
    protected void checkAvailability(File file) throws FileLockedException {

        if (file.exists() && !file.canWrite()) {
            throw new FileLockedException(file.getAbsolutePath());
        }
    }

    @Override
    protected IRpgleInput createInput(File file, IProgressMonitor monitor) throws Exception {

        IFile workspaceFile = getWorkspaceFile(file);
        if (workspaceFile != null) {
            // The file may have been created or changed outside of the
            // workspace.
            workspaceFile.refreshLocal(IResource.DEPTH_ZERO, monitor);
            return RpgleInputFactory.createFromStreamFile(workspaceFile);
        }

        // A file outside of the workspace has no character set of its own:
        // a file, that is not UTF-8, is read with the encoding of the
        // workspace, if it can read the file.
        return RpgleInputFactory.createFromJavaIoFile(file, Charset.forName(ResourcesPlugin.getEncoding()));
    }

    @Override
    protected Preferences getPreferences(File file) {

        IFile workspaceFile = getWorkspaceFile(file);
        if (workspaceFile != null) {
            return SourcePreferences.get(workspaceFile);
        }

        return super.getPreferences(file);
    }

    /**
     * Returns the file of the workspace, that is the given file, or
     * <code>null</code>, if the file is not in an open project of the
     * workspace. The file need not exist in the workspace yet: a new file, that
     * was created outside of the workspace, is added to it by the refresh in
     * {@link #createInput(File, IProgressMonitor)}.
     */
    private IFile getWorkspaceFile(File file) {

        if (!workspaceFiles.containsKey(file)) {
            workspaceFiles.put(file, findWorkspaceFile(file));
        }

        return workspaceFiles.get(file);
    }

    private static IFile findWorkspaceFile(File file) {

        for (IFile workspaceFile : ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.toURI())) {
            if (workspaceFile.getProject().isAccessible()) {
                return workspaceFile;
            }
        }

        return null;
    }
}
