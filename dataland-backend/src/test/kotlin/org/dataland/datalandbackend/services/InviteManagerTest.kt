package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.InviteMetaInfoEntity
import org.dataland.datalandbackend.model.email.Email
import org.dataland.datalandbackend.repositories.InviteMetaInfoRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

@SpringBootTest(classes = [DatalandBackend::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class InviteManagerTest {
    @Test
    fun `check for correct email error handling`() {
        val mockEmailSender = Mockito.mock(EmailSender::class.java)
        val mockInviteMetaInfoRepository = Mockito.mock(InviteMetaInfoRepository::class.java)

        Mockito.`when`(mockEmailSender.sendEmail(any(Email::class.java))).thenReturn(
            false
        )

        Mockito.`when`(mockInviteMetaInfoRepository.save(any(InviteMetaInfoEntity::class.java)))
            .thenAnswer { invocation -> invocation.arguments[0] as InviteMetaInfoEntity }

        val inviteManager = InviteManager(mockEmailSender, mockInviteMetaInfoRepository)
        val file: MultipartFile = MockMultipartFile("test.xlsx", "this is content".toByteArray())
        val inviteMetaInfo = inviteManager.submitInvitation(file, false)

        assertFalse(inviteMetaInfo.wasInviteSuccessful)
        assertTrue(inviteMetaInfo.inviteResultMessage.contains("sending an email"))
    }
}
