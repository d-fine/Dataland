package org.dataland.documentmanager.configurations

import org.dataland.documentmanager.services.conversion.mockClamAvClient
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestClamAvClientConfig {
    @Bean
    fun getClamAvClient() = mockClamAvClient()
}
