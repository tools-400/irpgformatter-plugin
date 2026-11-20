/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * A simple map-based implementation of IPreferenceStore for testing purposes.
 * This allows preferences to be stored and retrieved without requiring the
 * Eclipse runtime. Like the preferences of Eclipse, it keeps every value as a
 * string, so that e.g. {@link #getString(String)} returns a boolean value as
 * <code>true</code> or <code>false</code>.
 */
public class MapPreferenceStore implements IPreferenceStore {

    private final Map<String, String> values = new HashMap<>();
    private final Map<String, String> defaults = new HashMap<>();

    @Override
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        // Not implemented for testing
    }

    @Override
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        // Not implemented for testing
    }

    @Override
    public boolean contains(String name) {
        return values.containsKey(name) || defaults.containsKey(name);
    }

    @Override
    public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
        // Not implemented for testing
    }

    @Override
    public boolean getBoolean(String name) {
        return toBoolean(getValue(name));
    }

    @Override
    public boolean getDefaultBoolean(String name) {
        return toBoolean(defaults.get(name));
    }

    @Override
    public double getDefaultDouble(String name) {
        return toDouble(defaults.get(name));
    }

    @Override
    public float getDefaultFloat(String name) {
        return toFloat(defaults.get(name));
    }

    @Override
    public int getDefaultInt(String name) {
        return toInt(defaults.get(name));
    }

    @Override
    public long getDefaultLong(String name) {
        return toLong(defaults.get(name));
    }

    @Override
    public String getDefaultString(String name) {
        return toString(defaults.get(name));
    }

    @Override
    public double getDouble(String name) {
        return toDouble(getValue(name));
    }

    @Override
    public float getFloat(String name) {
        return toFloat(getValue(name));
    }

    @Override
    public int getInt(String name) {
        return toInt(getValue(name));
    }

    @Override
    public long getLong(String name) {
        return toLong(getValue(name));
    }

    @Override
    public String getString(String name) {
        return toString(getValue(name));
    }

    @Override
    public boolean isDefault(String name) {
        return !values.containsKey(name);
    }

    @Override
    public boolean needsSaving() {
        return false;
    }

    @Override
    public void putValue(String name, String value) {
        values.put(name, value);
    }

    @Override
    public void setDefault(String name, boolean value) {
        defaults.put(name, String.valueOf(value));
    }

    @Override
    public void setDefault(String name, double value) {
        defaults.put(name, String.valueOf(value));
    }

    @Override
    public void setDefault(String name, float value) {
        defaults.put(name, String.valueOf(value));
    }

    @Override
    public void setDefault(String name, int value) {
        defaults.put(name, String.valueOf(value));
    }

    @Override
    public void setDefault(String name, long value) {
        defaults.put(name, String.valueOf(value));
    }

    @Override
    public void setDefault(String name, String value) {
        defaults.put(name, value);
    }

    @Override
    public void setToDefault(String name) {
        values.remove(name);
    }

    @Override
    public void setValue(String name, boolean value) {
        values.put(name, String.valueOf(value));
    }

    @Override
    public void setValue(String name, double value) {
        values.put(name, String.valueOf(value));
    }

    @Override
    public void setValue(String name, float value) {
        values.put(name, String.valueOf(value));
    }

    @Override
    public void setValue(String name, int value) {
        values.put(name, String.valueOf(value));
    }

    @Override
    public void setValue(String name, long value) {
        values.put(name, String.valueOf(value));
    }

    @Override
    public void setValue(String name, String value) {
        values.put(name, value);
    }

    /**
     * Returns the value or, if there is none, the default value or
     * <code>null</code>.
     */
    private String getValue(String name) {
        String value = values.get(name);
        if (value != null) {
            return value;
        }
        return defaults.get(name);
    }

    private static boolean toBoolean(String value) {
        if (value == null) {
            return BOOLEAN_DEFAULT_DEFAULT;
        }
        return Boolean.parseBoolean(value);
    }

    private static double toDouble(String value) {
        try {
            return value == null ? DOUBLE_DEFAULT_DEFAULT : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return DOUBLE_DEFAULT_DEFAULT;
        }
    }

    private static float toFloat(String value) {
        try {
            return value == null ? FLOAT_DEFAULT_DEFAULT : Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return FLOAT_DEFAULT_DEFAULT;
        }
    }

    private static int toInt(String value) {
        try {
            return value == null ? INT_DEFAULT_DEFAULT : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return INT_DEFAULT_DEFAULT;
        }
    }

    private static long toLong(String value) {
        try {
            return value == null ? LONG_DEFAULT_DEFAULT : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return LONG_DEFAULT_DEFAULT;
        }
    }

    private static String toString(String value) {
        return value == null ? STRING_DEFAULT_DEFAULT : value;
    }
}
