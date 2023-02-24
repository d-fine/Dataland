package org.dataland.datalandbackend.configurations

import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.dataland.datalandmessagequeueutils.cloudevents.ExchangeConfig2
import org.dataland.datalandmessagequeueutils.cloudevents.ExchangeConfig3
import org.springframework.beans.factory.annotation.Autowired


@Configuration
class BackendExchangeConfig2 (
    @Autowired Exchange: ExchangeConfig3
        ){
    val dataStoredExchange = Exchange.fanoutInternalStorage12()
    /*@Bean
    fun fanoutBackend1(): FanoutExchange {
        return FanoutExchange("dataStored")
    }*/

private class BindingConfig {

        @Bean
        fun autoDeleteQueue4(): Queue {
            return AnonymousQueue()
        }


        @Bean
        fun binding4(
            dataStoredExchange: FanoutExchange?,
            autoDeleteQueue4: Queue?
        ): Binding {
            return BindingBuilder.bind(autoDeleteQueue4).to(dataStoredExchange)
        }
    }


}
/*
@Configuration
class BackendExchangeConfig {
    @Bean
    fun fanoutBackend2(): FanoutExchange {
        return FanoutExchange("dataQualityAssured")
    }

    private class BindinjgConfig {
        @Bean
        fun autoDeleteQueue3(): Queue {
            return AnonymousQueue()
        }

        @Bean
        fun binding3(
            fanoutQaService: FanoutExchange?,
            fanoutBackend2: Queue?
        ): Binding {
            return BindingBuilder.bind(autoDeleteQueue3).to(fanoutBackend2)
        }

    }


}*/