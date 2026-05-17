/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

public class ColorUtils {

    private static final String COLORS_NAMESPACE = "de.tools400.lpex.irpgformatter.";

    public static final String ERROR_COLOR = JFacePreferences.ERROR_COLOR;
    public static final String CURRENT_LINE_COLOR = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
    public static final String LINE_NUMBER_COLOR = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
    public static final String PRINT_MARGIN_COLOR = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR;

    public static final String LINE_NUMBER_RULER_ENABLED = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;

    private ColorUtils() {
        // Utility class
    }

    /**
     * Returns the color of error messages.
     * <p>
     * Eclipse UI:<br>
     * General -> Appearance -> Colors and Fonts -> Basics -> Error text color
     * 
     * @return color of error messages
     */
    public static Color getErrorTextColor() {

        Color errorColor = JFaceResources.getColorRegistry().get(ERROR_COLOR);

        return errorColor;
    }

    /**
     * Returns the color of the current text line.
     * <p>
     * Eclipse UI:<br>
     * General -> Editors -> Text Editors -> Current line highlight
     * 
     * @return current line color
     */
    public static Color getCurrentLineHighlightColor() {

        Color currentLineColor = getOrUpdateEditorsUIColor(CURRENT_LINE_COLOR);

        return currentLineColor;
    }

    /**
     * Returns the color of the line number ruler.
     * <p>
     * Eclipse UI:<br>
     * General -> Editors -> Text Editors -> Line number foreground
     * 
     * @return color of error messages
     */
    public static Color getLineNumberColor() {

        Color lineNumberColor = getOrUpdateEditorsUIColor(LINE_NUMBER_COLOR);

        return lineNumberColor;
    }

    /**
     * Returns the color of the print margin. This color is used for the
     * vertical ruler of the source preview panel.
     * <p>
     * Eclipse UI:<br>
     * General -> Editors -> Text Editors -> Print margin
     * 
     * @return color of error messages
     */
    public static Color getPrintMarginColor() {

        Color printMarginColor = getOrUpdateEditorsUIColor(PRINT_MARGIN_COLOR);

        return printMarginColor;
    }

    private static Color getOrUpdateEditorsUIColor(String colorKey) {

        String fullColorKey = COLORS_NAMESPACE + colorKey;

        IPreferenceStore store = EditorsUI.getPreferenceStore();
        RGB fontColorRGB = PreferenceConverter.getColor(store, colorKey);

        ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
        colorRegistry.put(fullColorKey, fontColorRGB);

        Color color = colorRegistry.get(fullColorKey);

        return color;
    }

    public static void clearEditorUIColors() {

        ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
        for (String fullColorKey : colorRegistry.getKeySet()) {
            if (fullColorKey.startsWith(COLORS_NAMESPACE)) {
                String colorKey = fullColorKey.substring(COLORS_NAMESPACE.length());
                getOrUpdateEditorsUIColor(colorKey);
            }
        }
    }
}
