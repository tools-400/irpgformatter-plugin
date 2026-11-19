/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.problems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.problems.FormatterProblem.Severity;

/**
 * Keeps the markers of the problems in line with {@link FormatterProblems}:
 * each problem of a source with a file of the workspace (see
 * {@link IProblemSource#getMarkerFile()}) becomes a marker of the type
 * {@link #MARKER_TYPE}. The LPEX editor shows the markers in its ruler, the
 * Problems view lists them.
 * <p>
 * A change of the problems schedules a job, which replaces the markers of the
 * files, that have or had problems. Like the problem markers of RDi, the
 * markers are not persistent.
 * </p>
 */
public final class ProblemMarkers implements IFormatterProblemsListener {

    public static final String MARKER_TYPE = IRpgleFormatterPlugin.PLUGIN_ID + ".problem"; //$NON-NLS-1$

    /** Changed, when an editor opens the file, see {@link #refresh(IFile)}. */
    private static final String ATTRIBUTE_REFRESH = "refresh"; //$NON-NLS-1$

    /** Collects several changes, e.g. of a batch job, into one update. */
    private static final long DELAY = 200;

    private final Set<IFile> markedFiles;
    private final UpdateJob updateJob;

    public ProblemMarkers() {
        this.markedFiles = new HashSet<>();
        this.updateJob = new UpdateJob();
    }

    @Override
    public void problemsChanged() {
        updateJob.schedule(DELAY);
    }

    /**
     * Removes all markers, e.g. when the plug-in is stopped.
     */
    public void dispose() {

        updateJob.cancel();

        synchronized (markedFiles) {
            for (IFile file : markedFiles) {
                deleteMarkers(file);
            }
            markedFiles.clear();
        }
    }

    private void updateMarkers() {

        Map<IFile, List<FormatterProblem>> problemsByFile = new HashMap<>();
        for (FormatterProblem problem : FormatterProblems.getInstance().getProblems()) {
            IFile file = problem.getSource().getMarkerFile();
            if (file != null) {
                problemsByFile.computeIfAbsent(file, key -> new ArrayList<>()).add(problem);
            }
        }

        synchronized (markedFiles) {

            Set<IFile> files = new HashSet<>(markedFiles);
            files.addAll(problemsByFile.keySet());

            for (IFile file : files) {
                deleteMarkers(file);
                List<FormatterProblem> problems = problemsByFile.get(file);
                if (problems != null) {
                    createMarkers(file, problems);
                }
            }

            markedFiles.clear();
            markedFiles.addAll(problemsByFile.keySet());
        }
    }

    /**
     * Changes the markers of a file, which an LPEX editor just opened. The
     * annotation model of the LPEX editor does not take over the markers, that
     * the file already has, so it neither shows them in its ruler nor goes to
     * them. A changed marker, that the model does not know, is added to it
     * (<code>AbstractMarkerAnnotationModel.modifyMarkerAnnotation</code>).
     * <p>
     * The markers are changed at once, so that the Problems view, which opened
     * the editor, can go to its marker, and once more later, in case the editor
     * had not yet loaded the document. The markers stay the same objects.
     * </p>
     *
     * @param file - the file of the editor
     */
    public static void refresh(IFile file) {

        touchMarkers(file);

        WorkspaceJob job = new WorkspaceJob("Refresh iRPGFormatter markers") { //$NON-NLS-1$
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) {
                touchMarkers(file);
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.schedule(DELAY);
    }

    /**
     * Returns the marker of a problem, so that the view can go to it like the
     * Problems view.
     *
     * @param problem - the problem
     * @return the marker or <code>null</code>, if the problem has none
     */
    public static IMarker findMarker(FormatterProblem problem) {

        IFile file = problem.getSource().getMarkerFile();
        if (file == null || !file.exists() || problem.getLineNumber() <= 0) {
            return null;
        }

        try {
            for (IMarker marker : file.findMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO)) {
                if (marker.getAttribute(IMarker.LINE_NUMBER, -1) == problem.getLineNumber()
                    && problem.getMessage().equals(marker.getAttribute(IMarker.MESSAGE, ""))) { //$NON-NLS-1$
                    return marker;
                }
            }
        } catch (CoreException e) {
            IRpgleFormatterPlugin.logError("Could not read the markers of " + file.getFullPath(), e); //$NON-NLS-1$
        }

        return null;
    }

    private static void touchMarkers(IFile file) {

        if (!file.exists()) {
            return;
        }

        try {
            for (IMarker marker : file.findMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO)) {
                marker.setAttribute(ATTRIBUTE_REFRESH, marker.getAttribute(ATTRIBUTE_REFRESH, 0) + 1);
            }
        } catch (CoreException e) {
            IRpgleFormatterPlugin.logError("Could not refresh the markers of " + file.getFullPath(), e); //$NON-NLS-1$
        }
    }

    private static void deleteMarkers(IFile file) {

        if (!file.exists()) {
            return;
        }

        try {
            file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
        } catch (CoreException e) {
            IRpgleFormatterPlugin.logError("Could not delete the markers of " + file.getFullPath(), e); //$NON-NLS-1$
        }
    }

    private static void createMarkers(IFile file, List<FormatterProblem> problems) {

        if (!file.exists()) {
            return;
        }

        for (FormatterProblem problem : problems) {
            try {
                IMarker marker = file.createMarker(MARKER_TYPE);
                Map<String, Object> attributes = new HashMap<>();
                attributes.put(IMarker.MESSAGE, problem.getMessage());
                attributes.put(IMarker.SEVERITY, getMarkerSeverity(problem.getSeverity()));
                if (problem.getLineNumber() > 0) {
                    attributes.put(IMarker.LINE_NUMBER, problem.getLineNumber());
                }
                marker.setAttributes(attributes);
            } catch (CoreException e) {
                IRpgleFormatterPlugin.logError("Could not create a marker for " + file.getFullPath(), e); //$NON-NLS-1$
            }
        }
    }

    private static int getMarkerSeverity(Severity severity) {

        switch (severity) {
        case ERROR:
            return IMarker.SEVERITY_ERROR;
        case WARNING:
            return IMarker.SEVERITY_WARNING;
        default:
            return IMarker.SEVERITY_INFO;
        }
    }

    private final class UpdateJob extends WorkspaceJob {

        private UpdateJob() {
            super("Update iRPGFormatter markers"); //$NON-NLS-1$
            setSystem(true);
        }

        @Override
        public IStatus runInWorkspace(IProgressMonitor monitor) {
            updateMarkers();
            return Status.OK_STATUS;
        }
    }
}
