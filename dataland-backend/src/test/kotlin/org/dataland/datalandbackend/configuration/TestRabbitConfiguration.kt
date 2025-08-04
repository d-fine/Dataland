package org.dataland.datalandbackend.configuration

import org.mockito.Mockito
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

/**
 * Test configuration that provides mock RabbitMQ-related beans for tests
 * when RabbitMQ is disabled but some beans still need these dependencies
 */
@TestConfiguration
@Profile("test")
class TestRabbitConfiguration {

    @Bean
    @Primary
    fun mockRabbitTemplate(): RabbitTemplate {
        return Mockito.mock(RabbitTemplate::class.java)
    }

    @Bean
    @Primary  
    fun mockAmqpTemplate(): AmqpTemplate {
        return Mockito.mock(AmqpTemplate::class.java)
    }
}