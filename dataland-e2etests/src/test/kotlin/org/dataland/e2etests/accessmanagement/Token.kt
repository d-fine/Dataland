package org.dataland.e2etests.accessmanagement

import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient

class Token(private val token: String) {

    fun setToken() {
        ApiClient.Companion.accessToken = token
    }
}
