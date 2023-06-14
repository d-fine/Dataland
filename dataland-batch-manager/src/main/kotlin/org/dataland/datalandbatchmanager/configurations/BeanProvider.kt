package org.dataland.datalandbatchmanager.configurations

import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * The provider for the OkHttpClient bean
 */
@Configuration
class BeanProvider {
    /**
     * The getter for an OkHttpClient
     */
    @Bean
    fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient()
    }
}
