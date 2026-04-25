package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.rules.MinimumNameLengthRule;

public class MinimumNameLengthRuleTest extends AbstractTestCase {

    @Test
    public void satisfied_whenAboveMinimum() {
        FormatterConfig config = getFormatterConfig();
        config.setMinLiteralLength(20);
        MinimumNameLengthRule rule = new MinimumNameLengthRule(config);
        assertTrue(rule.isSatisfiedBy(25));
    }

    @Test
    public void satisfied_whenEqualToMinimum() {
        FormatterConfig config = getFormatterConfig();
        config.setMinLiteralLength(20);
        MinimumNameLengthRule rule = new MinimumNameLengthRule(config);
        assertTrue(rule.isSatisfiedBy(20));
    }

    @Test
    public void notSatisfied_whenBelowMinimum() {
        FormatterConfig config = getFormatterConfig();
        config.setMinLiteralLength(20);
        MinimumNameLengthRule rule = new MinimumNameLengthRule(config);
        assertFalse(rule.isSatisfiedBy(19));
    }

    @Test
    public void notSatisfied_whenZero() {
        FormatterConfig config = getFormatterConfig();
        config.setMinLiteralLength(20);
        MinimumNameLengthRule rule = new MinimumNameLengthRule(config);
        assertFalse(rule.isSatisfiedBy(0));
    }
}
