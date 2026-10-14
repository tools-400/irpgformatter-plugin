/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.test;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;

import de.tools400.lpex.irpgformatter.menu.MenuExtension;
import de.tools400.lpex.menuextension.AbstractLpexMenuExtension;
import de.tools400.lpex.menuextension.model.UserAction;

/**
 * Unit tests for {@link MenuExtension}. The prefixes identify the entries of
 * iRPGFormatter in the LPEX settings. They must cover all actions, otherwise
 * key actions of the user on these actions are left behind, when
 * iRPGFormatter is uninstalled.
 */
public class MenuExtensionTest {

    private final MenuExtension menuExtension = MenuExtension.newStartOfSourceMenuExtension();

    @Test
    public void ownedClassPrefixes_coverAllActionClasses() throws Exception {

        String[] prefixes = invoke("getOwnedClassPrefixes");
        UserAction[] actions = invoke("getAllUserActions");

        assertTrue(actions.length > 0);
        for (UserAction action : actions) {
            assertTrue(action.getClassName(), startsWithAny(action.getClassName(), prefixes));
        }
    }

    @Test
    public void ownedActionIdPrefixes_coverAllActionIds() throws Exception {

        String[] prefixes = invoke("getOwnedActionIdPrefixes");
        UserAction[] actions = invoke("getAllUserActions");

        assertTrue(actions.length > 0);
        for (UserAction action : actions) {
            assertTrue(action.getActionId(), startsWithAny(action.getActionId(), prefixes));
        }
    }

    @Test
    public void ownedPrefixes_endWithDot() throws Exception {

        // "de.tools400.lpex.irpgformatter" would also match the classes of a
        // plug-in "de.tools400.lpex.irpgformatterx".
        for (String prefix : (String[])invoke("getOwnedClassPrefixes")) {
            assertTrue(prefix, prefix.endsWith("."));
        }

        for (String prefix : (String[])invoke("getOwnedActionIdPrefixes")) {
            assertTrue(prefix, prefix.endsWith("."));
        }
    }

    private boolean startsWithAny(String value, String[] prefixes) {

        for (String prefix : prefixes) {
            if (value.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName) throws Exception {

        Method method = AbstractLpexMenuExtension.class.getDeclaredMethod(methodName);
        method.setAccessible(true);

        return (T)method.invoke(menuExtension);
    }
}
