/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.iagentbridge;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.tools400.iagentbridge.mcp.IMcpTool;
import de.tools400.iagentbridge.mcp.McpToolResult;
import de.tools400.iagentbridge.mcp.ToolArguments;
import de.tools400.lpex.irpgformatter.api.FileFormatResult;
import de.tools400.lpex.irpgformatter.api.FileFormatter;

/**
 * Tool <code>format_rpg</code> of the MCP server of iAgentBridge: formats RPG
 * source files with iRPGFormatter like <i>iFormat</i> of their context menu,
 * with the settings, that apply in RDi.
 */
public class FormatRpgTool implements IMcpTool {

    private static final String ARGUMENT_FILES = "files"; //$NON-NLS-1$

    private static final String DESCRIPTION = "Formats free-form ILE RPG source files (**FREE, extension .rpgle or .sqlrpgle) " //$NON-NLS-1$
        + "with iRPGFormatter of this RDi (Eclipse), like 'iFormat' of their context menu, and saves them. " //$NON-NLS-1$
        + "It uses the iRPGFormatter settings, that apply in RDi: the ones of the project, if the file belongs to an open project " //$NON-NLS-1$
        + "of the RDi workspace with project specific settings, otherwise the ones of the workspace. " //$NON-NLS-1$
        + "Returns for each file the status (formatted, skipped e.g. because it is not **FREE, failed), whether the file changed, " //$NON-NLS-1$
        + "the settings and the problems, that iRPGFormatter reports (severity, line, message). " //$NON-NLS-1$
        + "A statement with an error keeps its original lines. RDi shows the problems in the view 'iRPGFormatter Problems', too. " //$NON-NLS-1$
        + "Files with unsaved changes in an RDi editor are refused. Formats files of the local file system only, " //$NON-NLS-1$
        + "no source members and no files in the IFS of the IBM i."; //$NON-NLS-1$

    private static final String INPUT_SCHEMA = "{\"type\":\"object\",\"properties\":{\"files\":{\"type\":\"array\"," //$NON-NLS-1$
        + "\"items\":{\"type\":\"string\"},\"description\":\"Absolute paths of the RPG source files in the file system, " //$NON-NLS-1$
        + "e.g. C:/workspaces/project/qrpglesrc/pgm.rpgle\"}},\"required\":[\"files\"]}"; //$NON-NLS-1$

    @Override
    public String getName() {
        return "format_rpg"; //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public JsonObject getInputSchema() {
        return new JsonParser().parse(INPUT_SCHEMA).getAsJsonObject();
    }

    @Override
    public McpToolResult execute(JsonObject arguments) throws InterruptedException {

        List<String> paths = ToolArguments.getStringList(arguments, ARGUMENT_FILES);
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        List<Target> targets = new ArrayList<>();
        for (String path : paths) {
            Target target = new Target(path);
            targets.add(target);
            try {
                prepare(root, target);
            } catch (IllegalArgumentException e) {
                target.error = e.getMessage();
            } catch (CoreException e) {
                target.error = e.getMessage();
            } catch (IOException e) {
                target.error = e.getMessage();
            }
        }

        Set<File> dirtyFiles = getFilesOfDirtyEditors();

        FormatRpgReport report = new FormatRpgReport();
        boolean canceled = false;
        for (Target target : targets) {
            if (target.error == null && dirtyFiles.contains(target.localFile)) {
                target.error = "The file has unsaved changes in an RDi editor."; //$NON-NLS-1$
            }
            if (target.error != null) {
                report.addError(target.path, target.error);
            } else if (canceled) {
                report.addCanceled(target.path);
            } else {
                FileFormatResult result = format(target);
                report.addResult(target.path, target.file == null ? null : target.file.getProject().getName(), result);
                canceled = result.getStatus() == FileFormatResult.Status.CANCELED;
            }
        }

        return report.toToolResult(root.getLocation().toOSString());
    }

    /**
     * Finds the file of the workspace, that belongs to a path, and refreshes
     * it.
     */
    private static void prepare(IWorkspaceRoot root, Target target) throws CoreException, IOException {

        File localFile = new File(ToolArguments.toLocalPath(target.path));
        if (!localFile.isFile()) {
            throw new IllegalArgumentException("File not found."); //$NON-NLS-1$
        }

        target.localFile = localFile.getCanonicalFile();

        IFile file = findFile(root, target.localFile);
        if (file == null) {
            return;
        }

        if (FileFormatter.isLocalCopyOfRemoteFile(file)) {
            throw new IllegalArgumentException("The file is the local copy of a source member or of a stream file of the IFS, " //$NON-NLS-1$
                + "that Remote Systems downloaded. Only files of the local file system can be formatted."); //$NON-NLS-1$
        }

        // The file may have been changed outside of RDi.
        file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());

        target.file = file;
    }

    private static IFile findFile(IWorkspaceRoot root, File localFile) {

        for (IFile file : root.findFilesForLocationURI(localFile.toURI())) {
            if (file.getProject().isOpen()) {
                return file;
            }
        }

        return null;
    }

    private static FileFormatResult format(Target target) throws InterruptedException {

        if (target.file != null) {
            return FileFormatter.formatWorkspaceFile(target.file);
        }

        return FileFormatter.formatLocalFile(target.localFile);
    }

    /**
     * Returns the files, that are shown in editors with unsaved changes. The
     * editors are checked in the UI thread.
     */
    private static Set<File> getFilesOfDirtyEditors() {

        final Set<File> files = new HashSet<>();
        if (!PlatformUI.isWorkbenchRunning()) {
            return files;
        }

        final IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
                    for (IWorkbenchPage page : window.getPages()) {
                        for (IEditorReference editor : page.getEditorReferences()) {
                            if (editor.isDirty()) {
                                addFile(files, editor);
                            }
                        }
                    }
                }
            }
        });

        return files;
    }

    private static void addFile(Set<File> files, IEditorReference editor) {

        IEditorInput input;
        try {
            input = editor.getEditorInput();
        } catch (PartInitException e) {
            return;
        }

        File file = getLocalFile(input);
        if (file == null) {
            return;
        }

        try {
            files.add(file.getCanonicalFile());
        } catch (IOException e) {
            files.add(file.getAbsoluteFile());
        }
    }

    /**
     * Returns the file of the file system, that an editor shows.
     *
     * @return the file or <code>null</code>, e.g. for a source member
     */
    private static File getLocalFile(IEditorInput input) {

        IFile file = input.getAdapter(IFile.class);
        if (file != null) {
            IPath location = file.getLocation();
            return location == null ? null : location.toFile();
        }

        if (input instanceof IURIEditorInput) {
            URI uri = ((IURIEditorInput)input).getURI();
            if (uri != null && "file".equals(uri.getScheme())) { //$NON-NLS-1$
                return new File(uri);
            }
        }

        if (input instanceof IPathEditorInput) {
            return ((IPathEditorInput)input).getPath().toFile();
        }

        return null;
    }

    /**
     * A file passed by the client.
     */
    private static class Target {

        private final String path;
        private File localFile;
        private IFile file;
        private String error;

        public Target(String path) {
            this.path = path;
        }
    }
}
