/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.utils.UIUtils;

public class DisplayErrorJob extends UIJob {

    private Exception exception;

    public DisplayErrorJob(Exception exception) {
        super(UIUtils.getDisplay(), "");

        this.exception = exception;
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor arg0) {

        Shell shell = UIUtils.getShell();
        MessageDialog.openError(shell, Messages.E_R_R_O_R, exception.getLocalizedMessage());

        return Status.OK_STATUS;
    }

}
