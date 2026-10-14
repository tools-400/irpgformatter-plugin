/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import de.tools400.lpex.menuextension.logic.KeyNames;
import de.tools400.lpex.menuextension.logic.Pair;
import de.tools400.lpex.menuextension.logic.PairList;

/**
 * Unit tests for {@link KeyNames}. Covers the key conflict of a view: a plug-in
 * key that the user assigned in the LPEX settings is skipped.
 */
public class KeyNamesTest {

    @Test
    public void isSameKey_equalNames() {
        assertTrue(KeyNames.isSameKey("c-a-f", "c-a-f"));
    }

    @Test
    public void isSameKey_modifiersInAnyOrder() {
        assertTrue(KeyNames.isSameKey("c-a-f", "a-c-f"));
        assertTrue(KeyNames.isSameKey("c-a-s-f12", "s-c-a-f12"));
    }

    @Test
    public void isSameKey_caseIgnored() {
        assertTrue(KeyNames.isSameKey("c-a-F", "c-a-f"));
        assertTrue(KeyNames.isSameKey("C-A-f", "c-a-f"));
        assertTrue(KeyNames.isSameKey("c-pageDown", "c-pagedown"));
    }

    @Test
    public void isSameKey_noSuffixIsTextArea() {
        assertTrue(KeyNames.isSameKey("c-s-o", "c-s-o.t"));
    }

    @Test
    public void isSameKey_otherArea() {
        assertFalse(KeyNames.isSameKey("c-s-o", "c-s-o.p"));
        assertFalse(KeyNames.isSameKey("c-s-o.c", "c-s-o.p"));
    }

    @Test
    public void isSameKey_overlappingAreas() {
        assertTrue(KeyNames.isSameKey("c-s-o.t.p", "c-s-o.p"));
        assertTrue(KeyNames.isSameKey("f4.p", "f4.c.p"));
    }

    @Test
    public void isSameKey_secondaryIgnored() {
        assertTrue(KeyNames.isSameKey("c-s-o.secondary", "c-s-o"));
    }

    @Test
    public void isSameKey_otherModifiers() {
        assertFalse(KeyNames.isSameKey("c-a-f", "c-f"));
        assertFalse(KeyNames.isSameKey("c-a-f", "c-a-s-f"));
    }

    @Test
    public void isSameKey_otherKey() {
        assertFalse(KeyNames.isSameKey("c-a-f", "c-a-g"));
        assertFalse(KeyNames.isSameKey("f1", "f12"));
    }

    @Test
    public void isSameKey_keyLooksLikeModifier() {
        assertFalse(KeyNames.isSameKey("c-a", "a-c"));
        assertTrue(KeyNames.isSameKey("s-c-a", "c-s-a"));
    }

    @Test
    public void isSameKey_null() {
        assertFalse(KeyNames.isSameKey(null, "c-a-f"));
        assertFalse(KeyNames.isSameKey("c-a-f", null));
    }

    @Test
    public void isAssigned_keyOfUser() {
        List<Pair> userKeyActions = PairList.parse(TestData.USER_KEY_ACTIONS).getPairs();
        assertTrue(KeyNames.isAssigned("a-c-F", userKeyActions));
        assertTrue(KeyNames.isAssigned("c-tab", userKeyActions));
    }

    @Test
    public void isAssigned_freeKey() {
        List<Pair> userKeyActions = PairList.parse(TestData.USER_KEY_ACTIONS).getPairs();
        assertFalse(KeyNames.isAssigned("c-a-s-f12", userKeyActions));
        assertFalse(KeyNames.isAssigned("c-a-f.p", userKeyActions));
    }
}
