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

import de.tools400.lpex.irpgformatter.rules.statements.RemoveConstKeywordRule;

public class RemoveConstKeywordRuleTest {

    @Test
    public void constDisabled_removesConst() {
        RemoveConstKeywordRule rule = new RemoveConstKeywordRule(false);
        assertEquals("", rule.format("const"));
    }

    @Test
    public void constDisabled_removesConst_uppercase() {
        RemoveConstKeywordRule rule = new RemoveConstKeywordRule(false);
        assertEquals("", rule.format("CONST"));
    }

    @Test
    public void constDisabled_removesConst_mixedCase() {
        RemoveConstKeywordRule rule = new RemoveConstKeywordRule(false);
        assertEquals("", rule.format("Const"));
    }

    @Test
    public void constEnabled_preservesConst() {
        RemoveConstKeywordRule rule = new RemoveConstKeywordRule(true);
        assertEquals("const", rule.format("const"));
    }

    @Test
    public void constEnabled_preservesConst_uppercase() {
        RemoveConstKeywordRule rule = new RemoveConstKeywordRule(true);
        assertEquals("CONST", rule.format("CONST"));
    }

    @Test
    public void constDisabled_otherKeyword_passThrough() {
        RemoveConstKeywordRule rule = new RemoveConstKeywordRule(false);
        assertEquals("value", rule.format("value"));
    }

    @Test
    public void constEnabled_otherKeyword_passThrough() {
        RemoveConstKeywordRule rule = new RemoveConstKeywordRule(true);
        assertEquals("value", rule.format("value"));
    }

    @Test
    public void constDisabled_emptyString_passThrough() {
        RemoveConstKeywordRule rule = new RemoveConstKeywordRule(false);
        assertEquals("", rule.format(""));
    }
}
