package org.dataland.datalandmessagequeueutils.cloudevents

import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class ExchangeConfig2 {
    @Bean
    fun fanoutBackend12(): FanoutExchange {
        return FanoutExchange("dataReceived")
    }
}
@Configuration
class ExchangeConfig3 {
    @Bean
    fun fanoutInternalStorage12(): FanoutExchange {
        return FanoutExchange("dataStoredTest")
    }
}
/*
    @Bean
    fun fanoutInternalStorage12(): FanoutExchange {
        return FanoutExchange("dataStoredTest")
    }
    @Bean
    fun fanoutQaService12(): FanoutExchange {
        return FanoutExchange("dataQualityAssured")
    }
*/
