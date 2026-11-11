/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import com.ibm.etools.iseries.parsers.ISeriesEditorRPGILEParser;
import com.ibm.lpex.alef.LpexTextEditor;
import com.ibm.lpex.core.LpexView;

public class IBMRpgleFormatterUtils {

    private IBMRpgleFormatterUtils() {
        // Utility class
    }

    public static void executeIBMFormatter(LpexView lpexView) {

        ISeriesEditorRPGILEParser parser = LpexViewUtils.getParser(lpexView);
        LpexTextEditor editor = parser.getEditor();
        com.ibm.etools.iseries.edit.ui.actions.FormatRPGSourceAction ibmFormatRPGSourceAction = new com.ibm.etools.iseries.edit.ui.actions.FormatRPGSourceAction(
            parser, editor);
        ibmFormatRPGSourceAction.run();
    }
}
