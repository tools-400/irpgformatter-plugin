/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.problems;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.lpex.core.LpexView;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.utils.ExceptionUtils;
import de.tools400.lpex.irpgformatter.utils.LpexViewUtils;
import de.tools400.lpex.irpgformatter.utils.UIUtils;

/**
 * Opens the source of a problem and goes to its line.
 */
public final class ProblemNavigator {

    private ProblemNavigator() {
        // Utility class
    }

    /**
     * Opens the source of the problem, or activates its editor, and puts the
     * cursor on the first column of the line of the problem.
     *
     * @param problem - the problem
     * @param page - the workbench page
     */
    public static void goTo(FormatterProblem problem, IWorkbenchPage page) {

        IProblemSource source = problem.getSource();

        IEditorPart[] editor = new IEditorPart[1];
        Exception[] error = new Exception[1];
        BusyIndicator.showWhile(page.getWorkbenchWindow().getShell().getDisplay(), () -> {
            try {
                editor[0] = source.open(page);
            } catch (Exception e) {
                error[0] = e;
            }
        });

        if (error[0] != null) {
            IRpgleFormatterPlugin.logError("Could not open " + source.getLabel(), error[0]); //$NON-NLS-1$
            UIUtils.displaySimpleErrorDialog(
                Messages.bind(Messages.Error_Could_not_open_A_B, source.getLabel(), ExceptionUtils.getLocalizedMessage(error[0])));
            return;
        }

        if (editor[0] == null) {
            return;
        }

        LpexView view;
        if (source instanceof EditorSource) {
            view = ((EditorSource)source).getView();
        } else {
            view = EditorSources.getView(editor[0]);
            if (view != null) {
                // formatting in this editor replaces the problems of the source
                EditorSources.put(view, source);
            }
        }

        page.activate(editor[0]);

        if (view != null) {
            goToLine(view, problem.getLineNumber());
        } else if (editor[0] instanceof ITextEditor) {
            goToLine((ITextEditor)editor[0], problem.getLineNumber());
        }

        editor[0].setFocus();
    }

    private static void goToLine(LpexView view, int lineNumber) {

        if (lineNumber <= 0 || view.isDisposed()) {
            return;
        }

        LpexViewUtils.setLine(view, Math.min(lineNumber, LpexViewUtils.getNumLines(view)));
        LpexViewUtils.setPosition(view, 1);
    }

    private static void goToLine(ITextEditor editor, int lineNumber) {

        if (lineNumber <= 0) {
            return;
        }

        IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        if (document == null) {
            return;
        }

        try {
            int line = Math.min(lineNumber, document.getNumberOfLines()) - 1;
            editor.selectAndReveal(document.getLineOffset(line), 0);
        } catch (BadLocationException e) {
            // the source was changed meanwhile
        }
    }
}
