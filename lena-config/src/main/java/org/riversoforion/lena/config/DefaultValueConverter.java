/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.util.Set;

public class DefaultValueConverter implements ValueConverter {

    protected static final Set<String> TRUE_VALUES = Set.of("true", "yes", "y", "on", "1");

    @Override
    public boolean toBoolean(String value) {

        if (value == null) {
            return false;
        }
        return TRUE_VALUES.contains(value.toLowerCase());
    }

    @Override
    public short toShort(String value) {

        if (value == null) {
            return 0;
        }
        return Short.parseShort(value);
    }

    @Override
    public int toInt(String value) {

        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    @Override
    public long toLong(String value) {

        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value);
    }

    @Override
    public float toFloat(String value) {

        if (value == null) {
            return 0.0f;
        }
        return Float.parseFloat(value);
    }

    @Override
    public double toDouble(String value) {

        if (value == null) {
            return 0.0;
        }
        return Double.parseDouble(value);
    }
}
