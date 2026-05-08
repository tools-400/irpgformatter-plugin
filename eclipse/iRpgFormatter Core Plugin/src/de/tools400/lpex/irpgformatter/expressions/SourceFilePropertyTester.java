/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.expressions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

public class SourceFilePropertyTester extends PropertyTester {

    public static final String PROPERTY_NAMESPACE = "de.tools400.lpex.irpgformatter.expressions.file";

    public static final String PROPERTY_EXTENSION = "extension";

    public static final String PROPERTY_IS_DIRECTORY = "isDirectory";

    public static final String PROPERTY_IS_FILE = "isFile";

    public boolean test(Object aReceiver, String aProperty, Object[] anArgs, Object anExpectedValue) {

        if (PROPERTY_EXTENSION.equals(aProperty)) {
            if (!(anExpectedValue instanceof String)) {
                return false;
            }
            String expectedValue = (String)anExpectedValue;
            if (aReceiver instanceof IRemoteFile) {
                String name = ((IRemoteFile)aReceiver).getName();
                int idx = name.lastIndexOf('.');
                if (idx < 0) {
                    return false;
                }
                return expectedValue.equalsIgnoreCase(name.substring(idx + 1));
            }
            if (aReceiver instanceof IFile) {
                return expectedValue.equalsIgnoreCase(((IFile)aReceiver).getFileExtension());
            }
        }

        if (PROPERTY_IS_DIRECTORY.equals(aProperty)) {
            boolean expected = toBoolean(anExpectedValue);
            if (aReceiver instanceof IRemoteFile) {
                return ((IRemoteFile)aReceiver).isDirectory() == expected;
            }
            if (aReceiver instanceof IContainer) {
                return expected;
            }
        }

        if (PROPERTY_IS_FILE.equals(aProperty)) {
            boolean expected = toBoolean(anExpectedValue);
            if (aReceiver instanceof IRemoteFile) {
                return !((IRemoteFile)aReceiver).isDirectory() == expected;
            }
            if (aReceiver instanceof IFile) {
                return expected;
            }
            if (aReceiver instanceof IContainer) {
                return !expected;
            }
        }

        return false;
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean)value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String)value);
        }
        return false;
    }

}
