package org.dataland.datalandcommunitymanager

import org.dataland.datalandemail.email.Email
import org.dataland.datalandemail.email.EmailContact
import org.dataland.datalandemail.email.EmailContent
import org.dataland.datalandemail.email.EmailSender
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [DatalandCommunityManager::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class EmailSenderTest {
    @Test
    fun `check if an invalid mailjet server url causes a false result on email send`() {
        val emailContact = EmailContact("dev.null@dataland.com")
        val email = Email(emailContact, listOf(emailContact), listOf(), EmailContent("", "", ""))
        val emailSender = EmailSender("https://notmailjet.dataland.com")
        assertFalse(emailSender.sendEmail(email))
    }
}
