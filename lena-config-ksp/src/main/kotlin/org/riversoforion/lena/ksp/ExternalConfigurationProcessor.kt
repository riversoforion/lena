/*
 * Copyright (c) 2024-2026. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import org.riversoforion.lena.config.annotations.ConfigurationProperty
import org.riversoforion.lena.config.annotations.ExternalConfiguration

/**
 * Symbol processor for [org.riversoforion.lena.config.annotations.ExternalConfiguration].
 *
 * It generates a implementation class that extends [org.riversoforion.lena.config.ConfigurationProperties]
 * and implements the annotated interface.
 */
public class ExternalConfigurationProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(ExternalConfiguration::class.qualifiedName!!)
        val unableToProcess = symbols.filterNot { it.validate() }.toList()

        symbols.filter { it is KSClassDeclaration && it.validate() }
            .forEach { symbol ->
                val classDeclaration = symbol as KSClassDeclaration
                generateImplementation(classDeclaration)
            }

        return unableToProcess
    }

    @OptIn(KspExperimental::class)
    private fun generateImplementation(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        val interfaceName = classDeclaration.simpleName.asString()
        val implementationName = "${interfaceName}Impl"

        val fileSpecBuilder = FileSpec.builder(packageName, implementationName)
        
        val classBuilder = TypeSpec.classBuilder(implementationName)
            .addModifiers(KModifier.PUBLIC)
            .superclass(ClassName("org.riversoforion.lena.config", "ConfigurationProperties"))
            .addSuperinterface(classDeclaration.toClassName())
            
        // Constructor
        val sourceParam = ParameterSpec.builder("source", ClassName("org.riversoforion.lena.config", "ConfigurationSource")).build()
        val namespaceParam = ParameterSpec.builder("namespace", ClassName("org.riversoforion.lena.config", "Namespace"))
            .defaultValue("%T.root()", ClassName("org.riversoforion.lena.config", "Namespace"))
            .build()
            
        val constructor = FunSpec.constructorBuilder()
            .addParameter(sourceParam)
            .addParameter(namespaceParam)
            .build()
            
        classBuilder.primaryConstructor(constructor)
            .addSuperclassConstructorParameter("source")
            .addSuperclassConstructorParameter("namespace")

        val propertiesMetadata = mutableListOf<CodeBlock>()

        classDeclaration.getAllFunctions().forEach { function ->
            val annotation = function.getAnnotationsByType(ConfigurationProperty::class).firstOrNull()
            if (annotation != null) {
                val propertyName = annotation.value
                val returnType = function.returnType?.resolve() ?: return@forEach
                val typeName = returnType.toTypeName()
                
                val funBuilder = FunSpec.builder(function.simpleName.asString())
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(typeName)
                
                val methodName = mapToValMethod(returnType)
                
                funBuilder.addStatement("return %L(%T.of(%S))", methodName, ClassName("org.riversoforion.lena.config", "Name"), propertyName)
                classBuilder.addFunction(funBuilder.build())
                
                // For PropertyRegistry
                propertiesMetadata.add(
                    CodeBlock.of(
                        "%T(%T.of(%S), %T::class)",
                        ClassName("org.riversoforion.lena.config", "PropertyMetadata"),
                        ClassName("org.riversoforion.lena.config", "Name"),
                        propertyName,
                        typeName.copy(nullable = false)
                    )
                )
            }
        }
        
        // init block for registration
        if (propertiesMetadata.isNotEmpty()) {
            val initBlock = CodeBlock.builder()
                .addStatement("%T.register(this::class, listOf(", ClassName("org.riversoforion.lena.config", "PropertyRegistry"))
                .indent()
            
            propertiesMetadata.forEachIndexed { index, codeBlock ->
                initBlock.add(codeBlock)
                if (index < propertiesMetadata.size - 1) {
                    initBlock.add(",")
                }
                initBlock.add("\n")
            }
            
            initBlock.unindent()
                .addStatement("))")
            
            classBuilder.addInitializerBlock(initBlock.build())
        }

        fileSpecBuilder.addType(classBuilder.build())
        fileSpecBuilder.build().writeTo(codeGenerator, false)
    }

    private fun mapToValMethod(type: KSType): String {
        val qualifiedName = type.declaration.qualifiedName?.asString()
        val isNullable = type.isMarkedNullable
        
        return when (qualifiedName) {
            "kotlin.String" -> if (isNullable) "optionalStringVal" else "stringVal"
            "kotlin.Boolean" -> "booleanVal"
            "kotlin.Int" -> "intVal"
            "kotlin.Long" -> "longVal"
            "kotlin.Double" -> "doubleVal"
            "kotlin.Float" -> "floatVal"
            "kotlin.Short" -> "shortVal"
            else -> {
                logger.warn("Unsupported type: $qualifiedName. Defaulting to stringVal.")
                "stringVal"
            }
        }
    }
}

/** Registers [ExternalConfigurationProcessor] with the KSP tool-chain via `ServiceLoader`. */
public class ExternalConfigurationProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        ExternalConfigurationProcessor(environment.codeGenerator, environment.logger)
}
