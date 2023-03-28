package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.InviteControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.auth.ApiKeyAuthenticationHelper
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

class InviteControllerTest {
    private val inviteControllerApi = InviteControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val jwtHelper = JwtAuthenticationHelper()
    private val apiKeyHelper = ApiKeyAuthenticationHelper()

    @Test
    fun `post invite request with empty file and check if it gets rejected`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val file: File = File.createTempFile("test", ".xlsx")
        val inviteMetaInfoEntity = inviteControllerApi.submitInvite(file)
        assertFalse(inviteMetaInfoEntity.wasInviteSuccessful!!)
        assertTrue(inviteMetaInfoEntity.inviteResultMessage!!.contains("file is empty"))
        assertTrue(file.delete())
    }

    @Test
    fun `post invite request with file of invalid type and check if it gets rejected`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val file: File = File.createTempFile("test", ".png")
        file.writeText("this is content")
        val inviteMetaInfoEntity = inviteControllerApi.submitInvite(file)
        assertFalse(inviteMetaInfoEntity.wasInviteSuccessful!!)
        assertTrue(inviteMetaInfoEntity.inviteResultMessage!!.contains(".xlsx format"))
        assertTrue(file.delete())
    }

    @Test
    fun `post invite request authenticating via api key and check if it gets rejected`() {
        apiKeyHelper.authenticateApiCallsWithApiKeyForTechnicalUser(TechnicalUser.Uploader)
        val file: File = File.createTempFile("test", ".xlsx")
        val exception =
            assertThrows<ClientException> {
                inviteControllerApi.submitInvite(file)
            }
        val response = exception.response
        assertTrue(response is ClientError<*>)
        val clientError = response as ClientError<*>
        assertTrue(
            clientError.body?.let {
                it is String && it.contains("authentication-method-not-supported-for-this-request")
            } ?: false,
        )
    }
}
