/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import java.net.UnknownHostException;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;
import de.tools400.lpex.irpgformatter.utils.jobs.DisplayErrorJob;

public class ExceptionUtils {

    private ExceptionUtils() {
        // Utility class
    }

    public static void handleBatchException(Exception e) {

        IRpgleFormatterPlugin.logError("Unexpected error in: " + IRpgleFormatterPlugin.class.getSimpleName(), e);

        DisplayErrorJob job = new DisplayErrorJob(e);
        job.schedule();
    }

    public static String getLocalizedMessage(Throwable throwable) {

        String exceptionMessage;
        if (throwable instanceof UnknownHostException) {
            exceptionMessage = throwable.toString();
        } else {
            exceptionMessage = throwable.getLocalizedMessage();
        }

        if (StringUtils.isNullOrEmpty(exceptionMessage)) {
            return throwable.getClass().getName();
        } else {
            return exceptionMessage.replaceAll("\\p{C}", "�");
        }

    }
}
