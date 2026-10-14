/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.tools400.lpex.menuextension.logic.LpexParameter;
import de.tools400.lpex.menuextension.logic.OwnedEntriesMatcher;
import de.tools400.lpex.menuextension.logic.Pair;
import de.tools400.lpex.menuextension.logic.PairList;
import de.tools400.lpex.menuextension.logic.UserEntries;

/**
 * Unit tests for {@link OwnedEntriesMatcher}.
 */
public class OwnedEntriesMatcherTest {

    private static final String IRPG_PREFIX = "de.tools400.lpex.irpgformatter.";
    private static final String FORMAT = "iRPGFormatter.Format";
    private static final String FORMAT_CLASS = "de.tools400.lpex.irpgformatter.actions.FormatLpexSourceAction";

    private static final String[] NONE = new String[0];

    @Test
    public void match_ownClassPrefix() {

        UserEntries entries = entries(FORMAT + " " + FORMAT_CLASS + " CMOneCompile de.taskforce.cmoneng.rse.lpexactions.CompileLPEXAction", null, null,
            null);
        OwnedEntriesMatcher matcher = new OwnedEntriesMatcher(new String[] { IRPG_PREFIX }, NONE, Collections.<String> emptyList());

        UserEntries owned = matcher.match(entries);

        assertEquals(Arrays.asList(new Pair(FORMAT, FORMAT_CLASS)), owned.get(LpexParameter.USER_ACTIONS));
    }

    @Test
    public void match_olderClassInSamePackage() {

        UserEntries entries = entries("iRPGFormatter.Old de.tools400.lpex.irpgformatter.actions.OldAction", null, "c-a-o iRPGFormatter.Old", null);
        OwnedEntriesMatcher matcher = new OwnedEntriesMatcher(new String[] { IRPG_PREFIX }, NONE, Arrays.asList(FORMAT));

        UserEntries owned = matcher.match(entries);

        assertEquals(1, owned.get(LpexParameter.USER_ACTIONS).size());
        assertEquals(Arrays.asList(new Pair("c-a-o", "iRPGFormatter.Old")), owned.get(LpexParameter.USER_KEY_ACTIONS));
    }

    @Test
    public void match_prefixesDoNotOverlap() {

        UserEntries entries = entries(
            "iSphere.Lpex.Comment biz.isphere.lpex.comments.lpex.action.CommentAction SprPrePrc.Edit biz.isphere.strpreprc.lpex.action.EditAction", null,
            null, null);

        UserEntries ownedByComments = new OwnedEntriesMatcher(new String[] { "biz.isphere.lpex.comments." }, NONE, Collections.<String> emptyList())
            .match(entries);
        UserEntries ownedBySTRPREPRC = new OwnedEntriesMatcher(new String[] { "biz.isphere.strpreprc." }, NONE, Collections.<String> emptyList())
            .match(entries);

        assertEquals(Arrays.asList(new Pair("iSphere.Lpex.Comment", "biz.isphere.lpex.comments.lpex.action.CommentAction")),
            ownedByComments.get(LpexParameter.USER_ACTIONS));
        assertEquals(Arrays.asList(new Pair("SprPrePrc.Edit", "biz.isphere.strpreprc.lpex.action.EditAction")),
            ownedBySTRPREPRC.get(LpexParameter.USER_ACTIONS));
    }

    @Test
    public void match_actionIdPrefix() {

        UserEntries entries = entries(null, null, "c-a-x iRPGFormatter.Legacy c-a-1 CMOneCompile", null);

        UserEntries withPrefix = new OwnedEntriesMatcher(NONE, new String[] { "iRPGFormatter." }, Collections.<String> emptyList()).match(entries);
        UserEntries withoutPrefix = new OwnedEntriesMatcher(NONE, NONE, Collections.<String> emptyList()).match(entries);

        assertEquals(Arrays.asList(new Pair("c-a-x", "iRPGFormatter.Legacy")), withPrefix.get(LpexParameter.USER_KEY_ACTIONS));
        assertTrue(withoutPrefix.isEmpty());
    }

    @Test
    public void match_configuredActionId() {

        UserEntries entries = entries(null, null, "c-a-f " + FORMAT, null);
        OwnedEntriesMatcher matcher = new OwnedEntriesMatcher(NONE, NONE, Arrays.asList(FORMAT));

        assertEquals(Arrays.asList(new Pair("c-a-f", FORMAT)), matcher.match(entries).get(LpexParameter.USER_KEY_ACTIONS));
    }

    @Test
    public void match_emptyPrefixIgnored() {

        UserEntries entries = entries(TestData.USER_ACTIONS, TestData.USER_COMMANDS, TestData.USER_KEY_ACTIONS, null);
        OwnedEntriesMatcher matcher = new OwnedEntriesMatcher(new String[] { "", " ", null }, new String[] { "" }, Collections.<String> emptyList());

        assertTrue(matcher.match(entries).isEmpty());
    }

    @Test
    public void match_h1_iSphereScenario() {

        // iSphere Comments with commenting and indenting disabled: the 5
        // actions are configured, but none is enabled.
        UserEntries entries = entries(TestData.USER_ACTIONS, TestData.USER_COMMANDS, TestData.USER_KEY_ACTIONS, null);
        List<String> configuredActionIds = Arrays.asList("iSphere.Lpex.Comment", "iSphere.Lpex.UnComment", "iSphere.Lpex.ToggleComment",
            "iSphere.Lpex.Indent", "iSphere.Lpex.UnIndent");
        OwnedEntriesMatcher matcher = new OwnedEntriesMatcher(new String[] { "biz.isphere.lpex.comments." }, new String[] { "iSphere.Lpex." },
            configuredActionIds);

        UserEntries owned = matcher.match(entries);

        assertEquals(5, owned.get(LpexParameter.USER_ACTIONS).size());
        for (Pair action : owned.get(LpexParameter.USER_ACTIONS)) {
            assertTrue(action.getSecond().startsWith("biz.isphere.lpex.comments.lpex.action."));
        }
        assertEquals(Arrays.asList(new Pair("c-tab", "iSphere.Lpex.Indent"), new Pair("c-s-tab", "iSphere.Lpex.UnIndent")),
            owned.get(LpexParameter.USER_KEY_ACTIONS));
        assertTrue(owned.get(LpexParameter.USER_COMMANDS).isEmpty());
    }

    @Test
    public void match_h2_keyOfUserOnForeignActionIsNotOwned() {

        // The plug-in would like to use c-a-1, but the user assigned it to
        // CMOne. The entry is compared by its action, not by its key.
        UserEntries entries = entries(null, null, TestData.USER_KEY_ACTIONS, null);
        OwnedEntriesMatcher matcher = new OwnedEntriesMatcher(new String[] { IRPG_PREFIX }, new String[] { "iRPGFormatter." }, Arrays.asList(FORMAT));

        UserEntries owned = matcher.match(entries);

        assertEquals(Arrays.asList(new Pair("c-a-f", FORMAT)), owned.get(LpexParameter.USER_KEY_ACTIONS));
    }

    @Test
    public void match_h2_ownActionOnAnyKey() {

        UserEntries entries = entries(null, null, "c-a-f " + FORMAT + " f11 " + FORMAT + " c-a-s-f12 " + FORMAT, null);
        OwnedEntriesMatcher matcher = new OwnedEntriesMatcher(NONE, NONE, Arrays.asList(FORMAT));

        assertEquals(3, matcher.match(entries).get(LpexParameter.USER_KEY_ACTIONS).size());
    }

    @Test
    public void match_userCommands() {

        UserEntries entries = entries(null, TestData.USER_COMMANDS + " iFormat de.tools400.lpex.irpgformatter.commands.FormatCommand", null, null);
        OwnedEntriesMatcher matcher = new OwnedEntriesMatcher(new String[] { IRPG_PREFIX }, NONE, Collections.<String> emptyList());

        assertEquals(Arrays.asList(new Pair("iFormat", "de.tools400.lpex.irpgformatter.commands.FormatCommand")),
            matcher.match(entries).get(LpexParameter.USER_COMMANDS));
    }

    @Test
    public void match_userMouseActions() {

        UserEntries entries = entries(null, null, null, "1-pressed " + FORMAT + " 2-pressed blockMarkElement");
        OwnedEntriesMatcher matcher = new OwnedEntriesMatcher(NONE, NONE, Arrays.asList(FORMAT));

        assertEquals(Arrays.asList(new Pair("1-pressed", FORMAT)), matcher.match(entries).get(LpexParameter.USER_MOUSE_ACTIONS));
    }

    @Test
    public void isOwnedClass() {

        OwnedEntriesMatcher matcher = new OwnedEntriesMatcher(new String[] { IRPG_PREFIX }, NONE, Collections.<String> emptyList());

        assertTrue(matcher.isOwnedClass(FORMAT_CLASS));
        assertTrue(!matcher.isOwnedClass("de.tools400.rdp.removecolorcodes.action.RemoveColorCodesAction"));
    }

    static UserEntries entries(String userActions, String userCommands, String userKeyActions, String userMouseActions) {

        UserEntries entries = new UserEntries();
        entries.addAll(LpexParameter.USER_ACTIONS, PairList.parse(userActions).getPairs());
        entries.addAll(LpexParameter.USER_COMMANDS, PairList.parse(userCommands).getPairs());
        entries.addAll(LpexParameter.USER_KEY_ACTIONS, PairList.parse(userKeyActions).getPairs());
        entries.addAll(LpexParameter.USER_MOUSE_ACTIONS, PairList.parse(userMouseActions).getPairs());

        return entries;
    }
}
