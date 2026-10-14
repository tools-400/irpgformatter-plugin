/*******************************************************************************
 * Copyright (c) 2012-2026 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.menuextension.test.fakes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.tools400.lpex.menuextension.logic.IClassChecker;

/**
 * Knows a fixed set of action and command classes.
 */
public class FakeClassChecker implements IClassChecker {

    private final Set<String> actionClasses = new HashSet<String>();
    private final Set<String> commandClasses = new HashSet<String>();

    public FakeClassChecker withActions(String... classNames) {
        actionClasses.addAll(Arrays.asList(classNames));
        return this;
    }

    public FakeClassChecker withCommands(String... classNames) {
        commandClasses.addAll(Arrays.asList(classNames));
        return this;
    }

    public boolean isAction(String className) {
        return actionClasses.contains(className);
    }

    public boolean isCommand(String className) {
        return commandClasses.contains(className);
    }
}
