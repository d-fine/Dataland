package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.SkyminderControllerApi
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SkyminderControllerTest {
    private val skyminderControllerApi = SkyminderControllerApi(BASE_PATH_TO_DATALAND_PROXY)

    @Test
    fun `get dummy company data by sending a request to dummy skyminder server`() {
        assertTrue(
            skyminderControllerApi.getDataSkyminderRequest("dummy", "dummy").isNotEmpty(),
            "The dummy skyminder server is returning an empty response."
        )
    }
}
