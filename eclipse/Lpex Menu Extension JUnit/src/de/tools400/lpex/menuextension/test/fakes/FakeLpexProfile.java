/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.test.fakes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tools400.lpex.menuextension.logic.ILpexProfile;
import de.tools400.lpex.menuextension.logic.LpexParameter;

/**
 * LPEX settings in memory. The current value is the default value, if set,
 * otherwise the install value. Counts writes and flushes.
 */
public class FakeLpexProfile implements ILpexProfile {

    private final Map<LpexParameter, String> defaultValues = new HashMap<LpexParameter, String>();
    private final Map<LpexParameter, String> installValues = new HashMap<LpexParameter, String>();
    private final List<String> events;

    private boolean available = true;
    private int writeCount;
    private int flushCount;

    public FakeLpexProfile(List<String> events) {
        this.events = events;
    }

    public FakeLpexProfile withDefault(LpexParameter parameter, String value) {
        defaultValues.put(parameter, value);
        return this;
    }

    public FakeLpexProfile withInstall(LpexParameter parameter, String value) {
        installValues.put(parameter, value);
        return this;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean hasDefault(LpexParameter parameter) {
        return defaultValues.containsKey(parameter);
    }

    public String getDefault(LpexParameter parameter) {
        return defaultValues.get(parameter);
    }

    public int getWriteCount() {
        return writeCount;
    }

    public int getFlushCount() {
        return flushCount;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getCurrent(LpexParameter parameter) {

        if (defaultValues.containsKey(parameter)) {
            return defaultValues.get(parameter);
        }

        return installValues.get(parameter);
    }

    public String getInstall(LpexParameter parameter) {
        return installValues.get(parameter);
    }

    public void setDefault(LpexParameter parameter, String value) {
        writeCount++;
        defaultValues.put(parameter, value);
        events.add("profile.setDefault " + parameter.getName());
    }

    public void resetToInstall(LpexParameter parameter) {
        writeCount++;
        defaultValues.remove(parameter);
        events.add("profile.resetToInstall " + parameter.getName());
    }

    public void flush() {
        flushCount++;
        events.add("profile.flush");
    }
}
