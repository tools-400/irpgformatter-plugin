/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PreferencesUtil;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.preferencepages.keywordeditor.KeywordEditor;
import de.tools400.lpex.irpgformatter.preferences.CalculationFormatter;
import de.tools400.lpex.irpgformatter.preferences.ConditionParentheses;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.ParameterSpacingStyle;
import de.tools400.lpex.irpgformatter.preferences.PreferenceStoreProvider;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.preferences.PreferencesProfileManager;
import de.tools400.lpex.irpgformatter.preferences.PreferencesProfileManager.ProfileData;
import de.tools400.lpex.irpgformatter.preferences.SourcePreferences;
import de.tools400.lpex.irpgformatter.utils.ExceptionUtils;
import de.tools400.lpex.irpgformatter.utils.KeywordUtils;
import de.tools400.lpex.irpgformatter.utils.WidgetFactory;

/**
 * Preference page for RPGLE Formatter settings.
 * <p>
 * The page is also the property page of a project. There it edits the settings
 * of the project, if the project specific settings are enabled. Otherwise it
 * shows the settings of the workspace, which apply to the project. The settings
 * of the preview and the IBM settings always are those of the workspace.
 * </p>
 */
public class IRPGFormatterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IWorkbenchPropertyPage, IPropertyChangeListener {

    /** ID of the preference page, see plugin.xml */
    public static final String ID = "de.tools400.lpex.irpgformatter.preferencepages.IRPGFormatterPreferencePage";

    /**
     * The preferences, that the page edits: the preferences of the workspace or
     * of the project of the property page.
     */
    private Preferences preferences;

    /* Property page of a project */
    private IAdaptable element;
    private IProject project;
    private Button projectSpecificSettingsCheckbox;
    private Composite settingsContainer;
    private ControlEnableState settingsEnableState;

    private PreviewPanel previewPanel;

    /* IBM preferences */
    private Text startColumnText;
    private Text endColumnText;
    private Text spacesToIndentText;
    private Text selectIndentText;
    private Button preserveRelativeIndentCheckbox;
    private Button sqlFormattingCheckbox;
    private Button sqlUppercasingCheckbox;
    private Text casingStyleText;

    /* Local preferences */
    private Button useConstKeywordCheckbox;
    private Button putDelimiterBeforeParameterCheckbox;
    private Combo parameterSpacingStyleCombo;
    private Button alignSubFieldsCheckbox;
    private Button breakNameOnCaseChangeCheckbox;
    private Button breakBeforeKeywordCheckbox;
    private Button sortConstValueToEndCheckbox;
    private Button replacePiNameCheckbox;
    private Button removeEndProcNameCheckbox;
    private Button unindentCompilerDirectivesCheckbox;
    private Spinner maxNameLengthSpinner;
    private Spinner minNameLengthSpinner;
    private Button formatDeclarationsCheckbox;
    private Combo formatCalculationsCombo;
    private Button formatOnSaveCheckbox;
    private Button removeEmptyCommentLinesCheckbox;
    private Button removeEmptyLinesBeforeDclPiCheckbox;
    private Combo conditionParenthesesCombo;
    private Button formatOperationCodesAndBuiltInFunctionsCheckbox;
    private Button formatSpecialWordsOfCalculationsCheckbox;

    private KeywordEditor dataTypesEditor;
    private KeywordEditor declarationTypesEditor;
    private KeywordEditor keywordsEditor;
    private KeywordEditor specialWordsEditor;
    private KeywordEditor operationCodesEditor;
    private KeywordEditor builtInFunctionsEditor;

    /**
     * Creates the preference page.
     */
    public IRPGFormatterPreferencePage() {
        super();
        preferences = Preferences.getInstance();
    }

    @Override
    public void init(IWorkbench workbench) {
        // Nothing to initialize
    }

    @Override
    public IAdaptable getElement() {
        return element;
    }

    /**
     * Sets the project of the property page. The page then edits the
     * preferences of the project.
     */
    @Override
    public void setElement(IAdaptable element) {

        this.element = element;

        project = element.getAdapter(IProject.class);
        if (project != null) {
            preferences = SourcePreferences.getProjectPreferences(project);
        }
    }

    private boolean isPropertyPage() {
        return project != null;
    }

    /**
     * Returns <code>true</code>, if the page has the tabs of the word lists. A
     * project has no word lists of its own, so that its page has no tabs of
     * them.
     */
    private boolean hasWordLists() {
        return !isPropertyPage();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }

    @Override
    protected Control createContents(Composite parent) {

        setTitle(getTitle() + " - " + IRpgleFormatterPlugin.getDefault().getVersion());

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        if (isPropertyPage()) {
            createProjectSpecificSettingsHeader(container);
        }

        // Create settings container first (left column in grid)
        settingsContainer = new Composite(container, SWT.NONE);
        settingsContainer.setLayout(new GridLayout(1, false));
        settingsContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Create preview panel second (right column in grid)
        previewPanel = new PreviewPanel(container, () -> {
            if (!isUIFullyInitialized()) return null;
            return createConfigFromUI();
        }, error -> setErrorMessage(error), Preferences.getInstance());

        // Populate settings panel (needs previewPanel for addPreviewTrigger)
        createKeywordsTabs(settingsContainer);

        // Load current values
        loadPreferences();

        if (isPropertyPage()) {
            updateProjectSpecificSettings();
        }

        // Listen for IBM preference changes
        registerPropertyChangeListeners();

        // Set focus to 1. line of preview editor
        previewPanel.setFocus();

        return container;
    }

    /**
     * Creates the check box, that enables the settings of the project, and the
     * link to the settings of the workspace, like the property pages of JDT.
     */
    private void createProjectSpecificSettingsHeader(Composite parent) {

        Composite header = new Composite(parent, SWT.NONE);
        GridLayout headerLayout = new GridLayout(2, false);
        headerLayout.marginWidth = 0;
        header.setLayout(headerLayout);
        GridData headerData = new GridData(GridData.FILL_HORIZONTAL);
        headerData.horizontalSpan = 2;
        header.setLayoutData(headerData);

        projectSpecificSettingsCheckbox = WidgetFactory.createCheckbox(header, Messages.Label_Enable_project_specific_settings,
            Messages.Tooltip_Enable_project_specific_settings);
        projectSpecificSettingsCheckbox.setSelection(preferences.isProjectSpecificSettings());
        projectSpecificSettingsCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateProjectSpecificSettings();
            }
        });

        Link workspaceSettingsLink = new Link(header, SWT.NONE);
        workspaceSettingsLink.setText(Messages.Label_Configure_workspace_settings);
        workspaceSettingsLink.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
        workspaceSettingsLink.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openWorkspaceSettings();
            }
        });

        // The page has no tabs of the word lists: tell, where they are.
        Label workspaceSettingsHint = new Label(header, SWT.NONE);
        workspaceSettingsHint.setText(Messages.Label_Workspace_settings_of_project);
        GridData hintData = new GridData(GridData.FILL_HORIZONTAL);
        hintData.horizontalSpan = 2;
        workspaceSettingsHint.setLayoutData(hintData);

        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData separatorData = new GridData(GridData.FILL_HORIZONTAL);
        separatorData.horizontalSpan = 2;
        separator.setLayoutData(separatorData);
    }

    /**
     * Enables the settings, if the project has settings of its own. Otherwise
     * the page shows the settings of the workspace, that apply to the project,
     * and disables them.
     */
    private void updateProjectSpecificSettings() {

        if (projectSpecificSettingsCheckbox.getSelection()) {
            // The settings of the workspace are the start of the settings of
            // the project.
            if (settingsEnableState != null) {
                settingsEnableState.restore();
                settingsEnableState = null;
            }
        } else if (settingsEnableState == null) {
            loadSettings(Preferences.getInstance());
            settingsEnableState = ControlEnableState.disable(settingsContainer);
            refreshPreview();
        }
    }

    private void openWorkspaceSettings() {

        int result = PreferencesUtil.createPreferenceDialogOn(getShell(), ID, new String[] { ID }, null).open();
        if (result != Window.OK || projectSpecificSettingsCheckbox.isDisposed()) {
            return;
        }

        // The preview belongs to the workspace: take over its state, that the
        // dialog may have changed, so that OK does not write back the former
        // state.
        loadPreviewSources();

        // The page shows the settings of the workspace, that may have changed.
        if (!projectSpecificSettingsCheckbox.getSelection()) {
            loadSettings(Preferences.getInstance());
            refreshPreview();
        }
    }

    private void refreshPreview() {

        if (!previewPanel.isEditMode()) {
            previewPanel.formatPreview();
        }
    }

    private boolean isUIFullyInitialized() {

        // The check box of the save actions is created after the other
        // settings.
        if (!hasWordLists()) {
            return formatOnSaveCheckbox != null;
        }

        if (keywordsEditor == null || keywordsEditor.getEntries() == null) {
            return false;
        }

        if (dataTypesEditor == null || dataTypesEditor.getEntries() == null) {
            return false;
        }
        if (declarationTypesEditor == null || declarationTypesEditor.getEntries() == null) {
            return false;
        }

        if (specialWordsEditor == null || specialWordsEditor.getEntries() == null) {
            return false;
        }

        if (operationCodesEditor == null || operationCodesEditor.getEntries() == null) {
            return false;
        }

        if (builtInFunctionsEditor == null || builtInFunctionsEditor.getEntries() == null) {
            return false;
        }

        return true;
    }

    private FormatterConfig createConfigFromUI() {

        FormatterConfig config = FormatterConfig.fromPreferences();
        config.setMaxNameLength(maxNameLengthSpinner.getSelection());
        config.setMinLiteralLength(minNameLengthSpinner.getSelection());
        config.setAlignSubFields(alignSubFieldsCheckbox.getSelection());
        config.setBreakBeforeKeyword(breakBeforeKeywordCheckbox.getSelection());
        config.setDelimiterBeforeParameter(putDelimiterBeforeParameterCheckbox.getSelection());
        config.setBreakBetweenCaseChange(breakNameOnCaseChangeCheckbox.getSelection());
        config.setUseConstKeyword(useConstKeywordCheckbox.getSelection());
        config.setSortConstValueToEnd(sortConstValueToEndCheckbox.getSelection());
        config.setReplacePiName(replacePiNameCheckbox.getSelection());
        config.setRemoveEndProcName(removeEndProcNameCheckbox.getSelection());
        config.setUnindentCompilerDirectives(unindentCompilerDirectivesCheckbox.getSelection());
        config.setRemoveEmptyCommentLines(removeEmptyCommentLinesCheckbox.getSelection());
        config.setRemoveEmptyLinesBeforeDclPi(removeEmptyLinesBeforeDclPiCheckbox.getSelection());
        config.setConditionParentheses(ConditionParentheses.fromLabel(conditionParenthesesCombo.getText()));
        config.setFormatOperationCodesAndBuiltInFunctions(formatOperationCodesAndBuiltInFunctionsCheckbox.getSelection());
        config.setFormatSpecialWordsOfCalculations(formatSpecialWordsOfCalculationsCheckbox.getSelection());
        config.setFormatDeclarations(formatDeclarationsCheckbox.getSelection());
        // The IBM formatter cannot run in the preview, because it needs the
        // RPG model of an LPEX editor. iRPGFormatter indents the calculations
        // by the same IBM settings, so the preview shows its result instead.
        // It differs from the IBM formatter for inactive /IF branches, which
        // the IBM formatter leaves unchanged, and for sources with structural
        // errors, such as a missing ENDIF, which the IBM formatter does not
        // format at all.
        CalculationFormatter calculationFormatter = CalculationFormatter.fromLabel(formatCalculationsCombo.getText());
        if (calculationFormatter == CalculationFormatter.IBM_FORMATTER) {
            calculationFormatter = CalculationFormatter.IRPG_FORMATTER;
        }
        config.setCalculationFormatter(calculationFormatter);
        config.setParameterSpacingStyle(ParameterSpacingStyle.fromLabel(parameterSpacingStyleCombo.getText()));
        // Without tabs the config keeps the word lists of the workspace.
        if (hasWordLists()) {
            config.setKeywords(KeywordUtils.entriesToMap(keywordsEditor.getEntries()));
            config.setDataTypes(KeywordUtils.entriesToMap(dataTypesEditor.getEntries()));
            config.setDeclarationTypes(KeywordUtils.entriesToMap(declarationTypesEditor.getEntries()));
            config.setSpecialWords(KeywordUtils.entriesToMap(specialWordsEditor.getEntries()));
            config.setOperationCodes(KeywordUtils.entriesToMap(operationCodesEditor.getEntries()));
            config.setBuiltInFunctions(KeywordUtils.entriesToMap(builtInFunctionsEditor.getEntries()));
        }
        return config;
    }

    private boolean isDisposed() {

        if (startColumnText == null || startColumnText.isDisposed()) {
            return true;
        }

        return false;
    }

    @Override
    public void dispose() {

        previewPanel.dispose();

        disposePropertyChangeListeners();

        super.dispose();
    }

    private Group createSubGroup(Composite parent, String label) {

        Group group = new Group(parent, SWT.NONE);
        group.setText(label);
        group.setLayout(new GridLayout(2, true));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return group;
    }

    private void createIBMSettingsGroup(Composite parent) {

        Group group = createSubGroup(parent, Messages.Label_IBM_Settings);

        createIBMFormatterSettingsSubGroup(group);

        WidgetFactory.createLineSeparator(group);

        createIBMSqlFormattingSettingsSubGroup(group);

        WidgetFactory.createLineSeparator(group);

        createIBMKeyBehaviorSettingsSubGroup(group);
    }

    private void createIBMFormatterSettingsSubGroup(Composite parent) {

        // Hint
        Link ibmFormatterSettingsHelp = new Link(parent, SWT.NONE);
        ibmFormatterSettingsHelp.setText(Messages.Label_IBM_Formatter_Settings_Help);
        GridData helpData = new GridData();
        helpData.horizontalSpan = 2;
        ibmFormatterSettingsHelp.setLayoutData(helpData);
        ibmFormatterSettingsHelp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(getShell(),
                    "com.ibm.etools.iseries.edit.preferences.formatter.ISeriesFormatterPreferencePageRPGILE", null, null).open();
            }
        });

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

    private void createIBMSqlFormattingSettingsSubGroup(Composite parent) {

        // Hint
        Link ibmSqlFormattingSettingsHelp = new Link(parent, SWT.NONE);
        ibmSqlFormattingSettingsHelp.setText(Messages.Label_IBM_SQL_Formatting_Settings_Help);
        GridData helpData = new GridData();
        helpData.horizontalSpan = 2;
        ibmSqlFormattingSettingsHelp.setLayoutData(helpData);
        ibmSqlFormattingSettingsHelp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(getShell(),
                    "com.ibm.etools.iseries.edit.preferences.sql.ParserPreferencePageSQLRPGLEFormatting", null, null).open();
            }
        });

        // Formatting
        sqlFormattingCheckbox = WidgetFactory.createReadOnlyCheckbox(parent, Messages.Label_IBM_Settings_SQL_Formatting,
            Messages.Tooltip_IBM_Settings_SQL_Formatting);

        // Automatic uppercasing
        sqlUppercasingCheckbox = WidgetFactory.createReadOnlyCheckbox(parent, Messages.Label_IBM_Settings_SQL_Uppercasing,
            Messages.Tooltip_IBM_Settings_SQL_Uppercasing);
    }

    private void createIBMKeyBehaviorSettingsSubGroup(Composite parent) {

        // Hint
        Link ibmKeyBehaviorSettingsHelp = new Link(parent, SWT.NONE);
        ibmKeyBehaviorSettingsHelp.setText(Messages.Label_IBM_Key_Behavior_Settings_Help);
        GridData helpData = new GridData();
        helpData.horizontalSpan = 2;
        ibmKeyBehaviorSettingsHelp.setLayoutData(helpData);
        ibmKeyBehaviorSettingsHelp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(getShell(),
                    "com.ibm.etools.iseries.edit.ui.preferences.ParserPreferencePageRPGILEKeyBehavior.RPGILE", null, null).open();
            }
        });

        // Keyword casing style
        casingStyleText = WidgetFactory.createReadOnlyText(parent, Messages.Label_Keyword_casing_style, Messages.Tooltip_Keyword_casing_style);
        casingStyleText.setLayoutData(createTextGridData(120));
    }

    private void createGeneralSettingsGroup(Composite parent) {

        Group group = createSubGroup(parent, Messages.Label_General_Settings);

        useConstKeywordCheckbox = createSettingCheckbox(group, Messages.Label_Use_const_keyword, Messages.Tooltip_Use_const_keyword,
            PreviewComments.USE_CONST_KEYWORD);

        putDelimiterBeforeParameterCheckbox = createSettingCheckbox(group, Messages.Label_Put_delimiter_before_parameter,
            Messages.Tooltip_Put_delimiter_before_parameter, PreviewComments.PUT_DELIMITER_BEFORE_PARAMETER);

        parameterSpacingStyleCombo = createSettingCombo(group, Messages.Label_Parameter_spacing_style, Messages.Tooltip_Parameter_spacing_style,
            preferences.getParameterSpacingStyles(), PreviewComments.PUT_DELIMITER_BEFORE_PARAMETER);

        alignSubFieldsCheckbox = createSettingCheckbox(group, Messages.Label_Align_Sub_Fields, Messages.Tooltip_Align_Sub_Fields,
            PreviewComments.ALIGN_SUB_FIELDS);

        breakNameOnCaseChangeCheckbox = createSettingCheckbox(group, Messages.Label_Break_on_case_change, Messages.Tooltip_Break_on_case_change,
            PreviewComments.BREAK_NAME_ON_CASE_CHANGE);

        breakBeforeKeywordCheckbox = createSettingCheckbox(group, Messages.Label_Break_before_keyword, Messages.Tooltip_Break_before_keyword,
            PreviewComments.BREAK_BEFORE_KEYWORD);

        sortConstValueToEndCheckbox = createSettingCheckbox(group, Messages.Label_Sort_const_value_to_end, Messages.Tooltip_Sort_const_value_to_end,
            PreviewComments.SORT_CONST_VALUE_TO_END);

        replacePiNameCheckbox = createSettingCheckbox(group, Messages.Label_Replace_pi_name, Messages.Tooltip_Replace_pi_name,
            PreviewComments.REPLACE_PI_NAME);

        removeEndProcNameCheckbox = createSettingCheckbox(group, Messages.Label_Remove_end_proc_name, Messages.Tooltip_Remove_end_proc_name,
            PreviewComments.REMOVE_END_PROC_NAME);

        unindentCompilerDirectivesCheckbox = createSettingCheckbox(group, Messages.Label_Unindent_compiler_directives,
            Messages.Tooltip_Unindent_compiler_directives, PreviewComments.UNINDENT_COMPILER_DIRECTIVES);

        // Maximum name length
        maxNameLengthSpinner = WidgetFactory.createSpinner(group, Messages.Label_Max_name_length, Messages.Tooltip_Max_name_length, 51, 100);
        maxNameLengthSpinner.setLayoutData(createSpinnerGridData());
        previewPanel.addPreviewTrigger(maxNameLengthSpinner, PreviewComments.BREAK_NAME_ON_CASE_CHANGE);

        // Minimum name length
        minNameLengthSpinner = WidgetFactory.createSpinner(group, Messages.Label_Min_name_length, Messages.Tooltip_Min_name_length, 1, 50);
        minNameLengthSpinner.setLayoutData(createSpinnerGridData());
        previewPanel.addPreviewTrigger(minNameLengthSpinner, PreviewComments.BREAK_NAME_ON_CASE_CHANGE);

        // Format declarations
        formatDeclarationsCheckbox = createSettingCheckbox(group, Messages.Label_Format_declarations, Messages.Tooltip_Format_declarations, null);

        formatCalculationsCombo = createSettingCombo(group, Messages.Label_Format_calculations, Messages.Tooltip_Format_calculations,
            preferences.getCalculationFormatters(), PreviewComments.FORMAT_CALCULATIONS);
    }

    private void createSaveActionsGroup(Composite parent) {

        Group group = createSubGroup(parent, Messages.Label_Save_Actions);

        // Format on save
        formatOnSaveCheckbox = WidgetFactory.createCheckbox(group, Messages.Label_Format_on_save, Messages.Tooltip_Format_on_save);
        formatOnSaveCheckbox.setLayoutData(createCheckboxGridData());
    }

    private void createExperimentalSettingsGroup(Composite parent) {

        Group group = createSubGroup(parent, Messages.Label_Experimental_Settings);

        removeEmptyCommentLinesCheckbox = createSettingCheckbox(group, Messages.Label_Remove_empty_comment_lines,
            Messages.Tooltip_Remove_empty_comment_lines, PreviewComments.REMOVE_EMPTY_COMMENT_LINES);

        removeEmptyLinesBeforeDclPiCheckbox = createSettingCheckbox(group, Messages.Label_Remove_empty_lines_before_dcl_pi,
            Messages.Tooltip_Remove_empty_lines_before_dcl_pi, PreviewComments.REMOVE_EMPTY_LINES_BEFORE_DCL_PI);

        conditionParenthesesCombo = createSettingCombo(group, Messages.Label_Condition_parentheses, Messages.Tooltip_Condition_parentheses,
            preferences.getConditionParenthesesStyles(), PreviewComments.CONDITION_PARENTHESES);

        formatOperationCodesAndBuiltInFunctionsCheckbox = createSettingCheckbox(group, Messages.Label_Format_operation_codes_and_built_in_functions,
            Messages.Tooltip_Format_operation_codes_and_built_in_functions, PreviewComments.FORMAT_OPERATION_CODES_AND_BUILT_IN_FUNCTIONS);

        formatSpecialWordsOfCalculationsCheckbox = createSettingCheckbox(group, Messages.Label_Format_special_words_of_calculations,
            Messages.Tooltip_Format_special_words_of_calculations, PreviewComments.FORMAT_SPECIAL_WORDS_OF_CALCULATIONS);
    }

    /**
     * Creates a checkbox of a formatter setting, that updates the preview and
     * scrolls it to the given comment.
     */
    private Button createSettingCheckbox(Composite group, String label, String tooltip, String previewComment) {

        Button checkbox = WidgetFactory.createCheckbox(group, label, tooltip);
        checkbox.setLayoutData(createCheckboxGridData());
        previewPanel.addPreviewTrigger(checkbox, previewComment);

        return checkbox;
    }

    /**
     * Creates a combo box of a formatter setting with its label, that updates
     * the preview and scrolls it to the given comment. The label and the combo
     * box show the same tooltip.
     */
    private Combo createSettingCombo(Composite group, String label, String tooltip, String[] items, String previewComment) {

        Label comboLabel = new Label(group, SWT.NONE);
        comboLabel.setText(label);
        comboLabel.setToolTipText(tooltip);

        Combo combo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
        combo.setItems(items);
        combo.setLayoutData(createStyleData());
        combo.setToolTipText(tooltip);
        previewPanel.addPreviewTrigger(combo, previewComment);

        return combo;
    }

    private void createImportExportButtons(Composite parent) {

        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(2, false));
        buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button exportButton = WidgetFactory.createStandardButton(buttonComposite, Messages.Label_Export, Messages.Tooltip_Export);
        exportButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleExport();
            }
        });

        Button importButton = WidgetFactory.createStandardButton(buttonComposite, Messages.Label_Import, Messages.Tooltip_Import);
        importButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleImport();
            }
        });
    }

    private static final String DIALOG_SETTINGS_SECTION = "IRPGFormatterPreferencePage";
    private static final String DIALOG_SETTINGS_PROFILE_PATH = "profileFilePath";

    private void handleExport() {

        FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
        dialog.setText(Messages.Title_Export_Profile);
        dialog.setFileName("iRPGFormatter.xml");
        dialog.setOverwrite(true);

        String filePath = selectProfileFile(dialog);
        if (filePath == null) {
            return;
        }

        try {
            ProfileData data = collectProfileDataFromUI();
            PreferencesProfileManager.exportProfile(filePath, data);
        } catch (Exception e) {
            // An unchecked exception is a bug: log its stack trace.
            if (e instanceof RuntimeException) {
                IRpgleFormatterPlugin.logError("Unexpected error while exporting a profile.", e);
            }
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R,
                Messages.bind(Messages.Error_Export_failed_A, ExceptionUtils.getLocalizedMessage(e)));
        }
    }

    private void handleImport() {

        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
        dialog.setText(Messages.Title_Import_Profile);

        String filePath = selectProfileFile(dialog);
        if (filePath == null) {
            return;
        }

        try {
            // Settings missing in a partial profile keep the values of the
            // page.
            ProfileData data = collectProfileDataFromUI();
            boolean isComplete = PreferencesProfileManager.importProfile(filePath, data);
            applyProfileDataToUI(data);
            if (!isComplete) {
                MessageDialog.openInformation(getShell(), Messages.I_N_F_O_R_M_A_T_I_O_N, Messages.Info_Profile_does_not_contain_all_settings);
            }
        } catch (IllegalArgumentException e) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R,
                Messages.bind(Messages.Error_Invalid_profile_format_A, ExceptionUtils.getLocalizedMessage(e)));
        } catch (Exception e) {
            // An unchecked exception is a bug: log its stack trace.
            if (e instanceof RuntimeException) {
                IRpgleFormatterPlugin.logError("Unexpected error while importing a profile.", e);
            }
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R,
                Messages.bind(Messages.Error_Import_failed_A, ExceptionUtils.getLocalizedMessage(e)));
        }
    }

    /**
     * Lets the user select the XML file of a profile. Starts with the file used
     * last and remembers the selected file.
     *
     * @param dialog the dialog with style, title and the settings of the caller
     * @return the path of the file or <code>null</code>, if the user cancelled
     */
    private String selectProfileFile(FileDialog dialog) {

        dialog.setFilterExtensions(new String[] { "*.xml" });
        dialog.setFilterNames(new String[] { Messages.Label_XML_Files });

        String lastPath = getProfileFilePath();
        if (lastPath != null) {
            File lastFile = new File(lastPath);
            dialog.setFilterPath(lastFile.getParent());
            dialog.setFileName(lastFile.getName());
        }

        String filePath = dialog.open();
        if (filePath != null) {
            saveProfileFilePath(filePath);
        }

        return filePath;
    }

    private String getProfileFilePath() {
        IDialogSettings settings = getDialogSettingsSection();
        return settings.get(DIALOG_SETTINGS_PROFILE_PATH);
    }

    private void saveProfileFilePath(String filePath) {
        IDialogSettings settings = getDialogSettingsSection();
        settings.put(DIALOG_SETTINGS_PROFILE_PATH, filePath);
    }

    private IDialogSettings getDialogSettingsSection() {
        IDialogSettings root = IRpgleFormatterPlugin.getDefault().getDialogSettings();
        IDialogSettings section = root.getSection(DIALOG_SETTINGS_SECTION);
        if (section == null) {
            section = root.addNewSection(DIALOG_SETTINGS_SECTION);
        }
        return section;
    }

    private ProfileData collectProfileDataFromUI() {

        ProfileData data = new ProfileData();

        data.setParameterSpacingStyle(ParameterSpacingStyle.fromLabel(parameterSpacingStyleCombo.getText()).name());
        data.setUseConstKeyword(useConstKeywordCheckbox.getSelection());
        data.setDelimiterBeforeParameter(putDelimiterBeforeParameterCheckbox.getSelection());
        data.setAlignSubFields(alignSubFieldsCheckbox.getSelection());
        data.setBreakBetweenCaseChange(breakNameOnCaseChangeCheckbox.getSelection());
        data.setBreakBeforeKeyword(breakBeforeKeywordCheckbox.getSelection());
        data.setSortConstValueToEnd(sortConstValueToEndCheckbox.getSelection());
        data.setReplacePiName(replacePiNameCheckbox.getSelection());
        data.setRemoveEndProcName(removeEndProcNameCheckbox.getSelection());
        data.setUnindentCompilerDirectives(unindentCompilerDirectivesCheckbox.getSelection());
        data.setMaxNameLength(maxNameLengthSpinner.getSelection());
        data.setMinNameLength(minNameLengthSpinner.getSelection());
        data.setCalculationFormatter(CalculationFormatter.fromLabel(formatCalculationsCombo.getText()).name());
        data.setFormatDeclarations(formatDeclarationsCheckbox.getSelection());
        data.setFormatOnSave(formatOnSaveCheckbox.getSelection());
        data.setRemoveEmptyCommentLines(removeEmptyCommentLinesCheckbox.getSelection());
        data.setRemoveEmptyLinesBeforeDclPi(removeEmptyLinesBeforeDclPiCheckbox.getSelection());
        data.setConditionParentheses(ConditionParentheses.fromLabel(conditionParenthesesCombo.getText()).name());
        data.setFormatOperationCodesAndBuiltInFunctions(formatOperationCodesAndBuiltInFunctionsCheckbox.getSelection());
        data.setFormatSpecialWordsOfCalculations(formatSpecialWordsOfCalculationsCheckbox.getSelection());
        if (hasWordLists()) {
            data.setDataTypes(KeywordUtils.entriesToMap(dataTypesEditor.getEntries()));
            data.setDeclarationTypes(KeywordUtils.entriesToMap(declarationTypesEditor.getEntries()));
            data.setKeywords(KeywordUtils.entriesToMap(keywordsEditor.getEntries()));
            data.setSpecialWords(KeywordUtils.entriesToMap(specialWordsEditor.getEntries()));
            data.setOperationCodes(KeywordUtils.entriesToMap(operationCodesEditor.getEntries()));
            data.setBuiltInFunctions(KeywordUtils.entriesToMap(builtInFunctionsEditor.getEntries()));
        } else {
            // The word lists, that apply to the project.
            Preferences workspacePreferences = Preferences.getInstance();
            data.setDataTypes(workspacePreferences.getDataTypes());
            data.setDeclarationTypes(workspacePreferences.getDeclarationTypes());
            data.setKeywords(workspacePreferences.getKeywords());
            data.setSpecialWords(workspacePreferences.getSpecialWords());
            data.setOperationCodes(workspacePreferences.getOperationCodes());
            data.setBuiltInFunctions(workspacePreferences.getBuiltInFunctions());
        }

        // Custom preview source
        data.setCustomPreviewSource(previewPanel.getCustomSource());

        return data;
    }

    private void applyProfileDataToUI(ProfileData data) {

        // Scalar settings
        selectByName(parameterSpacingStyleCombo, ParameterSpacingStyle.class, data.getParameterSpacingStyle());
        useConstKeywordCheckbox.setSelection(data.isUseConstKeyword());
        putDelimiterBeforeParameterCheckbox.setSelection(data.isDelimiterBeforeParameter());
        alignSubFieldsCheckbox.setSelection(data.isAlignSubFields());
        breakNameOnCaseChangeCheckbox.setSelection(data.isBreakBetweenCaseChange());
        breakBeforeKeywordCheckbox.setSelection(data.isBreakBeforeKeyword());
        sortConstValueToEndCheckbox.setSelection(data.isSortConstValueToEnd());
        replacePiNameCheckbox.setSelection(data.isReplacePiName());
        removeEndProcNameCheckbox.setSelection(data.isRemoveEndProcName());
        unindentCompilerDirectivesCheckbox.setSelection(data.isUnindentCompilerDirectives());
        maxNameLengthSpinner.setSelection(data.getMaxNameLength());
        minNameLengthSpinner.setSelection(data.getMinNameLength());
        selectByName(formatCalculationsCombo, CalculationFormatter.class, data.getCalculationFormatter());
        formatDeclarationsCheckbox.setSelection(data.isFormatDeclarations());
        formatOnSaveCheckbox.setSelection(data.isFormatOnSave());
        removeEmptyCommentLinesCheckbox.setSelection(data.isRemoveEmptyCommentLines());
        removeEmptyLinesBeforeDclPiCheckbox.setSelection(data.isRemoveEmptyLinesBeforeDclPi());
        selectByName(conditionParenthesesCombo, ConditionParentheses.class, data.getConditionParentheses());
        formatOperationCodesAndBuiltInFunctionsCheckbox.setSelection(data.isFormatOperationCodesAndBuiltInFunctions());
        formatSpecialWordsOfCalculationsCheckbox.setSelection(data.isFormatSpecialWordsOfCalculations());

        // Map settings: a project takes over the settings of a profile, but
        // not its word lists.
        if (hasWordLists()) {
            dataTypesEditor.load(KeywordUtils.mapToEntries(data.getDataTypes()));
            declarationTypesEditor.load(KeywordUtils.mapToEntries(data.getDeclarationTypes()));
            keywordsEditor.load(KeywordUtils.mapToEntries(data.getKeywords()));
            specialWordsEditor.load(KeywordUtils.mapToEntries(data.getSpecialWords()));
            operationCodesEditor.load(KeywordUtils.mapToEntries(data.getOperationCodes()));
            builtInFunctionsEditor.load(KeywordUtils.mapToEntries(data.getBuiltInFunctions()));
        }

        // Custom preview source
        String customSource = data.getCustomPreviewSource();
        if (customSource != null) {
            previewPanel.setCustomSource(customSource);
        }

        // Refresh preview
        if (!previewPanel.isEditMode()) {
            previewPanel.formatPreview();
        }
    }

    /**
     * Selects the item of the constant with the given name, e.g. of a profile.
     * The items of the combo box are in the order of the constants. A name,
     * that is no constant, or <code>null</code> keeps the current selection.
     */
    private static <E extends Enum<E>> void selectByName(Combo combo, Class<E> type, String name) {

        try {
            combo.select(Enum.valueOf(type, name).ordinal());
        } catch (IllegalArgumentException | NullPointerException e) {
            // Keep the current selection
        }
    }

    private GridData createStyleData() {
        GridData styleData = new GridData();
        styleData.widthHint = 120;
        return styleData;
    }

    private void createKeywordsTabs(Composite parent) {

        TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
        GridData tabData = new GridData(GridData.FILL_BOTH);
        tabData.heightHint = 300;
        tabFolder.setLayoutData(tabData);

        // Settings tab
        TabItem settingsTab = new TabItem(tabFolder, SWT.NONE);
        settingsTab.setText(Messages.Label_Settings);
        settingsTab.setControl(createSettingsTab(tabFolder));

        if (!hasWordLists()) {
            return;
        }

        // Data Types tab
        dataTypesEditor = KeywordEditor.forDataTypes(tabFolder);
        TabItem dataTypesTab = new TabItem(tabFolder, SWT.NONE);
        dataTypesTab.setText(Messages.Label_Data_Types);
        dataTypesTab.setControl(dataTypesEditor.getControl());

        // Declaration Types tab
        declarationTypesEditor = KeywordEditor.forDeclarationTypes(tabFolder);
        TabItem declarationTypesTab = new TabItem(tabFolder, SWT.NONE);
        declarationTypesTab.setText(Messages.Label_Declaration_Types);
        declarationTypesTab.setControl(declarationTypesEditor.getControl());

        // Keywords tab
        keywordsEditor = KeywordEditor.forKeywords(tabFolder);
        TabItem keywordsTab = new TabItem(tabFolder, SWT.NONE);
        keywordsTab.setText(Messages.Label_Keywords);
        keywordsTab.setControl(keywordsEditor.getControl());

        // Keyword Parameters tab
        specialWordsEditor = KeywordEditor.forSpecialWords(tabFolder);
        TabItem paramsTab = new TabItem(tabFolder, SWT.NONE);
        paramsTab.setText(Messages.Label_Special_Words);
        paramsTab.setControl(specialWordsEditor.getControl());

        // Operation Codes tab
        operationCodesEditor = KeywordEditor.forOperationCodes(tabFolder);
        TabItem operationCodesTab = new TabItem(tabFolder, SWT.NONE);
        operationCodesTab.setText(Messages.Label_Operation_Codes);
        operationCodesTab.setControl(operationCodesEditor.getControl());

        // Built-in Functions tab
        builtInFunctionsEditor = KeywordEditor.forBuiltInFunctions(tabFolder);
        TabItem builtInFunctionsTab = new TabItem(tabFolder, SWT.NONE);
        builtInFunctionsTab.setText(Messages.Label_Built_in_Functions);
        builtInFunctionsTab.setControl(builtInFunctionsEditor.getControl());

        // The preview formats with the word lists of the tabs.
        for (KeywordEditor editor : new KeywordEditor[] { dataTypesEditor, declarationTypesEditor, keywordsEditor, specialWordsEditor,
            operationCodesEditor, builtInFunctionsEditor }) {
            editor.setChangeListener(this::refreshPreview);
        }
    }

    private Control createSettingsTab(Composite parent) {

        ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.V_SCROLL);
        scrolled.setExpandHorizontal(true);
        scrolled.setExpandVertical(true);

        Composite container = new Composite(scrolled, SWT.NONE);
        container.setLayout(new GridLayout(1, false));

        // IBM settings group
        createIBMSettingsGroup(container);

        // General settings group
        createGeneralSettingsGroup(container);

        // Experimental settings group
        createExperimentalSettingsGroup(container);

        // Save actions group
        createSaveActionsGroup(container);

        // Import / Export buttons
        createImportExportButtons(container);

        scrolled.setContent(container);
        scrolled.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        return scrolled;
    }

    private void loadPreferences() {

        loadIbmPreferences();

        if (isPropertyPage() && !preferences.isProjectSpecificSettings()) {
            loadSettings(Preferences.getInstance());
        } else {
            loadSettings(preferences);
        }

        loadPreviewSources();
    }

    /**
     * Loads the sources and the state of the preview. The preview belongs to
     * the workspace.
     */
    private void loadPreviewSources() {

        Preferences workspacePreferences = Preferences.getInstance();
        previewPanel.loadSources(workspacePreferences.getDefaultPluginFormatterSource(), workspacePreferences.getCustomFormatterSource(),
            workspacePreferences.isCustomPreviewContent(), workspacePreferences.getFormatterPreviewVerticalRulerColumn());
    }

    /**
     * Loads the settings of the formatter from the given preferences into the
     * page.
     */
    private void loadSettings(Preferences source) {

        formatOnSaveCheckbox.setSelection(source.isFormatOnSave());
        removeEmptyCommentLinesCheckbox.setSelection(source.isRemoveEmptyCommentLines());
        removeEmptyLinesBeforeDclPiCheckbox.setSelection(source.isRemoveEmptyLinesBeforeDclPi());
        conditionParenthesesCombo.select(source.getConditionParentheses().ordinal());
        formatOperationCodesAndBuiltInFunctionsCheckbox.setSelection(source.isFormatOperationCodesAndBuiltInFunctions());
        formatSpecialWordsOfCalculationsCheckbox.setSelection(source.isFormatSpecialWordsOfCalculations());
        useConstKeywordCheckbox.setSelection(source.isUseConstKeyword());
        putDelimiterBeforeParameterCheckbox.setSelection(source.isDelimiterBeforeParameter());
        parameterSpacingStyleCombo.select(source.getParameterSpacingStyle().ordinal());
        alignSubFieldsCheckbox.setSelection(source.isAlignSubFields());
        breakNameOnCaseChangeCheckbox.setSelection(source.isBreakBetweenCaseChange());
        breakBeforeKeywordCheckbox.setSelection(source.isBreakBeforeKeyword());
        sortConstValueToEndCheckbox.setSelection(source.isSortConstValueToEnd());
        replacePiNameCheckbox.setSelection(source.isReplacePiName());
        removeEndProcNameCheckbox.setSelection(source.isRemoveEndProcName());
        unindentCompilerDirectivesCheckbox.setSelection(source.isUnindentCompilerDirectives());
        maxNameLengthSpinner.setSelection(source.getMaxNameLength());
        minNameLengthSpinner.setSelection(source.getMinNameLength());

        formatCalculationsCombo.select(source.getCalculationFormatter().ordinal());
        formatDeclarationsCheckbox.setSelection(source.isFormatDeclarations());

        if (hasWordLists()) {
            dataTypesEditor.load(KeywordUtils.mapToEntries(source.getDataTypes()));
            declarationTypesEditor.load(KeywordUtils.mapToEntries(source.getDeclarationTypes()));
            keywordsEditor.load(KeywordUtils.mapToEntries(source.getKeywords()));
            specialWordsEditor.load(KeywordUtils.mapToEntries(source.getSpecialWords()));
            operationCodesEditor.load(KeywordUtils.mapToEntries(source.getOperationCodes()));
            builtInFunctionsEditor.load(KeywordUtils.mapToEntries(source.getBuiltInFunctions()));
        }
    }

    private void loadIbmPreferences() {

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

    /**
     * Stores the settings of the page. The property page of a project either
     * stores the settings of the project or removes them.
     *
     * @return <code>false</code>, if the settings of the project could not be
     *         saved
     */
    private boolean storePreferences() {

        // The preview belongs to the workspace.
        previewPanel.storeState(Preferences.getInstance());

        if (!isPropertyPage()) {
            storeSettings();
            return true;
        }

        // The settings are written into the node of the project, that the
        // formatter reads, before the node is saved: if saving fails, the
        // former settings are restored, so that Cancel does not leave the
        // settings of the page active.
        Map<String, String> formerSettings = preferences.getProjectSettingValues();

        if (projectSpecificSettingsCheckbox.getSelection()) {
            preferences.setProjectSpecificSettings();
            storeSettings();
        } else {
            preferences.removeProjectSpecificSettings();
        }

        try {
            preferences.save();
        } catch (IOException e) {
            preferences.setProjectSettingValues(formerSettings);
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R,
                Messages.bind(Messages.Error_Project_settings_not_saved_A, ExceptionUtils.getLocalizedMessage(e)));
            return false;
        }

        return true;
    }

    /**
     * Stores the settings of the formatter of the page into the preferences,
     * that the page edits.
     */
    private void storeSettings() {

        preferences.setFormatOnSave(formatOnSaveCheckbox.getSelection());
        preferences.setRemoveEmptyCommentLines(removeEmptyCommentLinesCheckbox.getSelection());
        preferences.setRemoveEmptyLinesBeforeDclPi(removeEmptyLinesBeforeDclPiCheckbox.getSelection());
        preferences.setConditionParentheses(ConditionParentheses.fromLabel(conditionParenthesesCombo.getText()));
        preferences.setFormatOperationCodesAndBuiltInFunctions(formatOperationCodesAndBuiltInFunctionsCheckbox.getSelection());
        preferences.setFormatSpecialWordsOfCalculations(formatSpecialWordsOfCalculationsCheckbox.getSelection());
        preferences.setUseConstKeyword(useConstKeywordCheckbox.getSelection());
        preferences.setDelimiterBeforeParameter(putDelimiterBeforeParameterCheckbox.getSelection());
        String style = parameterSpacingStyleCombo.getText();
        preferences.setParameterSpacingStyle(ParameterSpacingStyle.fromLabel(style));
        preferences.setAlignSubFields(alignSubFieldsCheckbox.getSelection());
        preferences.setBreakBetweenCaseChange(breakNameOnCaseChangeCheckbox.getSelection());
        preferences.setBreakBeforeKeyword(breakBeforeKeywordCheckbox.getSelection());
        preferences.setSortConstValueToEnd(sortConstValueToEndCheckbox.getSelection());
        preferences.setReplacePiName(replacePiNameCheckbox.getSelection());
        preferences.setRemoveEndProcName(removeEndProcNameCheckbox.getSelection());
        preferences.setUnindentCompilerDirectives(unindentCompilerDirectivesCheckbox.getSelection());
        preferences.setMaxNameLength(maxNameLengthSpinner.getSelection());
        preferences.setMinLiteralLength(minNameLengthSpinner.getSelection());

        preferences.setCalculationFormatter(CalculationFormatter.fromLabel(formatCalculationsCombo.getText()));
        preferences.setFormatDeclarations(formatDeclarationsCheckbox.getSelection());

        if (hasWordLists()) {
            preferences.setDataTypes(KeywordUtils.entriesToMap(dataTypesEditor.getEntries()));
            preferences.setDeclarationTypes(KeywordUtils.entriesToMap(declarationTypesEditor.getEntries()));
            preferences.setKeywords(KeywordUtils.entriesToMap(keywordsEditor.getEntries()));
            preferences.setSpecialWords(KeywordUtils.entriesToMap(specialWordsEditor.getEntries()));
            preferences.setOperationCodes(KeywordUtils.entriesToMap(operationCodesEditor.getEntries()));
            preferences.setBuiltInFunctions(KeywordUtils.entriesToMap(builtInFunctionsEditor.getEntries()));
        }
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();

        // Like the property pages of JDT: the settings of the workspace apply
        // to the project again.
        if (isPropertyPage()) {
            projectSpecificSettingsCheckbox.setSelection(false);
            updateProjectSpecificSettings();
            return;
        }

        // Reset format on save
        formatOnSaveCheckbox.setSelection(preferences.getDefaultFormatOnSave());
        removeEmptyCommentLinesCheckbox.setSelection(preferences.getDefaultRemoveEmptyCommentLines());
        removeEmptyLinesBeforeDclPiCheckbox.setSelection(preferences.getDefaultRemoveEmptyLinesBeforeDclPi());
        conditionParenthesesCombo.select(preferences.getDefaultConditionParentheses().ordinal());
        formatOperationCodesAndBuiltInFunctionsCheckbox.setSelection(preferences.getDefaultFormatOperationCodesAndBuiltInFunctions());
        formatSpecialWordsOfCalculationsCheckbox.setSelection(preferences.getDefaultFormatSpecialWordsOfCalculations());

        // Reset use const keyword
        useConstKeywordCheckbox.setSelection(preferences.getDefaultUseConstKeyword());
        putDelimiterBeforeParameterCheckbox.setSelection(preferences.getDefaultDelimiterBeforeParameter());
        parameterSpacingStyleCombo.select(preferences.getDefaultParameterSpacingStyle().ordinal());
        alignSubFieldsCheckbox.setSelection(preferences.getDefaultAlignSubFields());
        breakNameOnCaseChangeCheckbox.setSelection(preferences.getDefaultBreakBetweenCaseChange());
        breakBeforeKeywordCheckbox.setSelection(preferences.getDefaultBreakBeforeKeyword());
        sortConstValueToEndCheckbox.setSelection(preferences.getDefaultSortConstValueToEnd());
        replacePiNameCheckbox.setSelection(preferences.getDefaultReplacePiName());
        removeEndProcNameCheckbox.setSelection(preferences.getDefaultRemoveEndProcName());
        unindentCompilerDirectivesCheckbox.setSelection(preferences.getDefaultUnindentCompilerDirectives());
        maxNameLengthSpinner.setSelection(preferences.getDefaultMaxNameLength());
        minNameLengthSpinner.setSelection(preferences.getDefaultMinLiteralLength());

        // Reset formatters
        formatCalculationsCombo.select(preferences.getDefaultCalculationFormatter().ordinal());
        formatDeclarationsCheckbox.setSelection(preferences.getDefaultFormatDeclarations());

        // Reset keyword editors
        dataTypesEditor.load(KeywordUtils.mapToEntries(preferences.getDefaultDataTypes()));
        declarationTypesEditor.load(KeywordUtils.mapToEntries(preferences.getDefaultDeclarationTypes()));
        keywordsEditor.load(KeywordUtils.mapToEntries(preferences.getDefaultKeywords()));
        specialWordsEditor.load(KeywordUtils.mapToEntries(preferences.getDefaultSpecialWords()));
        operationCodesEditor.load(KeywordUtils.mapToEntries(preferences.getDefaultOperationCodes()));
        builtInFunctionsEditor.load(KeywordUtils.mapToEntries(preferences.getDefaultBuiltInFunctions()));

        previewPanel.resetToDefaults(preferences.getDefaultPluginFormatterSource(), preferences.getDefaultCustomFormatterSource(),
            preferences.getDefaultFormatterPreviewVerticalRulerColumn());
    }

    @Override
    public boolean performOk() {

        if (!storePreferences()) {
            return false;
        }

        return super.performOk();
    }

    private GridData createCheckboxGridData() {

        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;

        return gridData;
    }

    private GridData createSpinnerGridData() {

        GridData gridData = new GridData();
        gridData.widthHint = 30;

        return gridData;
    }

    private GridData createTextGridData(int widthHint) {

        GridData gridData = new GridData();
        gridData.widthHint = widthHint;

        return gridData;
    }

    // --------------------------------------------------------------------
    // Property change listener - IBM Preferences (Formatter/Key Behavior)
    // --------------------------------------------------------------------

    @Override
    public void propertyChange(PropertyChangeEvent event) {

        String property = event.getProperty();

        // The preview panel listens to the same store and formats the preview
        // itself. The page only shows the new values in its read-only fields.

        // ----------------------------------------------------
        // Remote Systems -> Remote Systems LPEX Editor ->
        // IBM i Parsers -> ILE RPG -> Formatter
        // ----------------------------------------------------

        if (property.startsWith("RPGLE.FORMATTING.")) {
            loadIbmPreferences();
        }

        // ----------------------------------------------------
        // Remote Systems -> Remote Systems LPEX Editor ->
        // IBM i Parsers -> ILE RPG -> Free-form SQL Formatting
        // ----------------------------------------------------

        if (property.startsWith("SQLRPGLE.")) {
            loadIbmPreferences();
        }

        // ----------------------------------------------------
        // Remote Systems -> Remote Systems LPEX Editor ->
        // IBM i Parsers -> ILE RPG -> Key Behavior
        // ----------------------------------------------------

        if (property.startsWith("com.ibm.etools.iseries.edit.preferences.parser.ilerpg.enter.autoclosecontrol")) {
            loadIbmPreferences();
        }
    }

    private void registerPropertyChangeListeners() {

        PreferenceStoreProvider.getIbmPreferenceStore().addPropertyChangeListener(this);
    }

    private void disposePropertyChangeListeners() {

        PreferenceStoreProvider.getIbmPreferenceStore().removePropertyChangeListener(this);
    }
}
