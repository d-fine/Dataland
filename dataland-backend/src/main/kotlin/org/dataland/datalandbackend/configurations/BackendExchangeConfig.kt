package org.dataland.datalandbackend.configurations

import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class BackendBindingConfig2 (
) {
    @Bean
    fun fanoutBackend1(): FanoutExchange {
        return FanoutExchange("dataReceived")
    }
}
