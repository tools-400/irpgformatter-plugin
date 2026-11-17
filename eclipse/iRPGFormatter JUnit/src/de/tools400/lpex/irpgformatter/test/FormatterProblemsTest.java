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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.irpgformatter.formatter.BlockStructureException;
import de.tools400.lpex.irpgformatter.formatter.FormattedResult;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatterException;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;
import de.tools400.lpex.irpgformatter.problems.FormatterProblem;
import de.tools400.lpex.irpgformatter.problems.FormatterProblem.Severity;
import de.tools400.lpex.irpgformatter.problems.FormatterProblems;
import de.tools400.lpex.irpgformatter.problems.IFormatterProblemsListener;
import de.tools400.lpex.irpgformatter.problems.IProblemSource;
import de.tools400.lpex.irpgformatter.problems.ProblemFactory;
import de.tools400.lpex.irpgformatter.views.ProblemSeverityFilter;

/**
 * Tests the problems of the view "iRPGFormatter Problems".
 */
public class FormatterProblemsTest extends AbstractTestCase {

    private FormatterProblems problems;
    private int changeCount;
    private IFormatterProblemsListener listener;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        problems = FormatterProblems.getInstance();
        problems.removeAll();

        changeCount = 0;
        listener = () -> changeCount++;
        problems.addListener(listener);
    }

    @Override
    @After
    public void tearDown() {
        problems.removeListener(listener);
        problems.removeAll();

        super.tearDown();
    }

    @Test
    public void replace_ordersProblemsOfSourceByLineNumber() {

        Source a = new Source("A");
        problems.replace(a, Arrays.asList(error(a, 20), error(a, 3), error(a, 10)));

        List<FormatterProblem> result = problems.getProblems();
        assertEquals(3, result.size());
        assertEquals(3, result.get(0).getLineNumber());
        assertEquals(10, result.get(1).getLineNumber());
        assertEquals(20, result.get(2).getLineNumber());
        assertEquals(1, changeCount);
    }

    @Test
    public void replace_keepsOrderOfSources() {

        Source a = new Source("A");
        Source b = new Source("B");
        problems.replace(a, Arrays.asList(error(a, 1)));
        problems.replace(b, Arrays.asList(error(b, 1)));

        // formatting A again replaces its problems in place
        problems.replace(a, Arrays.asList(error(a, 5), error(a, 6)));

        List<FormatterProblem> result = problems.getProblems();
        assertEquals(3, result.size());
        assertSame(a, result.get(0).getSource());
        assertEquals(5, result.get(0).getLineNumber());
        assertSame(a, result.get(1).getSource());
        assertSame(b, result.get(2).getSource());
    }

    @Test
    public void replace_withoutProblemsRemovesSource() {

        Source a = new Source("A");
        Source b = new Source("B");
        problems.replace(a, Arrays.asList(error(a, 1)));
        problems.replace(b, Arrays.asList(error(b, 2)));

        problems.replace(a, Collections.<FormatterProblem> emptyList());

        List<FormatterProblem> result = problems.getProblems();
        assertEquals(1, result.size());
        assertSame(b, result.get(0).getSource());
        assertTrue(problems.getProblems("A").isEmpty());
    }

    @Test
    public void replace_withoutProblemsOfUnknownSourceChangesNothing() {

        problems.replace(new Source("A"), Collections.<FormatterProblem> emptyList());

        assertEquals(0, changeCount);
    }

    @Test
    public void remove_removesTheGivenSources() {

        Source a = new Source("A");
        Source b = new Source("B");
        problems.replace(a, Arrays.asList(error(a, 1)));
        problems.replace(b, Arrays.asList(error(b, 2)));
        changeCount = 0;

        problems.remove(Arrays.asList("A", "X"));

        assertEquals(1, problems.getProblems().size());
        assertEquals(1, changeCount);

        problems.remove(Arrays.asList("X"));
        assertEquals(1, changeCount);
    }

    @Test
    public void removeAll_removesProblemsAndSummary() {

        Source a = new Source("A");
        problems.replace(a, Arrays.asList(error(a, 1)));
        problems.setSummary("1 file formatted.");

        problems.removeAll();

        assertTrue(problems.getProblems().isEmpty());
        assertNull(problems.getSummary());
    }

    @Test
    public void fromBlockStructureException_keepsLineNumbers() {

        Source a = new Source("A");
        BlockStructureException e = new BlockStructureException(new String[] { "first", "second" }, new int[] { 193, 195 });

        List<FormatterProblem> result = ProblemFactory.fromBlockStructureException(a, e);

        assertEquals(2, result.size());
        assertEquals(193, result.get(0).getLineNumber());
        assertEquals("first", result.get(0).getMessage());
        assertEquals(Severity.ERROR, result.get(0).getSeverity());
        assertEquals(195, result.get(1).getLineNumber());
    }

    /**
     * The comment, that cannot be indented, moves from line 4 to line 5,
     * because the prototype in front of it gets two lines.
     */
    @Test
    public void fromFormatter_mapsLineNumbersToFormattedSource() throws RpgleFormatterException {

        RpgleFormatter formatter = new RpgleFormatter();
        formatter.setSourceLength(40);

        // @formatter:off
        FormattedResult result = formatter.format(new TextLinesInput(
            "**free",
            "dcl-pr myLongPrototypeName extproc('MY_LONG_PROTOTYPE_NAME') end-pr;",
            "dcl-proc p;",
            "// This comment has exactly 39 chars...",
            "dcl-s x int(10);",
            "end-proc;"
        ), 0);
        // @formatter:on

        assertEquals(1, formatter.getErrorCount());
        assertEquals(4, formatter.getErrors().get(0).getStartLineNumber());
        assertEquals("// This comment has exactly 39 chars...", result.toLines()[4]);

        Source a = new Source("A");
        List<FormatterProblem> problemsOfA = ProblemFactory.fromFormatter(a, formatter, result);

        assertEquals(1, problemsOfA.size());
        assertEquals(Severity.ERROR, problemsOfA.get(0).getSeverity());
        assertEquals(5, problemsOfA.get(0).getLineNumber());
    }

    @Test
    public void fromMessage_isErrorWithoutLineNumber() {

        List<FormatterProblem> result = ProblemFactory.fromMessage(new Source("A"), "Could not format member. See error log.");

        assertEquals(1, result.size());
        assertEquals(Severity.ERROR, result.get(0).getSeverity());
        assertEquals(0, result.get(0).getLineNumber());
    }

    /**
     * A source, that is not in free format, is not wrong. iRPGFormatter skips
     * it, so it is an information.
     */
    @Test
    public void fromValidation_isInformationWithoutLineNumber() {

        List<FormatterProblem> result = ProblemFactory.fromValidation(new Source("A"), "Source is not in **free format.");

        assertEquals(1, result.size());
        assertEquals(Severity.INFORMATION, result.get(0).getSeverity());
        assertEquals(0, result.get(0).getLineNumber());
    }

    @Test
    public void severityFilter_showsAllSeveritiesAtFirst() {

        ProblemSeverityFilter filter = new ProblemSeverityFilter();

        for (Severity severity : Severity.values()) {
            assertTrue(filter.isVisible(severity));
        }
    }

    @Test
    public void severityFilter_hidesSwitchedOffSeverity() {

        Source a = new Source("A");
        FormatterProblem error = error(a, 1);
        FormatterProblem information = new FormatterProblem(a, Severity.INFORMATION, 0, "Source is not in **free format.");

        ProblemSeverityFilter filter = new ProblemSeverityFilter();
        filter.setVisible(Severity.INFORMATION, false);

        assertTrue(filter.select(null, null, error));
        assertFalse(filter.select(null, null, information));
        assertEquals(2, filter.countHidden(Arrays.asList(error, information, information)));

        filter.setVisible(Severity.INFORMATION, true);
        assertTrue(filter.select(null, null, information));
        assertEquals(0, filter.countHidden(Arrays.asList(error, information)));
    }

    private FormatterProblem error(IProblemSource source, int lineNumber) {
        return new FormatterProblem(source, Severity.ERROR, lineNumber, "Error on line #" + lineNumber);
    }

    private static class Source implements IProblemSource {

        private final String key;

        Source(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getLabel() {
            return key;
        }

        @Override
        public IEditorPart open(IWorkbenchPage page) {
            return null;
        }
    }
}
