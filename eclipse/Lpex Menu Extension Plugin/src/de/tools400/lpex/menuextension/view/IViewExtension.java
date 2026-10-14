/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.view;

import de.tools400.lpex.menuextension.model.UserAction;
import de.tools400.lpex.menuextension.model.UserCommand;
import de.tools400.lpex.menuextension.model.UserKeyAction;
import de.tools400.lpex.menuextension.model.UserMouseAction;

/**
 * Supplies the extensions that a {@link ViewExtender} applies to each LPEX
 * view. Nothing of it is stored in the LPEX preferences.
 */
public interface IViewExtension {

    /**
     * Returns a name that identifies the extension in log messages.
     */
    public String getName();

    /**
     * Returns the symbolic name of the bundle that owns the extension, for log
     * messages.
     */
    public String getBundleName();

    /**
     * Returns the class loader that loads the classes of the actions and
     * commands.
     */
    public ClassLoader getClassLoader();

    /**
     * Returns the actions that are defined in each view.
     */
    public UserAction[] getViewUserActions();

    /**
     * Returns the commands that are defined in each view.
     */
    public UserCommand[] getViewUserCommands();

    /**
     * Returns the key actions that are defined in each view.
     */
    public UserKeyAction[] getViewUserKeyActions();

    /**
     * Returns the mouse actions that are defined in each view.
     */
    public UserMouseAction[] getViewUserMouseActions();

    /**
     * Returns the popup menu with the menu items of the extension added.
     * Returns the specified popup menu unchanged, if it already contains the
     * menu items.
     */
    public String addViewPopupMenuItems(String popupMenu);
}
