/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.utils;

/**
 * Evaluates the values, that LPEX reports for a block selection. Has no LPEX
 * types, so that the unit tests can use it.
 */
public final class BlockSelectionUtils {

    /**
     * Type of a block, that is selected with the mouse or with Shift and the
     * cursor keys, as the LPEX query <code>block.type</code> returns it.
     */
    public static final String STREAM = "stream";

    private BlockSelectionUtils() {
        // Utility class
    }

    /**
     * Returns the last element of a block selection, that contains selected
     * text. A stream block ends in front of its bottom position, so that it
     * selects nothing of its bottom element, if it ends in position 1. The
     * other blocks (character, element, rectangle) include their bottom
     * element. The position of the cursor does not matter.
     *
     * @param blockType - the type of the block: <code>block.type</code>
     * @param topElement - the first element: <code>block.topElement</code>
     * @param bottomElement - the last element: <code>block.bottomElement</code>
     * @param bottomPosition - the position in the last element:
     *        <code>block.bottomPosition</code>
     * @return the last element with selected text
     */
    public static int getLastSelectedElement(String blockType, int topElement, int bottomElement, int bottomPosition) {

        if (STREAM.equals(blockType) && bottomPosition == 1 && bottomElement > topElement) {
            return bottomElement - 1;
        }

        return bottomElement;
    }
}
