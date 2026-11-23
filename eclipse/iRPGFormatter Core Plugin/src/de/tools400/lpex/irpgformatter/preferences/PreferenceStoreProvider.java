/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferences;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;

import com.ibm.etools.iseries.edit.IBMiEditPlugin;

import de.tools400.lpex.irpgformatter.IRpgleFormatterPlugin;

/**
 * Factory for providing the appropriate IPreferenceStore implementation.
 * <p>
 * In Eclipse runtime, this returns the plugin's preference store. In test mode,
 * this returns a map-based preference store that doesn't require Eclipse.
 * </p>
 */
public final class PreferenceStoreProvider {

    private static boolean testMode = false;
    private static MapPreferenceStore testModePreferenceStore = null;
    private static MapPreferenceStore testModeIbmPreferenceStore = null;

    private PreferenceStoreProvider() {
        // Utility class
    }

    /**
     * Gets the appropriate preference store based on the current mode.
     *
     * @return the preference store (Eclipse store or map-based test store)
     */
    public static IPreferenceStore getPreferenceStore() {
        if (testMode) {
            if (testModePreferenceStore == null) {
                testModePreferenceStore = new MapPreferenceStore();
            }
            return testModePreferenceStore;
        }
        return IRpgleFormatterPlugin.getDefault().getPreferenceStore();
    }

    /**
     * Gets the IBM preference store based on the current mode.
     * <p>
     * In Eclipse runtime, this returns the IBMiEditPlugin preference store. In
     * test mode, this returns a map-based preference store.
     * </p>
     *
     * @return the IBM preference store
     */
    public static IPreferenceStore getIbmPreferenceStore() {
        if (testMode) {
            if (testModeIbmPreferenceStore == null) {
                testModeIbmPreferenceStore = new MapPreferenceStore();
            }
            return testModeIbmPreferenceStore;
        }
        return IBMiEditPlugin.getDefault().getPreferenceStore();
    }

    /**
     * Enables test mode - uses a map-based preference store without requiring
     * Eclipse runtime. Call this before any test that uses Preferences.
     * <p>
     * After calling this method, call {@link Preferences#resetInstance()} to
     * reset the Preferences singleton, then access Preferences.getInstance()
     * which will use the test store. Finally, call
     * {@link #initializeTestDefaults()} to set up default values.
     * </p>
     */
    public static void enableTestMode() {
        testMode = true;
        testModePreferenceStore = null;
        testModeIbmPreferenceStore = null;
    }

    /**
     * Initializes default preference values for test mode. Call this after
     * enabling test mode and resetting the Preferences instance.
     */
    public static void initializeTestDefaults() {
        if (testMode) {
            Preferences.getInstance().initializeDefaultPreferences();
        }
    }

    /**
     * Disables test mode and resets the test preference store.
     */
    public static void disableTestMode() {
        testMode = false;
        testModePreferenceStore = null;
        testModeIbmPreferenceStore = null;
    }

    /**
     * Returns true if running in test mode.
     */
    public static boolean isTestMode() {
        return testMode;
    }

    /**
     * Sets a value of the test preference store, for example the value of a
     * preference of a former version, that the preferences do not write any
     * more. Unlike {@link #getPreferenceStore()}, the tests can call it without
     * the type of the store, that belongs to JFace.
     *
     * @throws IllegalStateException if test mode is not enabled
     */
    public static void setTestValue(String name, boolean value) {
        getTestPreferenceStore().setValue(name, value);
    }

    /**
     * Sets a value of the test preference store past the setters of the
     * preferences, like the import of an <code>.epf</code> file does, see
     * {@link #setTestValue(String, boolean)}.
     *
     * @throws IllegalStateException if test mode is not enabled
     */
    public static void setTestValue(String name, String value) {
        getTestPreferenceStore().setValue(name, value);
    }

    /**
     * Removes a value of the test preference store, like the Eclipse preference
     * store does, when a value is set to its default value.
     *
     * @throws IllegalStateException if test mode is not enabled
     */
    public static void setTestValueToDefault(String name) {
        getTestPreferenceStore().setToDefault(name);
    }

    /**
     * Sets a value of the test store of the IBM preferences, for example the
     * style of <i>Automatic closure of control block</i>. Unlike
     * {@link #getIbmPreferenceStore()}, the tests can call it without the type
     * of the store, that belongs to JFace.
     *
     * @throws IllegalStateException if test mode is not enabled
     */
    public static void setIbmTestValue(String name, String value) {
        getTestIbmPreferenceStore().setValue(name, value);
    }

    /**
     * Creates the preferences of a project for a test. They have a store of
     * their own with the default values of the workspace, like the project
     * scope of Eclipse.
     *
     * @throws IllegalStateException if test mode is not enabled
     */
    public static Preferences createTestProjectPreferences() {
        return createTestProjectPreferences(Collections.emptyMap());
    }

    /**
     * Creates the preferences of a project for a test, whose file contains the
     * given values, e.g. the file of a former version without the IBM settings.
     * The values are taken as they are, like Eclipse reads the file.
     *
     * @param fileValues - the values of the file by the names of the settings
     * @throws IllegalStateException if test mode is not enabled
     */
    public static Preferences createTestProjectPreferences(Map<String, String> fileValues) {

        if (!testMode) {
            throw new IllegalStateException("Test mode is not enabled.");
        }

        MapPreferenceStore store = new MapPreferenceStore();
        for (Map.Entry<String, String> value : fileValues.entrySet()) {
            store.putValue(value.getKey(), value.getValue());
        }

        Preferences preferences = Preferences.forProject(store);
        preferences.initializeDefaultPreferences();

        return preferences;
    }

    private static IPreferenceStore getTestPreferenceStore() {

        if (!testMode) {
            throw new IllegalStateException("Test mode is not enabled.");
        }

        return getPreferenceStore();
    }

    private static IPreferenceStore getTestIbmPreferenceStore() {

        if (!testMode) {
            throw new IllegalStateException("Test mode is not enabled.");
        }

        return getIbmPreferenceStore();
    }
}
