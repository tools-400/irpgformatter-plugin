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
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.menuextension.logic.LpexParameter;
import de.tools400.lpex.menuextension.logic.Pair;
import de.tools400.lpex.menuextension.logic.UserEntries;
import de.tools400.lpex.menuextension.logic.UserEntriesStore;
import de.tools400.lpex.menuextension.test.fakes.FakePreferencesNode;

/**
 * Unit tests for {@link UserEntriesStore}.
 */
public class UserEntriesStoreTest {

    private static final String PREFIX = "lpexMenuExtension.de.tools400.lpex.irpgformatter.LPEX.";

    private FakePreferencesNode node;
    private UserEntriesStore store;

    @Before
    public void setUp() {
        node = new FakePreferencesNode();
        store = new UserEntriesStore(node, "de.tools400.lpex.irpgformatter.LPEX");
    }

    @Test
    public void keys() {
        assertEquals(PREFIX + "version", store.getVersionKey());
        assertEquals(PREFIX + "userActions", store.getKey(LpexParameter.USER_ACTIONS));
        assertEquals(PREFIX + "userCommands", store.getKey(LpexParameter.USER_COMMANDS));
        assertEquals(PREFIX + "userKeyActions", store.getKey(LpexParameter.USER_KEY_ACTIONS));
        assertEquals(PREFIX + "userMouseActions", store.getKey(LpexParameter.USER_MOUSE_ACTIONS));
    }

    @Test
    public void replace_writesValuesAndVersionAndFlushes() {

        UserEntries entries = new UserEntries();
        entries.add(LpexParameter.USER_KEY_ACTIONS, "c-a-f", "iRPGFormatter.Format");
        entries.add(LpexParameter.USER_KEY_ACTIONS, "f11", "iRPGFormatter.Format");

        store.replace(entries);

        assertEquals("c-a-f iRPGFormatter.Format f11 iRPGFormatter.Format", node.get(PREFIX + "userKeyActions"));
        assertEquals("1", node.get(PREFIX + "version"));
        assertFalse(node.getValues().containsKey(PREFIX + "userActions"));
        assertEquals(1, node.getFlushCount());
    }

    @Test
    public void replace_removesEmptyParameter() {

        node.put(PREFIX + "userKeyActions", "c-a-f iRPGFormatter.Format");

        store.replace(new UserEntries());

        assertFalse(node.getValues().containsKey(PREFIX + "userKeyActions"));
        assertTrue(store.load().isEmpty());
    }

    @Test
    public void load_readsAllParameters() {

        node.put(PREFIX + "userActions", "My.Action de.tools400.lpex.irpgformatter.actions.MyAction");
        node.put(PREFIX + "userCommands", "myCommand de.tools400.lpex.irpgformatter.commands.MyCommand");
        node.put(PREFIX + "userKeyActions", "c-a-f iRPGFormatter.Format");
        node.put(PREFIX + "userMouseActions", "1-pressed iRPGFormatter.Format");

        UserEntries entries = store.load();

        assertEquals(Arrays.asList(new Pair("My.Action", "de.tools400.lpex.irpgformatter.actions.MyAction")), entries.get(LpexParameter.USER_ACTIONS));
        assertEquals(Arrays.asList(new Pair("myCommand", "de.tools400.lpex.irpgformatter.commands.MyCommand")),
            entries.get(LpexParameter.USER_COMMANDS));
        assertEquals(Arrays.asList(new Pair("c-a-f", "iRPGFormatter.Format")), entries.get(LpexParameter.USER_KEY_ACTIONS));
        assertEquals(Arrays.asList(new Pair("1-pressed", "iRPGFormatter.Format")), entries.get(LpexParameter.USER_MOUSE_ACTIONS));
    }

    @Test
    public void load_otherMarkIdIgnored() {

        node.put("lpexMenuExtension.biz.iSphere.LPEX.userKeyActions", "c-tab iSphere.Lpex.Indent");

        assertTrue(store.load().isEmpty());
    }

    @Test
    public void merge_addsWithoutDuplicatesAndKeepsOrder() {

        node.put(PREFIX + "userKeyActions", "c-a-f iRPGFormatter.Format f11 iRPGFormatter.Format");

        UserEntries entries = new UserEntries();
        entries.add(LpexParameter.USER_KEY_ACTIONS, "f11", "iRPGFormatter.Format");
        entries.add(LpexParameter.USER_KEY_ACTIONS, "c-a-x", "iRPGFormatter.Format");

        store.merge(entries);

        assertEquals("c-a-f iRPGFormatter.Format f11 iRPGFormatter.Format c-a-x iRPGFormatter.Format", node.get(PREFIX + "userKeyActions"));
        assertEquals(1, node.getFlushCount());
    }

    @Test
    public void merge_nothingToAdd_nothingWritten() {

        store.merge(new UserEntries());

        assertTrue(node.getValues().isEmpty());
        assertEquals(0, node.getFlushCount());
    }

    @Test
    public void replace_flushFails() {

        node.setFailing(true);

        try {
            store.replace(new UserEntries());
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("de.tools400.lpex.irpgformatter.LPEX"));
        }
    }
}
