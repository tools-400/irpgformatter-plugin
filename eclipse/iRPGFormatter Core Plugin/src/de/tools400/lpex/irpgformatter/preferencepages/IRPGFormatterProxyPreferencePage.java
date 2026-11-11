/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferencepages;

import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.tools400.lpex.irpgformatter.Messages;

/**
 * Preference page at the root of the preferences, that leads to the
 * preference page of iRPGFormatter. The preference page of iRPGFormatter is
 * located beside the formatter settings of IBM for ILE RPG, which is hard to
 * reach.
 * <p>
 * When the page becomes visible, it opens the preference page of iRPGFormatter
 * in the same dialog.
 * </p>
 */
public class IRPGFormatterProxyPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    /** ID of the preference page of iRPGFormatter, see plugin.xml */
    private static final String TARGET_PAGE_ID = "de.tools400.lpex.irpgformatter.preferencepages.IRPGFormatterPreferencePage";

    public IRPGFormatterProxyPreferencePage() {
        noDefaultAndApplyButton();
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(Composite parent) {

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));

        Link link = new Link(container, SWT.NONE);
        link.setText(Messages.Label_iRPGFormatter_settings_are_located_beside_IBM_settings);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openTargetPage();
            }
        });

        return container;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        // Opening another page while the dialog shows this page is not
        // allowed, therefore it is deferred.
        if (visible) {
            getShell().getDisplay().asyncExec(() -> {
                if (getControl() != null && !getControl().isDisposed() && getControl().isVisible()) {
                    openTargetPage();
                }
            });
        }
    }

    private void openTargetPage() {

        IPreferencePageContainer container = getContainer();
        if (container instanceof IWorkbenchPreferenceContainer) {
            ((IWorkbenchPreferenceContainer)container).openPage(TARGET_PAGE_ID, null);
        }
    }
}
