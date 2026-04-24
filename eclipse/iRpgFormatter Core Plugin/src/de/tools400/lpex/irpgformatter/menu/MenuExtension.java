/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.menu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import de.tools400.lpex.irpgformatter.Messages;
import de.tools400.lpex.irpgformatter.actions.FormatLpexSourceAction;
import de.tools400.lpex.irpgformatter.preferences.Preferences;
import de.tools400.lpex.menuextension.AbstractLpexMenuExtension;
import de.tools400.lpex.menuextension.LpexMenuExtensionPlugin;
import de.tools400.lpex.menuextension.LpexKey;
import de.tools400.lpex.menuextension.LpexMenu;
import de.tools400.lpex.menuextension.model.UserAction;
import de.tools400.lpex.menuextension.model.UserKeyAction;

/**
 * This class extends the popup menue of the Lpex editor. It adds the following
 * options:
 * <ul>
 * <li>iFormat</li>
 * </ul>
 */
public class MenuExtension extends AbstractLpexMenuExtension implements IPropertyChangeListener {

    private static final String PROPERTY_LPEX_USER_KEY_ACTIONS = "default.updateProfile.userKeyActions"; //$NON-NLS-1$
    private static final String SUB_MENU_NAME = Messages.MenuItem_SubMenu_iRPGFormatter;
    private static final String MARK_ID = "de.tools400.lpex.irpgformatter.LPEX"; //$NON-NLS-1$

    private static final boolean START_OF_MENU = false;
    private static final boolean END_OF_MENU = true;

    private Boolean isEndOfMenu;

    public static MenuExtension newMainMenuExtension() {
        return new MenuExtension(null);
    }

    public static MenuExtension newStartOfSourceMenuExtension() {
        return new MenuExtension(START_OF_MENU);
    }

    public static MenuExtension newEndOfSourceMenuExtension() {
        return new MenuExtension(END_OF_MENU);
    }

    private MenuExtension(Boolean isEndOfMenu) {
        super(BOTTOM);
        this.isEndOfMenu = isEndOfMenu;
    }

    @Override
    public void initializeLpexEditor(LpexMenuExtensionPlugin plugin) {
        super.initializeLpexEditor(plugin);
    }

    @Override
    protected UserAction[] getUserActionsInternal(boolean allActions) {

        List<UserAction> actions = new LinkedList<>();

        checkAndAddUserAction(actions, FormatLpexSourceAction.ID, FormatLpexSourceAction.class.getName());

        return actions.toArray(new UserAction[actions.size()]);
    }

    @Override
    protected String getMenuName() {
        return SUB_MENU_NAME;
    }

    @Override
    protected String getMarkId() {
        return MARK_ID;
    }

    @Override
    protected UserKeyAction[] getUserKeyActions() {

        List<UserKeyAction> actions = new LinkedList<>();
        String shortcut = getUserKeyActionShortcut(Preferences.getInstance().getUserKeyActions(), FormatLpexSourceAction.ID);
        checkAndAddUserKeyAction(actions, shortcut, FormatLpexSourceAction.ID);

        return actions.toArray(new UserKeyAction[actions.size()]);
    }

    @Override
    protected List<String> getMenuActions() {

        List<String> menuActions = new ArrayList<>();

        menuActions.add("\"" + Messages.MenuItem_Format + "\" " + FormatLpexSourceAction.ID);

        return menuActions;
    }

    @Override
    protected int findStartOfLpexSubMenu(String menu) {

        if (isEndOfMenu == null) {
            return -1;
        }

        int i = menu.indexOf(LpexMenu.SOURCE);
        if (i >= 0) {
            i = i + LpexMenu.SOURCE.length();
            if (isEndOfMenu) {
                // int j = menu.indexOf(LpexMenu.END_SUBMENU, i + 1);
                int j = menu.indexOf("endSubmenu", i + 1);
                if (j >= 0) {
                    i = j - 1;
                }
            }
        }

        return i;
    }

    @Override
    protected IPropertyChangeListener getPreferencesChangeListener() {
        return this;
    }

    public static String getInitialUserKeyActions() {

        List<UserKeyAction> actions = new LinkedList<>();

        checkAndAddUserKeyAction(actions, createShortcut(LpexKey.CTRL, LpexKey.ALT, "F"), FormatLpexSourceAction.ID);

        StringBuilder buffer = new StringBuilder();
        for (UserKeyAction action : actions) {
            appendActionToBuffer(buffer, action);
        }

        return buffer.toString();

    }

    public void propertyChange(PropertyChangeEvent event) {

        if (!PROPERTY_LPEX_USER_KEY_ACTIONS.equals(event.getProperty())) {
            return;
        }

        UserKeyAction[] newUserKeyActions = parseUserKeyActions((String)event.getNewValue());

        UserAction[] userActionsList = getEnabledUserActions();
        Set<String> knownActionClasses = new HashSet<>();
        for (UserAction action : userActionsList) {
            knownActionClasses.add(action.getActionId());
        }

        StringBuilder buffer = new StringBuilder();
        for (UserKeyAction action : newUserKeyActions) {
            if (knownActionClasses.contains(action.getActionId())) {
                appendActionToBuffer(buffer, action);
            }
        }

        Preferences.getInstance().setUserKeyActions(buffer.toString());
    }
}
