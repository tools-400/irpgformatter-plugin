/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.logic;

/**
 * Stores the remembered user entries of a menu extension in a preference node.
 * Keys:
 * <ul>
 * <li><code>lpexMenuExtension.&lt;markId&gt;.version</code>: format version,
 * currently <code>1</code></li>
 * <li><code>lpexMenuExtension.&lt;markId&gt;.userActions</code>,
 * <code>.userCommands</code>, <code>.userKeyActions</code>,
 * <code>.userMouseActions</code>: pairs in the format of the LPEX
 * parameters</li>
 * </ul>
 */
public class UserEntriesStore implements IUserEntriesStore {

    public static final String FORMAT_VERSION = "1"; //$NON-NLS-1$

    private static final String KEY_PREFIX = "lpexMenuExtension."; //$NON-NLS-1$
    private static final String VERSION = "version"; //$NON-NLS-1$
    private static final String UPDATE_PROFILE = "updateProfile."; //$NON-NLS-1$

    private final IPreferencesNode node;
    private final String markId;

    public UserEntriesStore(IPreferencesNode node, String markId) {
        this.node = node;
        this.markId = markId;
    }

    public UserEntries load() {

        UserEntries entries = new UserEntries();

        for (LpexParameter parameter : LpexParameter.userParameters()) {
            PairList pairs = PairList.parse(node.get(getKey(parameter)));
            entries.addAll(parameter, pairs.getPairs());
        }

        return entries;
    }

    public void replace(UserEntries entries) {

        for (LpexParameter parameter : LpexParameter.userParameters()) {
            String value = new PairList(entries.get(parameter), null).format();
            if (value.length() == 0) {
                node.remove(getKey(parameter));
            } else {
                node.put(getKey(parameter), value);
            }
        }

        node.put(getVersionKey(), FORMAT_VERSION);

        try {
            node.flush();
        } catch (Exception e) {
            throw new IllegalStateException("Could not save the user entries of LPEX menu extension: " + markId, e); //$NON-NLS-1$
        }
    }

    public void merge(UserEntries entries) {

        if (entries.isEmpty()) {
            return;
        }

        UserEntries mergedEntries = load();
        mergedEntries.addAll(entries);

        replace(mergedEntries);
    }

    public String getKey(LpexParameter parameter) {

        String name = parameter.getName();
        if (name.startsWith(UPDATE_PROFILE)) {
            name = name.substring(UPDATE_PROFILE.length());
        }

        return KEY_PREFIX + markId + "." + name; //$NON-NLS-1$
    }

    public String getVersionKey() {
        return KEY_PREFIX + markId + "." + VERSION; //$NON-NLS-1$
    }
}
