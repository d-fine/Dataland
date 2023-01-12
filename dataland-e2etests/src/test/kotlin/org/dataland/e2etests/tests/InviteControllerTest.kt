package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.InviteControllerApi
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.dataland.e2etests.utils.UserType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class InviteControllerTest {
    private val inviteControllerApi = InviteControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val tokenHandler = TokenHandler()

    @Test
    fun `post invite request with empty file and check if it gets rejected`() {
        tokenHandler.obtainTokenForUserType(UserType.Uploader)
        val file: File = File.createTempFile("test", ".xlsx")
        val inviteMetaInfoEntity = inviteControllerApi.submitInvite(false, file)
        assertFalse(inviteMetaInfoEntity.wasInviteSuccessful!!)
        assertTrue(inviteMetaInfoEntity.inviteResultMessage!!.contains("file is empty"))
        assertTrue(file.delete())
    }

    @Test
    fun `post invite request with file of invalid type and check if it gets rejected`() {
        tokenHandler.obtainTokenForUserType(UserType.Uploader)
        val file: File = File.createTempFile("test", ".png")
        file.writeText("this is content")
        val inviteMetaInfoEntity = inviteControllerApi.submitInvite(false, file)
        assertFalse(inviteMetaInfoEntity.wasInviteSuccessful!!)
        assertTrue(inviteMetaInfoEntity.inviteResultMessage!!.contains(".xlsx format"))
        assertTrue(file.delete())
    }
}
