/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PrioritizedConfigurationSourceTest {

    @Test
    fun getValue_SingleSource() {
        val ns = Namespace.of("prefix")
        val source = ConfigurationSource { namespace, name ->
            if (namespace != ns) null
            else if (name.segments().first().startsWith("existing")) "$name value" else null
        }
        val prioritized = PrioritizedConfigurationSource(listOf(source to null))

        assertEquals("existing-num value", prioritized.getValue(ns, Name.of("existing num")))
        assertEquals("existing-str value", prioritized.getValue(ns, Name.of("existing str")))
        assertNull(prioritized.getValue(ns, Name.of("missing num")))
    }

    @Test
    fun getValue_MultipleSources() {
        val ns = Namespace.of("this", "that")
        var firstCalls = 0
        var secondCalls = 0
        var thirdCalls = 0
        val first = ConfigurationSource { namespace, name ->
            firstCalls++
            if (namespace == ns && name == Name.of("first num")) "first value" else null
        }
        val second = ConfigurationSource { namespace, name ->
            secondCalls++
            if (namespace == ns && name == Name.of("second bool")) "second value" else null
        }
        val third = ConfigurationSource { namespace, name ->
            thirdCalls++
            if (namespace == ns && name == Name.of("third string")) "third value" else null
        }
        val prioritized = PrioritizedConfigurationSource(
            listOf(first to null, second to null, third to null)
        )

        assertEquals("first value", prioritized.getValue(ns, Name.of("first num")))
        assertEquals("second value", prioritized.getValue(ns, Name.of("second bool")))
        assertEquals("third value", prioritized.getValue(ns, Name.of("third string")))
        assertNull(prioritized.getValue(ns, Name.of("fourth num")))

        assertEquals(4, firstCalls)
        assertEquals(3, secondCalls)
        assertEquals(2, thirdCalls)
    }
}
