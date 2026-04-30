/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.formatter;

import org.eclipse.osgi.util.NLS;

import de.tools400.lpex.irpgformatter.Messages;

/**
 * Exception thrown when a file is locked or read-only and cannot be formatted.
 */
public class FileLockedException extends RpgleFormatterException {

    private static final long serialVersionUID = 1L;

    private final String path;

    /**
     * Generic lock exception: the message is built from the path with the
     * default "File is locked: {0}" template.
     */
    public FileLockedException(String path) {
        super(NLS.bind(Messages.Error_File_is_locked_A, path));
        this.path = path;
    }

    /**
     * Lock exception with a caller-supplied detail message (e.g. a message
     * that already includes the holding job/user, fetched from the RDi
     * API). The default template is bypassed so the detail is not wrapped
     * in a generic "File is locked: ..." prefix.
     */
    public FileLockedException(String path, String detailMessage) {
        super(detailMessage);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
