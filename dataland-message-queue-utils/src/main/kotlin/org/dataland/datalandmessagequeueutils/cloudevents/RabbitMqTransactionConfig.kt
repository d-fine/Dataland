package org.dataland.datalandmessagequeueutils.cloudevents

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Configuration

/**
 * Configures RabbitMQ to use transactions
 */
@Configuration
class RabbitMqTransactionConfig(
    rabbitTemplate: RabbitTemplate,
) {
    init {
        rabbitTemplate.isChannelTransacted = true
    }
}
