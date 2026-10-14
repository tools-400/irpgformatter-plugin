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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.tools400.lpex.menuextension.logic.Pair;
import de.tools400.lpex.menuextension.logic.PairList;

/**
 * Unit tests for {@link PairList}.
 */
public class PairListTest {

    @Test
    public void parse_null() {
        PairList list = PairList.parse(null);
        assertTrue(list.isEmpty());
        assertEquals("", list.format());
    }

    @Test
    public void parse_blank() {
        assertTrue(PairList.parse("   ").isEmpty());
        assertTrue(PairList.parse("\"\"").isEmpty());
    }

    @Test
    public void parse_enclosingQuotes() {
        PairList list = PairList.parse("\"c-a-1 CMOneCompile c-a-f iRPGFormatter.Format\"");
        assertEquals(Arrays.asList(new Pair("c-a-1", "CMOneCompile"), new Pair("c-a-f", "iRPGFormatter.Format")), list.getPairs());
        assertFalse(list.hasRemainder());
    }

    @Test
    public void parse_multipleWhiteSpace() {
        PairList list = PairList.parse("  c-a-1   CMOneCompile \t c-a-2\nCMOneRecompile  ");
        assertEquals(Arrays.asList(new Pair("c-a-1", "CMOneCompile"), new Pair("c-a-2", "CMOneRecompile")), list.getPairs());
    }

    @Test
    public void parse_incompletePair() {
        PairList list = PairList.parse("c-a-1 CMOneCompile c-a-2");
        assertEquals(Arrays.asList(new Pair("c-a-1", "CMOneCompile")), list.getPairs());
        assertTrue(list.hasRemainder());
        assertEquals("c-a-2", list.getRemainder());
        assertEquals("c-a-1 CMOneCompile c-a-2", list.format());
    }

    @Test
    public void parse_keepsOrder() {
        List<Pair> pairs = PairList.parse(TestData.USER_KEY_ACTIONS).getPairs();
        assertEquals(5, pairs.size());
        assertEquals(new Pair("c-tab", "iSphere.Lpex.Indent"), pairs.get(0));
        assertEquals(new Pair("c-a-1", "CMOneCompile"), pairs.get(1));
        assertEquals(new Pair("c-a-2", "CMOneRecompile"), pairs.get(2));
        assertEquals(new Pair("c-a-f", "iRPGFormatter.Format"), pairs.get(3));
        assertEquals(new Pair("c-s-tab", "iSphere.Lpex.UnIndent"), pairs.get(4));
    }

    @Test
    public void format_workspaceValuesUnchanged() {
        assertEquals(TestData.USER_ACTIONS, PairList.parse(TestData.USER_ACTIONS).format());
        assertEquals(TestData.USER_COMMANDS, PairList.parse(TestData.USER_COMMANDS).format());
        assertEquals(TestData.USER_KEY_ACTIONS, PairList.parse(TestData.USER_KEY_ACTIONS).format());
    }

    @Test
    public void remove_keepsOrderOfOtherPairs() {
        PairList list = PairList.parse(TestData.USER_KEY_ACTIONS);
        PairList newList = list.remove(Arrays.asList(new Pair("c-tab", "iSphere.Lpex.Indent"), new Pair("c-s-tab", "iSphere.Lpex.UnIndent")));
        assertEquals("c-a-1 CMOneCompile c-a-2 CMOneRecompile c-a-f iRPGFormatter.Format", newList.format());
    }

    @Test
    public void remove_comparesBothParts() {
        PairList list = PairList.parse("c-a-1 CMOneCompile");
        assertEquals("c-a-1 CMOneCompile", list.remove(Arrays.asList(new Pair("c-a-1", "iRPGFormatter.Format"))).format());
    }

    @Test
    public void remove_keepsRemainder() {
        PairList list = PairList.parse("c-a-1 CMOneCompile c-a-f iRPGFormatter.Format dangling");
        assertEquals("c-a-1 CMOneCompile dangling", list.remove(Arrays.asList(new Pair("c-a-f", "iRPGFormatter.Format"))).format());
    }

    @Test
    public void append_remainderStaysAtEnd() {
        PairList list = PairList.parse("c-a-1 CMOneCompile dangling");
        PairList newList = list.append(Arrays.asList(new Pair("c-a-f", "iRPGFormatter.Format")));
        assertEquals("c-a-1 CMOneCompile c-a-f iRPGFormatter.Format dangling", newList.format());
        assertEquals(2, PairList.parse(newList.format()).getPairs().size());
    }

    @Test
    public void getRemainder_nullForCompletePairs() {
        assertNull(PairList.parse("c-a-1 CMOneCompile").getRemainder());
    }
}
