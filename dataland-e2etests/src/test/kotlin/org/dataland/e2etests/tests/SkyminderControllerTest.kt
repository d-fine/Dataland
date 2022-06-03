package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.SkyminderControllerApi
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_PROXY
import org.dataland.e2etests.accessmanagement.TokenRequester
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SkyminderControllerTest {
    private val skyminderControllerApi = SkyminderControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val tokenRequester = TokenRequester()

    @Test
    fun `get dummy company data by sending a request to dummy skyminder server`() {
        tokenRequester.requestTokenForUserType(TokenRequester.UserType.SomeUser).setToken()
        assertTrue(
            skyminderControllerApi.getDataSkyminderRequest("dummy", "dummy").isNotEmpty(),
            "The dummy skyminder server is returning an empty response."
        )
    }
}
