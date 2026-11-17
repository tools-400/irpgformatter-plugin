/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.views;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import de.tools400.lpex.irpgformatter.problems.FormatterProblem;
import de.tools400.lpex.irpgformatter.problems.FormatterProblem.Severity;

/**
 * Hides the problems of the severities, that the user switched off in the menu
 * "Show Severity" of the view {@link FormatterProblemsView}, e.g. the sources,
 * that are not in free format. At first all severities are shown.
 * <p>
 * It does not extend <code>ViewerFilter</code>, so that the tests in the plain
 * Java project "iRPGFormatter JUnit" do not need JFace; the view wraps it.
 * </p>
 */
public class ProblemSeverityFilter {

    private final Set<Severity> visibleSeverities;

    public ProblemSeverityFilter() {
        this.visibleSeverities = EnumSet.allOf(Severity.class);
    }

    public boolean isVisible(Severity severity) {
        return visibleSeverities.contains(severity);
    }

    public void setVisible(Severity severity, boolean isVisible) {
        if (isVisible) {
            visibleSeverities.add(severity);
        } else {
            visibleSeverities.remove(severity);
        }
    }

    public boolean isVisible(FormatterProblem problem) {
        return isVisible(problem.getSeverity());
    }

    /**
     * Returns the number of problems, that the filter hides.
     *
     * @param problems - the problems
     * @return the number of hidden problems
     */
    public int countHidden(List<FormatterProblem> problems) {

        int count = 0;
        for (FormatterProblem problem : problems) {
            if (!isVisible(problem)) {
                count++;
            }
        }

        return count;
    }
}
