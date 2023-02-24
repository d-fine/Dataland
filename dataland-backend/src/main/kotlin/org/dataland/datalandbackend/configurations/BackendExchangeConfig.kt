package org.dataland.datalandbackend.configurations

import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.dataland.datalandmessagequeueutils.cloudevents.InternalStorageExchangeConfig
import org.dataland.datalandmessagequeueutils.cloudevents.QualityAssuredExchangeConfig
import org.springframework.beans.factory.annotation.Autowired


@Configuration
class BackendBindingConfig2 (
    @Autowired exchange: InternalStorageExchangeConfig
){val dataStoredExchange = exchange.fanoutInternalStorage()

    @Bean
    fun fanoutBackend1(): FanoutExchange {
        return dataStoredExchange
    }

private class BindingConfig2 {

        @Bean
        fun autoDeleteQueue4(): Queue {
            return AnonymousQueue()
        }


        @Bean
        fun binding4(
            fanoutBackend1: FanoutExchange?,
            autoDeleteQueue4: Queue?
        ): Binding {
            return BindingBuilder.bind(autoDeleteQueue4).to(fanoutBackend1)
        }
    }
}



@Configuration
class BackendBindingConfig (
    @Autowired exchange: QualityAssuredExchangeConfig
){val dataQualityAssuredExchange = exchange.fanoutQaService()
    @Bean
    fun fanoutBackend2(): FanoutExchange {
        return dataQualityAssuredExchange
    }

    private class BindingConfig {
        @Bean
        fun autoDeleteQueue3(): Queue {
            return AnonymousQueue()
        }

        @Bean
        fun binding3(
            fanoutBackend2: FanoutExchange?,
            autoDeleteQueue3: Queue?
        ): Binding {
            return BindingBuilder.bind(autoDeleteQueue3).to(fanoutBackend2)
        }

    }


}