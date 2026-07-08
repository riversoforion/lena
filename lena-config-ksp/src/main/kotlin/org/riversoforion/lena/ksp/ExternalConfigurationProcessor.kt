/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * Symbol processor for [org.riversoforion.lena.config.annotations.ExternalConfiguration].
 *
 * Generation logic is deferred — see `docs/kotlin-migration-plan.md` Phase 5. This scaffold only
 * proves the module compiles and wires into the KSP pipeline; it currently generates nothing.
 */
public class ExternalConfigurationProcessor : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> = emptyList()
}

/** Registers [ExternalConfigurationProcessor] with the KSP tool-chain via `ServiceLoader`. */
public class ExternalConfigurationProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        ExternalConfigurationProcessor()
}
