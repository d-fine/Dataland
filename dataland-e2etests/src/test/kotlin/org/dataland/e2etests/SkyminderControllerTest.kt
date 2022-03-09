package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.DataControllerApi
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SkyminderControllerTest {
    val dataControllerApi = DataControllerApi(basePath = "http://proxy:80/api")

    @Test
    fun `get dummy company data by sending a request to dummy skyminder server`() {
        assertTrue(
            dataControllerApi.getDataSkyminderRequest(code = "dummy", companyName = "dummy").isNotEmpty(),
            "The dummy skyminder server is returning an empty response."
        )
    }
}
