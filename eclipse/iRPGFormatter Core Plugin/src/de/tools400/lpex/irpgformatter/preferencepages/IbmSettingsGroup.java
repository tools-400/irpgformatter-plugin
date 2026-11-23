/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.preferences.PreferenceStoreProvider;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.utils.WidgetFactory;

/**
 * The group <i>IBM settings</i> of the preference page. It shows the settings
 * of the IBM formatter, of the SQL formatting and of the key behavior, that
 * iRPGFormatter uses, read-only with links to their preference pages, and shows
 * their new values, when they change.
 */
public class IbmSettingsGroup implements IPropertyChangeListener {

    private final Preferences preferences;

    private Text startColumnText;
    private Text endColumnText;
    private Text spacesToIndentText;
    private Text selectIndentText;
    private Button preserveRelativeIndentCheckbox;
    private Button sqlFormattingCheckbox;
    private Button sqlUppercasingCheckbox;
    private Text casingStyleText;

    /**
     * Creates the group of the IBM settings.
     *
     * @param preferences - the preferences, that the page edits; the IBM
     *        settings are always those of the workspace
     */
    public IbmSettingsGroup(Preferences preferences) {
        this.preferences = preferences;
    }

    /**
     * Creates the controls of the group.
     */
    public void create(Composite parent) {

        Group group = WidgetFactory.createGroup(parent, Messages.Label_IBM_Settings);

        createFormatterSubGroup(group);

        WidgetFactory.createLineSeparator(group);

        createSqlFormattingSubGroup(group);

        WidgetFactory.createLineSeparator(group);

        createKeyBehaviorSubGroup(group);
    }

    private void createFormatterSubGroup(Composite parent) {

        createHelpLink(parent, Messages.Label_IBM_Formatter_Settings_Help,
            "com.ibm.etools.iseries.edit.preferences.formatter.ISeriesFormatterPreferencePageRPGILE");

        // Start column
        startColumnText = WidgetFactory.createReadOnlyText(parent, Messages.Label_IBM_Settings_Start_column,
            Messages.Tooltip_IBM_Settings_Start_column);
        startColumnText.setLayoutData(createTextGridData(60));

        // End column
        endColumnText = WidgetFactory.createReadOnlyText(parent, Messages.Label_IBM_Settings_End_column, Messages.Tooltip_IBM_Settings_End_column);
        endColumnText.setLayoutData(createTextGridData(60));

        // Indent length
        spacesToIndentText = WidgetFactory.createReadOnlyText(parent, Messages.Label_IBM_Settings_Indent, Messages.Tooltip_IBM_Settings_Indent);
        spacesToIndentText.setLayoutData(createTextGridData(60));

        // Extra indent of WHEN/OTHER
        selectIndentText = WidgetFactory.createReadOnlyText(parent, Messages.Label_IBM_Settings_Select_indent,
            Messages.Tooltip_IBM_Settings_Select_indent);
        selectIndentText.setLayoutData(createTextGridData(60));

        // Preserve relative indentation of continued lines
        preserveRelativeIndentCheckbox = WidgetFactory.createReadOnlyCheckbox(parent, Messages.Label_IBM_Settings_Preserve_relative_indent,
            Messages.Tooltip_IBM_Settings_Preserve_relative_indent);
    }

    private void createSqlFormattingSubGroup(Composite parent) {

        createHelpLink(parent, Messages.Label_IBM_SQL_Formatting_Settings_Help,
            "com.ibm.etools.iseries.edit.preferences.sql.ParserPreferencePageSQLRPGLEFormatting");

        // Formatting
        sqlFormattingCheckbox = WidgetFactory.createReadOnlyCheckbox(parent, Messages.Label_IBM_Settings_SQL_Formatting,
            Messages.Tooltip_IBM_Settings_SQL_Formatting);

        // Automatic uppercasing
        sqlUppercasingCheckbox = WidgetFactory.createReadOnlyCheckbox(parent, Messages.Label_IBM_Settings_SQL_Uppercasing,
            Messages.Tooltip_IBM_Settings_SQL_Uppercasing);
    }

    private void createKeyBehaviorSubGroup(Composite parent) {

        createHelpLink(parent, Messages.Label_IBM_Key_Behavior_Settings_Help,
            "com.ibm.etools.iseries.edit.ui.preferences.ParserPreferencePageRPGILEKeyBehavior.RPGILE");

        // Keyword casing style
        casingStyleText = WidgetFactory.createReadOnlyText(parent, Messages.Label_Keyword_casing_style, Messages.Tooltip_Keyword_casing_style);
        casingStyleText.setLayoutData(createTextGridData(120));
    }

    /**
     * Creates the hint with the link to the given IBM preference page, that
     * spans both columns.
     */
    private void createHelpLink(Composite parent, String text, String preferencePageId) {

        Link link = new Link(parent, SWT.NONE);
        link.setText(text);
        GridData helpData = new GridData();
        helpData.horizontalSpan = 2;
        link.setLayoutData(helpData);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(link.getShell(), preferencePageId, null, null).open();
            }
        });
    }

    private GridData createTextGridData(int widthHint) {

        GridData gridData = new GridData();
        gridData.widthHint = widthHint;

        return gridData;
    }

    /**
     * Shows the current IBM settings.
     */
    public void load() {

        if (isDisposed()) {
            return;
        }

        startColumnText.setText(preferences.getStartColumnAsText());
        endColumnText.setText(preferences.getEndColumnAsText());
        spacesToIndentText.setText(Integer.toString(preferences.getIndent()));
        selectIndentText.setText(Integer.toString(preferences.getSelectIndent()));
        preserveRelativeIndentCheckbox.setSelection(preferences.isPreserveRelativeIndentOfContinuedLines());
        sqlFormattingCheckbox.setSelection(preferences.isFormatEmbeddedSql());
        sqlUppercasingCheckbox.setSelection(preferences.isSqlUppercasing());
        casingStyleText.setText(preferences.getKeywordCasingStyle().getDisplayName());
    }

    private boolean isDisposed() {

        if (startColumnText == null || startColumnText.isDisposed()) {
            return true;
        }

        return false;
    }

    /**
     * Starts listening to the changes of the IBM settings.
     */
    public void registerListener() {
        PreferenceStoreProvider.getIbmPreferenceStore().addPropertyChangeListener(this);
    }

    /**
     * Stops listening to the changes of the IBM settings.
     */
    public void dispose() {
        PreferenceStoreProvider.getIbmPreferenceStore().removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {

        String property = event.getProperty();

        // The preview panel listens to the same store and formats the preview
        // itself. The group only shows the new values in its read-only fields.

        // ----------------------------------------------------
        // Remote Systems -> Remote Systems LPEX Editor ->
        // IBM i Parsers -> ILE RPG -> Formatter
        // ----------------------------------------------------

        if (property.startsWith("RPGLE.FORMATTING.")) {
            load();
        }

        // ----------------------------------------------------
        // Remote Systems -> Remote Systems LPEX Editor ->
        // IBM i Parsers -> ILE RPG -> Free-form SQL Formatting
        // ----------------------------------------------------

        if (property.startsWith("SQLRPGLE.")) {
            load();
        }

        // ----------------------------------------------------
        // Remote Systems -> Remote Systems LPEX Editor ->
        // IBM i Parsers -> ILE RPG -> Key Behavior
        // ----------------------------------------------------

        if (property.startsWith("com.ibm.etools.iseries.edit.preferences.parser.ilerpg.enter.autoclosecontrol")) {
            load();
        }
    }
}
