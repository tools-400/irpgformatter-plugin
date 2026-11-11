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

import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.statements.MaximumNameLengthRule;

public class MaximumNameLengthRuleTest extends AbstractTestCase {

    @Test
    public void maxLength_belowConfiguredMax() {
        FormatterConfig config = getFormatterConfig();
        config.setMaxNameLength(60);
        MaximumNameLengthRule rule = new MaximumNameLengthRule(config);
        assertEquals(40, rule.apply(40));
    }

    @Test
    public void maxLength_aboveConfiguredMax() {
        FormatterConfig config = getFormatterConfig();
        config.setMaxNameLength(60);
        MaximumNameLengthRule rule = new MaximumNameLengthRule(config);
        assertEquals(60, rule.apply(80));
    }

    @Test
    public void maxLength_equalToConfiguredMax() {
        FormatterConfig config = getFormatterConfig();
        config.setMaxNameLength(60);
        MaximumNameLengthRule rule = new MaximumNameLengthRule(config);
        assertEquals(60, rule.apply(60));
    }

    @Test
    public void maxLength_zero() {
        FormatterConfig config = getFormatterConfig();
        config.setMaxNameLength(60);
        MaximumNameLengthRule rule = new MaximumNameLengthRule(config);
        assertEquals(0, rule.apply(0));
    }
}
