package org.dataland.datalandcompanyservice.configurations

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Fails the application start if the "azure" proxy suggestion provider is selected
 * but the Azure OpenAI connection settings are incomplete.
 */
@Component
@ConditionalOnProperty(name = ["dataland.company-service.provider"], havingValue = "azure")
class AzureOpenAiConfigurationValidator(
    @Value("\${dataland.company-service.azure.endpoint:}") private val endpoint: String,
    @Value("\${dataland.company-service.azure.api-key:}") private val apiKey: String,
    @Value("\${dataland.company-service.azure.deployment-name:}") private val deploymentName: String,
) {
    init {
        val missing =
            listOf(
                "AZURE_OPENAI_ENDPOINT" to endpoint,
                "AZURE_OPENAI_API_KEY" to apiKey,
                "AZURE_OPENAI_DEPLOYMENT_NAME" to deploymentName,
            ).filter { it.second.isBlank() }.map { it.first }
        check(missing.isEmpty()) {
            "COMPANY_SERVICE_PROVIDER is 'azure' but the following settings are missing: ${missing.joinToString()}"
        }
    }
}
