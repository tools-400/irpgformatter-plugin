/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * Initializes default preference values for the RPGLE Formatter.
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {

        Preferences.getInstance().initializeDefaultPreferences();
    }
}
