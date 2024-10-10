package org.dataland.datalandbatchmanager.configurations

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * The jackson object mapper class for reading CSV data into objects
 */
@Configuration
class Jackson {
    /**
     * The getter for the object mapper
     */
    @Bean
    fun getObjectMapper(): ObjectMapper = jacksonObjectMapper()
}
