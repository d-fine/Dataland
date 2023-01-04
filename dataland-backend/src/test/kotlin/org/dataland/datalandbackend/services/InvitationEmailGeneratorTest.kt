package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.utils.InvitationEmailGenerator
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile

@SpringBootTest(classes = [DatalandBackend::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class InvitationEmailGeneratorTest {
    @Test
    fun `check if an invalid mailjet server url causes a false result on email send`() {
        val file = MockMultipartFile("test.xlsx", "this is content".toByteArray())
        Mockito.mockStatic(System::class.java, Mockito.CALLS_REAL_METHODS).use {
                mocked ->
            mocked.`when`<String> { System.getenv() }.thenReturn("")
            assertThrows< InternalServerErrorApiException> { InvitationEmailGenerator.generate(file, false) }
        }
    }
}
