/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import de.tools400.lpex.irpgformatter.handlers.jobs.FormatJavaIoStreamFileJob;
import de.tools400.lpex.irpgformatter.handlers.jobs.FormatStreamFileJob;
import de.tools400.lpex.irpgformatter.handlers.jobs.IFormatPostRun;
import de.tools400.lpex.irpgformatter.preferences.SourcePreferences;
import de.tools400.lpex.irpgformatter.problems.FormatterProblem;
import de.tools400.lpex.irpgformatter.problems.FormatterProblems;
import de.tools400.lpex.irpgformatter.problems.LocalFileSource;
import de.tools400.lpex.irpgformatter.problems.WorkspaceFileSource;

/**
 * Formats stream files like <i>iFormat</i> of their context menu, for other
 * plug-ins. The file is formatted by the same job as by the menu, with the same
 * settings, and its problems replace its problems in the view <i>iRPGFormatter
 * Problems</i> and its markers. Unlike the menu, no dialog is shown and the
 * view is not activated.
 * <p>
 * The methods block, until the file is formatted. Do not call them in the UI
 * thread. They neither check, whether an editor has unsaved changes of the
 * file, nor refresh a file of the workspace: that is the job of the caller.
 * </p>
 */
public final class FileFormatter {

    private FileFormatter() {
        // Utility class
    }

    /**
     * Formats a stream file of the workspace with the settings of its project,
     * if the project has settings of its own, otherwise with the settings of
     * the workspace.
     *
     * @param file - a file of an open project, that is in sync with the file
     *        system
     * @return the result
     * @throws InterruptedException if the thread was interrupted, while it
     *         waited for the format job
     */
    public static FileFormatResult formatWorkspaceFile(IFile file) throws InterruptedException {

        boolean projectSettings = SourcePreferences.get(file).isProject();
        IPath location = file.getLocation();
        File localFile = location == null ? null : location.toFile();

        PostRun postRun = new PostRun();
        FormatStreamFileJob job = new FormatStreamFileJob(new IFile[] { file }, postRun);

        return format(job, postRun, localFile, new WorkspaceFileSource(file).getKey(), projectSettings);
    }

    /**
     * Formats a file of the local file system, that is not part of the
     * workspace, with the settings of the workspace.
     *
     * @param file - the file
     * @return the result
     * @throws InterruptedException if the thread was interrupted, while it
     *         waited for the format job
     */
    public static FileFormatResult formatLocalFile(File file) throws InterruptedException {

        PostRun postRun = new PostRun();
        FormatJavaIoStreamFileJob job = new FormatJavaIoStreamFileJob(new File[] { file }, postRun);

        return format(job, postRun, file, new LocalFileSource(file).getKey(), false);
    }

    /**
     * Returns <code>true</code>, if a file of the workspace is the local copy
     * of a source member or of a stream file of the IFS, that Remote Systems
     * downloaded. Formatting the copy would not change the remote source.
     *
     * @param file - a file of the workspace
     * @return <code>true</code> for a local copy of a remote file
     */
    public static boolean isLocalCopyOfRemoteFile(IFile file) {
        return SourcePreferences.isLocalCopyOfRemoteFile(file);
    }

    private static FileFormatResult format(Job job, PostRun postRun, File file, String problemKey, boolean projectSettings)
        throws InterruptedException {

        byte[] before = readBytes(file);

        job.schedule();
        job.join();

        boolean changed = !Arrays.equals(before, readBytes(file));
        boolean canceled = postRun.isCanceled() || isCanceled(job.getResult());
        int formattedCount = postRun.getFormattedCount();
        int notFormattedCount = postRun.getNotFormattedCount();

        // A job, that did not process the file, left the problems of an
        // earlier run.
        List<FileFormatProblem> problems;
        if (formattedCount + notFormattedCount > 0) {
            problems = getProblems(problemKey);
        } else {
            problems = new ArrayList<>();
        }

        return FileFormatResult.create(formattedCount, notFormattedCount, canceled, changed, projectSettings, problems);
    }

    private static boolean isCanceled(IStatus status) {
        return status != null && status.getSeverity() == IStatus.CANCEL;
    }

    private static List<FileFormatProblem> getProblems(String problemKey) {

        List<FileFormatProblem> problems = new ArrayList<>();
        for (FormatterProblem problem : FormatterProblems.getInstance().getProblems(problemKey)) {
            FileFormatProblem.Severity severity = FileFormatProblem.Severity.valueOf(problem.getSeverity().name());
            problems.add(new FileFormatProblem(severity, problem.getLineNumber(), problem.getMessage()));
        }

        return problems;
    }

    /**
     * Returns the content of a file.
     *
     * @return the content or <code>null</code>, if the file cannot be read
     */
    private static byte[] readBytes(File file) {

        if (file == null || !file.isFile()) {
            return null;
        }

        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Receives the counts of the format job.
     */
    private static final class PostRun implements IFormatPostRun {

        private int formattedCount;
        private int notFormattedCount;
        private boolean canceled;

        @Override
        public synchronized void postRun(int formattedCount, int notFormattedCount, int statementErrorsCount, boolean canceled) {
            this.formattedCount = formattedCount;
            this.notFormattedCount = notFormattedCount;
            this.canceled = canceled;
        }

        public synchronized int getFormattedCount() {
            return formattedCount;
        }

        public synchronized int getNotFormattedCount() {
            return notFormattedCount;
        }

        public synchronized boolean isCanceled() {
            return canceled;
        }
    }
}
