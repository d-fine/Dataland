package org.dataland.datalandemailservice.configurations

import org.dataland.datalandemailservice.converters.StringToDataTypeEnumConverter
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * This class is used to configure the web server started by Spring
 */
@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToDataTypeEnumConverter())
    }
}
