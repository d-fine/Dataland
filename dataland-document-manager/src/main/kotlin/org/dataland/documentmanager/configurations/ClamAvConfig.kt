package org.dataland.documentmanager.configurations

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import xyz.capybara.clamav.ClamavClient

/**
 * Configuration providing ClamAV related beans
 */
@Configuration
class ClamAvConfig {
    /**
     * Provider for a ClamAV client
     * @returns a ClamAV client
     */
    @Bean
    fun getClamAvClient(@Value("\${dataland.clamav.base-url}") clamAvBaseUrl: String): ClamavClient {
        return ClamavClient(clamAvBaseUrl)
    }
}
