package org.dataland.datalandcommunitymanager.configurations

import org.dataland.datalandcommunitymanager.converters.StringToDataTypeEnumConverter
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToDataTypeEnumConverter())
    }
}