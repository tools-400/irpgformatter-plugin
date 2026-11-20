/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.api.FileFormatProblem;
import de.tools400.lpex.irpgformatter.api.FileFormatProblem.Severity;
import de.tools400.lpex.irpgformatter.api.FileFormatResult;
import de.tools400.lpex.irpgformatter.api.FileFormatResult.Status;
import de.tools400.lpex.irpgformatter.problems.FormatterProblem;

/**
 * Tests the status, that {@link FileFormatResult} derives from the counts of
 * the format job and the problems of the file.
 */
public class FileFormatResultTest {

    private static final List<FileFormatProblem> NO_PROBLEMS = Collections.emptyList();

    @Test
    public void testFormatted() {

        FileFormatResult result = FileFormatResult.create(1, 0, false, true, true, NO_PROBLEMS);

        assertEquals(Status.FORMATTED, result.getStatus());
        assertTrue(result.isChanged());
        assertTrue(result.isProjectSettings());
        assertTrue(result.getProblems().isEmpty());
    }

    @Test
    public void testFormatted_unchanged() {

        FileFormatResult result = FileFormatResult.create(1, 0, false, false, false, NO_PROBLEMS);

        assertEquals(Status.FORMATTED, result.getStatus());
        assertFalse(result.isChanged());
        assertFalse(result.isProjectSettings());
    }

    @Test
    public void testFormatted_withStatementErrors() {

        List<FileFormatProblem> problems = Arrays.asList(problem(Severity.ERROR, 12), problem(Severity.WARNING, 30));

        FileFormatResult result = FileFormatResult.create(1, 0, false, true, false, problems);

        assertEquals(Status.FORMATTED, result.getStatus());
        assertEquals(problems, result.getProblems());
    }

    @Test
    public void testSkipped_notFreeFormat() {

        FileFormatResult result = FileFormatResult.create(0, 1, false, false, false, Arrays.asList(problem(Severity.INFORMATION, 0)));

        assertEquals(Status.SKIPPED, result.getStatus());
    }

    @Test
    public void testFailed_blockStructure() {

        List<FileFormatProblem> problems = Arrays.asList(problem(Severity.ERROR, 5), problem(Severity.ERROR, 9));

        FileFormatResult result = FileFormatResult.create(0, 1, false, false, false, problems);

        assertEquals(Status.FAILED, result.getStatus());
        assertEquals(2, result.getProblems().size());
    }

    @Test
    public void testFailed_readError() {

        FileFormatResult result = FileFormatResult.create(0, 1, false, false, false, Arrays.asList(problem(Severity.ERROR, 0)));

        assertEquals(Status.FAILED, result.getStatus());
    }

    @Test
    public void testFailed_withoutProblems() {
        assertEquals(Status.FAILED, FileFormatResult.create(0, 1, false, false, false, NO_PROBLEMS).getStatus());
    }

    @Test
    public void testFailed_jobDidNotReport() {
        assertEquals(Status.FAILED, FileFormatResult.create(0, 0, false, false, false, NO_PROBLEMS).getStatus());
    }

    @Test
    public void testCanceled() {
        assertEquals(Status.CANCELED, FileFormatResult.create(0, 0, true, false, false, NO_PROBLEMS).getStatus());
    }

    @Test
    public void testProblemsAreACopy() {

        List<FileFormatProblem> problems = new ArrayList<>();
        problems.add(problem(Severity.WARNING, 3));

        FileFormatResult result = FileFormatResult.create(1, 0, false, true, false, problems);
        problems.clear();

        assertEquals(1, result.getProblems().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testProblemsAreUnmodifiable() {
        FileFormatResult.create(1, 0, false, true, false, NO_PROBLEMS).getProblems().add(problem(Severity.ERROR, 1));
    }

    @Test
    public void testProblem() {

        FileFormatProblem problem = new FileFormatProblem(Severity.WARNING, 7, "Statement not closed.");

        assertEquals(Severity.WARNING, problem.getSeverity());
        assertEquals(7, problem.getLineNumber());
        assertEquals("Statement not closed.", problem.getMessage());
        assertEquals("WARNING:7: Statement not closed.", problem.toString());
    }

    /**
     * {@link de.tools400.lpex.irpgformatter.api.FileFormatter} converts the
     * severities by their names.
     */
    @Test
    public void testSeverities_sameAsFormatterProblem() {

        assertEquals(FormatterProblem.Severity.values().length, Severity.values().length);
        for (FormatterProblem.Severity severity : FormatterProblem.Severity.values()) {
            assertEquals(severity.name(), Severity.valueOf(severity.name()).name());
        }
    }

    private static FileFormatProblem problem(Severity severity, int lineNumber) {
        return new FileFormatProblem(severity, lineNumber, "Message of line " + lineNumber);
    }
}
