/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.util.HashMap;
import java.util.Map;

public abstract class ConfigurationProperties {

    private final ConfigurationSource source;
    private final Map<Class<? extends ConfigurationProperties>, ConfigurationProperties> nested = new HashMap<>();
    private ValueConverter valueConverter = new DefaultValueConverter();

    protected ConfigurationProperties(ConfigurationSource source, ConfigurationProperties... nested) {

        this.source = source;
        for (ConfigurationProperties props : nested) {
            this.nested.put(props.getClass(), props);
        }
    }

    public ConfigurationProperties withConverter(ValueConverter valueConverter) {

        this.valueConverter = valueConverter;
        return this;
    }

    protected <T> T nested(Class<T> type) {

        if (!nested.containsKey(type)) {
            throw new IllegalArgumentException("No nested configuration properties of type " + type);
        }
        return type.cast(nested.get(type));
    }

    protected String stringVal(String name) {

        return source.getValue(name);
    }

    protected boolean booleanVal(String name) {

        return valueConverter.toBoolean(source.getValue(name));
    }

    protected short shortVal(String name) {

        return valueConverter.toShort(source.getValue(name));
    }

    protected int intVal(String name) {

        return valueConverter.toInt(source.getValue(name));
    }

    protected long longVal(String name) {

        return valueConverter.toLong(source.getValue(name));
    }
}
