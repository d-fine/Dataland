package org.dataland.datalandemailservice.configurations

import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * This class holds the configuration properties and the beans to auto-configure mailjet
 */
@Configuration
open class ConfigurationMailjet(
    @Value("\${mailjet.api.url}") private val mailjetApiUrl: String,
) {
    /**
     * The bean to configure the general purpose Mailjet client
     */
    @Bean
    open fun getMailjetClient(): MailjetClient {
        val clientOptions =
            ClientOptions
                .builder()
                .baseUrl(mailjetApiUrl)
                .apiKey(System.getenv("MAILJET_API_ID"))
                .apiSecretKey(System.getenv("MAILJET_API_SECRET"))
                .build()
        return MailjetClient(clientOptions)
    }
}
