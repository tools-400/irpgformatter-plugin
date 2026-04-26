/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.progress.UIJob;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.handlers.jobs.IErrorObject;
import de.tools400.lpex.irpgformatter.utils.UIUtils;

public abstract class AbstractFormatHandler extends AbstractHandler {

    protected void displaySuccessDialog(String message) {
        UIJob job = new UIJob(UIUtils.getDisplay(), "") {
            @Override
            public IStatus runInUIThread(IProgressMonitor arg0) {
                MessageDialog.openInformation(UIUtils.getShell(), Messages.I_N_F_O_R_M_A_T_I_O_N, message);
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    protected void displayErrorDialog(String message, IErrorObject[] errors) {
        String[] errorDetails = new String[errors.length];
        for (int i = 0; i < errors.length; i++) {
            errorDetails[i] = errors[i].getFullPath() + ": " + errors[i].getErrorMessage();
        }
        UIJob job = new UIJob(UIUtils.getDisplay(), "") {
            @Override
            public IStatus runInUIThread(IProgressMonitor arg0) {
                UIUtils.displayErrorDetailsTitleAreaDialog(message, errorDetails);
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
}
