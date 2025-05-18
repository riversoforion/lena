/*
 * Copyright (c) 2025. Eric McIntyre / Rivers of Orion
 */
package org.riversoforion.lena.processor.config;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes("org.riversoforion.lena.config.annotations.ExternalConfiguration")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ConfigAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        return false;
    }
}
