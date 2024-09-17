package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.UserUploadsControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformationForMyDatasets
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserUploadsControllerTest {

    val jwtHelper = JwtAuthenticationHelper()
    private fun getUserUploads(userId: String, technicalUser: TechnicalUser): List<DataMetaInformationForMyDatasets> {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(technicalUser)
        return UserUploadsControllerApi(BASE_PATH_TO_DATALAND_BACKEND).getUserUploadsDataMetaInformation(userId)
    }

    @Test
    fun `check if a user can see uploads of another user`() {
        assertThrows<ClientException> {
            getUserUploads(TechnicalUser.Admin.technicalUserId, TechnicalUser.Uploader)
        }
    }

    @Test
    fun `check that a reading user has no uploads`() {
        val shouldBeEmpty = getUserUploads(TechnicalUser.Reviewer.technicalUserId, TechnicalUser.Reviewer)
        assertTrue(shouldBeEmpty.isEmpty())
    }
}
