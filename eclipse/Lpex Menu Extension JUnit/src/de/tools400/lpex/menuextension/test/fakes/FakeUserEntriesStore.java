/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.test.fakes;

import java.util.List;

import de.tools400.lpex.menuextension.logic.IUserEntriesStore;
import de.tools400.lpex.menuextension.logic.UserEntries;

/**
 * List of remembered user entries in memory. Can be made to fail.
 */
public class FakeUserEntriesStore implements IUserEntriesStore {

    private final List<String> events;
    private UserEntries entries = new UserEntries();
    private boolean failing;

    public FakeUserEntriesStore(List<String> events) {
        this.events = events;
    }

    public void setFailing(boolean failing) {
        this.failing = failing;
    }

    public UserEntries getEntries() {
        return copy(entries);
    }

    public void setEntries(UserEntries entries) {
        this.entries = copy(entries);
    }

    public UserEntries load() {
        if (failing) {
            throw new IllegalStateException("store failed");
        }
        return copy(entries);
    }

    public void replace(UserEntries newEntries) {
        if (failing) {
            throw new IllegalStateException("store failed");
        }
        entries = copy(newEntries);
        events.add("store.replace " + newEntries);
    }

    public void merge(UserEntries newEntries) {
        if (failing) {
            throw new IllegalStateException("store failed");
        }
        entries.addAll(newEntries);
        events.add("store.merge " + newEntries);
    }

    private static UserEntries copy(UserEntries entries) {
        UserEntries copy = new UserEntries();
        copy.addAll(entries);
        return copy;
    }
}
