package org.dataland.e2etests.auth

import org.dataland.datalandapikeymanager.openApiClient.infrastructure.ApiClient as ApiClientApiKeyManager
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient as ApiClientBackend
import org.dataland.documentmanager.openApiClient.infrastructure.ApiClient as ApiClientDocumentManager
import org.dataland.datalandqaservice.openApiClient.infrastructure.ApiClient as ApiClientQaService

object GlobalAuth {
    fun setBearerToken(token: String?) {
        ApiClientApiKeyManager.Companion.accessToken = token
        ApiClientBackend.Companion.accessToken = token
        ApiClientDocumentManager.Companion.accessToken = token
        ApiClientQaService.Companion.accessToken = token
    }
}
