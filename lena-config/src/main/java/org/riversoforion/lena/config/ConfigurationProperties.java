/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ConfigurationProperties {

    private final ConfigurationSource source;
    private final Map<String, ConfigurationProperties> nested = new HashMap<>();
    private ValueConverter valueConverter = new DefaultValueConverter();
    private final Map<String, String> defaults = new HashMap<>();
    private Function<String, String> defaultsResolver;

    protected ConfigurationProperties(ConfigurationSource source) {

        this.source = source;
        this.throwExceptionForMissing();
    }

    protected ConfigurationProperties withNested(String name, ConfigurationProperties nested) {

        this.nested.put(name, nested);
        return this;
    }

    protected ConfigurationProperties withConverter(ValueConverter valueConverter) {

        this.valueConverter = valueConverter;
        return this;
    }

    protected ConfigurationProperties withDefaults(Map<String, String> defaults) {

        this.defaults.putAll(defaults);
        return this;
    }

    protected ConfigurationProperties throwExceptionForMissing() {

        this.defaultsResolver = (name) -> defaults.computeIfAbsent(name, (ignored) -> {
            throw new IllegalArgumentException("No configuration property named " + name);
        });
        return this;
    }

    protected ConfigurationProperties returnNullForMissing() {

        this.defaultsResolver = defaults::get;
        return this;
    }

    public boolean isMissing(String name) {

        return !isSet(name) && !defaults.containsKey(name);
    }

    public boolean isSet(String name) {

        return source.getValue(name)
                     .isPresent();
    }

    public boolean isDefault(String name) {

        return !isSet(name) && defaults.containsKey(name);
    }

    protected <T extends ConfigurationProperties> T nested(String name, Class<T> type) {

        if (!nested.containsKey(name)) {
            throw new IllegalArgumentException("No nested configuration properties named " + name);
        }
        return type.cast(nested.get(name));
    }

    protected String stringVal(String name) {

        return source.getValue(name)
                     .orElseGet(() -> defaultsResolver.apply(name));
    }

    protected boolean booleanVal(String name) {

        return valueConverter.toBoolean(stringVal(name));
    }

    protected short shortVal(String name) {

        return valueConverter.toShort(stringVal(name));
    }

    protected int intVal(String name) {

        return valueConverter.toInt(stringVal(name));
    }

    protected long longVal(String name) {

        return valueConverter.toLong(stringVal(name));
    }

    protected float floatVal(String name) {

        return valueConverter.toFloat(stringVal(name));
    }

    protected double doubleVal(String name) {

        return valueConverter.toDouble(stringVal(name));
    }
}
