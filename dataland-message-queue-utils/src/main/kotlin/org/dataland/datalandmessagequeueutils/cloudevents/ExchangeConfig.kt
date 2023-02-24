package org.dataland.datalandmessagequeueutils.cloudevents

import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class BackendExchangeConfig {
    @Bean
    fun fanoutBackend(): FanoutExchange {
        return FanoutExchange("dataReceived")
    }
}
@Configuration
class InternalStorageExchangeConfig {
    @Bean
    fun fanoutInternalStorage(): FanoutExchange {
        return FanoutExchange("dataStored")
    }
}
@Configuration
class QualityAssuredExchangeConfig {
    @Bean
    fun fanoutQaService(): FanoutExchange {
        return FanoutExchange("dataQualityAssured")
    }
}
