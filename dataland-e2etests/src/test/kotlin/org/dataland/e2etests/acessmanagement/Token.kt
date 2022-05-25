package org.dataland.e2etests.acessmanagement

import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient

class Token(token: String) {
    private val token = token

    fun setToken() {
        ApiClient.Companion.accessToken = token
    }
}
