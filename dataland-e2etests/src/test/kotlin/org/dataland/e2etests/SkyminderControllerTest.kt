package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.SkyminderControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SkyminderControllerTest {
    private val skyminderControllerApi = SkyminderControllerApi(BASE_PATH_TO_DATALAND_PROXY)

    init {
        ApiClient.Companion.accessToken = "TODO: Add Access Token here!"
    }

    @Test
    fun `get dummy company data by sending a request to dummy skyminder server`() {
        assertTrue(
            skyminderControllerApi.getDataSkyminderRequest("dummy", "dummy").isNotEmpty(),
            "The dummy skyminder server is returning an empty response."
        )
    }
}
