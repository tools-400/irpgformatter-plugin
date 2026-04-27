/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;

public class UIUtils {

    private UIUtils() {
        // Utility class
    }

    public static Shell getShell() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }

    public static Display getDisplay() {
        return PlatformUI.getWorkbench().getDisplay();
    }

    public static void displayErrorDialog(String message, String[] errorDetails) {

        MultiStatus multiStatus = new MultiStatus(IRpgleFormatterPlugin.PLUGIN_ID, IStatus.ERROR, message, null);

        for (String detail : errorDetails) {
            multiStatus.add(new Status(IStatus.ERROR, IRpgleFormatterPlugin.PLUGIN_ID, detail));
        }

        ErrorDialog.openError(getShell(), Messages.E_R_R_O_R, message, multiStatus);
    }

    public static void displayErrorDetailsTitleAreaDialog(String message, String[] errorDetails) {
        ErrorDetailsTitleAreaDialog dialog = new ErrorDetailsTitleAreaDialog(getShell(), message, errorDetails);
        dialog.open();
    }

    public static void displayErrorDetailsMessageDialog(String message, String[] errorDetails) {
        ErrorDetailsMessageDialog dialog = new ErrorDetailsMessageDialog(getShell(), message, errorDetails);
        dialog.open();
    }

    public static Text createReadOnlyText(Composite parent, String label, String tooltipText) {

        Label textLabel = new Label(parent, SWT.NONE);
        textLabel.setText(label);
        textLabel.setToolTipText(tooltipText);

        Text text = new Text(parent, SWT.NONE);
        text.setEditable(false);
        text.setToolTipText(tooltipText);

        return text;
    }

    public static Button createToggleButton(Composite parent, String label, String tooltipText) {

        Button button = new Button(parent, SWT.TOGGLE);
        button.setText(label);
        button.setToolTipText(tooltipText);

        return button;
    }

    public static Button createStandardButton(Composite parent, String label, String tooltipText) {

        Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        button.setToolTipText(tooltipText);

        PixelConverter pixelConverter = new PixelConverter(parent);
        int buttonWidth = pixelConverter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);

        GridData exportGridData = new GridData();
        exportGridData.widthHint = Math.max(buttonWidth, button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
        button.setLayoutData(exportGridData);

        return button;
    }

    /**
     * Creates a checkbox that spans 2 columns with label, tooltip, and an
     * optional selection listener.
     */
    public static Button createCheckbox(Composite parent, String label, String tooltip) {

        Button checkbox = new Button(parent, SWT.CHECK);
        checkbox.setText(label);
        checkbox.setToolTipText(tooltip);

        return checkbox;
    }

    public static Spinner createSpinner(Composite parent, String label, String tooltip, int min, int max) {

        Label spinnerLabel = new Label(parent, SWT.NONE);
        spinnerLabel.setText(label);
        spinnerLabel.setToolTipText(tooltip);

        Spinner spinner = new Spinner(parent, SWT.BORDER);
        spinner.setToolTipText(tooltip);
        spinner.setMinimum(min);
        spinner.setMaximum(max);

        return spinner;
    }

    public static void createLineSeparator(Composite composite) {

        Layout layout = composite.getLayout();
        if (!(layout instanceof GridLayout)) {
            throw new IllegalArgumentException("Composite has not a GridLayout: " + layout.getClass().getSimpleName());
        }

        GridLayout gridLayout = (GridLayout)layout;

        Label separator = new Label(composite, SWT.NONE);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL_HORIZONTAL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = gridLayout.numColumns;
        separator.setLayoutData(gridData);
    }
}
