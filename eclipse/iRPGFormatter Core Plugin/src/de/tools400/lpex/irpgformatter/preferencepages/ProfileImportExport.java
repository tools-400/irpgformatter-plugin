/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.preferences.PreferencesProfileManager;
import de.tools400.lpex.irpgformatter.preferences.PreferencesProfileManager.ProfileData;
import de.tools400.lpex.irpgformatter.utils.ExceptionUtils;
import de.tools400.lpex.irpgformatter.utils.WidgetFactory;

/**
 * The buttons <i>Export</i> and <i>Import</i> of the preference page, that
 * write the settings of the page into the XML file of a profile and read them
 * from it. The file dialog starts with the file used last.
 */
public class ProfileImportExport {

    /** Name of the section in the dialog settings, kept from the page. */
    private static final String DIALOG_SETTINGS_SECTION = "IRPGFormatterPreferencePage";
    private static final String DIALOG_SETTINGS_PROFILE_PATH = "profileFilePath";

    private final Supplier<ProfileData> collectProfileData;
    private final Consumer<ProfileData> applyProfileData;

    /**
     * Creates the import and export of profiles.
     *
     * @param collectProfileData - returns the settings of the page
     * @param applyProfileData - shows the settings of a profile in the page
     */
    public ProfileImportExport(Supplier<ProfileData> collectProfileData, Consumer<ProfileData> applyProfileData) {
        this.collectProfileData = collectProfileData;
        this.applyProfileData = applyProfileData;
    }

    /**
     * Creates the buttons <i>Export</i> and <i>Import</i>.
     */
    public void createButtons(Composite parent) {

        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(2, false));
        buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button exportButton = WidgetFactory.createStandardButton(buttonComposite, Messages.Label_Export, Messages.Tooltip_Export);
        exportButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleExport(exportButton.getShell());
            }
        });

        Button importButton = WidgetFactory.createStandardButton(buttonComposite, Messages.Label_Import, Messages.Tooltip_Import);
        importButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleImport(importButton.getShell());
            }
        });
    }

    private void handleExport(Shell shell) {

        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setText(Messages.Title_Export_Profile);
        dialog.setFileName("iRPGFormatter.xml");
        dialog.setOverwrite(true);

        String filePath = selectProfileFile(dialog);
        if (filePath == null) {
            return;
        }

        try {
            ProfileData data = collectProfileData.get();
            PreferencesProfileManager.exportProfile(filePath, data);
        } catch (Exception e) {
            // An unchecked exception is a bug: log its stack trace.
            if (e instanceof RuntimeException) {
                IRpgleFormatterPlugin.logError("Unexpected error while exporting a profile.", e);
            }
            MessageDialog.openError(shell, Messages.E_R_R_O_R, Messages.bind(Messages.Error_Export_failed_A, ExceptionUtils.getLocalizedMessage(e)));
        }
    }

    private void handleImport(Shell shell) {

        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setText(Messages.Title_Import_Profile);

        String filePath = selectProfileFile(dialog);
        if (filePath == null) {
            return;
        }

        try {
            // Settings missing in a partial profile keep the values of the
            // page.
            ProfileData data = collectProfileData.get();
            boolean isComplete = PreferencesProfileManager.importProfile(filePath, data);
            applyProfileData.accept(data);
            if (!isComplete) {
                MessageDialog.openInformation(shell, Messages.I_N_F_O_R_M_A_T_I_O_N, Messages.Info_Profile_does_not_contain_all_settings);
            }
        } catch (IllegalArgumentException e) {
            MessageDialog.openError(shell, Messages.E_R_R_O_R,
                Messages.bind(Messages.Error_Invalid_profile_format_A, ExceptionUtils.getLocalizedMessage(e)));
        } catch (Exception e) {
            // An unchecked exception is a bug: log its stack trace.
            if (e instanceof RuntimeException) {
                IRpgleFormatterPlugin.logError("Unexpected error while importing a profile.", e);
            }
            MessageDialog.openError(shell, Messages.E_R_R_O_R, Messages.bind(Messages.Error_Import_failed_A, ExceptionUtils.getLocalizedMessage(e)));
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
}
