/*
 * Copyright (c) 2024-2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class ConfigurationProperties {

    private final ConfigurationSource source;
    private final Namespace namespace;
    private final ValueConverter valueConverter;
    private final Map<Name, String> defaults = new HashMap<>();
    private Function<Name, String> defaultsResolver;
    // Cache here for convenience
    private transient final ConfigurationPropertiesRegistry registry = ConfigurationPropertiesRegistry.instance();

    protected ConfigurationProperties(ConfigurationSource source) {

        this(source, Namespace.root());
    }

    protected ConfigurationProperties(ConfigurationSource source, Namespace namespace) {

        this.source = source;
        this.namespace = namespace;
        this.valueConverter = createConverter();
        this.defaults.putAll(createDefaults());
        this.throwExceptionForMissing();
        this.registry.register(this);
        createChildren();
    }

    protected ValueConverter createConverter() {

        return new DefaultValueConverter();
    }

    protected Map<Name, String> createDefaults() {

        return Map.of();
    }

    protected void createChildren() {
        // Only for subclasses
    }

    protected void throwExceptionForMissing() {

        this.defaultsResolver = (name) -> defaults.computeIfAbsent(name, (propName) -> {
            throw new IllegalArgumentException("No configuration property named " + propName);
        });
    }

    protected void returnNullForMissing() {

        this.defaultsResolver = defaults::get;
    }

    public Namespace namespace() {

        return namespace;
    }

    public boolean isMissing(Name name) {

        return !isSet(name) && !defaults.containsKey(name);
    }

    public boolean isSet(Name name) {

        return sourceVal(name).isPresent();
    }

    public boolean isDefault(Name name) {

        return !isSet(name) && defaults.containsKey(name);
    }

    protected <T extends ConfigurationProperties> T nested(Namespace namespace, Class<T> type) {

        return type.cast(registry.get(namespace));
    }

    protected Optional<String> sourceVal(Name name) {

        return source.getValue(namespace, name);
    }

    protected String stringVal(Name name) {

        return sourceVal(name).orElseGet(() -> defaultsResolver.apply(name));
    }

    protected boolean booleanVal(Name name) {

        return valueConverter.toBoolean(stringVal(name));
    }

    protected short shortVal(Name name) {

        return valueConverter.toShort(stringVal(name));
    }

    protected int intVal(Name name) {

        return valueConverter.toInt(stringVal(name));
    }

    protected long longVal(Name name) {

        return valueConverter.toLong(stringVal(name));
    }

    protected float floatVal(Name name) {

        return valueConverter.toFloat(stringVal(name));
    }

    protected double doubleVal(Name name) {

        return valueConverter.toDouble(stringVal(name));
    }
}
