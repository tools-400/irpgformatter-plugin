/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.views;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.problems.FormatterProblem;
import de.tools400.lpex.irpgformatter.problems.FormatterProblem.Severity;
import de.tools400.lpex.irpgformatter.problems.FormatterProblems;
import de.tools400.lpex.irpgformatter.problems.IFormatterProblemsListener;
import de.tools400.lpex.irpgformatter.problems.IProblemSource;
import de.tools400.lpex.irpgformatter.problems.ProblemNavigator;

/**
 * Lists the errors, warnings and informations of the sources, that were
 * formatted, in the editor, on save or several at once. A double-click opens
 * the source and goes to the line of the problem. Formatting a source again
 * replaces its problems. The menu "Show Severity" of the view hides the
 * problems of a severity; the choice is kept in the dialog settings.
 */
public class FormatterProblemsView extends ViewPart implements IFormatterProblemsListener {

    public static final String ID = "de.tools400.lpex.irpgformatter.views.FormatterProblemsView"; //$NON-NLS-1$

    private static final String SETTINGS_SECTION = "FormatterProblemsView"; //$NON-NLS-1$
    private static final String SETTING_HIDE = "hide."; //$NON-NLS-1$

    private TableViewer viewer;
    private ProblemSeverityFilter severityFilter;
    private Action goToAction;
    private Action copyAction;
    private Action selectAllAction;
    private Action removeAction;
    private Action removeAllAction;

    /**
     * Shows the view.
     *
     * @param activate - <code>true</code> to activate it, <code>false</code> to
     *        leave the focus, where it is, e.g. in the editor
     */
    public static void show(boolean activate) {
        showView(activate);
    }

    /**
     * Shows the view without activating it and selects the first problem of
     * the given source, so that the problems of the source, that was just
     * formatted, are visible, also below the problems of other sources.
     *
     * @param source - the source, that was formatted
     */
    public static void show(IProblemSource source) {

        FormatterProblemsView view = showView(false);
        if (view != null) {
            view.selectFirstProblem(source.getKey());
        }
    }

    private static FormatterProblemsView showView(boolean activate) {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null && PlatformUI.getWorkbench().getWorkbenchWindowCount() > 0) {
            window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
        }

        if (window == null || window.getActivePage() == null) {
            return null;
        }

        try {
            IViewPart view = window.getActivePage().showView(ID, null, activate ? IWorkbenchPage.VIEW_ACTIVATE : IWorkbenchPage.VIEW_VISIBLE);
            if (view instanceof FormatterProblemsView) {
                return (FormatterProblemsView)view;
            }
        } catch (PartInitException e) {
            IRpgleFormatterPlugin.logError("Could not show the view " + ID, e); //$NON-NLS-1$
        }

        return null;
    }

    private void selectFirstProblem(String key) {

        if (viewer == null || viewer.getControl().isDisposed()) {
            return;
        }

        // The listener refreshes later, with asyncExec.
        refresh();

        for (FormatterProblem problem : FormatterProblems.getInstance().getProblems(key)) {
            if (severityFilter.isVisible(problem)) {
                viewer.setSelection(new StructuredSelection(problem), true);
                return;
            }
        }
    }

    @Override
    public void createPartControl(Composite parent) {

        viewer = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        createColumn(Messages.ColumnLabel_Message, 500, FormatterProblem::getMessage, true);
        createColumn(Messages.ColumnLabel_Source, 300, problem -> problem.getSource().getLabel(), false);
        createColumn(Messages.ColumnLabel_Line, 60,
            problem -> problem.getLineNumber() > 0 ? Integer.toString(problem.getLineNumber()) : "", false); //$NON-NLS-1$

        viewer.setContentProvider(ArrayContentProvider.getInstance());

        severityFilter = new ProblemSeverityFilter();
        loadSeverityFilter();
        viewer.addFilter(severityFilter);

        // double-click or Enter
        viewer.addOpenListener(event -> goToSelectedProblem());
        viewer.addSelectionChangedListener(event -> updateActions());

        createActions();
        createContextMenu();
        createToolBar();
        createViewMenu();

        FormatterProblems.getInstance().addListener(this);
        refresh();
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    @Override
    public void dispose() {
        FormatterProblems.getInstance().removeListener(this);
        super.dispose();
    }

    @Override
    public void problemsChanged() {

        if (viewer == null || viewer.getControl().isDisposed()) {
            return;
        }

        Display display = viewer.getControl().getDisplay();
        display.asyncExec(this::refresh);
    }

    private void refresh() {

        if (viewer.getControl().isDisposed()) {
            return;
        }

        List<FormatterProblem> problems = FormatterProblems.getInstance().getProblems();
        viewer.setInput(problems);

        updateContentDescription(problems);
        updateActions();
    }

    /**
     * Shows the result of the last formatting of several sources and, if the
     * filter hides problems, their number, so that no problem disappears
     * unnoticed.
     */
    private void updateContentDescription(List<FormatterProblem> problems) {

        StringBuilder description = new StringBuilder();

        String summary = FormatterProblems.getInstance().getSummary();
        if (summary != null) {
            description.append(summary);
        }

        int hiddenCount = severityFilter.countHidden(problems);
        if (hiddenCount > 0) {
            if (description.length() > 0) {
                description.append(' ');
            }
            description.append(Messages.bind(Messages.Label_N_entries_hidden, hiddenCount));
        }

        setContentDescription(description.toString());
    }

    private void createColumn(String title, int width, Function<FormatterProblem, String> text, boolean withImage) {

        TableViewerColumn column = new TableViewerColumn(viewer, SWT.LEFT);
        column.getColumn().setText(title);
        column.getColumn().setWidth(width);
        column.getColumn().setResizable(true);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return text.apply((FormatterProblem)element);
            }

            @Override
            public Image getImage(Object element) {
                if (!withImage) {
                    return null;
                }
                return PlatformUI.getWorkbench().getSharedImages().getImage(getImageId(((FormatterProblem)element).getSeverity()));
            }
        });
    }

    private void createActions() {

        goToAction = new Action(Messages.Action_Go_to) {
            @Override
            public void run() {
                goToSelectedProblem();
            }
        };

        copyAction = new Action(Messages.MenuItem_Copy) {
            @Override
            public void run() {
                copySelectedProblems();
            }
        };

        selectAllAction = new Action(Messages.MenuItem_Select_All) {
            @Override
            public void run() {
                viewer.getTable().selectAll();
                updateActions();
            }
        };

        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();

        removeAction = new Action(Messages.Action_Remove) {
            @Override
            public void run() {
                FormatterProblems.getInstance().removeProblems(getSelectedProblems());
            }
        };
        removeAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_ELCL_REMOVE));
        removeAction.setToolTipText(Messages.Action_Remove);

        removeAllAction = new Action(Messages.Action_Remove_all) {
            @Override
            public void run() {
                FormatterProblems.getInstance().removeAll();
            }
        };
        removeAllAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_ELCL_REMOVEALL));
        removeAllAction.setToolTipText(Messages.Action_Remove_all);

        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), selectAllAction);
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), removeAction);
    }

    private void createContextMenu() {

        MenuManager menuManager = new MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(this::fillContextMenu);
        viewer.getControl().setMenu(menuManager.createContextMenu(viewer.getControl()));
    }

    private void fillContextMenu(IMenuManager menu) {
        menu.add(goToAction);
        menu.add(new Separator());
        menu.add(copyAction);
        menu.add(selectAllAction);
        menu.add(new Separator());
        menu.add(removeAction);
        menu.add(removeAllAction);
    }

    private void createToolBar() {
        IToolBarManager toolBar = getViewSite().getActionBars().getToolBarManager();
        toolBar.add(removeAction);
        toolBar.add(removeAllAction);
    }

    /**
     * Creates the menu "Show Severity" of the view, like in the view
     * "Error List" of RDi.
     */
    private void createViewMenu() {

        MenuManager severityMenu = new MenuManager(Messages.MenuItem_Show_Severity);
        severityMenu.add(createSeverityAction(Severity.ERROR, Messages.Severity_Error));
        severityMenu.add(createSeverityAction(Severity.WARNING, Messages.Severity_Warning));
        severityMenu.add(createSeverityAction(Severity.INFORMATION, Messages.Severity_Information));

        getViewSite().getActionBars().getMenuManager().add(severityMenu);
    }

    private Action createSeverityAction(Severity severity, String label) {

        Action action = new Action(label, IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                severityFilter.setVisible(severity, isChecked());
                saveSeverityFilter();
                viewer.refresh();
                updateContentDescription(FormatterProblems.getInstance().getProblems());
                updateActions();
            }
        };
        action.setChecked(severityFilter.isVisible(severity));
        action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(getImageId(severity)));

        return action;
    }

    private static String getImageId(Severity severity) {

        switch (severity) {
        case ERROR:
            return ISharedImages.IMG_OBJS_ERROR_TSK;
        case WARNING:
            return ISharedImages.IMG_OBJS_WARN_TSK;
        default:
            return ISharedImages.IMG_OBJS_INFO_TSK;
        }
    }

    private void loadSeverityFilter() {

        IDialogSettings settings = getSettings();
        for (Severity severity : Severity.values()) {
            severityFilter.setVisible(severity, !settings.getBoolean(SETTING_HIDE + severity.name()));
        }
    }

    private void saveSeverityFilter() {

        IDialogSettings settings = getSettings();
        for (Severity severity : Severity.values()) {
            settings.put(SETTING_HIDE + severity.name(), !severityFilter.isVisible(severity));
        }
    }

    private static IDialogSettings getSettings() {

        IDialogSettings pluginSettings = IRpgleFormatterPlugin.getDefault().getDialogSettings();
        IDialogSettings settings = pluginSettings.getSection(SETTINGS_SECTION);
        if (settings == null) {
            settings = pluginSettings.addNewSection(SETTINGS_SECTION);
        }

        return settings;
    }

    private void updateActions() {

        boolean haveSelection = !viewer.getSelection().isEmpty();
        boolean haveProblems = viewer.getTable().getItemCount() > 0;

        goToAction.setEnabled(((IStructuredSelection)viewer.getSelection()).size() == 1);
        copyAction.setEnabled(haveSelection);
        selectAllAction.setEnabled(haveProblems);
        removeAction.setEnabled(haveSelection);
        // also the problems, that the filter hides
        boolean haveAnyProblems = !FormatterProblems.getInstance().getProblems().isEmpty();
        removeAllAction.setEnabled(haveAnyProblems || FormatterProblems.getInstance().getSummary() != null);
    }

    private void goToSelectedProblem() {

        List<FormatterProblem> problems = getSelectedProblems();
        if (problems.size() != 1) {
            return;
        }

        ProblemNavigator.goTo(problems.get(0), getSite().getPage());
    }

    private void copySelectedProblems() {

        StringBuilder buffer = new StringBuilder();
        for (FormatterProblem problem : getSelectedProblems()) {
            buffer.append(problem.getMessage());
            buffer.append('\t');
            buffer.append(problem.getSource().getLabel());
            buffer.append('\t');
            if (problem.getLineNumber() > 0) {
                buffer.append(problem.getLineNumber());
            }
            buffer.append(System.lineSeparator());
        }

        if (buffer.length() == 0) {
            return;
        }

        Clipboard clipboard = new Clipboard(viewer.getControl().getDisplay());
        try {
            clipboard.setContents(new Object[] { buffer.toString() }, new Transfer[] { TextTransfer.getInstance() });
        } finally {
            clipboard.dispose();
        }
    }

    private List<FormatterProblem> getSelectedProblems() {

        List<FormatterProblem> problems = new ArrayList<>();
        for (Object element : ((IStructuredSelection)viewer.getSelection()).toList()) {
            problems.add((FormatterProblem)element);
        }

        return problems;
    }
}
