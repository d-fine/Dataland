package org.dataland.datalandcompanyservice.configurations

import com.openai.azure.credential.AzureApiKeyCredential
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import java.time.Duration

/**
 * Provides the [OpenAIClient] used to call the Responses API of the Azure OpenAI deployment.
 * Only active for the "azure" provider.
 */
@Configuration
@ConditionalOnProperty(name = ["dataland.company-service.provider"], havingValue = "azure")
class AzureOpenAiClientConfiguration {
    /**
     * Builds the OpenAI client for Azure. The endpoint must end with /openai/v1, so that the Azure v1 API is used
     * and no api-version query parameter is required. Authentication is done via the api-key header.
     */
    @Bean(destroyMethod = "close")
    @DependsOn("azureOpenAiConfigurationValidator")
    fun azureOpenAiClient(
        @Value("\${dataland.company-service.azure.endpoint}") endpoint: String,
        @Value("\${dataland.company-service.azure.api-key}") apiKey: String,
        @Value("\${dataland.company-service.azure.max-research-duration}") timeout: Duration,
        @Value("\${dataland.company-service.azure.max-retries}") maxRetries: Int,
    ): OpenAIClient =
        OpenAIOkHttpClient
            .builder()
            .baseUrl(endpoint.trim().trimEnd('/'))
            .credential(AzureApiKeyCredential.create(apiKey))
            .timeout(timeout)
            .maxRetries(maxRetries)
            .build()
}
