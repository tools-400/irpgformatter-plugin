/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.test.fakes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.tools400.lpex.menuextension.logic.IMenuExtensionConfiguration;
import de.tools400.lpex.menuextension.logic.LpexParameter;
import de.tools400.lpex.menuextension.logic.UserEntries;
import de.tools400.lpex.menuextension.model.MenuMarkers;

/**
 * Configuration of a menu extension. The factory methods describe the menu
 * extensions of iRpgFormatter and iSphere Comments.
 */
public class FakeConfiguration implements IMenuExtensionConfiguration {

    public static final String IRPGFORMATTER_FORMAT = "iRPGFormatter.Format";
    public static final String IRPGFORMATTER_FORMAT_CLASS = "de.tools400.lpex.irpgformatter.actions.FormatLpexSourceAction";
    public static final String IRPGFORMATTER_MARK_ID = "de.tools400.lpex.irpgformatter.LPEX";

    public static final String ISPHERE_ACTION_PACKAGE = "biz.isphere.lpex.comments.lpex.action.";
    public static final String ISPHERE_MARK_ID = "biz.iSphere.LPEX";

    private String name;
    private String[] classPrefixes = new String[0];
    private String[] actionIdPrefixes = new String[0];
    private UserEntries configuredEntries = new UserEntries();
    private Set<String> enabledActionIds = new HashSet<String>();
    private List<MenuMarkers> markers = new ArrayList<MenuMarkers>();

    public FakeConfiguration(String markId) {
        this.name = markId;
        markers.add(new MenuMarkers("MARK-" + markId + ".End", "MARK-" + markId + ".Start"));
    }

    /**
     * Menu extension of iRpgFormatter: action <code>iRPGFormatter.Format</code>,
     * key action <code>c-a-S</code> from the preferences of iRpgFormatter.
     */
    public static FakeConfiguration iRpgFormatter() {

        FakeConfiguration configuration = new FakeConfiguration(IRPGFORMATTER_MARK_ID);
        configuration.withClassPrefixes("de.tools400.lpex.irpgformatter.");
        configuration.withActionIdPrefixes("iRPGFormatter.");
        configuration.withEnabledAction(IRPGFORMATTER_FORMAT, IRPGFORMATTER_FORMAT_CLASS);
        configuration.withKeyAction("c-a-S", IRPGFORMATTER_FORMAT);

        return configuration;
    }

    /**
     * Menu extension of iSphere Comments with commenting and indenting
     * disabled: all 5 actions are configured, none is enabled.
     */
    public static FakeConfiguration iSphereCommentsDisabled() {

        FakeConfiguration configuration = new FakeConfiguration(ISPHERE_MARK_ID);
        configuration.withClassPrefixes("biz.isphere.lpex.comments.");
        configuration.withActionIdPrefixes("iSphere.Lpex.");
        configuration.withDisabledAction("iSphere.Lpex.Comment", ISPHERE_ACTION_PACKAGE + "CommentAction");
        configuration.withDisabledAction("iSphere.Lpex.UnComment", ISPHERE_ACTION_PACKAGE + "UnCommentAction");
        configuration.withDisabledAction("iSphere.Lpex.ToggleComment", ISPHERE_ACTION_PACKAGE + "ToggleCommentAction");
        configuration.withDisabledAction("iSphere.Lpex.Indent", ISPHERE_ACTION_PACKAGE + "IndentAction");
        configuration.withDisabledAction("iSphere.Lpex.UnIndent", ISPHERE_ACTION_PACKAGE + "UnIndentAction");
        configuration.withLegacyMarkers("MARK-Source.Start", "MARK-Source.End");
        configuration.withLegacyMarkers("MARK-Quelle.Start", "MARK-Quelle.End");

        return configuration;
    }

    public FakeConfiguration withClassPrefixes(String... prefixes) {
        this.classPrefixes = prefixes;
        return this;
    }

    public FakeConfiguration withActionIdPrefixes(String... prefixes) {
        this.actionIdPrefixes = prefixes;
        return this;
    }

    public FakeConfiguration withEnabledAction(String actionId, String className) {
        configuredEntries.add(LpexParameter.USER_ACTIONS, actionId, className);
        enabledActionIds.add(actionId);
        return this;
    }

    public FakeConfiguration withDisabledAction(String actionId, String className) {
        configuredEntries.add(LpexParameter.USER_ACTIONS, actionId, className);
        return this;
    }

    public FakeConfiguration withCommand(String name, String className) {
        configuredEntries.add(LpexParameter.USER_COMMANDS, name, className);
        return this;
    }

    public FakeConfiguration withKeyAction(String key, String actionId) {
        configuredEntries.add(LpexParameter.USER_KEY_ACTIONS, key, actionId);
        return this;
    }

    public FakeConfiguration withMouseAction(String mouseEvent, String actionId) {
        configuredEntries.add(LpexParameter.USER_MOUSE_ACTIONS, mouseEvent, actionId);
        return this;
    }

    public FakeConfiguration withLegacyMarkers(String start, String end) {
        markers.add(new MenuMarkers(start, end));
        return this;
    }

    public String getName() {
        return name;
    }

    public String[] getOwnedClassPrefixes() {
        return classPrefixes;
    }

    public String[] getOwnedActionIdPrefixes() {
        return actionIdPrefixes;
    }

    public UserEntries getConfiguredEntries() {
        UserEntries copy = new UserEntries();
        copy.addAll(configuredEntries);
        return copy;
    }

    public Set<String> getEnabledActionIds() {
        return new HashSet<String>(enabledActionIds);
    }

    public MenuMarkers[] getMenuMarkers() {
        return markers.toArray(new MenuMarkers[markers.size()]);
    }
}
