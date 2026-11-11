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
 * Dialog for adding or changing an entry of a keyword table: data type,
 * declaration type, keyword, special word, operation code or built-in
 * function.
 */
public class AddKeywordDialog extends TitleAreaDialog {

    private final String dialogTitle;
    private final KeywordKind kind;

    private Text keyText;
    private Text valueText;

    private String key;
    private String value;

    private String initialKey;
    private String initialValue;

    /**
     * Creates a new add keyword dialog.
     *
     * @param parentShell the parent shell
     * @param title the dialog title
     * @param kind the kind of the entries
     */
    public AddKeywordDialog(Shell parentShell, String title, KeywordKind kind) {
        super(parentShell);
        this.dialogTitle = title;
        this.kind = kind;
    }

    /**
     * Creates a new keyword dialog pre-filled with initial values (for changing
     * an existing entry).
     *
     * @param parentShell the parent shell
     * @param title the dialog title
     * @param kind the kind of the entries
     * @param initialKey the initial key to display
     * @param initialValue the initial value to display
     */
    public AddKeywordDialog(Shell parentShell, String title, KeywordKind kind, String initialKey, String initialValue) {
        this(parentShell, title, kind);
        this.initialKey = initialKey;
        this.initialValue = initialValue;
    }

    @Override
    public void create() {
        super.create();
        setTitle(dialogTitle);
        setMessage(Messages.Label_Enter_the_keyword_key_and_its_canonical_value + "\n"
            + Messages.Label_The_key_will_be_converted_to_uppercase_automatically);
        // Not in createDialogArea(): the listeners of the fields validate the
        // input and enable the OK button, which does not exist there yet.
        setInitialValues();
        validateInput();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite)super.createDialogArea(parent);

        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setLayout(new GridLayout(2, false));

        createKeyText(container);
        createValueText(container);

        return area;
    }

    /**
     * Creates the key field. Its listener generates the canonical value from
     * the key, as long as the value is empty or the canonical value of the
     * previous key.
     */
    private void createKeyText(Composite container) {

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

        keyText.setMessage(Messages.bind(Messages.Label_For_example_A, kind.getExampleKey()));
    }

    private void createValueText(Composite container) {

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

        valueText.setMessage(Messages.bind(Messages.Label_For_example_A, kind.getExampleValue()));
    }

    /**
     * Pre-fills the fields with the entry to change.
     */
    private void setInitialValues() {

        if (initialKey != null) {
            key = initialKey.toUpperCase();
            keyText.setText(initialKey);
        }
        if (initialValue != null) {
            value = initialValue;
            valueText.setText(initialValue);
        }
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

        // Handle *params and %functions
        if (text.startsWith("*") || text.startsWith("%")) {
            String rest = text.substring(1);
            if (rest.isEmpty()) {
                return text;
            }
            return text.charAt(0) + capitalizeFirst(rest.toLowerCase());
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
        } else if (kind.getRequiredPrefix() != null && !currentKey.startsWith(kind.getRequiredPrefix())) {
            errorMessage = kind.getPrefixErrorMessage();
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
