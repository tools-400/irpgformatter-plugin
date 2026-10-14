/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.platform;

import de.tools400.lpex.menuextension.helper.LogHelper;
import de.tools400.lpex.menuextension.logic.ILogger;

/**
 * Writes the messages to the platform log and the trace to the console.
 */
public class PlatformLogger implements ILogger {

    public void error(String message, Throwable exception) {
        LogHelper.logError(message, exception);
    }

    public void warning(String message) {
        LogHelper.logWarning(message);
    }

    public void trace(String message) {
        LogHelper.trace(message);
    }
}
