package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.SkyminderControllerApi
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SkyminderControllerTest {
    private val skyminderControllerApi = SkyminderControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val tokenHandler = TokenHandler()

    @Test
    fun `get dummy company data by sending a request to dummy skyminder server`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.SomeUser)
        assertTrue(
            skyminderControllerApi.getDataSkyminderRequest("dummy", "dummy").isNotEmpty(),
            "The dummy skyminder server is returning an empty response."
        )
    }
}
