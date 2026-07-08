/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleConfigurationSourceTest {

    @Test
    fun getValue_DelegatesToResolvers() {
        val root = Namespace.root()
        val thisProp = Name.of("this.prop")
        var resolvedNamespace: Namespace? = null
        var resolvedName: Name? = null
        val names = NameResolver { namespace, name ->
            resolvedNamespace = namespace
            resolvedName = name
            "THIS_PROP"
        }
        var resolvedKey: String? = null
        val values = ValueResolver { key ->
            resolvedKey = key
            "some_value"
        }

        val source = SimpleConfigurationSource(names, values)
        val value = source.getValue(root, thisProp)

        assertEquals("some_value", value)
        assertEquals(root, resolvedNamespace)
        assertEquals(thisProp, resolvedName)
        assertEquals("THIS_PROP", resolvedKey)
    }
}
