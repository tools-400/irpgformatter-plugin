/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.PreferenceStoreProvider;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.preferences.PreferencesProfileManager.ProfileData;
import de.tools400.lpex.irpgformatter.utils.WidgetFactory;

/**
 * The group <i>IBM settings</i> of the preference page. It shows the settings
 * of the IBM formatter, of the SQL formatting and of the key behavior, that
 * iRPGFormatter uses, read-only with links to their preference pages.
 * <p>
 * The preference page shows the settings of RDi and their new values, when they
 * change. The IBM settings of a profile, that is imported, are written into the
 * settings of RDi, when the page is saved, if the user agrees.
 * </p>
 * <p>
 * The property page of a project shows the IBM settings of the project. The
 * project takes them over from RDi, when its settings are enabled or with the
 * button <i>Take Over RDi Settings</i>. The button <i>Apply to RDi</i> writes
 * them into the settings of RDi, when the page is saved. The project of a
 * former version has no IBM settings: then the group shows the settings of RDi,
 * and saving the page does not add them to the project.
 * </p>
 */
public class IbmSettingsGroup implements IPropertyChangeListener {

    private final boolean projectPage;
    private final Runnable refreshPreview;

    /**
     * The IBM settings of the page or <code>null</code>, if the page shows the
     * settings of RDi.
     */
    private Map<String, String> values;

    /**
     * <code>true</code>, if the IBM settings of the page are written into the
     * settings of RDi, when the page is saved.
     */
    private boolean applyToRdi;

    private Group group;
    private Text startColumnText;
    private Text endColumnText;
    private Text spacesToIndentText;
    private Text selectIndentText;
    private Button preserveRelativeIndentCheckbox;
    private Button sqlFormattingCheckbox;
    private Text sqlIndentText;
    private Text sqlBreakOnCommaText;
    private Text sqlBreakOnAndOrText;
    private Button sqlUppercasingCheckbox;
    private Text sqlKeywordCasingText;
    private Text sqlIdentifierCasingText;
    private Text casingStyleText;
    private CLabel stateLabel;

    /** Only on the property page of a project */
    private Button applyToRdiButton;

    /**
     * Creates the group of the IBM settings.
     *
     * @param projectPage - <code>true</code> for the property page of a project
     * @param refreshPreview - formats the preview again, unless it is in edit
     *        mode
     */
    public IbmSettingsGroup(boolean projectPage, Runnable refreshPreview) {
        this.projectPage = projectPage;
        this.refreshPreview = refreshPreview;
    }

    /**
     * Creates the controls of the group. The group shows the changes of the IBM
     * settings of RDi, until it is disposed.
     */
    public void create(Composite parent) {

        group = WidgetFactory.createGroup(parent, Messages.Label_IBM_Settings);
        group.setLayout(new GridLayout(1, false));

        // The settings have two columns of equal width. If a long link spans
        // both columns, GridLayout makes them wider than the group. Therefore
        // the label of the state and the buttons, that take the full width,
        // are below the settings in a column of their own: in the columns of
        // the settings they would hide the border of the group.
        Composite settings = new Composite(group, SWT.NONE);
        GridLayout settingsLayout = new GridLayout(2, true);
        settingsLayout.marginWidth = 0;
        settingsLayout.marginHeight = 0;
        settings.setLayout(settingsLayout);
        settings.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        createFormatterSubGroup(settings);

        WidgetFactory.createLineSeparator(settings);

        createSqlFormattingSubGroup(settings);

        WidgetFactory.createLineSeparator(settings);

        createKeyBehaviorSubGroup(settings);

        createStateLabel(group);

        if (projectPage) {
            createProjectButtons(group);
        }

        PreferenceStoreProvider.getIbmPreferenceStore().addPropertyChangeListener(this);
        group.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                PreferenceStoreProvider.getIbmPreferenceStore().removePropertyChangeListener(IbmSettingsGroup.this);
            }
        });
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

        // Indent
        sqlIndentText = WidgetFactory.createReadOnlyText(parent, Messages.Label_IBM_Settings_SQL_Indent, Messages.Tooltip_IBM_Settings_SQL_Indent);
        sqlIndentText.setLayoutData(createTextGridData(60));

        // New line on comma
        sqlBreakOnCommaText = WidgetFactory.createReadOnlyText(parent, Messages.Label_IBM_Settings_SQL_Break_on_comma,
            Messages.Tooltip_IBM_Settings_SQL_Break_on_comma);
        sqlBreakOnCommaText.setLayoutData(createTextGridData(120));

        // New line on AND/OR
        sqlBreakOnAndOrText = WidgetFactory.createReadOnlyText(parent, Messages.Label_IBM_Settings_SQL_Break_on_and_or,
            Messages.Tooltip_IBM_Settings_SQL_Break_on_and_or);
        sqlBreakOnAndOrText.setLayoutData(createTextGridData(120));

        // Automatic uppercasing
        sqlUppercasingCheckbox = WidgetFactory.createReadOnlyCheckbox(parent, Messages.Label_IBM_Settings_SQL_Uppercasing,
            Messages.Tooltip_IBM_Settings_SQL_Uppercasing);

        // Casing of the keywords and built-in functions
        sqlKeywordCasingText = WidgetFactory.createReadOnlyText(parent, Messages.Label_IBM_Settings_SQL_Keyword_casing,
            Messages.Tooltip_IBM_Settings_SQL_Keyword_casing);
        sqlKeywordCasingText.setLayoutData(createTextGridData(120));

        // Casing of the identifiers
        sqlIdentifierCasingText = WidgetFactory.createReadOnlyText(parent, Messages.Label_IBM_Settings_SQL_Identifier_casing,
            Messages.Tooltip_IBM_Settings_SQL_Identifier_casing);
        sqlIdentifierCasingText.setLayoutData(createTextGridData(120));
    }

    private void createKeyBehaviorSubGroup(Composite parent) {

        createHelpLink(parent, Messages.Label_IBM_Key_Behavior_Settings_Help,
            "com.ibm.etools.iseries.edit.ui.preferences.ParserPreferencePageRPGILEKeyBehavior.RPGILE");

        // Keyword casing style
        casingStyleText = WidgetFactory.createReadOnlyText(parent, Messages.Label_Keyword_casing_style, Messages.Tooltip_Keyword_casing_style);
        casingStyleText.setLayoutData(createTextGridData(120));
    }

    /**
     * Creates the label, that tells, where the IBM settings come from and
     * whether they differ from the settings of RDi. It is hidden, while it has
     * no text.
     * <p>
     * The label has one line with the symbol of an information or of a warning
     * and takes the width of the group, but does not make the group wider. A
     * text, that does not fit, is shortened with an ellipsis and shown
     * completely in the tooltip.
     * </p>
     */
    private void createStateLabel(Composite parent) {

        stateLabel = new CLabel(parent, SWT.NONE);
        stateLabel.setMargins(0, 0, 0, 0);

        GridData stateData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        stateData.widthHint = 1;
        stateData.verticalIndent = getSectionIndent(stateLabel);
        stateData.exclude = true;
        stateLabel.setLayoutData(stateData);
        stateLabel.setVisible(false);
    }

    /**
     * Returns the space in front of the label of the state and the buttons:
     * half a line.
     */
    private int getSectionIndent(Control control) {
        return new PixelConverter(control).convertHeightInCharsToPixels(1) / 2;
    }

    /**
     * Shows the text with the symbol in the label of the state or hides the
     * label, if the text is empty. If the label is shown or hidden, the page is
     * laid out again.
     *
     * @param state - the text
     * @param imageKey - the key of the symbol in the image registry of JFace,
     *        e.g. {@link Dialog#DLG_IMG_MESSAGE_INFO}
     */
    private void showState(String state, String imageKey) {

        stateLabel.setImage(JFaceResources.getImage(imageKey));
        stateLabel.setText(state);
        stateLabel.setToolTipText(state.isEmpty() ? null : state);

        boolean visible = !state.isEmpty();
        GridData stateData = (GridData)stateLabel.getLayoutData();
        if (stateData.exclude != visible) {
            return;
        }

        stateData.exclude = !visible;
        stateLabel.setVisible(visible);

        Composite content = group.getParent();
        content.layout(true, true);
        if (content.getParent() instanceof ScrolledComposite) {
            ((ScrolledComposite)content.getParent()).setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        }
    }

    /**
     * Creates the button, that takes over the settings of RDi, and the toggle
     * button, that writes the settings of the project into the settings of RDi,
     * when the page is saved. The toggle button can be released again; it is
     * disabled, while the project has no IBM settings.
     */
    private void createProjectButtons(Composite parent) {

        Composite buttonComposite = new Composite(parent, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(2, false);
        buttonLayout.marginWidth = 0;
        buttonComposite.setLayout(buttonLayout);
        GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
        buttonData.verticalIndent = getSectionIndent(buttonComposite);
        buttonComposite.setLayoutData(buttonData);

        Button takeOverButton = WidgetFactory.createStandardButton(buttonComposite, Messages.Label_Take_over_RDi_settings,
            Messages.Tooltip_Take_over_RDi_settings);
        takeOverButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                takeOverRdiSettings();
                refreshPreview.run();
            }
        });

        applyToRdiButton = WidgetFactory.createStandardToggleButton(buttonComposite, Messages.Label_Apply_to_RDi, Messages.Tooltip_Apply_to_RDi);
        applyToRdiButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                applyToRdi = values != null && applyToRdiButton.getSelection();
                refresh();
            }
        });
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
     * Shows the IBM settings of the given preferences: those of a project, if
     * it has IBM settings of its own, otherwise those of RDi.
     *
     * @param preferences - the preferences of the workspace or of a project
     */
    public void load(Preferences preferences) {

        if (preferences.hasIbmSettings()) {
            values = preferences.getIbmSettingValues();
        } else {
            values = null;
        }
        applyToRdi = false;

        refresh();
    }

    /**
     * Shows the settings of RDi, e.g. when the settings of the project are
     * disabled. A profile, that was imported, is no longer written into the
     * settings of RDi.
     */
    public void showRdiSettings() {

        values = null;
        applyToRdi = false;

        refresh();
    }

    /**
     * Takes over the current settings of RDi as the IBM settings of the
     * project, e.g. when the settings of the project are enabled.
     */
    public void takeOverRdiSettings() {

        values = Preferences.getInstance().getIbmSettingValues();
        applyToRdi = false;

        refresh();
    }

    /**
     * Returns the IBM settings of the page, that the page saves.
     *
     * @return the values by the names of the IBM settings or <code>null</code>,
     *         if the page shows the settings of RDi
     */
    public Map<String, String> getValues() {
        return values;
    }

    /**
     * Returns <code>true</code>, if the IBM settings of the page are written
     * into the settings of RDi, when the page is saved.
     */
    public boolean isApplyToRdi() {
        return applyToRdi;
    }

    /**
     * Sets the IBM settings, that the group shows, in the configuration of the
     * preview.
     */
    public void applyTo(FormatterConfig config) {
        config.setIbmSettings(getShownPreferences());
    }

    /**
     * Adds the IBM settings, that the group shows, to the profile, that is
     * exported.
     */
    public void collectProfileData(ProfileData data) {
        data.setIbmSettings(getShownPreferences().getIbmSettingValues());
    }

    /**
     * Shows the IBM settings of a profile, that is imported. They replace the
     * IBM settings of a project. The preference page asks, whether they are
     * written into the settings of RDi, when they differ. A profile without IBM
     * settings, e.g. of a former version, changes nothing.
     *
     * @param data - the profile, whose IBM settings are those of the file only
     */
    public void applyProfileData(ProfileData data) {

        Map<String, String> imported = data.getIbmSettings();
        if (imported == null || imported.isEmpty()) {
            return;
        }

        Map<String, String> merged = new LinkedHashMap<>(getShownPreferences().getIbmSettingValues());
        merged.putAll(imported);

        if (!projectPage) {
            String differences = getDifferences(Preferences.forIbmSettingValues(merged));
            if (differences.isEmpty()) {
                showRdiSettings();
                return;
            }
            if (!MessageDialog.openQuestion(group.getShell(), Messages.Label_IBM_Settings,
                Messages.bind(Messages.Question_Apply_IBM_settings_of_profile_to_RDi_A, differences))) {
                // The last answer counts: a profile, that was imported before,
                // is no longer written into the settings of RDi either.
                showRdiSettings();
                return;
            }
        }

        values = merged;
        applyToRdi = !projectPage;

        refresh();
    }

    /**
     * Returns the preferences with the IBM settings, that the group shows.
     */
    private Preferences getShownPreferences() {

        if (values == null) {
            return Preferences.getInstance();
        }

        return Preferences.forIbmSettingValues(values);
    }

    /**
     * Shows the IBM settings of the page or of RDi and where they come from.
     */
    private void refresh() {

        if (isDisposed()) {
            return;
        }

        Preferences shown = getShownPreferences();

        startColumnText.setText(shown.getStartColumnAsText());
        endColumnText.setText(shown.getEndColumnAsText());
        spacesToIndentText.setText(Integer.toString(shown.getIndent()));
        selectIndentText.setText(Integer.toString(shown.getSelectIndent()));
        preserveRelativeIndentCheckbox.setSelection(shown.isPreserveRelativeIndentOfContinuedLines());
        sqlFormattingCheckbox.setSelection(shown.isFormatEmbeddedSql());
        sqlIndentText.setText(Integer.toString(shown.getSqlIndent()));
        sqlBreakOnCommaText.setText(shown.getSqlBreakOnComma().getDisplayName());
        sqlBreakOnAndOrText.setText(shown.getSqlBreakOnAndOr().getDisplayName());
        sqlUppercasingCheckbox.setSelection(shown.isSqlUppercasing());
        sqlKeywordCasingText.setText(shown.getSqlKeywordCasingStyle().getDisplayName());
        sqlIdentifierCasingText.setText(shown.getSqlIdentifierCasingStyle().getDisplayName());
        casingStyleText.setText(shown.getKeywordCasingStyle().getDisplayName());

        if (applyToRdiButton != null) {
            applyToRdiButton.setSelection(applyToRdi);
            applyToRdiButton.setEnabled(values != null);
        }

        showState(shown);
    }

    /**
     * Shows, where the IBM settings come from and whether they differ from the
     * settings of RDi. A difference is a warning: the LPEX editor and the IBM
     * formatter use the settings of RDi. The other texts are information.
     */
    private void showState(Preferences shown) {

        if (values == null) {
            if (projectPage) {
                showState(Messages.Label_IBM_settings_not_stored_in_project, Dialog.DLG_IMG_MESSAGE_INFO);
            } else {
                showState("", Dialog.DLG_IMG_MESSAGE_INFO);
            }
            return;
        }

        if (applyToRdi) {
            showState(Messages.Label_IBM_settings_are_applied_to_RDi, Dialog.DLG_IMG_MESSAGE_INFO);
            return;
        }

        String differences = getDifferences(shown);
        if (differences.isEmpty()) {
            showState("", Dialog.DLG_IMG_MESSAGE_INFO);
        } else {
            showState(Messages.bind(Messages.Label_IBM_settings_differ_from_RDi_A, differences), Dialog.DLG_IMG_MESSAGE_WARNING);
        }
    }

    /**
     * Returns the labels of the IBM settings, whose values differ from the
     * settings of RDi, separated by commas, see
     * {@link Preferences#getIbmSettingDifferences(Preferences)}.
     *
     * @param preferences - the preferences with the IBM settings to compare
     * @return the labels or an empty string, if no value differs
     */
    private static String getDifferences(Preferences preferences) {

        List<String> labels = new ArrayList<>();
        for (String name : preferences.getIbmSettingDifferences(Preferences.getInstance())) {
            labels.add(getDifferenceLabel(name));
        }

        return String.join(", ", labels);
    }

    /**
     * Returns the label of an IBM setting for the list of the differences:
     * without the colon, and the label of an SQL setting starts with "SQL",
     * e.g. "SQL Indent" unlike "Number of spaces to indent by".
     */
    private static String getDifferenceLabel(String name) {

        switch (name) {
        case Preferences.IBM_START_COLUMN:
            return withoutColon(Messages.Label_IBM_Settings_Start_column);
        case Preferences.IBM_END_COLUMN:
            return withoutColon(Messages.Label_IBM_Settings_End_column);
        case Preferences.IBM_INDENT:
            return withoutColon(Messages.Label_IBM_Settings_Indent);
        case Preferences.IBM_SELECT_INDENT:
            return withoutColon(Messages.Label_IBM_Settings_Select_indent);
        case Preferences.IBM_PRESERVE_RELATIVE_INDENT:
            return withoutColon(Messages.Label_IBM_Settings_Preserve_relative_indent);
        case Preferences.IBM_SQL_FORMATTING:
            return ofSql(Messages.Label_IBM_Settings_SQL_Formatting);
        case Preferences.IBM_SQL_INDENT:
            return ofSql(Messages.Label_IBM_Settings_SQL_Indent);
        case Preferences.IBM_SQL_BREAK_ON_COMMA:
            return ofSql(Messages.Label_IBM_Settings_SQL_Break_on_comma);
        case Preferences.IBM_SQL_BREAK_ON_AND_OR:
            return ofSql(Messages.Label_IBM_Settings_SQL_Break_on_and_or);
        case Preferences.IBM_SQL_UPPERCASING:
            return ofSql(Messages.Label_IBM_Settings_SQL_Uppercasing);
        case Preferences.IBM_SQL_KEYWORD_CASING:
            return ofSql(Messages.Label_IBM_Settings_SQL_Keyword_casing);
        case Preferences.IBM_SQL_IDENTIFIER_CASING:
            return ofSql(Messages.Label_IBM_Settings_SQL_Identifier_casing);
        case Preferences.IBM_KEYWORD_CASING_STYLE:
            return withoutColon(Messages.Label_Keyword_casing_style);
        default:
            return name;
        }
    }

    private static String ofSql(String label) {
        return Messages.bind(Messages.Label_IBM_setting_of_SQL_A, withoutColon(label));
    }

    private static String withoutColon(String label) {
        return label.replaceAll("\\s*:\\s*$", "");
    }

    private boolean isDisposed() {

        if (startColumnText == null || startColumnText.isDisposed()) {
            return true;
        }

        return false;
    }

    /**
     * Shows the new values of RDi or, if the group shows the IBM settings of
     * the page, whether they differ from RDi. The preview panel listens to the
     * same store and formats the preview itself.
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {

        if (Preferences.getIbmSettingNames().contains(event.getProperty())) {
            refresh();
        }
    }
}
