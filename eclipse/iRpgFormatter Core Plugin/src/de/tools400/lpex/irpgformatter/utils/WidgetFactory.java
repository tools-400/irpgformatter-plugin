/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * Factory for creating SWT widgets, named like the WidgetFactory of iSphere.
 */
public final class WidgetFactory {

    private WidgetFactory() {
        // Utility class
    }

    /**
     * Creates a label and a read-only text field, both with the tooltip.
     */
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

    /**
     * Creates a push button, that is at least as wide as the buttons of a
     * dialog.
     */
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
     * Creates a checkbox with label and tooltip.
     */
    public static Button createCheckbox(Composite parent, String label, String tooltip) {

        Button checkbox = new Button(parent, SWT.CHECK);
        checkbox.setText(label);
        checkbox.setToolTipText(tooltip);

        return checkbox;
    }

    /**
     * Creates a label and a spinner, both with the tooltip.
     */
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

    /**
     * Creates an empty row, that spans all columns of the grid layout of the
     * composite.
     */
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
