package org.dataland.datalandemailservice.email

import ch.qos.logback.classic.Level
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetResponse
import org.dataland.datalandbackend.utils.InMemoryLogAppender
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever

class EmailSenderTest {
    private var dummySender = EmailContact.create("sender@senderco.com")
    private var dummyReceiver1 = EmailContact.create("receiver1@receiverco1.com")
    private var dummyReceiver2 = EmailContact.create("receiver2@receiverco2.com")
    private var dummyCcReceiver = EmailContact.create("ccreceiver@ccreceiverco.com")
    private var dummySubject = "dummy-subject"

    private var emptySendEmailsResponseAsJsonString = "{\"messages\":[]}"

    private var dummyEmail =
        Email(
            sender = dummySender,
            receivers = listOf(dummyReceiver1, dummyReceiver2),
            cc = listOf(dummyCcReceiver),
            bcc = listOf<EmailContact>(),
            content = EmailContent(dummySubject, "", ""),
        )

    private var mockMailjetResponse = mock<MailjetResponse>()
    private var mockMailjetClient = mock<MailjetClient>()

    private lateinit var emailSender: EmailSender

    @BeforeEach
    fun setUp() {
        reset(
            mockMailjetResponse,
            mockMailjetClient,
        )

        doReturn(emptySendEmailsResponseAsJsonString).whenever(mockMailjetResponse).rawResponseContent
        doReturn(mockMailjetResponse).whenever(mockMailjetClient).post(anyOrNull())
    }

    @Test
    fun `check that an email is sent when dry run mode is off`() {
        val appender = InMemoryLogAppender().getAppender()
        emailSender =
            EmailSender(
                mailjetClient = mockMailjetClient,
                dryRunIsActive = false,
            )
        emailSender.sendEmail(dummyEmail)
        assertTrue(appender.contains("Email successfully sent.", Level.INFO))
    }

    @Test
    fun `check that no email is sent when dry run mode is on`() {
        emailSender =
            EmailSender(
                mailjetClient = mockMailjetClient,
                dryRunIsActive = true,
            )

        emailSender.sendEmail(dummyEmail)
    }
}
