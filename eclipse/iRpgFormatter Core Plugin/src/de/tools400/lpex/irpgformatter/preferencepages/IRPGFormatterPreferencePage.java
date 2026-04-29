/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages;

import java.io.File;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.ui.dialogs.PreferencesUtil;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.preferencepages.keywordeditor.KeywordEditor;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.ParameterSpacingStyle;
import de.tools400.lpex.irpgformatter.preferences.PreferenceStoreProvider;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.preferences.PreferencesProfileManager;
import de.tools400.lpex.irpgformatter.preferences.PreferencesProfileManager.ProfileData;
import de.tools400.lpex.irpgformatter.utils.KeywordUtils;
import de.tools400.lpex.irpgformatter.utils.UIUtils;

/**
 * Preference page for RPGLE Formatter settings.
 */
public class IRPGFormatterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private Preferences preferences;
    private IPreferenceStore ibmPreferenceStore;
    private IPropertyChangeListener ibmPropertyChangeListener;

    private PreviewPanel previewPanel;

    /* IBM preferences */
    private Text startColumnText;
    private Text endColumnText;
    private Text spacesToIndentText;
    private Text casingStyleText;

    /* Local preferences */
    private Button useConstKeywordCheckbox;
    private Button putDelemiterBeforeParameterCheckbox;
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
    private Button executeIbmFormatterCheckbox;
    private Button executeIrpgFormatterCheckbox;
    private Button formatOnSaveCheckbox;

    private KeywordEditor dataTypesEditor;
    private KeywordEditor declarationTypesEditor;
    private KeywordEditor keywordsEditor;
    private KeywordEditor specialWordsEditor;

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
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }

    @Override
    protected Control createContents(Composite parent) {

        setTitle(getTitle() + " - " + IRpgleFormatterPlugin.getDefault().getVersion());

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Create settings container first (left column in grid)
        Composite settingsContainer = new Composite(container, SWT.NONE);
        settingsContainer.setLayout(new GridLayout(1, false));
        settingsContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Create preview panel second (right column in grid)
        previewPanel = new PreviewPanel(container, () -> {
            if (!isUIFullyInitialized()) return null;
            return createConfigFromUI();
        }, error -> setErrorMessage(error), preferences);

        // Populate settings panel (needs previewPanel for getPreviewUpdater)
        createKeywordsTabs(settingsContainer);

        // Load current values
        loadPreferences();

        // Listen for IBM preference changes
        ibmPreferenceStore = PreferenceStoreProvider.getIbmPreferenceStore();
        ibmPropertyChangeListener = new IPropertyChangeListener() {
            @Override
            public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
                if (event.getProperty().startsWith("RPGLE.FORMATTING.")
                    || event.getProperty().startsWith("com.ibm.etools.iseries.edit.preferences.parser.ilerpg.enter.autoclosecontrol")) {
                    loadIbmPreferences();
                    previewPanel.formatPreview();
                }
            }
        };
        ibmPreferenceStore.addPropertyChangeListener(ibmPropertyChangeListener);

        // Set focus to 1. line of preview editor
        previewPanel.setFocus();

        return container;
    }

    private boolean isUIFullyInitialized() {

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

        return true;
    }

    private FormatterConfig createConfigFromUI() {

        FormatterConfig config = FormatterConfig.fromPreferences();
        config.setMaxNameLength(maxNameLengthSpinner.getSelection());
        config.setMinLiteralLength(minNameLengthSpinner.getSelection());
        config.setAlignSubFields(alignSubFieldsCheckbox.getSelection());
        config.setBreakBeforeKeyword(breakBeforeKeywordCheckbox.getSelection());
        config.setDelimiterBeforeParameter(putDelemiterBeforeParameterCheckbox.getSelection());
        config.setBreakBetweenCaseChange(breakNameOnCaseChangeCheckbox.getSelection());
        config.setUseConstKeyword(useConstKeywordCheckbox.getSelection());
        config.setSortConstValueToEnd(sortConstValueToEndCheckbox.getSelection());
        config.setReplacePiName(replacePiNameCheckbox.getSelection());
        config.setRemoveEndProcName(removeEndProcNameCheckbox.getSelection());
        config.setUnindentCompilerDirectives(unindentCompilerDirectivesCheckbox.getSelection());
        config.setParameterSpacingStyle(ParameterSpacingStyle.fromLabel(parameterSpacingStyleCombo.getText()));
        config.setKeywords(KeywordUtils.entriesToMap(keywordsEditor.getEntries()));
        config.setDataTypes(KeywordUtils.entriesToMap(dataTypesEditor.getEntries()));
        config.setDeclarationTypes(KeywordUtils.entriesToMap(declarationTypesEditor.getEntries()));
        config.setSpecialWords(KeywordUtils.entriesToMap(specialWordsEditor.getEntries()));
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

        UIUtils.createLineSeparator(group);

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
        startColumnText = UIUtils.createReadOnlyText(parent, Messages.Label_IBM_Settings_Start_column, Messages.Tooltip_IBM_Settings_Start_column);
        startColumnText.setLayoutData(createTextGridData(60));

        // End column
        endColumnText = UIUtils.createReadOnlyText(parent, Messages.Label_IBM_Settings_End_column, Messages.Tooltip_IBM_Settings_End_column);
        endColumnText.setLayoutData(createTextGridData(60));

        // Indent length
        spacesToIndentText = UIUtils.createReadOnlyText(parent, Messages.Label_IBM_Settings_Indent, Messages.Tooltip_IBM_Settings_Indent);
        spacesToIndentText.setLayoutData(createTextGridData(60));
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
        casingStyleText = UIUtils.createReadOnlyText(parent, Messages.Label_Keyword_casing_style, Messages.Tooltip_Keyword_casing_style);
        casingStyleText.setLayoutData(createTextGridData(120));

        // Execute IBM formatter before iRPGFormatter
        executeIbmFormatterCheckbox = UIUtils.createCheckbox(parent, Messages.Label_Execute_IBM_formatter, Messages.Tooltip_Execute_IBM_formatter);
        executeIbmFormatterCheckbox.setLayoutData(createCheckboxGridData());
    }

    private void createGeneralSettingsGroup(Composite parent) {

        Group group = createSubGroup(parent, Messages.Label_General_Settings);

        SelectionListener previewUpdater = previewPanel.getPreviewUpdater();

        // Use const() keyword in dcl-c statements
        useConstKeywordCheckbox = UIUtils.createCheckbox(group, Messages.Label_Use_const_keyword, Messages.Tooltip_Use_const_keyword);
        useConstKeywordCheckbox.setLayoutData(createCheckboxGridData());
        useConstKeywordCheckbox.addSelectionListener(previewUpdater);

        // Place delimiter before parameter
        putDelemiterBeforeParameterCheckbox = UIUtils.createCheckbox(group, Messages.Label_Put_delimiter_before_parameter,
            Messages.Tooltip_Put_delimiter_before_parameter);
        putDelemiterBeforeParameterCheckbox.setLayoutData(createCheckboxGridData());
        putDelemiterBeforeParameterCheckbox.addSelectionListener(previewUpdater);

        // Parameter spacing style
        Label parameterSpacingLabel = new Label(group, SWT.NONE);
        parameterSpacingLabel.setText(Messages.Label_Parameter_spacing_style);

        parameterSpacingStyleCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
        parameterSpacingStyleCombo.setItems(preferences.getParameterSpacingStyles());
        parameterSpacingStyleCombo.setLayoutData(createStyleData());
        parameterSpacingStyleCombo.setToolTipText(Messages.Tooltip_Parameter_spacing_style);
        parameterSpacingStyleCombo.addSelectionListener(previewUpdater);

        // Align sub-fields/parameters
        alignSubFieldsCheckbox = UIUtils.createCheckbox(group, Messages.Label_Align_Sub_Fields, Messages.Tooltip_Align_Sub_Fields);
        alignSubFieldsCheckbox.setLayoutData(createCheckboxGridData());
        alignSubFieldsCheckbox.addSelectionListener(previewUpdater);

        // Break literal between case change
        breakNameOnCaseChangeCheckbox = UIUtils.createCheckbox(group, Messages.Label_Break_on_case_change, Messages.Tooltip_Break_on_case_change);
        breakNameOnCaseChangeCheckbox.setLayoutData(createCheckboxGridData());
        breakNameOnCaseChangeCheckbox.addSelectionListener(previewUpdater);

        // Break line before keyword
        breakBeforeKeywordCheckbox = UIUtils.createCheckbox(group, Messages.Label_Break_before_keyword, Messages.Tooltip_Break_before_keyword);
        breakBeforeKeywordCheckbox.setLayoutData(createCheckboxGridData());
        breakBeforeKeywordCheckbox.addSelectionListener(previewUpdater);

        // Sort const/value to end of sub-field declarations
        sortConstValueToEndCheckbox = UIUtils.createCheckbox(group, Messages.Label_Sort_const_value_to_end, Messages.Tooltip_Sort_const_value_to_end);
        sortConstValueToEndCheckbox.setLayoutData(createCheckboxGridData());
        sortConstValueToEndCheckbox.addSelectionListener(previewUpdater);

        // Replace dcl-pi name with *N
        replacePiNameCheckbox = UIUtils.createCheckbox(group, Messages.Label_Replace_pi_name, Messages.Tooltip_Replace_pi_name);
        replacePiNameCheckbox.setLayoutData(createCheckboxGridData());
        replacePiNameCheckbox.addSelectionListener(previewUpdater);

        // Remove end-proc name
        removeEndProcNameCheckbox = UIUtils.createCheckbox(group, Messages.Label_Remove_end_proc_name, Messages.Tooltip_Remove_end_proc_name);
        removeEndProcNameCheckbox.setLayoutData(createCheckboxGridData());
        removeEndProcNameCheckbox.addSelectionListener(previewUpdater);

        // Unindent compiler directives
        unindentCompilerDirectivesCheckbox = UIUtils.createCheckbox(group, Messages.Label_Unindent_compiler_directives,
            Messages.Tooltip_Unindent_compiler_directives);
        unindentCompilerDirectivesCheckbox.setLayoutData(createCheckboxGridData());
        unindentCompilerDirectivesCheckbox.addSelectionListener(previewUpdater);

        ModifyListener spinnerPreviewUpdater = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!previewPanel.isEditMode()) {
                    String comment = (String)e.widget.getData(PreviewPanel.PREVIEW_COMMENT_KEY);
                    previewPanel.formatPreview(comment);
                }
            }
        };

        // Maximum name length
        maxNameLengthSpinner = UIUtils.createSpinner(group, Messages.Label_Max_name_length, Messages.Tooltip_Max_name_length, 51, 100);
        maxNameLengthSpinner.addModifyListener(spinnerPreviewUpdater);
        maxNameLengthSpinner.setLayoutData(createSpinnerGridData());

        // Minimum name length
        minNameLengthSpinner = UIUtils.createSpinner(group, Messages.Label_Min_name_length, Messages.Tooltip_Min_name_length, 1, 50);
        minNameLengthSpinner.addModifyListener(spinnerPreviewUpdater);
        minNameLengthSpinner.setLayoutData(createSpinnerGridData());

        // Set preview comment tags on controls
        useConstKeywordCheckbox.setData(PreviewPanel.PREVIEW_COMMENT_KEY, "// Use const() in dcl-c statements.");
        putDelemiterBeforeParameterCheckbox.setData(PreviewPanel.PREVIEW_COMMENT_KEY, "// Put delimiter before parameter.");
        parameterSpacingStyleCombo.setData(PreviewPanel.PREVIEW_COMMENT_KEY, "// Put delimiter before parameter.");
        alignSubFieldsCheckbox.setData(PreviewPanel.PREVIEW_COMMENT_KEY, "// Align sub-fields/parameters.");
        breakNameOnCaseChangeCheckbox.setData(PreviewPanel.PREVIEW_COMMENT_KEY, "// Break name on case change.");
        breakBeforeKeywordCheckbox.setData(PreviewPanel.PREVIEW_COMMENT_KEY, "// Break before keyword.");
        sortConstValueToEndCheckbox.setData(PreviewPanel.PREVIEW_COMMENT_KEY, "// Sort const/value to end.");
        replacePiNameCheckbox.setData(PreviewPanel.PREVIEW_COMMENT_KEY, "// Replace dcl-pi name with *N.");
        removeEndProcNameCheckbox.setData(PreviewPanel.PREVIEW_COMMENT_KEY, "// Remove end-proc name.");
        unindentCompilerDirectivesCheckbox.setData(PreviewPanel.PREVIEW_COMMENT_KEY, "// Unindent compiler directives.");
        maxNameLengthSpinner.setData(PreviewPanel.PREVIEW_COMMENT_KEY, "// Break name on case change.");
        minNameLengthSpinner.setData(PreviewPanel.PREVIEW_COMMENT_KEY, "// Break name on case change.");

        // Execute iRPG formatter
        executeIrpgFormatterCheckbox = UIUtils.createCheckbox(group, Messages.Label_Execute_iRPG_formatter, Messages.Tooltip_Execute_iRPG_formatter);
        executeIrpgFormatterCheckbox.setLayoutData(createCheckboxGridData());
    }

    private void createSaveActionsGroup(Composite parent) {

        Group group = createSubGroup(parent, Messages.Label_Save_Actions);

        // Format on save
        formatOnSaveCheckbox = UIUtils.createCheckbox(group, Messages.Label_Format_on_save, Messages.Tooltip_Format_on_save);
        formatOnSaveCheckbox.setLayoutData(createCheckboxGridData());
    }

    private void createImportExportButtons(Composite parent) {

        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(2, false));
        buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button exportButton = UIUtils.createStandardButton(buttonComposite, Messages.Label_Export, Messages.Tooltip_Export);
        exportButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleExport();
            }
        });

        Button importButton = UIUtils.createStandardButton(buttonComposite, Messages.Label_Import, Messages.Tooltip_Import);
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
        dialog.setFilterExtensions(new String[] { "*.xml" });
        dialog.setFilterNames(new String[] { Messages.Label_XML_Files });
        dialog.setFileName("iRPGFormatter.xml");
        dialog.setOverwrite(true);

        String lastPath = getProfileFilePath();
        if (lastPath != null) {
            File lastFile = new File(lastPath);
            dialog.setFilterPath(lastFile.getParent());
            dialog.setFileName(lastFile.getName());
        }

        String filePath = dialog.open();
        if (filePath == null) {
            return;
        }

        saveProfileFilePath(filePath);

        try {
            ProfileData data = collectProfileDataFromUI();
            PreferencesProfileManager.exportProfile(filePath, data);
        } catch (Exception e) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, Messages.bind(Messages.Error_Export_failed_A, e.getLocalizedMessage()));
        }
    }

    private void handleImport() {

        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
        dialog.setText(Messages.Title_Import_Profile);
        dialog.setFilterExtensions(new String[] { "*.xml" });
        dialog.setFilterNames(new String[] { Messages.Label_XML_Files });

        String lastPath = getProfileFilePath();
        if (lastPath != null) {
            File lastFile = new File(lastPath);
            dialog.setFilterPath(lastFile.getParent());
            dialog.setFileName(lastFile.getName());
        }

        String filePath = dialog.open();
        if (filePath == null) {
            return;
        }

        saveProfileFilePath(filePath);

        try {
            ProfileData data = PreferencesProfileManager.importProfile(filePath);
            applyProfileDataToUI(data);
        } catch (IllegalArgumentException e) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, Messages.bind(Messages.Error_Invalid_profile_format_A, e.getLocalizedMessage()));
        } catch (Exception e) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, Messages.bind(Messages.Error_Import_failed_A, e.getLocalizedMessage()));
        }
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
        data.setDelimiterBeforeParameter(putDelemiterBeforeParameterCheckbox.getSelection());
        data.setAlignSubFields(alignSubFieldsCheckbox.getSelection());
        data.setBreakBetweenCaseChange(breakNameOnCaseChangeCheckbox.getSelection());
        data.setBreakBeforeKeyword(breakBeforeKeywordCheckbox.getSelection());
        data.setSortConstValueToEnd(sortConstValueToEndCheckbox.getSelection());
        data.setReplacePiName(replacePiNameCheckbox.getSelection());
        data.setRemoveEndProcName(removeEndProcNameCheckbox.getSelection());
        data.setUnindentCompilerDirectives(unindentCompilerDirectivesCheckbox.getSelection());
        data.setMaxNameLength(maxNameLengthSpinner.getSelection());
        data.setMinNameLength(minNameLengthSpinner.getSelection());
        data.setExecuteIbmFormatter(executeIbmFormatterCheckbox.getSelection());
        data.setExecuteIrpgFormatter(executeIrpgFormatterCheckbox.getSelection());
        data.setFormatOnSave(formatOnSaveCheckbox.getSelection());
        data.setDataTypes(KeywordUtils.entriesToMap(dataTypesEditor.getEntries()));
        data.setDeclarationTypes(KeywordUtils.entriesToMap(declarationTypesEditor.getEntries()));
        data.setKeywords(KeywordUtils.entriesToMap(keywordsEditor.getEntries()));
        data.setSpecialWords(KeywordUtils.entriesToMap(specialWordsEditor.getEntries()));

        // Custom preview source
        data.setCustomPreviewSource(previewPanel.getCustomSource());

        return data;
    }

    private void applyProfileDataToUI(ProfileData data) {

        // Scalar settings
        try {
            ParameterSpacingStyle style = ParameterSpacingStyle.valueOf(data.getParameterSpacingStyle());
            parameterSpacingStyleCombo.select(style.ordinal());
        } catch (IllegalArgumentException e) {
            // Keep current selection if the value is invalid
        }

        useConstKeywordCheckbox.setSelection(data.isUseConstKeyword());
        putDelemiterBeforeParameterCheckbox.setSelection(data.isDelimiterBeforeParameter());
        alignSubFieldsCheckbox.setSelection(data.isAlignSubFields());
        breakNameOnCaseChangeCheckbox.setSelection(data.isBreakBetweenCaseChange());
        breakBeforeKeywordCheckbox.setSelection(data.isBreakBeforeKeyword());
        sortConstValueToEndCheckbox.setSelection(data.isSortConstValueToEnd());
        replacePiNameCheckbox.setSelection(data.isReplacePiName());
        removeEndProcNameCheckbox.setSelection(data.isRemoveEndProcName());
        unindentCompilerDirectivesCheckbox.setSelection(data.isUnindentCompilerDirectives());
        maxNameLengthSpinner.setSelection(data.getMaxNameLength());
        minNameLengthSpinner.setSelection(data.getMinNameLength());
        executeIbmFormatterCheckbox.setSelection(data.isExecuteIbmFormatter());
        executeIrpgFormatterCheckbox.setSelection(data.isExecuteIrpgFormatter());
        formatOnSaveCheckbox.setSelection(data.isFormatOnSave());

        // Map settings (only apply if present in profile)
        Map<String, String> dataTypes = data.getDataTypes();
        if (dataTypes != null) {
            dataTypesEditor.load(KeywordUtils.mapToEntries(dataTypes));
        }

        Map<String, String> declarationTypes = data.getDeclarationTypes();
        if (declarationTypes != null) {
            declarationTypesEditor.load(KeywordUtils.mapToEntries(declarationTypes));
        }

        Map<String, String> keywords = data.getKeywords();
        if (keywords != null) {
            keywordsEditor.load(KeywordUtils.mapToEntries(keywords));
        }

        Map<String, String> specialWords = data.getSpecialWords();
        if (specialWords != null) {
            specialWordsEditor.load(KeywordUtils.mapToEntries(specialWords));
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

        formatOnSaveCheckbox.setSelection(preferences.isFormatOnSave());
        useConstKeywordCheckbox.setSelection(preferences.isUseConstKeyword());
        putDelemiterBeforeParameterCheckbox.setSelection(preferences.isDelimiterBeforeParameter());
        parameterSpacingStyleCombo.select(preferences.getParameterSpacingStyle().ordinal());
        alignSubFieldsCheckbox.setSelection(preferences.isAlignSubFields());
        breakNameOnCaseChangeCheckbox.setSelection(preferences.isBreakBetweenCaseChange());
        breakBeforeKeywordCheckbox.setSelection(preferences.isBreakBeforeKeyword());
        sortConstValueToEndCheckbox.setSelection(preferences.isSortConstValueToEnd());
        replacePiNameCheckbox.setSelection(preferences.isReplacePiName());
        removeEndProcNameCheckbox.setSelection(preferences.isRemoveEndProcName());
        unindentCompilerDirectivesCheckbox.setSelection(preferences.isUnindentCompilerDirectives());
        maxNameLengthSpinner.setSelection(preferences.getMaxNameLength());
        minNameLengthSpinner.setSelection(preferences.getMinNameLength());

        executeIbmFormatterCheckbox.setSelection(preferences.isExecuteIbmFormatter());
        executeIrpgFormatterCheckbox.setSelection(preferences.isExecuteIrpgFormatter());

        dataTypesEditor.load(KeywordUtils.mapToEntries(preferences.getDataTypes()));
        declarationTypesEditor.load(KeywordUtils.mapToEntries(preferences.getDeclarationTypes()));
        keywordsEditor.load(KeywordUtils.mapToEntries(preferences.getKeywords()));
        specialWordsEditor.load(KeywordUtils.mapToEntries(preferences.getSpecialWords()));

        previewPanel.loadSources(preferences.getDefaultPluginFormatterSource(), preferences.getCustomFormatterSource(),
            preferences.isCustomPreviewContent(), preferences.getFormatterPreviewVerticalRulerColumn());
    }

    private void loadIbmPreferences() {

        if (isDisposed()) {
            return;
        }

        startColumnText.setText(preferences.getStartColumnAsText());
        endColumnText.setText(preferences.getEndColumnAsText());
        spacesToIndentText.setText(Integer.toString(preferences.getIndent()));
        casingStyleText.setText(preferences.getKeywordCasingStyle().getDisplayName());
    }

    private void storePreferences() {

        preferences.setFormatOnSave(formatOnSaveCheckbox.getSelection());
        preferences.setUseConstKeyword(useConstKeywordCheckbox.getSelection());
        preferences.setDelimiterBeforeParameter(putDelemiterBeforeParameterCheckbox.getSelection());
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

        preferences.setExecuteIbmFormatter(executeIbmFormatterCheckbox.getSelection());
        preferences.setExecuteIrpgFormatter(executeIrpgFormatterCheckbox.getSelection());

        preferences.setDataTypes(KeywordUtils.entriesToMap(dataTypesEditor.getEntries()));
        preferences.setDeclarationTypes(KeywordUtils.entriesToMap(declarationTypesEditor.getEntries()));
        preferences.setKeywords(KeywordUtils.entriesToMap(keywordsEditor.getEntries()));
        preferences.setSpecialWords(KeywordUtils.entriesToMap(specialWordsEditor.getEntries()));

        previewPanel.storeState(preferences);
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();

        // Reset format on save
        formatOnSaveCheckbox.setSelection(preferences.getDefaultFormatOnSave());

        // Reset use const keyword
        useConstKeywordCheckbox.setSelection(preferences.getDefaultUseConstKeyword());
        putDelemiterBeforeParameterCheckbox.setSelection(preferences.getDefaultDelimiterBeforeParameter());
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

        // Reset execute IBM formatter
        executeIbmFormatterCheckbox.setSelection(preferences.getDefaultExecuteIbmFormatter());
        executeIrpgFormatterCheckbox.setSelection(preferences.getDefaultExecuteIrpgFormatter());

        // Reset keyword editors
        dataTypesEditor.load(KeywordUtils.mapToEntries(preferences.getDefaultDataTypes()));
        declarationTypesEditor.load(KeywordUtils.mapToEntries(preferences.getDefaultDeclarationTypes()));
        keywordsEditor.load(KeywordUtils.mapToEntries(preferences.getDefaultKeywords()));
        specialWordsEditor.load(KeywordUtils.mapToEntries(preferences.getDefaultSpecialWords()));

        previewPanel.resetToDefaults(preferences.getDefaultPluginFormatterSource(), preferences.getDefaultCustomFormatterSource(),
            preferences.getDefaultFormatterPreviewVerticalRulerColumn());
    }

    @Override
    public boolean performOk() {

        storePreferences();

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
}
