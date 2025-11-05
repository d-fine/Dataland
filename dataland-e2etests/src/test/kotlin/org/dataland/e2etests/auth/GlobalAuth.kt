package org.dataland.e2etests.auth

import org.dataland.communitymanager.openApiClient.infrastructure.ApiClient as ApiClientCommunityManager
import org.dataland.dataSourcingService.openApiClient.infrastructure.ApiClient as ApiClientDataSourcingService
import org.dataland.datalandapikeymanager.openApiClient.infrastructure.ApiClient as ApiClientApiKeyManager
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient as ApiClientBackend
import org.dataland.datalandqaservice.openApiClient.infrastructure.ApiClient as ApiClientQaService
import org.dataland.documentmanager.openApiClient.infrastructure.ApiClient as ApiClientDocumentManager
import org.dataland.userService.openApiClient.infrastructure.ApiClient as ApiClientUserService
import org.dataland.accountingService.openApiClient.infrastructure.ApiClient as ApiClientAccountingService

object GlobalAuth {
    val jwtHelper = JwtAuthenticationHelper()

    fun setBearerToken(token: String?) {
        ApiClientApiKeyManager.accessToken = token
        ApiClientBackend.accessToken = token
        ApiClientDocumentManager.accessToken = token
        ApiClientQaService.accessToken = token
        ApiClientCommunityManager.accessToken = token
        ApiClientUserService.accessToken = token
        ApiClientDataSourcingService.accessToken = token
        ApiClientAccountingService.accessToken = token
    }

    inline fun <T> withToken(
        token: String?,
        block: () -> T,
    ): T {
        val oldToken = ApiClientApiKeyManager.accessToken
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
