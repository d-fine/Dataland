package org.dataland.datalandinternalstorage.configurations

import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class InternalStorageBindingConfig (
){
    @Bean
    fun fanoutInternalStorage(): FanoutExchange {
        return FanoutExchange("dataStored")
    }
}
