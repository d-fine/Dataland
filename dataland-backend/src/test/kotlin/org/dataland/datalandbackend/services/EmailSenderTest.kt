package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.email.Email
import org.dataland.datalandbackend.model.email.EmailContact
import org.dataland.datalandbackend.model.email.EmailContent
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [DatalandBackend::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class EmailSenderTest {
    @Test
    fun `check if an invalid mailjet server url causes a false result on email send`() {
        val emailContact = EmailContact("dev.null@dataland.com")
        val email = Email(emailContact, listOf(emailContact), listOf(), EmailContent("", "", "", listOf()))
        val emailSender = EmailSender("https://notmailjet.dataland.com")
        assertFalse(emailSender.sendEmail(email))
    }
}
