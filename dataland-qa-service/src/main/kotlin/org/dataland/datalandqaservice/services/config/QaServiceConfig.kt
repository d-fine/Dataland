package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.config

import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class QaServiceBindingConfig(
){
    @Bean
    fun fanoutQaService(): FanoutExchange {
        return FanoutExchange("dataQualityAssured")
    }
}