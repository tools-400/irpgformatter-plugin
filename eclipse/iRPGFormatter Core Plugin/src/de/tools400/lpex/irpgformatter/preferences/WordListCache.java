/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferences;

import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;

import de.tools400.lpex.irpgformatter.utils.KeywordUtils;

/**
 * Caches a word list of the preferences, e.g. the keywords. The list is parsed
 * again, when its value in the preference store changes, also past the setters
 * of the preferences, e.g. when an <code>.epf</code> file is imported.
 */
final class WordListCache {

    private final String name;

    /**
     * The value and the list parsed from it. They are replaced together, so
     * that a job, that reads the list, while the preferences are changed, gets
     * a list, that matches a value.
     */
    private volatile Entry entry;

    /**
     * Creates the cache of a word list.
     *
     * @param name - the name of the word list in the preference store
     */
    WordListCache(String name) {
        this.name = name;
    }

    /**
     * Returns the word list of the current value in the preference store.
     *
     * @param preferenceStore - the preference store
     * @return the canonical spellings by the word in upper case
     */
    Map<String, String> get(IPreferenceStore preferenceStore) {

        String value = preferenceStore.getString(name);

        Entry current = entry;
        if (current == null || !current.value.equals(value)) {
            current = new Entry(value, KeywordUtils.stringToKeywords(value));
            entry = current;
        }

        return current.wordList;
    }

    private static final class Entry {

        private final String value;
        private final Map<String, String> wordList;

        private Entry(String value, Map<String, String> wordList) {
            this.value = value;
            this.wordList = wordList;
        }
    }
}
