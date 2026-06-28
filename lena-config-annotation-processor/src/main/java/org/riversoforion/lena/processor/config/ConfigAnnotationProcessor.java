/*
 * Copyright (c) 2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.processor.config;

import io.toolisticon.aptk.common.ToolingProvider;
import io.toolisticon.aptk.tools.MessagerUtils;
import org.riversoforion.lena.config.annotations.ExternalConfiguration;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Objects;
import java.util.Set;

@SupportedAnnotationTypes("org.riversoforion.lena.config.annotations.ExternalConfiguration")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ConfigAnnotationProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {

        super.init(processingEnv);
        ToolingProvider.setTooling(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (annotations.isEmpty()) {
            return false;
        }
        MessagerUtils.info(null, "processing annotations ${0}", annotations);
        roundEnv.getElementsAnnotatedWith(ExternalConfiguration.class)
                .stream()
                .map(ExternalConfigurationWrapper::wrap)
                .filter(Objects::nonNull)
                .forEach(this::processExternalConfiguration);
        return true;
    }


    private void processExternalConfiguration(ExternalConfigurationWrapper extConfigWrapper) {
        // TODO Validate and process annotation on type
        Element annotatedElement = extConfigWrapper._annotatedElement();
        MessagerUtils.info(annotatedElement, "processing class ${0}", annotatedElement);
        // Determine if type is interface or class
        ElementKind kind = annotatedElement.getKind();
        if (kind.isInterface()) {
            processConfigInterface(extConfigWrapper);
        } else if (kind.isClass()) {
            processConfigClass(extConfigWrapper);
        } else {
            MessagerUtils.error(annotatedElement, "invalid location for @ExternalConfiguration annotation: ${0}", kind);
        }
        // Validate type
        // Find getter methods
        // Build implementation class
    }

    private void processConfigInterface(ExternalConfigurationWrapper extConfigWrapper) {}

    private void processConfigClass(ExternalConfigurationWrapper extConfigWrapper) {}
}
