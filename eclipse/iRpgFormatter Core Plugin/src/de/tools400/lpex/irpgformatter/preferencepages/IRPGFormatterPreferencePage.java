/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.MarginPainter;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.WhitespaceCharacterPainter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.themes.IThemeRegistry;
import org.eclipse.ui.progress.UIJob;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.formatter.RpgleFormatter;
import de.tools400.lpex.irpgformatter.input.TextLinesInput;
import de.tools400.lpex.irpgformatter.preferencepages.keywordeditor.AddKeywordDialog;
import de.tools400.lpex.irpgformatter.preferencepages.keywordeditor.KeywordContentProvider;
import de.tools400.lpex.irpgformatter.preferencepages.keywordeditor.KeywordEditingSupport;
import de.tools400.lpex.irpgformatter.preferencepages.keywordeditor.KeywordEntry;
import de.tools400.lpex.irpgformatter.preferencepages.keywordeditor.KeywordLabelProvider;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.ParameterSpacingStyle;
import de.tools400.lpex.irpgformatter.preferences.PreferenceStoreProvider;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.utils.KeywordUtils;

/**
 * Preference page for RPGLE Formatter settings.
 */
public class IRPGFormatterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private Preferences preferences;
    private IPreferenceStore ibmPreferenceStore;
    private IPropertyChangeListener ibmPropertyChangeListener;
    private SelectionAdapter previewUpdater;

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
    private Spinner maxNameLengthSpinner;
    private Spinner minNameLengthSpinner;
    private Button executeIbmFormatterCheckbox;
    private Button executeIrpgFormatterCheckbox;
    private Button formatOnSaveCheckbox;

    private KeywordEditor dataTypesEditor;
    private KeywordEditor declarationTypesEditor;
    private KeywordEditor keywordsEditor;
    private KeywordEditor specialWordsEditor;

    /* Preview panel */
    private Group previewGroup;
    private TextViewer previewViewer;
    private Button viewEditButton;
    private Button customPreviewButton;
    private Button showWhitespacesCheckbox;
    private Spinner previewVerticalRulerSpinner;

    private WhitespaceCharacterPainter whitespacePainter;
    private MarginPainter marginPainter;

    private Color marginColor;
    private boolean isEditMode;
    private boolean isCustomContent;
    private IDocumentListener previewDocumentListener;
    private Font monoFont;
    private Color overflowColor;

    private String rawDefaultSource = "";
    private String rawCustomSource = "";

    private VerticalRulerUpdaterJob rulerUpdateJob;

    /**
     * Creates the preference page.
     */
    public IRPGFormatterPreferencePage() {
        super();
        preferences = Preferences.getInstance();

        previewUpdater = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!isEditMode) {
                    fromatPreviewSource();
                }
            }
        };
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

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        createSettingsPanel(container);

        createPreviewPanel(container);

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
                    fromatPreviewSource();
                }
            }
        };
        ibmPreferenceStore.addPropertyChangeListener(ibmPropertyChangeListener);

        return container;
    }

    private void createSettingsPanel(Composite parent) {

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Tab folder for keywords
        createKeywordsTabs(container);
    }

    private void createPreviewPanel(Composite parent) {

        previewGroup = new Group(parent, SWT.NONE);
        previewGroup.setText(Messages.Label_Preview);
        previewGroup.setLayout(new GridLayout(1, false));
        previewGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        // TextViewer for source preview
        previewViewer = new TextViewer(previewGroup, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        StyledText styledText = previewViewer.getTextWidget();
        GridData viewerData = new GridData(GridData.FILL_BOTH);
        viewerData.widthHint = 400;
        viewerData.heightHint = 300;
        styledText.setLayoutData(viewerData);

        // Set monospace font
        FontData[] fontData = styledText.getFont().getFontData();
        monoFont = new Font(styledText.getDisplay(), "Courier New", fontData[0].getHeight(), SWT.NORMAL);
        styledText.setFont(monoFont);

        // Set initial content
        IDocument document = new Document(preferences.getDefaultFormatterSource());
        previewViewer.setDocument(document);
        previewViewer.setEditable(false);

        // Document listener (placeholder for future use)
        previewDocumentListener = new IDocumentListener() {
            @Override
            public void documentAboutToBeChanged(DocumentEvent event) {
                // placeholder
            }

            @Override
            public void documentChanged(DocumentEvent event) {
                // placeholder
            }
        };
        document.addDocumentListener(previewDocumentListener);

        // Highlight lines exceeding the configured line width
        overflowColor = getErrorTextColor(styledText);
        styledText.addLineStyleListener(new LineStyleListener() {
            @Override
            public void lineGetStyle(LineStyleEvent event) {
                if (previewVerticalRulerSpinner == null) {
                    return;
                }
                int maxWidth = previewVerticalRulerSpinner.getSelection();
                if (maxWidth > 0 && event.lineText.length() > maxWidth) {
                    StyleRange range = new StyleRange();
                    range.start = event.lineOffset;
                    range.length = event.lineText.length();
                    range.foreground = overflowColor;
                    event.styles = new StyleRange[] { range };
                }
            }
        });

        // Margin painter (vertical ruler at line width column)
        marginColor = new Color(styledText.getDisplay(), 192, 192, 192);
        marginPainter = new MarginPainter(previewViewer);
        marginPainter.setMarginRulerColor(marginColor);
        marginPainter.setMarginRulerColumn(60);
        previewViewer.addPainter(marginPainter);

        // Button bar
        Composite buttonBar = new Composite(previewGroup, SWT.NONE);
        buttonBar.setLayout(new GridLayout(6, false));
        buttonBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // "View/Edit raw code" toggle button
        viewEditButton = new Button(buttonBar, SWT.TOGGLE);
        viewEditButton.setText(Messages.Label_View_Edit_raw_code);
        viewEditButton.setToolTipText(Messages.Tooltip_View_Edit_raw_code);
        viewEditButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isEditMode = viewEditButton.getSelection();
                previewViewer.setEditable(isEditMode);
                if (isEditMode) {
                    // Entering edit mode: show raw/unformatted source
                    previewGroup.setText(Messages.Label_Preview_Edit);
                    previewViewer.getDocument().set(getCurrentRawSource());
                } else {
                    // Leaving edit mode: capture raw source, then format
                    setCurrentRawSource(previewViewer.getDocument().get());
                    fromatPreviewSource();
                    previewGroup.setText(Messages.Label_Preview);
                }
            }
        });

        // "Custom preview contents" toggle button
        customPreviewButton = new Button(buttonBar, SWT.TOGGLE);
        customPreviewButton.setText(Messages.Label_Custom_preview_contents);
        customPreviewButton.setToolTipText(Messages.Tooltip_Custom_preview_contents);
        customPreviewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                switchPreviewContent();
            }
        });
        customPreviewButton.addSelectionListener(previewUpdater);

        // "Show whitespaces" checkbox
        showWhitespacesCheckbox = new Button(buttonBar, SWT.CHECK);
        showWhitespacesCheckbox.setText(Messages.Label_Show_whitespaces);
        showWhitespacesCheckbox.setToolTipText(Messages.Tooltip_Show_whitespaces);
        showWhitespacesCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                toggleWhitespaceDisplay();
            }
        });

        // "Preview line width:" label + spinner
        Label lineWidthLabel = new Label(buttonBar, SWT.NONE);
        lineWidthLabel.setText(Messages.Label_Preview_line_width);

        previewVerticalRulerSpinner = new Spinner(buttonBar, SWT.BORDER);
        previewVerticalRulerSpinner.setToolTipText(Messages.Tooltip_Preview_line_width);

        // GridData previewVerticalRulerGridData = new
        // GridData(GridData.BEGINNING);
        // previewVerticalRulerGridData.widthHint = 40;
        // minNameLengthSpinner.setLayoutData(previewVerticalRulerGridData);

        previewVerticalRulerSpinner.setMinimum(0);
        previewVerticalRulerSpinner.setMaximum(500);
        previewVerticalRulerSpinner.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (rulerUpdateJob != null) {
                    rulerUpdateJob.cancel();
                }
                rulerUpdateJob = new VerticalRulerUpdaterJob();
                rulerUpdateJob.schedule(200);
            }
        });

        // "Reset preview line width" button
        Button resetLineWidthButton = new Button(buttonBar, SWT.PUSH);
        resetLineWidthButton.setToolTipText(Messages.Tooltip_Reset_preview_line_width);
        resetLineWidthButton.setImage(IRpgleFormatterPlugin.getImageDescriptor(IRpgleFormatterPlugin.IMAGE_RESET).createImage());
        Point resetSize = resetLineWidthButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        GridData resetGridData = new GridData();
        resetGridData.widthHint = reduceButtonSize(resetSize.x);
        resetGridData.heightHint = reduceButtonSize(resetSize.y);
        resetLineWidthButton.setLayoutData(resetGridData);
        resetLineWidthButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                previewVerticalRulerSpinner.setSelection(preferences.getDefaultFormatterPreviewVerticalRulerColumn());
            }
        });
    }

    private int reduceButtonSize(int size) {
        size = size * 4 / 5;
        return size;
    }

    private void fromatPreviewSource() {

        if (isDisposed()) {
            return;
        }

        if (!isUIFullyInitialized()) {
            return;
        }

        try {

            String rawSource = getCurrentRawSource();
            if (rawSource == null || rawSource.isEmpty()) {
                previewViewer.getDocument().set("");
                setErrorMessage(null);
                return;
            }

            FormatterConfig config = createConfigFromUI();
            RpgleFormatter previewFormatter = new RpgleFormatter(config);
            previewFormatter.setSourceLength(previewVerticalRulerSpinner.getSelection());

            String[] unformatted = rawSource.split("\n");
            String[] lines = previewFormatter.format(new TextLinesInput(unformatted), 0);

            if (previewFormatter.getErrorCount() == 0) {
                setErrorMessage(null);
            } else {
                setErrorMessage("Could not format preview source.");
            }
            String formattedSource = String.join("\n", lines);
            previewViewer.getDocument().set(formattedSource);

        } catch (Exception e) {
            setErrorMessage(e.getLocalizedMessage());
        }
    }

    private boolean isUIFullyInitialized() {

        if (keywordsEditor == null || keywordsEditor.entries == null) {
            return false;
        }

        if (dataTypesEditor == null || dataTypesEditor.entries == null) {
            return false;
        }
        if (declarationTypesEditor == null || declarationTypesEditor.entries == null) {
            return false;
        }

        if (specialWordsEditor == null || specialWordsEditor.entries == null) {
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
        config.setParameterSpacingStyle(ParameterSpacingStyle.fromLabel(parameterSpacingStyleCombo.getText()));
        config.setKeywords(KeywordUtils.entriesToMap(keywordsEditor.entries));
        config.setDataTypes(KeywordUtils.entriesToMap(dataTypesEditor.entries));
        config.setDeclarationTypes(KeywordUtils.entriesToMap(declarationTypesEditor.entries));
        config.setSpecialWords(KeywordUtils.entriesToMap(specialWordsEditor.entries));
        return config;
    }

    private void switchPreviewContent() {

        // If in edit mode, save current document text before switching
        if (isEditMode) {
            setCurrentRawSource(previewViewer.getDocument().get());
        }

        // Save status of "customPreviewButton"
        isCustomContent = customPreviewButton.getSelection();
        preferences.setCustomPreviewContent(isCustomContent);

        // Show the appropriate raw source in the document.
        // previewUpdater (attached separately) will format if not in edit mode.
        previewViewer.getDocument().set(getCurrentRawSource());
    }

    private void toggleWhitespaceDisplay() {
        if (showWhitespacesCheckbox.getSelection()) {
            whitespacePainter = new WhitespaceCharacterPainter(previewViewer);
            previewViewer.addPainter(whitespacePainter);
        } else {
            if (whitespacePainter != null) {
                whitespacePainter.deactivate(true);
                whitespacePainter = null;
            }
        }
    }

    private void updateMarginColumn(int column) {
        if (marginPainter != null) {
            marginPainter.setMarginRulerColumn(column);
            previewViewer.getTextWidget().redraw();
        }
    }

    private Color getErrorTextColor(StyledText styledText) {

        // Returns "RGB {255, 0, 0}" if set to its default value.
        IThemeRegistry themeRegistry = WorkbenchPlugin.getDefault().getThemeRegistry();
        RGB rgb = themeRegistry.findColor("ERROR_COLOR").getValue();

        return new Color(styledText.getDisplay(), rgb);
    }

    private String getCurrentRawSource() {
        return isCustomContent ? rawCustomSource : rawDefaultSource;
    }

    private void setCurrentRawSource(String source) {
        if (isCustomContent) {
            rawCustomSource = source;
        } else {
            rawDefaultSource = source;
        }
    }

    private boolean isDisposed() {

        if (startColumnText == null || startColumnText.isDisposed()) {
            return true;
        }

        return false;
    }

    @Override
    public void dispose() {

        if (whitespacePainter != null) {
            whitespacePainter.deactivate(true);
            whitespacePainter = null;
        }

        if (marginPainter != null) {
            marginPainter.deactivate(true);
            marginPainter = null;
        }

        if (marginColor != null && !marginColor.isDisposed()) {
            marginColor.dispose();
        }

        if (overflowColor != null && !overflowColor.isDisposed()) {
            overflowColor.dispose();
        }

        if (monoFont != null && !monoFont.isDisposed()) {
            monoFont.dispose();
        }

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

        createSeparator(group);

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
        startColumnText = createReadOnlyLabeledText(parent, Messages.Label_IBM_Settings_Start_column, 60);

        // End column
        endColumnText = createReadOnlyLabeledText(parent, Messages.Label_IBM_Settings_End_column, 60);

        // Indent length
        spacesToIndentText = createReadOnlyLabeledText(parent, Messages.Label_IBM_Settings_Indent, 60);
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
        casingStyleText = createReadOnlyLabeledText(parent, Messages.Label_Keyword_casing_style, 120);

        // Execute IBM formatter before iRPGFormatter
        executeIbmFormatterCheckbox = createCheckbox(parent, Messages.Label_Execute_IBM_formatter, Messages.Tooltip_Execute_IBM_formatter, null);
    }

    private void createGeneralSettingsGroup(Composite parent) {

        Group group = createSubGroup(parent, Messages.Label_General_Settings);

        // Use const() keyword in dcl-c statements
        useConstKeywordCheckbox = createCheckbox(group, Messages.Label_Use_const_keyword, Messages.Tooltip_Use_const_keyword, previewUpdater);

        // Place delimiter before parameter
        putDelemiterBeforeParameterCheckbox = createCheckbox(group, Messages.Label_Put_delimiter_before_parameter,
            Messages.Tooltip_Put_delimiter_before_parameter, previewUpdater);

        // Parameter spacing style
        Label parameterSpacingLabel = new Label(group, SWT.NONE);
        parameterSpacingLabel.setText(Messages.Label_Parameter_spacing_style);

        parameterSpacingStyleCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
        parameterSpacingStyleCombo.setItems(preferences.getParameterSpacingStyles());
        parameterSpacingStyleCombo.setLayoutData(createStyleData());
        parameterSpacingStyleCombo.setToolTipText(Messages.Tooltip_Parameter_spacing_style);
        parameterSpacingStyleCombo.addSelectionListener(previewUpdater);

        // Align sub-fields/parameters
        alignSubFieldsCheckbox = createCheckbox(group, Messages.Label_Align_Sub_Fields, Messages.Tooltip_Align_Sub_Fields, previewUpdater);

        // Break literal between case change
        breakNameOnCaseChangeCheckbox = createCheckbox(group, Messages.Label_Break_on_case_change, Messages.Tooltip_Break_on_case_change,
            previewUpdater);

        // Break line before keyword
        breakBeforeKeywordCheckbox = createCheckbox(group, Messages.Label_Break_before_keyword, Messages.Tooltip_Break_before_keyword,
            previewUpdater);

        // Sort const/value to end of sub-field declarations
        sortConstValueToEndCheckbox = createCheckbox(group, Messages.Label_Sort_const_value_to_end, Messages.Tooltip_Sort_const_value_to_end,
            previewUpdater);

        ModifyListener spinnerPreviewUpdater = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!isEditMode) {
                    fromatPreviewSource();
                }
            }
        };

        // Maximum name length
        maxNameLengthSpinner = createLabeledSpinner(group, Messages.Label_Max_name_length, Messages.Tooltip_Max_name_length, 51, 100, 30,
            spinnerPreviewUpdater);

        // Minimum name length
        minNameLengthSpinner = createLabeledSpinner(group, Messages.Label_Min_name_length, Messages.Tooltip_Min_name_length, 1, 50, 30,
            spinnerPreviewUpdater);

        // Execute iRPG formatter
        executeIrpgFormatterCheckbox = createCheckbox(group, Messages.Label_Execute_iRPG_formatter, Messages.Tooltip_Execute_iRPG_formatter, null);
    }

    private void createSaveActionsGroup(Composite parent) {

        Group group = createSubGroup(parent, Messages.Label_Save_Actions);

        // Format on save
        formatOnSaveCheckbox = createCheckbox(group, Messages.Label_Format_on_save, Messages.Tooltip_Format_on_save, null);
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
        dataTypesEditor = new KeywordEditor(Messages.Title_Add_Data_Type, Messages.Title_Duplicate_Data_Type,
            Messages.Error_A_data_type_with_key_A_already_exists, false);
        TabItem dataTypesTab = new TabItem(tabFolder, SWT.NONE);
        dataTypesTab.setText(Messages.Label_Data_Types);
        dataTypesTab.setControl(createEditorTab(tabFolder, dataTypesEditor));

        // Declaration Types tab
        declarationTypesEditor = new KeywordEditor(Messages.Title_Add_Declaration_Type, Messages.Title_Duplicate_Declaration_Type,
            Messages.Error_A_declaration_type_with_key_A_already_exists, false);
        TabItem declarationTypesTab = new TabItem(tabFolder, SWT.NONE);
        declarationTypesTab.setText(Messages.Label_Declaration_Types);
        declarationTypesTab.setControl(createEditorTab(tabFolder, declarationTypesEditor));

        // Keywords tab
        keywordsEditor = new KeywordEditor(Messages.Title_Add_Keyword, Messages.Title_Duplicate_Keyword,
            Messages.Error_A_keyword_with_key_A_already_exists, false);
        TabItem keywordsTab = new TabItem(tabFolder, SWT.NONE);
        keywordsTab.setText(Messages.Label_Keywords);
        keywordsTab.setControl(createEditorTab(tabFolder, keywordsEditor));

        // Keyword Parameters tab
        specialWordsEditor = new KeywordEditor(Messages.Title_Add_Special_Word, Messages.Title_Duplicate_Special_Word,
            Messages.Error_A_special_word_with_key_A_already_exists, true);
        TabItem paramsTab = new TabItem(tabFolder, SWT.NONE);
        paramsTab.setText(Messages.Label_Special_Words);
        paramsTab.setControl(createEditorTab(tabFolder, specialWordsEditor));
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

        scrolled.setContent(container);
        scrolled.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        return scrolled;
    }

    private Control createEditorTab(Composite parent, final KeywordEditor editor) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        // Create table viewer
        editor.tableViewer = createKeywordTableViewer(composite);
        GridData tableData = new GridData(GridData.FILL_BOTH);
        tableData.heightHint = 200;
        editor.tableViewer.getTable().setLayoutData(tableData);

        // Button panel
        Composite buttonPanel = new Composite(composite, SWT.NONE);
        buttonPanel.setLayout(new GridLayout(1, false));
        buttonPanel.setLayoutData(new GridData());

        // Add button
        Button addButton = new Button(buttonPanel, SWT.PUSH);
        addButton.setText(Messages.Label_Add);
        addButton.setLayoutData(new GridData());
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addEntry(editor);
            }
        });

        // Remove button
        editor.removeButton = new Button(buttonPanel, SWT.PUSH);
        editor.removeButton.setText(Messages.Label_Remove);
        editor.removeButton.setLayoutData(new GridData());
        editor.removeButton.setEnabled(false);
        editor.removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removeSelectedEntry(editor);
            }
        });

        // Enable/disable remove button based on selection
        editor.tableViewer.addSelectionChangedListener(event -> {
            editor.removeButton.setEnabled(!event.getSelection().isEmpty());
        });

        return composite;
    }

    /**
     * Adds a new entry to the given editor via dialog.
     */
    private void addEntry(KeywordEditor editor) {
        AddKeywordDialog dialog = new AddKeywordDialog(getShell(), editor.addDialogTitle, editor.isSpecialWord);
        if (dialog.open() == Window.OK) {
            String key = dialog.getKey();
            String value = dialog.getValue();

            // Check for duplicate
            for (KeywordEntry entry : editor.entries) {
                if (entry.getKey().equalsIgnoreCase(key)) {
                    MessageDialog.openWarning(getShell(), editor.duplicateDialogTitle, Messages.bind(editor.duplicateErrorMessage, key));
                    return;
                }
            }

            // Add new entry
            KeywordEntry newEntry = new KeywordEntry(key, value);
            editor.entries.add(newEntry);
            editor.entries.sort((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()));
            editor.tableViewer.refresh();
        }
    }

    /**
     * Removes the selected entry from the given editor.
     */
    private void removeSelectedEntry(KeywordEditor editor) {
        IStructuredSelection selection = editor.tableViewer.getStructuredSelection();
        if (!selection.isEmpty()) {
            KeywordEntry entry = (KeywordEntry)selection.getFirstElement();
            editor.entries.remove(entry);
            editor.tableViewer.refresh();
        }
    }

    /**
     * Creates a TableViewer for keyword entries.
     */
    private TableViewer createKeywordTableViewer(Composite parent) {
        TableViewer viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);

        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // Key column (read-only)
        TableViewerColumn keyColumn = new TableViewerColumn(viewer, SWT.NONE);
        keyColumn.getColumn().setText(Messages.ColumnLabel_Key_read_only);
        keyColumn.getColumn().setWidth(150);
        keyColumn.getColumn().setResizable(true);
        // No editing support - column is read-only

        // Value column (editable)
        TableViewerColumn valueColumn = new TableViewerColumn(viewer, SWT.NONE);
        valueColumn.getColumn().setText(Messages.ColumnLabel_Value_editable);
        valueColumn.getColumn().setWidth(150);
        valueColumn.getColumn().setResizable(true);
        valueColumn.setEditingSupport(new KeywordEditingSupport(viewer));

        // Set providers
        viewer.setContentProvider(new KeywordContentProvider());
        viewer.setLabelProvider(new KeywordLabelProvider());

        // Enable tooltips
        ColumnViewerToolTipSupport.enableFor(viewer);

        // Add sorting by key column
        TableColumn keyCol = keyColumn.getColumn();
        keyCol.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Toggle sort direction
                int direction = table.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
                table.setSortDirection(direction);
                table.setSortColumn(keyCol);
                viewer.refresh();
            }
        });

        return viewer;
    }

    private void loadPreferences() {

        rawDefaultSource = preferences.getDefaultPluginFormatterSource();
        rawCustomSource = preferences.getCustomFormatterSource();

        loadIbmPreferences();

        formatOnSaveCheckbox.setSelection(preferences.isFormatOnSave());
        useConstKeywordCheckbox.setSelection(preferences.isUseConstKeyword());
        putDelemiterBeforeParameterCheckbox.setSelection(preferences.isDelimiterBeforeParameter());
        parameterSpacingStyleCombo.select(preferences.getParameterSpacingStyle().ordinal());
        alignSubFieldsCheckbox.setSelection(preferences.isAlignSubFields());
        breakNameOnCaseChangeCheckbox.setSelection(preferences.isBreakBetweenCaseChange());
        breakBeforeKeywordCheckbox.setSelection(preferences.isBreakBeforeKeyword());
        sortConstValueToEndCheckbox.setSelection(preferences.isSortConstValueToEnd());
        maxNameLengthSpinner.setSelection(preferences.getMaxNameLength());
        minNameLengthSpinner.setSelection(preferences.getMinNameLength());

        executeIbmFormatterCheckbox.setSelection(preferences.isExecuteIbmFormatter());
        executeIrpgFormatterCheckbox.setSelection(preferences.isExecuteIrpgFormatter());

        dataTypesEditor.load(KeywordUtils.mapToEntries(preferences.getDataTypes()));
        declarationTypesEditor.load(KeywordUtils.mapToEntries(preferences.getDeclarationTypes()));
        keywordsEditor.load(KeywordUtils.mapToEntries(preferences.getKeywords()));
        specialWordsEditor.load(KeywordUtils.mapToEntries(preferences.getSpecialWords()));

        previewVerticalRulerSpinner.setSelection(preferences.getFormatterPreviewVerticalRulerColumn());

        setupPreview(preferences.isCustomPreviewContent());
    }

    private void resetPreviewToDefault() {
        setupPreview(false);
    }

    private void setupPreview(boolean customContent) {
        if (previewViewer != null) {
            isCustomContent = customContent;
            isEditMode = false;
            previewViewer.getDocument().set(getCurrentRawSource());
            if (customPreviewButton != null) {
                customPreviewButton.setSelection(isCustomContent);
            }
            if (viewEditButton != null) {
                viewEditButton.setSelection(false);
            }
            previewViewer.setEditable(false);
            previewGroup.setText(Messages.Label_Preview);
            fromatPreviewSource();
        }
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
        preferences.setMaxNameLength(maxNameLengthSpinner.getSelection());
        preferences.setMinLiteralLength(minNameLengthSpinner.getSelection());

        preferences.setExecuteIbmFormatter(executeIbmFormatterCheckbox.getSelection());
        preferences.setExecuteIrpgFormatter(executeIrpgFormatterCheckbox.getSelection());

        preferences.setDataTypes(KeywordUtils.entriesToMap(dataTypesEditor.entries));
        preferences.setDeclarationTypes(KeywordUtils.entriesToMap(declarationTypesEditor.entries));
        preferences.setKeywords(KeywordUtils.entriesToMap(keywordsEditor.entries));
        preferences.setSpecialWords(KeywordUtils.entriesToMap(specialWordsEditor.entries));

        preferences.setFormatterPreviewVerticalRulerColumn(previewVerticalRulerSpinner.getSelection());

        // Save custom preview source as raw/unformatted; never save default
        // source
        if (previewViewer != null) {
            if (isEditMode) {
                setCurrentRawSource(previewViewer.getDocument().get());
            }
            preferences.setCustomFormatterSource(rawCustomSource);
        }
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();

        rawDefaultSource = preferences.getDefaultPluginFormatterSource();
        rawCustomSource = preferences.getDefaultCustomFormatterSource();

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

        previewVerticalRulerSpinner.setSelection(preferences.getDefaultFormatterPreviewVerticalRulerColumn());

        resetPreviewToDefault();
    }

    @Override
    public boolean performOk() {

        storePreferences();

        return super.performOk();
    }

    /**
     * Creates a checkbox that spans 2 columns with label, tooltip, and an
     * optional selection listener.
     */
    public static Button createCheckbox(Composite parent, String label, String tooltip, SelectionListener listener) {

        Button button = new Button(parent, SWT.CHECK);
        button.setText(label);
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        button.setLayoutData(gridData);
        button.setToolTipText(tooltip);
        if (listener != null) {
            button.addSelectionListener(listener);
        }
        return button;
    }

    /**
     * Creates a labeled spinner (label + spinner side by side) with min/max
     * bounds, tooltip, and an optional modify listener.
     */
    private Spinner createLabeledSpinner(Composite parent, String label, String tooltip, int min, int max, int widthHint, ModifyListener listener) {

        Label spinnerLabel = new Label(parent, SWT.NONE);
        spinnerLabel.setText(label);

        Spinner spinner = new Spinner(parent, SWT.BORDER);
        spinner.setMinimum(min);
        spinner.setMaximum(max);
        GridData gridData = new GridData();
        gridData.widthHint = widthHint;
        spinner.setLayoutData(gridData);
        spinner.setToolTipText(tooltip);
        if (listener != null) {
            spinner.addModifyListener(listener);
        }
        return spinner;
    }

    /**
     * Creates a read-only text field with a label (label + text side by side).
     */
    private Text createReadOnlyLabeledText(Composite parent, String label, int widthHint) {

        Label textLabel = new Label(parent, SWT.NONE);
        textLabel.setText(label);

        Text text = new Text(parent, SWT.NONE);
        GridData gridData = new GridData();
        gridData.widthHint = widthHint;
        text.setLayoutData(gridData);
        text.setEditable(false);
        return text;
    }

    /**
     * Groups the per-tab state for a keyword editor tab.
     */
    private static class KeywordEditor {

        TableViewer tableViewer;
        List<KeywordEntry> entries;
        Button removeButton;
        final String addDialogTitle;
        final String duplicateDialogTitle;
        final String duplicateErrorMessage;
        final boolean isSpecialWord;

        KeywordEditor(String addDialogTitle, String duplicateDialogTitle, String duplicateErrorMessage, boolean isSpecialWord) {
            this.addDialogTitle = addDialogTitle;
            this.duplicateDialogTitle = duplicateDialogTitle;
            this.duplicateErrorMessage = duplicateErrorMessage;
            this.isSpecialWord = isSpecialWord;
        }

        void load(List<KeywordEntry> entries) {
            this.entries = entries;
            tableViewer.setInput(entries);
        }
    }

    private class VerticalRulerUpdaterJob extends UIJob {

        public VerticalRulerUpdaterJob() {
            super("");
        }

        @Override
        public IStatus runInUIThread(IProgressMonitor arg0) {

            if (!isDisposed()) {
                int column = previewVerticalRulerSpinner.getSelection();
                updateMarginColumn(column);
                if (!isEditMode) {
                    fromatPreviewSource();
                    // Move focus away and back to re-select the spinner text,
                    // since forceFocus() is a no-op when the spinner already
                    // has focus.
                    previewViewer.getControl().forceFocus();
                    previewVerticalRulerSpinner.forceFocus();
                }
            }

            return Status.OK_STATUS;
        }

    }

    private void createSeparator(Group group) {
        Label separator = new Label(group, SWT.NONE);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL_HORIZONTAL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 2;
        separator.setLayoutData(gridData);
    }
}
