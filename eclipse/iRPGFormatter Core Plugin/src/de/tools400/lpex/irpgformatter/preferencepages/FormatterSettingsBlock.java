/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.preferencepages.keywordeditor.KeywordEditor;
import de.tools400.lpex.irpgformatter.preferences.CalculationFormatter;
import de.tools400.lpex.irpgformatter.preferences.ConditionParentheses;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.ParameterSpacingStyle;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.preferences.PreferencesProfileManager.ProfileData;
import de.tools400.lpex.irpgformatter.utils.KeywordUtils;
import de.tools400.lpex.irpgformatter.utils.WidgetFactory;

/**
 * The settings of the formatter on the preference page: the groups
 * <i>General</i>, <i>Experimental</i> and <i>Save actions</i> of the tab
 * <i>Settings</i> and the tabs of the word lists. It shows the settings of
 * given preferences, stores them and maps them to the configuration of the
 * preview and to a profile.
 */
public class FormatterSettingsBlock {

    private final PreviewPanel previewPanel;
    private final Preferences preferences;
    private final boolean hasWordLists;
    private final Runnable refreshPreview;

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
     * Creates the settings of the formatter.
     *
     * @param previewPanel - the preview, that the controls update
     * @param preferences - the preferences, that the page edits
     * @param hasWordLists - <code>true</code>, if the page has the tabs of the
     *        word lists; a project has none
     * @param refreshPreview - formats the preview again, unless it is in edit
     *        mode
     */
    public FormatterSettingsBlock(PreviewPanel previewPanel, Preferences preferences, boolean hasWordLists, Runnable refreshPreview) {
        this.previewPanel = previewPanel;
        this.preferences = preferences;
        this.hasWordLists = hasWordLists;
        this.refreshPreview = refreshPreview;
    }

    /**
     * Creates the groups <i>General</i>, <i>Experimental</i> and <i>Save
     * actions</i>.
     */
    public void createSettingGroups(Composite parent) {

        // General settings group
        createGeneralSettingsGroup(parent);

        // Experimental settings group
        createExperimentalSettingsGroup(parent);

        // Save actions group
        createSaveActionsGroup(parent);
    }

    private void createGeneralSettingsGroup(Composite parent) {

        Group group = WidgetFactory.createGroup(parent, Messages.Label_General_Settings);

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

        Group group = WidgetFactory.createGroup(parent, Messages.Label_Save_Actions);

        // Format on save
        formatOnSaveCheckbox = WidgetFactory.createCheckbox(group, Messages.Label_Format_on_save, Messages.Tooltip_Format_on_save);
        formatOnSaveCheckbox.setLayoutData(createCheckboxGridData());
    }

    private void createExperimentalSettingsGroup(Composite parent) {

        Group group = WidgetFactory.createGroup(parent, Messages.Label_Experimental_Settings);

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
     * Creates the tabs of the word lists, if the page has them.
     */
    public void createWordListTabs(TabFolder tabFolder) {

        if (!hasWordLists) {
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
            editor.setChangeListener(refreshPreview);
        }
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

    /**
     * Returns <code>true</code>, if all controls are created and the word lists
     * are loaded, so that the preview can be formatted.
     */
    public boolean isInitialized() {

        // The check box of the save actions is created after the other
        // settings.
        if (!hasWordLists) {
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

    /**
     * Returns the configuration of the formatter with the settings of the
     * controls, for the preview.
     */
    public FormatterConfig createConfig() {

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
        if (hasWordLists) {
            config.setKeywords(KeywordUtils.entriesToMap(keywordsEditor.getEntries()));
            config.setDataTypes(KeywordUtils.entriesToMap(dataTypesEditor.getEntries()));
            config.setDeclarationTypes(KeywordUtils.entriesToMap(declarationTypesEditor.getEntries()));
            config.setSpecialWords(KeywordUtils.entriesToMap(specialWordsEditor.getEntries()));
            config.setOperationCodes(KeywordUtils.entriesToMap(operationCodesEditor.getEntries()));
            config.setBuiltInFunctions(KeywordUtils.entriesToMap(builtInFunctionsEditor.getEntries()));
        }
        return config;
    }

    /**
     * Shows the settings of the formatter of the given preferences.
     */
    public void load(Preferences source) {

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

        if (hasWordLists) {
            dataTypesEditor.load(KeywordUtils.mapToEntries(source.getDataTypes()));
            declarationTypesEditor.load(KeywordUtils.mapToEntries(source.getDeclarationTypes()));
            keywordsEditor.load(KeywordUtils.mapToEntries(source.getKeywords()));
            specialWordsEditor.load(KeywordUtils.mapToEntries(source.getSpecialWords()));
            operationCodesEditor.load(KeywordUtils.mapToEntries(source.getOperationCodes()));
            builtInFunctionsEditor.load(KeywordUtils.mapToEntries(source.getBuiltInFunctions()));
        }
    }

    /**
     * Stores the settings of the formatter into the given preferences.
     */
    public void store(Preferences target) {

        target.setFormatOnSave(formatOnSaveCheckbox.getSelection());
        target.setRemoveEmptyCommentLines(removeEmptyCommentLinesCheckbox.getSelection());
        target.setRemoveEmptyLinesBeforeDclPi(removeEmptyLinesBeforeDclPiCheckbox.getSelection());
        target.setConditionParentheses(ConditionParentheses.fromLabel(conditionParenthesesCombo.getText()));
        target.setFormatOperationCodesAndBuiltInFunctions(formatOperationCodesAndBuiltInFunctionsCheckbox.getSelection());
        target.setFormatSpecialWordsOfCalculations(formatSpecialWordsOfCalculationsCheckbox.getSelection());
        target.setUseConstKeyword(useConstKeywordCheckbox.getSelection());
        target.setDelimiterBeforeParameter(putDelimiterBeforeParameterCheckbox.getSelection());
        String style = parameterSpacingStyleCombo.getText();
        target.setParameterSpacingStyle(ParameterSpacingStyle.fromLabel(style));
        target.setAlignSubFields(alignSubFieldsCheckbox.getSelection());
        target.setBreakBetweenCaseChange(breakNameOnCaseChangeCheckbox.getSelection());
        target.setBreakBeforeKeyword(breakBeforeKeywordCheckbox.getSelection());
        target.setSortConstValueToEnd(sortConstValueToEndCheckbox.getSelection());
        target.setReplacePiName(replacePiNameCheckbox.getSelection());
        target.setRemoveEndProcName(removeEndProcNameCheckbox.getSelection());
        target.setUnindentCompilerDirectives(unindentCompilerDirectivesCheckbox.getSelection());
        target.setMaxNameLength(maxNameLengthSpinner.getSelection());
        target.setMinLiteralLength(minNameLengthSpinner.getSelection());

        target.setCalculationFormatter(CalculationFormatter.fromLabel(formatCalculationsCombo.getText()));
        target.setFormatDeclarations(formatDeclarationsCheckbox.getSelection());

        if (hasWordLists) {
            target.setDataTypes(KeywordUtils.entriesToMap(dataTypesEditor.getEntries()));
            target.setDeclarationTypes(KeywordUtils.entriesToMap(declarationTypesEditor.getEntries()));
            target.setKeywords(KeywordUtils.entriesToMap(keywordsEditor.getEntries()));
            target.setSpecialWords(KeywordUtils.entriesToMap(specialWordsEditor.getEntries()));
            target.setOperationCodes(KeywordUtils.entriesToMap(operationCodesEditor.getEntries()));
            target.setBuiltInFunctions(KeywordUtils.entriesToMap(builtInFunctionsEditor.getEntries()));
        }
    }

    /**
     * Shows the default values of the settings of the formatter of the given
     * preferences.
     */
    public void loadDefaults(Preferences defaults) {

        // Reset format on save
        formatOnSaveCheckbox.setSelection(defaults.getDefaultFormatOnSave());
        removeEmptyCommentLinesCheckbox.setSelection(defaults.getDefaultRemoveEmptyCommentLines());
        removeEmptyLinesBeforeDclPiCheckbox.setSelection(defaults.getDefaultRemoveEmptyLinesBeforeDclPi());
        conditionParenthesesCombo.select(defaults.getDefaultConditionParentheses().ordinal());
        formatOperationCodesAndBuiltInFunctionsCheckbox.setSelection(defaults.getDefaultFormatOperationCodesAndBuiltInFunctions());
        formatSpecialWordsOfCalculationsCheckbox.setSelection(defaults.getDefaultFormatSpecialWordsOfCalculations());

        // Reset use const keyword
        useConstKeywordCheckbox.setSelection(defaults.getDefaultUseConstKeyword());
        putDelimiterBeforeParameterCheckbox.setSelection(defaults.getDefaultDelimiterBeforeParameter());
        parameterSpacingStyleCombo.select(defaults.getDefaultParameterSpacingStyle().ordinal());
        alignSubFieldsCheckbox.setSelection(defaults.getDefaultAlignSubFields());
        breakNameOnCaseChangeCheckbox.setSelection(defaults.getDefaultBreakBetweenCaseChange());
        breakBeforeKeywordCheckbox.setSelection(defaults.getDefaultBreakBeforeKeyword());
        sortConstValueToEndCheckbox.setSelection(defaults.getDefaultSortConstValueToEnd());
        replacePiNameCheckbox.setSelection(defaults.getDefaultReplacePiName());
        removeEndProcNameCheckbox.setSelection(defaults.getDefaultRemoveEndProcName());
        unindentCompilerDirectivesCheckbox.setSelection(defaults.getDefaultUnindentCompilerDirectives());
        maxNameLengthSpinner.setSelection(defaults.getDefaultMaxNameLength());
        minNameLengthSpinner.setSelection(defaults.getDefaultMinLiteralLength());

        // Reset formatters
        formatCalculationsCombo.select(defaults.getDefaultCalculationFormatter().ordinal());
        formatDeclarationsCheckbox.setSelection(defaults.getDefaultFormatDeclarations());

        // Reset keyword editors
        if (hasWordLists) {
            dataTypesEditor.load(KeywordUtils.mapToEntries(defaults.getDefaultDataTypes()));
            declarationTypesEditor.load(KeywordUtils.mapToEntries(defaults.getDefaultDeclarationTypes()));
            keywordsEditor.load(KeywordUtils.mapToEntries(defaults.getDefaultKeywords()));
            specialWordsEditor.load(KeywordUtils.mapToEntries(defaults.getDefaultSpecialWords()));
            operationCodesEditor.load(KeywordUtils.mapToEntries(defaults.getDefaultOperationCodes()));
            builtInFunctionsEditor.load(KeywordUtils.mapToEntries(defaults.getDefaultBuiltInFunctions()));
        }
    }

    /**
     * Returns the settings of the controls and the custom source of the preview
     * as a profile.
     */
    public ProfileData collectProfileData() {

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
        if (hasWordLists) {
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

    /**
     * Shows the settings of a profile and formats the preview again.
     */
    public void applyProfileData(ProfileData data) {

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
        if (hasWordLists) {
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

        refreshPreview.run();
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
}
