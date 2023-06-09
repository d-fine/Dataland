package org.dataland.datalandbatchmanager.configurations

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Jackson {
    @Bean
    fun getObjectMapper(): ObjectMapper {
        return jacksonObjectMapper()
    }
}
