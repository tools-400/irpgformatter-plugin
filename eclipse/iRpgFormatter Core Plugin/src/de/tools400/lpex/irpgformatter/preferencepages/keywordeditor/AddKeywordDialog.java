/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages.keywordeditor;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.tools400.lpex.irpgformatter.Messages;

/**
 * Dialog for adding a new keyword or keyword parameter.
 */
public class AddKeywordDialog extends TitleAreaDialog {

    private final String dialogTitle;
    private final boolean isSpecialWord;

    private Text keyText;
    private Text valueText;

    private String key;
    private String value;

    /**
     * Creates a new add keyword dialog.
     *
     * @param parentShell the parent shell
     * @param title the dialog title
     * @param isSpecialWord true if adding a keyword parameter (starts with *)
     */
    public AddKeywordDialog(Shell parentShell, String title, boolean isSpecialWord) {
        super(parentShell);
        this.dialogTitle = title;
        this.isSpecialWord = isSpecialWord;
    }

    @Override
    public void create() {
        super.create();
        setTitle(dialogTitle);
        setMessage(Messages.Label_Enter_the_keyword_key_and_its_canonical_value + "\n"
            + Messages.Label_The_key_will_be_converted_to_uppercase_automatically);
        validateInput();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite)super.createDialogArea(parent);

        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setLayout(new GridLayout(2, false));

        // Key field
        Label keyLabel = new Label(container, SWT.NONE);
        keyLabel.setText(Messages.Label_Key);

        keyText = new Text(container, SWT.BORDER);
        keyText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        keyText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                // Auto-generate value from key if value is empty or matches
                // previous auto-value
                String currentKey = keyText.getText().trim();
                String currentValue = valueText.getText().trim();
                if (currentValue.isEmpty() || currentValue.equals(toCanonicalForm(key))) {
                    valueText.setText(toCanonicalForm(currentKey));
                }
                key = currentKey.toUpperCase();
                validateInput();
            }
        });

        // Value field
        Label valueLabel = new Label(container, SWT.NONE);
        valueLabel.setText(Messages.Label_Value);

        valueText = new Text(container, SWT.BORDER);
        valueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        valueText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                value = valueText.getText().trim();
                validateInput();
            }
        });

        // Set initial hint
        if (isSpecialWord) {
            keyText.setMessage("e.g., *NOPASS");
            valueText.setMessage("e.g., *NoPass");
        } else {
            keyText.setMessage("e.g., DCL-DS");
            valueText.setMessage("e.g., Dcl-Ds");
        }

        return area;
    }

    /**
     * Converts a key to a canonical UpperCamelCase form.
     */
    private String toCanonicalForm(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String text = input.trim();
        if (text.isEmpty()) {
            return "";
        }

        // Handle *params
        if (text.startsWith("*")) {
            String rest = text.substring(1);
            if (rest.isEmpty()) {
                return "*";
            }
            return "*" + capitalizeFirst(rest.toLowerCase());
        }

        // Handle hyphenated keywords
        if (text.contains("-")) {
            String[] parts = text.split("-");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    result.append("-");
                }
                result.append(capitalizeFirst(parts[i].toLowerCase()));
            }
            return result.toString();
        }

        // Simple keyword
        return capitalizeFirst(text.toLowerCase());
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void validateInput() {
        String errorMessage = null;

        String currentKey = keyText != null ? keyText.getText().trim() : "";
        String currentValue = valueText != null ? valueText.getText().trim() : "";

        if (currentKey.isEmpty()) {
            errorMessage = Messages.Error_Key_is_required;
        } else if (currentValue.isEmpty()) {
            errorMessage = Messages.Error_Value_is_required;
        } else if (isSpecialWord && !currentKey.startsWith("*")) {
            errorMessage = Messages.Error_Keyword_parameters_must_start_with_A;
        }

        setErrorMessage(errorMessage);
        getButton(IDialogConstants.OK_ID).setEnabled(errorMessage == null);
    }

    @Override
    protected void okPressed() {
        key = keyText.getText().trim().toUpperCase();
        value = valueText.getText().trim();
        super.okPressed();
    }

    /**
     * Gets the entered key (uppercase).
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the entered value (canonical form).
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }
}
