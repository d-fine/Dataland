package org.dataland.e2etests.auth

import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyAndMetaInfo
import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyMetaInfo
import org.dataland.datalandapikeymanager.openApiClient.model.RevokeApiKeyResponse
import org.dataland.e2etests.BASE_PATH_TO_API_KEY_MANAGER

class ApiKeyAuthenticationHelper {
    private val jwtHelper = JwtAuthenticationHelper()
    private val apiKeyManagerClient = ApiKeyControllerApi(BASE_PATH_TO_API_KEY_MANAGER)

    fun obtainApikeyForTechnicalUser(technicalUser: TechnicalUser, daysValid: Int? = null): ApiKeyAndMetaInfo {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        val apiKeyAndMetaInfo = apiKeyManagerClient.generateApiKey(daysValid)
        return apiKeyAndMetaInfo
    }

    fun authenticateApiCallsWithApiKeyForTechnicalUser
    (technicalUser: TechnicalUser, daysValid: Int? = null): ApiKeyAndMetaInfo {
        val metaInformation = obtainApikeyForTechnicalUser(technicalUser, daysValid)
        GlobalAuth.setBearerToken(metaInformation.apiKey)
        return metaInformation
    }

    fun revokeApiKeyForTechnicalUserAndResetAuthentication(technicalUser: TechnicalUser): RevokeApiKeyResponse {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        val revokeApiKeyResponse = apiKeyManagerClient.revokeApiKey()
        GlobalAuth.setBearerToken(null)
        return revokeApiKeyResponse
    }

    fun resetAuthenticationAndValidateApiKey(receivedApiKey: String): ApiKeyMetaInfo {
        GlobalAuth.setBearerToken(null)
        return apiKeyManagerClient.validateApiKey(receivedApiKey)
    }

    fun getApiKeyMetaInformationForTechnicalUser(technicalUser: TechnicalUser): ApiKeyMetaInfo {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        return apiKeyManagerClient.getApiKeyMetaInfoForUser()
    }
}
