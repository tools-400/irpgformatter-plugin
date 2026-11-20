/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The result of formatting a file with {@link FileFormatter}.
 */
public final class FileFormatResult {

    public enum Status {
        /**
         * The file was formatted. A file, whose lines stay the same, is not
         * written. Statements with errors keep their original lines, see
         * {@link FileFormatResult#getProblems()}.
         */
        FORMATTED,
        /**
         * The file was not formatted, because iRPGFormatter does not format it,
         * e.g. because it is not in free format.
         */
        SKIPPED,
        /**
         * The file could not be formatted, e.g. because a declaration block is
         * not closed or the file could not be read or written.
         */
        FAILED,
        /**
         * The format job was canceled, e.g. in the view <i>Progress</i>.
         */
        CANCELED;
    }

    private final Status status;
    private final boolean changed;
    private final boolean projectSettings;
    private final List<FileFormatProblem> problems;

    private FileFormatResult(Status status, boolean changed, boolean projectSettings, List<FileFormatProblem> problems) {
        this.status = status;
        this.changed = changed;
        this.projectSettings = projectSettings;
        this.problems = Collections.unmodifiableList(new ArrayList<>(problems));
    }

    /**
     * Creates the result of a format job, that formatted one file, from the
     * counts, that the job passed to its post run.
     *
     * @param formattedCount - 1, if the file was formatted
     * @param notFormattedCount - 1, if the file could not be formatted or was
     *        skipped
     * @param canceled - <code>true</code>, if the job was canceled
     * @param changed - <code>true</code>, if the content of the file changed
     * @param projectSettings - <code>true</code>, if the settings of the
     *        project of the file were used
     * @param problems - the problems of the file
     * @return the result
     */
    public static FileFormatResult create(int formattedCount, int notFormattedCount, boolean canceled, boolean changed, boolean projectSettings,
        List<FileFormatProblem> problems) {

        Status status;
        if (formattedCount > 0) {
            status = Status.FORMATTED;
        } else if (notFormattedCount > 0) {
            status = isSkipped(problems) ? Status.SKIPPED : Status.FAILED;
        } else if (canceled) {
            status = Status.CANCELED;
        } else {
            status = Status.FAILED;
        }

        return new FileFormatResult(status, changed, projectSettings, problems);
    }

    /**
     * A file, that is skipped, has only the information, why it is skipped.
     */
    private static boolean isSkipped(List<FileFormatProblem> problems) {

        if (problems.isEmpty()) {
            return false;
        }

        for (FileFormatProblem problem : problems) {
            if (problem.getSeverity() != FileFormatProblem.Severity.INFORMATION) {
                return false;
            }
        }

        return true;
    }

    public Status getStatus() {
        return status;
    }

    /**
     * Returns <code>true</code>, if the content of the file changed.
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * Returns <code>true</code>, if the file was formatted with the settings of
     * its project, <code>false</code>, if it was formatted with the settings of
     * the workspace.
     */
    public boolean isProjectSettings() {
        return projectSettings;
    }

    /**
     * Returns the problems of the file, ordered by their line numbers, as they
     * are shown in the view <i>iRPGFormatter Problems</i>.
     *
     * @return the problems, empty if there are none
     */
    public List<FileFormatProblem> getProblems() {
        return problems;
    }
}
