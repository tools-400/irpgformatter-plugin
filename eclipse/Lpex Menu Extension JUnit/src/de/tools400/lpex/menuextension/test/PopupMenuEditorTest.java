/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.tools400.lpex.menuextension.logic.PopupMenuEditor;
import de.tools400.lpex.menuextension.model.MenuMarkers;

/**
 * Unit tests for {@link PopupMenuEditor}: the popup menu text of a view and
 * the removal of menu blocks of earlier versions.
 */
public class PopupMenuEditorTest {

    private static final String START = "MARK-de.tools400.lpex.irpgformatter.LPEX.End";
    private static final String END = "MARK-de.tools400.lpex.irpgformatter.LPEX.Start";
    private static final MenuMarkers MARKERS = new MenuMarkers(START, END);

    private static final List<String> MENU_ACTIONS = Arrays.asList("\"iFormat\" iRPGFormatter.Format");
    private static final String SOURCE_MENU = "popup.sourceMenu";

    private static final String BLOCK_IN_SOURCE_MENU = START + " separator \"iFormat\" iRPGFormatter.Format " + END;
    private static final String BLOCK_AS_SUB_MENU = START + " beginSubmenu \"iRPGFormatter\" \"iFormat\" iRPGFormatter.Format endSubmenu separator "
        + END;

    private final PopupMenuEditor editor = new PopupMenuEditor(MARKERS);

    @Test
    public void createBlock_intoExistingMenu() {
        assertEquals(BLOCK_IN_SOURCE_MENU, editor.createBlock("iRPGFormatter", MENU_ACTIONS, true));
    }

    @Test
    public void createBlock_subMenu() {
        assertEquals(BLOCK_AS_SUB_MENU, editor.createBlock("iRPGFormatter", MENU_ACTIONS, false));
    }

    @Test
    public void createBlock_nullIsSeparator() {
        String block = editor.createBlock("Menu", Arrays.asList("\"A\" actionA", null, "\"B\" actionB"), true);
        assertEquals(START + " separator \"A\" actionA separator \"B\" actionB " + END, block);
    }

    @Test
    public void insertBlock_afterSourceMenu() {

        String popupMenu = TestData.INSTALL_POPUP;
        int location = findStartOfSourceMenu(popupMenu);

        String newPopupMenu = editor.insertBlock(popupMenu, editor.createBlock("iRPGFormatter", MENU_ACTIONS, true), location, PopupMenuEditor.BOTTOM);

        assertTrue(newPopupMenu.contains(SOURCE_MENU + " " + BLOCK_IN_SOURCE_MENU + " separator popup.hexEditLine hexEditLine endSubmenu"));
        assertTrue(newPopupMenu.startsWith("popup.cut cut"));
    }

    @Test
    public void insertBlock_subMenuAtBottom() {

        String popupMenu = "popup.cut cut popup.copy copy";

        String newPopupMenu = editor.insertBlock(popupMenu, BLOCK_AS_SUB_MENU, -1, PopupMenuEditor.BOTTOM);

        assertEquals(popupMenu + " " + BLOCK_AS_SUB_MENU, newPopupMenu);
    }

    @Test
    public void insertBlock_subMenuAtTop() {

        String popupMenu = "popup.cut cut popup.copy copy";

        String newPopupMenu = editor.insertBlock(popupMenu, BLOCK_AS_SUB_MENU, -1, PopupMenuEditor.TOP);

        assertEquals(BLOCK_AS_SUB_MENU + " " + popupMenu, newPopupMenu);
    }

    @Test
    public void insertBlock_topIntoSourceMenu() {

        // Version 1.0 threw a StringIndexOutOfBoundsException here.
        String popupMenu = TestData.INSTALL_POPUP;
        int location = findStartOfSourceMenu(popupMenu);

        String top = editor.insertBlock(popupMenu, BLOCK_IN_SOURCE_MENU, location, PopupMenuEditor.TOP);
        String bottom = editor.insertBlock(popupMenu, BLOCK_IN_SOURCE_MENU, location, PopupMenuEditor.BOTTOM);

        assertEquals(bottom, top);
        assertTrue(top.contains(SOURCE_MENU + " " + BLOCK_IN_SOURCE_MENU));
    }

    @Test
    public void insertBlock_twiceGivesOneBlock() {

        String popupMenu = TestData.POPUP;
        int location = findStartOfSourceMenu(popupMenu);

        String once = editor.insertBlock(popupMenu, BLOCK_IN_SOURCE_MENU, location, PopupMenuEditor.BOTTOM);
        String twice = editor.insertBlock(once, BLOCK_IN_SOURCE_MENU, findStartOfSourceMenu(once), PopupMenuEditor.BOTTOM);

        assertEquals(once, twice);
        assertEquals(1, count(twice, START));
    }

    @Test
    public void insertBlock_noPopupMenu() {
        assertEquals(BLOCK_AS_SUB_MENU, editor.insertBlock(null, BLOCK_AS_SUB_MENU, -1, PopupMenuEditor.BOTTOM));
        assertEquals(BLOCK_AS_SUB_MENU, editor.insertBlock(" ", BLOCK_AS_SUB_MENU, -1, PopupMenuEditor.BOTTOM));
    }

    @Test
    public void insertBlock_locationBeyondEnd() {

        String popupMenu = "popup.cut cut";

        assertEquals(popupMenu + " " + BLOCK_IN_SOURCE_MENU, editor.insertBlock(popupMenu, BLOCK_IN_SOURCE_MENU, 100, PopupMenuEditor.BOTTOM));
    }

    @Test
    public void containsBlock() {
        assertFalse(editor.containsBlock(null));
        assertFalse(editor.containsBlock(TestData.POPUP));
        assertTrue(editor.containsBlock("popup.cut cut " + BLOCK_IN_SOURCE_MENU));
    }

    @Test
    public void containsBlock_wholeTokenOnly() {
        assertFalse(editor.containsBlock("popup.cut cut " + START + "X"));
        assertFalse(editor.containsBlock("X" + START + " popup.cut cut"));
    }

    @Test
    public void removeBlocks_insertedBlock() {

        String popupMenu = TestData.POPUP;
        String newPopupMenu = editor.insertBlock(popupMenu, BLOCK_IN_SOURCE_MENU, findStartOfSourceMenu(popupMenu), PopupMenuEditor.BOTTOM);
        List<String> warnings = new ArrayList<String>();

        assertEquals(popupMenu, PopupMenuEditor.removeBlocks(newPopupMenu, MARKERS, warnings));
        assertTrue(warnings.isEmpty());
    }

    @Test
    public void removeBlocks_blockAtStartAndEnd() {

        List<String> warnings = new ArrayList<String>();

        assertEquals("popup.cut cut", PopupMenuEditor.removeBlocks(BLOCK_AS_SUB_MENU + " popup.cut cut", MARKERS, warnings));
        assertEquals("popup.cut cut", PopupMenuEditor.removeBlocks("popup.cut cut " + BLOCK_AS_SUB_MENU, MARKERS, warnings));
        assertEquals("", PopupMenuEditor.removeBlocks(BLOCK_AS_SUB_MENU, MARKERS, warnings));
    }

    @Test
    public void removeBlocks_severalBlocks() {

        // Version 1.0 removed only the first block (M4).
        String popupMenu = "popup.cut cut " + BLOCK_AS_SUB_MENU + " popup.copy copy " + BLOCK_IN_SOURCE_MENU + " popup.paste paste";
        List<String> warnings = new ArrayList<String>();

        assertEquals("popup.cut cut popup.copy copy popup.paste paste", PopupMenuEditor.removeBlocks(popupMenu, MARKERS, warnings));
    }

    @Test
    public void removeBlocks_missingEndMarker() {

        String popupMenu = "popup.cut cut " + BLOCK_AS_SUB_MENU + " popup.copy copy " + START + " separator \"iFormat\" iRPGFormatter.Format";
        List<String> warnings = new ArrayList<String>();

        String newPopupMenu = PopupMenuEditor.removeBlocks(popupMenu, MARKERS, warnings);

        assertEquals("popup.cut cut popup.copy copy " + START + " separator \"iFormat\" iRPGFormatter.Format", newPopupMenu);
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains(END));
    }

    @Test
    public void removeBlocks_legacyMarkers() {

        // MARK-Source of install.popup is an anchor of IBM and must stay.
        MenuMarkers legacyMarkers = new MenuMarkers("MARK-Source.Start", "MARK-Source.End");
        String popupMenu = TestData.INSTALL_POPUP.replace("popup.hexEditLine hexEditLine",
            "popup.hexEditLine hexEditLine MARK-Source.Start \"Comment\" iSphere.Lpex.Comment MARK-Source.End");
        List<String> warnings = new ArrayList<String>();

        assertEquals(TestData.INSTALL_POPUP, PopupMenuEditor.removeBlocks(popupMenu, legacyMarkers, warnings));
        assertTrue(warnings.isEmpty());
    }

    @Test
    public void removeBlocks_noBlock() {

        List<String> warnings = new ArrayList<String>();

        assertEquals(TestData.POPUP, PopupMenuEditor.removeBlocks(TestData.POPUP, MARKERS, warnings));
    }

    @Test
    public void isSameMenu() {
        assertTrue(PopupMenuEditor.isSameMenu("popup.cut  cut\tpopup.copy copy ", " popup.cut cut popup.copy copy"));
        assertFalse(PopupMenuEditor.isSameMenu("popup.cut cut", "popup.copy copy"));
        assertFalse(PopupMenuEditor.isSameMenu(null, "popup.copy copy"));
        assertTrue(PopupMenuEditor.isSameMenu(null, null));
    }

    /**
     * Like <code>MenuExtension.findStartOfLpexSubMenu()</code> of iRpgFormatter:
     * insert at the start of the source menu.
     */
    static int findStartOfSourceMenu(String popupMenu) {

        int i = popupMenu.indexOf(SOURCE_MENU);
        if (i < 0) {
            return -1;
        }

        return i + SOURCE_MENU.length();
    }

    private static int count(String text, String token) {

        int count = 0;
        int i = text.indexOf(token);
        while (i >= 0) {
            count++;
            i = text.indexOf(token, i + 1);
        }

        return count;
    }
}
