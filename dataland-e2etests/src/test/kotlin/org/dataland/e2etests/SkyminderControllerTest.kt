package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.SkyminderControllerApi
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SkyminderControllerTest {
    private val basePathToDatalandProxy = "http://proxy:80/api"
    private val skyminderControllerApi = SkyminderControllerApi(basePathToDatalandProxy)

    @Test
    fun `get dummy company data by sending a request to dummy skyminder server`() {
        assertTrue(
            skyminderControllerApi.getDataSkyminderRequest("dummy", "dummy").isNotEmpty(),
            "The dummy skyminder server is returning an empty response."
        )
    }
}
