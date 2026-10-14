/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.test.fakes;

import java.util.List;

import de.tools400.lpex.menuextension.logic.IExtensionCallback;
import de.tools400.lpex.menuextension.logic.LpexParameter;
import de.tools400.lpex.menuextension.logic.MenuExtensionLifecycle;
import de.tools400.lpex.menuextension.logic.UserEntries;

/**
 * Records the steps. Removes the legacy entries like
 * <code>AbstractLpexMenuExtension</code> does. Can be made to fail in one
 * step.
 */
public class FakeCallback implements IExtensionCallback {

    public static final String REMOVE_LEGACY_ENTRIES = "callback.removeLegacyEntries";
    public static final String REGISTER_EXTENSION = "callback.registerExtension";
    public static final String START_VIEW_EXTENSION = "callback.startViewExtension";
    public static final String ADD_PREFERENCES_LISTENER = "callback.addPreferencesListener";
    public static final String STOP_VIEW_EXTENSION = "callback.stopViewExtension";
    public static final String REMOVE_PREFERENCES_LISTENER = "callback.removePreferencesListener";

    private final List<String> events;
    private MenuExtensionLifecycle lifecycle;
    private String failingStep;
    private UserEntries restoredEntries;

    public FakeCallback(List<String> events) {
        this.events = events;
    }

    public void setLifecycle(MenuExtensionLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public void setFailingStep(String step) {
        this.failingStep = step;
    }

    /**
     * Returns the parameter of the last call of
     * {@link #startViewExtension(UserEntries)} or <code>null</code>.
     */
    public UserEntries getRestoredEntries() {
        return restoredEntries;
    }

    public void removeLegacyEntries(UserEntries ownedEntries) {

        record(REMOVE_LEGACY_ENTRIES);

        for (LpexParameter parameter : LpexParameter.userParameters()) {
            lifecycle.removeEntries(parameter, ownedEntries.get(parameter));
        }

        lifecycle.removeMenuBlocks();
    }

    public void registerExtension() {
        record(REGISTER_EXTENSION);
    }

    public void startViewExtension(UserEntries entries) {
        record(START_VIEW_EXTENSION);
        this.restoredEntries = entries;
    }

    public void addPreferencesListener() {
        record(ADD_PREFERENCES_LISTENER);
    }

    public void stopViewExtension() {
        record(STOP_VIEW_EXTENSION);
    }

    public void removePreferencesListener() {
        record(REMOVE_PREFERENCES_LISTENER);
    }

    private void record(String step) {

        events.add(step);

        if (step.equals(failingStep)) {
            throw new IllegalStateException("failed: " + step);
        }
    }
}
