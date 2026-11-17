/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.problems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The problems of the sources, that were formatted, for the view
 * {@link de.tools400.lpex.irpgformatter.views.FormatterProblemsView}. Formatting
 * a source replaces its problems; a source without problems is removed. The
 * sources keep the order, in which they got their first problems.
 * <p>
 * The problems are changed from the UI thread and from the job, that formats
 * several sources, therefore the methods are synchronized.
 * </p>
 */
public final class FormatterProblems {

    private static final FormatterProblems INSTANCE = new FormatterProblems();

    private final Map<String, List<FormatterProblem>> problemsBySource;
    private final List<IFormatterProblemsListener> listeners;
    private String summary;

    private FormatterProblems() {
        this.problemsBySource = new LinkedHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.summary = null;
    }

    public static FormatterProblems getInstance() {
        return INSTANCE;
    }

    /**
     * Replaces the problems of a source. The problems are ordered by their line
     * numbers.
     *
     * @param source - the source
     * @param problems - the problems of the source, empty to remove the source
     */
    public void replace(IProblemSource source, List<FormatterProblem> problems) {

        synchronized (this) {
            if (problems.isEmpty()) {
                if (problemsBySource.remove(source.getKey()) == null) {
                    return;
                }
            } else {
                List<FormatterProblem> sortedProblems = new ArrayList<>(problems);
                sortedProblems.sort(Comparator.comparingInt(FormatterProblem::getLineNumber));
                problemsBySource.put(source.getKey(), sortedProblems);
            }
        }

        fireProblemsChanged();
    }

    /**
     * Removes the problems of the sources with the given keys.
     *
     * @param keys - the keys of the sources, see {@link IProblemSource#getKey()}
     */
    public void remove(Collection<String> keys) {

        boolean isChanged = false;
        synchronized (this) {
            for (String key : keys) {
                if (problemsBySource.remove(key) != null) {
                    isChanged = true;
                }
            }
        }

        if (isChanged) {
            fireProblemsChanged();
        }
    }

    /**
     * Removes the given problems. A source without problems is removed.
     *
     * @param problems - the problems
     */
    public void removeProblems(Collection<FormatterProblem> problems) {

        boolean isChanged = false;
        synchronized (this) {
            for (FormatterProblem problem : problems) {
                String key = problem.getSource().getKey();
                List<FormatterProblem> sourceProblems = problemsBySource.get(key);
                if (sourceProblems != null && sourceProblems.remove(problem)) {
                    isChanged = true;
                    if (sourceProblems.isEmpty()) {
                        problemsBySource.remove(key);
                    }
                }
            }
        }

        if (isChanged) {
            fireProblemsChanged();
        }
    }

    /**
     * Removes all problems and the summary.
     */
    public void removeAll() {

        synchronized (this) {
            problemsBySource.clear();
            summary = null;
        }

        fireProblemsChanged();
    }

    /**
     * Returns the problems of all sources, source by source.
     */
    public synchronized List<FormatterProblem> getProblems() {

        List<FormatterProblem> problems = new ArrayList<>();
        for (List<FormatterProblem> sourceProblems : problemsBySource.values()) {
            problems.addAll(sourceProblems);
        }

        return problems;
    }

    /**
     * Returns the problems of a source.
     *
     * @param key - the key of the source, see {@link IProblemSource#getKey()}
     * @return the problems, empty if the source has none
     */
    public synchronized List<FormatterProblem> getProblems(String key) {

        List<FormatterProblem> problems = problemsBySource.get(key);
        if (problems == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(problems);
    }

    /**
     * Returns the result of the last formatting of several sources, e.g.
     * "15 files formatted. 53 files not formatted.", or <code>null</code>.
     */
    public synchronized String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {

        synchronized (this) {
            this.summary = summary;
        }

        fireProblemsChanged();
    }

    public void addListener(IFormatterProblemsListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IFormatterProblemsListener listener) {
        listeners.remove(listener);
    }

    private void fireProblemsChanged() {
        for (IFormatterProblemsListener listener : listeners) {
            listener.problemsChanged();
        }
    }
}
