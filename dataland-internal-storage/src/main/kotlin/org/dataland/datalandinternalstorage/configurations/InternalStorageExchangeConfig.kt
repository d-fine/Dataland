package org.dataland.datalandinternalstorage.configurations

import org.dataland.datalandmessagequeueutils.cloudevents.BackendExchangeConfig
import org.springframework.amqp.core.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
class InternalStorageBindingConfig (
    @Autowired exchange: BackendExchangeConfig
){val dataReceivedExchange = exchange.fanoutBackend()
    @Bean
    fun fanoutInternalStorage(): FanoutExchange {
        return dataReceivedExchange
    }


    class BindingConfig {
        @Bean
        fun autoDeleteQueue1(): Queue {
            return AnonymousQueue()

        }

        @Bean
        fun binding1(
            fanoutInternalStorage: FanoutExchange?,
            autoDeleteQueue1: Queue?
        ): Binding {
            //hier soll die Queue an den Exchange "dataReceived" des Backends gebunden werden
            return BindingBuilder.bind(autoDeleteQueue1).to(fanoutInternalStorage)
        }
    }
}