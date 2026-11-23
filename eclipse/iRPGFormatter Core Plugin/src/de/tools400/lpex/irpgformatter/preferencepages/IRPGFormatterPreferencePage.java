/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PreferencesUtil;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.preferences.PreferencesProfileManager.ProfileData;
import de.tools400.lpex.irpgformatter.preferences.SourcePreferences;
import de.tools400.lpex.irpgformatter.utils.ExceptionUtils;
import de.tools400.lpex.irpgformatter.utils.WidgetFactory;

/**
 * Preference page for RPGLE Formatter settings.
 * <p>
 * The page is also the property page of a project. There it edits the settings
 * of the project, if the project specific settings are enabled. Otherwise it
 * shows the settings of the workspace, which apply to the project. The settings
 * of the preview and the word lists always are those of the workspace. The IBM
 * settings of the workspace are those of RDi.
 * </p>
 * <p>
 * The controls of the settings and their mappings are in
 * {@link FormatterSettingsBlock}, the IBM settings in {@link IbmSettingsGroup}
 * and the import and export of profiles in {@link ProfileImportExport}. The
 * page keeps the settings of a project, the preview and the life cycle.
 * </p>
 */
public class IRPGFormatterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IWorkbenchPropertyPage {

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

    private IbmSettingsGroup ibmSettingsGroup;

    private FormatterSettingsBlock settingsBlock;

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
        previewPanel = new PreviewPanel(container, this::createPreviewConfig, error -> setErrorMessage(error), Preferences.getInstance());

        // Populate settings panel (needs previewPanel for addPreviewTrigger)
        settingsBlock = new FormatterSettingsBlock(previewPanel, hasWordLists(), this::refreshPreview);
        createTabs(settingsContainer);

        // Load current values
        loadPreferences();

        if (isPropertyPage()) {
            updateProjectSpecificSettings();
        }

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
                if (projectSpecificSettingsCheckbox.getSelection()) {
                    // The settings of RDi are the start of the IBM settings of
                    // the project. After the controls are enabled again, so
                    // that the group enables its toggle button itself.
                    ibmSettingsGroup.takeOverRdiSettings();
                }
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
            settingsBlock.load(Preferences.getInstance());
            ibmSettingsGroup.showRdiSettings();
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
            settingsBlock.load(Preferences.getInstance());
            refreshPreview();
        }
    }

    private void refreshPreview() {

        if (!previewPanel.isEditMode()) {
            previewPanel.formatPreview();
        }
    }

    /**
     * Returns the configuration of the preview with the settings and the IBM
     * settings of the page, or <code>null</code>, while the controls are
     * created.
     */
    private FormatterConfig createPreviewConfig() {

        if (settingsBlock == null || !settingsBlock.isInitialized() || ibmSettingsGroup == null) {
            return null;
        }

        FormatterConfig config = settingsBlock.createConfig();
        ibmSettingsGroup.applyTo(config);

        return config;
    }

    @Override
    public void dispose() {

        previewPanel.dispose();

        super.dispose();
    }

    /**
     * Creates the tab <i>Settings</i> and the tabs of the word lists.
     */
    private void createTabs(Composite parent) {

        TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
        GridData tabData = new GridData(GridData.FILL_BOTH);
        tabData.heightHint = 300;
        tabFolder.setLayoutData(tabData);

        // Settings tab
        TabItem settingsTab = new TabItem(tabFolder, SWT.NONE);
        settingsTab.setText(Messages.Label_Settings);
        settingsTab.setControl(createSettingsTab(tabFolder));

        settingsBlock.createWordListTabs(tabFolder);
    }

    private Control createSettingsTab(Composite parent) {

        ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.V_SCROLL);
        scrolled.setExpandHorizontal(true);
        scrolled.setExpandVertical(true);

        Composite container = new Composite(scrolled, SWT.NONE);
        container.setLayout(new GridLayout(1, false));

        // IBM settings group
        ibmSettingsGroup = new IbmSettingsGroup(isPropertyPage(), this::refreshPreview);
        ibmSettingsGroup.create(container);

        // General, experimental and save actions groups
        settingsBlock.createSettingGroups(container);

        // Import / Export buttons
        new ProfileImportExport(this::collectProfileData, this::applyProfileData).createButtons(container);

        scrolled.setContent(container);
        scrolled.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        return scrolled;
    }

    /**
     * Returns the settings and the IBM settings of the page and the custom
     * source of the preview as a profile.
     */
    private ProfileData collectProfileData() {

        ProfileData data = settingsBlock.collectProfileData();
        ibmSettingsGroup.collectProfileData(data);

        return data;
    }

    /**
     * Shows the settings of a profile. The IBM settings come first, because the
     * settings block formats the preview again.
     */
    private void applyProfileData(ProfileData data) {

        ibmSettingsGroup.applyProfileData(data);
        settingsBlock.applyProfileData(data);
    }

    private void loadPreferences() {

        if (isPropertyPage() && !preferences.isProjectSpecificSettings()) {
            settingsBlock.load(Preferences.getInstance());
            ibmSettingsGroup.showRdiSettings();
        } else {
            settingsBlock.load(preferences);
            ibmSettingsGroup.load(preferences);
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
     * Stores the settings of the page. The property page of a project either
     * stores the settings of the project or removes them. The IBM settings of
     * the page are written into the settings of RDi, if the user wants it.
     *
     * @return <code>false</code>, if the settings of the project or of RDi
     *         could not be saved
     */
    private boolean storePreferences() {

        // The preview belongs to the workspace.
        previewPanel.storeState(Preferences.getInstance());

        if (!isPropertyPage()) {
            settingsBlock.store(preferences);
        } else if (!storeProjectPreferences()) {
            return false;
        }

        if (!storeIbmSettingsOfRdi()) {
            return false;
        }

        // The page shows the stored IBM settings, e.g. after Apply.
        ibmSettingsGroup.load(preferences);

        return true;
    }

    /**
     * Stores or removes the settings of the project.
     *
     * @return <code>false</code>, if the settings of the project could not be
     *         saved
     */
    private boolean storeProjectPreferences() {

        // The settings are written into the node of the project, that the
        // formatter reads, before the node is saved: if saving fails, the
        // former settings are restored, so that Cancel does not leave the
        // settings of the page active.
        Map<String, String> formerSettings = preferences.getProjectSettingValues();

        if (projectSpecificSettingsCheckbox.getSelection()) {
            preferences.setProjectSpecificSettings();
            settingsBlock.store(preferences);
            // A project of a former version without IBM settings gets them
            // only, if the user takes them over.
            if (ibmSettingsGroup.getValues() != null) {
                preferences.setIbmSettingValues(ibmSettingsGroup.getValues());
            }
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
     * Writes the IBM settings of the page into the settings of RDi, if the user
     * wants it.
     *
     * @return <code>false</code>, if the settings of RDi could not be saved
     */
    private boolean storeIbmSettingsOfRdi() {

        if (!ibmSettingsGroup.isApplyToRdi()) {
            return true;
        }

        Preferences workspacePreferences = Preferences.getInstance();
        Map<String, String> formerIbmSettings = workspacePreferences.getIbmSettingValues();
        workspacePreferences.setIbmSettingValues(ibmSettingsGroup.getValues());

        try {
            workspacePreferences.save();
        } catch (IOException e) {
            // RDi uses its settings at once: restore the former ones, like the
            // settings of a project.
            workspacePreferences.setIbmSettingValues(formerIbmSettings);
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R,
                Messages.bind(Messages.Error_IBM_settings_not_saved_A, ExceptionUtils.getLocalizedMessage(e)));
            return false;
        }

        return true;
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

        settingsBlock.loadDefaults(preferences);

        // The IBM settings belong to RDi: a profile, that was imported, is no
        // longer written into them.
        ibmSettingsGroup.showRdiSettings();

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
}
