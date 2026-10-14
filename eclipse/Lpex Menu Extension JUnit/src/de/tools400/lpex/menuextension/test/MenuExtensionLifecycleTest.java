/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.tools400.lpex.menuextension.logic.LpexParameter;
import de.tools400.lpex.menuextension.logic.MenuExtensionLifecycle;
import de.tools400.lpex.menuextension.logic.Pair;
import de.tools400.lpex.menuextension.logic.PairList;
import de.tools400.lpex.menuextension.logic.UserEntries;
import de.tools400.lpex.menuextension.test.fakes.FakeCallback;
import de.tools400.lpex.menuextension.test.fakes.FakeClassChecker;
import de.tools400.lpex.menuextension.test.fakes.FakeConfiguration;
import de.tools400.lpex.menuextension.test.fakes.FakeLogger;
import de.tools400.lpex.menuextension.test.fakes.FakeLpexProfile;
import de.tools400.lpex.menuextension.test.fakes.FakeUserEntriesStore;

/**
 * Unit tests for {@link MenuExtensionLifecycle}: entries of earlier versions,
 * the list of remembered user entries, errors and saving.
 */
public class MenuExtensionLifecycleTest {

    private static final String FORMAT = FakeConfiguration.IRPGFORMATTER_FORMAT;
    private static final String FORMAT_CLASS = FakeConfiguration.IRPGFORMATTER_FORMAT_CLASS;
    private static final String MY_ACTION_CLASS = "de.tools400.lpex.irpgformatter.actions.MyAction";
    private static final String MY_COMMAND_CLASS = "de.tools400.lpex.irpgformatter.commands.MyCommand";

    private static final String START = "MARK-de.tools400.lpex.irpgformatter.LPEX.End";
    private static final String END = "MARK-de.tools400.lpex.irpgformatter.LPEX.Start";

    /** Menu block, as version 1.0 stored it in the source menu. */
    private static final String BLOCK_10 = START + " separator \"iFormat\" " + FORMAT + " " + END;

    /** User action and key action, as version 1.0 stored them. */
    private static final String USER_ACTION_10 = FORMAT + " " + FORMAT_CLASS;
    private static final String KEY_ACTION_10 = "c-a-S " + FORMAT;

    private List<String> events;
    private FakeLpexProfile profile;
    private FakeUserEntriesStore store;
    private FakeClassChecker classChecker;
    private FakeLogger logger;
    private FakeCallback callback;

    @Before
    public void setUp() {
        events = new ArrayList<String>();
        profile = new FakeLpexProfile(events).withInstall(LpexParameter.POPUP, TestData.INSTALL_POPUP);
        store = new FakeUserEntriesStore(events);
        classChecker = new FakeClassChecker().withActions(FORMAT_CLASS, MY_ACTION_CLASS).withCommands(MY_COMMAND_CLASS);
        logger = new FakeLogger();
        callback = new FakeCallback(events);
    }

    /*
     * Entries of earlier versions
     */

    @Test
    public void start_removesEntriesOfVersion10() {

        profile.withDefault(LpexParameter.USER_ACTIONS, TestData.USER_ACTIONS + " " + USER_ACTION_10);
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-1 CMOneCompile " + KEY_ACTION_10);
        profile.withDefault(LpexParameter.POPUP, TestData.INSTALL_POPUP.replace("popup.sourceMenu", "popup.sourceMenu " + BLOCK_10));

        createLifecycle(FakeConfiguration.iRpgFormatter()).start(callback);

        assertEquals(TestData.USER_ACTIONS, current(LpexParameter.USER_ACTIONS));
        assertEquals("c-a-1 CMOneCompile", current(LpexParameter.USER_KEY_ACTIONS));
        assertFalse("popup reset to install value", profile.hasDefault(LpexParameter.POPUP));
        assertTrue("configured entries are not remembered", store.getEntries().isEmpty());
        assertTrue(logger.getErrors().isEmpty());
    }

    @Test
    public void start_removesMenuBlockAndKeepsOtherMenuItems() {

        String popupMenu = TestData.POPUP;
        profile.withDefault(LpexParameter.POPUP, popupMenu.replace("popup.sourceMenu", "popup.sourceMenu " + BLOCK_10));

        createLifecycle(FakeConfiguration.iRpgFormatter()).start(callback);

        assertEquals(popupMenu, current(LpexParameter.POPUP));
    }

    @Test
    public void start_keyActionOfUserIsRememberedAndRestored() {

        // c-a-f iRPGFormatter.Format was created by the user, as the README of
        // iRpgFormatter recommends.
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, TestData.USER_KEY_ACTIONS);

        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);

        assertEquals(Arrays.asList(new Pair("c-a-f", FORMAT)), store.getEntries().get(LpexParameter.USER_KEY_ACTIONS));
        assertEquals("c-tab iSphere.Lpex.Indent c-a-1 CMOneCompile c-a-2 CMOneRecompile c-s-tab iSphere.Lpex.UnIndent c-a-f iRPGFormatter.Format",
            current(LpexParameter.USER_KEY_ACTIONS));
        assertTrue(lifecycle.isRestoreCompleted());
    }

    @Test
    public void startAndStop_h1_iSphereOrphansRemoved() {

        setWorkspaceValues();

        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iSphereCommentsDisabled());
        lifecycle.start(callback);

        // The 5 user actions and the key actions on the indent actions are
        // removed. The key actions are not restored, because indenting is
        // disabled.
        assertFalse(current(LpexParameter.USER_ACTIONS).contains("biz.isphere"));
        assertEquals("c-a-1 CMOneCompile c-a-2 CMOneRecompile c-a-f iRPGFormatter.Format", current(LpexParameter.USER_KEY_ACTIONS));
        assertTrue(current(LpexParameter.USER_ACTIONS).contains("RemoveColorCodesAction de.tools400.rdp.removecolorcodes.action.RemoveColorCodesAction"));
        assertTrue(current(LpexParameter.USER_ACTIONS).contains("CMOneCompile de.taskforce.cmoneng.rse.lpexactions.CompileLPEXAction"));
        assertEquals(TestData.USER_COMMANDS, current(LpexParameter.USER_COMMANDS));
        assertEquals(TestData.POPUP, current(LpexParameter.POPUP));

        lifecycle.stop(callback);

        // Not restored, therefore not remembered any longer.
        assertTrue(store.getEntries().isEmpty());
        assertEquals("c-a-1 CMOneCompile c-a-2 CMOneRecompile c-a-f iRPGFormatter.Format", current(LpexParameter.USER_KEY_ACTIONS));
    }

    @Test
    public void start_legacyMarkersRemoved() {

        String popupMenu = TestData.POPUP.replace("popup.hexEditLine hexEditLine",
            "popup.hexEditLine hexEditLine MARK-Source.Start \"Comment\" iSphere.Lpex.Comment MARK-Source.End");
        profile.withDefault(LpexParameter.POPUP, popupMenu);

        createLifecycle(FakeConfiguration.iSphereCommentsDisabled()).start(callback);

        assertEquals(TestData.POPUP, current(LpexParameter.POPUP));
    }

    @Test
    public void start_menuBlockWithoutEndMarker() {

        String popupMenu = TestData.POPUP + " " + START + " separator \"iFormat\" " + FORMAT;
        profile.withDefault(LpexParameter.POPUP, popupMenu);

        createLifecycle(FakeConfiguration.iRpgFormatter()).start(callback);

        assertEquals(popupMenu, current(LpexParameter.POPUP));
        assertEquals(1, logger.getWarnings().size());
        assertTrue(logger.getWarnings().get(0).contains(END));
    }

    /*
     * Remembered user entries at stop
     */

    @Test
    public void stop_userEntryRememberedAndRemoved() {

        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-1 CMOneCompile");
        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);

        // The user creates a key action on the action of the plug-in.
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-1 CMOneCompile c-a-u " + FORMAT);

        lifecycle.stop(callback);

        assertEquals(Arrays.asList(new Pair("c-a-u", FORMAT)), store.getEntries().get(LpexParameter.USER_KEY_ACTIONS));
        assertEquals("c-a-1 CMOneCompile", current(LpexParameter.USER_KEY_ACTIONS));
    }

    @Test
    public void stop_configuredEntryNotRemembered() {

        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);

        // Written by version 1.0 in a parallel session.
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-1 CMOneCompile " + KEY_ACTION_10);
        profile.withDefault(LpexParameter.USER_ACTIONS, USER_ACTION_10);

        lifecycle.stop(callback);

        assertTrue(store.getEntries().isEmpty());
        assertEquals("c-a-1 CMOneCompile", current(LpexParameter.USER_KEY_ACTIONS));
        assertFalse("reset to install value", profile.hasDefault(LpexParameter.USER_ACTIONS));
    }

    @Test
    public void stop_userEntryRemovedByUser_listCleared() {

        store.setEntries(entries(LpexParameter.USER_KEY_ACTIONS, "c-a-f " + FORMAT));
        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);
        assertEquals("c-a-f " + FORMAT, current(LpexParameter.USER_KEY_ACTIONS));

        // The user removes the key action in the LPEX settings.
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "");

        lifecycle.stop(callback);

        assertTrue(store.getEntries().isEmpty());
    }

    @Test
    public void stop_mergesAfterFailedStart() {

        store.setEntries(entries(LpexParameter.USER_KEY_ACTIONS, "c-a-x " + FORMAT));
        store.setFailing(true);
        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);
        assertFalse(lifecycle.isRestoreCompleted());

        store.setFailing(false);
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-u " + FORMAT);

        lifecycle.stop(callback);

        // c-a-x was not restored in this session and must not get lost.
        assertEquals(Arrays.asList(new Pair("c-a-x", FORMAT), new Pair("c-a-u", FORMAT)), store.getEntries().get(LpexParameter.USER_KEY_ACTIONS));
    }

    @Test
    public void stop_listSavedBeforeRemoving() {

        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-1 CMOneCompile c-a-u " + FORMAT);
        events.clear();

        lifecycle.stop(callback);

        int saved = indexOf("store.replace");
        int removed = indexOf("profile.setDefault updateProfile.userKeyActions");
        int flushed = indexOf("profile.flush");
        assertTrue(saved >= 0 && removed >= 0 && flushed >= 0);
        assertTrue(saved < removed);
        assertTrue(removed < flushed);
    }

    @Test
    public void stop_twiceKeepsList() {

        store.setEntries(entries(LpexParameter.USER_KEY_ACTIONS, "c-a-f " + FORMAT));
        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);

        lifecycle.stop(callback);
        lifecycle.stop(callback);

        assertEquals(Arrays.asList(new Pair("c-a-f", FORMAT)), store.getEntries().get(LpexParameter.USER_KEY_ACTIONS));
    }

    @Test
    public void stop_secondStopDoesNothing() {

        // First stop after the workbench has shut down, second stop by the
        // plug-in.
        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-u " + FORMAT);
        lifecycle.stop(callback);
        events.clear();

        lifecycle.stop(callback);

        assertTrue(events.isEmpty());
        assertEquals(1, profile.getFlushCount());
        assertEquals(Arrays.asList(new Pair("c-a-u", FORMAT)), store.getEntries().get(LpexParameter.USER_KEY_ACTIONS));
        assertTrue(traced("already stopped"));
    }

    @Test
    public void stop_afterNewStart() {

        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);
        lifecycle.stop(callback);

        lifecycle.start(callback);
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-u " + FORMAT);
        lifecycle.stop(callback);

        assertEquals(2, profile.getFlushCount());
        assertEquals(Arrays.asList(new Pair("c-a-u", FORMAT)), store.getEntries().get(LpexParameter.USER_KEY_ACTIONS));
        assertFalse(profile.hasDefault(LpexParameter.USER_KEY_ACTIONS));
    }

    @Test
    public void stop_lastEntryRemoved_resetToInstallValue() {

        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-u " + FORMAT);

        lifecycle.stop(callback);

        assertFalse(profile.hasDefault(LpexParameter.USER_KEY_ACTIONS));
    }

    @Test
    public void stop_incompleteEntryKept() {

        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-1 CMOneCompile c-a-u " + FORMAT + " dangling");

        lifecycle.stop(callback);

        assertEquals("c-a-1 CMOneCompile dangling", current(LpexParameter.USER_KEY_ACTIONS));
        assertFalse(logger.getWarnings().isEmpty());
    }

    /*
     * Remembered user entries at start
     */

    @Test
    public void start_userActionWithMissingClassSkipped() {

        UserEntries remembered = entries(LpexParameter.USER_ACTIONS, "My.Gone de.tools400.lpex.irpgformatter.actions.GoneAction");
        remembered.add(LpexParameter.USER_KEY_ACTIONS, "c-a-g", "My.Gone");
        store.setEntries(remembered);
        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());

        lifecycle.start(callback);

        assertTrue(PairList.parse(current(LpexParameter.USER_ACTIONS)).isEmpty());
        assertTrue(PairList.parse(current(LpexParameter.USER_KEY_ACTIONS)).isEmpty());
        assertTrue(traced("not an LPEX action"));
        assertTrue(traced("action is not defined"));

        lifecycle.stop(callback);

        assertTrue("skipped entries drop out of the list", store.getEntries().isEmpty());
    }

    @Test
    public void start_userActionAndKeyActionOnItRestored() {

        UserEntries remembered = entries(LpexParameter.USER_ACTIONS, "My.Action " + MY_ACTION_CLASS);
        remembered.add(LpexParameter.USER_KEY_ACTIONS, "c-a-m", "My.Action");
        store.setEntries(remembered);
        profile.withDefault(LpexParameter.USER_ACTIONS, TestData.USER_ACTIONS);

        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);

        assertEquals(TestData.USER_ACTIONS + " My.Action " + MY_ACTION_CLASS, current(LpexParameter.USER_ACTIONS));
        assertEquals("c-a-m My.Action", current(LpexParameter.USER_KEY_ACTIONS));

        lifecycle.stop(callback);

        assertEquals(TestData.USER_ACTIONS, current(LpexParameter.USER_ACTIONS));
        assertEquals(remembered, store.getEntries());
    }

    @Test
    public void start_userActionWithUsedNameSkipped() {

        store.setEntries(entries(LpexParameter.USER_ACTIONS, FORMAT + " " + MY_ACTION_CLASS));

        createLifecycle(FakeConfiguration.iRpgFormatter()).start(callback);

        assertTrue(PairList.parse(current(LpexParameter.USER_ACTIONS)).isEmpty());
        assertTrue(traced("name is used"));
    }

    @Test
    public void start_keyActionOnUndefinedActionSkipped() {

        store.setEntries(entries(LpexParameter.USER_KEY_ACTIONS, "c-a-z iRPGFormatter.Removed"));

        createLifecycle(FakeConfiguration.iRpgFormatter()).start(callback);

        assertTrue(PairList.parse(current(LpexParameter.USER_KEY_ACTIONS)).isEmpty());
        assertTrue(traced("action is not defined"));
    }

    @Test
    public void start_keyActionOnUsedKeySkipped() {

        store.setEntries(entries(LpexParameter.USER_KEY_ACTIONS, "A-C-1 " + FORMAT + " c-a-f " + FORMAT));
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-1 CMOneCompile");

        createLifecycle(FakeConfiguration.iRpgFormatter()).start(callback);

        assertEquals("c-a-1 CMOneCompile c-a-f " + FORMAT, current(LpexParameter.USER_KEY_ACTIONS));
        assertTrue(traced("key is used"));
    }

    @Test
    public void start_mouseActions() {

        store.setEntries(entries(LpexParameter.USER_MOUSE_ACTIONS, "1-pressed " + FORMAT + " 2-pressed " + FORMAT));
        profile.withDefault(LpexParameter.USER_MOUSE_ACTIONS, "2-Pressed blockMarkElement");

        createLifecycle(FakeConfiguration.iRpgFormatter()).start(callback);

        assertEquals("2-Pressed blockMarkElement 1-pressed " + FORMAT, current(LpexParameter.USER_MOUSE_ACTIONS));
        assertTrue(traced("mouse event is used"));
    }

    @Test
    public void start_userCommands() {

        store.setEntries(entries(LpexParameter.USER_COMMANDS, "myCommand " + MY_COMMAND_CLASS + " editISeriesMember " + MY_COMMAND_CLASS
            + " gone de.tools400.lpex.irpgformatter.commands.GoneCommand"));
        profile.withDefault(LpexParameter.USER_COMMANDS, TestData.USER_COMMANDS);

        createLifecycle(FakeConfiguration.iRpgFormatter()).start(callback);

        assertEquals(TestData.USER_COMMANDS + " myCommand " + MY_COMMAND_CLASS, current(LpexParameter.USER_COMMANDS));
        assertTrue(traced("name is used"));
        assertTrue(traced("not an LPEX command"));
    }

    @Test
    public void start_workspaceValues_onlyOwnEntriesChanged() {

        setWorkspaceValues();

        createLifecycle(FakeConfiguration.iRpgFormatter()).start(callback);

        assertEquals(TestData.USER_ACTIONS, current(LpexParameter.USER_ACTIONS));
        assertEquals(TestData.USER_COMMANDS, current(LpexParameter.USER_COMMANDS));
        assertEquals(TestData.POPUP, current(LpexParameter.POPUP));
        assertEquals(
            PairList.parse(TestData.USER_KEY_ACTIONS).remove(Arrays.asList(new Pair("c-a-f", FORMAT))).append(Arrays.asList(new Pair("c-a-f", FORMAT)))
                .format(),
            current(LpexParameter.USER_KEY_ACTIONS));
    }

    /*
     * Errors
     */

    @Test
    public void start_errorInStep_otherStepsRun() {

        callback.setFailingStep(FakeCallback.REMOVE_LEGACY_ENTRIES);

        createLifecycle(FakeConfiguration.iRpgFormatter()).start(callback);

        assertEquals(Arrays.asList(FakeCallback.REMOVE_LEGACY_ENTRIES, FakeCallback.REGISTER_EXTENSION, FakeCallback.START_VIEW_EXTENSION,
            FakeCallback.ADD_PREFERENCES_LISTENER), callbackSteps());
        assertEquals(1, logger.getErrors().size());
    }

    @Test
    public void start_errorInRegister_viewsStillExtended() {

        callback.setFailingStep(FakeCallback.REGISTER_EXTENSION);

        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);

        assertTrue(callbackSteps().contains(FakeCallback.START_VIEW_EXTENSION));
        assertTrue(callbackSteps().contains(FakeCallback.ADD_PREFERENCES_LISTENER));
        assertTrue(lifecycle.isRestoreCompleted());
        assertEquals(1, logger.getErrors().size());
    }

    @Test
    public void start_storeFails_userEntriesStayInLpex() {

        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-f " + FORMAT + " " + KEY_ACTION_10);
        store.setFailing(true);

        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);

        // The entry of version 1.0 is removed, the entry of the user stays.
        assertEquals("c-a-f " + FORMAT, current(LpexParameter.USER_KEY_ACTIONS));
        assertFalse(lifecycle.isRestoreCompleted());
        assertFalse(logger.getErrors().isEmpty());
        assertTrue(callbackSteps().contains(FakeCallback.START_VIEW_EXTENSION));
    }

    @Test
    public void stop_errorInStep_otherStepsRun() {

        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-u " + FORMAT);
        callback.setFailingStep(FakeCallback.STOP_VIEW_EXTENSION);
        events.clear();

        lifecycle.stop(callback);

        assertTrue(callbackSteps().contains(FakeCallback.REMOVE_PREFERENCES_LISTENER));
        assertFalse(profile.hasDefault(LpexParameter.USER_KEY_ACTIONS));
        assertEquals(1, profile.getFlushCount());
        assertEquals(1, logger.getErrors().size());
    }

    @Test
    public void stop_storeFails_entriesRemovedNevertheless() {

        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-1 CMOneCompile c-a-u " + FORMAT);
        store.setFailing(true);

        lifecycle.stop(callback);

        assertEquals("c-a-1 CMOneCompile", current(LpexParameter.USER_KEY_ACTIONS));
        assertEquals(1, profile.getFlushCount());
        assertEquals(1, logger.getErrors().size());
    }

    /*
     * Saving
     */

    @Test
    public void start_noFlush_stop_flush() {

        setWorkspaceValues();
        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());

        lifecycle.start(callback);
        assertEquals(0, profile.getFlushCount());

        lifecycle.stop(callback);
        assertEquals(1, profile.getFlushCount());
    }

    @Test
    public void notAvailable_nothingWritten() {

        setWorkspaceValues();
        store.setEntries(entries(LpexParameter.USER_KEY_ACTIONS, "c-a-u " + FORMAT));
        profile.setAvailable(false);
        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());

        lifecycle.start(callback);
        lifecycle.stop(callback);

        assertEquals(0, profile.getWriteCount());
        assertEquals(0, profile.getFlushCount());
        assertEquals(Arrays.asList(new Pair("c-a-u", FORMAT)), store.getEntries().get(LpexParameter.USER_KEY_ACTIONS));
        assertFalse(callbackSteps().contains(FakeCallback.REGISTER_EXTENSION));
        assertTrue("listeners are removed nevertheless", callbackSteps().contains(FakeCallback.STOP_VIEW_EXTENSION));
        assertEquals(2, logger.getWarnings().size());
    }

    @Test
    public void start_nothingChanged_noWriteAndNothingRestored() {

        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, "c-a-1 CMOneCompile");
        profile.withDefault(LpexParameter.POPUP, TestData.POPUP);

        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);

        assertEquals(0, profile.getWriteCount());
        assertTrue(lifecycle.getRestoredEntries().isEmpty());
        assertTrue(callback.getRestoredEntries().isEmpty());
    }

    @Test
    public void start_restoredEntriesPassedToOpenViews() {

        UserEntries remembered = entries(LpexParameter.USER_KEY_ACTIONS, "c-a-f " + FORMAT + " c-a-z iRPGFormatter.Removed");
        store.setEntries(remembered);

        MenuExtensionLifecycle lifecycle = createLifecycle(FakeConfiguration.iRpgFormatter());
        lifecycle.start(callback);

        // Only the restored entry, not the skipped one.
        UserEntries expected = entries(LpexParameter.USER_KEY_ACTIONS, "c-a-f " + FORMAT);
        assertEquals(expected, lifecycle.getRestoredEntries());
        assertEquals(expected, callback.getRestoredEntries());
    }

    private MenuExtensionLifecycle createLifecycle(FakeConfiguration configuration) {

        MenuExtensionLifecycle lifecycle = new MenuExtensionLifecycle(configuration, profile, store, classChecker, logger);
        callback.setLifecycle(lifecycle);

        return lifecycle;
    }

    private void setWorkspaceValues() {
        profile.withDefault(LpexParameter.POPUP, TestData.POPUP);
        profile.withDefault(LpexParameter.USER_ACTIONS, TestData.USER_ACTIONS);
        profile.withDefault(LpexParameter.USER_COMMANDS, TestData.USER_COMMANDS);
        profile.withDefault(LpexParameter.USER_KEY_ACTIONS, TestData.USER_KEY_ACTIONS);
    }

    private String current(LpexParameter parameter) {

        String value = profile.getCurrent(parameter);
        if (value == null) {
            return "";
        }

        return value;
    }

    private static UserEntries entries(LpexParameter parameter, String value) {

        UserEntries entries = new UserEntries();
        entries.addAll(parameter, PairList.parse(value).getPairs());

        return entries;
    }

    private List<String> callbackSteps() {

        List<String> steps = new ArrayList<String>();
        for (String event : events) {
            if (event.startsWith("callback.")) {
                steps.add(event);
            }
        }

        return steps;
    }

    private int indexOf(String eventPrefix) {

        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).startsWith(eventPrefix)) {
                return i;
            }
        }

        return -1;
    }

    private boolean traced(String text) {

        for (String trace : logger.getTraces()) {
            if (trace.contains(text)) {
                return true;
            }
        }

        return false;
    }
}
