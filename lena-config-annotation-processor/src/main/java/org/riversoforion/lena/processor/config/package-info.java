/*
 * Copyright (c) 2025. Eric McIntyre / Rivers of Orion
 */
@AnnotationWrapper({
        ExternalConfiguration.class,
        ExternalConfigurationSource.class,
        CustomConfigurationSource.class,
})
package org.riversoforion.lena.processor.config;

import io.toolisticon.aptk.annotationwrapper.api.AnnotationWrapper;
import org.riversoforion.lena.config.annotations.CustomConfigurationSource;
import org.riversoforion.lena.config.annotations.ExternalConfiguration;
import org.riversoforion.lena.config.annotations.ExternalConfigurationSource;
