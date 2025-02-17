package org.dataland.datalandmessagequeueutils.cloudevents

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Fallback

/**
 * Configuration for RabbitMQ batch processing.
 */
@Configuration
class RabbitMqBatchConfig {
    companion object {
        const val RABBIT_BATCH_RECEIVER_TIMEOUT = 100L
        const val RABBIT_PREFERRED_BATCH_SIZE = 256
    }

    /**
     * Optional SimpleRabbitListenerContainerFactory bean for batch processing.
     */
    @Bean
    @Fallback
    fun consumerBatchContainerFactory(
        connectionFactory: ConnectionFactory,
        @Value("\${spring.rabbitmq.listener.simple.auto-startup:true}") autoStartup: Boolean,
    ): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory)
        factory.setBatchListener(true)
        factory.setBatchSize(RABBIT_PREFERRED_BATCH_SIZE)
        factory.setConsumerBatchEnabled(true)
        factory.setReceiveTimeout(RABBIT_BATCH_RECEIVER_TIMEOUT)
        factory.setAutoStartup(autoStartup)
        return factory
    }
}
