/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config

import kotlin.reflect.KClass

internal actual fun <T : Any> platformEnumLookup(value: String, klass: KClass<T>): T {
    // On native, we can't easily enumerate enum constants without reflection
    // This is a limitation of native targets. Users should use the `converted` method for enums.
    throw UnsupportedOperationException(
        "Enum support on native targets is limited. Use the `converted` delegate instead: " +
        "val myEnum: MyEnum by converted(\"key\") { MyEnum.valueOf(it) }"
    )
}
