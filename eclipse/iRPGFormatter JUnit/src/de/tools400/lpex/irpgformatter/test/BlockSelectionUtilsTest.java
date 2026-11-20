/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.utils.BlockSelectionUtils;

/**
 * Unit tests for the last element of a block selection, with the values, that
 * LPEX reports for the blocks (measured with LPEX 4.6.12, 4.23.5 and 4.31.5).
 * The position of the cursor does not matter.
 */
public class BlockSelectionUtilsTest {

    @Test
    public void elementBlock_includesBottomElement() {
        // Alt+L on the lines 2 and 4, cursor anywhere, e.g. in position 1
        assertEquals(4, BlockSelectionUtils.getLastSelectedElement("element", 2, 4, 11));
    }

    @Test
    public void streamBlock_endingInPosition1_excludesBottomElement() {
        // Shift+Down from 2/1 to 5/1
        assertEquals(4, BlockSelectionUtils.getLastSelectedElement("stream", 2, 5, 1));
    }

    @Test
    public void streamBlock_endingBehindPosition1_includesBottomElement() {
        // from 2/1 to 5/3 or upward from 5/3 to 2/1
        assertEquals(5, BlockSelectionUtils.getLastSelectedElement("stream", 2, 5, 3));
    }

    @Test
    public void streamBlock_withinOneElement_isThatElement() {
        // upward from 2/3 to 2/1
        assertEquals(2, BlockSelectionUtils.getLastSelectedElement("stream", 2, 2, 3));
    }

    @Test
    public void characterBlock_endingInPosition1_includesBottomElement() {
        // Alt+Z from 2/1 to 5/1
        assertEquals(5, BlockSelectionUtils.getLastSelectedElement("character", 2, 5, 1));
    }

    @Test
    public void rectangleBlock_endingInPosition1_includesBottomElement() {
        // Alt+R from 2/1 to 4/1
        assertEquals(4, BlockSelectionUtils.getLastSelectedElement("rectangle", 2, 4, 1));
    }
}
