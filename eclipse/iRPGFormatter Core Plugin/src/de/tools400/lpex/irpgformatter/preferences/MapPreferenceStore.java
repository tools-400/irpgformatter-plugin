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
 * Eclipse runtime.
 */
public class MapPreferenceStore implements IPreferenceStore {

    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, Object> defaults = new HashMap<>();

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
        Object value = values.get(name);
        if (value instanceof Boolean) {
            return (Boolean)value;
        }
        Object defaultValue = defaults.get(name);
        if (defaultValue instanceof Boolean) {
            return (Boolean)defaultValue;
        }
        return BOOLEAN_DEFAULT_DEFAULT;
    }

    @Override
    public boolean getDefaultBoolean(String name) {
        Object value = defaults.get(name);
        if (value instanceof Boolean) {
            return (Boolean)value;
        }
        return BOOLEAN_DEFAULT_DEFAULT;
    }

    @Override
    public double getDefaultDouble(String name) {
        Object value = defaults.get(name);
        if (value instanceof Double) {
            return (Double)value;
        }
        return DOUBLE_DEFAULT_DEFAULT;
    }

    @Override
    public float getDefaultFloat(String name) {
        Object value = defaults.get(name);
        if (value instanceof Float) {
            return (Float)value;
        }
        return FLOAT_DEFAULT_DEFAULT;
    }

    @Override
    public int getDefaultInt(String name) {
        Object value = defaults.get(name);
        if (value instanceof Integer) {
            return (Integer)value;
        }
        return INT_DEFAULT_DEFAULT;
    }

    @Override
    public long getDefaultLong(String name) {
        Object value = defaults.get(name);
        if (value instanceof Long) {
            return (Long)value;
        }
        return LONG_DEFAULT_DEFAULT;
    }

    @Override
    public String getDefaultString(String name) {
        Object value = defaults.get(name);
        if (value instanceof String) {
            return (String)value;
        }
        return STRING_DEFAULT_DEFAULT;
    }

    @Override
    public double getDouble(String name) {
        Object value = values.get(name);
        if (value instanceof Double) {
            return (Double)value;
        }
        return getDefaultDouble(name);
    }

    @Override
    public float getFloat(String name) {
        Object value = values.get(name);
        if (value instanceof Float) {
            return (Float)value;
        }
        return getDefaultFloat(name);
    }

    @Override
    public int getInt(String name) {
        Object value = values.get(name);
        if (value instanceof Integer) {
            return (Integer)value;
        }
        return getDefaultInt(name);
    }

    @Override
    public long getLong(String name) {
        Object value = values.get(name);
        if (value instanceof Long) {
            return (Long)value;
        }
        return getDefaultLong(name);
    }

    @Override
    public String getString(String name) {
        Object value = values.get(name);
        if (value instanceof String) {
            return (String)value;
        }
        return getDefaultString(name);
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
        defaults.put(name, value);
    }

    @Override
    public void setDefault(String name, double value) {
        defaults.put(name, value);
    }

    @Override
    public void setDefault(String name, float value) {
        defaults.put(name, value);
    }

    @Override
    public void setDefault(String name, int value) {
        defaults.put(name, value);
    }

    @Override
    public void setDefault(String name, long value) {
        defaults.put(name, value);
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
        values.put(name, value);
    }

    @Override
    public void setValue(String name, double value) {
        values.put(name, value);
    }

    @Override
    public void setValue(String name, float value) {
        values.put(name, value);
    }

    @Override
    public void setValue(String name, int value) {
        values.put(name, value);
    }

    @Override
    public void setValue(String name, long value) {
        values.put(name, value);
    }

    @Override
    public void setValue(String name, String value) {
        values.put(name, value);
    }
}
