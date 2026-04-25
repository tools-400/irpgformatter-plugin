/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

public class FileUtils {

    private FileUtils() {
        // Utility class
    }

    public static String getExtension(String path) {

        if (path == null || !path.contains(".")) {
            return "";
        }

        return path.substring(path.lastIndexOf(".") + 1);
    }
}
