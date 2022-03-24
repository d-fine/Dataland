package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.SkyminderControllerApi
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SkyminderControllerTest {
    val skyminderControllerApi = SkyminderControllerApi(basePath = "http://proxy:80/api")

    @Test
    fun `get dummy company data by sending a request to dummy skyminder server`() {
        assertTrue(
            skyminderControllerApi.getDataSkyminderRequest(code = "dummy", name = "dummy").isNotEmpty(),
            "The dummy skyminder server is returning an empty response."
        )
    }
}
