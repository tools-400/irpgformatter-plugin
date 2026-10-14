/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.test.fakes;

import java.util.LinkedHashMap;
import java.util.Map;

import de.tools400.lpex.menuextension.logic.IPreferencesNode;

/**
 * Preference node in memory. Counts flushes and can be made to fail.
 */
public class FakePreferencesNode implements IPreferencesNode {

    private final Map<String, String> values = new LinkedHashMap<String, String>();
    private int flushCount;
    private boolean failing;

    public Map<String, String> getValues() {
        return values;
    }

    public int getFlushCount() {
        return flushCount;
    }

    public void setFailing(boolean failing) {
        this.failing = failing;
    }

    public String get(String key) {
        return values.get(key);
    }

    public void put(String key, String value) {
        values.put(key, value);
    }

    public void remove(String key) {
        values.remove(key);
    }

    public void flush() throws Exception {
        if (failing) {
            throw new Exception("flush failed");
        }
        flushCount++;
    }
}
