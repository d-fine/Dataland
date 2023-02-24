package org.dataland.datalandinternalstorage.configurations

import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
class InternalStorageExchangeConfig {
    @Bean
    fun fanoutInternalStorage(): FanoutExchange {
        return FanoutExchange("dataStored")
    }


    class BindingConfig {
        @Bean
        fun autoDeleteQueue1(): Queue {
            return AnonymousQueue()

        }

        @Bean
        fun binding1(
            fanoutBackend1: FanoutExchange?,
            autoDeleteQueue1: Queue?
        ): Binding {
            //hier soll die Queue an den Exchange "dataReceived" des Backends gebunden werden
            return BindingBuilder.bind(autoDeleteQueue1).to(fanoutBackend1)
        }
    }
}