package org.dataland.datalandproxyfinder.configurations

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Fails the application start if the "azure" proxy suggestion provider is selected
 * but the Azure OpenAI connection settings are incomplete.
 */
@Component
@ConditionalOnProperty(name = ["dataland.proxy-finder.provider"], havingValue = "azure")
class AzureOpenAiConfigurationValidator(
    @Value("\${spring.ai.openai.base-url:}") private val endpoint: String,
    @Value("\${spring.ai.openai.api-key:}") private val apiKey: String,
    @Value("\${spring.ai.openai.chat.model:}") private val deploymentName: String,
) {
    init {
        val missing =
            listOf(
                "AZURE_OPENAI_ENDPOINT" to endpoint,
                "AZURE_OPENAI_API_KEY" to apiKey,
                "AZURE_OPENAI_DEPLOYMENT_NAME" to deploymentName,
            ).filter { it.second.isBlank() }.map { it.first }
        check(missing.isEmpty()) {
            "PROXY_FINDER_PROVIDER is 'azure' but the following settings are missing: ${missing.joinToString()}"
        }
    }
}
