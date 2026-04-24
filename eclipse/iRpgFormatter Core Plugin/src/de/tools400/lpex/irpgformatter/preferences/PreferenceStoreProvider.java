/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferences;

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
}
