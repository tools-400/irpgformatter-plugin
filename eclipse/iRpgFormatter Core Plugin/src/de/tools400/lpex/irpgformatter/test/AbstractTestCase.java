/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;

import de.tools400.lpex.irpgformatter.formatter.FormatterUtils;
import de.tools400.lpex.irpgformatter.preferences.FormatterConfig;
import de.tools400.lpex.irpgformatter.preferences.PreferenceStoreProvider;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.irpgformatter.tokenizer.Tokenizer;

/**
 * Abstract base class for all test cases.
 * <p>
 * Sets up the test environment with English locale and test mode preferences.
 * Subclasses should call {@code super.setUp()} and {@code super.tearDown()} if
 * they override these methods.
 * </p>
 */
public abstract class AbstractTestCase {

    private Locale originalLocale;
    private Preferences preferences;
    private Tokenizer tokenizer;
    private FormatterUtils formatterUtils;

    @Before
    public void setUp() {
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);

        setupPreferences();

        FormatterConfig config = FormatterConfig.fromPreferences();
        tokenizer = new Tokenizer(config);
        formatterUtils = new FormatterUtils(config);
    }

    @After
    public void tearDown() {
        if (originalLocale != null) {
            Locale.setDefault(originalLocale);
        }
    }

    /**
     * Sets up test mode preferences with default values.
     */
    protected void setupPreferences() {

        PreferenceStoreProvider.enableTestMode();
        Preferences.resetInstance();
        PreferenceStoreProvider.initializeTestDefaults();
        preferences = Preferences.getInstance();
        preferences.initializeDefaultPreferences();
    }

    protected Preferences getPreferences() {
        return preferences;
    }

    protected FormatterUtils getFormatterUtils() {
        return formatterUtils;
    }

    protected FormatterConfig getFormatterConfig() {
        return getFormatterUtils().getConfig();
    }

    protected Tokenizer getTokenizer() {
        return tokenizer;
    }
}
