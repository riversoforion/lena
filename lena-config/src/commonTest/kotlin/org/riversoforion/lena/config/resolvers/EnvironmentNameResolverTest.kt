/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.config.resolvers

import org.riversoforion.lena.config.Name
import org.riversoforion.lena.config.Namespace
import kotlin.test.Test
import kotlin.test.assertEquals

class EnvironmentNameResolverTest {

    @Test
    fun resolveName_Valid() {
        val cases = listOf(
            Triple("", "env_var", "ENV_VAR"),
            Triple("", "envVar", "ENVVAR"),
            Triple("", "foo.bar", "FOO_BAR"),
            Triple("the.namespace", "the name", "THE_NAMESPACE_THE_NAME"),
            Triple("Namespace with space", "name%with-weird+Characters!", "NAMESPACE_WITH_SPACE_NAME_WITH_WEIRD_CHARACTERS"),
            Triple("withNumbers6", "IContain2Numbers31AndAQ", "WITHNUMBERS6_ICONTAIN2NUMBERS31ANDAQ"),
            Triple("my-ns", "_some-name", "MY_NS_SOME_NAME"),
            Triple("my-ns", "@some-name", "MY_NS_SOME_NAME"),
            Triple("PerfectlyValidNamespace", "ValidName", "PERFECTLYVALIDNAMESPACE_VALIDNAME"),
            Triple("my-prefix", "my-name/_Another Name /4th+Name_", "MY_PREFIX_MY_NAME_ANOTHER_NAME_4TH_NAME"),
            Triple("/", "-./*", "_"),
            Triple("/", "././.", "__"),
            Triple("/my/namespace", "()\$%", "MY_NAMESPACE_"),
            Triple("PerfectlyValidNamespace", "@*\\%", "PERFECTLYVALIDNAMESPACE_"),
        )
        val resolver = EnvironmentNameResolver()
        for ((namespace, rawName, expected) in cases) {
            val ns = Namespace.parse(namespace)
            val name = Name.of(rawName)
            assertEquals(expected, resolver.resolveName(ns, name), "namespace='$namespace' rawName='$rawName'")
        }
    }
}
