package org.dataland.e2etests.auth

import org.dataland.datalandapikeymanager.openApiClient.infrastructure.ApiClient as ApiClientApiKeyManager
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient as ApiClientBackend

object GlobalAuth {
    fun setBearerToken(token: String?) {
        ApiClientApiKeyManager.Companion.accessToken = token
        ApiClientBackend.Companion.accessToken = token
    }
}
