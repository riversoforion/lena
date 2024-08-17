/*
 * Copyright (c) 2024. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers;

import org.riversoforion.lena.config.NameResolver;

public class PassThroughNameResolver implements NameResolver {

    @Override
    public String resolveName(String name) {

        return name;
    }
}
