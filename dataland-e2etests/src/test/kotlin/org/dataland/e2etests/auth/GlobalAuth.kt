package org.dataland.e2etests.auth

import org.dataland.communitymanager.openApiClient.infrastructure.ApiClient as ApiClientCommunityManager
import org.dataland.dataSourcingService.openApiClient.infrastructure.ApiClient as ApiClientDataSourcingService
import org.dataland.datalandapikeymanager.openApiClient.infrastructure.ApiClient as ApiClientApiKeyManager
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient as ApiClientBackend
import org.dataland.datalandqaservice.openApiClient.infrastructure.ApiClient as ApiClientQaService
import org.dataland.documentmanager.openApiClient.infrastructure.ApiClient as ApiClientDocumentManager
import org.dataland.userService.openApiClient.infrastructure.ApiClient as ApiClientUserService

object GlobalAuth {
    val jwtHelper = JwtAuthenticationHelper()

    fun setBearerToken(token: String?) {
        ApiClientApiKeyManager.Companion.accessToken = token
        ApiClientBackend.Companion.accessToken = token
        ApiClientDocumentManager.Companion.accessToken = token
        ApiClientQaService.Companion.accessToken = token
        ApiClientCommunityManager.Companion.accessToken = token
        ApiClientUserService.Companion.accessToken = token
        ApiClientDataSourcingService.Companion.accessToken = token
    }

    inline fun <T> withToken(
        token: String?,
        block: () -> T,
    ): T {
        val oldToken = ApiClientApiKeyManager.Companion.accessToken
        setBearerToken(token)
        try {
            return block()
        } finally {
            setBearerToken(oldToken)
        }
    }

    inline fun <T> withTechnicalUser(
        technicalUser: TechnicalUser,
        block: () -> T,
    ): T =
        withToken(
            jwtHelper
                .obtainJwtForTechnicalUser(technicalUser),
            block,
        )
}
