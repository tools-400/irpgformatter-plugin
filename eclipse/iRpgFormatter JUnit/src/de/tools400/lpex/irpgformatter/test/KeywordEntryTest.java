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

import de.tools400.lpex.irpgformatter.preferencepages.keywordeditor.KeywordEntry;

public class KeywordEntryTest {

    @Test
    public void constructor_setsKeyAndValue() {
        KeywordEntry entry = new KeywordEntry("EXTPROC", "ExtProc");
        assertEquals("EXTPROC", entry.getKey());
        assertEquals("ExtProc", entry.getValue());
    }

    @Test
    public void setValue_changesValue() {
        KeywordEntry entry = new KeywordEntry("EXTPROC", "ExtProc");
        entry.setValue("EXTPROC");
        assertEquals("EXTPROC", entry.getValue());
    }

    @Test
    public void setValue_preservesKey() {
        KeywordEntry entry = new KeywordEntry("EXTPROC", "ExtProc");
        entry.setValue("extproc");
        assertEquals("EXTPROC", entry.getKey());
    }

    @Test
    public void toString_formatIsKeyEqualsValue() {
        KeywordEntry entry = new KeywordEntry("EXTPROC", "ExtProc");
        assertEquals("EXTPROC=ExtProc", entry.toString());
    }

    @Test
    public void toString_afterSetValue() {
        KeywordEntry entry = new KeywordEntry("OPDESC", "OpDesc");
        entry.setValue("OPDESC");
        assertEquals("OPDESC=OPDESC", entry.toString());
    }
}
