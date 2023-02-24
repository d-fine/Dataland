package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.config

import org.dataland.datalandmessagequeueutils.cloudevents.InternalStorageExchangeConfig
import org.springframework.amqp.core.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class QaServiceBindingConfig(
    @Autowired exchange: InternalStorageExchangeConfig
){val dataStoredExchange = exchange.fanoutInternalStorage()
    @Bean
    fun fanoutQaService(): FanoutExchange {
        return dataStoredExchange
    }

    private class BindingConfig {
        @Bean
        fun autoDeleteQueue2(): Queue {
            return AnonymousQueue()
        }

        @Bean
        fun binding2(
            fanoutQaService: FanoutExchange?,
            autoDeleteQueue2: Queue?
        ): Binding {
            //hier soll die Queue an den dataStored Exchange gebunden werden
            return BindingBuilder.bind(autoDeleteQueue2).to(fanoutQaService)
        }
    }

}