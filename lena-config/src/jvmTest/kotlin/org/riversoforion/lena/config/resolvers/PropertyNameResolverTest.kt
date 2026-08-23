/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers

import org.riversoforion.lena.config.Name
import org.riversoforion.lena.config.Namespace
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PropertyNameResolverTest {

    @Test
    fun resolveName_Valid() {
        val cases = listOf(
            Triple("/", "myProp", "myProp"),
            Triple("/", "my.otherProp", "my.otherProp"),
            Triple("/", "prop-name", "prop-name"),
            Triple("/my-prefix", "myProp", "my-prefix.myProp"),
            Triple("/my/prefix", "other-prop", "my.prefix.other-prop"),
            Triple("/my/prefix", "other.prop", "my.prefix.other.prop"),
            Triple("/", "name+with#funky?Characters!", "name+with#funky?Characters!"),
            Triple("/My Prefix", "%some\$Other@PROP", "My Prefix.%some\$Other@PROP"),
            Triple("/Another/Prefix", ".valid", "Another.Prefix..valid"),
            Triple("/", ".how.about.this?", ".how.about.this?"),
            Triple("/", ".", "."),
            Triple("/It \$eem\$", "property..names.are...Very\tFlexible", "It \$eem\$.property..names.are...Very\tFlexible"),
            Triple("/", "#Not.a.Comment", "#Not.a.Comment"),
        )
        val testProps = Properties()
        javaClass.getResourceAsStream("/properties/prop-name-resolver.properties").use { testProps.load(it) }
        val resolver = PropertyNameResolver()

        for ((namespace, rawName, expected) in cases) {
            val ns = Namespace.parse(namespace)
            val name = Name.of(rawName)

            val result = resolver.resolveName(ns, name)

            assertEquals(expected, result, "namespace='$namespace' rawName='$rawName'")
            // Make sure that property names we think are valid can actually be used to look up a property
            assertTrue(testProps.containsKey(expected), "expected properties file to contain key '$expected'")
        }
    }
}
