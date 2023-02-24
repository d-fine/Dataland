package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.config

import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class QaServiceConfig {
    @Bean
    fun fanoutQaService(): FanoutExchange {
        return FanoutExchange("dataQualityAssured")
    }

    private class BindingConfig {
        @Bean
        fun autoDeleteQueue2(): Queue {
            return AnonymousQueue()
        }

        @Bean
        fun binding2(
            fanoutInternalStorage: FanoutExchange?,
            autoDeleteQueue2: Queue?
        ): Binding {
            //hier soll die Queue an den dataStored Exchange gebunden werden
            return BindingBuilder.bind(autoDeleteQueue2).to(fanoutInternalStorage)
        }
    }

}